/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.view.processor.annotation.AnnotationMetaSingularAttribute;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;
import java.util.Collection;
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
        ClassWriterUtils.writeFile(sb, entity.getPackageName(), entity.getSimpleName() + BUILDER_CLASS_NAME_SUFFIX, entity.getBuilderImportContext(), context, entity.getOriginatingElements());
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

        sb.append("@").append(entity.builderImportType(Constants.STATIC_BUILDER)).append("(").append(entity.builderImportType(entity.getQualifiedName())).append(".class)");
        sb.append(NEW_LINE);
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
        String builderTypeVariable = entity.getSafeTypeVariable("BuilderType");

        for (MetaAttribute metaMember : members) {
            appendMember(sb, entity, metaMember, builderTypeVariable);
        }

        appendUtilityMethods(sb, entity, context);
        appendGetMethods(sb, entity, context);
        appendWithMethods(sb, entity, builderTypeVariable, context);
        appendWithBuilderMethods(sb, entity, builderTypeVariable, context);

        sb.append(NEW_LINE);

        List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) entity.getTypeElement().asType()).getTypeArguments();
        String elementType = entity.getSafeTypeVariable("ElementType");
        StringBuilder tempSb = new StringBuilder();
        for (MetaConstructor constructor : entity.getConstructors()) {
            String className = Character.toUpperCase(constructor.getName().charAt(0)) + constructor.getName().substring(1);
            sb.append(NEW_LINE);
            sb.append("    public static class ").append(className);
            tempSb.setLength(0);
            tempSb.append(className);

            if (!typeArguments.isEmpty()) {
                sb.append("<");
                tempSb.append("<");
                printTypeVariable(sb, entity, typeArguments.get(0));
                tempSb.append(typeArguments.get(0));
                for (int i = 1; i < typeArguments.size(); i++) {
                    sb.append(", ");
                    printTypeVariable(sb, entity, typeArguments.get(i));
                    tempSb.append(", ");
                    tempSb.append(typeArguments.get(i));
                }
                sb.append(">");
                tempSb.append(">");
            }
            String builderType = tempSb.toString();

            sb.append(" extends ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX);
            sb.append("<");
            for (int i = 0; i < typeArguments.size(); i++) {
                sb.append(typeArguments.get(i));
                sb.append(", ");
            }
            sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(">");
            sb.append("> implements ");

            sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER));
            sb.append("<");
            sb.append(entity.builderImportType(entity.getQualifiedName()));
            sb.append(">");

            sb.append(" {").append(NEW_LINE);

            for (MetaAttribute metaMember : constructor.getParameters()) {
                metaMember.appendBuilderAttributeDeclarationString(sb);
                sb.append(NEW_LINE);
            }

            sb.append(NEW_LINE);
            sb.append("        public ").append(className).append("(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters) {").append(NEW_LINE);
            sb.append("            super(optionalParameters);").append(NEW_LINE);
            for (MetaAttribute metaMember : constructor.getParameters()) {
                if (metaMember.getKind() == MappingKind.PARAMETER) {
                    if (metaMember.isPrimitive()) {
                        sb.append("!optionalParameters.containsKey(\"").append(metaMember.getMapping()).append("\") ? ");
                        metaMember.appendDefaultValue(sb, false, true, entity.getBuilderImportContext());
                        sb.append(" : ");
                    }
                    sb.append("            this.").append(metaMember.getPropertyName()).append(" = (").append(entity.builderImportType(metaMember.getBuilderImplementationTypeString())).append(") optionalParameters.get(\"").append(metaMember.getMapping()).append("\");").append(NEW_LINE);
                }
            }
            sb.append("        }").append(NEW_LINE);

            // T build()
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public ").append(entity.builderImportType(entity.getQualifiedName())).append(" build() {").append(NEW_LINE);
            sb.append("            return new ").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append("(");
            if (members.isEmpty() && constructor.getParameters().isEmpty()) {
                sb.append("(").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append(") null, optionalParameters);").append(NEW_LINE);
            } else {
                sb.append(NEW_LINE);
                appendConstructorArguments(sb, entity, constructor);
                sb.append(NEW_LINE).append("            );").append(NEW_LINE);
            }
            sb.append("        }").append(NEW_LINE);

            // X with(int parameterIndex, Object value)
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public ");

            sb.append(builderType);

            sb.append(" with(int parameterIndex, Object value) {").append(NEW_LINE);
            sb.append("            switch (parameterIndex) {").append(NEW_LINE);
            List<MetaAttribute> parameters = constructor.getParameters();
            boolean first = true;
            for (int i = 0; i < parameters.size(); i++) {
                first = false;
                MetaAttribute metaMember = parameters.get(i);
                sb.append("                case ").append(i).append(":").append(NEW_LINE);
                sb.append("                    this.").append(metaMember.getPropertyName()).append(" = ");
                sb.append("value == null ? ");
                metaMember.appendDefaultValue(sb, true, true, entity.getBuilderImportContext());
                sb.append(" : ");
                sb.append("(").append(metaMember.getBuilderImplementationTypeString()).append(") value;").append(NEW_LINE);
                sb.append("                    break;").append(NEW_LINE);
            }
            sb.append("                default:").append(NEW_LINE);
            sb.append("                     throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);
            if (!first) {
                sb.append("        return this;").append(NEW_LINE);
            }
            sb.append("    }").append(NEW_LINE);

            // <ElementType> ElementType get(int parameterIndex)
            sb.append(NEW_LINE);
            sb.append("        @Override").append(NEW_LINE);
            sb.append("        public <").append(elementType).append("> ").append(elementType).append(" get(int parameterIndex) {").append(NEW_LINE);
            sb.append("            switch (parameterIndex) {").append(NEW_LINE);
            for (int i = 0; i < parameters.size(); i++) {
                MetaAttribute metaMember = parameters.get(i);
                sb.append("                case ").append(i).append(":").append(NEW_LINE);
                sb.append("                    return (").append(elementType).append(") (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
            }
            sb.append("            }").append(NEW_LINE);
            sb.append("            throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
            sb.append("        }").append(NEW_LINE);

            for (MetaAttribute metaMember : entity.getMembers()) {
                appendMemberWith(sb, entity, metaMember, builderType);
            }
            appendWithMethods(sb, entity, builderType, context);
            appendWithBuilderMethods(sb, entity, builderType, context);
            appendWithBuilderMethods(sb, constructor, context, builderType);

            for (MetaAttribute metaMember : constructor.getParameters()) {
                appendMember(sb, entity, metaMember, builderType);
            }

            sb.append("    }").append(NEW_LINE);
        }

        MetaConstructor constructor = null;
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

        String builderResult = entity.getSafeTypeVariable("BuilderResult");
        sb.append(NEW_LINE);
        sb.append("    public static class Nested<");
        tempSb.setLength(0);
        tempSb.append("Nested<");
        for (int i = 0; i < typeArguments.size(); i++) {
            printTypeVariable(sb, entity, typeArguments.get(i));
            sb.append(", ");
            tempSb.append(typeArguments.get(i));
            tempSb.append(", ");
        }
        sb.append(builderResult).append(">");
        tempSb.append(builderResult).append(">");
        String builderType = tempSb.toString();
        sb.append(" extends ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX);
        sb.append("<");
        for (int i = 0; i < typeArguments.size(); i++) {
            sb.append(typeArguments.get(i));
            sb.append(", ");
        }
        sb.append(builderType);
        sb.append("> implements ");

        sb.append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER));
        sb.append("<");
        sb.append(entity.builderImportType(entity.getQualifiedName()));
        sb.append(", ").append(builderResult).append(", ");
        sb.append(builderType);
        sb.append("> {").append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append("        private final ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LISTENER)).append(" listener;").append(NEW_LINE);
        sb.append("        private final ").append(builderResult).append(" result;").append(NEW_LINE);

        for (MetaAttribute metaMember : constructor.getParameters()) {
            metaMember.appendBuilderAttributeDeclarationString(sb);
            sb.append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("        public Nested(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters, ").append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_LISTENER)).append(" listener, ").append(builderResult).append(" result) {").append(NEW_LINE);
        sb.append("            super(optionalParameters);").append(NEW_LINE);
        for (MetaAttribute metaMember : constructor.getParameters()) {
            if (metaMember.getKind() == MappingKind.PARAMETER) {
                if (metaMember.isPrimitive()) {
                    sb.append("!optionalParameters.containsKey(\"").append(metaMember.getMapping()).append("\") ? ");
                    metaMember.appendDefaultValue(sb, false, true, entity.getBuilderImportContext());
                    sb.append(" : ");
                }
                sb.append("            this.").append(metaMember.getPropertyName()).append(" = (").append(entity.builderImportType(metaMember.getBuilderImplementationTypeString())).append(") optionalParameters.get(\"").append(metaMember.getMapping()).append("\");").append(NEW_LINE);
            }
        }
        sb.append("            this.listener = listener;").append(NEW_LINE);
        sb.append("            this.result = result;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);

        // X build()
        sb.append(NEW_LINE);
        sb.append("        @Override").append(NEW_LINE);
        sb.append("        public ").append(builderResult).append(" build() {").append(NEW_LINE);
        sb.append("            listener.onBuildComplete(new ").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append("(");
        if (members.isEmpty() && constructor.getParameters().isEmpty()) {
            sb.append("(").append(entity.builderImportType(TypeUtils.getDerivedTypeName(entity.getTypeElement()) + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX)).append(") null, optionalParameters));").append(NEW_LINE);
        } else {
            sb.append(NEW_LINE);
            appendConstructorArguments(sb, entity, constructor);
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
            sb.append("value == null ? ");
            metaMember.appendDefaultValue(sb, true, true, entity.getBuilderImportContext());
            sb.append(" : ");
            sb.append("(").append(metaMember.getBuilderImplementationTypeString()).append(") value;").append(NEW_LINE);
            sb.append("                break;").append(NEW_LINE);
        }
        sb.append("            default:").append(NEW_LINE);
        sb.append("                throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        if (!first) {
            sb.append("        return this;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        // <ElementType> ElementType get(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("        @Override").append(NEW_LINE);
        sb.append("        public <").append(elementType).append("> ").append(elementType).append(" get(int parameterIndex) {").append(NEW_LINE);
        sb.append("            switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < parameters.size(); i++) {
            MetaAttribute metaMember = parameters.get(i);
            sb.append("                case ").append(i).append(":").append(NEW_LINE);
            sb.append("                    return (").append(elementType).append(") (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
        }
        sb.append("            }").append(NEW_LINE);
        sb.append("            throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);

        for (MetaAttribute metaMember : entity.getMembers()) {
            appendMemberWith(sb, entity, metaMember, builderType);
        }
        appendWithMethods(sb, entity, builderType, context);
        appendWithBuilderMethods(sb, entity, builderType, context);
        appendWithBuilderMethods(sb, constructor, context, builderType);

        for (MetaAttribute metaMember : constructor.getParameters()) {
            appendMember(sb, entity, metaMember, builderType);
        }

        sb.append("    }").append(NEW_LINE);

        sb.append("}");
        sb.append(NEW_LINE);
    }

    private static void appendConstructorArguments(StringBuilder sb, MetaEntityView entity, MetaConstructor constructor) {
        boolean first = true;
        MetaAttribute idMember = entity.getIdMember();
        if (idMember != null) {
            sb.append("            this.").append(idMember.getPropertyName());
            first = false;
        }

        for (MetaAttribute member : entity.getMembers()) {
            if (first) {
                first = false;
            } else if (member != idMember) {
                sb.append(",").append(NEW_LINE);
            }
            if (member != idMember) {
                if (member.getConvertedModelType() == null) {
                    sb.append("            this.").append(member.getPropertyName());
                } else if (member.isSubview()) {
                    sb.append("            (").append(member.getImplementationTypeString()).append(") ")
                            .append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX).append(".")
                            .append(member.getPropertyName()).append(".attr().getType().getConverter().convertToViewType(this.").append(member.getPropertyName()).append(')');
                } else {
                    sb.append("            (").append(member.getImplementationTypeString()).append(") ")
                            .append(entity.getSimpleName()).append(MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX).append(".")
                            .append(member.getPropertyName()).append(".getType().getConverter().convertToViewType(this.").append(member.getPropertyName()).append(')');
                }
            }
        }

        for (MetaAttribute member : constructor.getParameters()) {
            if (first) {
                first = false;
            } else {
                sb.append(",").append(NEW_LINE);
            }
            sb.append("            this.").append(member.getPropertyName());
        }
    }

    private static void appendMember(StringBuilder sb, MetaEntityView entity, MetaAttribute metaMember, String builderType) {
        metaMember.appendBuilderAttributeGetterAndSetterString(sb);
        sb.append(NEW_LINE);
        appendMemberWith(sb, entity, metaMember, builderType);
    }

    private static void appendMemberWith(StringBuilder sb, MetaEntityView entity, MetaAttribute metaMember, String builderType) {
        ElementKind kind = metaMember.getElement() == null ? null : metaMember.getElement().getKind();
        sb.append("    public ").append(builderType).append(" with");
        if (kind == ElementKind.PARAMETER) {
            sb.append(PARAMETER_PREFIX);
        }
        sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
        sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
        sb.append('(');

        sb.append(metaMember.getBuilderImplementationTypeString());

        sb.append(' ')
                .append(metaMember.getPropertyName())
                .append(") {").append(NEW_LINE)
                .append("        this.")
                .append(metaMember.getPropertyName())
                .append(" = ")
                .append(metaMember.getPropertyName())
                .append(";")
                .append(NEW_LINE)
                .append("        return (").append(builderType).append(") this;")
                .append(NEW_LINE)
                .append("    }");

        if (metaMember.isSubview()) {
            String memberBuilderFqn = metaMember.getGeneratedTypePrefix() + BuilderClassWriter.BUILDER_CLASS_NAME_SUFFIX;
            List<TypeVariable> typeArguments = (List<TypeVariable>) ((DeclaredType) metaMember.getSubviewElement().asType()).getTypeArguments();
            if (metaMember instanceof AnnotationMetaMap) {
                AnnotationMetaMap mapMember = (AnnotationMetaMap) metaMember;
                String listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_MAP_LISTENER) + "(getMap(\"" + metaMember.getPropertyName() + "\"), key)";
                sb.append(NEW_LINE);
                sb.append("    public ");
                if (!typeArguments.isEmpty()) {
                    sb.append("<");
                    for (int i = 0; i < typeArguments.size(); i++) {
                        sb.append("Sub");
                        printTypeVariable(sb, entity, typeArguments.get(i));
                        sb.append(", ");
                    }
                    sb.setCharAt(sb.length() - 2, '>');
                }
                sb.append(entity.builderImportType(memberBuilderFqn)).append(".Nested<");
                for (int i = 0; i < typeArguments.size(); i++) {
                    sb.append("Sub");
                    sb.append(typeArguments.get(i));
                    sb.append(", ");
                }
                sb.append("? extends ").append(builderType).append("> with");
                if (kind == ElementKind.PARAMETER) {
                    sb.append(PARAMETER_PREFIX);
                }
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                sb.append("Builder(Object key) {")
                        .append(NEW_LINE)
                        .append("        return new ").append(entity.builderImportType(memberBuilderFqn))
                        .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this);")
                        .append(NEW_LINE)
                        .append("    }");

                sb.append(NEW_LINE);

                if (mapMember.isKeySubview()) {
                    String keyMemberBuilderFqn = mapMember.getGeneratedKeyTypePrefix() + BuilderClassWriter.BUILDER_CLASS_NAME_SUFFIX;
                    List<TypeVariable> keyTypeArguments = (List<TypeVariable>) ((DeclaredType) mapMember.getKeySubviewElement().asType()).getTypeArguments();
                    sb.append(NEW_LINE);
                    sb.append("    public ");
                    if (!keyTypeArguments.isEmpty() || !typeArguments.isEmpty()) {
                        sb.append("<");
                        for (int i = 0; i < keyTypeArguments.size(); i++) {
                            printTypeVariable(sb, entity, keyTypeArguments.get(i));
                            sb.append(", ");
                        }
                        for (int i = 0; i < typeArguments.size(); i++) {
                            printTypeVariable(sb, entity, typeArguments.get(i));
                            sb.append(", ");
                        }
                        sb.setCharAt(sb.length() - 2, '>');
                    }

                    sb.append(entity.builderImportType(keyMemberBuilderFqn)).append(".Nested<");
                    for (int i = 0; i < keyTypeArguments.size(); i++) {
                        sb.append(keyTypeArguments.get(i));
                        sb.append(", ");
                    }
                    sb.append("? extends ").append(entity.builderImportType(memberBuilderFqn)).append(".Nested<");
                    for (int i = 0; i < typeArguments.size(); i++) {
                        sb.append(typeArguments.get(i));
                        sb.append(", ");
                    }
                    sb.append("? extends ").append(builderType).append(">> with");
                    if (kind == ElementKind.PARAMETER) {
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
                        sb.append("    public ");
                        if (!typeArguments.isEmpty()) {
                            sb.append("<");
                            for (int i = 0; i < typeArguments.size(); i++) {
                                sb.append("Sub");
                                printTypeVariable(sb, entity, typeArguments.get(i));
                                sb.append(", ");
                            }
                            sb.setCharAt(sb.length() - 2, '>');
                        }
                        sb.append(entity.builderImportType(memberBuilderFqn)).append(".Nested<");
                        for (int i = 0; i < typeArguments.size(); i++) {
                            sb.append("Sub");
                            sb.append(typeArguments.get(i));
                            sb.append(", ");
                        }
                        sb.append("? extends ").append(builderType).append("> with");
                        if (kind == ElementKind.PARAMETER) {
                            sb.append(PARAMETER_PREFIX);
                        }
                        sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                        sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                        sb.append("Builder(int index) {")
                                .append(NEW_LINE)
                                .append("        return new ").append(entity.builderImportType(memberBuilderFqn))
                                .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this);")
                                .append(NEW_LINE)
                                .append("    }");

                        sb.append(NEW_LINE);
                    }
                    listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_COLLECTION_LISTENER) + "(getCollection(\"" + metaMember.getPropertyName() + "\"))";
                } else {
                    listener = "new " + entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_SINGULAR_NAME_LISTENER) + "(this, \"" + metaMember.getPropertyName() + "\")";
                }
                sb.append(NEW_LINE);
                sb.append("    public ");
                if (!typeArguments.isEmpty()) {
                    sb.append("<");
                    for (int i = 0; i < typeArguments.size(); i++) {
                        sb.append("Sub");
                        printTypeVariable(sb, entity, typeArguments.get(i));
                        sb.append(", ");
                    }
                    sb.setCharAt(sb.length() - 2, '>');
                }
                sb.append(entity.builderImportType(memberBuilderFqn)).append(".Nested<");
                for (int i = 0; i < typeArguments.size(); i++) {
                    sb.append("Sub");
                    sb.append(typeArguments.get(i));
                    sb.append(", ");
                }
                sb.append("? extends ").append(builderType).append("> with");
                if (kind == ElementKind.PARAMETER) {
                    sb.append(PARAMETER_PREFIX);
                }
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length());
                sb.append("Builder() {")
                        .append(NEW_LINE)
                        .append("    return new ").append(entity.builderImportType(memberBuilderFqn))
                        .append(".Nested<>(optionalParameters, ").append(listener).append(", (").append(builderType).append(") this);")
                        .append(NEW_LINE)
                        .append("    }");

                sb.append(NEW_LINE);
            }
        }
        sb.append(NEW_LINE);
    }

    private static void appendUtilityMethods(StringBuilder sb, MetaEntityView entity, Context context) {
        String collectionType = entity.getSafeTypeVariable("CollectionType");
        String elementType = entity.getSafeTypeVariable("ElementType");
        String keyType = entity.getSafeTypeVariable("KeyType");
        sb.append(NEW_LINE);
        sb.append("    protected <").append(elementType).append("> ").append(elementType).append(" get(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return get(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return get(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> ").append(collectionType).append(" getCollection(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return getCollection(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return getCollection(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> ").append(collectionType).append(" getMap(").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return getMap(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return getMap(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> ").append(collectionType).append(" getCollection(String attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> ").append(collectionType).append(" getMap(String attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.COLLECTION)).append("<Object>> ").append(collectionType).append(" getCollection(int attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") currentValue;").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        sb.append(NEW_LINE);
        sb.append("    protected <").append(collectionType).append(" extends ").append(entity.builderImportType(Constants.MAP)).append("<Object, Object>> ").append(collectionType).append(" getMap(int attr) {").append(NEW_LINE);
        sb.append("        Object currentValue = get(attr);").append(NEW_LINE);
        sb.append("        if (currentValue == null) {").append(NEW_LINE);
        sb.append("            with(attr, null);").append(NEW_LINE);
        sb.append("            currentValue = get(attr);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("        if (currentValue instanceof ").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") ((").append(entity.builderImportType(Constants.RECORDING_CONTAINER)).append("<?>) currentValue).getDelegate();").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return (").append(collectionType).append(") currentValue;").append(NEW_LINE);
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

    private static void appendGetMethods(StringBuilder sb, MetaEntityView entity, Context context) {
        Collection<MetaAttribute> members = entity.getMembers();
        String collectionType = entity.getSafeTypeVariable("CollectionType");
        String elementType = entity.getSafeTypeVariable("ElementType");
        String keyType = entity.getSafeTypeVariable("KeyType");

        // <ElementType> ElementType get(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(elementType).append(" get(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
            sb.append("                return (").append(elementType).append(") (Object) this.").append(metaMember.getPropertyName()).append(";").append(NEW_LINE);
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> ElementType get(SingularAttribute<T, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(elementType).append(" get(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute) {").append(NEW_LINE);
        sb.append("        return get((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <CollectionType> CollectionType get(PluralAttribute<T, CollectionType, ?> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(collectionType).append("> ").append(collectionType).append(" get(").append(entity.builderImportType(Constants.PLURAL_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(collectionType).append(", ?> attribute) {").append(NEW_LINE);
        sb.append("        return get((").append(entity.builderImportType(Constants.ATTRIBUTE)).append("<?, ?>) attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendWithMethods(StringBuilder sb, MetaEntityView entity, String builderType, Context context) {
        String builderTypeCast = "(" + builderType + ") ";
        Collection<MetaAttribute> members = entity.getMembers();
        String collectionType = entity.getSafeTypeVariable("CollectionType");
        String elementType = entity.getSafeTypeVariable("ElementType");
        String keyType = entity.getSafeTypeVariable("KeyType");

        // X with(String attribute, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" with(String attribute, Object value) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
            sb.append("                this.").append(metaMember.getPropertyName()).append(" = ");
            sb.append("value == null ? ");
            metaMember.appendDefaultValue(sb, true, true, entity.getBuilderImportContext());
            sb.append(" : ");
            sb.append("(").append(metaMember.getBuilderImplementationTypeString()).append(") value;").append(NEW_LINE);
            sb.append("                break;").append(NEW_LINE);
        }
        sb.append("            default:").append(NEW_LINE);
        sb.append("                throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        if (!members.isEmpty()) {
            sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        }
        sb.append("    }").append(NEW_LINE);

        // <ElementType> X with(SingularAttribute<T, ElementType> attribute, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(builderType).append(" with(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        if (attribute instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attribute).getName(), value);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attribute).getIndex(), value);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <CollectionType> X with(PluralAttribute<T, CollectionType, ?> attribute, CollectionType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(collectionType).append("> ").append(builderType).append(" with(").append(entity.builderImportType(Constants.PLURAL_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(collectionType).append(", ?> attribute, ").append(collectionType).append(" value) {").append(NEW_LINE);
        sb.append("        if (attribute instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attribute).getName(), value);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return with(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attribute).getIndex(), value);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);


        // X withElement(String attribute, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withElement(String attribute, Object value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withElement(int parameterIndex, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withElement(int parameterIndex, Object value) {").append(NEW_LINE);
        sb.append("        getCollection(parameterIndex).add(value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withListElement(String attribute, int index, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withListElement(String attribute, int index, Object value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(attribute);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withListElement(int parameterIndex, int index, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withListElement(int parameterIndex, int index, Object value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(parameterIndex);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withEntry(String attribute, Object key, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withEntry(String attribute, Object key, Object value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(attribute);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // X withEntry(int parameterIndex, Object key, Object value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public ").append(builderType).append(" withEntry(int parameterIndex, Object key, Object value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(parameterIndex);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> X withElement(CollectionAttribute<T, ElementType> attribute, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(builderType).append(" withElement(").append(entity.builderImportType(Constants.COLLECTION_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> X withElement(SetAttribute<T, ElementType> attribute, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(builderType).append(" withElement(").append(entity.builderImportType(Constants.SET_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> X withElement(ListAttribute<T, ElementType> attribute, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(builderType).append(" withElement(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        getCollection(attribute).add(value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> X withListElement(ListAttribute<T, ElementType> attribute, int index, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(builderType).append(" withListElement(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attribute, int index, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        List<Object> list = getCollection(attribute);").append(NEW_LINE);
        sb.append("        addListValue(list, index, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <KeyType, ElementType> X withEntry(MapAttribute<T, KeyType, ElementType> attribute, KeyType key, ElementType value)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(keyType).append(", ").append(elementType).append("> ").append(builderType).append(" withEntry(")
                .append(entity.builderImportType(Constants.MAP_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(keyType).append(", ").append(elementType).append("> attribute, ").append(keyType).append(" key, ").append(elementType).append(" value) {").append(NEW_LINE);
        sb.append("        Map<Object, Object> map = getMap(attribute);").append(NEW_LINE);
        sb.append("        map.put(key, value);").append(NEW_LINE);
        sb.append("        return ").append(builderTypeCast).append("this;").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendWithBuilderMethods(StringBuilder sb, MetaEntityView entity, String builderType, Context context) {
        Collection<MetaAttribute> members = entity.getMembers();
        String collectionType = entity.getSafeTypeVariable("CollectionType");
        String elementType = entity.getSafeTypeVariable("ElementType");
        String keyType = entity.getSafeTypeVariable("KeyType");

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withSingularBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withSingularBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaSingularAttribute && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withCollectionBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withCollectionBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withListBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withListBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withListBuilder(String attribute, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withListBuilder(String attribute, int index) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && ((AnnotationMetaCollection) metaMember).isIndexedList() && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(index);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withSetBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withSetBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withMapBuilder(String attribute, Object key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withMapBuilder(String attribute, Object key) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaMap && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(key);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends X, ?>, ?> withMapBuilder(String attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(keyType).append(", ").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER))
                .append("<").append(keyType).append(", ? extends ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>, ?> withMapBuilder(String attribute) {").append(NEW_LINE);
        sb.append("        switch (attribute) {").append(NEW_LINE);
        for (MetaAttribute metaMember : members) {
            if (metaMember instanceof AnnotationMetaMap && ((AnnotationMetaMap) metaMember).isKeySubview() && metaMember.isSubview()) {
                sb.append("            case \"").append(metaMember.getPropertyName()).append("\":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(keyType).append(", ? extends ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER))
                        .append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>, ?>) (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<?, ?, ?>) with");
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown attribute: \" + attribute);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(SingularAttribute<T, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.SINGULAR_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withSingularBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withSingularBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(CollectionAttribute<T, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.COLLECTION_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withCollectionBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withCollectionBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(ListAttribute<T, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(ListAttribute<T, ElementType> attribute, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.LIST_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attr, int index) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName(), index);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withListBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex(), index);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(SetAttribute<T, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.SET_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(elementType).append("> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withSetBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withSetBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withBuilder(MapAttribute<T, KeyType, ElementType> attribute, KeyType key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(keyType).append(", ").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withBuilder(").append(entity.builderImportType(Constants.MAP_ATTRIBUTE));
        sb.append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(keyType).append(", ").append(elementType).append("> attr, ").append(keyType).append(" key) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName(), key);").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex(), key);").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends X, ?>, ?> withBuilder(MapAttribute<T, KeyType, ElementType> attribute)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(keyType).append(", ").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(keyType).append(", ? extends ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>, ?> withBuilder(");
        sb.append(entity.builderImportType(Constants.MAP_ATTRIBUTE)).append("<").append(entity.builderImportType(entity.getQualifiedName())).append(", ").append(keyType).append(", ").append(elementType).append("> attr) {").append(NEW_LINE);
        sb.append("        if (attr instanceof ").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.METHOD_ATTRIBUTE)).append(") attr).getName());").append(NEW_LINE);
        sb.append("        } else {").append(NEW_LINE);
        sb.append("            return withMapBuilder(((").append(entity.builderImportType(Constants.PARAMETER_ATTRIBUTE)).append(") attr).getIndex());").append(NEW_LINE);
        sb.append("        }").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);
    }

    private static void appendWithBuilderMethods(StringBuilder sb, MetaConstructor constructor, Context context, String builderType) {
        String collectionType = constructor.getHostingEntity().getSafeTypeVariable("CollectionType");
        String elementType = constructor.getHostingEntity().getSafeTypeVariable("ElementType");
        String keyType = constructor.getHostingEntity().getSafeTypeVariable("KeyType");
        MetaEntityView entity = constructor.getHostingEntity();
        List<MetaAttribute> members = constructor.getParameters();

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withSingularBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withSingularBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaSingularAttribute && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withCollectionBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withCollectionBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withListBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withListBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withListBuilder(int parameterIndex, int index)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withListBuilder(int parameterIndex, int index) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && ((AnnotationMetaCollection) metaMember).isIndexedList() && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(index);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withSetBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withSetBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaCollection && !(metaMember instanceof AnnotationMetaMap) && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder();").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <ElementType> EntityViewNestedBuilder<ElementType, ? extends X, ?> withMapBuilder(int parameterIndex, Object key)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?> withMapBuilder(int parameterIndex, Object key) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaMap && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>) with").append(PARAMETER_PREFIX);
                sb.append(Character.toUpperCase(metaMember.getPropertyName().charAt(0)));
                sb.append(metaMember.getPropertyName(), 1, metaMember.getPropertyName().length()).append("Builder(key);").append(NEW_LINE);
            }
        }
        sb.append("        }").append(NEW_LINE);
        sb.append("        throw new IllegalArgumentException(\"Unknown parameter index: \" + parameterIndex);").append(NEW_LINE);
        sb.append("    }").append(NEW_LINE);

        // <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends X, ?>, ?> withMapBuilder(int parameterIndex)
        sb.append(NEW_LINE);
        sb.append("    @Override").append(NEW_LINE);
        sb.append("    public <").append(keyType).append(", ").append(elementType).append("> ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(keyType).append(", ? extends ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER))
                .append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>, ?> withMapBuilder(int parameterIndex) {").append(NEW_LINE);
        sb.append("        switch (parameterIndex) {").append(NEW_LINE);
        for (int i = 0; i < members.size(); i++) {
            MetaAttribute metaMember = members.get(i);
            if (metaMember instanceof AnnotationMetaMap && ((AnnotationMetaMap) metaMember).isKeySubview() && metaMember.isSubview()) {
                sb.append("            case ").append(i).append(":").append(NEW_LINE);
                sb.append("                return (").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(keyType).append(", ? extends ").append(entity.builderImportType(Constants.ENTITY_VIEW_NESTED_BUILDER)).append("<").append(elementType).append(", ? extends ").append(builderType).append(", ?>, ?>) with");
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
        String builderType = entity.getSafeTypeVariable("BuilderType");

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
        sb.append(builderType).append(" extends EntityViewBuilderBase<");
        sb.append(entity.builderImportType(entity.getQualifiedName()));
        sb.append(", ").append(builderType).append(">> implements ");
        sb.append(entity.builderImportType(Constants.ENTITY_VIEW_BUILDER_BASE));
        sb.append("<");
        sb.append(entity.builderImportType(entity.getQualifiedName()));
        sb.append(", ").append(builderType).append(">");

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
        sb.append("    public ").append(entity.getSimpleName()).append(BUILDER_CLASS_NAME_SUFFIX).append("(").append(entity.builderImportType(Constants.MAP)).append("<String, Object> optionalParameters) {").append(NEW_LINE);

        for (MetaAttribute member : entity.getMembers()) {
            sb.append("        this.").append(member.getPropertyName()).append(" = ");
            if (member.getKind() == MappingKind.PARAMETER) {
                if (member.isPrimitive()) {
                    sb.append("!optionalParameters.containsKey(\"").append(member.getMapping()).append("\") ? ");
                    member.appendDefaultValue(sb, false, true, entity.getBuilderImportContext());
                    sb.append(" : ");
                }
                sb.append("(").append(entity.builderImportType(member.getImplementationTypeString())).append(") optionalParameters.get(\"").append(member.getMapping()).append("\")").append(NEW_LINE);
            } else {
                member.appendDefaultValue(sb, true, true, entity.getBuilderImportContext());
            }
            sb.append(";");
            sb.append(NEW_LINE);
        }

        sb.append("        this.optionalParameters = optionalParameters;").append(NEW_LINE);
        sb.append("    }");
        sb.append(NEW_LINE);
    }
}
