package com.standbyus.app.data.repository

import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseConfigurationException
import com.standbyus.app.data.remote.SupabaseHttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CheckinSubmissionTest {
    private val record = CheckinData(userId = "existing-device", timestamp = 123L)

    @Test fun cachesTheAcknowledgedRecordAndKeepsItsOriginalIdentity() = runTest {
        val acknowledged = record.copy(id = 42)
        var cached: CheckinData? = null
        val result = persistCheckin(record, { acknowledged }, { cached = it })
        assertEquals(CheckinSubmitResult.Saved(true), result)
        assertEquals(acknowledged, cached)
    }

    @Test fun networkFailureDoesNotInventALocalSuccess() = runTest {
        var cached = false
        val result = persistCheckin(record, { throw IOException("offline") }, { cached = true })
        assertEquals(CheckinSubmitResult.Failed(CheckinFailure.NETWORK), result)
        assertFalse(cached)
    }

    @Test fun emptyResponseCannotBeReportedAsSuccess() = runTest {
        val result = persistCheckin(record, { null }, { fail("must not cache") })
        assertEquals(CheckinSubmitResult.Failed(CheckinFailure.INVALID_RESPONSE), result)
    }

    @Test fun responseForAnotherIdentityCannotContaminateTheCache() = runTest {
        val result = persistCheckin(record, { record.copy(userId = "someone-else") }, { fail("must not cache") })
        assertEquals(CheckinSubmitResult.Failed(CheckinFailure.INVALID_RESPONSE), result)
    }

    @Test fun acknowledgedWriteStaysSuccessfulIfLocalCacheFails() = runTest {
        val result = persistCheckin(record, { record }, { error("disk full") })
        assertEquals(CheckinSubmitResult.Saved(false), result)
    }

    @Test fun distinguishesBrokenPackageConfigurationFromNetworkAndPermissions() = runTest {
        assertEquals(CheckinFailure.CONFIGURATION, CheckinFailure.from(SupabaseConfigurationException()))
        assertEquals(CheckinFailure.ACCESS_DENIED, CheckinFailure.from(SupabaseHttpException(403, "denied")))
        assertEquals(CheckinFailure.SERVER, CheckinFailure.from(SupabaseHttpException(503, "unavailable")))
    }

    @Test fun cancellationPropagatesWithoutWritingCache() = runTest {
        try {
            persistCheckin(record, { throw CancellationException() }, { fail("must not cache") })
            fail("cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
