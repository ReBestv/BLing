package com.standbyus.app.data.repository

import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseConfigurationException
import com.standbyus.app.data.remote.SupabaseHttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException

enum class CheckinFailure {
    CONFIGURATION, NETWORK, ACCESS_DENIED, SERVER, INVALID_RESPONSE, IDENTITY, UNKNOWN;

    val submitMessage: String
        get() = when (this) {
            CONFIGURATION -> "当前版本无法连接同步服务，请安装配置完整的修复版"
            NETWORK -> "打卡未成功，请检查网络后重试"
            ACCESS_DENIED -> "打卡未成功，同步服务暂时拒绝访问，请稍后重试"
            SERVER -> "打卡未成功，同步服务暂时不可用，请稍后重试"
            INVALID_RESPONSE -> "未能确认打卡结果，请稍后刷新记录"
            IDENTITY -> "暂时无法读取本机身份，请重新打开应用"
            UNKNOWN -> "打卡未成功，请稍后重试"
        }

    companion object {
        fun from(error: Exception): CheckinFailure = when (error) {
            is SupabaseConfigurationException -> CONFIGURATION
            is SupabaseHttpException -> if (error.statusCode == 401 || error.statusCode == 403) ACCESS_DENIED else SERVER
            is IOException -> NETWORK
            else -> UNKNOWN
        }
    }
}

sealed interface CheckinSubmitResult {
    data class Saved(val cached: Boolean) : CheckinSubmitResult
    data class Failed(val reason: CheckinFailure) : CheckinSubmitResult
}

/** A failed local cache write must never turn an acknowledged remote write into a retry. */
internal suspend fun persistCheckin(
    record: CheckinData,
    create: suspend (CheckinData) -> CheckinData?,
    cache: suspend (CheckinData) -> Unit
): CheckinSubmitResult {
    if (record.userId.isBlank()) return CheckinSubmitResult.Failed(CheckinFailure.IDENTITY)
    val saved = try {
        create(record)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        return CheckinSubmitResult.Failed(CheckinFailure.from(error))
    }
    if (saved == null || saved.userId != record.userId || saved.timestamp != record.timestamp) {
        return CheckinSubmitResult.Failed(CheckinFailure.INVALID_RESPONSE)
    }
    val cached = try {
        cache(saved)
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }
    return CheckinSubmitResult.Saved(cached)
}
