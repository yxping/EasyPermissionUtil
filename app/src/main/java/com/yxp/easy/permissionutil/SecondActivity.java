package com.yxp.easy.permissionutil;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yxp.permission.util.lib.PermissionUtil;
import com.yxp.permission.util.lib.callback.PermissionResultAdapter;

/**
 * Created by yanxing on 16/1/19.
 */
public class SecondActivity extends Activity {
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.getInstance().request(new String[]{Manifest.permission.CAMERA},new PermissionResultAdapter() {

                    @Override
                    public void onPermissionGranted(String... permissions) {
                        Toast.makeText(SecondActivity.this, permissions[0] + " is granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String... permissions) {
                        Toast.makeText(SecondActivity.this, permissions[0] + " is denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRationalShow(String... permissions) {
                        Toast.makeText(SecondActivity.this, permissions[0] + " is rational", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}
