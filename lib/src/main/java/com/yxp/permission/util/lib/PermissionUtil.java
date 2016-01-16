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
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Android6.0权限申请工具类
 * Created by yanxing on 12/9/15.
 */
public class PermissionUtil {

    public final static String TAG = "PermissionUtil";
    public final static String ACCEPT = "accept";
    public final static String DENIED = "denied";
    public final static String RATIONAL = "rational";

    private PermissionResultCallBack mPermissionResultCallBack;
    private volatile static PermissionUtil instance;
    private int mRequestCode;
    private Context mContext;
    private Fragment mFragment;
    private List<PermissionInfo> mPermissionListNeedReq;
    /**
     * 被拒绝的权限列表
     */
    private List<PermissionInfo> mPermissionListDenied;
    /**
     * 被接受的权限列表
     */
    private List<PermissionInfo> mPermissionListAccepted;
    private String[] mPermissions;

    private PermissionUtil() {

    }

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
     *
     * @param fragment
     * @param permissions
     * @param requestCode
     * @param callBack
     */
    public void request(@NonNull Fragment fragment, @NonNull String[] permissions, @NonNull int requestCode, PermissionResultCallBack callBack) {
        this.mFragment = fragment;
        this.request(fragment.getActivity(), permissions, requestCode, callBack);
    }

    /**
     * 用于activity中请求权限
     *
     * @param context
     * @param permissions
     * @param requestCode
     * @param callBack
     */
    public void request(@NonNull Context context, @NonNull String[] permissions, @NonNull int requestCode, PermissionResultCallBack callBack) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("request permission only can run in MainThread!");
        }

        if (!(context instanceof Activity)) {
            Log.e(TAG, "the first Argument of request() method shuold instance of Activity");
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
        this.mPermissionListDenied = new ArrayList<PermissionInfo>();
        this.mPermissionListAccepted = new ArrayList<PermissionInfo>();

        if (needToRequest()) {
            requestPermissions();
        } else {
            onResult(mPermissionListAccepted, null, mPermissionListDenied);
            if (mPermissionListDenied.isEmpty()) {
                onGranted();
            } else {
                onDenied(mPermissionListDenied);
            }
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
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean needToRequest() {
        for (String permission : mPermissions) {
            int checkRes = ContextCompat.checkSelfPermission(mContext, permission);
            if (checkRes != PackageManager.PERMISSION_GRANTED) {
                PermissionInfo info = new PermissionInfo(permission);
                if (((Activity)mContext).shouldShowRequestPermissionRationale(permission)) {
                    mPermissionListNeedReq.add(info);
                } else {
                    mPermissionListDenied.add(info);
                }
            }
            if (checkRes == PackageManager.PERMISSION_GRANTED) {
                mPermissionListAccepted.add(new PermissionInfo(permission));
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
     *
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

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (((Activity) mContext).shouldShowRequestPermissionRationale(permissions[i])) {
                        PermissionInfo info = new PermissionInfo(permissions[i]);
                        needRationalPermissionList.add(info);
                    } else {
                        mPermissionListDenied.add(mPermissionListNeedReq.get(i));
                    }
                    isAllGranted = false;
                } else {
                    mPermissionListAccepted.add(new PermissionInfo(permissions[i]));
                }
            }

            onResult(mPermissionListAccepted, needRationalPermissionList, mPermissionListDenied);

            if (mPermissionListDenied.size() != 0) {
                onDenied(mPermissionListDenied);
            }

            if (needRationalPermissionList.size() != 0) {
                showRational(needRationalPermissionList);
            }

            if (isAllGranted) {
                onGranted();
            }

        }
    }

    /**
     * 返回所有结果的列表list,包括通过的,拒绝的,允许提醒的三个内容,各个list有可能为空
     *
     * @param acceptPermissionList
     * @param needRationalPermissionList
     * @param deniedPermissionList
     * @return
     */
    private void onResult(List<PermissionInfo> acceptPermissionList,
                          List<PermissionInfo> needRationalPermissionList,
                          List<PermissionInfo> deniedPermissionList) {
        HashMap<String, List<PermissionInfo>> map = new HashMap<String, List<PermissionInfo>>();
        if (acceptPermissionList != null && !acceptPermissionList.isEmpty()) {
            map.put(ACCEPT, acceptPermissionList);
        }
        if (needRationalPermissionList != null && !needRationalPermissionList.isEmpty()) {
            map.put(RATIONAL, needRationalPermissionList);
        }
        if (deniedPermissionList != null && !deniedPermissionList.isEmpty()) {
            map.put(DENIED, deniedPermissionList);
        }

        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onResult(map);
        }
    }

    /**
     * 转换为string的list
     *
     * @param list
     * @return
     */
    private List<String> toStringList(List<PermissionInfo> list) {
        List<String> result = new ArrayList<String>();
        for (PermissionInfo info : list) {
            result.add(info.getName());
        }
        return result;
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
     *
     * @param list
     */
    private void onDenied(List<PermissionInfo> list) {
        if (list == null || list.size() == 0) return;

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
     *
     * @param list
     */
    private void showRational(List<PermissionInfo> list) {
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onRationalShow(permissions);
        }
    }

}
