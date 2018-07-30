package com.example.zchutil.viewcache;

import android.support.annotation.IdRes;
import android.util.SparseArray;
import android.view.View;

public interface IViewCache {
    SparseArray<View> getViewArray();

    View getRootView();

    void put(@IdRes int var1, View var2);

    <T extends View> T get(@IdRes int var1);

    void remove(@IdRes int var1);

    void onDestroy();
}
