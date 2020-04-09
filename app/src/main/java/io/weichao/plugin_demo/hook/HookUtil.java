package io.weichao.plugin_demo.hook;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class HookUtil {
    private static final String TARGET_INTENT = "target_intent";

    public static void hookAMS() {
        try {
            // 获取 Singleton<T> 类的对象
            Field gDefaultField = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> activityManager = Class.forName("android.app.ActivityManager");
                gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
            } else {
                Class<?> activityManager = Class.forName("android.app.ActivityManagerNative");
                gDefaultField = activityManager.getDeclaredField("gDefault");
            }
            gDefaultField.setAccessible(true);
            Object singleton = gDefaultField.get(null);

            // 获取 mInstance 对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            // 创建代理对象
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            /**
                             * int result = ActivityManager.getService()
                             *                 .startActivity(whoThread, who.getBasePackageName(), intent,
                             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                             *                         token, target != null ? target.mEmbeddedID : null,
                             *                         requestCode, 0, null, options);
                             */
                            // 当执行的方法是 startActivity 时作处理
                            if ("startActivity".equals(method.getName())) {
                                int index = 0;

                                // 获取插件的 intent
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                Intent intent = (Intent) args[index];

                                // 创建代理的 intent
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("io.weichao.plugin_demo", ProxyActivity.class.getName());
                                // 保存插件的 intent 到代理的 intent 中
                                proxyIntent.putExtra(TARGET_INTENT, intent);

                                // 将插件的 intent 替换为代理的 intent
                                args[index] = proxyIntent;
                            }

                            // IActivityManager 对象 --- 通过反射
                            return method.invoke(mInstance, args);
                        }
                    });

            // 使用代理对象替换原有的 mInstance 对象
            mInstanceField.set(singleton, proxyInstance);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("aaaaa", "aaaaa" + e.getMessage());
        }
    }

    public static void hookHandler() {
        try {
            // 获取 ActivityThread 对象
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThread = sCurrentActivityThreadField.get(null);

            // 获取 Handler 对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Object mH = mHField.get(activityThread);

            // 创建一个 Callback 替换系统的 Callback 对象
            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    switch (msg.what) {
                        case 100:
                            try {
                                // 获取保存在代理的 intent 中的插件的 intent
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                // 判断调用的是否是插件的，如果不是插件的，intent 就会为空
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("aaaaa", "aaaaa" + e.getMessage());
                            }
                            break;
                        case 159: {
                            try {
                                Class<?> clazz = Class.forName("android.app.servertransaction.ClientTransaction");
                                Field mActivityCallbacksField = clazz.getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);

                                List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);
                                if (mActivityCallbacks != null) {
                                    for (int i = 0; i < mActivityCallbacks.size(); i++) {
                                        if ("android.app.servertransaction.LaunchActivityItem".equals(mActivityCallbacks.get(i).getClass().getCanonicalName())) {
                                            Object launchActivityItem = mActivityCallbacks.get(i);
                                            Field mIntentField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                            mIntentField.setAccessible(true);
                                            Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);
                                            Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                            // 判断调用的是否是插件的，如果不是插件的，intent 就会为空
                                            if (intent != null) {
                                                mIntentField.set(launchActivityItem, intent);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("aaaaa", "aaaaa" + e.getMessage());
                            }
                        }
                        break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("aaaaa", "aaaaa" + e.getMessage());
        }
    }
}