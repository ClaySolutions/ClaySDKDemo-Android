package com.salto.claysdkdemo.access_code

import android.os.Bundle
import android.view.View
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.access_code.presenters.IAccessCodePresenter
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.databinding.ActivityAccessCodeBinding

class AccessCodeActivity : SaltoActivity<IAccessCodePresenter.View, IAccessCodePresenter.Action>(),
    IAccessCodePresenter.View {

    lateinit var binding: ActivityAccessCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccessCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            binding.saveButton.setOnClickListener click@ {
                if (accessCodeEt.text.isNullOrEmpty() || firstNameEt.text.isNullOrEmpty() || lastNameEt.text.isNullOrEmpty()) {
                    return@click
                }

                saveButton.isEnabled = false
                progressBar.visibility = View.VISIBLE
                presenter.didTapSave(accessCodeEt.text.toString(), firstNameEt.text.toString(), lastNameEt.text.toString())
            }
        }

    }

    override fun didGetError(error: String?) {
        binding.apply {
            saveButton.isEnabled = true
            progressBar.visibility = View.GONE
        }
        showMessageDialog(getString(R.string.verify_access_code))
    }

    override fun didSaveGDKey() {
        finish()
    }

    override fun bindView() {
        presenter.bindView(this)
    }

}