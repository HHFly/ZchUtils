package com.example.zchutil.location;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.zchutil.location.listener.LocationCallback;
import com.example.zchutil.location.model.LocationModel;

import java.util.List;

public interface LocationUtil extends ILocationHandler {
    String TAG = "LocationUtil";
    String[] ARRAY_PERMISSION = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    List<LocationCallback> getLocationCallbackArray();

    void setNewestLocation(LocationModel var1);

    void onRequestPermissionsResult(Activity var1, int var2, @NonNull String[] var3, @NonNull int[] var4);
}
