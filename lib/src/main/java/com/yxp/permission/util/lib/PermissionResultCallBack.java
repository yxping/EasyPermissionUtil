package com.yxp.permission.util.lib;

import java.util.List;
import java.util.Map;

/**
 * Created by yanxing on 12/9/15.
 */
public interface PermissionResultCallBack {

    /**
     * 当所有权限的申请被用户同意之后,该方法会被调用
     */
    void onPermissionGranted();

    /**
     * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,并勾选了不再提醒选项时（权限的申请窗口不能再弹出，
     * 没有办法再次申请）,该方法将会被调用。该方法调用时机在onRationalShow之前.onDenied和onRationalShow
     * 有可能都会被触发.
     *
     * @param permissions
     */
    void onPermissionDenied(String... permissions);

    /**
     * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,但没有勾选不再提醒选项时（权限申请窗口还能再次申请弹出）
     * 该方法将会被调用.这个方法会在onPermissionDenied之后调用,当申请权限为多个时,onDenied和onRationalShow
     * 有可能都会被触发.
     *
     * @param permissions
     */
    void onRationalShow(String... permissions);

    /**
     * 返回所有结果的列表list,包括通过的,拒绝的,允许提醒的三个内容,各个list有可能为空
     * 通过PermissionUtil.ACCEPT / DENIED / RATIONAL 获取相应的list
     * list中的元素为PermissionInfo,提供getName()[例如:android.permission.CAMERA]和getShortName()[例如:CAMERA]方法
     * 在进行申请方法调用后,此方法一定会被调用返回此次请求后的权限申请的情况
     *
     * @param result
     */
    void onResult(Map<String, List<PermissionInfo>> result);
}
