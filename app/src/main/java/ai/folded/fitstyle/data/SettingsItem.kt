package ai.folded.fitstyle.data

data class SettingsItem(
    val label: String = "",
    var value: String = "",
    var type: SettingType = SettingType.UNKNOWN
) {
    fun actionableItem(): Boolean {
        return type == SettingType.FEEDBACK
    }
}

enum class SettingType {
    USER_ID, APP_VERSION, FEEDBACK, UNKNOWN
}