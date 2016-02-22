package com.sovnem.yoyoweibo.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sovnem.data.utils.L;
import com.sovnem.yoyoweibo.R;
import com.sovnem.yoyoweibo.model.WeiboProvider;
import com.sovnem.yoyoweibo.utils.T;
import com.sovnem.yoyoweibo.view.adapter.StatussAdapter;
import com.sovnem.yoyoweibo.widget.LoadMoreListview;

import java.util.ArrayList;

/**
 * 首页fragment
 * Created by 赵军辉 on 2015/12/31.
 * <p/>
 * 加载更多和  加载最新
 * <p/>
 * <p/>
 * 加载更多是
 */
public class FirstFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    public static final String TITLE = "首页";
    private LoadMoreListview mlv;
    private SwipeRefreshLayout srl;
    private ArrayList<Status> statuses;
    private String newest, oldest;//最新微博和最老微博的id
    private StatussAdapter adapter;

    @Override
    public void setHead(TextView title) {
        super.setHead(title);
        title.setText(TITLE);
    }

    public static FirstFragment getInstance() {
        FirstFragment ff = new FirstFragment();
        return ff;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void initViews() {
        mlv = (LoadMoreListview) getView().findViewById(R.id.listview_first_list);
        srl = (SwipeRefreshLayout) getView().findViewById(R.id.srfl_firstpage_refresh);
        srl.setColorSchemeColors(getResources().getColor(R.color.globalcolornormal), getResources().getColor(R.color.globalcolorpress));
        srl.setOnRefreshListener(this);
    }

    @Override
    public void initData() {
        statuses = new ArrayList<>();
        firstRequest();
    }

    /**
     * 第一次请求
     */
    private void firstRequest() {
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(true);
                WeiboProvider.getFriendsWeibos(getActivity(), new RequestListener() {
                    @Override
                    public void onComplete(String s) {
                        showNewData(s);
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                    }
                });
            }
        });
    }

    private void showNewData(String s) {
        srl.setRefreshing(false);
        StatusList statuss = StatusList.parse(s);
        statuses.addAll(0, statuss.statusList);
        recordId();
        adapter = new StatussAdapter(getActivity(), statuss.statusList);
        mlv.setAdapter(adapter);
        mlv.setOnScrollListener(this);
    }

    private void recordId() {
        newest = statuses.get(0).id;
        oldest = statuses.get(statuses.size() - 1).id;
    }

    /**
     * 下拉刷新回调，在这里加载最新的微博
     */
    @Override
    public void onRefresh() {
        WeiboProvider.getFriendsWeibosAfter(newest, getActivity(), new RequestListener() {
            @Override
            public void onComplete(String s) {
                addNewestStatus(s);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                L.i(e.getMessage());
            }
        });
    }

    /**
     * 加入最新微博并更新列表
     *
     * @param s
     */
    private void addNewestStatus(String s) {
        srl.setRefreshing(false);
        StatusList list = StatusList.parse(s);


        if (list.statusList == null || list.statusList.size() == 0) {
            T.show(getActivity(), "没有新微博", Toast.LENGTH_LONG);
            return;
        }
        newest = list.statusList.get(0).id;
        if (adapter != null) {
            adapter.addNewStatus(list.statusList);
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            Glide.with(this).pauseRequests();
        } else {
            Glide.with(this).resumeRequests();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        L.i("总共item个数:" + totalItemCount);
        if (firstVisibleItem + visibleItemCount - 1 == srl.getChildCount() - 1 &&
                srl.getChildAt(srl.getChildCount() - 1).getBottom() <= srl.getHeight()) {
            loadMore();
        }
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        L.i("加载更多");
        mlv.setStatusLoading(true);
        WeiboProvider.getFriendsWeibosAfter(oldest, getActivity(), new RequestListener() {
            @Override
            public void onComplete(String s) {
                addOldStatuss(s);
                mlv.setStatusLoading(false);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                mlv.setStatusLoading(false);
            }
        });
    }

    private void addOldStatuss(String s) {
        StatusList list = StatusList.parse(s);
        if (list.statusList == null) {
            return;
        }
        adapter.addOldStatuses(list.statusList);
        oldest = list.statusList.get(list.statusList.size() - 1).id;
    }
}
