package com.standbyus.app.data.remote

import java.io.IOException

class SupabaseConfigurationException : IOException("同步服务未配置")

class SupabaseHttpException(val statusCode: Int, message: String) : IOException(message)
