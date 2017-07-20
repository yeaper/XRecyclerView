package com.yyp.xrecyclerview.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yyp.xrecyclerview.widget.XRecyclerView;

/**
 * Created by yyp on 2017/7/20.
 */

public class IsBottom {


    public static boolean isLinearBottom(XRecyclerView recyclerView) {
        boolean isBottom = true;
        int scrollY = getLinearScrollY(recyclerView);
        int totalHeight = getLinearTotalHeight(recyclerView);
        int height = recyclerView.getHeight();
        //    Log.e("height","scrollY  " + scrollY + "  totalHeight  "
        // +  totalHeight + "  recyclerHeight  " + height);
        if (scrollY + height < totalHeight) {
            isBottom = false;
        }
        return isBottom;
    }

    /**
     * 算出所有子项的总高度
     * @param recyclerView
     * @return
     */
    public static int getLinearTotalHeight(XRecyclerView recyclerView) {
        int totalHeight = 0;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        View child = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition());
        int headerChildHeight = getHeaderHeight(recyclerView);
        if (child != null) {
            int itemHeight = getItemHeight(recyclerView);
            int childCount = layoutManager.getItemCount();
            totalHeight = headerChildHeight + (childCount - 1) * itemHeight;
        }
        return totalHeight;
    }

    /**
     * 算出滑过的子项的总距离
     * @param recyclerView
     * @return
     */
    public static int getLinearScrollY(XRecyclerView recyclerView) {
        int scrollY = 0;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int headerChildHeight = getHeaderHeight(recyclerView);
        int firstPos = layoutManager.findFirstVisibleItemPosition();
        View child = layoutManager.findViewByPosition(firstPos);
        int itemHeight = getItemHeight(recyclerView);
        if (child != null) {
            int firstItemBottom = layoutManager.getDecoratedBottom(child);
            scrollY = headerChildHeight + itemHeight * firstPos - firstItemBottom;
            if(scrollY < 0){
                scrollY = 0;
            }
        }
        return scrollY;
    }

    /**
     * 算出一个子项的高度
     * @param recyclerView
     * @return
     */
    public static int getItemHeight(XRecyclerView recyclerView) {
        int itemHeight = 0; View child = null;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstPos = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastPos = layoutManager.findLastCompletelyVisibleItemPosition();
        child = layoutManager.findViewByPosition(lastPos);
        if (child != null) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            itemHeight = child.getHeight() + params.topMargin + params.bottomMargin;
        }
        return itemHeight;
    }

    public static int getHeaderHeight(XRecyclerView recyclerView){
        return recyclerView.getHeight();
    }
}
