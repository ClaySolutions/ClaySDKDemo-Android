package com.salto.claysdkdemo.base

import android.content.Context
import com.salto.claysdkdemo.application.ISharedPrefsUtil

abstract class BasePresenter<V: IBasePresenter.View>(val context: Context, val sharedPrefs: ISharedPrefsUtil)
    : IBasePresenter.Action<V> {

    var view: V? = null

    override fun bindView(view: V) {
        this.view = view
    }
}