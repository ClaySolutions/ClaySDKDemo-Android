package com.salto.claysdkdemo.base

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.quality.claysdkdemo.R
import dagger.android.AndroidInjection
import javax.inject.Inject

abstract class SaltoActivity<V: IBasePresenter.View, P: IBasePresenter.Action<V>>: AppCompatActivity() {

    @Inject
    lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        bindView()
    }

    abstract fun bindView()

    open fun showMessageDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
                    run {
                        dialogInterface.cancel()
                    }
                }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }
}