package ink.organics.learning.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import ink.organics.learning.TestProjectService;

public class TestProjectServiceImpl implements TestProjectService {

    Project project;

    public TestProjectServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public void testA() {
        final NotificationGroup notificationGroup = new NotificationGroup("my.NotificationGroup", NotificationDisplayType.BALLOON, true);
        Notification notification = notificationGroup.createNotification("TestProjectService", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }
}
