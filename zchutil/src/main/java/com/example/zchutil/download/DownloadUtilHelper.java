package com.example.zchutil.download;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.zchutil.UtilsConfig;
import com.example.zchutil.permisstion.deflistener.PermissionCheckCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionRequestSuccessCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionUtil;

public class DownloadUtilHelper {
    private static final String TAG = "DownloadUtilHelper";
    private static String mUpdateUrl;
    private static String mFileProviderName;
    private static DownloadUtil.ProgressCallback mCallback;
    private static DownloadUtil mDownloadUtil;

    public DownloadUtilHelper() {
    }

    public static void startDownload(final String fileProviderName, final Activity activity, final String url, final DownloadUtil.ProgressCallback callback) {
        mFileProviderName = fileProviderName;
        mUpdateUrl = url;
        mCallback = callback;
        PermissionUtil.checkAndRequestMorePermissions(activity, DownloadUtil.per, 1008, new PermissionRequestSuccessCallBack() {
            public void onHasPermission() {
                try {
                    DownloadUtilHelper.mDownloadUtil = new DownloadUtil(fileProviderName, activity, url, callback);
                    DownloadUtilHelper.mDownloadUtil.start();
                } catch (Exception var2) {
                    var2.printStackTrace();
                }

            }
        });
    }

    public static void startDownload(final Activity activity, final String url, final DownloadUtil.ProgressCallback callback) {
        final String fileProviderName = UtilsConfig.getInstance(activity).getFileProvider();
        mFileProviderName = fileProviderName;
        mUpdateUrl = url;
        mCallback = callback;
        PermissionUtil.checkAndRequestMorePermissions(activity, DownloadUtil.per, 1008, new PermissionRequestSuccessCallBack() {
            public void onHasPermission() {
                try {
                    DownloadUtilHelper.mDownloadUtil = new DownloadUtil(fileProviderName, activity, url, callback);
                    DownloadUtilHelper.mDownloadUtil.start();
                } catch (Exception var2) {
                    var2.printStackTrace();
                }

            }
        });
    }

    public static void onRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1008) {
            PermissionUtil.onRequestMorePermissionsResult(activity, DownloadUtil.per, new PermissionCheckCallBack() {
                public void onHasPermission() {
                    try {
                        DownloadUtilHelper.mDownloadUtil = new DownloadUtil(DownloadUtilHelper.mFileProviderName, activity, DownloadUtilHelper.mUpdateUrl, DownloadUtilHelper.mCallback);
                        DownloadUtilHelper.mDownloadUtil.start();
                    } catch (Exception var2) {
                        var2.printStackTrace();
                    }

                    DownloadUtilHelper.mUpdateUrl = null;
                    DownloadUtilHelper.mFileProviderName = null;
                    DownloadUtilHelper.mCallback = null;
                }

                public void onUserHasAlreadyTurnedDown(String... permission) {
                }

                public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                }
            });
        }
    }

    public static void onDestroy() {
        if (mDownloadUtil != null) {
            mDownloadUtil.destroy();
        }

    }
}
