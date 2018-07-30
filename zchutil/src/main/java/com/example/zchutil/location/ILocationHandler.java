package com.example.zchutil.location;

import android.app.Activity;

import com.example.zchutil.location.listener.LocationCallback;

import java.util.List;

public interface ILocationHandler {
    void register(Activity var1, LocationCallback var2);

    void unRegister(LocationCallback var1);

    void unRegister(List<LocationCallback> var1);

    void unRegisterAll();
}
