/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
public final class MultiRelationClassWriter extends ClassWriter {

    public static final String MULTI_RELATION_CLASS_NAME_SUFFIX = "MultiRelation";
    private static final String NEW_LINE = System.lineSeparator();

    private MultiRelationClassWriter(FileObject fileObject, MetaEntityView entity, Context context, Collection<Runnable> mainThreadQueue, LongAdder elapsedTime) {
        super(fileObject, entity, entity.getMultiRelationImportContext(), context, mainThreadQueue, elapsedTime);
    }

    public static void writeFile(MetaEntityView entity, Context context, ExecutorService executorService, Collection<Runnable> mainThreadQueue, LongAdder multiRelationTime) {
        FileObject fileObject = ClassWriter.createFile(entity.getPackageName(), entity.getSimpleName() + MULTI_RELATION_CLASS_NAME_SUFFIX, context, entity.getOriginatingElements());
        if (fileObject == null) {
            return;
        }
        executorService.submit(new MultiRelationClassWriter(fileObject, entity, context, mainThreadQueue, multiRelationTime));
    }

    @Override
    public void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        entity.multiRelationImportType(Constants.COLLECTION);
        if (context.addGeneratedAnnotation()) {
            ClassWriter.writeGeneratedAnnotation(sb, entity.getMultiRelationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriter.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        String entityViewFqcn = entity.multiRelationImportType(entity.getQualifiedName());
        sb.append("@").append(entity.multiRelationImportType(Constants.STATIC_RELATION)).append("(").append(entityViewFqcn).append(".class)");
        sb.append(NEW_LINE);
        sb.append("public class ").append(entity.getSimpleName()).append(MULTI_RELATION_CLASS_NAME_SUFFIX).append("<T, C extends Collection<").append(entityViewFqcn).append(">, A extends ").append(entity.multiRelationImportType(Constants.METHOD_PLURAL_ATTRIBUTE))
                .append("<?, ?, C>> extends ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_PATH_WRAPPER)).append("<T, ").append(entityViewFqcn).append(", C> {").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    public ").append(entity.getSimpleName()).append(MULTI_RELATION_CLASS_NAME_SUFFIX).append("(").append(entity.multiRelationImportType(Constants.ATTRIBUTE_PATH)).append("<T, ").append(entityViewFqcn).append(", C> path) {").append(NEW_LINE);
        sb.append("        super(path);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            if (!metaMember.isSynthetic()) {
                if (metaMember.isSubview()) {
                    String relationType = entity.multiRelationImportType(metaMember.getGeneratedTypePrefix() + (metaMember.isMultiCollection() ? MultiRelationClassWriter.MULTI_RELATION_CLASS_NAME_SUFFIX : RelationClassWriter.RELATION_CLASS_NAME_SUFFIX));
                    sb.append("    public ").append(relationType).append("<T, ");
                    if (metaMember.isMultiCollection()) {
                        metaMember.appendElementType(sb, entity.getMultiRelationImportContext());
                        sb.append(", ");
                    }
                    metaMember.appendMetamodelAttributeType(sb, entity.getMultiRelationImportContext());
                    sb.append("> ").append(metaMember.getPropertyName()).append("() {").append(NEW_LINE);
                    sb.append("        ").append(relationType).append("<").append(entityViewFqcn).append(", ");
                    if (metaMember.isMultiCollection()) {
                        metaMember.appendElementType(sb, entity.getMultiRelationImportContext());
                        sb.append(", ");
                    }
                    metaMember.appendMetamodelAttributeType(sb, entity.getMultiRelationImportContext());
                    sb.append("> relation = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                    sb.append('.').append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("        return new ").append(relationType).append("<>(relation == null ? getWrapped().<");

                    if (metaMember.isMultiCollection()) {
                        sb.append(entity.multiRelationImportType(metaMember.getModelType()));
                        sb.append(", ");
                    }
                    metaMember.appendElementType(sb, entity.getMultiRelationImportContext());
                    if (metaMember.isMultiCollection()) {
                        sb.append(">getMulti(\"");
                    } else {
                        sb.append(">get(\"");
                    }
                    sb.append(metaMember.getPropertyName()).append("\") : getWrapped().get(relation));").append(NEW_LINE);
                } else {
                    sb.append("    public ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_PATH)).append("<T, ");
                    sb.append(entity.multiRelationImportType(metaMember.getModelType()));
                    sb.append(", ");
                    metaMember.appendElementType(sb, entity.getMultiRelationImportContext());
                    sb.append("> ").append(metaMember.getPropertyName()).append("() {").append(NEW_LINE);
                    sb.append("        ");
                    metaMember.appendMetamodelAttributeType(sb, entity.getMultiRelationImportContext());
                    sb.append(" attribute = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                    sb.append('.').append(metaMember.getPropertyName()).append(';').append(NEW_LINE);
                    sb.append("        return attribute == null ? getWrapped().<");
                    if (metaMember.isMultiCollection()) {
                        sb.append(entity.multiRelationImportType(metaMember.getModelType()));
                        sb.append(", ");
                    }
                    metaMember.appendElementType(sb, entity.getMultiRelationImportContext());
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
                    sb.append("    public ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH)).append("<T, ");
                    filter.getFilterValueType().append(entity.getMultiRelationImportContext(), sb);
                    sb.append("> ").append(metaMember.getPropertyName()).append('_');
                    if (filter.getName().isEmpty()) {
                        sb.append("filter");
                    } else {
                        sb.append(filter.getName());
                    }
                    sb.append("() {").append(NEW_LINE);
                    if (metaMember.isSubview()) {
                        String relationType = entity.multiRelationImportType(metaMember.getGeneratedTypePrefix() + (metaMember.isMultiCollection() ? MultiRelationClassWriter.MULTI_RELATION_CLASS_NAME_SUFFIX : RelationClassWriter.RELATION_CLASS_NAME_SUFFIX));
                        sb.append("        ").append(relationType).append("<").append(entityViewFqcn).append(", ");
                        metaMember.appendMetamodelAttributeType(sb, entity.getMultiRelationImportContext());
                        sb.append("> relation = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                        sb.append('.').append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
                        sb.append("        return relation == null ? new ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(\"").append(metaMember.getPropertyName()).append("\"), \"").append(filter.getName())
                                .append("\") : new ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(relation), relation.").append(metaMember.getPropertyName()).append("_");
                        if (filter.getName().isEmpty()) {
                            sb.append("filter");
                        } else {
                            sb.append(filter.getName());
                        }
                        sb.append("());").append(NEW_LINE);
                    } else {
                        sb.append("        ");
                        metaMember.appendMetamodelAttributeType(sb, entity.getMultiRelationImportContext());
                        sb.append(" attribute = ").append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX);
                        sb.append('.').append(metaMember.getPropertyName()).append(';').append(NEW_LINE);
                        sb.append("        return attribute == null ? new ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
                                .append("<>(getWrapped().get(\"").append(metaMember.getPropertyName()).append("\"), \"").append(filter.getName())
                                .append("\") : new ").append(entity.multiRelationImportType(Constants.ATTRIBUTE_FILTER_MAPPING_PATH))
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
