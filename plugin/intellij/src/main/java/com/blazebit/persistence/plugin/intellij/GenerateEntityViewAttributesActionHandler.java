/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.plugin.intellij;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.EncapsulatableClassMember;
import com.intellij.codeInsight.generation.GenerateMembersHandlerBase;
import com.intellij.codeInsight.generation.GenerateMembersUtil;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.codeInsight.generation.PsiDocCommentOwnerMemberChooserObject;
import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.codeInsight.generation.TemplateGenerationInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInspection.ex.GlobalInspectionContextBase;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.ContextAwareActionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.generate.exception.GenerateCodeException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.blazebit.persistence.plugin.intellij.EditEntityViewDialog.toAttributeName;

public class GenerateEntityViewAttributesActionHandler implements CodeInsightActionHandler, ContextAwareActionHandler {

    private final String myChooserTitle;
    protected boolean myToCopyJavaDoc;

    public GenerateEntityViewAttributesActionHandler() {
        this.myChooserTitle = "Generate Entity View attributes";
    }

    @Nullable
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
        ClassMember[] allMembers = getAllOriginalMembers(aClass);
        return chooseMembers(allMembers, false, false, project, null);
    }

    @Nullable
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project, Editor editor) {
        return chooseOriginalMembers(aClass, project);
    }

    @Nullable
    protected ClassMember[] chooseMembers(ClassMember[] members,
                                          boolean allowEmptySelection,
                                          boolean copyJavadocCheckbox,
                                          Project project,
                                          @Nullable Editor editor) {
        MemberChooser<ClassMember> chooser = createMembersChooser(members, allowEmptySelection, copyJavadocCheckbox, project);
        if (editor != null) {
            final int offset = editor.getCaretModel().getOffset();

            ClassMember preselection = null;
            for (ClassMember member : members) {
                if (member instanceof PsiElementClassMember) {
                    final PsiDocCommentOwner owner = ((PsiElementClassMember)member).getElement();
                    if (owner != null) {
                        final TextRange textRange = owner.getTextRange();
                        if (textRange != null && textRange.contains(offset)) {
                            preselection = member;
                            break;
                        }
                    }
                }
            }
            if (preselection != null) {
                chooser.selectElements(new ClassMember[]{preselection});
            }
        }

        chooser.show();
        myToCopyJavaDoc = chooser.isCopyJavadoc();
        final List<ClassMember> list = chooser.getSelectedElements();
        return list == null ? null : list.toArray(ClassMember.EMPTY_ARRAY);
    }

    @Nullable
    protected JComponent getHeaderPanel(Project project) {
        return null;
    }

    @Nullable
    protected JComponent[] getOptionControls() {
        return null;
    }

    protected MemberChooser<ClassMember> createMembersChooser(ClassMember[] members,
                                                              boolean allowEmptySelection,
                                                              boolean copyJavadocCheckbox,
                                                              Project project) {
        MemberChooser<ClassMember> chooser = new MemberChooser<ClassMember>(members, allowEmptySelection, true, project, getHeaderPanel(project), getOptionControls()) {
            @Nullable
            @Override
            protected String getHelpId() {
                return GenerateEntityViewAttributesActionHandler.this.getHelpId();
            }
        };
        chooser.setTitle(myChooserTitle);
        chooser.setCopyJavadocVisible(copyJavadocCheckbox);
        return chooser;
    }

    protected String getHelpId() {
        return null;
    }

    protected boolean hasMembers(@NotNull PsiClass aClass) {
        return true;
    }

    @Override
    public boolean isAvailableForQuickList(@NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext) {
        PsiClass aClass = OverrideImplementUtil.getContextClass(file.getProject(), editor, file, true);
        return aClass != null && this.hasMembers(aClass);
    }

    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (EditorModificationUtil.checkModificationAllowed(editor)) {
            if (FileDocumentManager.getInstance().requestWriting(editor.getDocument(), project)) {
                PsiClass aClass = OverrideImplementUtil.getContextClass(project, editor, file, true);
                if (aClass != null && aClass.isInterface() || aClass.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT)) {

                    try {
                        ClassMember[] members = this.chooseOriginalMembers(aClass, project, editor);
                        if (members == null) {
                            return;
                        }

                        CommandProcessor.getInstance().executeCommand(project, () -> {
                            int offset = editor.getCaretModel().getOffset();

                            try {
                                this.doGenerate(project, editor, aClass, members);
                            } catch (GenerateCodeException ex) {
                                String message = ex.getMessage();
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    if (!editor.isDisposed()) {
                                        editor.getCaretModel().moveToOffset(offset);
                                        HintManager.getInstance().showErrorHint(editor, message);
                                    }

                                }, project.getDisposed());
                            }

                        }, (String)null, (Object)null);
                    } finally {
                        this.cleanup();
                    }

                }
            }
        }
    }

    protected ClassMember[] getAllOriginalMembers(PsiClass entityViewClass) {
        PsiClass entityClass = EntityViewUtils.getEntityViewEntityClass(entityViewClass);

        PsiMethod[] entityMethods = entityClass.getAllMethods();
        PsiField[] entityFields = entityClass.getAllFields();
        List<ClassMember> classMembers = new ArrayList<>(entityMethods.length + entityFields.length);
        for (PsiMethod entityMethod : entityMethods) {
            if (!"java.lang.Object".equals(entityMethod.getContainingClass().getQualifiedName())) {
                String attributeName = toAttributeName(entityMethod.getName());
                if (attributeName != null) {
                    classMembers.add(new EntityAttribute(entityViewClass, entityClass, attributeName, entityMethod.getReturnType()));
                }
            }
        }
        return classMembers.toArray(new ClassMember[classMembers.size()]);
    }

    protected List<GenerationInfo> generateMemberPrototypes(PsiClass psiClass, ClassMember[] classMembers) throws IncorrectOperationException {
        if (classMembers != null && classMembers.length != 0) {
            List<GenerationInfo> generationInfos = new ArrayList<>(classMembers.length);
            for (int i = 0; i < classMembers.length; i++) {
                generationInfos.add(((EntityAttribute) classMembers[i]).generateGetter());
            }

            return generationInfos;
        }
        return Collections.emptyList();
    }

    protected void cleanup() {
    }

    private void doGenerate(Project project, Editor editor, PsiClass aClass, final ClassMember[] members) {
        int offset = editor.getCaretModel().getOffset();
        int col = editor.getCaretModel().getLogicalPosition().column;
        int line = editor.getCaretModel().getLogicalPosition().line;
        Document document = editor.getDocument();
        int lineStartOffset = document.getLineStartOffset(line);
        CharSequence docText = document.getCharsSequence();
        String textBeforeCaret = docText.subSequence(lineStartOffset, offset).toString();
        String afterCaret = docText.subSequence(offset, document.getLineEndOffset(line)).toString();
        PsiElement lBrace = aClass.getLBrace();
        if (textBeforeCaret.trim().length() > 0 && StringUtil.isEmptyOrSpaces(afterCaret) && (lBrace == null || lBrace.getTextOffset() < offset) && !editor.getSelectionModel().hasSelection()) {
            PsiDocumentManager.getInstance(project).commitDocument(document);
            offset = editor.getCaretModel().getOffset();
            col = editor.getCaretModel().getLogicalPosition().column;
            line = editor.getCaretModel().getLogicalPosition().line;
        }

        int finalOffset = offset;
        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 0));
        List<? extends GenerationInfo> newMembers = (List) WriteAction.compute(() -> {
            return GenerateMembersUtil.insertMembersAtOffset(aClass, finalOffset, this.generateMemberPrototypes(aClass, members));
        });
        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line, col));
        if (newMembers.isEmpty()) {
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
                HintManager.getInstance().showErrorHint(editor, this.getNothingFoundMessage());
            }

        } else {
            List<PsiElement> templates = new ArrayList();
            for (GenerationInfo member : newMembers) {
                if (!(member instanceof TemplateGenerationInfo)) {
                    ContainerUtil.addIfNotNull(templates, member.getPsiMember());
                }
            }

            GlobalInspectionContextBase.cleanupElements(project, (Runnable)null, (PsiElement[])templates.toArray(PsiElement.EMPTY_ARRAY));
            if (!newMembers.isEmpty()) {
                this.notifyOnSuccess(editor, members, newMembers);
            }

        }
    }

    protected void notifyOnSuccess(Editor editor, ClassMember[] members, List<? extends GenerationInfo> generatedMembers) {
        ((GenerationInfo)generatedMembers.get(0)).positionCaret(editor, false);
    }

    protected String getNothingFoundMessage() {
        return "Nothing found to insert";
    }

    private static class EntityAttribute implements EncapsulatableClassMember {

        private final PsiClass entityViewClass;
        private final PsiClass entityClass;
        private final String attributeName;
        private final PsiType attributeType;
        private final Icon myIcon;

        public EntityAttribute(PsiClass entityViewClass, PsiClass entityClass, String attributeName, PsiType attributeType) {
            this.entityViewClass = entityViewClass;
            this.entityClass = entityClass;
            this.attributeName = attributeName;
            this.attributeType = attributeType;
            this.myIcon = AllIcons.Javaee.PersistenceAttribute;
        }

        @Nullable
        @Override
        public GenerationInfo generateGetter() throws IncorrectOperationException {
            StringBuilder definition = new StringBuilder();
            definition.append("public abstract ").append(attributeType.getCanonicalText());
            if ("boolean".equals(attributeType.getCanonicalText())) {
                definition.append(" is");
            } else {
                definition.append(" get");
            }
            definition.append(Character.toUpperCase(attributeName.charAt(0)));
            definition.append(attributeName, 1, attributeName.length());
            definition.append("();");
            PsiMethod prototype = JavaPsiFacade.getElementFactory(entityViewClass.getProject()).createMethodFromText(definition.toString(), entityViewClass);
            PsiMethod method = createMethodIfNotExists(entityViewClass, prototype);
            if (method != null) {
                return new PsiGenerationInfo(method);
            }

            return null;
        }

        @Nullable
        @Override
        public GenerationInfo generateSetter() throws IncorrectOperationException {
            StringBuilder definition = new StringBuilder();
            definition.append("public abstract void set");
            definition.append(Character.toUpperCase(attributeName.charAt(0)));
            definition.append(attributeName, 1, attributeName.length());
            definition.append('(').append(attributeType.getCanonicalText()).append(' ').append(attributeName).append(");");
            PsiMethod prototype = JavaPsiFacade.getElementFactory(entityViewClass.getProject()).createMethodFromText(definition.toString(), entityViewClass);
            PsiMethod method = createMethodIfNotExists(entityViewClass, prototype);
            if (method != null) {
                return new PsiGenerationInfo(method);
            }

            return null;
        }

        @Override
        public MemberChooserObject getParentNodeDelegate() {
            String text = PsiFormatUtil.formatClass(entityClass, 2049);
            return new PsiDocCommentOwnerMemberChooserObject(entityClass, text, entityClass.getIcon(0));
        }

        @Override
        public void renderTreeNode(SimpleColoredComponent component, JTree tree) {
            SpeedSearchUtil.appendFragmentsForSpeedSearch(tree, this.getText(), this.getTextAttributes(tree), false, component);
            component.setIcon(this.myIcon);
        }

        @Override
        public String getText() {
            return attributeName;
        }

        protected SimpleTextAttributes getTextAttributes(JTree tree) {
            return new SimpleTextAttributes(0, tree.getForeground());
        }
    }

    @Nullable
    private static PsiMethod createMethodIfNotExists(PsiClass aClass, PsiMethod template) {
        PsiMethod existing = aClass.findMethodBySignature(template, false);
        return existing != null && existing.isPhysical() ? null : template;
    }
}
