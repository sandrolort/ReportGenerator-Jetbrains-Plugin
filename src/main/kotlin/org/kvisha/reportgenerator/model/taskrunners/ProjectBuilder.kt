package org.kvisha.reportgenerator.model.taskrunners

import BaseTaskRunner
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.kvisha.reportgenerator.settings.ReportGeneratorSettings
import javax.swing.SwingUtilities

object ProjectBuilder : BaseTaskRunner() {

    override fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit) {
        manualFileSave()

        val settings = ReportGeneratorSettings.getInstance(project).state
        val buildCommand = ReportGeneratorSettings.convertPathToAbsolute(settings.buildScript, project)

        val commandParts = parseCommand(buildCommand)
        if (commandParts.isEmpty()) {
            SwingUtilities.invokeLater {
                Messages.showErrorDialog(project, "Invalid build command: $buildCommand", "Error")
            }
            return
        }

        val processBuilder = createProcessBuilder(commandParts, projectPath)
        executeProcess(processBuilder, project, onComplete)
    }
}