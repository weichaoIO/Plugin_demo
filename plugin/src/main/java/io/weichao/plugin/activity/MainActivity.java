package io.weichao.plugin.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        Toast.makeText(this, "插件MainActivity#onCreate()", Toast.LENGTH_SHORT).show();
    }
}