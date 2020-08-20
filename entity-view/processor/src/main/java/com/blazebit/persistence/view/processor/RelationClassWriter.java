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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class RelationClassWriter {

    public static final String RELATION_CLASS_NAME_SUFFIX = "Relation";
    private static final String NEW_LINE = System.lineSeparator();

    private RelationClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + RELATION_CLASS_NAME_SUFFIX, entity.getRelationImportContext(), context);
    }

    private static void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, entity.getRelationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        String entityViewFqcn = entity.relationImportType(entity.getQualifiedName().toString());
        sb.append("@").append(entity.relationImportType(Constants.STATIC_RELATION)).append("(").append(entity.relationImportType(entity.getQualifiedName())).append(".class)");
        sb.append(NEW_LINE);
        sb.append("public class ").append(entity.getSimpleName()).append(RELATION_CLASS_NAME_SUFFIX).append("<T, A extends ").append(entity.relationImportType(Constants.METHOD_ATTRIBUTE))
                .append("<?, ?>> extends ").append(entity.relationImportType(Constants.ATTRIBUTE_PATH_WRAPPER)).append("<T, ").append(entityViewFqcn).append(", ").append(entityViewFqcn).append("> {").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    public ").append(entity.getSimpleName()).append(RELATION_CLASS_NAME_SUFFIX).append("(").append(entity.relationImportType(Constants.ATTRIBUTE_PATH)).append("<T, ").append(entityViewFqcn).append(", ").append(entityViewFqcn).append("> path) {").append(NEW_LINE);
        sb.append("        super(path);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                if (metaMember.isSubview()) {
                    String relationType = entity.relationImportType(metaMember.getGeneratedTypePrefix() + (metaMember.isMultiCollection() ? MultiRelationClassWriter.MULTI_RELATION_CLASS_NAME_SUFFIX : RelationClassWriter.RELATION_CLASS_NAME_SUFFIX));
                    sb.append("    public ").append(relationType).append("<T, ");
                    if (metaMember.isMultiCollection()) {
                        metaMember.appendElementType(sb, entity.getRelationImportContext());
                        sb.append(", ");
                    }
                    metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                    sb.append("> ").append(metaMember.getPropertyName()).append("() {").append(NEW_LINE);
                    sb.append("        ").append(relationType).append("<").append(entityViewFqcn).append(", ");
                    if (metaMember.isMultiCollection()) {
                        metaMember.appendElementType(sb, entity.getRelationImportContext());
                        sb.append(", ");
                    }
                    metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                    sb.append("> relation = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                    sb.append('.').append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("        return new ").append(relationType).append("<>(relation == null ? getWrapped().<");
                    if (metaMember.isMultiCollection()) {
                        sb.append(entity.relationImportType(metaMember.getType()));
                        sb.append(", ");
                    }
                    metaMember.appendElementType(sb, entity.getRelationImportContext());
                    if (metaMember.isMultiCollection()) {
                        sb.append(">getMulti(\"");
                    } else {
                        sb.append(">get(\"");
                    }
                    sb.append(metaMember.getPropertyName()).append("\") : getWrapped().get(relation));").append(NEW_LINE);
                } else {
                    sb.append("    public ").append(entity.relationImportType(Constants.ATTRIBUTE_PATH)).append("<T, ");
                    sb.append(entity.relationImportType(metaMember.getType()));
                    sb.append(", ");
                    metaMember.appendElementType(sb, entity.getRelationImportContext());
                    sb.append("> ").append(metaMember.getPropertyName()).append("() {").append(NEW_LINE);
                    sb.append("        ");
                    metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                    sb.append(" attribute = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                    sb.append('.').append(metaMember.getPropertyName()).append(';').append(NEW_LINE);
                    sb.append("        return attribute == null ? getWrapped().<");
                    if (metaMember.isMultiCollection()) {
                        sb.append(entity.relationImportType(metaMember.getType()));
                        sb.append(", ");
                    }
                    metaMember.appendElementType(sb, entity.getRelationImportContext());
                    if (metaMember.isMultiCollection()) {
                        sb.append(">getMulti(\"");
                    } else {
                        sb.append(">get(\"");
                    }
                    sb.append(metaMember.getPropertyName()).append("\") : getWrapped().get(attribute);").append(NEW_LINE);
                }
                sb.append("    }").append(NEW_LINE);
                sb.append(NEW_LINE);
            }
        }

        sb.append("    public A attr() {").append(NEW_LINE);
        sb.append("        return (A) getWrapped().getAttributes().get(getWrapped().getAttributes().size() - 1);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                for (AttributeFilter filter : metaMember.getFilters()) {
                    sb.append(NEW_LINE);
                    sb.append("    public ").append(entity.relationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH)).append("<T, ");
                    if (filter.getFilterValueType().getKind() == TypeKind.TYPEVAR) {
                        sb.append(entity.relationImportType(metaMember.getRealType()));
                    } else {
                        DeclaredType filterValueType = (DeclaredType) filter.getFilterValueType();
                        TypeElement filterValueTypeElement = (TypeElement) filterValueType.asElement();
                        sb.append(entity.relationImportType(filterValueTypeElement.getQualifiedName().toString()));
                        if (!filterValueType.getTypeArguments().isEmpty()) {
                            sb.append("<");
                            for (TypeMirror typeArgument : filterValueType.getTypeArguments()) {
                                if (typeArgument.getKind() == TypeKind.TYPEVAR) {
                                    sb.append(entity.relationImportType(metaMember.getRealType()));
                                } else {
                                    sb.append(entity.relationImportType(typeArgument.toString()));
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
                    sb.append("() {").append(NEW_LINE);
                    if (metaMember.isSubview()) {
                        String relationType = entity.relationImportType(metaMember.getGeneratedTypePrefix() + (metaMember.isMultiCollection() ? MultiRelationClassWriter.MULTI_RELATION_CLASS_NAME_SUFFIX : RelationClassWriter.RELATION_CLASS_NAME_SUFFIX));
                        sb.append("        ").append(relationType).append("<").append(entityViewFqcn).append(", ");
                        metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                        sb.append("> relation = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                        sb.append('.').append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
                        sb.append("        return relation == null ? new ").append(entity.relationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(\"").append(metaMember.getPropertyName()).append("\"), \"").append(filter.getName())
                                .append("\") : new ").append(entity.relationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(relation), relation.").append(metaMember.getPropertyName()).append("_");
                        if (filter.getName().isEmpty()) {
                            sb.append("filter");
                        } else {
                            sb.append(filter.getName());
                        }
                        sb.append("());").append(NEW_LINE);
                    } else {
                        sb.append("        ");
                        metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                        sb.append(" attribute = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                        sb.append('.').append(metaMember.getPropertyName()).append(';').append(NEW_LINE);
                        sb.append("        return attribute == null ? new ").append(entity.relationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(\"").append(metaMember.getPropertyName()).append("\"), \"").append(filter.getName())
                                .append("\") : new ").append(entity.relationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(attribute), ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)
                                .append(".").append(metaMember.getPropertyName()).append("_");
                        if (filter.getName().isEmpty()) {
                            sb.append("filter");
                        } else {
                            sb.append(filter.getName());
                        }
                        sb.append(");").append(NEW_LINE);
                    }

                    sb.append("    }").append(NEW_LINE);
                }
            }
        }

        sb.append(NEW_LINE);
        sb.append("}");
        sb.append(NEW_LINE);
    }
}
