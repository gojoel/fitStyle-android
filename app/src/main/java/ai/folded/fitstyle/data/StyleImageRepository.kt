package ai.folded.fitstyle.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository module for handling data operations.
 *
 * Collecting from the Flows in [StyleImageDao] is main-safe. Room supports Coroutines and moves the
 * query execution off of the main thread.
 */
@Singleton
class StyleImageRepository @Inject constructor(private val styleImageDao: StyleImageDao) {

    fun getStyles() = styleImageDao.getStyles()

    fun getStyle(styleId: Int) = styleImageDao.getStyle(styleId)
}