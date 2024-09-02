package org.kvisha.reportgenerator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MyStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Ensure ToolWindow operations are done on EDT
        withContext(Dispatchers.EDT) {
            ApplicationManager.getApplication().invokeLater {
                val toolWindowManager = ToolWindowManager.getInstance(project)
                val toolWindow = toolWindowManager.getToolWindow("Report Generator")
                toolWindow?.show { }
            }
        }
    }
}
