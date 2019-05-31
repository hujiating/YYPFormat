package main;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import entity.Element;
import org.apache.http.util.TextUtils;
import utils.Util;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class PluginDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane textPane1;

    private Editor mEditor;
    private PsiFile mFile;
    private Project mProject;
    private PsiClass mClass;

    public void setmEditor(Editor mEditor) {
        this.mEditor = mEditor;
    }

    public void setmFile(PsiFile mFile) {
        this.mFile = mFile;
    }

    public void setmProject(Project mProject) {
        this.mProject = mProject;
    }

    public void setmClass(PsiClass mClass) {
        this.mClass = mClass;
    }

    public PluginDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
        // add your code here
        String str = textPane1.getText();
        if (TextUtils.isEmpty(str)) {
            return;
        }
        List<Element> elements = Util.convertToBean(mEditor, str);
        setCreator(elements);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 生成
     */
    private void setCreator(List<Element> mElements) {
        new ViewFieldMethodCreator(this, mEditor, mFile, mClass,
                "Generate Injections", mElements, "")
                .execute();
    }

    public static void main(String[] args) {
/*        PluginDialog dialog = new PluginDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);*/


        PluginDialog pluginDialog = new PluginDialog();

        pluginDialog.setSize(600, 400);
        pluginDialog.setLocationRelativeTo(null);
        pluginDialog.setVisible(true);


    }

}
