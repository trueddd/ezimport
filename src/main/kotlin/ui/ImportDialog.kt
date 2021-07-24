package ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import data.LibInfo
import org.jdesktop.swingx.combobox.ListComboBoxModel
import javax.swing.JComponent

class ImportDialog(
    private val lib: LibInfo
) : DialogWrapper(true) {

    init {
        init()
        title = "Import ${lib.actualName}"
    }

    var selectedVersionIndex = -1

    private var selected: String
        get() = lib.versions[selectedVersionIndex]
        set(value) { selectedVersionIndex = lib.versions.indexOf(value) }

    override fun createCenterPanel(): JComponent? {
        val versionsModel = ListComboBoxModel(lib.versions)
        return panel {
            row {
                comboBox(
                    versionsModel,
                    { lib.versions[selectedVersionIndex] },
                    { selectedVersionIndex = lib.versions.indexOf(it) }
                )
            }
        }
    }
}