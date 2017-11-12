package com.supsim.redditexplorer.Views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class ListViewWithoutScroll extends ListView {

    public ListViewWithoutScroll(Context context){
        super(context);
    }

    public ListViewWithoutScroll(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public ListViewWithoutScroll(Context context, AttributeSet attributeSet, int style){
        super(context, attributeSet, style);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST
        );
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
