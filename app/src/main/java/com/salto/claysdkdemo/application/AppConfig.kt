package com.salto.claysdkdemo.application

import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_ID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.DEVICE_UUID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.ODATA_FILTER_DEVICE_UUID
import com.salto.claysdkdemo.application.AppConfig.ApiParams.USER_ID

object AppConfig {

    object RequestCodes {
        const val AUTH_CODE: Int = 1000
        const val LOGOUT: Int = 1001
        const val REQUEST_FINE_LOCATION_PERMISSION: Int = 1002
    }

    object Endpoints {
        private const val UAP_VERSION = "/v1.1"
        private const val ME_DEVICES = "$UAP_VERSION/me/devices"
        const val REGISTER_MKEY_DEVICE = ME_DEVICES
        const val GET_MOBILE_KEY = "$ME_DEVICES/{$USER_ID}/mkey"
        const val MKEY_DEVICE = "$ME_DEVICES/{$DEVICE_ID}"
        const val MKEY_DEVICE_LIST = ME_DEVICES
        const val PUT_MKEY_DEVICE_CERTIFICATE = "$ME_DEVICES/{$USER_ID}/certificate"
    }

    object ApiParams {
        const val USER_ID = "userId"
        const val DEVICE_ID = "deviceId"
        const val DEVICE_NAME = "device_name"
        const val DEVICE_UUID = "device_uid"
        const val PUBLIC_KEY = "public_key"
        const val DEVICE_UUID_PLACE_HOLDER = "uuidPlaceHolder"
        const val ODATA_FILTER = "\$filter"
        const val ODATA_FILTER_DEVICE_UUID = "$DEVICE_UUID eq \'$DEVICE_UUID_PLACE_HOLDER\'"
    }

    object Dagger {
        const val RESOURCE_URL = "resourceUrl"
        const val API_KEY = "apiKey"
    }

    object Timers {
        const val MKEY_TIMEOUT_THRESHOLD: Long = 5000
        const val MKEY_BLUETOOTH_ON_RETRY_DOUBLE: Long = 600
        const val MKEY_BLUETOOTH_ON_RETRY: Long = 300
    }
}