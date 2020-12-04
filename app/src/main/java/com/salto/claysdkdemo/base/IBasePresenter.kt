package com.salto.claysdkdemo.base

interface IBasePresenter {

    interface View

    interface Action<V> {

        fun bindView(view: V)
    }
}