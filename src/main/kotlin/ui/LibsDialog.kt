package ui

import androidx.compose.desktop.ComposePanel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import data.LibItem
import java.awt.Dimension
import javax.swing.JComponent

class LibsDialog(
    private val libs: List<LibItem>,
    project: Project?,
) : DialogWrapper(project) {

    init {
        init()
        title = "Available Libraries"
    }

    var selectedLibIndex = -1

    override fun createCenterPanel(): JComponent? {
        return ComposePanel().apply {
            preferredSize = Dimension(800, 600)
            setContent {
                MaterialTheme {
                    val stateVertical = rememberScrollState(0)
                    var selected by remember { mutableStateOf(-1) }
                    Box(
                        modifier = Modifier
                            .background(color = Color.LightGray)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(stateVertical)) {
                            libs.forEachIndexed { index, libItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(selected = false) {
                                            selectedLibIndex = index
                                            selected = index
                                        }
                                        .background(color = if (index == selected) Color.Gray else Color.Transparent)
                                ) {
                                    Text(
                                        text = libItem.name,
                                        fontStyle = FontStyle.Normal,
                                    )
                                }
                            }
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(stateVertical),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .padding(end = 12.dp),
                        )
                    }
                }
            }
        }
    }
}