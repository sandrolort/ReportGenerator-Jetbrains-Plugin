package org.kvisha.reportgenerator.model

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBList
import org.kvisha.reportgenerator.model.taskrunners.ProjectBuilder
import org.kvisha.reportgenerator.model.taskrunners.ReportGenerator
import org.kvisha.reportgenerator.model.taskrunners.TestRunner
import javax.swing.SwingUtilities

class GenerateReportAction : AnAction("Generate Report"), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val testProjects = TestProjectFinder.findTestProjects(project)

        when {
            testProjects.isEmpty() -> NotificationManager.notifyError(project, "No test projects found.")
            testProjects.size == 1 -> buildRunReport(project, testProjects.first().path)
            else -> showProjectSelectionPopup(project, testProjects)
        }
    }

    private fun showProjectSelectionPopup(project: Project, testProjects: List<TestProject>) {
        val projectList = JBList(testProjects.map { it.name })
        PopupChooserBuilder(projectList)
            .setTitle("Select a Test Project")
            .setItemChosenCallback { selectedValue ->
                val selectedProjectPath = testProjects.find { it.name == selectedValue }?.path
                if (selectedProjectPath != null) {
                    buildRunReport(project, selectedProjectPath)
                }
            }
            .createPopup()
            .showInFocusCenter()
    }

    private fun buildRunReport(project: Project, projectPath: String) = try {
        SwingUtilities.invokeLater {
            ProjectBuilder.runTask(projectPath, project, "Building project") {
                SwingUtilities.invokeLater {
                    TestRunner.runTask(projectPath, project, "Running tests") {
                        SwingUtilities.invokeLater {
                            ReportGenerator.generateReport(projectPath, project) {
                                NotificationManager.notifyInfo(project, "Report generation completed.")
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        SwingUtilities.invokeLater {
            NotificationManager.notifyError(project, "Error running tests: ${e.message}")
        }
    }
}

data class TestProject(val name: String, val path: String)