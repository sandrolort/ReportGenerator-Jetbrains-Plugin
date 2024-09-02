package org.kvisha.reportgenerator.model.taskrunners

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.kvisha.reportgenerator.model.Notifications
import java.io.File
import javax.swing.SwingUtilities

abstract class BaseTaskRunner {
    protected fun parseCommand(command: String): List<String> {
        // the regex matches either unquoted words or quoted substrings.
        val regex = Regex("""[^\s"]+|"([^"]*)"""")
        return regex.findAll(command).map { it.value.trim('"') }.toList()
    }

    protected fun createProcessBuilder(commandParts: List<String>, projectPath: String): ProcessBuilder {
        return ProcessBuilder(commandParts)
            .directory(File(projectPath))
            .redirectErrorStream(true)
    }

    protected fun executeProcess(processBuilder: ProcessBuilder, project: Project, onComplete: () -> Unit) {
        try {
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            SwingUtilities.invokeLater {
                if (exitCode == 0) {
                    onComplete()
                } else {
                    Notifications.error(project, "Process failed:\n$output")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SwingUtilities.invokeLater {
                Notifications.error(project, "Error: ${e.message}")
            }
        }
    }

    protected abstract fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit)

    fun runTask(projectPath: String, project: Project, taskName: String, onComplete: () -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, taskName, true) {
            override fun run(indicator: ProgressIndicator) = try {
                runTaskPrivate(projectPath, project, onComplete)
            } catch (e: Exception) {
                e.printStackTrace()
                SwingUtilities.invokeLater {
                    Notifications.error(project, "Error: ${e.message}")
                }
            }
        })
    }

    fun manualFileSave() = SwingUtilities.invokeLater { FileDocumentManager.getInstance().saveAllDocuments() }
}