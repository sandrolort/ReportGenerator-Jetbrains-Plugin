package org.kvisha.reportgenerator.model.taskrunners

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.kvisha.reportgenerator.model.Notifications
import org.kvisha.reportgenerator.settings.ReportGeneratorSettings
import javax.swing.SwingUtilities

object TestRunner : BaseTaskRunner() {
    override fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit) {
        val settings = ReportGeneratorSettings.getInstance(project).state
        val dotnetTestCommand = ReportGeneratorSettings.convertPathToAbsolute(settings.testScript, project)

        val commandParts = parseCommand(dotnetTestCommand)
        if (commandParts.isEmpty()) {
            Notifications.popupError(project, "Invalid test command: $dotnetTestCommand", "Error")
            return
        }

        val processBuilder = createProcessBuilder(commandParts, projectPath)
        executeProcess(processBuilder, project, onComplete)
    }
}