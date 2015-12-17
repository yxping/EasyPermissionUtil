package com.yxp.permission.util.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Android6.0权限申请工具类
 * Created by yanxing on 12/9/15.
 */
public class PermissionUtil {

    private PermissionResultCallBack mPermissionResultCallBack;
    private volatile static PermissionUtil instance;
    private int mRequestCode;
    private Context mContext;
    private Fragment mFragment;
    private List<PermissionInfo> mPermissionListNeedReq;
    private String[] mPermissions;

    public static PermissionUtil getInstance() {
        if (instance == null) {
            synchronized (PermissionUtil.class) {
                if (instance == null) {
                    instance = new PermissionUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 用于fragment中请求权限
     * @param fragment
     * @param permissions
     * @param requestCode
     * @param callBack
     */
    public void request(@NonNull Fragment fragment,@NonNull String[] permissions,@NonNull int requestCode, PermissionResultCallBack callBack) {
        this.mFragment = fragment;
        this.request(fragment.getActivity(), permissions, requestCode, callBack);
    }

    /**
     * 用于activity中请求权限
     * @param context
     * @param permissions
     * @param requestCode
     * @param callBack
     */
    public void request(@NonNull Context context,@NonNull String[] permissions,@NonNull int requestCode, PermissionResultCallBack callBack) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("request permission only can run in MainThread!");
        }

        if (permissions.length == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onGranted();
            return;
        }

        this.mContext = context;
        this.mPermissions = permissions;
        this.mRequestCode = requestCode;
        this.mPermissionResultCallBack = callBack;
        this.mPermissionListNeedReq = new ArrayList<PermissionInfo>();

        if (needToRequest()) {
            requestPermissions();
        } else {
            onGranted();
        }
    }

    /**
     * 通过开启一个新的activity作为申请权限的媒介
     */
    private void requestPermissions() {

        Intent intent = new Intent(mContext, HelpActivity.class);
        intent.putExtra("permissions", mPermissions);
        intent.putExtra("requestCode", mRequestCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 检查是否需要申请权限
     * @return
     */
    private boolean needToRequest() {
        for (String permission : mPermissions) {
            int checkRes = ContextCompat.checkSelfPermission(mContext, permission);
            if (checkRes != PackageManager.PERMISSION_GRANTED) {
                PermissionInfo info = new PermissionInfo(permission);
                if (mContext instanceof Activity &&
                        ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, permission)) {
                    info.setRationalNeed(true);
                }
                mPermissionListNeedReq.add(info);
            }
        }

        if (mPermissionListNeedReq.size() > 0) {
            mPermissions = new String[mPermissionListNeedReq.size()];
            for (int i = 0; i < mPermissionListNeedReq.size(); i++) {
                mPermissions[i] = mPermissionListNeedReq.get(i).getName();
            }
            return true;
        }

        return false;
    }

    /**
     * 申请权限结果返回
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == mRequestCode) {

            if (mContext != null && mContext instanceof Activity) {
                ((Activity) mContext).onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

            if (mFragment != null) {
                mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

            boolean isAllGranted = true;
            List<PermissionInfo> needRationalPermissionList = new ArrayList<PermissionInfo>();
            List<PermissionInfo> deniedPermissionList = new ArrayList<PermissionInfo>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (mPermissionListNeedReq.get(i).isRationalNeed()) {
                        needRationalPermissionList.add(mPermissionListNeedReq.get(i));
                    } else {
                        deniedPermissionList.add(mPermissionListNeedReq.get(i));
                    }
                    isAllGranted = false;
                }
            }

            if (needRationalPermissionList.size() != 0) {
                showRational(needRationalPermissionList);
            }

            if (deniedPermissionList.size() != 0) {
                onDenied(deniedPermissionList);
            }

            if (isAllGranted) {
                onGranted();
            }

        }
    }

    /**
     * 权限被用户许可之后回调的方法
     */
    private void onGranted() {
        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onPermissionGranted();
        }
    }

    /**
     * 权限申请被用户否定之后的回调方法,这个主要是当用户点击否定的同时点击了不在弹出,
     * 那么当再次申请权限,此方法会被调用
     * @param list
     */
    private void onDenied(List<PermissionInfo> list) {
        if(list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onPermissionDenied(permissions);
        }
    }

    /**
     * 权限申请被用户否定后的回调方法,这个主要场景是当用户点击了否定,但未点击不在弹出,
     * 那么当再次申请权限的时候,此方法会被调用
     * @param list
     */
    private void showRational(List<PermissionInfo> list) {
        if(list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onRationalShow(permissions);
        }
    }

}
