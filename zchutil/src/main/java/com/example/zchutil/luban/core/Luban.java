package com.example.zchutil.luban.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class Luban  {
    private static final int FIRST_GEAR = 1;
    public static final int THIRD_GEAR = 3;
    private static final String TAG = "Luban";
    private static String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";
    private static volatile Luban INSTANCE;
    private final File mCacheDir;
    private OnCompressListener compressListener;
    private File mFile;
    private int gear = 3;
    private String filename;

    private Luban(File cacheDir) {
        this.mCacheDir = cacheDir;
    }

    private static synchronized File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    private static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir == null) {
            if (Log.isLoggable("Luban", 6)) {
                Log.e("Luban", "default disk cache dir is null");
            }

            return null;
        } else {
            File result = new File(cacheDir, cacheName);
            if (result.mkdirs() || result.exists() && result.isDirectory()) {
                File noMedia = new File(cacheDir + "/.nomedia");
                return noMedia.mkdirs() || noMedia.exists() && noMedia.isDirectory() ? result : null;
            } else {
                return null;
            }
        }
    }

    public static Luban get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Luban(getPhotoCacheDir(context));
        }

        return INSTANCE;
    }

    public Luban launch() {
        Preconditions.checkNotNull(this.mFile, "the image file cannot be null, please call .load() before this method!");
        if (this.compressListener != null) {
            this.compressListener.onStart();
        }

        if (this.gear == 1) {
            Observable.just(this.mFile).map(new Func1<File, File>() {
                public File call(File file) {
                    return Luban.this.firstCompress(file);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Action1<Throwable>() {
                public void call(Throwable throwable) {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onError(throwable);
                    }

                }
            }).onErrorResumeNext(Observable.<File>empty()).filter(new Func1<File, Boolean>() {
                public Boolean call(File file) {
                    return file != null;
                }
            }).subscribe(new Action1<File>() {
                public void call(File file) {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onSuccess(file);
                    }

                }
            });
        } else if (this.gear == 3) {
            Observable.just(this.mFile).map(new Func1<File, File>() {
                public File call(File file) {
                    return Luban.this.thirdCompress(file);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Action1<Throwable>() {
                public void call(Throwable throwable) {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onError(throwable);
                    }

                }
            }).onErrorResumeNext((Func1<Throwable, ? extends Observable<? extends File>>) Observable.empty()).filter(new Func1<File, Boolean>() {
                public Boolean call(File file) {
                    return file != null;
                }
            }).subscribe(new Action1<File>() {
                public void call(File file) {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onSuccess(file);
                    }

                }
            });
        }

        return this;
    }

    public Luban load(File file) {
        this.mFile = file;
        return this;
    }

    public Luban setCompressListener(OnCompressListener listener) {
        this.compressListener = listener;
        return this;
    }

    public Luban putGear(int gear) {
        this.gear = gear;
        return this;
    }

    /** @deprecated */
    public Luban setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Observable<File> asObservable() {
        if (this.gear == 1) {
            return Observable.just(this.mFile).map(new Func1<File, File>() {
                public File call(File file) {
                    return Luban.this.firstCompress(file);
                }
            });
        } else {
            return this.gear == 3 ? Observable.just(this.mFile).map(new Func1<File, File>() {
                public File call(File file) {
                    return Luban.this.thirdCompress(file);
                }
            }) : (Observable) Observable.empty();
        }
    }

    private File thirdCompress(@NonNull File file) {
        String thumb = this.mCacheDir.getAbsolutePath() + File.separator + (TextUtils.isEmpty(this.filename) ? System.currentTimeMillis() : this.filename) + ".jpg";
        String filePath = file.getAbsolutePath();
        int angle = this.getImageSpinAngle(filePath);
        int width = this.getImageSize(filePath)[0];
        int height = this.getImageSize(filePath)[1];
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;
        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;
        double scale = (double)width / (double)height;
        double size;
        int multiple;
        if (scale <= 1.0D && scale > 0.5625D) {
            if (height < 1664) {
                if (file.length() / 1024L < 150L) {
                    return file;
                }

                size = (double)(width * height) / Math.pow(1664.0D, 2.0D) * 150.0D;
                size = size < 60.0D ? 60.0D : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (double)(thumbW * thumbH) / Math.pow(2495.0D, 2.0D) * 300.0D;
                size = size < 60.0D ? 60.0D : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (double)(thumbW * thumbH) / Math.pow(2560.0D, 2.0D) * 300.0D;
                size = size < 100.0D ? 100.0D : size;
            } else {
                multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (double)(thumbW * thumbH) / Math.pow(2560.0D, 2.0D) * 300.0D;
                size = size < 100.0D ? 100.0D : size;
            }
        } else if (scale <= 0.5625D && scale > 0.5D) {
            if (height < 1280 && file.length() / 1024L < 200L) {
                return file;
            }

            multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (double)(thumbW * thumbH) / 3686400.0D * 400.0D;
            size = size < 100.0D ? 100.0D : size;
        } else {
            multiple = (int)Math.ceil((double)height / (1280.0D / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (double)(thumbW * thumbH) / (1280.0D * (1280.0D / scale)) * 500.0D;
            size = size < 100.0D ? 100.0D : size;
        }

        return this.compress(filePath, thumb, thumbW, thumbH, angle, (long)size);
    }

    private File firstCompress(@NonNull File file) {
        int minSize = 60;
        int longSide = 720;
        int shortSide = 1280;
        String filePath = file.getAbsolutePath();
        String thumbFilePath = this.mCacheDir.getAbsolutePath() + File.separator + (TextUtils.isEmpty(this.filename) ? System.currentTimeMillis() : this.filename) + ".jpg";
        long size = 0L;
        long maxSize = file.length() / 5L;
        int angle = this.getImageSpinAngle(filePath);
        int[] imgSize = this.getImageSize(filePath);
        int width = 0;
        int height = 0;
        double scale;
        if (imgSize[0] <= imgSize[1]) {
            scale = (double)imgSize[0] / (double)imgSize[1];
            if (scale <= 1.0D && scale > 0.5625D) {
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = (long)minSize;
            } else if (scale <= 0.5625D) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = maxSize;
            }
        } else {
            scale = (double)imgSize[1] / (double)imgSize[0];
            if (scale <= 1.0D && scale > 0.5625D) {
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = (long)minSize;
            } else if (scale <= 0.5625D) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = maxSize;
            }
        }

        return this.compress(filePath, thumbFilePath, width, height, angle, size);
    }

    public int[] getImageSize(String imagePath) {
        int[] res = new int[2];
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);
        res[0] = options.outWidth;
        res[1] = options.outHeight;
        return res;
    }

    private Bitmap compress(String imagePath, int width, int height) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;
        int heightRatio;
        int widthRatio;
        if (outH > height || outW > width) {
            heightRatio = outH / 2;

            for(widthRatio = outW / 2; heightRatio / inSampleSize > height && widthRatio / inSampleSize > width; inSampleSize *= 2) {
                ;
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        heightRatio = (int)Math.ceil((double)((float)options.outHeight / (float)height));
        widthRatio = (int)Math.ceil((double)((float)options.outWidth / (float)width));
        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private int getImageSpinAngle(String path) {
        short degree = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt("Orientation", 1);
            switch(orientation) {
                case 3:
                    degree = 180;
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return degree;
    }

    private File compress(String largeImagePath, String thumbFilePath, int width, int height, int angle, long size) {
        Bitmap thbBitmap = this.compress(largeImagePath, width, height);
        thbBitmap = rotatingImage(angle, thbBitmap);
        return this.saveImage(thumbFilePath, thbBitmap, size);
    }

    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float)angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private File saveImage(String filePath, Bitmap bitmap, long size) {
        Preconditions.checkNotNull(bitmap, "Lubanbitmap cannot be null");
        File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if (!result.exists() && !result.mkdirs()) {
            return null;
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(CompressFormat.JPEG, options, stream);

            while((long)(stream.toByteArray().length / 1024) > size && options > 6) {
                stream.reset();
                options -= 6;
                bitmap.compress(CompressFormat.JPEG, options, stream);
            }

            bitmap.recycle();

            try {
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(stream.toByteArray());
                fos.flush();
                fos.close();
                stream.close();
            } catch (IOException var9) {
                var9.printStackTrace();
            }

            return new File(filePath);
        }
    }
}
