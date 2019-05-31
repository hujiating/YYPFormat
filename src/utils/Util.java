package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import entity.Element;

import java.awt.*;
import java.util.ArrayList;

public class Util {

    /**
     * 显示dialog
     *
     * @param editor
     * @param result 内容
     * @param time   显示时间，单位秒
     */
    public static void showPopupBalloon(final Editor editor, final String result, final int time) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)), null)
                        .setFadeoutTime(time * 1000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    /**
     * 根据当前文件获取对应的class文件
     *
     * @param editor
     * @param file
     * @return
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    public static ArrayList<Element> convertToBean(Editor mEditor, String str) {
        //去掉注释
        StringBuilder newStr = new StringBuilder();
        String[] lines = str.split("\\r?\\n");
        String[] lines2 = str.split("\n");
        String[] lines3 = str.split("\\n");
        String[] line4 = str.split("\\\\n");
//        showPopupBalloon(mEditor, "lines.length: " + lines.length + " lines2.length: " + lines2.length + " line3.length: " + lines3.length + " line4.length: " + line4.length, 5);
//        System.out.println("lines.length: " + lines.length + " lines2.length: " + lines2.length + " line3.length: " + lines3.length);
        ArrayList<String> newLinesExcludeAnnotation = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("//")) {
                int index = line.indexOf("//");
                newLinesExcludeAnnotation.add(line.substring(0, index).trim());
                newStr.append(line.substring(0, index).trim());
            }
        }

        ArrayList<Element> elementList = new ArrayList<>();
        String[] lineArray = newStr.toString().split(";");
        for (String line : lineArray) {
            line = line.trim();
            String[] strSplitArray = line.split(" ");
            if (strSplitArray.length > 1) {
                Element element = new Element();
                if (line.contains("<") && line.contains("> ")) {
                    //列表
                    int index = line.indexOf(">") + 1;
                    element.setType(line.substring(0, index));
                    element.setValue(line.substring(index).trim());
                } else {
                    element.setType(strSplitArray[0]);
                    element.setValue(strSplitArray[1]);
                }
                elementList.add(element);
            }
        }
        return elementList;
    }

    public static String cppToJavaType(String cppType) {
        if (cppType.equals("int32_t") || cppType.equals("uint32_t") || cppType.equals("int64_t")) {
            return "long";
        } else if (cppType.equals("std::string")) {
            return "String";
        } else if (cppType.startsWith("std::map<")) {
            return cppType
                    .replace("std::map", "Map")
                    .replace("uint32_t", "Long")
                    .replace("int32_t", "Long")
                    .replace("int64_t", "Long")
                    .replace("std::string", "String");
        } else if (cppType.startsWith("std::vector")) {
            return cppType
                    .replace("std::vector", "List")
                    .replace("uint32_t", "Long")
                    .replace("int32_t", "Long")
                    .replace("int64_t", "Long")
                    .replace("std::string", "String");
        }
        return cppType;
    }

    public static String cppToRequestMethod(Element element) {
        String cppType = element.getType();
        if (cppType.equals("int32_t")) {
            return "pushInt32((int)";
        } else if (cppType.equals("uint32_t")) {
            return "pushInt32((int)";
        } else if (cppType.equals("int64_t")) {
            return "pushInt64(";
        } else if (cppType.equals("std::string")) {
            return "pushString16(";
        } else {
            return "push" + element.getValue() + "(";
        }
    }

    public static String cppToResponseMethod(Element element) {
        String cppType = element.getType();
        if (cppType.equals("int32_t")) {
            return element.getValue() + " = popInt32();";
        } else if (cppType.equals("uint32_t")) {
            return element.getValue() + " = popUint32();";
        } else if (cppType.equals("int64_t")) {
            return element.getValue() + " = popInt64();";
        } else if (cppType.equals("std::string")) {
            return element.getValue() + " = popString16();";
        } else {
            return element.getValue() + " = pop" + element.getValue() + "();";
        }
    }
}
