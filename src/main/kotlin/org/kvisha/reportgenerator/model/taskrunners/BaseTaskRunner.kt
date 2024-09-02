import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.kvisha.reportgenerator.model.NotificationManager
import java.io.File
import javax.swing.SwingUtilities

abstract class BaseTaskRunner {

    protected fun parseCommand(command: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in command) {
            when {
                char == ' ' && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            result.add(current.toString())
        }

        return result
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
                    NotificationManager.notifyError(project, "Process failed:\n$output")
                }
            }
        } catch (e: Exception) {
            SwingUtilities.invokeLater {
                NotificationManager.notifyError(project, "Error: ${e.message}")
            }
        }
    }

    protected abstract fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit)

    fun runTask(projectPath: String, project: Project, taskName: String, onComplete: () -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, taskName, true) {
            override fun run(indicator: ProgressIndicator) = try {
                runTaskPrivate(projectPath, project, onComplete)
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    NotificationManager.notifyError(project, "Error: ${e.message}")
                }
            }
        })
    }

    fun manualFileSave() = SwingUtilities.invokeLater { FileDocumentManager.getInstance().saveAllDocuments() }
}