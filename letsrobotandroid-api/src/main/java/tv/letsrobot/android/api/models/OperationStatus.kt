package tv.letsrobot.android.api.models

import androidx.annotation.IntDef

/**
 * Created by Brendon on 12/25/2018.
 */
object Operation {
    @IntDef(NOT_OK, LOADING, OK)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Status

    const val NOT_OK = 0
    const val LOADING = 1
    const val OK = 2
}