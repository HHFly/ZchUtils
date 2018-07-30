package com.example.zchutil.viewcache;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;

public interface IViewHandle {
    <T extends View> T getView(@IdRes int var1);

    <T extends View> T getView(@IdRes int var1, @IdRes int var2);

    <T extends View> T getView(View var1, @IdRes int var2);

    void setTvText(@IdRes int var1, String var2);

    void setTvText(@IdRes int var1, @StringRes int var2);

    String getTvText(@IdRes int var1);

    void setClickListener(@IdRes int var1, View.OnClickListener var2);

    void setViewVisibilityGONE(@IdRes int var1, boolean var2);

    void setViewVisibilityINVISIBLE(@IdRes int var1, boolean var2);

    void setViewVisibility(@IdRes int var1, boolean var2, int var3, int var4);

    void setViewSelected(@IdRes int var1, boolean var2);

    void setViewSelected(View var1, boolean var2);
}
