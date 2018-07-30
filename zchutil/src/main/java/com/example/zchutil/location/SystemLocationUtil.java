package com.example.zchutil.location;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import com.example.zchutil.R;
import com.example.zchutil.ToastUtil;
import com.example.zchutil.location.listener.LocationCallback;
import com.example.zchutil.location.listener.MySystemLocationListener;
import com.example.zchutil.location.model.LocationModel;
import com.example.zchutil.log.LogUtil;
import com.example.zchutil.permisstion.deflistener.DefPermissionRequestCallback;
import com.example.zchutil.permisstion.deflistener.PermissionCheckCallBack;
import com.example.zchutil.permisstion.deflistener.PermissionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SystemLocationUtil implements LocationUtil {
    private final String TAG;
    private static final long MIN_TIME = 30000L;
    private static final long MIN_DISTANCE = 45L;
    private static SystemLocationUtil util;
    private Context mAppContext;
    private LocationManager mManager;
    private long mMinTime;
    private long mMinDistance;
    private MySystemLocationListener mMyNetworkLocationListener;
    private MySystemLocationListener mMyGPSLocationListener;
    private List<LocationCallback> mLocationCallbackArray;
    private LocationCallback mPermissionCallback;
    private LocationModel mNewestLocation;

    private SystemLocationUtil(Context context) {
        this(context, 30000L, 45L);
    }

    private SystemLocationUtil(Context context, long minTime, long minDistance) {
        this.TAG = "SystemLocationUtil";
        this.mAppContext = context.getApplicationContext();
        this.mMinTime = minTime;
        this.mMinDistance = minDistance;
    }

    public static SystemLocationUtil getInstance(Context context) {
        if (util == null) {
            Class var1 = SystemLocationUtil.class;
            synchronized(SystemLocationUtil.class) {
                if (util == null) {
                    util = new SystemLocationUtil(context);
                }
            }
        }

        return util;
    }

    public static SystemLocationUtil getInstance(Context context, long minTime, long minDistance) {
        if (util == null) {
            Class var5 = SystemLocationUtil.class;
            synchronized(SystemLocationUtil.class) {
                if (util == null) {
                    util = new SystemLocationUtil(context, minTime, minDistance);
                }
            }
        }

        return util;
    }

    public LocationManager getManager() {
        if (this.mManager == null) {
            this.mManager = (LocationManager)this.mAppContext.getSystemService("location");
        }

        return this.mManager;
    }

    public List<LocationCallback> getLocationCallbackArray() {
        return this.mLocationCallbackArray;
    }

    public void setNewestLocation(LocationModel data) {
        this.mNewestLocation = data;
    }

    public void register(Activity activity, final LocationCallback callback) {
        LogUtil.logI("SystemLocationUtil", "register", "正在校验");
        if (MyCommonLocationListenerUtil.checkCallbackAndSelfPermission("SystemLocationUtil", activity, callback)) {
            LogUtil.logI("SystemLocationUtil", "register", "正在进行权限校验");
            PermissionUtil.checkAndRequestMorePermissions(activity, LocationUtil.ARRAY_PERMISSION, 101, new DefPermissionRequestCallback() {
                public void onHasPermission() {
                    LogUtil.logI("SystemLocationUtil", "register", "权限校验通过");
                    SystemLocationUtil.this.registerCallback(callback);
                }

                public void onNeedRequestMorePermissions() {
                    super.onNeedRequestMorePermissions();
                    LogUtil.logI("SystemLocationUtil", "register", "没有权限，请求权限申请");
                    SystemLocationUtil.this.mPermissionCallback = callback;
                }
            });
        }

    }

    public synchronized void registerCallback(LocationCallback callback) {
        StringBuilder log = new StringBuilder();
        if (callback != null) {
            log.append("callback不为空\n");
            Location location = this.getLastKnownLocation();
            if (location != null) {
                LocationModel l = new LocationModel(location);
                callback.getLastKnownLocation(l);
                log.append("获取上一个定位信息" + l.toString() + "\n");
            }

            log.append("添加监听到mLocationCallbackArray>>>[isLocationOne]" + callback.isLocationOne() + "\n");
            if (this.mLocationCallbackArray == null) {
                this.mLocationCallbackArray = new ArrayList();
            }

            this.mLocationCallbackArray.add(callback);
        } else {
            log.append("callback为空\n");
            if (this.mLocationCallbackArray == null || this.mLocationCallbackArray.size() == 0) {
                log.append("监听回调列表为空，不注册系统定位监听");
                return;
            }
        }

        if (MyCommonLocationListenerUtil.isNetworkEnabled(this.mAppContext)) {
            this.registerCallback(this.mMyNetworkLocationListener, callback, "network", log);
        } else {
            log.append("network定位不可用\n");
        }

        if (MyCommonLocationListenerUtil.isGpsEnabled(this.mAppContext)) {
            this.registerCallback(this.mMyGPSLocationListener, callback, "gps", log);
        } else {
            log.append("gps定位不可用\n");
        }

        LogUtil.logI("SystemLocationUtil", "registerCallback", log.toString());
    }

    private Location getLastKnownLocation() {
        Location location = this.getManager().getLastKnownLocation("gps");
        if (location == null) {
            location = this.getManager().getLastKnownLocation("network");
        }

        return location;
    }

    private synchronized void registerCallback(MySystemLocationListener listener, LocationCallback callback, String provider, StringBuilder log) {
        log.append("------------------------------------\n");
        log.append("注册" + provider + "定位监听>>>");
        log.append("[mMinTime]" + this.mMinTime + "---");
        log.append("[mMinDistance]" + this.mMinDistance + "---");
        log.append("[provider]" + provider + "\n");
        if (listener == null) {
            log.append("未注册该监听\n");
            listener = new MySystemLocationListener(this, provider);
            if ("gps".equals(provider)) {
                log.append("保存gps定位监听\n");
                this.mMyGPSLocationListener = listener;
            } else if ("network".equals(provider)) {
                log.append("保存network定位监听\n");
                this.mMyNetworkLocationListener = listener;
            }

            log.append("注册该监听\n");
            this.getManager().requestLocationUpdates(provider, this.mMinTime, (float)this.mMinDistance, listener);
        } else {
            log.append("已注册该监听\n");
            if (this.mNewestLocation != null) {
                if (callback != null) {
                    log.append("已存在定位坐标，直接返回\n");
                    callback.onLocationChanged(new LocationModel(this.mNewestLocation));
                    MyCommonLocationListenerUtil.checkRemoveCallback("SystemLocationUtil", this, callback, log);
                }
            } else {
                log.append("未存在定位坐标，等待系统返回\n");
            }
        }

        log.append("------------------------------------\n");
    }

    public synchronized void unRegister(LocationCallback callback) {
        if (this.mLocationCallbackArray != null && callback != null) {
            StringBuilder log = new StringBuilder();
            log.append("移除该监听>>>" + callback.getClass().hashCode() + "---" + callback.getClass().getName() + "\n");
            this.mLocationCallbackArray.remove(callback);
            if (this.mLocationCallbackArray.size() == 0) {
                log.append("列表中没有别的回调需要处理，移除系统定位监听");
                this.unRegisterAll();
            }

            LogUtil.logI("SystemLocationUtil", "unRegister", log.toString());
        }

    }

    public synchronized void unRegister(List<LocationCallback> callback) {
        if (callback != null) {
            Iterator var2 = callback.iterator();

            while(var2.hasNext()) {
                LocationCallback item = (LocationCallback)var2.next();
                this.unRegister(item);
            }
        }

    }

    public synchronized void unRegisterNetwork() {
        if (this.mManager != null && this.mMyNetworkLocationListener != null) {
            LogUtil.logI("SystemLocationUtil", "unRegisterNetwork", "注销network定位监听");
            this.mManager.removeUpdates(this.mMyNetworkLocationListener);
            this.mMyNetworkLocationListener = null;
        }

    }

    public synchronized void unRegisterGps() {
        if (this.mManager != null && this.mMyGPSLocationListener != null) {
            LogUtil.logI("SystemLocationUtil", "unRegisterNetwork", "注销gps定位监听");
            this.mManager.removeUpdates(this.mMyGPSLocationListener);
            this.mMyGPSLocationListener = null;
        }

    }

    public synchronized void unRegisterAll() {
        LogUtil.logI("SystemLocationUtil", "unRegister", "注销全部监听");
        if (this.mManager != null) {
            this.unRegisterGps();
            this.unRegisterNetwork();
            this.mManager = null;
        }

        if (this.mLocationCallbackArray != null) {
            this.mLocationCallbackArray.clear();
            this.mLocationCallbackArray = null;
        }

    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            LogUtil.logI("SystemLocationUtil", "onRequestPermissionsResult", "权限申请回调");
            PermissionUtil.onRequestMorePermissionsResult(activity, LocationUtil.ARRAY_PERMISSION, new PermissionCheckCallBack() {
                public void onHasPermission() {
                    LogUtil.logI("SystemLocationUtil", "onRequestPermissionsResult", "权限申请成功");
                    if (SystemLocationUtil.this.mPermissionCallback != null) {
                        SystemLocationUtil.this.registerCallback(SystemLocationUtil.this.mPermissionCallback);
                    } else {
                        LogUtil.logI("SystemLocationUtil", "onRequestPermissionsResult", "mPermissionCallback == null || TextUtils.isEmpty(mProvider)");
                    }

                    SystemLocationUtil.this.mPermissionCallback = null;
                }

                public void onUserHasAlreadyTurnedDown(String... permission) {
                    LogUtil.logI("SystemLocationUtil", "onRequestPermissionsResult", "用户拒绝权限");
                    ToastUtil.show(R.string.mcs_1);
                    SystemLocationUtil.this.mPermissionCallback = null;
                }

                public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                    LogUtil.logI("SystemLocationUtil", "onRequestPermissionsResult", "用户拒绝权限，且勾选不在询问");
                    SystemLocationUtil.this.mPermissionCallback = null;
                }
            });
        }

    }

    private static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(1);
        return criteria;
    }
}
