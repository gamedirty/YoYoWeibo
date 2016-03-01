package com.sovnem.yoyoweibo.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 我的里边的 微博fragment
 * Created by 赵军辉 on 2016/2/29.
 */
public class MyTimelineFragment extends Fragment {
    public static MyTimelineFragment getInstance() {
        return new MyTimelineFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("微博");
        return textView;
    }
}
