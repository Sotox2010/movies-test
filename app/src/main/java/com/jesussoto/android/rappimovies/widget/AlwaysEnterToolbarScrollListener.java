package com.jesussoto.android.rappimovies.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class AlwaysEnterToolbarScrollListener extends RecyclerView.OnScrollListener
        implements SynchronizedScrollView.OnScrollListener {

    private Toolbar mToolbar;

    public AlwaysEnterToolbarScrollListener(@NonNull Toolbar toolbar) {
        mToolbar = toolbar;
    }

    @Override
    public void onScrollChanged(int left, int top, int deltaX, int deltaY) {
        animateOnScroll(deltaY);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        animateOnScroll(dy);
    }

    private void animateOnScroll(int deltaY) {
        if (mToolbar == null) {
            return;
        }

        int toolbarHeight = mToolbar.getHeight();

        int translationY = (int) Math.min(0, Math.max(
                -toolbarHeight, mToolbar.getTranslationY() - deltaY));

        mToolbar.setTranslationY(translationY);
    }
}
