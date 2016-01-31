package com.yxp.easy.permissionutil;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yxp.permission.util.lib.PermissionInfo;
import com.yxp.permission.util.lib.PermissionUtil;
import com.yxp.permission.util.lib.callback.PermissionOriginResultCallBack;
import com.yxp.permission.util.lib.callback.PermissionResultCallBack;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button mSingleBtn;
    private Button mMultiBtn;
    private Button mCheckSingleBtn;
    private Button mCheckMultiBtn;
    private Button mSecondBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSingleBtn = (Button) findViewById(R.id.single_btn);
        mMultiBtn = (Button) findViewById(R.id.multi_btn);
        mCheckMultiBtn = (Button) findViewById(R.id.check_multi_btn);
        mCheckSingleBtn = (Button) findViewById(R.id.check_single_btn);
        mSecondBtn = (Button) findViewById(R.id.second_btn);
        mMultiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS},
                        new PermissionResultCallBack() {
                            @Override
                            public void onPermissionGranted() {
                                Toast.makeText(MainActivity.this, "all granted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionGranted(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " is granted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(String... permissions) {
                                StringBuilder builder = new StringBuilder();
                                for (String permission : permissions) {
                                    builder.append(permission.substring(permission.lastIndexOf(".") + 1) + " ");
                                }
                                Toast.makeText(MainActivity.this, builder.toString() + " is denied", Toast.LENGTH_SHORT).show();
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
                PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR},
                        new PermissionOriginResultCallBack() {

                            @Override
                            public void onResult(List<PermissionInfo> acceptList, List<PermissionInfo> rationalList, List<PermissionInfo> deniedList) {
                                if (!acceptList.isEmpty()) {
                                    Toast.makeText(MainActivity.this, acceptList.get(0).getName() + " is accepted", Toast.LENGTH_SHORT).show();
                                }
                                if (!rationalList.isEmpty()) {
                                    Toast.makeText(MainActivity.this, rationalList.get(0).getName() + " is rational", Toast.LENGTH_SHORT).show();
                                }
                                if (!deniedList.isEmpty()) {
                                    Toast.makeText(MainActivity.this, deniedList.get(0).getName() + " is denied", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });

        mCheckSingleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = PermissionUtil.getInstance().checkSinglePermission(Manifest.permission.CAMERA);
                Toast.makeText(MainActivity.this, "result:" + result, Toast.LENGTH_SHORT).show();
            }
        });

        mCheckMultiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, List<PermissionInfo>> map = PermissionUtil.getInstance().checkMultiPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS});
                Toast.makeText(MainActivity.this, map.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
