package data

data class LibInfo(
    val name: String,
    val link: String,
    val versionPlaceholder: String?,
    val declarations: List<Declaration>,
    val versions: List<String>
) {
    val actualName: String
        get() = name.replace(" *", "")
}
