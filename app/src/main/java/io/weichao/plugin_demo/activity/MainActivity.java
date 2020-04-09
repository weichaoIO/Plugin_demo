package io.weichao.plugin_demo.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

import io.weichao.Message;
import io.weichao.plugin_demo.R;
import io.weichao.plugin_demo.util.LoadUtil;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final String PLUGIN_APK_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "plugin-debug.apk";

    private View mBtn2;
    private View mBtn22;
    private View mBtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(this);
//        findViewById(R.id.btn21).setOnClickListener(this);
        mBtn2 = findViewById(R.id.btn2);
        mBtn2.setOnClickListener(null);
//        mBtn22 = findViewById(R.id.btn22);
//        mBtn22.setOnClickListener(this);
        mBtn3 = findViewById(R.id.btn3);
        mBtn3.setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        boolean success;
        String text;
        switch (v.getId()) {
            case R.id.btn1:
                success = LoadUtil.loadPluginDex(this, PLUGIN_APK_PATH, LoadUtil.LOAD_METHOD_PLUGIN);
                if (success) {
                    Toast.makeText(this, "加载插件成功", Toast.LENGTH_SHORT).show();
                    mBtn2.setOnClickListener(MainActivity.this);
                    mBtn3.setOnClickListener(MainActivity.this);
                } else {
                    Toast.makeText(this, "加载插件失败", Toast.LENGTH_SHORT).show();
                }
                break;
//            case R.id.btn21:
//                success = loadPluginDex(PLUGIN_APK_PATH, LoadUtil.LOAD_METHOD_HOTFIX);
//                if (success) {
//                    Toast.makeText(this, "加载插件成功", Toast.LENGTH_SHORT).show();
//                    mBtn22.setClickable(true);
//                } else {
//                    Toast.makeText(this, "加载插件失败", Toast.LENGTH_SHORT).show();
//                }
//                break;
            case R.id.btn2:
                text = loadPluginMethod();
                if (!TextUtils.isEmpty(text)) {
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
                break;
//            case R.id.btn22:
//                text = loadMessage();
//                if (!TextUtils.isEmpty(text)) {
//                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
//                }
//                break;
            case R.id.btn3:
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("io.weichao.plugin", "io.weichao.plugin.activity.MainActivity"));
                startActivity(intent);
                break;
        }
    }

    private String loadPluginMethod() {
        try {
            Class<?> threadClazz = Class.forName("io.weichao.plugin.util.PluginUtil");
            Method method = threadClazz.getMethod("getMessage");
            return (String) method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String loadMessage() {
        Message message = new Message();
        return message.getMessage();
    }
}