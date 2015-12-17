package com.yxp.easy.permissionutil;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yxp.permission.util.lib.PermissionResultCallBack;
import com.yxp.permission.util.lib.PermissionUtil;


public class MainActivity extends AppCompatActivity {
    private Button mSingleBtn, mMultiBtn;
    private int mRequestCode = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSingleBtn = (Button) findViewById(R.id.single_btn);
        mMultiBtn = (Button) findViewById(R.id.multi_btn);
        mMultiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS}, mRequestCode,
                        new PermissionResultCallBack() {
                            @Override
                            public void onPermissionGranted() {
                                Toast.makeText(MainActivity.this, "multi granted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onRationalShow(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " show Rational", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        mSingleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, mRequestCode,
                        new PermissionResultCallBack() {
                            @Override
                            public void onPermissionGranted() {
                                Toast.makeText(MainActivity.this, "single granted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onRationalShow(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " show Rational", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
