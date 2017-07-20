package com.yyp.xrecyclerview.widget.interfaces;

public interface BaseFooter {

	int STATE_NORMAL = 0; // 正常状态
	int STATE_LOADING = 1; // 加载状态
	int STATE_DONE = 2; // 加载完成状态
	int STATE_NOMORE = 3; // 全部加载完成状态

	void onMove(float delta);

	boolean releaseAction(float height);

	void loadMoreComplete();

	void setNoMore(boolean noMore);
}