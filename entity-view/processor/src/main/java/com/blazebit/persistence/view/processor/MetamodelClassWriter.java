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

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaEntityView;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class MetamodelClassWriter {

    public static final String META_MODEL_CLASS_NAME_SUFFIX = "_";
    private static final String NEW_LINE = System.lineSeparator();

    private MetamodelClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + META_MODEL_CLASS_NAME_SUFFIX, entity.getMetamodelImportContext(), context, entity.getOriginatingElements());
    }

    private static void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, entity.getMetamodelImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        sb.append("@").append(entity.metamodelImportType(Constants.STATIC_METAMODEL)).append("(").append(entity.metamodelImportType(entity.getQualifiedName())).append(".class)");
        sb.append(NEW_LINE);
        sb.append("public abstract class ").append(entity.getSimpleName()).append(META_MODEL_CLASS_NAME_SUFFIX).append(" {");

        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                metaMember.appendMetamodelAttributeDeclarationString(sb, entity.getMetamodelImportContext());
                sb.append(NEW_LINE);
            }
        }
        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                for (AttributeFilter filter : metaMember.getFilters()) {
                    sb.append("    public static volatile ").append(entity.metamodelImportType(Constants.ATTRIBUTE_FILTER_MAPPING)).append('<');
                    sb.append(entity.metamodelImportType(entity.getQualifiedName())).append(", ");
                    if (filter.getFilterValueType().getKind() == TypeKind.TYPEVAR) {
                        sb.append(entity.metamodelImportType(metaMember.getDeclaredJavaType()));
                    } else {
                        DeclaredType filterValueType = (DeclaredType) filter.getFilterValueType();
                        TypeElement filterValueTypeElement = (TypeElement) filterValueType.asElement();
                        sb.append(entity.metamodelImportType(filterValueTypeElement.getQualifiedName().toString()));
                        if (!filterValueType.getTypeArguments().isEmpty()) {
                            sb.append("<");
                            for (TypeMirror typeArgument : filterValueType.getTypeArguments()) {
                                if (typeArgument.getKind() == TypeKind.TYPEVAR) {
                                    sb.append(entity.metamodelImportType(metaMember.getDeclaredJavaType()));
                                } else {
                                    sb.append(entity.metamodelImportType(typeArgument.toString()));
                                }
                                sb.append(", ");
                            }
                            sb.setLength(sb.length() - 2);
                            sb.append(">");
                        }
                    }
                    sb.append("> ").append(metaMember.getPropertyName()).append('_');
                    if (filter.getName().isEmpty()) {
                        sb.append("filter");
                    } else {
                        sb.append(filter.getName());
                    }
                    sb.append(";").append(NEW_LINE);
                }
            }
        }
        sb.append(NEW_LINE);

        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                metaMember.appendMetamodelAttributeNameDeclarationString(sb, entity.getMetamodelImportContext());
                sb.append(NEW_LINE);
            }
        }
        if (context.isGenerateDeepConstants() && entity.hasSubviews()) {
            generateNestedClasses("", "", sb, entity, context, new HashMap<>(context.getMetaEntityViewMap()), new HashSet<>());
        }

        sb.append(NEW_LINE);

        for (MetaConstructor constructor : entity.getConstructors()) {
            Map<String, TypeElement> optionalParameters = createOrderedOptionalParameters(entity, context, constructor.getOptionalParameters());

            sb.append("    public static ").append(entity.metamodelImportType(Constants.ENTITY_VIEW_SETTING)).append("<").append(entity.metamodelImportType(entity.getQualifiedName())).append(", ")
                    .append(entity.metamodelImportType(Constants.CRITERIA_BUILDER)).append("<").append(entity.metamodelImportType(entity.getQualifiedName())).append(">> createSetting");
            sb.append(Character.toUpperCase(constructor.getName().charAt(0))).append(constructor.getName(), 1, constructor.getName().length());
            sb.append("(");
            if (!optionalParameters.isEmpty()) {
                for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                    sb.append(entity.metamodelImportType(entry.getValue().getQualifiedName().toString())).append(" ").append(entry.getKey()).append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append(") {").append(NEW_LINE);

            sb.append("        return EntityViewSetting.create(").append(entity.metamodelImportType(entity.getQualifiedName())).append(".class, \"").append(constructor.getName()).append("\")");
            for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                sb.append(NEW_LINE).append("            .withOptionalParameter(\"").append(entry.getKey()).append("\", ").append(entry.getKey()).append(")");
            }
            sb.append(";").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);

            sb.append("    public static ").append(entity.metamodelImportType(Constants.ENTITY_VIEW_SETTING)).append("<").append(entity.metamodelImportType(entity.getQualifiedName())).append(", ")
                    .append(entity.metamodelImportType(Constants.PAGINATED_CRITERIA_BUILDER)).append("<").append(entity.metamodelImportType(entity.getQualifiedName())).append(">> createPaginatedSetting");
            sb.append(Character.toUpperCase(constructor.getName().charAt(0))).append(constructor.getName(), 1, constructor.getName().length());
            sb.append("(int firstResult, int maxResults");
            for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                sb.append(", ").append(entity.metamodelImportType(entry.getValue().getQualifiedName().toString())).append(" ").append(entry.getKey());
            }
            sb.append(") {").append(NEW_LINE);

            sb.append("        return EntityViewSetting.create(").append(entity.metamodelImportType(entity.getQualifiedName())).append(".class, firstResult, maxResults, \"").append(constructor.getName()).append("\")");
            for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                sb.append(NEW_LINE).append("            .withOptionalParameter(\"").append(entry.getKey()).append("\", ").append(entry.getKey()).append(")");
            }
            sb.append(";").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
        }

        for (Map.Entry<String, ViewFilter> filterEntry : entity.getViewFilters().entrySet()) {
            Map<String, TypeElement> optionalParameters = new TreeMap<>();
            for (Map.Entry<String, TypeElement> entry : filterEntry.getValue().getOptionalParameters().entrySet()) {
                if (!context.getOptionalParameters().containsKey(entry.getKey()) && !entity.getOptionalParameters().containsKey(entry.getKey())) {
                    optionalParameters.put(entry.getKey(), entry.getValue());
                }
            }

            sb.append("    public static void apply");
            String viewFilterName = filterEntry.getKey();
            sb.append(Character.toUpperCase(viewFilterName.charAt(0))).append(viewFilterName, 1, viewFilterName.length());
            sb.append("(").append(entity.metamodelImportType(Constants.ENTITY_VIEW_SETTING)).append("<").append(entity.metamodelImportType(entity.getQualifiedName())).append(", ?> setting");
            for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                sb.append(", ").append(entity.metamodelImportType(entry.getValue().getQualifiedName().toString())).append(" ").append(entry.getKey());
            }
            sb.append(") {").append(NEW_LINE);

            sb.append("        setting.withViewFilter(\"").append(viewFilterName).append("\")");
            for (Map.Entry<String, TypeElement> entry : optionalParameters.entrySet()) {
                sb.append(NEW_LINE).append("            .withOptionalParameter(\"").append(entry.getKey()).append("\", ").append(entry.getKey()).append(")");
            }
            sb.append(";").append(NEW_LINE);
            sb.append("    }").append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static Map<String, TypeElement> createOrderedOptionalParameters(MetaEntityView entity, Context context, Map<String, TypeElement> elementOptionalParameters) {
        Map<String, TypeElement> optionalParameters = new TreeMap<>();
        for (Map.Entry<String, TypeElement> entry : elementOptionalParameters.entrySet()) {
            if (!context.getOptionalParameters().containsKey(entry.getKey())) {
                TypeElement typeElement = entity.getOptionalParameters().get(entry.getKey());
                if (typeElement == null) {
                    optionalParameters.put(entry.getKey(), entry.getValue());
                } else {
                    if (context.getTypeUtils().isAssignable(typeElement.asType(), entry.getValue().asType())) {
                        optionalParameters.put(entry.getKey(), typeElement);
                    } else {
                        optionalParameters.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        for (Map.Entry<String, TypeElement> entry : entity.getOptionalParameters().entrySet()) {
            if (!context.getOptionalParameters().containsKey(entry.getKey())) {
                optionalParameters.put(entry.getKey(), entry.getValue());
            }
        }
        return optionalParameters;
    }

    private static void generateNestedClasses(String parentProperty, String parent, StringBuilder sb, MetaEntityView entity, Context context, Map<String, MetaEntityView> entityViewMap, Set<MetaEntityView> visited) {
        if (visited.add(entity)) {
            for (MetaAttribute metaMember : entity.getMembers()) {
                if (metaMember.isSubview()) {
                    TypeElement subviewElement = metaMember.getSubviewElement();
                    String subviewFqcn = subviewElement.getQualifiedName().toString();
                    MetaEntityView subviewEntityView = entityViewMap.get(subviewFqcn);
                    if (subviewEntityView == null) {
                        subviewEntityView = new AnnotationMetaEntityView(subviewElement, context);
                        entityViewMap.put(subviewFqcn, subviewEntityView);
                    }

                    String newParent;
                    String newParentProperty;
                    if (parent.isEmpty()) {
                        newParent = metaMember.getPropertyName();
                        StringBuilder tempSb = new StringBuilder();
                        appendPropertyNameAsConstant(tempSb, metaMember.getPropertyName());
                        newParentProperty = tempSb.toString();
                    } else {
                        newParent = parent + "." + metaMember.getPropertyName();
                        StringBuilder tempSb = new StringBuilder(parentProperty).append("__");
                        appendPropertyNameAsConstant(tempSb, metaMember.getPropertyName());
                        newParentProperty = tempSb.toString();
                    }
                    for (MetaAttribute subviewMetaMember : subviewEntityView.getMembers()) {
                        sb.append("    ").append("public static final String ").append(newParentProperty);
                        appendPropertyNameAsConstant(sb, subviewMetaMember.getPropertyName());
                        sb.append(" = \"").append(newParent).append(".").append(subviewMetaMember.getPropertyName()).append("\";").append(NEW_LINE);
                    }
                    if (subviewEntityView.hasSubviews()) {
                        sb.append(NEW_LINE);
                        generateNestedClasses(newParentProperty, newParent, sb, subviewEntityView, context, entityViewMap, visited);
                    }
                }
            }
            visited.remove(entity);
        }
    }

    private static void appendPropertyNameAsConstant(StringBuilder sb, String propertyName) {
        for (int i = 0; i < propertyName.length(); i++) {
            final char c = propertyName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
    }
}
