package com.yxp.easy.permissionutil;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by yanxing on 12/10/15.
 */
public class CompareActivity extends Activity {
    private int mRequestCode = 200;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // 由于用户拒绝了所申请的权限,再此申请时进行提示
                Toast.makeText(CompareActivity.this, "permission show rational", Toast.LENGTH_SHORT).show();
            } else {
                // 权限申请
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, mRequestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mRequestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // 权限申请被拒绝
                    Toast.makeText(CompareActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请被同意
                    Toast.makeText(CompareActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
