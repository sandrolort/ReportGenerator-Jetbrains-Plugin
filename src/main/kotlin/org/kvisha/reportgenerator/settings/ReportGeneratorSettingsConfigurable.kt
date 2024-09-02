package org.kvisha.reportgenerator.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import java.awt.Component
import javax.swing.*

class ReportGeneratorSettingsConfigurable(private val project: Project) : Configurable {

    private val coverageReportDirField = JTextField(20)
    private val buildScriptField = JTextField(20)
    private val testScriptField = JTextField(20)
    private val outputPathField = JTextField(20)
    private val eraseOldReportsCheckBox = JCheckBox("Erase old reports")

    override fun createComponent(): JComponent? {
        val panel = JPanel()
        val layout = GroupLayout(panel)
        panel.layout = layout
        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        // Create labels
        val coverageLabel = JLabel("Coverage Report Directory:")
        val testScriptLabel = JLabel("Test Script:")
        val buildScriptLabel = JLabel("Build Script:")
        val outputPathLabel = JLabel("Output Path:")
        val descriptionLabel = JLabel("Leave empty for default values.")
        descriptionLabel.foreground = JBColor.GRAY

        // Set alignment for description label to be centered
        descriptionLabel.alignmentX = Component.CENTER_ALIGNMENT

        // Define the horizontal and vertical layout groups
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(coverageLabel)
                        .addComponent(buildScriptLabel)
                        .addComponent(testScriptLabel)
                        .addComponent(outputPathLabel)
                        .addComponent(eraseOldReportsCheckBox) // New checkbox alignment
                )
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(coverageReportDirField)
                        .addComponent(buildScriptField)
                        .addComponent(testScriptField)
                        .addComponent(outputPathField)
                        .addComponent(descriptionLabel)
                )
        )

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(coverageLabel)
                        .addComponent(coverageReportDirField)
                )
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buildScriptLabel)
                        .addComponent(buildScriptField)
                )
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(testScriptLabel)
                        .addComponent(testScriptField)
                )
                .addGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(outputPathLabel)
                        .addComponent(outputPathField)
                )
                .addComponent(eraseOldReportsCheckBox)
                .addComponent(descriptionLabel)
        )

        return panel
    }

    override fun isModified(): Boolean {
        val settings = ReportGeneratorSettings.getInstance(project).state
        return settings.coverageReportDir != coverageReportDirField.text ||
                settings.buildScript != buildScriptField.text ||
                settings.testScript != testScriptField.text ||
                settings.outputPath != outputPathField.text ||
                settings.shouldEraseOldReports != eraseOldReportsCheckBox.isSelected // Check if modified
    }

    override fun apply() {
        val settings = ReportGeneratorSettings.getInstance(project).state
        settings.coverageReportDir = coverageReportDirField.text
        settings.buildScript = buildScriptField.text
        settings.testScript = testScriptField.text
        settings.outputPath = outputPathField.text
        settings.shouldEraseOldReports = eraseOldReportsCheckBox.isSelected // Apply checkbox value
    }

    override fun reset() {
        val settings = ReportGeneratorSettings.getInstance(project).state
        coverageReportDirField.text = settings.coverageReportDir
        buildScriptField.text = settings.buildScript
        testScriptField.text = settings.testScript
        outputPathField.text = settings.outputPath
        eraseOldReportsCheckBox.isSelected = settings.shouldEraseOldReports // Reset checkbox value
    }

    override fun getDisplayName(): String = "Report Generator Settings"
}
