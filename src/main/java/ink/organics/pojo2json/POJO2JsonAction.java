package ink.organics.pojo2json;

import com.google.gson.GsonBuilder;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NonNls;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Pojo 2 json action.
 *
 * @author juhuan.wy
 */
public class POJO2JsonAction extends AnAction {
    /**
     * The constant notificationGroup.
     */
    private static final NotificationGroup notificationGroup = new NotificationGroup("pojo2json.NotificationGroup", NotificationDisplayType.BALLOON, true);
    /**
     * The constant normalTypes.
     */
    @NonNls
    private static final Map<String, Object> normalTypes = new HashMap<>();
    /**
     * The constant gsonBuilder.
     */
    private static final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    /**
     * The constant zero.
     */
    private static final BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    static {

        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        normalTypes.put("Boolean", false);
        normalTypes.put("Float", zero);
        normalTypes.put("Double", zero);
        normalTypes.put("BigDecimal", zero);
        normalTypes.put("Number", 0);
        normalTypes.put("CharSequence", "");
        normalTypes.put("Date", dateTime);
        normalTypes.put("Temporal", now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        normalTypes.put("LocalDateTime", dateTime);
        normalTypes.put("LocalDate", now.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        normalTypes.put("LocalTime", now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

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
     * Action performed.
     *
     * @param e the e
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(elementAt, PsiClass.class);
        try {
            Map<String, Object> kv = getFields(selectedClass);
            String json = gsonBuilder.create().toJson(kv);
            StringSelection selection = new StringSelection(json);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            String message = "Convert " + selectedClass.getName() + " to JSON success, copied to clipboard.";
            Notification success = notificationGroup.createNotification(message, NotificationType.INFORMATION);
            Notifications.Bus.notify(success, project);


        } catch (KnownException ex) {
            Notification warn = notificationGroup.createNotification(ex.getMessage(), NotificationType.WARNING);
            Notifications.Bus.notify(warn, project);
        } catch (Exception ex) {
            Notification error = notificationGroup.createNotification("Convert to JSON failed.", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
        }
    }

    /**
     * 获取所有字段
     *
     * @param psiClass the psi class
     * @return the fields
     */
    private static Map<String, Object> getFields(PsiClass psiClass) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (psiClass == null) {
            return map;
        }

        for (PsiField field : psiClass.getAllFields()) {

            String name = field.getName();
            PsiType type = field.getType();

            if (SKIP_FIELD_NAMES.contains(name)) {
                continue;
            }
            if (SKIP_FIELD_TYPES.contains(type.toString())) {
                continue;
            }

            map.put(name, typeResolve(type, 0));
        }

        return map;
    }

    /**
     * Type resolve object.
     *
     * @param type  the type
     * @param level the level
     * @return the object
     */
    private static Object typeResolve(PsiType type, int level) {

        level = ++level;
        if (type instanceof PsiPrimitiveType) {
            //array type

            return getDefaultValue(type);

        } else if (type instanceof PsiArrayType) {
            //array type

            List<Object> list = new ArrayList<>();
            PsiType deepType = type.getDeepComponentType();
            list.add(typeResolve(deepType, level));
            return list;

        } else {
            //reference Type

            Map<String, Object> map = new LinkedHashMap<>();

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);

            if (psiClass == null) {
                return map;
            }
            // enum
            if (psiClass.isEnum()) {

                for (PsiField field : psiClass.getFields()) {
                    if (field instanceof PsiEnumConstant) {
                        return field.getName();
                    }
                }
                return "";

            } else {

                List<String> fieldTypeNames = new ArrayList<>();

                PsiType[] types = type.getSuperTypes();

                fieldTypeNames.add(type.getPresentableText());
                fieldTypeNames.addAll(Arrays.stream(types).map(PsiType::getPresentableText).collect(Collectors.toList()));


                if (fieldTypeNames.stream().anyMatch(s -> s.startsWith("Collection") || s.startsWith("Iterable"))) {
                    // Iterable

                    List<Object> list = new ArrayList<>();
                    PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
                    list.add(typeResolve(deepType, level));
                    return list;

                } else {
                    // Object

                    List<String> retain = new ArrayList<>(fieldTypeNames);
                    retain.retainAll(normalTypes.keySet());
                    if (!retain.isEmpty()) {
                        return normalTypes.get(retain.get(0));
                    } else {

                        if (level > 500) {
                            throw new KnownException("This class reference level exceeds maximum limit or has nested references!");
                        }

                        for (PsiField field : psiClass.getAllFields()) {
                            String name = field.getName();
                            PsiType type1 = field.getType();
                            if (SKIP_FIELD_NAMES.contains(name)) {
                                continue;
                            }
                            if (SKIP_FIELD_TYPES.contains(type1.toString())) {
                                continue;
                            }
                            map.put(name, typeResolve(type1, level));
                        }

                        return map;
                    }
                }
            }
        }
    }

    /**
     * 获取该类型的默认值
     *
     * @param type the type
     * @return the default value
     */
    public static Object getDefaultValue(PsiType type) {
        if (!(type instanceof PsiPrimitiveType)) {
            return null;
        }
        switch (type.getCanonicalText()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "char":
                return '\0';
            case "float":
            case "double":
                return zero;
            default:
                return null;
        }
    }
}


