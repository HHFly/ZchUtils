package com.example.zchutil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.example.zchutil.permisstion.deflistener.PermissionCheckCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionRequestSuccessCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionUtil;

import java.io.File;

public class TakePhoneUtil {
    private static final String permissionWrite = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String permissionCamera = "android.permission.CAMERA";
    private static Uri mUri;

    public TakePhoneUtil() {
    }

    public static void choosePhoto(final Activity activity) {
        PermissionUtil.checkAndRequestMorePermissions(activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 2, new PermissionRequestSuccessCallBack() {
            public void onHasPermission() {
                TakePhoneUtil.openChooser(activity);
            }
        });
    }

    private static void openChooser(Activity activity) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, "选择图片"), 2);
    }

    public static void takePhoto(final Activity activity) {
        String[] pers = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
        PermissionUtil.checkAndRequestMorePermissions(activity, pers, 1, new PermissionRequestSuccessCallBack() {
            public void onHasPermission() {
                TakePhoneUtil.openPhoto(activity);
            }
        });
    }

    private static void openPhoto(Activity context) {
        File file = getFile();
        Intent intentFromCapture = new Intent("android.media.action.IMAGE_CAPTURE");
        if (Build.VERSION.SDK_INT >= 24) {
            String strFileProvider = UtilsConfig.getInstance(context).getFileProvider();
            intentFromCapture.addFlags(3);
            mUri = FileProvider.getUriForFile(context, strFileProvider, file);
        } else {
            mUri = Uri.fromFile(file);
        }

        intentFromCapture.putExtra("output", mUri);
        context.startActivityForResult(intentFromCapture, 1);
    }

    private static File getFile() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "images");
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }

        return new File(imagePath, System.currentTimeMillis() + ".jpg");
    }

    public static void onRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                PermissionUtil.onRequestMorePermissionsResult(activity, permissions, new PermissionCheckCallBack() {
                    public void onHasPermission() {
                        TakePhoneUtil.openPhoto(activity);
                    }

                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        ToastUtil.show(R.string.mcs_1);
                    }

                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                    }
                });
                break;
            case 2:
                PermissionUtil.onRequestMorePermissionsResult(activity, permissions, new PermissionCheckCallBack() {
                    public void onHasPermission() {
                        TakePhoneUtil.openChooser(activity);
                    }

                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        ToastUtil.show(R.string.mcs_1);
                    }

                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                    }
                });
        }

    }

    public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, TakePhoneUtil.CallBack call) {
        switch(requestCode) {
            case 1:
                if (resultCode == -1 && call != null) {
                    call.back(mUri);
                }

                mUri = null;
                break;
            case 2:
                if (resultCode == -1 && data != null) {
                    mUri = data.getData();
                    if (mUri == null) {
                        Bundle bundle = data.getExtras();
                        mUri = (Uri)bundle.get("data");
                    }

                    if (call != null && mUri != null) {
                        call.back(mUri);
                    }
                }

                mUri = null;
        }

    }

    public interface CallBack {
        void back(Uri var1);
    }
}
