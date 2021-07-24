import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import data.Declaration
import data.LibInfo
import data.LibItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ui.ImportDialog
import ui.LibsDialog
import java.lang.Exception

class JetpackImportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val fileName = (e.dataContext.getData(LangDataKeys.VIRTUAL_FILE.name) as? VirtualFile)?.name ?: return
        println("current file: $fileName")
        if (!fileName.endsWith("gradle.kts")) {
            NotificationUtils.notify(e.project, "Cannot import in non-Gradle files")
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            val libs = loadLibs() ?: run {
                NotificationUtils.notify(e.project, "Libraries retrieving error")
                return@launch
            }
            println("found ${libs.size} libs")
            showLibs(libs, e.project)
        }
    }

    private suspend fun showLibs(libs: List<LibItem>, project: Project?) {
        val libsDialog = LibsDialog(libs, project)
        if (libsDialog.showAndGet()) {
            val lib = libs[libsDialog.selectedLibIndex]
            val libInfo = loadLib(lib.name, lib.link) ?: return
            println("lib info: $libInfo")
            val importDialog = ImportDialog(libInfo)
            if (importDialog.showAndGet()) {
                println("import ok")
            } else {
                println("import cancel")
            }
        } else {
            println("not OK")
        }
    }

    private suspend fun loadLib(name: String, link: String): LibInfo? {
        val targetLink = "https://developer.android.com$link"
        val document = try {
            withContext(Dispatchers.IO) {
                Jsoup.connect(targetLink).post()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        val versionsList = withContext(Dispatchers.Default) {
            document.body()
                .getElementsByTag("h3")
                .filter { it.text().startsWith("Version") }
                .map { it.text().substringAfter("Version").trim() }
        }
        val implCode = withContext(Dispatchers.Default) {
            document.body()
                .getElementsByTag("pre")
                .firstOrNull { it.text().contains("dependencies {") }
                ?.text()
                ?.split('\n')
                ?.map { it.trim() }
        }
        val versionPlaceholder = implCode
            ?.firstOrNull { it.startsWith("def") }
            ?.substringAfter("def")
            ?.substringBefore("=")
            ?.trim()
        val declarations = versionPlaceholder?.let { placeholder ->
            implCode
                .filter { it.endsWith("$placeholder\"") }
                .map {
                    val split = it.split(" ")
                    Declaration(split.first(), split.last())
                }
        } ?: emptyList()
        return LibInfo(name, targetLink, versionPlaceholder, declarations, versionsList)
    }

    private suspend fun loadLibs(): List<LibItem>? {
        val document = try {
            withContext(Dispatchers.IO) {
                Jsoup
                    .connect("https://developer.android.com/jetpack/androidx/explorer")
                    .get()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        val tableBody = document.body().getElementsByClass("list").firstOrNull() ?: return null
        return tableBody.children().map {
            val name = it.child(0).child(0).text()
            val link = it.child(0).child(0).attr("href")
            val desc = it.child(1).text()
            LibItem(name, link, desc)
        }
    }

    override fun update(e: AnActionEvent) {
        println("update called within ${e.project?.name ?: ""} project")
    }
}