import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import entity.Element;
import utils.Util;

import java.util.List;

public class ViewFieldMethodCreator extends WriteCommandAction.Simple {

    private PluginDialog mDialog;
    private Editor mEditor;
    private PsiFile mFile;
    private Project mProject;
    private PsiClass mClass;
    private List<Element> mElements;
    private PsiElementFactory mFactory;

    public ViewFieldMethodCreator(PluginDialog dialog, Editor editor, PsiFile psiFile, PsiClass psiClass, String command, List<Element> elements, String selectedText) {
        super(psiClass.getProject(), command);
        mDialog = dialog;
        mEditor = editor;
        mFile = psiFile;
        mProject = psiClass.getProject();
        mClass = psiClass;
        mElements = elements;
        // 获取Factory
        mFactory = JavaPsiFacade.getElementFactory(mProject);
    }

    @Override
    protected void run() throws Throwable {
        try {
            generateFields();
            generateRequestMethod();
            generateResponseMethod();
        } catch (Exception e) {
            // 异常打印
            mDialog.dispose();
            Util.showPopupBalloon(mEditor, e.getMessage(), 10);
            return;
        }
        // 重写class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
        Util.showPopupBalloon(mEditor, "生成成功", 5);
    }

    private void generateFields() {
        for (Element element : mElements) {
            StringBuilder fromText = new StringBuilder();
//            if (!TextUtils.isEmpty(text)) {
//                fromText.append("/****" + text + "****/\n");
//            }
            fromText.append("public ");
            fromText.append(Util.cppToJavaType(element.getType()));
            fromText.append(" ");
            fromText.append(element.getValue());
            fromText.append(";");
            // 创建点击方法
            // 添加到class
            mClass.add(mFactory.createFieldFromText(fromText.toString(), mClass));

        }
    }

    private void generateRequestMethod() {
        // 拼接方法的字符串
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public void pushPacketData() {\n");
        for (Element element : mElements) {
            methodBuilder.append(Util.cppToRequestMethod(element) + element.getValue() + ");\n");
        }
        methodBuilder.append(" }\n");
        // 创建OnClick方法
        mClass.add(mFactory.createMethodFromText(methodBuilder.toString(), mClass));
    }

    private void generateResponseMethod() {
        // 拼接方法的字符串
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public void popPacketData() {\n" +
                "        try {\n");
        for (Element element : mElements) {
            methodBuilder.append(Util.cppToResponseMethod(element));
        }
        methodBuilder.append("  } catch (Exception e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n");
        // 创建OnClick方法
        mClass.add(mFactory.createMethodFromText(methodBuilder.toString(), mClass));
    }


}
