package com.salto.claysdkdemo.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quality.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.login.presenters.ILoginPresenter
import com.salto.claysdkdemo.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class LoginActivity : SaltoActivity<ILoginPresenter.View, ILoginPresenter.Action>(), ILoginPresenter.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(presenter.isLoggedIn) {
            onLoginSuccess()
            return
        }

        login_button.apply {
            setOnClickListener {
                presenter.login()
            }
            visibility = View.VISIBLE
        }
    }

    override fun bindView() {
        presenter.bindView(this)
    }

    override fun onLoginSuccess() {
        login_button.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun hideProgress() {
        login_button.isEnabled = true
        progress_bar.visibility = View.GONE
    }

    override fun onLoginError() {
        login_button.isEnabled = true
        hideProgress()
    }

    override fun displayOpenIDIntent(authIntent: Intent) {
        login_button.isEnabled = false
        progress_bar.visibility = View.VISIBLE
        startActivityForResult(authIntent, AppConfig.RequestCodes.AUTH_CODE)
    }

    override fun displayBrowserError() {
        showMessageDialog(getString(R.string.browser_not_found))
    }

    override fun displayError(error: String) {
        hideProgress()
        showMessageDialog(error)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AppConfig.RequestCodes.AUTH_CODE && data != null) {
            presenter.exchangeToken(AuthorizationResponse.fromIntent(data), AuthorizationException.fromIntent(data))
        }
    }
}