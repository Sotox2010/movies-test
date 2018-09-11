package com.jesussoto.android.rappimovies.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedScrollView extends NestedScrollView {

    public interface OnScrollListener {
        void onScrollChanged(int left, int top, int deltaX, int deltaY);
    }

    private List<OnScrollListener> mListeners;

    public SynchronizedScrollView(Context context) {
        super(context);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mListeners != null) {
            for (OnScrollListener listener : mListeners) {
                listener.onScrollChanged(l, t, l - oldl, t - oldt);
            }
        }
    }

    public void addOnScrollListener(OnScrollListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }

        mListeners.add(listener);
    }

    public void removeOnScrollListener(OnScrollListener listener) {
        if (mListeners == null) {
            return;
        }

        mListeners.remove(listener);
        if (mListeners.isEmpty()) {
            mListeners = null;
        }
    }

    @Nullable
    public List<OnScrollListener> getOnScrollListeners() {
        return mListeners;
    }
}
