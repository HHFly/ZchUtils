package com.example.zchutil.viewcache;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zchutil.log.LogUtil;

public class ViewCacheUtils implements IViewCache, IViewHandle {
    private View mRootView;
    private SparseArray<View> mViews;
    private View.OnClickListener normalOnClickListener;

    public ViewCacheUtils(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View rootView = window.getDecorView();
                this.mRootView = rootView;
            }
        }

        if (this.mRootView == null) {
            String TAG = "ViewCacheUtils";
            if (activity != null) {
                TAG = activity.getClass().getName();
            }

            LogUtil.logE(TAG, "ViewCacheUtils", "activity.getWindow().getDecorView() 空指针");
        }

    }

    public ViewCacheUtils(View rootView) {
        this.mRootView = rootView;
    }

    public SparseArray<View> getViewArray() {
        if (this.mViews == null) {
            this.mViews = new SparseArray();
        }

        return this.mViews;
    }

    public View getRootView() {
        return this.mRootView;
    }

    public void put(int id, View view) {
        if (view != null) {
            SparseArray<View> views = this.getViewArray();
            views.put(id, view);
        }

    }

    public <T extends View> T get(int id) {
        SparseArray<View> views = this.getViewArray();
        View child = (View)views.get(id);
        return (T) child;
    }

    public void remove(int id) {
        SparseArray<View> views = this.getViewArray();
        views.remove(id);
    }

    public void onDestroy() {
        if (this.mViews != null) {
            this.mViews.clear();
            this.mViews = null;
        }

        this.normalOnClickListener = null;
        this.mRootView = null;
    }

    public <T extends View> T getView(@IdRes int id) {
        View child = this.get(id);
        if (child == null) {
            child = this.getView(this.getRootView(), id);
            this.put(id, child);
        }

        return (T) child;
    }

    public <T extends View> T getView(@IdRes int rootResId, @IdRes int childResId) {
        View rootView = this.getView(rootResId);
        return this.getView(rootView, childResId);
    }

    public <T extends View> T getView(View rootView, @IdRes int id) {
        if (rootView != null) {
            View view = rootView.findViewById(id);
            return (T) view;
        } else {
            return null;
        }
    }

    public void setTvText(@IdRes int id, String data) {
        View view = this.getView(id);
        if (view != null && view instanceof TextView) {
            TextView tv = (TextView)view;
            tv.setText(data);
        }

    }

    public void setTvText(@IdRes int id, @StringRes int resId) {
        View view = this.getView(id);
        if (view != null && view instanceof TextView) {
            TextView tv = (TextView)view;
            tv.setText(resId);
            if (tv instanceof EditText) {
                EditText et = (EditText)tv;
                et.setSelection(et.getText().length());
            }
        }

    }

    public String getTvText(@IdRes int id) {
        View view = this.getView(id);
        if (view != null && view instanceof TextView) {
            TextView tv = (TextView)view;
            return tv.getText().toString();
        } else {
            return "";
        }
    }

    public void setClickListener(@IdRes int id, View.OnClickListener listener) {
        View view = this.getView(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }

    }

    public void setViewVisibilityGONE(@IdRes int id, boolean flag) {
        this.setViewVisibility(id, flag, 0, 8);
    }

    public void setViewVisibilityINVISIBLE(@IdRes int id, boolean flag) {
        this.setViewVisibility(id, flag, 0, 4);
    }

    public void setViewVisibility(@IdRes int id, boolean flag, int trueValue, int falseValue) {
        View view = this.getView(id);
        if (view != null) {
            view.setVisibility(flag ? trueValue : falseValue);
        }

    }

    public void setViewSelected(@IdRes int id, boolean flag) {
        View view = this.getView(id);
        this.setViewSelected(view, flag);
    }

    public void setViewSelected(View view, boolean flag) {
        if (view != null) {
            view.setSelected(flag);
        }

    }
}
