package org.kvisha.reportgenerator.view

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefBrowser
import org.kvisha.reportgenerator.model.GenerateReportAction
import org.kvisha.reportgenerator.model.taskrunners.ReportGenerator
import org.kvisha.reportgenerator.settings.ReportGeneratorSettings
import java.awt.*
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*

class HtmlReportUiComponents(private val project: Project) {
    private var jCefBrowser: JBCefBrowser? = null

    fun createPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        jCefBrowser = JBCefBrowser("about:blank")
        panel.add(jCefBrowser!!.component, BorderLayout.CENTER)

        val buttonPanel = JPanel(BorderLayout())

        val leftPanel = JPanel()
        leftPanel.layout = FlowLayout(FlowLayout.LEFT)
        leftPanel.add(createGenerateReportButton())
        leftPanel.add(createReloadButton())

        val rightPanel = JPanel()
        rightPanel.layout = FlowLayout(FlowLayout.RIGHT)
        rightPanel.add(createDropdown())

        buttonPanel.add(leftPanel, BorderLayout.WEST)
        buttonPanel.add(rightPanel, BorderLayout.EAST)

        panel.add(buttonPanel, BorderLayout.NORTH)

        reloadHtmlContent()
        return panel
    }

    private fun createDropdown(): JComboBox<String> {
        val options = arrayOf("Star - Plugin", "Star - ReportGenerator", "Sponsor - ReportGenerator")
        val dropdown = ComboBox(options)

        dropdown.preferredSize = Dimension(150, 30)
        dropdown.background = JBColor.LIGHT_GRAY
        dropdown.foreground = JBColor.BLACK
        dropdown.font = Font("Arial", Font.BOLD, 12)
        dropdown.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (index == -1) {
                    component as JLabel
                    component.text = "Support the Devs"
                    component.horizontalAlignment = JLabel.CENTER
                    component.background = JBColor.LIGHT_GRAY
                    component.foreground = JBColor.BLACK
                    component.font = Font("Arial", Font.BOLD, 12)
                }
                return component
            }
        }

        dropdown.addActionListener { e: ActionEvent ->
            val selectedOption = dropdown.selectedItem as String
            when (selectedOption) {
                "Star - Plugin" -> BrowserUtil.browse("https://github.com/sandrolort/ReportGenerator-Jetbrains-Plugin")
                "Star - ReportGenerator" -> BrowserUtil.browse("https://github.com/danielpalme/ReportGenerator")
                "Sponsor - ReportGenerator" -> BrowserUtil.browse("https://github.com/sponsors/danielpalme")
            }
            dropdown.selectedIndex = -1
        }

        return dropdown
    }


    private fun createGenerateReportButton(): JButton {
        return JButton("Generate Report").apply {
            addActionListener {
                val action = GenerateReportAction()
                action.actionPerformed(createActionEvent())
            }
        }
    }

    private fun createReloadButton(): JButton {
        return JButton("Reload").apply {
            addActionListener {
                reloadHtmlContent()
            }
        }
    }

    fun reloadHtmlContent() {
        val htmlContent = loadHtmlContent()
        jCefBrowser?.loadHTML(htmlContent)
    }

    private fun loadHtmlContent(): String {
        val coverageReportDir = ensureCoverageReportsDirExists()
        val htmlFile = Files.walk(coverageReportDir)
            .filter { it.fileName.toString().equals("index.html", ignoreCase = true) }
            .findFirst()
            .orElse(null)

        return if (htmlFile != null && Files.exists(htmlFile)) {
            var htmlContent = Files.readString(htmlFile)

            val baseDir = htmlFile.parent
            htmlContent = updateResourcePaths(htmlContent, baseDir)

            htmlContent
        } else {
            findFallbackHtml()
        }
    }

    private fun updateResourcePaths(content: String, baseDir: Path): String {
        var modifiedContent = content.replace(Regex("""<a\s+[^>]*class="button"[^>]*>(.*?)</a>"""), "")

        modifiedContent.replace(Regex("""href="([^"]+)"""")) { match ->
            val resourcePath = match.groupValues[1]
            if (resourcePath.startsWith("http://") || resourcePath.startsWith("https://")) {
                """href="#" """
            } else {
                val absolutePath = baseDir.resolve(resourcePath).toAbsolutePath().toUri().toString()
                """href="$absolutePath""""
            }
        }.also { modifiedContent = it }

        modifiedContent.replace(Regex("""src="([^"]+)"""")) { match ->
            val resourcePath = match.groupValues[1]
            val absolutePath = baseDir.resolve(resourcePath).toAbsolutePath().toUri().toString()
            """src="$absolutePath""""
        }.also { modifiedContent = it }

        return modifiedContent
    }


    private fun findFallbackHtml(): String {
        val pluginPath = Paths.get(PathManager.getPluginsPath())
        val firstHtmlFile = Files.walk(pluginPath)
            .filter { it.toString().endsWith(".html", ignoreCase = true) }
            .findFirst()
            .orElse(null)

        val placeholder = "No Coverage Report Found. Placeholder HTML not found."

        return firstHtmlFile?.let {
            Files.readString(it)
        } ?: javaClass.getResourceAsStream("/no-report.html")
            ?.bufferedReader()
            ?.use { it.readText() }
        ?: ReportGenerator.getReportGeneratorExePath()
            ?.let { javaClass.getResource("/tools-not-installed.html")?.readText() }
        ?: placeholder
    }

    private fun ensureCoverageReportsDirExists(): Path {

        val settings = ReportGeneratorSettings.getInstance(project).state
        val outputPath = ReportGeneratorSettings.convertPathToAbsolute(settings.outputPath, project)

        return Paths.get(outputPath).apply {
            if (!Files.exists(this)) {
                Files.createDirectories(this)
            }
        }
    }

    private fun createActionEvent(): AnActionEvent {
        val dataContext = DataContext { key ->
            when (key) {
                CommonDataKeys.PROJECT.name -> project
                else -> null
            }
        }
        return AnActionEvent(
            null, dataContext, "CustomActionPlace",
            Presentation(), ActionManager.getInstance(), 0
        )
    }
}