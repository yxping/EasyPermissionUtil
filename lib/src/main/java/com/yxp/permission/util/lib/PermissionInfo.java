package com.yxp.permission.util.lib;

/**
 * Created by yanxing on 12/9/15.
 */
public class PermissionInfo {
    private String mName;
    private boolean mRationalNeed;

    public PermissionInfo(String name) {
        this.mName = name;
        mRationalNeed = false;
    }

    public String getName() {
        return mName;
    }

    public boolean isRationalNeed() {
        return mRationalNeed;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public void setRationalNeed(boolean mRationalNeed) {
        this.mRationalNeed = mRationalNeed;
    }
}
