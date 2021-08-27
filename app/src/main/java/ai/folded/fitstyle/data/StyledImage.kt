package ai.folded.fitstyle.data

import ai.folded.fitstyle.utils.BUCKET_REQUESTS
import ai.folded.fitstyle.utils.PREVIEW_IMAGE_NAME
import ai.folded.fitstyle.utils.STYLED_IMAGE_NAME
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "styled_images")
data class StyledImage(
    @PrimaryKey @ColumnInfo(name = "id")
    val requestId: String,
    val imagePath: String,
    var purchased: Boolean = false,
    var updatedAt: Long = Date().time
) : Parcelable {
    fun previewImageKey(): String {
        return "$imagePath$PREVIEW_IMAGE_NAME"
    }

    fun imageKey(): String {
        return "$imagePath$STYLED_IMAGE_NAME"
    }

    fun downloadKey(): String {
        return "$BUCKET_REQUESTS$requestId/$STYLED_IMAGE_NAME"
    }
}