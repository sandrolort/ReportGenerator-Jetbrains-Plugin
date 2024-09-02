package org.kvisha.reportgenerator.model

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationManager {
    fun notifyError(project: Project, message: String, title: String = "Generate Report") {
        notify(project, title, message, NotificationType.ERROR)
    }

    fun notifyInfo(project: Project, message: String, title: String = "Generate Report") {
        notify(project, title, message, NotificationType.INFORMATION)
    }

    private fun notify(project: Project, title: String, message: String, type: NotificationType) {
        Notifications.Bus.notify(
            Notification("GenerateReportAction", title, message, type),
            project
        )
    }
}