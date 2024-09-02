package org.kvisha.reportgenerator.model

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object Notifications {
    fun error(project: Project, message: String, title: String = "ReportGenerator Error") =
        notify(project, message, NotificationType.ERROR, title)

    fun info(project: Project, message: String, title: String = "ReportGenerator") =
        notify(project, message, NotificationType.INFORMATION, title)

    fun popupError(project: Project, message: String, title: String = "ReportGenerator Error") =
        invokeLater {Messages.showErrorDialog(project, message, title)}

    fun popupInfo(project: Project, message: String, title: String = "ReportGenerator") =
        invokeLater {Messages.showInfoMessage(project, message, title)}

    private fun notify(project: Project, message: String, type: NotificationType, title: String) {
        invokeLater {
            Notifications.Bus.notify(
                Notification("GenerateReportAction", title, message, type),
                project
            )
        }
    }
}