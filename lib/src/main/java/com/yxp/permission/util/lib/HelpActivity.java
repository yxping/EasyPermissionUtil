package com.yxp.permission.util.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.yxp.permission.util.lib.PermissionUtil;

/**
 * Created by yanxing on 12/9/15.
 */
public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleIntent(Intent intent) {
        String[] permissions = intent.getStringArrayExtra("permissions");
        int requestCode = intent.getIntExtra("requestCode", PermissionUtil.PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtil.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
        finish();
    }
}
