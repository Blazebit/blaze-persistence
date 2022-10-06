/*
 * Copyright 2014 - 2022 Blazebit.
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

import javax.tools.FileObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class ForeignPackageAdapterClassWriter extends ClassWriter {

    private static final String NEW_LINE = System.lineSeparator();
    private final ForeignPackageType typeElement;
    private final String baseType;
    private final String metaModelPackage;

    private ForeignPackageAdapterClassWriter(FileObject fileObject, MetaEntityView entity, Context context, Collection<Runnable> mainThreadQueue, LongAdder elapsedTime, ForeignPackageType typeElement, String baseType, String metaModelPackage) {
        super(fileObject, entity, null, context, mainThreadQueue, elapsedTime);
        this.typeElement = typeElement;
        this.baseType = baseType;
        this.metaModelPackage = metaModelPackage;
    }

    public static void writeFiles(MetaEntityView entity, Context context, ExecutorService executorService, Collection<Runnable> mainThreadQueue, LongAdder implementationTime) {
        ArrayList<Map.Entry<String, ForeignPackageType>> entries = new ArrayList<>(entity.getForeignPackageSuperTypes().entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, ForeignPackageType> entry = entries.get(i);
            String name = entry.getValue().getName() + "_" + entity.getQualifiedName().replace('.', '_');
            if (!context.markGenerated(name)) {
                continue;
            }
            String baseType = entries.size() < i + 1 ? entries.get(i + 1).getValue().getName() : entity.getQualifiedName();
            writeFile(entry.getKey(), baseType, name, entry.getValue(), entity, context, executorService, mainThreadQueue, implementationTime);
        }
    }

    private static void writeFile(String metaModelPackage, String baseType, String name, ForeignPackageType typeElement, MetaEntityView entity, Context context, ExecutorService executorService, Collection<Runnable> mainThreadQueue, LongAdder implementationTime) {
        FileObject fileObject = ClassWriter.createFile(metaModelPackage, name.substring(metaModelPackage.length() + 1), context, entity.getOriginatingElements());
        if (fileObject == null) {
            return;
        }
        executorService.submit(new ForeignPackageAdapterClassWriter(fileObject, entity, context, mainThreadQueue, implementationTime, typeElement, baseType, metaModelPackage));
    }

    @Override
    protected String getPackageName() {
        return metaModelPackage;
    }

    @Override
    protected void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        generateBody(sb, typeElement, baseType, entity, context);
    }

    private static void generateBody(StringBuilder sb, ForeignPackageType typeElement, String baseType, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriter.writeGeneratedAnnotation(sb, null, context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriter.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        sb.append("public abstract class ")
                .append(typeElement.getSimpleName())
                .append('_')
                .append(entity.getQualifiedName().replace('.', '_'));

        List<JavaTypeVariable> typeArguments = entity.getTypeVariables();
        List<JavaTypeVariable> typeParameters = typeElement.getTypeVariables();
        if (typeArguments.isEmpty()) {
            if (!typeParameters.isEmpty()) {
                sb.append('<');
                typeParameters.get(0).append(ImportContext.NOOP, sb);
                for (int i = 1; i < typeParameters.size(); i++) {
                    sb.append(", ");
                    typeParameters.get(i).append(ImportContext.NOOP, sb);
                }
                sb.append('>');
            }
        } else {
            sb.append('<');
            if (!typeParameters.isEmpty()) {
                typeParameters.get(0).append(ImportContext.NOOP, sb);
                for (int i = 1; i < typeParameters.size(); i++) {
                    sb.append(", ");
                    typeParameters.get(i).append(ImportContext.NOOP, sb);
                }
                sb.append(", ");
            }
            typeArguments.get(0).append(ImportContext.NOOP, sb);
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                typeArguments.get(i).append(ImportContext.NOOP, sb);
            }
            sb.append('>');
        }

        sb.append(" extends ");

        sb.append(baseType);

        if (!typeArguments.isEmpty()) {
            sb.append('<');
            sb.append(typeArguments.get(0).getName());
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i).getName());
            }
            sb.append('>');
        }

        sb.append(" {");
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);

        printConstructors(sb, typeElement, entity, context);

        sb.append(NEW_LINE);

        for (ForeignPackageMethod element : typeElement.getMethods()) {
            sb.append("    public abstract ");
            sb.append(element.getReturnType())
                    .append(" ")
                    .append(element.getName())
                    .append("(");
            List<ForeignPackageMethodParameter> parameters = element.getParameters();
            if (!parameters.isEmpty()) {
                sb.append(parameters.get(0).getType())
                    .append(" arg0");
                for (int i = 1; i < parameters.size(); i++) {
                    sb.append(", ");
                    sb.append(parameters.get(i).getType())
                        .append(" arg")
                        .append(i);
                }
            }

            sb.append(");");
            sb.append(NEW_LINE);
            if (element.isPackagePrivate() && element.isGeneric()) {
                sb.append("    public abstract ");
                sb.append(element.getRealReturnType())
                        .append(" ")
                        .append(element.getName())
                        .append("(");
                if (!parameters.isEmpty()) {
                    sb.append(parameters.get(0).getRealType())
                            .append(" arg0");
                    for (int i = 1; i < parameters.size(); i++) {
                        sb.append(", ");
                        sb.append(parameters.get(i).getRealType())
                                .append(" arg")
                                .append(i);
                    }
                }

                sb.append(");");
                sb.append(NEW_LINE);
            }
        }

        sb.append(NEW_LINE);
        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void printParameter(StringBuilder sb, MetaEntityView entity, ForeignPackageMethodParameter variableElement, int index, Context context) {
        sb.append(variableElement.getRealType())
                .append(" arg")
                .append(index);
    }

    private static void printConstructors(StringBuilder sb, ForeignPackageType typeElement, MetaEntityView entity, Context context) {
        String simpleName = typeElement.getSimpleName() + "_" + entity.getQualifiedName().replace('.', '_');
        for (MetaConstructor constructor : entity.getConstructors()) {
            sb.append("    public ").append(simpleName).append("(");

            boolean first = true;
            for (MetaAttribute parameter : constructor.getParameters()) {
                if (first) {
                    first = false;
                    sb.append(NEW_LINE);
                    sb.append("        ").append(parameter.getModelType()).append(" ").append(parameter.getPropertyName());
                } else {
                    sb.append(",");
                    sb.append(NEW_LINE);
                    sb.append("        ").append(parameter.getModelType()).append(" ").append(parameter.getPropertyName());
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
            for (MetaAttribute parameter : constructor.getParameters()) {
                if (first) {
                    first = false;
                    sb.append(NEW_LINE);
                    sb.append("            ").append(parameter.getPropertyName());
                } else {
                    sb.append(",");
                    sb.append(NEW_LINE);
                    sb.append("            ").append(parameter.getPropertyName());
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
