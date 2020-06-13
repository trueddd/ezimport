import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationUtils {

    private val NOTIFICATION_GROUP = NotificationGroup("ezimport", NotificationDisplayType.BALLOON, true)

    fun notify(project: Project?, content: String): Notification? {
        val notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.WARNING)
        notification.notify(project)
        return notification
    }
}