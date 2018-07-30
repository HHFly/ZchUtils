package com.example.zchutil.download;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.example.zchutil.ExceptionUtil;
import com.example.zchutil.SPUtil;
import com.example.zchutil.UtilsConfig;
import com.example.zchutil.log.LogUtil;
import com.example.zchutil.permisstion.deflistener.PathUtil;

import java.io.File;
import java.text.DecimalFormat;

public class DownloadUtil {
    private final String TAG;
    private final String KEY_DOWN_ID;
    private final String DOWNLOAD_FILE_NAME;
    private Activity mContext;
    private DownloadManager mDownloadManager;
    private String mUrl;
    private long mDownloadId;
    private DownloadUtil.CompleteReceiver mReceiver;
    private DownloadUtil.DownloadObserver mDownloadObserver;
    private String mFileProviderName;
    public static final int CODE = 1008;
    public static final String[] per = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    private DownloadUtil.ProgressCallback mProgressCallback;
    private Handler mHandler;

    public DownloadUtil(Activity activity, String url, DownloadUtil.ProgressCallback callback) {
        this(UtilsConfig.getInstance(activity).getFileProvider(), activity, url, callback);
    }

    public DownloadUtil(String fileProviderName, Activity activity, String url, DownloadUtil.ProgressCallback callback) {
        this.TAG = "DownloadUtil";
        this.KEY_DOWN_ID = "down_id";
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                int mDownloadSoFar = msg.arg1;
                int mDownloadAll = msg.arg2;
                DecimalFormat format = new DecimalFormat("#0.00");
                String str = "已完成:" + format.format((long)mDownloadSoFar) + " b /总进度:" + format.format((long)mDownloadAll) + " b";
                LogUtil.logI("DownloadUtil", "updateVersion", "更新UI>>>" + str);
                DownloadUtil.this.useProgressCall(mDownloadSoFar, mDownloadAll);
            }
        };
        this.DOWNLOAD_FILE_NAME = System.currentTimeMillis() + ".apk";
        if (activity != null && !activity.isFinishing()) {
            this.mFileProviderName = fileProviderName;
            this.mContext = activity;
            this.mUrl = url;
            this.mProgressCallback = callback;
        }
    }

    public void destroy() {
        this.unregisterContentObserver(this.mDownloadObserver);
        this.unregisterUpdataReceiver(this.mReceiver);
        this.mContext = null;
        this.mDownloadManager = null;
        this.mProgressCallback = null;
        this.mDownloadObserver = null;
        this.mReceiver = null;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
            this.mHandler = null;
        }

    }

    public void start() {
        LogUtil.logI("DownloadUtil", "updateVersion", "从缓存中获取上一次下载id");
        long downloadId = SPUtil.getInstance(this.mContext).getLong("down_id", -1L);
        if (downloadId != -1L) {
            LogUtil.logI("DownloadUtil", "updateVersion", "获取该id下载状态信息");
            int status = this.getDownloadStatus(downloadId);
            switch(status) {
                case 2:
                    LogUtil.logI("DownloadUtil", "updateVersion", "该downloadId正在下载");
                    this.mDownloadId = downloadId;
                    this.registerUpdataReceiver();
                    this.registerContentObserver(this.mHandler, this.mContext, this.mDownloadId);
                    this.useProgressCall(-1, 0);
                    break;
                case 8:
                    LogUtil.logI("DownloadUtil", "updateVersion", "该id下载成功");
                    Uri uri = this.getDownloadUri(downloadId);
                    String path = PathUtil.getPathOfVersion(this.mContext, uri);
                    if (uri != null) {
                        if (this.compare(this.getApkInfo(path))) {
                            this.openFile(downloadId);
                            return;
                        }

                        this.remove(downloadId);
                    }

                    LogUtil.logI("DownloadUtil", "updateVersion", "该id对应的文件不存在或者与当前Apk的校验不通过");
                    this.startDownload(this.mUrl);
                    break;
                default:
                    LogUtil.logI("DownloadUtil", "updateVersion", "该id下载失败");
                    this.remove(downloadId);
                    this.startDownload(this.mUrl);
            }
        } else {
            LogUtil.logI("DownloadUtil", "updateVersion", "重新下载");
            this.startDownload(this.mUrl);
        }

    }

    private void registerContentObserver(Handler handler, Context context, long downloadId) {
        if (context != null) {
            LogUtil.logI("DownloadUtil", "registerContentObserver", "注册下载进度监听");
            if (this.mDownloadObserver != null) {
                this.unregisterContentObserver(this.mDownloadObserver);
            }

            this.mDownloadObserver = new DownloadUtil.DownloadObserver(handler, context, downloadId);
            context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, this.mDownloadObserver);
        }

    }

    private void unregisterContentObserver(DownloadUtil.DownloadObserver observer) {
        LogUtil.logI("DownloadUtil", "unregisterContentObserver", "注销下载进度监听");
        if (this.mContext != null && observer != null) {
            try {
                this.mContext.getContentResolver().unregisterContentObserver(observer);
            } catch (Exception var3) {
                ExceptionUtil.throwable(var3);
            }
        }

    }

    private void unregisterUpdataReceiver(DownloadUtil.CompleteReceiver receiver) {
        LogUtil.logI("DownloadUtil", "unregisterUpdataReceiver", "解除注册更新广播接收器");
        if (this.mContext != null && receiver != null) {
            try {
                this.mContext.unregisterReceiver(receiver);
            } catch (Exception var3) {
                ExceptionUtil.throwable(var3);
            }
        }

    }

    private void registerUpdataReceiver() {
        if (this.mContext != null) {
            LogUtil.logI("DownloadUtil", "registerUpdataReceiver", "注册更新广播接收器");
            if (this.mReceiver != null) {
                this.unregisterUpdataReceiver(this.mReceiver);
            }

            this.mReceiver = new DownloadUtil.CompleteReceiver();
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"));
        }

    }

    private void useProgressCall(int curProgress, int allProgress) {
        if (this.mProgressCallback != null) {
            try {
                this.mProgressCallback.call(curProgress, allProgress);
            } catch (Exception var4) {
                ExceptionUtil.throwable(var4);
            }
        }

    }

    private void startDownload(String url) {
        LogUtil.logI("DownloadUtil", "startDownload", "从头开始下载>>>url:" + url);
        this.useProgressCall(-1, 0);
        this.registerUpdataReceiver();
        Uri resource = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(resource);
        request.setAllowedNetworkTypes(3);
        request.setAllowedOverRoaming(false);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
        request.setShowRunningNotification(true);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, this.DOWNLOAD_FILE_NAME);
        this.mDownloadId = this.getManager().enqueue(request);
        this.registerContentObserver(this.mHandler, this.mContext, this.mDownloadId);
        SPUtil.getInstance(this.mContext).putLong("down_id", this.mDownloadId);
    }

    private void remove(long downloadId) {
        LogUtil.logI("DownloadUtil", "remove", "移除该任务downloadId:" + downloadId);
        this.getManager().remove(new long[]{downloadId});
    }

    private boolean compare(PackageInfo apkInfo) {
        if (apkInfo == null) {
            return false;
        } else {
            String localPackage = this.mContext.getPackageName();
            if (apkInfo.packageName.equals(localPackage)) {
                try {
                    PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(localPackage, 0);
                    if (apkInfo.versionCode > packageInfo.versionCode) {
                        return true;
                    }
                } catch (PackageManager.NameNotFoundException var4) {
                    var4.printStackTrace();
                }
            }

            return false;
        }
    }

    private PackageInfo getApkInfo(String path) {
        PackageManager pm = this.mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 1);
        return info != null ? info : null;
    }

    private Uri getDownloadUri(long downloadId) {
        return this.getManager().getUriForDownloadedFile(downloadId);
    }

    private DownloadManager getManager() {
        if (this.mDownloadManager == null) {
            this.mDownloadManager = (DownloadManager)this.mContext.getSystemService("download");
        }

        return this.mDownloadManager;
    }

    private int getDownloadStatus(long downloadId) {
        DownloadManager.Query query = (new DownloadManager.Query()).setFilterById(new long[]{downloadId});
        Cursor c = this.getManager().query(query);
        if (c != null) {
            int var5;
            try {
                if (!c.moveToFirst()) {
                    return -1;
                }

                var5 = c.getInt(c.getColumnIndexOrThrow("status"));
            } finally {
                c.close();
            }

            return var5;
        } else {
            return -1;
        }
    }

    private void openFile(long downloadId) {
        this.openFile(this.mContext, downloadId);
    }

    private void openFile(Context context, long downloadId) {
        LogUtil.logI("DownloadUtil", "updateVersion", "打开安装");
        if (context == null) {
            LogUtil.logE("DownloadUtil", "openFile", "context is null!");
        } else {
            LogUtil.logI("DownloadUtil", "openFile", "启动安装");

            try {
                Uri uri;
                Intent intent;
                if (Build.VERSION.SDK_INT < 23) {
                    uri = this.getDownloadUri(downloadId);
                    intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setType("application/vnd.android.package-archive");
                    intent.setData(uri);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.setFlags(268435456);
                    context.startActivity(intent);
                } else if (Build.VERSION.SDK_INT < 24) {
                    File file = this.queryDownloadedApk(downloadId);
                    if (file.exists()) {
                        intent = new Intent();
                        intent.addFlags(268435456);
                        intent.setAction("android.intent.action.VIEW");
                        String mimeType = this.getMIMEType(file);
                        intent.setDataAndType(Uri.fromFile(file), mimeType);

                        try {
                            context.startActivity(intent);
                        } catch (Exception var8) {
                            var8.printStackTrace();
                        }
                    }
                } else {
                    uri = this.getDownloadUri(downloadId);
                    intent = new Intent("android.intent.action.VIEW");
                    File file = new File(PathUtil.getPathOfVersion(context, uri));
                    intent.setFlags(268435456);
                    Uri apkUri = FileProvider.getUriForFile(context, this.mFileProviderName, file);
                    intent.addFlags(1);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    context.startActivity(intent);
                }
            } catch (Exception var9) {
                LogUtil.logI("DownloadUtil", "openFile", "启动安装异常");
                ExceptionUtil.throwable(var9);
            }

        }
    }

    private File queryDownloadedApk(long id) {
        File targetApkFile = null;
        if (id != -1L) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(new long[]{id});
            query.setFilterByStatus(8);
            Cursor cur = this.getManager().query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex("local_uri"));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }

                cur.close();
            }
        }

        return targetApkFile;
    }

    private String getMIMEType(File file) {
        String type = "";
        String name = file.getName();
        String fileName = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName);
        return type;
    }

    public interface ProgressCallback {
        void call(int var1, int var2);
    }

    private class DownloadObserver extends ContentObserver {
        private long downid;
        private Handler handler;
        private Context context;

        public DownloadObserver(Handler handler, Context context, long downid) {
            super(handler);
            this.handler = handler;
            this.downid = downid;
            this.context = context;
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            try {
                DownloadManager.Query query = (new DownloadManager.Query()).setFilterById(new long[]{this.downid});
                DownloadManager downloadManager = (DownloadManager)this.context.getSystemService("download");
                Cursor cursor = downloadManager.query(query);
                if (cursor != null) {
                    while(cursor.moveToNext()) {
                        int mDownload_so_far = cursor.getInt(cursor.getColumnIndexOrThrow("bytes_so_far"));
                        int mDownload_all = cursor.getInt(cursor.getColumnIndexOrThrow("total_size"));
                        if (mDownload_so_far < 0) {
                            mDownload_so_far = 0;
                        }

                        Message message = Message.obtain();
                        message.arg1 = mDownload_so_far;
                        message.arg2 = mDownload_all;
                        message.obj = this.downid;
                        this.handler.sendMessage(message);
                    }
                }
            } catch (Exception var8) {
                var8.printStackTrace();
            }

        }
    }

    private class CompleteReceiver extends BroadcastReceiver {
        private CompleteReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra("extra_download_id", -1L);
            LogUtil.logI("DownloadUtil", "onReceive", "下载完成>>>downloadId:" + completeDownloadId);
            if (DownloadUtil.this.mDownloadId == completeDownloadId) {
                LogUtil.logI("DownloadUtil", "onReceive", "mDownloadId == completeDownloadId");
                DownloadUtil.this.openFile(context, completeDownloadId);
                DownloadUtil.this.unregisterUpdataReceiver(DownloadUtil.this.mReceiver);
            }

        }
    }
}
