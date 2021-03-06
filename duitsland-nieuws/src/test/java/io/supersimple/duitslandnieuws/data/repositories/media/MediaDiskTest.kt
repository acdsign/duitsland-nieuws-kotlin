package io.supersimple.duitslandnieuws.data.repositories.media

import io.supersimple.duitslandnieuws.data.parcel.writeParcelable
import io.supersimple.duitslandnieuws.data.repositories.media.MediaRepositoryTest.Companion.testMediaId
import io.supersimple.duitslandnieuws.data.repositories.media.MediaRepositoryTest.Companion.testMediaItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MediaDiskTest {

    val fileDir: File = RuntimeEnvironment.application.applicationContext.filesDir

    @Before
    fun setup() {
        val testDir = File(fileDir.path, MediaDisk.MEDIA_DIR_NAME)
        testDir.deleteRecursively()
    }

    @Test
    fun testGet() {

        val mediaItem = testMediaItem
        val disk = MediaDisk(fileDir)

        disk.get(mediaItem.id)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues()

        val mediaItemFile = MediaDisk.fileForMediaItem(mediaItem, fileDir)
        assertTrue(mediaItemFile.writeParcelable(mediaItem))

        disk.get(mediaItem.id)
                .test()
                .assertResult(mediaItem)
    }

    @Test
    fun testSave() {
        val mediaItem = testMediaItem
        val mediaItemFile = MediaDisk.fileForMediaItem(mediaItem, fileDir)
        assertFalse(mediaItemFile.exists())

        val disk = MediaDisk(fileDir)
        disk.save(mediaItem)
                .test()
                .assertResult(mediaItem)

        assertTrue(mediaItemFile.exists())
    }

    @Test
    fun testDeleteId() {
        val disk = MediaDisk(fileDir)

        val mediaItem = testMediaItem
        val mediaItemFile = MediaDisk.fileForMediaItem(mediaItem, fileDir)
        assertTrue(mediaItemFile.writeParcelable(mediaItem))
        assertTrue(mediaItemFile.exists())

        disk.delete(testMediaId)
                .test()
                .assertResult(mediaItem)

        assertFalse(mediaItemFile.exists())
    }

    @Test
    fun testDelete() {
        val disk = MediaDisk(fileDir)

        val mediaItem = testMediaItem
        val mediaItemFile = MediaDisk.fileForMediaItem(mediaItem, fileDir)
        assertTrue(mediaItemFile.writeParcelable(mediaItem))
        assertTrue(mediaItemFile.exists())

        disk.delete(mediaItem)
                .test()
                .assertResult(mediaItem)

        assertFalse(mediaItemFile.exists())
    }

    @Test
    fun testDeleteAll() {
        val disk = MediaDisk(fileDir)

        val mediaItem = testMediaItem
        val mediaItem2 = mediaItem.copy()
        val mediaItemFile = MediaDisk.fileForMediaItem(mediaItem, fileDir)
        val mediaItemFile2 = MediaDisk.fileForMediaItem(mediaItem2, fileDir)
        assertTrue(mediaItemFile.writeParcelable(mediaItem))
        assertTrue(mediaItemFile.writeParcelable(mediaItem2))
        assertTrue(mediaItemFile.exists())
        assertTrue(mediaItemFile2.exists())

        disk.deleteAll()
                .test()
                .assertComplete()

        assertFalse(mediaItemFile.exists())
        assertFalse(mediaItemFile2.exists())
    }

}