package com.example.zchutil.permisstion.deflistener;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PathUtil {
    public PathUtil() {
    }

    public static File getFileByUri(Context context, Uri uri) {
        if (context != null && uri != null) {
            String path = getPathOfVersion(context, uri);
            if (!TextUtils.isEmpty(path)) {
                File bFile = new File(path);
                return bFile;
            }
        }

        return null;
    }

    public static String getPathOfVersion(Context context, Uri uri) {
        if (context != null && uri != null) {
            return Build.VERSION.SDK_INT < 24 ? getPath(context, uri) : getFilePathFromURI(context, uri);
        } else {
            return null;
        }
    }

    @SuppressLint({"NewApi"})
    private static String getPath(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= 19;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String docId;
            String[] split;
            String type;
            if (isExternalStorageDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri);
                split = docId.split(":");
                type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else {
                if (isDownloadsDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    return getDataColumn(context, contentUri, (String)null, (String[])null);
                }

                if (isMediaDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    split = docId.split(":");
                    type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, "_id=?", selectionArgs);
                }
            }
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, (String)null, (String[])null);
            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{"_data"};

        String var8;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }

            int column_index = cursor.getColumnIndexOrThrow("_data");
            var8 = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

        return var8;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getFilePathFromURI(Context context, Uri contentUri) {
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(getDiskCachePath(context) + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    private static String getFileName(Uri uri) {
        if (uri == null) {
            return null;
        } else {
            String fileName = null;
            String path = uri.getPath();
            int cut = path.lastIndexOf(47);
            if (cut != -1) {
                fileName = path.substring(cut + 1);
            }

            return fileName;
        }
    }

    private static String getDiskCachePath(Context context) {
        return !"mounted".equals(Environment.getExternalStorageState()) && Environment.isExternalStorageRemovable() ? context.getCacheDir().getPath() : context.getExternalCacheDir().getPath();
    }

    private static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) {
                return;
            }

            OutputStream outputStream = new FileOutputStream(dstFile);
            copyFile(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    private static void copyFile(InputStream inStream, OutputStream fs) {
        try {
            int bytesum = 0;
//            int byteread = false;
            byte[] buffer = new byte[1444];

            int byteread;
            while((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }

            inStream.close();
        } catch (Exception var6) {
            System.out.println("复制单个文件操作出错");
            var6.printStackTrace();
        }

    }
}
