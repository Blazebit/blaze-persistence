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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.MetaAttribute;
import com.blazebit.persistence.view.processor.MetaEntityView;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class AnnotationMetaAttribute implements MetaAttribute {

    private static final String NEW_LINE = System.lineSeparator();

    private final AnnotationMetaEntityView parent;
    private final Element element;
    private final String type;
    private final String realType;
    private final String attributeName;
    private final String defaultValue;
    private final Element setter;
    private final boolean idMember;

    public AnnotationMetaAttribute(AnnotationMetaEntityView parent, Element element, String type, String realType) {
        this.element = element;
        this.parent = parent;
        this.type = type;
        this.realType = realType;
        this.idMember = TypeUtils.containsAnnotation(element, Constants.ID_MAPPING);
        if (element.getKind() == ElementKind.FIELD) {
            attributeName = element.getSimpleName().toString();
            if (element.getModifiers().contains(Modifier.FINAL)) {
                setter = null;
            } else {
                setter = element;
            }
            defaultValue = TypeUtils.getDefaultValue(element.asType().getKind());
        } else if (element.getKind() == ElementKind.METHOD) {
            String name = element.getSimpleName().toString();
            String setterName;
            if (name.startsWith("get")) {
                attributeName = firstToLower(3, name);
                setterName = "set" + name.substring(3);
            } else if (name.startsWith("is")) {
                attributeName = firstToLower(2, name);
                setterName = "set" + name.substring(2);
            } else {
                attributeName = firstToLower(0, name);
                setterName = name;
                parent.getContext().logMessage(Diagnostic.Kind.WARNING, "Invalid method name for attribute:" + element);
            }

            Element setter = null;
            for (Element otherElement : element.getEnclosingElement().getEnclosedElements()) {
                if (setterName.equals(otherElement.getSimpleName().toString())) {
                    setter = otherElement;
                    break;
                }
            }

            List<TypeMirror> superClasses = new ArrayList<>();
            superClasses.add(parent.getTypeElement().asType());
            for (int i = 0; i < superClasses.size(); i++) {
                TypeMirror superClass = superClasses.get(i);
                final Element superClassElement = ((DeclaredType) superClass).asElement();
                for (Element enclosedElement : superClassElement.getEnclosedElements()) {
                    if (setterName.equals(enclosedElement.getSimpleName().toString())) {
                        setter = enclosedElement;
                        break;
                    }
                }

                superClass = ((TypeElement) superClassElement).getSuperclass();
                if (superClass.getKind() == TypeKind.DECLARED) {
                    superClasses.add(superClass);
                }
                superClasses.addAll(((TypeElement) superClassElement).getInterfaces());
            }
            this.setter = setter;
            defaultValue = TypeUtils.getDefaultValue(((ExecutableElement) element).getReturnType().getKind());
        } else {
            attributeName = null;
            setter = null;
            defaultValue = "null";
            parent.getContext().logMessage(Diagnostic.Kind.WARNING, "Invalid unknown attribute element kind " + element.getKind() + " for attribute: " + element);
        }
    }

    private static String firstToLower(int skip, CharSequence s) {
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(Character.toLowerCase(s.charAt(skip)));
        sb.append(s, skip + 1, s.length());
        return sb.toString();
    }

    public boolean isIdMember() {
        return idMember;
    }

    @Override
    public void appendMetamodelAttributeDeclarationString(StringBuilder sb) {
        sb.append("    public static volatile ")
                .append(parent.metamodelImportType(getMetaType()))
                .append("<")
                .append(parent.importType(parent.getQualifiedName()))
                .append(", ")
                .append(parent.importType(getType()))
                .append("> ")
                .append(getPropertyName())
                .append(";");
    }

    @Override
    public void appendMetamodelAttributeNameDeclarationString(StringBuilder sb) {
        String propertyName = getPropertyName();
        sb.append("    public static final ")
                .append(parent.importType(String.class.getName()))
                .append(" ");

        for (int i = 0; i < propertyName.length(); i++) {
            final char c = propertyName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        sb.append(" = ")
                .append("\"")
                .append(propertyName)
                .append("\"")
                .append(";");
    }

    @Override
    public void appendImplementationAttributeDeclarationString(StringBuilder sb) {
        sb.append("    private ");

        if (setter == null) {
            sb.append("final ");
        }

        sb.append(getImplementationTypeString());
        sb.append(' ').append(getPropertyName()).append(";");
    }

    @Override
    public void appendImplementationAttributeGetterAndSetterString(StringBuilder sb) {
        sb.append("    @Override")
                .append(NEW_LINE)
                .append("    public ");

        sb.append(getImplementationTypeString());

        sb.append(' ')
                .append(element.getSimpleName().toString())
                .append("() { return ")
                .append(getPropertyName())
                .append("; }");

        if (setter != null) {
            sb.append(NEW_LINE);
            sb.append("    @Override");
            sb.append("    public void ")
                    .append(setter.getSimpleName().toString())
                    .append('(');

            sb.append(getImplementationTypeString());

            sb.append(' ')
                    .append(getPropertyName())
                    .append(") { this.")
                    .append(getPropertyName())
                    .append(" = ")
                    .append(getPropertyName())
                    .append("; }");
        }
    }

    @Override
    public void appendImplementationAttributeConstructorParameterString(StringBuilder sb) {
        sb.append("        ").append(getImplementationTypeString()).append(" ").append(getPropertyName());
    }

    @Override
    public void appendImplementationAttributeConstructorAssignmentString(StringBuilder sb) {
        sb.append("        this.").append(getPropertyName()).append(" = ").append(getPropertyName()).append(";");
    }

    @Override
    public void appendImplementationAttributeConstructorAssignmentDefaultString(StringBuilder sb) {
        sb.append("        this.").append(getPropertyName()).append(" = ").append(getDefaultValue()).append(";");
    }

    protected String getImplementationTypeString() {
        return parent.implementationImportType(getRealType());
    }

    protected String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getPropertyName() {
        return attributeName;
    }

    public MetaEntityView getHostingEntity() {
        return parent;
    }

    @Override
    public abstract String getMetaType();

    @Override
    public String getType() {
        return type;
    }

    public String getRealType() {
        return realType;
    }
}
