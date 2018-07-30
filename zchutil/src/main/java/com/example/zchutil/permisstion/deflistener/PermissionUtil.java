package com.example.zchutil.permisstion.deflistener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    public PermissionUtil() {
    }

    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == 0;
    }

    public static List<String> checkMorePermissions(Context context, String[] permissions) {
        List<String> permissionList = new ArrayList();

        for(int i = 0; i < permissions.length; ++i) {
            if (!checkPermission(context, permissions[i])) {
                permissionList.add(permissions[i]);
            }
        }

        return permissionList;
    }

    public static void requestPermission(Context context, String permission, int requestCode) {
        ActivityCompat.requestPermissions((Activity)context, new String[]{permission}, requestCode);
    }

    public static void requestMorePermissions(Context context, List permissionList, int requestCode) {
        String[] permissions = (String[])((String[])permissionList.toArray(new String[permissionList.size()]));
        requestMorePermissions(context, permissions, requestCode);
    }

    public static void requestMorePermissions(Context context, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions((Activity)context, permissions, requestCode);
    }

    public static boolean judgePermission(Context context, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, permission);
    }

    public static void checkAndRequestPermission(Context context, String permission, int requestCode) {
        if (!checkPermission(context, permission)) {
            requestPermission(context, permission, requestCode);
        }

    }

    public static void checkAndRequestMorePermissions(Context context, String[] permissions, int requestCode) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        requestMorePermissions(context, permissionList, requestCode);
    }

    public static void checkPermission(Context context, String permission, PermissionCheckCallBack callBack) {
        if (checkPermission(context, permission)) {
            callBack.onHasPermission();
        } else if (judgePermission(context, permission)) {
            callBack.onUserHasAlreadyTurnedDown(new String[]{permission});
        } else {
            callBack.onUserHasAlreadyTurnedDownAndDontAsk(new String[]{permission});
        }

    }

    public static void checkMorePermissions(Context context, String[] permissions, PermissionCheckCallBack callBack) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) {
            callBack.onHasPermission();
        } else {
            boolean isFirst = true;

            for(int i = 0; i < permissionList.size(); ++i) {
                String permission = (String)permissionList.get(i);
                if (judgePermission(context, permission)) {
                    isFirst = false;
                    break;
                }
            }

            String[] unauthorizedMorePermissions = (String[])((String[])permissionList.toArray(new String[permissionList.size()]));
            if (isFirst) {
                callBack.onUserHasAlreadyTurnedDownAndDontAsk(unauthorizedMorePermissions);
            } else {
                callBack.onUserHasAlreadyTurnedDown(unauthorizedMorePermissions);
            }
        }

    }

    public static void checkAndRequestPermission(Context context, String permission, int requestCode, PermissionRequestSuccessCallBack callBack) {
        if (checkPermission(context, permission)) {
            callBack.onHasPermission();
        } else {
            requestPermission(context, permission, requestCode);
        }

    }

    public static void checkAndRequestMorePermissions(Context context, String[] permissions, int requestCode, PermissionRequestSuccessCallBack callBack) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) {
            callBack.onHasPermission();
        } else {
            requestMorePermissions(context, permissionList, requestCode);
        }

    }

    public static void checkAndRequestMorePermissions(Context context, String[] permissions, int requestCode, DefPermissionRequestCallback callBack) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) {
            callBack.onHasPermission();
        } else {
            callBack.onNeedRequestMorePermissions();
            requestMorePermissions(context, permissionList, requestCode);
        }

    }

    public static boolean isPermissionRequestSuccess(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == 0;
    }

    public static void onRequestPermissionResult(Context context, String permission, int[] grantResults, PermissionCheckCallBack callback) {
        if (isPermissionRequestSuccess(grantResults)) {
            callback.onHasPermission();
        } else if (judgePermission(context, permission)) {
            callback.onUserHasAlreadyTurnedDown(new String[]{permission});
        } else {
            callback.onUserHasAlreadyTurnedDownAndDontAsk(new String[]{permission});
        }

    }

    public static void onRequestMorePermissionsResult(Context context, String[] permissions, PermissionCheckCallBack callback) {
        boolean isBannedPermission = false;
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) {
            callback.onHasPermission();
        } else {
            for(int i = 0; i < permissionList.size(); ++i) {
                if (!judgePermission(context, (String)permissionList.get(i))) {
                    isBannedPermission = true;
                    break;
                }
            }

            if (isBannedPermission) {
                callback.onUserHasAlreadyTurnedDownAndDontAsk(permissions);
            } else {
                callback.onUserHasAlreadyTurnedDown(permissions);
            }
        }

    }

    public static void toAppSetting(Context context) {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), (String)null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction("android.intent.action.VIEW");
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }

        context.startActivity(intent);
    }
}
