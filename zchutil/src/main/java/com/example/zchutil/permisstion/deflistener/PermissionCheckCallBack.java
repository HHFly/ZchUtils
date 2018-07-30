package com.example.zchutil.permisstion.deflistener;

public interface PermissionCheckCallBack {
    void onHasPermission();

    void onUserHasAlreadyTurnedDown(String... var1);

    void onUserHasAlreadyTurnedDownAndDontAsk(String... var1);
}
