package com.yyp.xrecyclerview.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.yyp.xrecyclerview.widget.interfaces.XRecyclerViewLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyp on 2017/7/18.
 */

public class XRecyclerView extends RecyclerView {

    public static final String TAG = "XRecyclerView";
    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = false;

    private float mLastY = -1;
    XRecyclerViewLoadingListener mXRecyclerViewLoadingListener;

    private static final float DRAG_RATE = 1.8f; // 下拉速度
    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private static List<Integer> sHeaderTypes = new ArrayList<>();//每个header必须有不同的type,不然滚动的时候顺序会变化

    private ArrowRefreshHeader mRefreshHeader;


    private static final int TYPE_REFRESH_HEADER = 10000;
    private static final int TYPE_FOOTER = 10001;
    private static final int HEADER_INIT_INDEX = 10002;

    private WrapAdapter mWrapAdapter;
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    private LoadMoreFooter mFooter;

    public XRecyclerView(Context context) {
        super(context);
        init();
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化方法
     */
    public void init(){
        initRefreshHeader();
        initFooterView();
    }

    /**
     * 初始化下拉刷新头
     */
    public void initRefreshHeader(){
        if (pullRefreshEnabled) {
            mRefreshHeader = new ArrowRefreshHeader(getContext());
        }
    }

    /**
     * 初始化底部的加载更多布局
     */
    public void initFooterView(){
        mFooter = new LoadMoreFooter(getContext());
    }

    /**
     * 设置加载更多的开关
     * @param enabled
     */
    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
        if (!enabled) {
            if (mFooter instanceof LoadMoreFooter) {
                mFooter.loadMoreComplete();
            }
        }
    }

    /**
     * 设置监听回调
     * @param listener 监听回调接口
     */
    public void setLoadingListener(XRecyclerViewLoadingListener listener) {
        mXRecyclerViewLoadingListener = listener;
    }

    /**
     * 完成加载更多
     */
    public void loadMoreComplete(){
        if(mFooter instanceof LoadMoreFooter){
            mFooter.loadMoreComplete();
        }else {
            mFooter.setVisibility(GONE);
        }
    }

    public void setNoMore(boolean noMore) {
        if(mFooter instanceof LoadMoreFooter){
            mFooter.setNoMore(noMore);
        }else {
            mFooter.setVisibility(GONE);
        }
    }

    /**
     * 设置刷新的开关
     * @param enabled
     */
    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    /**
     * 完成刷新
     */
    public void refreshComplete() {
        mRefreshHeader.refreshComplete();
    }

    /**
     * 刷新完成后，重置状态
     */
    public void reset(){
        loadMoreComplete();
        refreshComplete();
    }

    /**
     * 添加头部布局，可添加多个
     * @param view 任一布局
     */
    public void addHeaderView(View view) {
        sHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size()); // 每个头布局都对应一个标量
        mHeaderViews.add(view);
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 判断一个type是否为头部布局
     * @param itemViewType
     * @return boolean
     */
    private boolean isHeaderType(int itemViewType) {
        return  mHeaderViews.size() > 0 &&  sHeaderTypes.contains(itemViewType);
    }

    /**
     * 判断是否是XRecyclerView已定义的itemViewType
     * @param itemViewType
     * @return boolean
     */
    private boolean isReservedItemViewType(int itemViewType) {
        return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_FOOTER || sHeaderTypes.contains(itemViewType);
    }

    /**
     * 根据header的ViewType标量判断是哪个headerView,并返回
     * @param itemType
     * @return headerView
     */
    private View getHeaderViewByType(int itemType) {
        if(!isHeaderType(itemType)) {
            return null;
        }
        return mHeaderViews.get(itemType - HEADER_INIT_INDEX);
    }

    /**
     * 设置适配器，并对适配器进行监听
     * @param adapter 适配器
     */
    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver); // 注册适配器数据监听
        mDataObserver.onChanged(); // 刷新适配器
    }

    /**
     * 避免用户自己调用getAdapter() 引起的ClassCastException
     */
    @Override
    public Adapter getAdapter() {
        if(mWrapAdapter != null)
            return mWrapAdapter.getOriginalAdapter();
        else
            return null;
    }

    /**
     * 设置layoutManager
     * @param layout layoutManager
     */
    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if(mWrapAdapter != null){
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        // 为不同类型的布局返回span个数
                        return (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position)
                                || mWrapAdapter.isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }
    }

    /**
     * 是否在顶部
     * @return boolean
     */
    private boolean isOnTop() {
        return mRefreshHeader.getParent() != null;
    }

    /**
     * 是否在底部
     * @return boolean
     */
    private boolean isOnBottom(){
        LayoutManager layoutManager = getLayoutManager();
        int lastVisibleItemPosition = 0;
        if(layoutManager instanceof GridLayoutManager){
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        }else if(layoutManager instanceof LinearLayoutManager){
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }else if(layoutManager instanceof StaggeredGridLayoutManager){
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
            lastVisibleItemPosition = findMax(into);
        }

        return layoutManager.getChildCount() > 0
                && lastVisibleItemPosition >= mWrapAdapter.getItemCount() - 2;
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY(); // 纵轴移动的绝对距离
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY; // 纵轴滑动的距离
                mLastY = ev.getRawY();
                if(isOnTop()){
                    if(pullRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED){
                        mRefreshHeader.onMove(deltaY / DRAG_RATE);
                        // 下拉距离不够大时，不调用刷新
                        if (mRefreshHeader.getVisibleHeight() > 0 && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                            return false;
                        }
                    }
                }else if(isOnBottom()){
                    if (loadingMoreEnabled) {
                        mFooter.onMove(Math.abs(deltaY) / DRAG_RATE);
                        // 上拉距离不够大时，不调用加载
                        if (mFooter.getVisibleHeight() > 0 && mFooter.getState() < LoadMoreFooter.STATE_LOADING) {
                            return false;
                        }
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop()){
                    if(pullRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                        if (mRefreshHeader.releaseAction()) {
                            if (mXRecyclerViewLoadingListener != null) {
                                mXRecyclerViewLoadingListener.onRefresh();
                            }
                        }
                    }
                }else if(isOnBottom()) {
                    if(pullRefreshEnabled){
                        if (mFooter.releaseAction(computeVerticalScrollExtent())) {
                            if (mXRecyclerViewLoadingListener != null) {
                                mXRecyclerViewLoadingListener.onLoadMore();
                            }
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 更新mAdapter里面的数据的时候，进行监听，再更新
     */
    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {

        private RecyclerView.Adapter adapter;

        public WrapAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        public RecyclerView.Adapter getOriginalAdapter() {
            return this.adapter;
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public boolean isHeader(int position) {
            return position >= 1 && position < mHeaderViews.size() + 1;
        }

        public int getHeadersCount() {
            return mHeaderViews.size();
        }

        /**
         * 是不是底部布局
         */
        public boolean isFooter(int position) {
            return loadingMoreEnabled && position == getItemCount() - 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mRefreshHeader);
            } else if (isHeaderType(viewType)) {
                return new SimpleViewHolder(getHeaderViewByType(viewType));
            }if (viewType == TYPE_FOOTER) {
                return new SimpleViewHolder(mFooter);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // 头部和刷新头不进行操作
            if (isHeader(position) || isRefreshHeader(position)) {
                return;
            }
            int adjPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                // 当有普通item添加时，进行适配
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                }
            }
        }

        @Override
        public int getItemCount() {
            // 有无底部布局
            if(loadingMoreEnabled) {
                if (adapter != null) {
                    return getHeadersCount() + adapter.getItemCount() + 2;
                } else {
                    return getHeadersCount() + 2;
                }
            }else {
                if (adapter != null) {
                    return getHeadersCount() + adapter.getItemCount() + 1;
                } else {
                    return getHeadersCount() + 1;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            int adjPosition = position - (getHeadersCount() + 1);
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)) {
                position = position - 1;
                return sHeaderTypes.get(position);
            }
            if (isFooter(position)) {
                return TYPE_FOOTER;
            }
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                // 当有普通item时，返回类型
                if (adjPosition < adapterCount) {
                    int type =  adapter.getItemViewType(adjPosition);
                    if(isReservedItemViewType(type)) {
                        throw new IllegalStateException("XRecyclerView require itemViewType in adapter should be less than 10000 " );
                    }
                    return type;
                }
            }
            return 0;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position)
                                || isRefreshHeader(position)) ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.getLayoutPosition()) || isRefreshHeader(holder.getLayoutPosition())
                    || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //解决和CollapsingToolbarLayout冲突的问题
        AppBarLayout appBarLayout = null;
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if(p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout)p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if(child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout)child;
                    break;
                }
            }
            if(appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }
    }
}
