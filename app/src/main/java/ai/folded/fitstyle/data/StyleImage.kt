package ai.folded.fitstyle.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Entity(tableName = "style_images")
@Parcelize
data class StyleImage(
    @PrimaryKey @ColumnInfo(name = "key")
    val key: String = ""
) : Parcelable {
    fun imageName() : String {
        val values = key.split("/")
        return if (values.isNotEmpty()) values[values.size - 1] else ""
    }
}
