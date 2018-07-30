package com.example.zchutil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.example.zchutil.permisstion.deflistener.PermissionCheckCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionRequestSuccessCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionUtil;

public class CallPhoneUtil {
    private static String permission = "android.permission.CALL_PHONE";
    private static String REQUEST_PHONE;

    public CallPhoneUtil() {
    }

    public static void call(final Activity activity, final String phone) {
        REQUEST_PHONE = phone;
        PermissionUtil.checkAndRequestPermission(activity, permission, 100, new PermissionRequestSuccessCallBack() {
            public void onHasPermission() {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(activity, "android.permission.CALL_PHONE") == 0) {
                    activity.startActivity(intent);
                }
            }
        });
    }

    public static void onRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            PermissionUtil.onRequestPermissionResult(activity, permission, grantResults, new PermissionCheckCallBack() {
                public void onHasPermission() {
                    CallPhoneUtil.call(activity, CallPhoneUtil.REQUEST_PHONE);
                }

                public void onUserHasAlreadyTurnedDown(String... permission) {
                    ToastUtil.show(R.string.mcs_1);
                }

                public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                }
            });
            REQUEST_PHONE = null;
        }

    }
}
