package io.weichao.plugin.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.lang.reflect.Field;

import io.weichao.plugin.R;
import io.weichao.plugin.util.LoadUtil;

public class MainActivity extends AppCompatActivity {
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resource = LoadUtil.getResource(getApplication());
        mContext = new ContextThemeWrapper(getBaseContext(), 0);
        Class<? extends Context> clazz = mContext.getClass();
        try {
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(mContext, resource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        setContentView(view);

        Toast.makeText(this, "插件MainActivity#onCreate()", Toast.LENGTH_SHORT).show();
    }
}