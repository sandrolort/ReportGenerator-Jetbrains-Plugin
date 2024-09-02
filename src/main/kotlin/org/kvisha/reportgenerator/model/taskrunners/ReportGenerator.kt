package org.kvisha.reportgenerator.model.taskrunners

import BaseTaskRunner
import com.intellij.openapi.project.Project
import org.kvisha.reportgenerator.model.NotificationManager
import org.kvisha.reportgenerator.settings.ReportGeneratorSettings
import org.kvisha.reportgenerator.view.HtmlReportPlugin
import java.io.File
import javax.swing.SwingUtilities

object ReportGenerator : BaseTaskRunner() {

    override fun runTaskPrivate(projectPath: String, project: Project, onComplete: () -> Unit) {
        val coverageReportPath = findCoverageReport(projectPath, project) ?: run {
            SwingUtilities.invokeLater {
                NotificationManager.notifyError(project, "No coverage report found.")
            }
            return
        }

        val settings = ReportGeneratorSettings.getInstance(project).state

        if (settings.shouldEraseOldReports)
            clearCoverageReports(projectPath, project)

        val reportGeneratorExePath = getReportGeneratorExePath(project) ?: run {
            SwingUtilities.invokeLater {
                NotificationManager.notifyError(
                    project,
                    "ReportGenerator.exe not found. Install it as a global tool using 'dotnet tool install -g dotnet-reportgenerator-globaltool'."
                )
            }
            return
        }

        runReportGenerator(reportGeneratorExePath, coverageReportPath, project)

        SwingUtilities.invokeLater { HtmlReportPlugin.getInstance()?.reloadHtmlContent() }
    }

    fun generateReport(projectPath: String, project: Project, onComplete: () -> Unit) {
        runTask(projectPath, project, "Generating report", onComplete)
    }

    private fun findCoverageReport(projectDirectory: String, project: Project): String? {
        val settings = ReportGeneratorSettings.getInstance(project).state
        val path = ReportGeneratorSettings.convertPathToAbsolute(settings.coverageReportDir, project)
        val testResultsDir = File(path)

        if (testResultsDir.exists() && testResultsDir.isDirectory) {
            val possibleFiles = listOf("coverage.xml", "cobertura.xml", "coverage.cobertura.xml")
            var latestCoverageFile: File? = null
            var latestModifiedTime: Long = 0

            for (fileName in possibleFiles) {
                val coverageFile = File(testResultsDir, fileName)
                if (coverageFile.exists() && coverageFile.lastModified() > latestModifiedTime) {
                    latestCoverageFile = coverageFile
                    latestModifiedTime = coverageFile.lastModified()
                }
            }

            testResultsDir.walkTopDown().forEach { file ->
                if (file.name in possibleFiles && file.lastModified() > latestModifiedTime) {
                    latestCoverageFile = file
                    latestModifiedTime = file.lastModified()
                }
            }

            if (latestCoverageFile != null) {
                return latestCoverageFile!!.absolutePath
            }
        }

        return null
    }

    private fun clearCoverageReports(projectPath: String, project: Project) {
        val settings = ReportGeneratorSettings.getInstance(project).state
        val path = settings.coverageReportDir.replace("\$PROJECT_DIR", projectPath)
        val coverageReportsDir = File(path)

        if (coverageReportsDir.exists() && coverageReportsDir.isDirectory) {
            coverageReportsDir.walkTopDown().forEach { it.delete() }
        } else {
            coverageReportsDir.mkdirs()
        }
    }

    internal fun getReportGeneratorExePath(project: Project): String? {
        val homeDirectory = System.getProperty("user.home")
        val reportGeneratorDir = File(homeDirectory, ".nuget/packages/reportgenerator")
        if (reportGeneratorDir.exists() && reportGeneratorDir.isDirectory) {
            val latestVersion = reportGeneratorDir.listFiles { file -> file.isDirectory }
                ?.maxByOrNull { it.name }
            if (latestVersion != null) {
                val exePath = File(latestVersion, "tools/net8.0/ReportGenerator.exe")
                if (exePath.exists()) {
                    return exePath.absolutePath
                }
            }
        }
        return null
    }

    private fun runReportGenerator(reportGeneratorExePath: String, coverageReportPath: String, project: Project) {
        val settings = ReportGeneratorSettings.getInstance(project).state
        val outputDir = File(ReportGeneratorSettings.convertPathToAbsolute(settings.outputPath, project)).absolutePath

        val reportGeneratorCommand = "$reportGeneratorExePath -reports:$coverageReportPath -targetdir:$outputDir"
        val reportGeneratorProcess = ProcessBuilder(*reportGeneratorCommand.split(" ").toTypedArray())
            .start()
        reportGeneratorProcess.waitFor()
    }
}