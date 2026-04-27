package io.github.posaydone.kinopub.core.common.sharedViewModel

import io.github.posaydone.kinopub.core.model.kinopub.KinoPubContentType
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubGenreType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShowsGridScreenViewModelTest {

    @Test
    fun `genre type maps movie family to movie`() {
        assertEquals(KinoPubGenreType.MOVIE, genreTypeForContentType(KinoPubContentType.MOVIE))
        assertEquals(KinoPubGenreType.MOVIE, genreTypeForContentType(KinoPubContentType.SERIAL))
        assertEquals(KinoPubGenreType.MOVIE, genreTypeForContentType(KinoPubContentType.FILM_3D))
    }

    @Test
    fun `genre type maps concert to music`() {
        assertEquals(KinoPubGenreType.MUSIC, genreTypeForContentType(KinoPubContentType.CONCERT))
    }

    @Test
    fun `genre type maps documentary family to docu`() {
        assertEquals(KinoPubGenreType.DOCU, genreTypeForContentType(KinoPubContentType.DOCUMOVIE))
        assertEquals(KinoPubGenreType.DOCU, genreTypeForContentType(KinoPubContentType.DOCUSERIAL))
    }

    @Test
    fun `genre type maps tvshow to tvshow`() {
        assertEquals(KinoPubGenreType.TVSHOW, genreTypeForContentType(KinoPubContentType.TVSHOW))
    }

    @Test
    fun `genre type is null for all content types`() {
        assertNull(genreTypeForContentType(null))
    }
}
