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
compile 'com.github.yxping:EasyPermissionUtil:v0.1.0'
```
### Api方法
## PermissionUtil.java
``` java
/**
 * 检查单个权限是否被允许,不会进行权限的申请.(注意:当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
 */
public int checkSinglePermission(String permission);

/**
 * 检查多个权限的状态,不会进行权限的申请.(注意:当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
 */
public Map<String, List<PermissionInfo>> checkMultiPermissions(String... permissions);

/**
 * 申请权限方法
 */
public void request(@NonNull String[] permissions, PermissionResultCallBack callBack);

public void request(@NonNull String[] permissions, PermissionOriginResultCallBack callBack);

/**
 * 用于fragment中请求权限
 */
public void request(Fragment fragment, String[] permissions, PermissionResultCallBack callBack);

public void request(Fragment fragment, String[] permissions, PermissionOriginResultCallBack callBack);

/**
 * 用于activity中请求权限
 */
public void request(Activity activity, String[] permissions, PermissionResultCallBack callBack);

public void request(Activity activity, String[] permissions, PermissionOriginResultCallBack callBack);
```

## PermissionInfo.java
``` java
/**
 * 获得权限的全名如android.permission.CAMERA
 */
public String getName();
/**
 * 获得权限的简短名称如CAMERA
 */
public String getShortName();
```

## PermissionResultCallback.java  回调接口
可以通过PermissionResultCallBack获得回调的结果,也可以通过PermissionResultAdapter获得回调的结果,区别是
PermissionResultAdapter支持任意重写方法,而无需重写所有的方法.
``` java
/**
 * 当所有权限的申请被用户同意之后,该方法会被调用
 */
void onPermissionGranted();

/**
 * 返回此次申请中通过的权限列表
 */
void onPermissionGranted(String... permissions);

/**
 * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,并勾选了不再提醒选项时（权限的申请窗口不能再弹出，
 * 没有办法再次申请）,该方法将会被调用。该方法调用时机在onRationalShow之前.onDenied和onRationalShow
 * 有可能都会被触发.
 */
void onPermissionDenied(String... permissions);

/**
 * 当权限申请中的某一个或多个权限,在此次申请中被用户否定了,但没有勾选不再提醒选项时（权限申请窗口还能再次申请弹出）
 * 该方法将会被调用.这个方法会在onPermissionDenied之后调用,当申请权限为多个时,onDenied和onRationalShow
 * 有可能都会被触发.
 */
void onRationalShow(String... permissions);
```

## PermissionOriginResultCallBack.java  回调接口
``` java
/**
 *
 * 返回所有结果的列表list,包括通过的,允许提醒,拒绝的的三个内容,各个list有可能为空
 * list中的元素为PermissionInfo,提供getName()[例如:android.permission.CAMERA]和getShortName()[例如:CAMERA]方法
 * 在进行申请方法调用后,此方法一定会被调用返回此次请求后的权限申请的情况
 */
void onResult(List<PermissionInfo> acceptList, List<PermissionInfo> rationalList, List<PermissionInfo> deniedList);
```

#### Example
``` java
PermissionUtil.getInstance().request(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS},
    new PermissionResultCallBack() {
        void onPermissionGranted();
        void onPermissionGranted(String... permissions);
        void onPermissionDenied(String... permissions);
        void onRationalShow(String... permissions);
    });
```

可以通过PermissionResultCallBack获得回调的结果,也可以通过PermissionResultAdapter获得回调的结果,区别是
PermissionResultAdapter支持任意重写方法,而无需重写所有的方法.
``` java
PermissionUtil.getInstance().request(new String[]{Manifest.permission.READ_CALENDAR}, mRequestCode,
    new PermissionResultAdapter() {
        @Override
        public void onResult(Map<String, List<PermissionInfo>> result) {

        }
    });
```

另外如果通过此方法进行调用,结果的返回也可以通过activity或者fragment的onRequestPermissionsResult得到结果

### Comparation
拿Google的例子来进行权限申请的比较，Google需要做的比较多的是方法回调后的判断，代码复杂。
```java
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // 由于用户拒绝了所申请的权限,再此申请时进行提示
                Toast.makeText(CompareActivity.this, "permission show rational", Toast.LENGTH_SHORT).show();
            } else {
                // 权限申请
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, mRequestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mRequestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // 权限申请被拒绝
                    Toast.makeText(CompareActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请被同意
                    Toast.makeText(CompareActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
```

#### Attension
只能在主线程中调用,建议不要在Service或Broadcast中使用,因为逻辑是通过获取当前程序的activity栈中的顶层activity进行请求的.
