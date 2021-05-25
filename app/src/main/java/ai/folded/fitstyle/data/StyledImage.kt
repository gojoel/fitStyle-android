package ai.folded.fitstyle.data

import ai.folded.fitstyle.utils.PREVIEW_IMAGE_NAME
import ai.folded.fitstyle.utils.STYLED_IMAGE_NAME
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "styled_images")
data class StyledImage(
    @PrimaryKey @ColumnInfo(name = "id")
    val requestId: String,
    val imagePath: String,
    var purchased: Boolean = false,
) : Parcelable {
    fun previewImageKey(): String {
        return "$imagePath$PREVIEW_IMAGE_NAME"
    }

    fun imageKey(): String {
        return "$imagePath$STYLED_IMAGE_NAME"
    }
}