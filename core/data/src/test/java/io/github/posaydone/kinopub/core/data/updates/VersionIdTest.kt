package io.github.posaydone.kinopub.core.data.updates

import org.junit.Assert.assertTrue
import org.junit.Test

class VersionIdTest {
    @Test
    fun stableReleaseIsNewerThanBetaWithSameNumbers() {
        assertTrue(VersionId("1.4.0") > VersionId("1.4.0-beta1"))
    }

    @Test
    fun releaseCandidateIsNewerThanBeta() {
        assertTrue(VersionId("2.0.0-rc1") > VersionId("2.0.0-beta3"))
    }

    @Test
    fun newerMinorVersionWins() {
        assertTrue(VersionId("1.2.0") > VersionId("1.1.9"))
    }

    @Test
    fun twoPartVersionIsParsedCorrectly() {
        assertTrue(VersionId("0.2") > VersionId("0.1"))
    }
}
