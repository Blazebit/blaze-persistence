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

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.JavaCodeFragmentFactory;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JavaReferenceEditorUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.TextFieldCompletionProvider;
import com.intellij.util.textCompletion.DefaultTextCompletionValueDescriptor;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.intellij.util.textCompletion.ValuesCompletionProvider;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EditEntityViewDialog extends DialogWrapper {

    private final JPanel dialogPanel = new JPanel(new BorderLayout());

    public EditEntityViewDialog(@NotNull Project project, PsiClass element) {
        super(project, true);
        init();
        setTitle("Edit Entity View");

        PsiAnnotation entityViewAnnotation = element.getAnnotation("com.blazebit.persistence.view.EntityView");
        PsiClass entityClass = ((PsiClassType) ExpressionUtils.computeConstantExpression((PsiExpression) entityViewAnnotation.findAttributeValue("value"), true)).resolve();
        PsiMethod[] methods = element.getMethods();
        List<MappingEntry> mappingEntries = new ArrayList<>(methods.length);
        List<String> methodNames = new ArrayList<>(methods.length);

        for (PsiMethod method : methods) {
            methodNames.add(method.getName());
            MappingEntry mappingEntry = MappingEntry.of(method);
            if (mappingEntry != null) {
                mappingEntries.add(mappingEntry);
            }
        }

        ValuesCompletionProvider<String> methodNameCompletionProvider = new ValuesCompletionProvider<>(new DefaultTextCompletionValueDescriptor.StringValueDescriptor(), methodNames);

        PsiMethod[] entityMethods = entityClass.getAllMethods();
        PsiField[] entityFields = entityClass.getAllFields();
        List<String> mappings = new ArrayList<>(entityMethods.length + entityFields.length);
        for (PsiMethod entityMethod : entityMethods) {
            if (!"java.lang.Object".equals(entityMethod.getContainingClass().getQualifiedName())) {
                String attributeName = toAttributeName(entityMethod.getName());
                if (attributeName != null) {
                    mappings.add(attributeName);
                }
            }
        }

        ValuesCompletionProvider<String> mappingCompletionProvider = new ValuesCompletionProvider<>(new DefaultTextCompletionValueDescriptor.StringValueDescriptor(), mappings);

        // ConfigPanel
        {
            JPanel configPanel = new JPanel(new GridLayoutManager(3, 2));
            JLabel label = new JLabel("Entity class");
            label.setPreferredSize(new Dimension(100, 100));

            // TODO: only entity classes?
            EditorTextField entityClassEditorTextField = new EditorTextField(JavaReferenceEditorUtil.createDocument(entityClass.getQualifiedName(), project, true), project, StdFileTypes.CLASS);
            entityClassEditorTextField.setPreferredWidth(400);

            GridConstraints gridConstraints = new GridConstraints();
            gridConstraints.setRow(0);
            gridConstraints.setColumn(0);
            configPanel.add(label, gridConstraints);

            gridConstraints = new GridConstraints();
            gridConstraints.setRow(0);
            gridConstraints.setColumn(1);
            configPanel.add(entityClassEditorTextField, gridConstraints);

            configPanel.setPreferredSize(new Dimension(600, 100));
            dialogPanel.add(configPanel, BorderLayout.NORTH);
        }

        // Mapping Panel
        {
            int rowCount = mappingEntries.size() + 1;
            int columnCount = 3;
            JPanel mappingPanel = new JPanel(new GridLayoutManager(rowCount, columnCount));

            {
                JLabel attributeLabel = new JLabel("Attribute");
                attributeLabel.setPreferredSize(new Dimension(100, 100));

                JLabel methodNameLabel = new JLabel("Method");
                methodNameLabel.setPreferredSize(new Dimension(100, 100));

                JLabel mappingLabel = new JLabel("Mapping");
                mappingLabel.setPreferredSize(new Dimension(100, 100));

                GridConstraints gridConstraints = new GridConstraints();
                gridConstraints.setRow(0);
                gridConstraints.setColumn(0);
                mappingPanel.add(attributeLabel, gridConstraints);

                gridConstraints = new GridConstraints();
                gridConstraints.setRow(0);
                gridConstraints.setColumn(1);
                mappingPanel.add(methodNameLabel, gridConstraints);

                gridConstraints = new GridConstraints();
                gridConstraints.setRow(0);
                gridConstraints.setColumn(2);
                mappingPanel.add(mappingLabel, gridConstraints);
            }

            int row = 1;
            for (MappingEntry mappingEntry : mappingEntries) {
                EditorTextField attributeNameEditorTextField = new EditorTextField(project, StdFileTypes.PLAIN_TEXT);
                attributeNameEditorTextField.setPreferredWidth(200);
                attributeNameEditorTextField.setText(mappingEntry.attributeName);

                EditorTextField methodEditorTextField = new TextFieldWithCompletion(project, methodNameCompletionProvider, mappingEntry.methodName, true, true, true);
                methodEditorTextField.setPreferredWidth(200);

                EditorTextField mappingEditorTextField = new TextFieldWithCompletion(project, mappingCompletionProvider, mappingEntry.mapping, true, true, true);
                mappingEditorTextField.setPreferredWidth(200);

                GridConstraints gridConstraints = new GridConstraints();
                gridConstraints.setRow(row);
                gridConstraints.setColumn(0);
                mappingPanel.add(attributeNameEditorTextField, gridConstraints);

                gridConstraints = new GridConstraints();
                gridConstraints.setRow(row);
                gridConstraints.setColumn(1);
                mappingPanel.add(methodEditorTextField, gridConstraints);

                gridConstraints = new GridConstraints();
                gridConstraints.setRow(row);
                gridConstraints.setColumn(2);
                mappingPanel.add(mappingEditorTextField, gridConstraints);
                row++;
            }


            mappingPanel.setPreferredSize(new Dimension(600, 400));
            dialogPanel.add(mappingPanel, BorderLayout.CENTER);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return dialogPanel;
    }

    public static String toAttributeName(String methodName) {
        StringBuilder sb = new StringBuilder();
        if (methodName.startsWith("is")) {
            sb.append(methodName, 2, methodName.length());
        } else if (methodName.startsWith("get")) {
            sb.append(methodName, 3, methodName.length());
        } else {
            return null;
        }
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    private static class MappingEntry {
        private String attributeName;
        private String methodName;
        private PsiMethod method;
        private String mapping;

        public MappingEntry(String attributeName, String methodName, PsiMethod method, String mapping) {
            this.attributeName = attributeName;
            this.methodName = methodName;
            this.method = method;
            this.mapping = mapping;
        }

        public static MappingEntry of(PsiMethod method) {
            if (!method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT)) {
                return null;
            }
            String attributeName = toAttributeName(method.getName());
            if (attributeName == null) {
                return null;
            }

            String mapping = attributeName;
            PsiAnnotation mappingAnnotation = method.getAnnotation("com.blazebit.persistence.view.Mapping");
            if (mappingAnnotation != null) {
                PsiAnnotationMemberValue value = mappingAnnotation.findAttributeValue("value");
                mapping = (String) ExpressionUtils.computeConstantExpression((PsiExpression) value, true);
            }

            return new MappingEntry(attributeName, method.getName(), method, mapping);
        }

    }
}
