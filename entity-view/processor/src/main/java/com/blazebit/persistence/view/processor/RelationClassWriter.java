/*
 * Copyright 2014 - 2024 Blazebit.
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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class RelationClassWriter extends ClassWriter {

    public static final String RELATION_CLASS_NAME_SUFFIX = "Relation";
    private static final String NEW_LINE = System.lineSeparator();

    public RelationClassWriter(FileObject fileObject, MetaEntityView entity, Context context, Collection<Runnable> mainThreadQueue, LongAdder elapsedTime) {
        super(fileObject, entity, entity.getRelationImportContext(), context, mainThreadQueue, elapsedTime);
    }

    public static void writeFile(MetaEntityView entity, Context context, ExecutorService executorService, Collection<Runnable> mainThreadQueue, LongAdder relationTime) {
        FileObject fileObject = ClassWriter.createFile(entity.getPackageName(), entity.getSimpleName() + RELATION_CLASS_NAME_SUFFIX, context, entity.getOriginatingElements());
        if (fileObject == null) {
            return;
        }
        executorService.submit(new RelationClassWriter(fileObject, entity, context, mainThreadQueue, relationTime));
    }

    @Override
    public void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriter.writeGeneratedAnnotation(sb, entity.getRelationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriter.writeSuppressWarnings());
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
                        sb.append(entity.relationImportType(metaMember.getModelType()));
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
                    sb.append(entity.relationImportType(metaMember.getModelType()));
                    sb.append(", ");
                    metaMember.appendElementType(sb, entity.getRelationImportContext());
                    sb.append("> ").append(metaMember.getPropertyName()).append("() {").append(NEW_LINE);
                    sb.append("        ");
                    metaMember.appendMetamodelAttributeType(sb, entity.getRelationImportContext());
                    sb.append(" attribute = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                    sb.append('.').append(metaMember.getPropertyName()).append(';').append(NEW_LINE);
                    sb.append("        return attribute == null ? getWrapped().<");
                    if (metaMember.isMultiCollection()) {
                        sb.append(entity.relationImportType(metaMember.getModelType()));
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
                    filter.getFilterValueType().append(entity.getRelationImportContext(), sb);
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
