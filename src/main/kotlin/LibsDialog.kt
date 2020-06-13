import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.*
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class LibsDialog(private val libs: List<LibItem>) : DialogWrapper(true) {

    init {
        init()
        title = "Available Libraries"
    }

    var selectedLibIndex = -1

    override fun createCenterPanel(): JComponent? {
        return JBScrollPane(
            JBList(libs).apply {
                installCellRenderer<LibItem> {
                    panel {
                        row {
                            label(it.name, bold = true).apply {
                                centerRelativeToParent()
                            }
                        }
                        row {
                            label(it.description, fontColor = UIUtil.FontColor.BRIGHTER, style = UIUtil.ComponentStyle.SMALL).apply {
                                centerRelativeToParent()
                            }
                        }
                    }
                }
                selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
                selectionModel.addListSelectionListener {
                    selectedLibIndex = it.firstIndex
                }
                setEmptyText("No libraries found")
            },
            JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }
}