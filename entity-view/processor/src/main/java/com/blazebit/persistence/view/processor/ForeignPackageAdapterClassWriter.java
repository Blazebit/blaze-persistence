/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class ForeignPackageAdapterClassWriter {

    private static final String NEW_LINE = System.lineSeparator();

    private ForeignPackageAdapterClassWriter() {
    }

    public static void writeFiles(StringBuilder sb, MetaEntityView entity, Context context) {
        ArrayList<Map.Entry<String, TypeElement>> entries = new ArrayList<>(entity.getForeignPackageSuperTypes().entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, TypeElement> entry = entries.get(i);
            String name = entry.getValue().getQualifiedName().toString() + "_" + entity.getQualifiedName().replace('.', '_');
            if (context.isAlreadyGenerated(name)) {
                continue;
            }
            String baseType = entries.size() < i + 1 ? entries.get(i + 1).getValue().getQualifiedName().toString() : entity.getQualifiedName();
            sb.setLength(0);
            writeFile(sb, entry.getKey(), baseType, name, entry.getValue(), entity, context);
            context.markGenerated(name);
        }
    }

    private static void writeFile(StringBuilder sb, String metaModelPackage, String baseType, String name, TypeElement typeElement, MetaEntityView entity, Context context) {
        generateBody(sb, typeElement, baseType, entity, context);
        ClassWriterUtils.writeFile(sb, metaModelPackage, name.substring(metaModelPackage.length() + 1), null, context);
    }

    private static void generateBody(StringBuilder sb, TypeElement typeElement, String baseType, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, null, context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        sb.append("public abstract class ")
                .append(typeElement.getSimpleName().toString())
                .append('_')
                .append(entity.getQualifiedName().replace('.', '_'));

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        List<TypeVariable> typeParameters = (List<TypeVariable>) ((DeclaredType) typeElement.asType()).getTypeArguments();
        if (typeArguments.isEmpty()) {
            if (!typeParameters.isEmpty()) {
                sb.append('<');
                printTypeVariable(sb, typeParameters.get(0));
                for (int i = 1; i < typeParameters.size(); i++) {
                    sb.append(", ");
                    printTypeVariable(sb, typeParameters.get(i));
                }
                sb.append('>');
            }
        } else {
            sb.append('<');
            if (!typeParameters.isEmpty()) {
                printTypeVariable(sb, typeParameters.get(0));
                for (int i = 1; i < typeParameters.size(); i++) {
                    sb.append(", ");
                    printTypeVariable(sb, typeParameters.get(i));
                }
                sb.append(", ");
            }
            printTypeVariable(sb, typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                printTypeVariable(sb, typeArguments.get(i));
            }
            sb.append('>');
        }

        sb.append(" extends ");

        sb.append(baseType);

        if (!typeArguments.isEmpty()) {
            sb.append('<');
            sb.append(typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i));
            }
            sb.append('>');
        }

        sb.append(" {");
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);

        printConstructors(sb, typeElement, entity, context);

        sb.append(NEW_LINE);

        for (Element element : typeElement.getEnclosedElements()) {
            if (element instanceof ExecutableElement && element.getModifiers().contains(Modifier.ABSTRACT) && !element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                ExecutableElement executableElement = (ExecutableElement) element;
                boolean packagePrivate = !element.getModifiers().contains(Modifier.PROTECTED);
                boolean isGeneric = false;
                sb.append("    public abstract ");
                sb.append(TypeUtils.toTypeString((DeclaredType) entity.getTypeElement().asType(), executableElement.getReturnType(), context))
                        .append(" ")
                        .append(executableElement.getSimpleName())
                        .append("(");
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (!parameters.isEmpty()) {
                    isGeneric = parameters.get(0).asType().getKind() == TypeKind.TYPEVAR;
                    printParameter(sb, entity, parameters.get(0), context);
                    for (int i = 1; i < parameters.size(); i++) {
                        isGeneric = isGeneric || parameters.get(i).asType().getKind() == TypeKind.TYPEVAR;
                        sb.append(", ");
                        printParameter(sb, entity, parameters.get(i), context);
                    }
                }

                sb.append(");");
                sb.append(NEW_LINE);
                if (packagePrivate && isGeneric) {
                    sb.append("    public abstract ");
                    sb.append(executableElement.getReturnType())
                            .append(" ")
                            .append(executableElement.getSimpleName())
                            .append("(");
                    if (!parameters.isEmpty()) {
                        sb.append(parameters.get(0).asType())
                                .append(" ")
                                .append(parameters.get(0));
                        for (int i = 1; i < parameters.size(); i++) {
                            sb.append(", ");
                            sb.append(parameters.get(i).asType())
                                    .append(" ")
                                    .append(parameters.get(i));
                        }
                    }

                    sb.append(");");
                    sb.append(NEW_LINE);
                }
            }
        }

        sb.append(NEW_LINE);
        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void printParameter(StringBuilder sb, MetaEntityView entity, VariableElement variableElement, Context context) {
        sb.append(TypeUtils.toTypeString((DeclaredType) entity.getTypeElement().asType(), variableElement.asType(), context))
                .append(" ")
                .append(variableElement);
    }

    private static void printTypeVariable(StringBuilder sb, TypeVariable t) {
        sb.append(t);
        if (t.getLowerBound().getKind() == TypeKind.NULL) {
            sb.append(" extends ").append(t.getUpperBound().toString());
        } else {
            sb.append(" super ").append(t.getLowerBound().toString());
        }
    }

    private static void printConstructors(StringBuilder sb, TypeElement typeElement, MetaEntityView entity, Context context) {
        String simpleName = typeElement.getSimpleName().toString() + "_" + entity.getQualifiedName().replace('.', '_');
        for (Element enclosedElement : entity.getTypeElement().getEnclosedElements()) {
            if (!enclosedElement.getModifiers().contains(Modifier.PRIVATE) && enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) enclosedElement;
                sb.append("    public ").append(simpleName).append("(");

                boolean first = true;
                for (VariableElement parameter : constructor.getParameters()) {
                    if (first) {
                        first = false;
                        sb.append(NEW_LINE);
                        sb.append("        ").append(parameter.asType()).append(" ").append(parameter.getSimpleName());
                    } else {
                        sb.append(",");
                        sb.append(NEW_LINE);
                        sb.append("        ").append(parameter.asType()).append(" ").append(parameter.getSimpleName());
                        sb.append(NEW_LINE);
                    }
                }
                if (first) {
                    sb.append(") {");
                    sb.append(NEW_LINE);
                } else {
                    sb.append("    ) {");
                    sb.append(NEW_LINE);
                }
                sb.append("        super(");

                first = true;
                for (VariableElement parameter : constructor.getParameters()) {
                    if (first) {
                        first = false;
                        sb.append(NEW_LINE);
                        sb.append("            ").append(parameter.getSimpleName());
                    } else {
                        sb.append(",");
                        sb.append(NEW_LINE);
                        sb.append("            ").append(parameter.getSimpleName());
                    }
                }
                if (first) {
                    sb.append(");");
                    sb.append(NEW_LINE);
                } else {
                    sb.append(NEW_LINE);
                    sb.append("        );");
                    sb.append(NEW_LINE);
                }
                sb.append("    }");
                sb.append(NEW_LINE);
            }
        }
    }
}
