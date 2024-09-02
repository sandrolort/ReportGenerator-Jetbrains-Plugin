package org.kvisha.reportgenerator.settings

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File

@State(
    name = "ReportGeneratorSettings",
    storages = [Storage("ReportGeneratorSettings.xml")]
)
@Service(Service.Level.PROJECT)
class ReportGeneratorSettings : PersistentStateComponent<ReportGeneratorSettings.State> {

    private var state = State()

    data class State(
        var coverageReportDir: String = "\$PLUGIN_DIR${File.separator}TestResults",
        var testScript: String = "dotnet test --collect:\"XPlat Code Coverage\" --results-directory \$PLUGIN_DIR${File.separator}TestResults",
        var buildScript: String = "dotnet build",
        var outputPath: String = "\$PLUGIN_DIR${File.separator}temp",
        var shouldEraseOldReports: Boolean = true
    )

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {

        fun getInstance(project: Project): ReportGeneratorSettings =
            project.getService(ReportGeneratorSettings::class.java)
        fun convertPathToAbsolute(path:String, project: Project): String = path
            .replace("\$PROJECT_DIR", project.basePath!!)
            .replace("\$HOME", System.getProperty("user.home"))
            .replace("\$PLUGIN_DIR", (PathManager.getPluginsPath()+"/coverageReports"))
            .replace("\$USER_HOME", System.getProperty("user.home"))
    }
}
