/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.htrefreshrecyclerview.viewimpl;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 水平方向刷新实现
 */
public class HTHorizontalRecyclerViewImpl extends HTBaseRecyclerViewImpl {

    public HTHorizontalRecyclerViewImpl(Context context) {
        super(context);
    }

    public HTHorizontalRecyclerViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HTHorizontalRecyclerViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean handleUpOrCancelAction(MotionEvent event) {

        int currentPadding = mHTOrientation == Orientation.HORIZONTAL_RIGHT ? mRefreshContainerView.getPaddingLeft() : mRefreshContainerView.getPaddingRight();
        // 如果当前头部刷新控件没有完全隐藏，则需要返回true，自己消耗ACTION_UP事件
        boolean isReturnTrue = currentPadding != mMinRefreshViewPadding;

        if (mRefreshStatus == RefreshStatus.PULL_DOWN || mRefreshStatus == RefreshStatus.IDLE) {
            // 处于下拉刷新状态，松手时隐藏下拉刷新控件
            changeRefreshViewPositionWithAnimation(mMinRefreshViewPadding, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mRefreshStatus = RefreshStatus.IDLE;
                    processRefreshStatusChanged();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else if (mRefreshStatus == RefreshStatus.RELEASE_TO_REFRESH) {
            // 处于松开进入刷新状态，松手时完全显示下拉刷新控件，进入正在刷新状态
            startRefresh();
        }
        mRefreshDownX = -1;
        return isReturnTrue;
    }

    @Override
    public boolean handleMoveAction(MotionEvent event) {
        if (mRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreStatus == LoadMoreStatus.LOADING) {
            return false;
        }
        stopRefreshPositionAnimation();
        if (mRefreshDownX == -1) {
            mRefreshDownX = (int) event.getX();
        }
        int currentPadding;
        float diffX;
        if (shouldHandleRefresh()) {
            if (mHTOrientation == Orientation.HORIZONTAL_RIGHT) {
                currentPadding = mRefreshContainerView.getPaddingLeft();
                diffX = event.getX() - mRefreshDownX;
            } else {
                currentPadding = mRefreshContainerView.getPaddingRight();
                diffX = mRefreshDownX - event.getX();
            }
            diffX = (int) (diffX / mHTViewHolder.getPullDistanceScale());
            int paddingLeftOrRight = (int) (currentPadding + diffX);
            paddingLeftOrRight = Math.max(paddingLeftOrRight, mMinRefreshViewPadding);
            // 下拉刷新控件完全显示，并且当前状态不是释放开始刷新状态
            if (paddingLeftOrRight > 0 && mRefreshStatus != RefreshStatus.RELEASE_TO_REFRESH) {
                mRefreshStatus = RefreshStatus.RELEASE_TO_REFRESH;
                processRefreshStatusChanged();
                mRefreshUIChangeListener.onRefreshPositionChange(1.0f, paddingLeftOrRight + Math.abs(mMinRefreshViewPadding));
            } else if (paddingLeftOrRight < 0) { // 下拉刷新控件没有完全显示
                if (mRefreshStatus != RefreshStatus.PULL_DOWN) {//并且当前状态没有处于下拉刷新状态
                    boolean isPreviousIdle = mRefreshStatus == RefreshStatus.IDLE;
                    mRefreshStatus = RefreshStatus.PULL_DOWN;
                    mRefreshUIChangeListener.onRefreshStart(isPreviousIdle);
                }
                float scale = 1 - paddingLeftOrRight * 1.0f / (mMinRefreshViewPadding == 0 ? 1 : mMinRefreshViewPadding);
                mRefreshUIChangeListener.onRefreshPositionChange(scale, paddingLeftOrRight + Math.abs(mMinRefreshViewPadding));
            }
            paddingLeftOrRight = Math.min(paddingLeftOrRight, mMaxRefreshViewPadding);
            if (mHTOrientation == Orientation.HORIZONTAL_RIGHT) {
                mRefreshContainerView.setPadding(paddingLeftOrRight, 0, 0, 0);
            } else {
                mRefreshContainerView.setPadding(0, 0, paddingLeftOrRight, 0);
            }
            mRefreshDownX = (int) event.getX();
            return true;
        }
        return false;
    }

}
