package com.yyp.xrecyclerview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yyp.xrecyclerview.R;
import com.yyp.xrecyclerview.widget.interfaces.BaseFooter;

/** 底部上拉加载
 * Created by yyp on 2017/7/18.
 */
public class LoadMoreFooter extends LinearLayout implements BaseFooter{

    private View mContainer;
    private TextView mStatusTextView;
    private ProgressBar progressBar;

    private int mState = STATE_NORMAL;

    public int mMeasuredHeight;

    public LoadMoreFooter(Context context) {
        super(context);
        initView();
    }

    public LoadMoreFooter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadMoreFooter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView(){
        // 初始情况，设置上拉加载view高度为0
        mContainer = LayoutInflater.from(getContext()).inflate(R.layout.xrecyclerview_footer_view, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.TOP);

        mStatusTextView = findViewById(R.id.footer_status_text);
        progressBar = findViewById(R.id.footer_progressbar);

        measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight(); // 计算的底部布局的真实高度
    }

    /**
     * 设置状态
     * @param state 状态
     */
    public void setState(int state){
        if (state == mState) return ;

        if (state == STATE_LOADING) {	// 显示进度
            progressBar.setVisibility(View.VISIBLE);
            smoothScrollTo(mMeasuredHeight);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }

        switch(state){
            case STATE_NORMAL:
                break;
            case STATE_LOADING:
                mStatusTextView.setText("正在加载...");
                break;
            case STATE_DONE:
                mStatusTextView.setText("加载完成");
                smoothScrollTo(0);
                break;
            case STATE_NOMORE:

                mStatusTextView.setText("已经到底了");
                smoothScrollTo(mMeasuredHeight);
                break;
            default:
        }

        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void loadMoreComplete() {
        setState(STATE_DONE);
    }

    @Override
    public void setNoMore(boolean noMore) {
        if(noMore){
            setState(STATE_NOMORE);
        }
    }

    /**
     * 设置刷新头高度
     * @param height 高度
     */
    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisibleHeight() {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

    @Override
    public void onMove(float delta) {
        if(getVisibleHeight() > 0 || delta > 0) {
            setVisibleHeight((int) delta + getVisibleHeight()); // 上拉滑动时，更新底部布局高度
            if (getVisibleHeight() >= mMeasuredHeight) {
                setState(STATE_LOADING);
            }else {
                setState(STATE_NORMAL);
            }
        }
    }

    @Override
    public boolean releaseAction(float height) {
        boolean isOnLoad = false;
        int mHeight = getVisibleHeight();
        if (mHeight == 0) // not visible.
            isOnLoad = false;
        // 显示高度大于底部布局高度，可以加载
        if(getVisibleHeight() >= mMeasuredHeight &&  mState <= STATE_LOADING){
            setState(STATE_LOADING);
            isOnLoad = true;
        }

        if (mState != STATE_LOADING) {
            smoothScrollTo(0);
        }

        if (mState == STATE_LOADING) {
            int destHeight = mMeasuredHeight;
            smoothScrollTo(destHeight);
        }

        return isOnLoad;
    }

    /**
     * 滑动时加入动画
     * @param destHeight
     */
    private void smoothScrollTo(int destHeight) {
        // 初始化动画开始、结束值
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        // 初始化动画时间
        animator.setDuration(300).start();
        // 监听动画
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        // 开启动画
        animator.start();
    }
}
