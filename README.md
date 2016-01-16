# EasyPermissionUtil
android6.0开始，权限的申请发生了改变，申请变的动态化，也就是运行时权限，和iOS相仿，动态化的意思是指，在每次使用需要危险权限的方法的时候，需要检查程序是否获得了该权限的许可。动态化的权限申请能够让用户更加清晰的知道程序需要什么权限，以及程序中哪些地方的操作需要涉及用户安全。不再是仅仅在程序安装的时候，一次性把所需要的普通的、危险级别的权限一次性列出来，然后展示给用户。
EasyPermissionUtil可以帮助简化权限申请的流程，同时使得代码更加具有逻辑。对申请的结果进行统一的返回。

## Structure
![](https://github.com/yxping/EasyPermissionUtil/raw/master/structure.png)

## Dependency
1.在项目的root gradle.build中
``` gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" } // 加入这句话
    }
}
```
2.在lib工程下的build.gradle中
``` gradle
compile 'com.github.yxping:EasyPermissionUtil:v0.0.3'
```
## Usage
``` java
PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS}, mRequestCode,
    new PermissionResultCallBack() {
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
    });
```

可以通过PermissionResultCallBack获得回调的结果,也可以通过PermissionResultAdapter获得回调的结果,区别是
PermissionResultAdapter支持任意重写方法,而无需重写所有的方法.
``` java
PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, mRequestCode,
    new PermissionResultAdapter() {
        @Override
        public void onResult(Map<String, List<PermissionInfo>> result) {

        }
    });
```

另外如果通过此方法进行调用,结果的返回也可以通过activity或者fragment的onRequestPermissionsResult得到结果

## Attention
权限的请求只能在主线程中进行
