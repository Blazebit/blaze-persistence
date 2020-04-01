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

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaCollection;
import com.blazebit.persistence.view.processor.annotation.AnnotationMetaMap;
import com.blazebit.persistence.view.processor.annotation.AnnotationMetaSingleAttribute;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class BuilderClassWriter {

    private static final String BUILDER_CLASS_NAME_SUFFIX = "Builder";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String PARAMETER_PREFIX = "Param";

    private BuilderClassWriter() {
    }

    public static void writeFile(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.setLength(0);
        generateBody(sb, entity, context);
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + BUILDER_CLASS_NAME_SUFFIX, entity.getBuilderImportContext(), context);
    }

    private static void generateBody(StringBuilder sb, MetaEntityView entity, Context context) {
        if (context.addGeneratedAnnotation()) {
            ClassWriterUtils.writeGeneratedAnnotation(sb, entity.getBuilderImportContext(), context);
            sb.append(NEW_LINE);
        }
        if (context.isAddSuppressWarningsAnnotation()) {
            sb.append(ClassWriterUtils.writeSuppressWarnings());
            sb.append(NEW_LINE);
        }

        printClassDeclaration(sb, entity, context);

        sb.append(NEW_LINE);

        Collection<MetaAttribute> members = entity.getMembers();
        for (MetaAttribute metaMember : members) {
            metaMember.appendBuilderAttributeDeclarationString(sb);
            sb.append(NEW_LINE);
        }
        sb.append("    protected final ").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters;").append(NEW_LINE);

        sb.append(NEW_LINE);

        printConstructors(sb, entity, context);

        sb.append(NEW_LINE);

        for (MetaAttribute metaMember : members) {
            appendMember(sb, entity, metaMember, "BuilderType");
        }

        appendUtilityMethods(sb, entity, context);
        appendGetAndWithMethods(sb, entity, context);
        appendWithBuilderMethods(sb, entity, context);

        sb.append(NEW_LINE);

        for (ExecutableElement specialMember : entity.getSpecialMembers()) {
            if (Constants.ENTITY_VIEW_MANAGER.equals(specialMember.getReturnType().toString())) {
                sb.append("    @Override");
                sb.append(NEW_LINE);
                sb.append("    public ").append(entity.builderImportType(specialMember.getReturnType().toString())).append(" ").append(specialMember.getSimpleName().toString())
                        .append("() { return ").append(entity.getSimpleName()).append(ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX).append(".").append(ImplementationClassWriter.SERIALIZABLE_EVM_FIELD_NAME).append("; }");
                sb.append(NEW_LINE);
            } else {
                context.logMessage(Diagnostic.Kind.ERROR, "Unsupported special member: " + specialMember);
            }
        }

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        for (MetaConstructor constructor : entity.getConstructors()) {
            String className = Character.toUpperCase(constructor.getName().charAt(0)) + constructor.getName().substring(1);
            String builderType = entity.importType(Constants.ENTITY_VIEW_BUILDER) + "<" + entity.builderImportType(entity.getBaseSuperclass()) + ">";
            sb.append(NEW_LINE);
            sb.append("    public static class ").append(className);

            if (!typeArguments.isEmpty()) {
                sb.append("<");
                printTypeVariable(sb, entity, typeArguments.get(0));
                for (int i = 1; i < typeArguments.size(); i++) {
                    sb.append(", ");
                    printTypeVariable(sb, entity, typeArguments.get(i));
                }
                sb.append(">");
            }

            sb.append(" extends ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX);
            sb.append("<");
            for (int i = 0; i < typeArguments.size(); i++) {
                sb.append(typeArguments.get(i));
                sb.append(", ");
            }
            sb.append(entity.importType(Constants.ENTITY_VIEW_BUILDER)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(">");
            sb.append("> implements ");

            sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER));
            sb.append("<");
            sb.append(entity.builderImportType(entity.getBaseSuperclass()));
            sb.append(">");

            sb.append(" {").append(NEW_LINE);

            for (MetaAttribute metaMember : constructor.getParameters()) {
                metaMember.appendBuilderAttributeDeclarationString(sb);
                sb.append(NEW_LINE);
            }

            sb.append(NEW_LINE);
            sb.append("        public ").append(className).append("(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters) {").append(NEW_LINE);
            sb.append("            super(optionalParameters);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);

            // T build()
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public ").append(entity.builderImportType(entity.getBaseSuperclass())).append(" build() {").append(NEW_LINE);
            sb.append("            return new ").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append("(");
            if (members.isEmpty() && constructor.getParameters().isEmpty()) {
                sb.append(");").append(NEW_LINE);
            } else {
                sb.append(NEW_LINE);
                Iterator<MetaAttribute> iterator = members.iterator();
                MetaAttribute member;
                if (iterator.hasNext()) {
                    member = iterator.next();
                    sb.append("            this.").append(member.getPropertyName());

                    for (; iterator.hasNext(); ) {
                        member = iterator.next();
                        sb.append(",").append(NEW_LINE).append("            this.").append(member.getPropertyName());
                    }

                    iterator = constructor.getParameters().iterator();
                } else {
                    iterator = constructor.getParameters().iterator();
                    member = iterator.next();
                    sb.append("            this.").append(member.getPropertyName());
                }

                for (; iterator.hasNext(); ) {
                    member = iterator.next();
                    sb.append(",").append(NEW_LINE).append("            this.").append(member.getPropertyName());
                }

                sb.append(NEW_LINE).append("            );").append(NEW_LINE);
            }
            sb.append("        }").append(NEW_LINE);

            // X with(int parameterIndex, Object value)
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public ");

            sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER));
            sb.append("<");
            sb.append(entity.builderImportType(entity.getBaseSuperclass()));
            sb.append(">");

            sb.append(" with(int parameterIndex, Object value) {").append(NEW_LINE);
            sb.append("            switch (parameterIndex) {").append(NEW_LINE);
            List<MetaAttribute> parameters = constructor.getParameters();
            boolean first = true;
            for (int i = 0; i < parameters.size(); i++) {
                first = false;
                MetaAttribute metaMember = parameters.get(i);
                sb.append("                case ").append(i).append(":").append(NEW_LINE);
                sb.append("                    this.").append(metaMember.getPropertyName()).append(" = ");
                String defaultValue = metaMember.getDefaultValue();
                if (metaMember.isPrimitive() || !"null".equals(defaultValue)) {
                    sb.append("value == null ? ").append(defaultValue).append(" : ");
                }
                sb.append("(").append(metaMember.getImplementationTypeString()).append(") value;").append(NEW_LINE);
                sb.append("                    break;").append(NEW_LINE);
            }
            sb.append("                default:").append(NEW_LINE);
            sb.append("                     throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            if (!first) {
                sb.append("        return (").append(builderType).append(") this;").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);

            // <E> E get(int parameterIndex)
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public <E> E get(int parameterIndex) {").append(NEW_LINE);
            sb.append("            switch (parameterIndex) {").append(NEW_LINE);
            for (int i = 0; i < parameters.size(); i++) {
                MetaAttribute metaMember = parameters.get(i);
                sb.append("                case ").append(i).append(":").append(NEW_LINE);
                sb.append("                    return (E) (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
            }
            sb.append("            }").append(NEW_LINE);
            sb.append("            throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);

            appendWithBuilderMethods(sb, constructor, context, entity.importType(Constants.ENTITY_VIEW_BUILDER) + "<" + entity.builderImportType(entity.getBaseSuperclass()) + ">");

            for (MetaAttribute metaMember : constructor.getParameters()) {
                appendMember(sb, entity, metaMember, builderType);
            }

            sb.append("    }").append(NEW_LINE);
        }

        MetaConstructor constructor = entity.getConstructor("init");
        if (constructor == null) {
            if (entity.hasEmptyConstructor()) {
                for (MetaConstructor entityConstructor : entity.getConstructors()) {
                    if (entityConstructor.getParameters().isEmpty()) {
                        constructor = entityConstructor;
                        break;
                    }
                }
            } else {
                constructor = entity.getConstructors().iterator().next();
            }
        }

        String builderType = entity.importType(Constants.ENTITY_VIEW_NESTED_BUILDER) + "<" + entity.builderImportType(entity.getBaseSuperclass()) + ", BuilderResult>";
        sb.append(NEW_LINE);
        sb.append("    public static class Nested<");

        for (int i = 0; i < typeArguments.size(); i++) {
            printTypeVariable(sb, entity, typeArguments.get(i));
            sb.append(", ");
        }
        sb.append("BuilderResult> extends ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX);
        sb.append("<");
        for (int i = 0; i < typeArguments.size(); i++) {
            sb.append(typeArguments.get(i));
            sb.append(", ");
        }
        sb.append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", BuilderResult>");
        sb.append("> implements ");

        sb.append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER));
        sb.append("<");
        sb.append(entity.builderImportType(entity.getBaseSuperclass()));
        sb.append(", BuilderResult>");

        sb.append(" {").append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append("        private final ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LISTENER)).append(" listener;").append(NEW_LINE);
        sb.append("        private final BuilderResult result;").append(NEW_LINE);

        for (MetaAttribute metaMember : constructor.getParameters()) {
            metaMember.appendBuilderAttributeDeclarationString(sb);
            sb.append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("        public Nested(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters, ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LISTENER)).append(" listener, BuilderResult result) {").append(NEW_LINE);
        sb.append("            super(optionalParameters);").append(NEW_LINE);
        sb.append("            this.listener = listener;").append(NEW_LINE);
        sb.append("            this.result = result;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);

        // X build()
        sb.append(NEW_LINE);
        sb.append("        @Override").append(NEW_LINE);
        sb.append("        public BuilderResult build() {").append(NEW_LINE);
        sb.append("            listener.onBuildComplete(new ").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append("(");
        if (members.isEmpty() && constructor.getParameters().isEmpty()) {
            sb.append("));").append(NEW_LINE);
        } else {
            sb.append(NEW_LINE);
            Iterator<MetaAttribute> iterator = members.iterator();
            MetaAttribute member;
            if (iterator.hasNext()) {
                member = iterator.next();
                sb.append("            this.").append(member.getPropertyName());

                for (; iterator.hasNext(); ) {
                    member = iterator.next();
                    sb.append(",").append(NEW_LINE).append("            this.").append(member.getPropertyName());
                }

                iterator = constructor.getParameters().iterator();
            } else {
                iterator = constructor.getParameters().iterator();
                member = iterator.next();
                sb.append("            this.").append(member.getPropertyName());
            }

            for (; iterator.hasNext(); ) {
                member = iterator.next();
                sb.append(",").append(NEW_LINE).append("            this.").append(member.getPropertyName());
            }

            sb.append(NEW_LINE).append("            ));").append(NEW_LINE);
        }
        sb.append("            return result;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);

        // X with(int parameterIndex, Object value)
        sb.append(NEW_LINE);
        sb.append("        @Override").append(NEW_LINE);
        sb.append("        public ");

        sb.append(builderType);

        sb.append(" with(int parameterIndex, Object value) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        List<MetaAttribute> parameters = constructor.getParameters();
        boolean first = true;
        for (int i = 0; i < parameters.size(); i++) {
            first = false;
            MetaAttribute metaMember = parameters.get(i);
            sb.append("            case ").append(i).append(":").append(NEW_LINE);
            sb.append("                this.").append(metaMember.getPropertyName()).append(" = ");
            String defaultValue = metaMember.getDefaultValue();
            if (metaMember.isPrimitive() || !"null".equals(defaultValue)) {
                sb.append("value == null ? ").append(defaultValue).append(" : ");
            }
            sb.append("(").append(metaMember.getImplementationTypeString()).append(") value;").append(NEW_LINE);
            sb.append("                break;").append(NEW_LINE);
        }
        sb.append("            default:").append(NEW_LINE);
        sb.append("                throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        if (!first) {
            sb.append("        return (").append(builderType).append(") this;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        // <E> E get(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("        @Override").append(NEW_LINE);
        sb.append("        public <E> E get(int parameterIndex) {").append(NEW_LINE);
        sb.append("            switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < parameters.size(); i++) {
            MetaAttribute metaMember = parameters.get(i);
            sb.append("                case ").append(i).append(":").append(NEW_LINE);
            sb.append("                    return (E) (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
        }
        sb.append("            }").append(NEW_LINE);
        sb.append("            throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);

        appendWithBuilderMethods(sb, constructor, context, entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER) + "<" + entity.builderImportType(entity.getBaseSuperclass()) + ", BuilderResult>");

        for (MetaAttribute metaMember : constructor.getParameters()) {
            appendMember(sb, entity, metaMember, builderType);
        }

        sb.append("    }").append(NEW_LINE);

        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void appendMember(StringBuilder sb, MetaEntityView entity, MetaAttribute metaMember, String builderType) {
        String memberBuilderFqn = metaMember.getGeneratedTypePrefix() + BuilderClassWriter.BUILDER_CLASS_NAME_SUFFIX;
        metaMember.appendBuilderAttributeGetterAndSetterString(sb);
        sb.append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" with");
        if (metaMember.getElement().getKind() == ElementKind.PARAMETER) {
            sb.append(PARAMETER_PREFIX);
        }
        sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
        sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
        sb.append('(');

        sb.append(metaMember.getImplementationTypeString());

        sb.append(' ')
                .append(metaMember.getPropertyName())
                .append(") { this.")
                .append(metaMember.getPropertyName())
                .append(" = ")
                .append(metaMember.getPropertyName())
                .append("; return (").append(builderType).append(") this; }");

        if (metaMember.isSubview()) {
            if (metaMember instanceof AnnotationMetaMap) {
                AnnotationMetaMap mapMember = (AnnotationMetaMap) metaMember;
                String listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_MAP_LISTENER) + "(getMap(\"" + metaMember.getPropertyName() + "\"), key)";
                sb.append(NEW_LINE);
                sb.append("    public ").append(entity.builderImportType(memberBuilderFqn)).append(".Nested<").append(builderType).append("> with");
                if (metaMember.getElement().getKind() == ElementKind.PARAMETER) {
                    sb.append(PARAMETER_PREFIX);
                }
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                sb.append("Builder(Object key) { return new ").append(entity.builderImportType(memberBuilderFqn))
                        .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this); }");

                sb.append(NEW_LINE);

                if (mapMember.isKeySubview()) {
                    String keyMemberBuilderFqn = mapMember.getGeneratedKeyTypePrefix() + BuilderClassWriter.BUILDER_CLASS_NAME_SUFFIX;
                    sb.append(NEW_LINE);
                    sb.append("    public ").append(entity.builderImportType(keyMemberBuilderFqn)).append(".Nested<").append(entity.builderImportType(memberBuilderFqn)).append(".Nested<").append(builderType).append(">> with");
                    if (metaMember.getElement().getKind() == ElementKind.PARAMETER) {
                        sb.append(PARAMETER_PREFIX);
                    }
                    sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                    sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                    sb.append("Builder() { ").append(NEW_LINE);
                    sb.append("        ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LISTENER)).append(" listener = new ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_MAP_KEY_LISTENER)).append("(getMap(\"").append(metaMember.getPropertyName()).append("\"));").append(NEW_LINE);
                    sb.append("        return new ").append(entity.builderImportType(keyMemberBuilderFqn))
                            .append(".Nested<>(optionalParameters, listener, ").append(NEW_LINE);
                    sb.append("            new ").append(entity.builderImportType(memberBuilderFqn))
                            .append(".Nested<>(optionalParameters, listener, (").append(builderType).append(") this)").append(NEW_LINE);
                    sb.append("        );").append(NEW_LINE);
                    sb.append("    }");

                    sb.append(NEW_LINE);
                }
            } else {
                String listener;
                if (metaMember instanceof AnnotationMetaCollection) {
                    if (((AnnotationMetaCollection) metaMember).isIndexedList()) {
                        listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LIST_LISTENER) + "(getCollection(\"" + metaMember.getPropertyName() + "\"), index)";
                        sb.append(NEW_LINE);
                        sb.append("    public ").append(entity.builderImportType(memberBuilderFqn)).append(".Nested<").append(builderType).append("> with");
                        if (metaMember.getElement().getKind() == ElementKind.PARAMETER) {
                            sb.append(PARAMETER_PREFIX);
                        }
                        sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                        sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                        sb.append("Builder(int index) { return new ").append(entity.builderImportType(memberBuilderFqn))
                                .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this); }");

                        sb.append(NEW_LINE);
                    }
                    listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_COLLECTION_LISTENER) + "(getCollection(\"" + metaMember.getPropertyName() + "\"))";
                } else {
                    listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_SINGULAR_NAME_LISTENER) + "(this, \"" + metaMember.getPropertyName() + "\")";
                }
                sb.append(NEW_LINE);
                sb.append("    public ").append(entity.builderImportType(memberBuilderFqn)).append(".Nested<").append(builderType).append("> with");
                if (metaMember.getElement().getKind() == ElementKind.PARAMETER) {
                    sb.append(PARAMETER_PREFIX);
                }
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                sb.append("Builder() { return new ").append(entity.builderImportType(memberBuilderFqn))
                        .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this); }");

                sb.append(NEW_LINE);
            }
        }
        sb.append(NEW_LINE);
    }

    private static void appendUtilityMethods(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append(NEW_LINE);
        sb.append("    protected <E> E get(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return get(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return get(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected BuilderType with(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr, Object value) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName(), value);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex(), value);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> A getCollection(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return getCollection(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return getCollection(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> A getMap(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return getMap(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return getMap(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> A getCollection(String attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (A) ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (A) currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> A getMap(String attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (A) ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (A) currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> A getCollection(int attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (A) ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (A) currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <A extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> A getMap(int attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (A) ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (A) currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected void addListValue(").append(entity.builderImportType(Constants.LIST)).append("<Object> list, int index, Object value) {").append(NEW_LINE);
        sb.append("        if (index > list.size()) {").append(NEW_LINE);
        sb.append("            for (int i = list.size(); i < index; i++) {").append(NEW_LINE);
        sb.append("                list.add(null);").append(NEW_LINE);
        sb.append("            }").append(NEW_LINE);
        sb.append("            list.add(value);").append(NEW_LINE);
        sb.append("        } else if (index < list.size()) {").append(NEW_LINE);
        sb.append("            list.set(index, value);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            list.add(value);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendGetAndWithMethods(StringBuilder sb, MetaEntityView entity, Context context) {
        Collection<MetaAttribute> members = entity.getMembers();

        // X with(String attribute, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType with(String attribute, Object value) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
            sb.append("                this.").append(metaMember.getPropertyName()).append(" = ");
            String defaultValue = metaMember.getDefaultValue();
            if (metaMember.isPrimitive() || !"null".equals(defaultValue)) {
                sb.append("value == null ? ").append(defaultValue).append(" : ");
            }
            sb.append("(").append(metaMember.getImplementationTypeString()).append(") value;").append(NEW_LINE);
            sb.append("                break;").append(NEW_LINE);
        }
        sb.append("            default:").append(NEW_LINE);
        sb.append("                throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        if (!members.isEmpty()) {
            sb.append("        return (BuilderType) this;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        // <E> X with(SingularAttribute<T, E> attribute, E value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> BuilderType with(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute, E value) {").append(NEW_LINE);
        sb.append("        return with((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute, value);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <C> X with(PluralAttribute<T, C, ?> attribute, C value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <C> BuilderType with(").append(entity.builderImportType(Constants.PLURAL_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", C, ?> attribute, C value) {").append(NEW_LINE);
        sb.append("        return with((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute, value);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> E get(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> E get(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
            sb.append("                return (E) (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> E get(SingularAttribute<T, E> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> E get(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute) {").append(NEW_LINE);
        sb.append("        return get((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <C> C get(PluralAttribute<T, C, ?> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <C> C get(").append(entity.builderImportType(Constants.PLURAL_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", C, ?> attribute) {").append(NEW_LINE);
        sb.append("        return get((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // TODO: check value?

        // X withElement(String attribute, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withElement(String attribute, Object value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withElement(int parameterIndex, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withElement(int parameterIndex, Object value) {").append(NEW_LINE);
        sb.append("        getCollection(parameterIndex).add(value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withListElement(String attribute, int index, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withListElement(String attribute, int index, Object value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(attribute);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withListElement(int parameterIndex, int index, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withListElement(int parameterIndex, int index, Object value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(parameterIndex);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withEntry(String attribute, Object key, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withEntry(String attribute, Object key, Object value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(attribute);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withEntry(int parameterIndex, Object key, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public BuilderType withEntry(int parameterIndex, Object key, Object value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(parameterIndex);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> X withElement(CollectionAttribute<T, E> attribute, E value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> BuilderType withElement(").append(entity.builderImportType(Constants.COLLECTION_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute, E value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> X withElement(SetAttribute<T, E> attribute, E value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> BuilderType withElement(").append(entity.builderImportType(Constants.SET_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute, E value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> X withElement(ListAttribute<T, E> attribute, E value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> BuilderType withElement(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute, E value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> X withListElement(ListAttribute<T, E> attribute, int index, E value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> BuilderType withListElement(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attribute, int index, E value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(attribute);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <K, V> X withEntry(MapAttribute<T, K, V> attribute, K key, V value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <K, V> BuilderType withEntry(").append(entity.builderImportType(Constants.MAP_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", K, V> attribute, K key, V value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(attribute);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return (BuilderType) this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendWithBuilderMethods(StringBuilder sb, MetaEntityView entity, Context context) {
        Collection<MetaAttribute> members = entity.getMembers();

        // <E> EntityViewNestedBuilder<E, X> withSingularBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withSingularBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaSingleAttribute && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withCollectionBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withCollectionBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withListBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withListBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withListBuilder(String attribute, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withListBuilder(String attribute, int index) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && ((AnnotationMetaCollection) metaMember).isIndexedList() && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(index);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withSetBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withSetBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <V> EntityViewNestedBuilder<V, X> withMapBuilder(String attribute, Object key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType> withMapBuilder(String attribute, Object key) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaMap && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(key);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withMapBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <K, V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<K, ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType>> withMapBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaMap && ((AnnotationMetaMap) metaMember).isKeySubview() && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<K, ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType>>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withBuilder(SingularAttribute<T, E> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withBuilder(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withSingularBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withSingularBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withBuilder(CollectionAttribute<T, E> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withBuilder(").append(entity.builderImportType(Constants.COLLECTION_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withCollectionBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withCollectionBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withBuilder(ListAttribute<T, E> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withBuilder(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withBuilder(ListAttribute<T, E> attribute, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withBuilder(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attr, int index) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName(), index);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex(), index);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withBuilder(SetAttribute<T, E> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, BuilderType> withBuilder(").append(entity.builderImportType(Constants.SET_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", E> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withSetBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withSetBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <K, V> EntityViewNestedBuilder<V, X> withBuilder(MapAttribute<T, K, V> attribute, K key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <K, V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType> withBuilder(").append(entity.builderImportType(Constants.MAP_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", K, V> attr, K key) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName(), key);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex(), key);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withBuilder(MapAttribute<T, K, V> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <K, V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<K, ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, BuilderType>> withBuilder(");
        sb.append(entity.builderImportType(Constants.MAP_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getBaseSuperclass())).append(", K, V> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendWithBuilderMethods(StringBuilder sb, MetaConstructor constructor, Context context, String builderType) {
        MetaEntityView entity = constructor.getHostingEntity();
        List<MetaAttribute> members = constructor.getParameters();

        // <E> EntityViewNestedBuilder<E, X> withSingularBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append("> withSingularBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaSingleAttribute && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withCollectionBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append("> withCollectionBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withListBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append("> withListBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withListBuilder(int parameterIndex, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append("> withListBuilder(int parameterIndex, int index) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && ((AnnotationMetaCollection) metaMember).isIndexedList() && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(index);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <E> EntityViewNestedBuilder<E, X> withSetBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <E> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append("> withSetBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<E, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <V> EntityViewNestedBuilder<V, X> withMapBuilder(int parameterIndex, Object key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, ").append(builderType).append("> withMapBuilder(int parameterIndex, Object key) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaMap && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, ").append(builderType).append(">) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(key);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withMapBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <K, V> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<K, ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, ").append(builderType).append(">> withMapBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaMap && ((AnnotationMetaMap) metaMember).isKeySubview() && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<K, ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<V, ").append(builderType).append(">>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void printClassDeclaration(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("public abstract class ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX);

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        sb.append("<");
        if (!typeArguments.isEmpty()) {
            printTypeVariable(sb, entity, typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                printTypeVariable(sb, entity, typeArguments.get(i));
            }
            sb.append(", ");
        }
        sb.append("BuilderType extends EntityViewBuilderBase<");
        sb.append(entity.builderImportType(entity.getBaseSuperclass()));
        sb.append(", BuilderType>>");

        if (entity.getTypeElement().getKind() == ElementKind.INTERFACE) {
            sb.append(" implements ");
        } else {
            sb.append(" extends ");
        }
        sb.append(entity.builderImportType(entity.getBaseSuperclass()));

        if (!typeArguments.isEmpty()) {
            sb.append("<");
            sb.append(typeArguments.get(0));
            for (int i = 1; i < typeArguments.size(); i++) {
                sb.append(", ");
                sb.append(typeArguments.get(i));
            }
            sb.append(">");
        }

        if (entity.getTypeElement().getKind() == ElementKind.INTERFACE) {
            sb.append(", ");
        } else {
            sb.append(" implements ");
        }
        sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_BASE));
        sb.append("<");
        sb.append(entity.builderImportType(entity.getBaseSuperclass()));
        sb.append(", BuilderType>");

        sb.append(" {");
        sb.append(NEW_LINE);
    }

    private static void printTypeVariable(StringBuilder sb, MetaEntityView entity, TypeVariable t) {
        sb.append(t);
        if (t.getLowerBound().getKind() == TypeKind.NULL) {
            sb.append(" extends ").append(entity.builderImportType(t.getUpperBound().toString()));
        } else {
            sb.append(" super ").append(entity.builderImportType(t.getLowerBound().toString()));
        }
    }

    private static void printConstructors(StringBuilder sb, MetaEntityView entity, Context context) {
        sb.append("    public ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX).append("(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters) {");
        sb.append(NEW_LINE);
        MetaConstructor constructor = entity.getConstructor("init");
        if (constructor == null) {
            if (entity.hasEmptyConstructor()) {
                for (MetaConstructor entityConstructor : entity.getConstructors()) {
                    if (entityConstructor.getParameters().isEmpty()) {
                        constructor = entityConstructor;
                        break;
                    }
                }
            } else {
                constructor = entity.getConstructors().iterator().next();
            }
        }

        sb.append("        super(");
        if (constructor.getParameters().isEmpty()) {
            sb.append(");").append(NEW_LINE);
        } else {
            sb.append(NEW_LINE);
            Iterator<MetaAttribute> iterator = constructor.getParameters().iterator();
            MetaAttribute member = iterator.next();
            // TODO: use optional parameters?
            sb.append("            ").append(member.getDefaultValue());

            for (; iterator.hasNext(); ) {
                member = iterator.next();
                sb.append(",").append(NEW_LINE).append("            ").append(member.getDefaultValue());
            }

            sb.append(NEW_LINE).append("        );").append(NEW_LINE);
        }

        for (MetaAttribute member : entity.getMembers()) {
            member.appendImplementationAttributeConstructorAssignmentDefaultString(sb);
            sb.append(NEW_LINE);
        }

        sb.append("        this.optionalParameters = optionalParameters;").append(NEW_LINE);
        sb.append("    }");
        sb.append(NEW_LINE);
    }
}
