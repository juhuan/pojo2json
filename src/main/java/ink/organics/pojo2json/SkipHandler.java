package ink.organics.pojo2json;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Skip handler.
 *
 * @author juhuan.wy
 */
public class SkipHandler {
    /**
     * The constant SKIP_FIELD_NAMES.
     */
    private static final List<String> SKIP_FIELD_NAMES = new ArrayList<String>() {{
        add("serialVersionUID");
    }};
    /**
     * The constant SKIP_FIELD_TYPES.
     */
    private static final List<String> SKIP_FIELD_TYPES = new ArrayList<String>() {{
    }};

    /**
     * 根据配置跳过该属性
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean skipField(PsiField field) {
        String name = field.getName();
        PsiType type = field.getType();
        if (SKIP_FIELD_NAMES.contains(name)) {
            return true;
        }
        if (SKIP_FIELD_TYPES.contains(type.getCanonicalText())) {
            return true;
        }
        return false;
    }
}
