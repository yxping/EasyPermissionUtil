package com.yxp.permission.util.lib;

/**
 * Created by yanxing on 12/9/15.
 */
public interface PermissionResultCallBack {

    /**
     * 当所有权限的申请被用户同意之后,该方法会被调用
     */
    void onPermissionGranted();

    /**
     * 当权限申请中的某一个或多个权限,被用户曾经否定了,并确认了不再提醒时,也就是权限的申请窗口不能再弹出时,
     * 该方法将会被调用
     * @param permissions
     */
    void onPermissionDenied(String... permissions);

    /**
     * 当权限申请中的某一个或多个权限,被用户否定了,但没有确认不再提醒时,也就是权限窗口申请时,但被否定了之后,
     * 该方法将会被调用.
     * @param permissions
     */
    void onRationalShow(String... permissions);
}
