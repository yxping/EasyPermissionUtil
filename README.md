# EasyPermissionUtil
android6.0开始，权限的申请发生了改变，申请变的动态化，也就是运行时权限，和iOS相仿，动态化的意思是指，在每次使用需要危险权限的方法的时候，需要检查程序是否获得了该权限的许可。动态化的权限申请能够让用户更加清晰的知道程序需要什么权限，以及程序中哪些地方的操作需要涉及用户安全。不再是仅仅在程序安装的时候，一次性把所需要的普通的、危险级别的权限一次性列出来，然后展示给用户。
EasyPermissionUtil可以帮助简化权限申请的流程，同时使得代码更加具有逻辑。对申请的结果进行统一的返回。

## Structure
![](https://github.com/yxping/EasyPermissionUtil/raw/master/structure.png)

## Dependency
在项目的gradle中包括以上的lib工程，暂且如此。。。
``` gradle
compile project(':lib')
```
## Usage
在需要使用权限的地方前，加入以上的代码，然后做相应的处理
``` java
PermissionUtil.getInstance().request(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, mRequestCode,
    new PermissionResultCallBack() {
        @Override
        public void onPermissionGranted() {
            // 当所有权限的申请被用户同意之后,该方法会被调用
        }

        @Override
        public void onPermissionDenied(String... permissions) {
            // 当权限申请中的某一个或多个权限,被用户曾经否定了,并确认了不再提醒时,也就是权限的申请窗口不能再弹出时,该方法将会被调用
        }

        @Override
        public void onRationalShow(String... permissions) {
            // 当权限申请中的某一个或多个权限,被用户否定了,但没有确认不再提醒时,也就是权限窗口申请时,但被否定了之后,该方法将会被调用.
        }
    });
```
## Attention
权限的请求只能在主线程中进行
