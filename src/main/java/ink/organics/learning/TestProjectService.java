package ink.organics.learning;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface TestProjectService {
    static TestProjectService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, TestProjectService.class);
    }

    void testA();
}
