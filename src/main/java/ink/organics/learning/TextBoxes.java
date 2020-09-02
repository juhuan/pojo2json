package ink.organics.learning;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class TextBoxes extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        // 输入框
        // Project project = event.getData(PlatformDataKeys.PROJECT);
        Project project = event.getProject();
        String result = Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());

        // 右下角提示
        final NotificationGroup notificationGroup = new NotificationGroup("pojo2json.NotificationGroup", NotificationDisplayType.BALLOON, true);
        Notification notification = notificationGroup.createNotification(result, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
        notification = notificationGroup.createNotification(result, NotificationType.WARNING);
        Notifications.Bus.notify(notification, project);
        notification = notificationGroup.createNotification(result, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }
}
