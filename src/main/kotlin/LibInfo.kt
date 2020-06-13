data class LibInfo(
    val name: String,
    val link: String,
    val versionPlaceholder: String?,
    val declarations: List<Declaration>,
    val versions: List<String>
)

data class Declaration(
    //testImplementation, implementation, etc.
    val statement: String,
    val packageName: String
)