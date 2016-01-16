package com.yxp.permission.util.lib;

import java.util.List;
import java.util.Map;

/**
 * 支持任意重写方法,而无需重写所有的方法
 *
 * Created by yanxing on 16/1/15.
 */
public abstract class PermissionResultAdapter implements PermissionResultCallBack {

    @Override
    public void onPermissionGranted() {

    }

    @Override
    public void onPermissionDenied(String... permissions) {

    }

    @Override
    public void onRationalShow(String... permissions) {

    }

    @Override
    public void onResult(Map<String, List<PermissionInfo>> result) {

    }
}
