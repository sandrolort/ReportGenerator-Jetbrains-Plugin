package org.kvisha.reportgenerator.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class HtmlReportPlugin : ToolWindowFactory {
    companion object {
        private var instance: HtmlReportPlugin? = null
        fun getInstance(): HtmlReportPlugin? = instance
    }

    private lateinit var uiComponents: HtmlReportUiComponents

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        uiComponents = HtmlReportUiComponents(project)
        val contentManager = toolWindow.contentManager
        val content = ContentFactory.getInstance().createContent(uiComponents.createPanel(), "", false)
        contentManager.addContent(content)
        instance = this
    }

    fun reloadHtmlContent() {
        uiComponents.reloadHtmlContent()
    }
}
