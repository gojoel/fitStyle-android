package ai.folded.fitstyle.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "style_images")
@Parcelize
data class StyleImage(
    @PrimaryKey @ColumnInfo(name = "id")
    val id: Int,
    val imageUrl: String = ""
) : Parcelable