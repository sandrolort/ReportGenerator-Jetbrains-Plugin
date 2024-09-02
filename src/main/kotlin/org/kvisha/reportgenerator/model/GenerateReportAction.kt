package org.kvisha.reportgenerator.model

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBList
import org.kvisha.reportgenerator.model.taskrunners.ProjectBuilder
import org.kvisha.reportgenerator.model.taskrunners.ReportGenerator
import org.kvisha.reportgenerator.model.taskrunners.TestRunner

class GenerateReportAction : AnAction("Generate Report"), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val testProjects = TestProjectFinder.findTestProjects(project)

        when {
            testProjects.isEmpty() -> Notifications.error(project, "No test projects found.")
            testProjects.size == 1 -> buildRunReport(project, testProjects.first().path)
            else -> showProjectSelectionPopup(project, testProjects)
        }
    }

    private fun showProjectSelectionPopup(project: Project, testProjects: List<TestProject>) {
        val projectList = JBList(testProjects.map { it.name })
        PopupChooserBuilder(projectList)
            .setTitle("Select a Test Project")
            .setItemChosenCallback { selectedValue ->
                testProjects.find { it.name == selectedValue }?.path?.let { selectedProjectPath ->
                    buildRunReport(
                        project,
                        selectedProjectPath
                    )
                }
            }
            .createPopup()
            .showInFocusCenter()
    }

    private fun buildRunReport(project: Project, projectPath: String) =
        try {
            runProjectBuilder(project, projectPath)
        } catch (e: Exception) {
            Notifications.popupError(project, e.message ?: "Error running tests.")
        }

    private fun runProjectBuilder(project: Project, projectPath: String) {
        invokeLater {
            ProjectBuilder.runTask(projectPath, project, "Building project") {
                runTestRunner(project, projectPath)
            }
        }
    }

    private fun runTestRunner(project: Project, projectPath: String) {
        invokeLater {
            TestRunner.runTask(projectPath, project, "Running tests") {
                runReportGenerator(project, projectPath)
            }
        }
    }

    private fun runReportGenerator(project: Project, projectPath: String) {
        invokeLater {
            ReportGenerator.runTask(projectPath, project, "Generating report") {
                Notifications.info(project, "Report generation completed.")
            }
        }
    }
}

data class TestProject(val name: String, val path: String)