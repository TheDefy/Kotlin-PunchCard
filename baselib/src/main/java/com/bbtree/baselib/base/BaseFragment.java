package com.bbtree.baselib.base;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bbtree.baselib.utils.StringUtils;


/**
 * Fragment基础类
 * Created by zhou on 14-7-30.
 */
public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    protected ProgressDialog mProgressDialog;
    private View rootView;

    /**
     * 用此方法替代setContentView设置ContentView
     *
     * @return
     */
    public abstract int contentView();

    /**
     * view的初始化
     */
    public abstract void initView(Bundle savedInstanceState);

    /**
     * 获取当前view
     *
     * @return
     */
    public View getContentView() {
        return rootView;
    }

    /**
     * 寻找ID
     *
     * @param resID
     * @return
     */
    public final View findViewById(int resID) {
        if (rootView != null) {
            return rootView.findViewById(resID);
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView != null && (Boolean) rootView.getTag()) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(contentView(), container, false);
            rootView.setTag(true);
            initView(savedInstanceState);
            onDetach();
        }
        return rootView;
    }

    public ProgressDialog showProgress(String title, String message) {
        return showProgress(title, message, -1);
    }

    public ProgressDialog showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(mContext, theme);
            else
                mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
