package org.kvisha.reportgenerator.model.taskrunners

import com.intellij.openapi.project.Project
import org.kvisha.reportgenerator.model.Notifications
import org.kvisha.reportgenerator.settings.ReportGeneratorSettings

object ProjectBuilder : BaseTaskRunner() {

    override fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit) {
        manualFileSave()

        val settings = ReportGeneratorSettings.getInstance(project).state
        val buildCommand = ReportGeneratorSettings.convertPathToAbsolute(settings.buildScript, project)

        val commandParts = parseCommand(buildCommand)
        if (commandParts.isEmpty()) {
            Notifications.error(project, "Invalid build command: $buildCommand", "Error")
            return
        }

        val processBuilder = createProcessBuilder(commandParts, projectPath)
        executeProcess(processBuilder, project, onComplete)
    }
}