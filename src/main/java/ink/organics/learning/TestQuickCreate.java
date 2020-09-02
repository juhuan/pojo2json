package ink.organics.learning;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;

public class TestQuickCreate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final NotificationGroup notificationGroup = new NotificationGroup("my.NotificationGroup", NotificationDisplayType.BALLOON, true);
        Notification notification = notificationGroup.createNotification("TestQuickCreate", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, e.getProject());

        TestProjectService projectService = ServiceManager.getService(e.getProject(), TestProjectService.class);
        projectService.testA();
    }
}
