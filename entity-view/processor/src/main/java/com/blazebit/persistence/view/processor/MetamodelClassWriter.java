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

package com.blazebit.persistence.view.processor;

import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class MetamodelClassWriter {

    private static final String META_MODEL_CLASS_NAME_SUFFIX = "_";
    private static final String NEW_LINE = System.lineSeparator();

    private MetamodelClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + META_MODEL_CLASS_NAME_SUFFIX, entity.getMetamodelImportContext(), context);
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

        sb.append("@").append(entity.metamodelImportType(Constants.STATIC_METAMODEL)).append("(").append(entity.getSimpleName()).append(".class)");
        sb.append(NEW_LINE);
        sb.append("public abstract class ").append(entity.getSimpleName()).append(META_MODEL_CLASS_NAME_SUFFIX).append(" {");

        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            metaMember.appendMetamodelAttributeDeclarationString(sb);
            sb.append(NEW_LINE);
        }
        sb.append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            metaMember.appendMetamodelAttributeNameDeclarationString(sb);
            sb.append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("}");
        sb.append(NEW_LINE);
    }
}
