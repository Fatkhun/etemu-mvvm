package com.fatkhun.core.repository

import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.network.RetrofitInstance

class MainRepository(
    private val retrofitInstance: RetrofitInstance,
    private val networkHelper: NetworkHelper,
    private val storeDataHelper: StoreDataHelper
) {

}