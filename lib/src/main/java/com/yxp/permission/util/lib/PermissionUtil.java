package com.yxp.permission.util.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import com.yxp.permission.util.lib.PermissionInfo;
import com.yxp.permission.util.lib.callback.PermissionOriginResultCallBack;
import com.yxp.permission.util.lib.callback.PermissionResultCallBack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android6.0权限申请工具类
 * Created by yanxing on 12/9/15.
 */
public class PermissionUtil {
    public final static String TAG = "PermissionUtil";
    public final static String ACCEPT = "accept";
    public final static String DENIED = "denied";
    public final static String RATIONAL = "rational";
    public final static int PERMISSION_GRANTED = 1;
    public final static int PERMISSION_RATIONAL = 2;
    public final static int PERMISSION_DENIED = 3;
    public final static int PERMISSION_REQUEST_CODE = 42;

    private PermissionResultCallBack mPermissionResultCallBack;
    private PermissionOriginResultCallBack mPermissionOriginResultCallBack;
    private volatile static PermissionUtil instance;
    private int mRequestCode = -1;
    private static Activity mActivity;
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
     * 检查单个权限是否被允许,(当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
     *
     * @param permission The name of the permission being checked.
     * @return PermissionUtil.PERMISSION_GRANTED / PERMISSION_DENIED / PERMISSION_RATIONAL or {@code null}
     *         if context is not instanceof Activity.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public int checkSinglePermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PERMISSION_GRANTED;
        }

        if (mActivity == null) {
            mActivity = getTopActivity();
        }

        if (mActivity != null) {
            if (mActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                return PERMISSION_GRANTED;
            } else {
                if (mActivity.shouldShowRequestPermissionRationale(permission)) {
                    return PERMISSION_RATIONAL;
                } else {
                    return PERMISSION_DENIED;
                }
            }
        } else {
            Log.e(TAG, "TopActivity not find");
            return -1;
        }
    }

    /**
     * 检查多个权限的状态,不会进行权限的申请.(当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
     *
     * @param permissions The name of the permission being checked.
     * @return Map<String, List<PermissionInfo>> or {@code null}
     *         if context is not instanceof Activity or topActivity can not be find
     */
    public Map<String, List<PermissionInfo>> checkMultiPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null;
        }

        if (mActivity == null) {
            mActivity = getTopActivity();
        }

        if (mActivity != null) {
            this.mPermissionListNeedReq = new ArrayList<PermissionInfo>();
            this.mPermissionListDenied = new ArrayList<PermissionInfo>();
            this.mPermissionListAccepted = new ArrayList<PermissionInfo>();

            for (String permission : permissions) {
                int result = checkSinglePermission(permission);
                switch (result) {
                    case PERMISSION_GRANTED:
                        mPermissionListAccepted.add(new PermissionInfo(permission));
                        break;
                    case PERMISSION_RATIONAL:
                        mPermissionListNeedReq.add(new PermissionInfo(permission));
                        break;
                    case PERMISSION_DENIED:
                        mPermissionListDenied.add(new PermissionInfo(permission));
                        break;
                    default:
                        break;
                }
            }

            HashMap<String, List<PermissionInfo>> map = new HashMap<String, List<PermissionInfo>>();
            if (!mPermissionListAccepted.isEmpty()) {
                map.put(ACCEPT, mPermissionListAccepted);
            }
            if (!mPermissionListNeedReq.isEmpty()) {
                map.put(RATIONAL, mPermissionListNeedReq);
            }
            if (!mPermissionListDenied.isEmpty()) {
                map.put(DENIED, mPermissionListDenied);
            }
            return map;
        } else {
            return new HashMap<String, List<PermissionInfo>>();
        }

    }

    /**
     * 用于fragment中请求权限
     *
     * @param fragment
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull Fragment fragment, @NonNull String[] permissions, PermissionResultCallBack callBack) {
        this.mFragment = fragment;
        this.request(fragment.getActivity(), permissions, callBack);
    }

    /**
     * 用于fragment中请求权限
     *
     * @param fragment
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull Fragment fragment, @NonNull String[] permissions, PermissionOriginResultCallBack callBack) {
        this.mFragment = fragment;
        this.request(fragment.getActivity(), permissions, callBack);
    }

    /**
     * 用于activity中请求权限
     *
     * @param activity
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull Activity activity, @NonNull String[] permissions, PermissionResultCallBack callBack) {

        if (!this.checkSituation(permissions, callBack)) {
            return;
        }

        this.request(activity, permissions);
    }

    /**
     * 用于activity中请求权限
     *
     * @param activity
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull Activity activity, @NonNull String[] permissions, PermissionOriginResultCallBack callBack) {

        if (!this.checkSituation(permissions, callBack)) {
            return;
        }

        this.request(activity, permissions);
    }

    /**
     * 申请权限方法
     *
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull String[] permissions, PermissionResultCallBack callBack) {

        if (!this.checkSituation(permissions, callBack)) {
            return;
        }

        this.request(mActivity, permissions, callBack);
    }

    /**
     * 申请权限方法
     *
     * @param permissions The name of the permission being checked.
     * @param callBack
     * @throws RuntimeException if not in the MainThread
     */
    public void request(@NonNull String[] permissions, PermissionOriginResultCallBack callBack) {

        if (!this.checkSituation(permissions, callBack)) {
            return;
        }

        this.request(mActivity, permissions, callBack);
    }

    /**
     * 请求权限核心方法
     *
     * @param activity
     * @param permissions
     */
    private void request(Activity activity, String[] permissions) {
        this.mActivity = activity;

        if (mActivity == null) {
            mActivity = getTopActivity();
            if (mActivity == null) {
                Log.e(TAG, "TopActivity not find");
                return;
            }
        }

        this.mPermissions = permissions;

        if (needToRequest()) {
            requestPermissions();
        } else {
            onResult(mPermissionListAccepted, mPermissionListNeedReq, mPermissionListDenied);
            if (mPermissionListDenied.isEmpty() && mPermissionListNeedReq.isEmpty()) {
                onGranted();
                onGranted(mPermissionListAccepted);
            }
        }
    }

    /**
     * 检查环境是否满足申请权限的要求
     *
     * @param permissions
     * @param callBack
     * @return
     * @throws RuntimeException if not in the MainThread
     */
    private boolean checkSituation(String[] permissions, PermissionResultCallBack callBack) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("request permission only can run in MainThread!");
        }

        if (permissions.length == 0) {
            return false;
        }
        this.mPermissionResultCallBack = null;
        this.mPermissionOriginResultCallBack = null;

        this.mPermissionResultCallBack = callBack;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onGranted();
            mPermissionResultCallBack.onPermissionGranted(permissions);
            onResult(toPermissionList(permissions), new ArrayList<PermissionInfo>(), new ArrayList<PermissionInfo>());
            return false;
        }
        return true;
    }

    /**
     * 检查环境是否满足申请权限的要求
     *
     * @param permissions
     * @param callBack
     * @return
     * @throws RuntimeException if not in the MainThread
     */
    private boolean checkSituation(String[] permissions, PermissionOriginResultCallBack callBack) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("request permission only can run in MainThread!");
        }

        if (permissions.length == 0) {
            return false;
        }
        this.mPermissionResultCallBack = null;
        this.mPermissionOriginResultCallBack = null;

        this.mPermissionOriginResultCallBack = callBack;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onGranted();
            onResult(toPermissionList(permissions), new ArrayList<PermissionInfo>(), new ArrayList<PermissionInfo>());
            return false;
        }
        return true;
    }

    /**
     * 通过开启一个新的activity作为申请权限的媒介
     */
    private void requestPermissions() {

        Intent intent = new Intent(mActivity, HelpActivity.class);
        intent.putExtra("permissions", mPermissions);
        if (mRequestCode < 0) {
            mRequestCode = PERMISSION_REQUEST_CODE;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }

    /**
     * 检查是否需要申请权限
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean needToRequest() {
        checkMultiPermissions(mPermissions);

        if (mPermissionListNeedReq.size() > 0 || mPermissionListDenied.size() > 0) {
            mPermissions = new String[mPermissionListNeedReq.size() + mPermissionListDenied.size()];
            for (int i = 0; i < mPermissionListNeedReq.size(); i++) {
                mPermissions[i] = mPermissionListNeedReq.get(i).getName();
            }
            for (int i = mPermissionListNeedReq.size(); i < mPermissions.length; i++) {
                mPermissions[i] = mPermissionListDenied.get(i - mPermissionListNeedReq.size()).getName();
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

            if (mActivity != null) {
                mActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

            if (mFragment != null) {
                mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

            boolean isAllGranted = true;
            List<PermissionInfo> needRationalPermissionList = new ArrayList<PermissionInfo>();
            List<PermissionInfo> deniedPermissionList = new ArrayList<PermissionInfo>();

            for (int i = 0; i < permissions.length; i++) {
                PermissionInfo info = new PermissionInfo(permissions[i]);
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (mActivity.shouldShowRequestPermissionRationale(permissions[i])) {
                        needRationalPermissionList.add(info);
                    } else {
                        deniedPermissionList.add(info);
                    }
                    isAllGranted = false;
                } else {
                    mPermissionListAccepted.add(info);
                }
            }

            onResult(mPermissionListAccepted, needRationalPermissionList, deniedPermissionList);

            if (deniedPermissionList.size() != 0) {
                onDenied(deniedPermissionList);
                isAllGranted = false;
            }

            if (needRationalPermissionList.size() != 0) {
                showRational(needRationalPermissionList);
                isAllGranted = false;
            }

            if (mPermissionListAccepted.size() != 0 && mPermissionResultCallBack != null) {
                onGranted(mPermissionListAccepted);
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
        if (mPermissionOriginResultCallBack == null) {
            return;
        }

        mPermissionOriginResultCallBack.onResult(acceptPermissionList, needRationalPermissionList, deniedPermissionList);
    }

    /**
     * 权限被用户许可之后回调的方法
     */
    private void onGranted() {
        if (mPermissionResultCallBack != null) {
            mPermissionResultCallBack.onPermissionGranted();
        }
    }

    private void onGranted(List<PermissionInfo> list) {
        if (mPermissionResultCallBack == null) {
            return;
        }
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        mPermissionResultCallBack.onPermissionGranted(permissions);
    }

    /**
     * 权限申请被用户否定之后的回调方法,这个主要是当用户点击否定的同时点击了不在弹出,
     * 那么当再次申请权限,此方法会被调用
     *
     * @param list
     */
    private void onDenied(List<PermissionInfo> list) {
        if (mPermissionResultCallBack == null) {
            return;
        }
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        mPermissionResultCallBack.onPermissionDenied(permissions);
    }

    /**
     * 权限申请被用户否定后的回调方法,这个主要场景是当用户点击了否定,但未点击不在弹出,
     * 那么当再次申请权限的时候,此方法会被调用
     *
     * @param list
     */
    private void showRational(List<PermissionInfo> list) {
        if (mPermissionResultCallBack == null) {
            return;
        }
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }
        mPermissionResultCallBack.onRationalShow(permissions);
    }

    /**
     * 通过反射的方法获取最上层的Activity
     * @return
     */
    private Activity getTopActivity() {
        Activity topActivity = null;
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getATMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            Object activityThread = getATMethod.invoke(null);
            activitiesField.setAccessible(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ArrayMap activites = (ArrayMap) activitiesField.get(activityThread);
                Object activityClientRecord = activites.valueAt(0);

                Class activityClientRecordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                topActivity = (Activity) activityField.get(activityClientRecord);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return topActivity;
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
     * 将字符串数组转换为PermissionInfoList
     * @param permissions
     * @return
     */
    private List<PermissionInfo> toPermissionList(String... permissions) {
        List<PermissionInfo> result = new ArrayList<PermissionInfo>();
        for (String permission : permissions) {
            result.add(new PermissionInfo(permission));
        }
        return result;
    }

}
