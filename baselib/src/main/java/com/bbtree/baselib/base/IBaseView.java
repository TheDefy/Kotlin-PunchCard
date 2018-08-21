
package com.bbtree.baselib.base;

import android.content.Context;

/**
 * Description :所有View的基类，所有的View继承此View
 */
public interface IBaseView<T> {
    /**
     * 给View设置Presenter
     * @param presenter
     */
    void setPresenter(T presenter);

    /**
     * 获取Context方便P层使用
     * @return
     */
    Context getContext();
}
