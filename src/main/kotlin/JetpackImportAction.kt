import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Tag
import java.lang.Exception
import java.util.regex.Pattern

class JetpackImportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val fileName = (e.dataContext.getData(LangDataKeys.VIRTUAL_FILE.name) as? VirtualFile)?.name ?: return
        println("current file: $fileName")
        if (!fileName.endsWith("gradle.kts")) {
            NotificationUtils.notify(e.project, "Cannot import in non-Gradle files")
            return
        }
        showLibs(e.project)
    }

    private fun showLibs(project: Project?) = loadLibs { libs ->
        if (libs == null) {
            NotificationUtils.notify(project, "Libraries retrieving error")
            return@loadLibs
        }
        println("found ${libs.size} libs")
        val libsDialog = LibsDialog(libs)
        if (libsDialog.showAndGet()) {
            val lib = libs[libsDialog.selectedLibIndex]
            loadLib(lib.name, lib.link) {
                println("lib info: ${it ?: "null"}")
            }
        } else {
            println("not OK")
        }
    }

    private fun loadLib(name: String, link: String, callback: (LibInfo?) -> Unit) {
        val targetLink = "https://developer.android.com$link"
        GlobalScope.launch(Dispatchers.IO) {
            val document = try {
                Jsoup.connect(targetLink).post()
            } catch (e: Exception) {
                runInEdt {
                    callback.invoke(null)
                }
                return@launch
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
                    .filter { it.text().contains("dependencies {") }
                    .firstOrNull()
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
            val libInfo = LibInfo(name, targetLink, versionPlaceholder, declarations, versionsList)
            runInEdt {
                callback.invoke(libInfo)
            }
        }
    }

    private fun loadLibs(callback: (List<LibItem>?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val document = try {
                Jsoup.connect("https://developer.android.com/jetpack/androidx/explorer").get()
            } catch (e: Exception) {
                runInEdt {
                    callback.invoke(null)
                }
                return@launch
            }
            val tableBody = document.body().getElementsByClass("list").firstOrNull() ?: run {
                runInEdt {
                    callback.invoke(null)
                }
                return@launch
            }
            val libs = tableBody.children().map {
                val name = it.child(0).child(0).text()
                val link = it.child(0).child(0).attr("href")
                val desc = it.child(1).text()
                LibItem(name, link, desc)
            }
            runInEdt {
                callback.invoke(libs)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        println("update called within ${e.project?.name ?: ""} project")
    }
}