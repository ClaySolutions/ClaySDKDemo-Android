package com.salto.claysdkdemo.modules

import android.content.Context
import android.net.Uri
import com.myclay.claysdk.api.ClaySDK
import com.myclay.claysdk.api.IClaySDK
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.application.App
import com.salto.claysdkdemo.application.AppConfig.Dagger.API_KEY
import com.salto.claysdkdemo.application.AppConfig.Dagger.IDENTITY_SERVER
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.application.SharedPrefsUtil
import com.salto.claysdkdemo.login.oid.IOIDConfig
import com.salto.claysdkdemo.login.oid.OIDConfig
import com.salto.claysdkdemo.utils.DeviceInfo
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(private val application: App) {

    @Provides
    @Singleton
    fun provideApplication(): Context = application

    @Provides
    @Singleton
    fun provideSharedPrefs(context: Context): ISharedPrefsUtil {
        return SharedPrefsUtil(context)
    }

    @Provides
    @Singleton
    fun provideAuthorizationService(context: Context): AuthorizationService {
        return AuthorizationService(context)
    }

    @Provides
    fun provideIOIDConfig(context: Context, @Named(IDENTITY_SERVER) idsUrl: String): IOIDConfig = OIDConfig(context, idsUrl)

    @Provides
    fun provideOIDConfig(context: Context, @Named(IDENTITY_SERVER) idsUrl: String): OIDConfig = OIDConfig(context, idsUrl)

    @Provides
    @Singleton
    fun provideAuthState(
        serviceConfig: AuthorizationServiceConfiguration,
        sharedPrefs: ISharedPrefsUtil
    ): AuthState {
        return sharedPrefs.readAuthState() ?: AuthState(serviceConfig)
    }

    @Provides
    fun provideAuthorizationServiceConfiguration(@Named(IDENTITY_SERVER) idsUrl: String): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse("$idsUrl${OIDConfig.Endpoint.AUTHORIZATION}"),
            Uri.parse("$idsUrl${OIDConfig.Endpoint.TOKEN}")
        )
    }

    @Provides
    @Singleton
    @Named(IDENTITY_SERVER)
    fun provideIdentityServerUrl(context: Context): String {
        return context.getString(R.string.identity_server_url)
    }

    @Provides
    @Singleton
    @Named(API_KEY)
    fun provideApiKey(context: Context): String {
        return context.getString(R.string.public_api_key)
    }

    @Provides
    @Singleton
    fun provideClaySDK(context: Context, @Named(API_KEY) apiKey: String): IClaySDK {
        return ClaySDK.init(context, apiKey, DeviceInfo.uniquePseudoID(context))
    }
}