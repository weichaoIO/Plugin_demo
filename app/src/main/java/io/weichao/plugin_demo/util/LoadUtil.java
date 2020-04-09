package io.weichao.plugin_demo.util;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadUtil {
    public static final int LOAD_METHOD_PLUGIN = 1;
    public static final int LOAD_METHOD_HOTFIX = 2;

    public static boolean loadPluginDex(Context context, String apkPath, int method) {
        try {
            // 1.获取 pathList 的字段
            Class baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoader.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // 2.获取 DexClassLoader 类中的属性 pathList 的值
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(), null, context.getClassLoader());
            Object pluginPathList = pathListField.get(dexClassLoader);

            // 3.获取 pathList 中的属性 dexElements[] 的值--- 插件的 dexElements[]
            Class pluginPathListClass = pluginPathList.getClass();
            Field pluginDexElementsField = pluginPathListClass.getDeclaredField("dexElements");
            pluginDexElementsField.setAccessible(true);
            Object[] pluginDexElements = (Object[]) pluginDexElementsField.get(pluginPathList);

            // 4.获取 PathClassLoader 类中的属性 pathList 的值
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Object hostPathList = pathListField.get(pathClassLoader);

            // 5.获取 pathList 中的属性 dexElements[] 的值--- 宿主的 dexElements[]
            Class hostPathListClass = hostPathList.getClass();
            Field hostDexElementsField = hostPathListClass.getDeclaredField("dexElements");
            hostDexElementsField.setAccessible(true);
            Object[] hostDexElements = (Object[]) hostDexElementsField.get(hostPathList);

            // 6.创建一个新的空数组，第一个参数是数组的类型，第二个参数是数组的长度
            Object[] dexElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(), pluginDexElements.length + hostDexElements.length);

            // 7.将插件和宿主的 dexElements[] 的值放入新的数组中
            switch (method) {
                case LOAD_METHOD_PLUGIN:
                    System.arraycopy(pluginDexElements, 0, dexElements, 0, pluginDexElements.length);
                    System.arraycopy(hostDexElements, 0, dexElements, pluginDexElements.length, hostDexElements.length);
                    break;
                case LOAD_METHOD_HOTFIX:
                    System.arraycopy(hostDexElements, 0, dexElements, 0, hostDexElements.length);
                    System.arraycopy(pluginDexElements, 0, dexElements, hostDexElements.length, pluginDexElements.length);
                    break;
            }

            hostDexElementsField.set(hostPathList, dexElements);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}