package com.salto.claysdkdemo.modules

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.salto.claysdkdemo.BuildConfig
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.application.AppConfig.Dagger.RESOURCE_URL
import com.salto.claysdkdemo.application.ISharedPrefsUtil
import com.salto.claysdkdemo.retrofit.IDeviceRetrofit
import com.salto.claysdkdemo.retrofit.IUAPInterceptor
import com.salto.claysdkdemo.retrofit.UAPInterceptor
import com.salto.claysdkdemo.services.DeviceService
import com.salto.claysdkdemo.services.IDeviceService
import com.salto.claysdkdemo.services.IOIDAuthenticator
import com.salto.claysdkdemo.services.OIDAuthenticator
import dagger.Lazy
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import okhttp3.Call
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class ServiceModule {

    @Provides
    @Singleton
    @Named(RESOURCE_URL)
    fun provideResourceUrl(context: Context): String {
        return context.getString(R.string.user_api_url)
    }

    @Provides
    @Singleton
    fun provideResourceBuilder(@Named(RESOURCE_URL) url: String): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
    }

    @Provides
    @Singleton
    fun provideResourceRetrofit(
        builder: Lazy<Retrofit.Builder>,
        client: Lazy<Call.Factory>
    ): Retrofit {
        return builder.get().client(client.get() as OkHttpClient).build()
    }

    @Provides
    fun provideLoggingInterceptor(level: HttpLoggingInterceptor.Level): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(level)
        return loggingInterceptor
    }

    @Provides
    @Singleton
    fun provideDebugLevel(): HttpLoggingInterceptor.Level {
        return if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else HttpLoggingInterceptor.Level.NONE
    }

    @Provides
    fun provideClient(
        interceptor: Lazy<IUAPInterceptor>,
        certificatePinner: CertificatePinner,
        loggingInterceptor: Lazy<HttpLoggingInterceptor>,
    ): Call.Factory {
        val builder = OkHttpClient.Builder()
        return builder
                .addInterceptor(interceptor.get())
                .addInterceptor(loggingInterceptor.get())
                .certificatePinner(certificatePinner)
                .build()
    }

    @Provides
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("*.my-clay.com", "sha256/yVro9s/ZvGfUV1VIx86AsxBvVekxFKfpcqOhAjX02RM=")
            .build()
    }

    @Provides
    fun provideInterceptor(context: Context, sharedPrefs: ISharedPrefsUtil): IUAPInterceptor {
        return UAPInterceptor(context, sharedPrefs)
    }

    @Provides
    @Singleton
    fun provideOIDAuthenticator(
        authorizationService: AuthorizationService, state: AuthState, handler: Handler
    ): IOIDAuthenticator {
        return OIDAuthenticator(authorizationService, state, handler)
    }

    @Provides
    fun provideMainHandler(): Handler {
        return Handler(Looper.getMainLooper())
    }

    @Provides
    @Singleton
    fun provideDeviceService(
        retrofit: IDeviceRetrofit,
        authenticator: IOIDAuthenticator
    ): IDeviceService {
        return DeviceService(retrofit, authenticator)
    }

    @Provides
    @Singleton
    fun provideRetrofitMKey(retrofit: Retrofit): IDeviceRetrofit {
        return retrofit.create(IDeviceRetrofit::class.java)
    }
}