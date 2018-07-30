package com.example.zchutil.location.listener;

import com.example.zchutil.location.model.LocationModel;

public interface LocationCallback {
    void getLastKnownLocation(LocationModel var1);

    void onLocationChanged(LocationModel var1);

    boolean isLocationOne();
}
