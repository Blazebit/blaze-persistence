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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class ImplementationClassWriter {

    private static final String IMPL_CLASS_NAME_SUFFIX = "Impl";
    private static final String NEW_LINE = System.lineSeparator();

    private ImplementationClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + IMPL_CLASS_NAME_SUFFIX, entity.getImplementationImportContext(), context);
    }

    private static void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, entity.getImplementationImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        printClassDeclaration(sb, entity, context);

        sb.append(NEW_LINE);

        if (entity.needsEntityViewManager()) {
            sb.append("    public static volatile ").append(entity.implementationImportType(Constants.ENTITY_VIEW_MANAGER)).append(" evm;");
            sb.append(NEW_LINE);
            sb.append(NEW_LINE);
        }

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            metaMember.appendImplementationAttributeDeclarationString(sb);
            sb.append(NEW_LINE);
        }

        sb.append(NEW_LINE);

        printConstructors(sb, entity, context);

        sb.append(NEW_LINE);

        for (MetaAttribute metaMember : members) {
            metaMember.appendImplementationAttributeGetterAndSetterString(sb);
            sb.append(NEW_LINE);
        }

        sb.append(NEW_LINE);

        for (ExecutableElement specialMember : entity.getSpecialMembers()) {
            if (Constants.ENTITY_VIEW_MANAGER.equals(specialMember.getReturnType().toString())) {
                sb.append("    @Override");
                sb.append(NEW_LINE);
                sb.append("    public ").append(entity.implementationImportType(specialMember.getReturnType().toString())).append(" ").append(specialMember.getSimpleName().toString()).append("() { return evm; }");
                sb.append(NEW_LINE);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "Unsupported special member: " + specialMember);
            }
        }

        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void printClassDeclaration(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("public class " + entity.getSimpleName() + IMPL_CLASS_NAME_SUFFIX);

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        if (!typeArguments.isEmpty()) {
            sb.append("<");
            printTypeVariable(sb, entity, typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                printTypeVariable(sb, entity, typeArguments.get(i));
            }
            sb.append(">");
        }

        if (entity.getTypeElement().getKind() == ElementKind.INTERFACE) {
            sb.append(" implements ");
        } else {
            sb.append(" extends ");
        }
        sb.append(entity.implementationImportType(entity.getBaseSuperclass()));

        if (!typeArguments.isEmpty()) {
            sb.append("<");
            sb.append(typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i));
            }
            sb.append(">");
        }

        sb.append(" {");
        sb.append(NEW_LINE);
    }

    private static void printTypeVariable(StringBuilder sb, MetaEntityView entity, TypeVariable t) {
        sb.append(t);
        if (t.getLowerBound().getKind() == TypeKind.NULL) {
            sb.append(" extends ").append(entity.implementationImportType(t.getUpperBound().toString()));
        } else {
            sb.append(" super ").append(entity.implementationImportType(t.getLowerBound().toString()));
        }
    }

    private static void printConstructors(StringBuilder sb, MetaEntityView entity, Context context) {
        printEmptyConstructor(sb, entity, context);
        sb.append(NEW_LINE);
        if (entity.getIdMember() != null && entity.getMembers().size() > 1) {
            printIdConstructor(sb, entity, context);
            sb.append(NEW_LINE);
        }

        printFullConstructor(sb, entity, context);
    }

    private static void printEmptyConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("() {");
        sb.append(NEW_LINE);

        for (MetaAttribute member : entity.getMembers()) {
            member.appendImplementationAttributeConstructorAssignmentDefaultString(sb);
            sb.append(NEW_LINE);
        }

        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printIdConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        MetaAttribute idMember = entity.getIdMember();
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(");
        idMember.appendImplementationAttributeConstructorParameterString(sb);
        sb.append(") {");
        sb.append(NEW_LINE);

        for (MetaAttribute member : entity.getMembers()) {
            if (member == idMember) {
                member.appendImplementationAttributeConstructorAssignmentString(sb);
                sb.append(NEW_LINE);
            } else {
                member.appendImplementationAttributeConstructorAssignmentDefaultString(sb);
                sb.append(NEW_LINE);
            }
        }

        sb.append("    }");
        sb.append(NEW_LINE);
    }

    private static void printFullConstructor(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("    public ").append(entity.getSimpleName()).append(IMPL_CLASS_NAME_SUFFIX).append("(");
        boolean first = true;
        for (MetaAttribute member : entity.getMembers()) {
            if (first) {
                first = false;
                sb.append(NEW_LINE);
                member.appendImplementationAttributeConstructorParameterString(sb);
            } else {
                sb.append(",");
                sb.append(NEW_LINE);
                member.appendImplementationAttributeConstructorParameterString(sb);
            }
        }
        if (first) {
            sb.append(") {");
            sb.append(NEW_LINE);
        } else {
            sb.append(NEW_LINE);
            sb.append("    ) {");
            sb.append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            member.appendImplementationAttributeConstructorAssignmentString(sb);
            sb.append(NEW_LINE);
        }

        sb.append("    }");
        sb.append(NEW_LINE);
    }

}
