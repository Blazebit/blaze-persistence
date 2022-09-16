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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.AttributeFilter;
import com.blazebit.persistence.view.processor.BuilderClassWriter;
import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.EntityViewTypeUtils;
import com.blazebit.persistence.view.processor.ImplementationClassWriter;
import com.blazebit.persistence.view.processor.ImportContext;
import com.blazebit.persistence.view.processor.MappingKind;
import com.blazebit.persistence.view.processor.MetaAttribute;
import com.blazebit.persistence.view.processor.MetaEntityView;
import com.blazebit.persistence.view.processor.MetamodelClassWriter;
import com.blazebit.persistence.view.processor.MultiRelationClassWriter;
import com.blazebit.persistence.view.processor.OptionalParameterUtils;
import com.blazebit.persistence.view.processor.RelationClassWriter;
import com.blazebit.persistence.view.processor.TypeUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class AnnotationMetaAttribute implements MetaAttribute {

    private static final String NEW_LINE = System.lineSeparator();

    private final AnnotationMetaEntityView parent;
    private final Element element;
    private final String modelType;
    private final String declaredJavaType;
    private final String implementationTypeString;
    private final String convertedModelType;
    private final TypeMirror typeMirror;
    private final String attributeName;
    private final MappingKind kind;
    private final String mapping;
    private final TypeElement subviewElement;
    private final EntityViewTypeUtils.SubviewInfo subviewInfo;
    private final Map<String, AttributeFilter> filters;
    private final Map<String, TypeElement> optionalParameters;
    private final boolean mutable;
    private final boolean updatable;
    private final boolean createEmptyFlatViews;
    private final String generatedTypePrefix;
    private final Element setter;
    private final boolean idMember;
    private final boolean versionMember;
    private final boolean self;
    private final boolean supportsDirtyTracking;
    private final String derivedTypeName;
    private int attributeIndex = -1;
    private int dirtyStateIndex = -1;

    protected AnnotationMetaAttribute(AnnotationMetaEntityView parent, Element element, String modelType, String declaredJavaType, String convertedModelType, Context context, boolean version) {
        this.element = element;
        this.parent = parent;
        this.modelType = modelType;
        this.declaredJavaType = declaredJavaType;
        this.implementationTypeString = getHostingEntity().importTypeExceptMetamodel(declaredJavaType);
        this.convertedModelType = convertedModelType;
        this.derivedTypeName = TypeUtils.getDerivedTypeName(parent.getTypeElement());

        String mapping = null;
        MappingKind kind = null;
        Boolean updatable = null;
        Boolean mutable = null;
        boolean self = false;
        boolean createEmptyFlatViews = context.isCreateEmptyFlatViews();
        Map<String, AttributeFilter> filters;
        Map<String, TypeElement> optionalParameters;
        if (version) {
            mapping = parent.getEntityVersionAttribute().getSimpleName().toString();
            if (parent.getEntityVersionAttribute().getKind() == ElementKind.METHOD) {
                mapping = mapping.substring(3);
            }
            kind = MappingKind.MAPPING;
            updatable = false;
            mutable = false;
            filters = Collections.emptyMap();
            optionalParameters = Collections.emptyMap();
        } else {
            filters = new HashMap<>();
            optionalParameters = new HashMap<>();
            for (AnnotationMirror mirror : TypeUtils.getAnnotationMirrors(element, Constants.ID_MAPPING, Constants.MAPPING, Constants.MAPPING_CORRELATED, Constants.MAPPING_CORRELATED_SIMPLE, Constants.MAPPING_PARAMETER, Constants.MAPPING_SUBQUERY, Constants.UPDATABLE_MAPPING, Constants.SELF, Constants.EMPTY_FLAT_VIEW_CREATION, Constants.ATTRIBUTE_FILTER, Constants.ATTRIBUTE_FILTERS)) {
                switch (mirror.getAnnotationType().toString()) {
                    case Constants.ID_MAPPING:
                        mapping = TypeUtils.getAnnotationValue(mirror, "value");
                        kind = MappingKind.MAPPING;
                        updatable = false;
                        mutable = false;
                        OptionalParameterUtils.addOptionalParameters(optionalParameters, mapping, context);
                        break;
                    case Constants.MAPPING:
                        mapping = TypeUtils.getAnnotationValue(mirror, "value");
                        kind = MappingKind.MAPPING;
                        OptionalParameterUtils.addOptionalParameters(optionalParameters, mapping, context);
                        break;
                    case Constants.MAPPING_CORRELATED:
                        kind = MappingKind.CORRELATED;
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                            //CHECKSTYLE:OFF: FallThrough
                            switch (entry.getKey().getSimpleName().toString()) {
                                case "correlationBasis":
                                case "correlationResult":
                                    OptionalParameterUtils.addOptionalParameters(optionalParameters, (String) entry.getValue().getValue(), context);
                                    break;
                                default:
                                    break;
                            }
                            //CHECKSTYLE:ON: FallThrough
                        }
                        break;
                    case Constants.MAPPING_CORRELATED_SIMPLE:
                        kind = MappingKind.CORRELATED;
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                            //CHECKSTYLE:OFF: FallThrough
                            switch (entry.getKey().getSimpleName().toString()) {
                                case "correlationBasis":
                                case "correlationResult":
                                case "correlationExpression":
                                    OptionalParameterUtils.addOptionalParameters(optionalParameters, (String) entry.getValue().getValue(), context);
                                    break;
                                default:
                                    break;
                            }
                            //CHECKSTYLE:ON: FallThrough
                        }
                        break;
                    case Constants.MAPPING_PARAMETER:
                        mapping = TypeUtils.getAnnotationValue(mirror, "value");
                        kind = MappingKind.PARAMETER;
                        updatable = false;
                        mutable = false;
                        TypeElement existingTypeElement = context.getOptionalParameters().get(mapping);
                        if (existingTypeElement == null) {
                            optionalParameters.put(mapping, context.getTypeElement(modelType));
                        } else {
                            optionalParameters.put(mapping, existingTypeElement);
                        }
                        break;
                    case Constants.MAPPING_SUBQUERY:
                        kind = MappingKind.SUBQUERY;
                        updatable = false;
                        mutable = false;
                        OptionalParameterUtils.addOptionalParameters(optionalParameters, TypeUtils.getAnnotationValue(mirror, "expression"), context);
                        break;
                    case Constants.UPDATABLE_MAPPING:
                        updatable = true;
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                            switch (entry.getKey().getSimpleName().toString()) {
                                case "updatable":
                                    updatable = (boolean) entry.getValue().getValue();
                                    break;
                                case "cascade":
                                    List<AnnotationValue> annotationValues = (List<AnnotationValue>) entry.getValue().getValue();
                                    mutable = !annotationValues.isEmpty();
                                    break;
                                default:
                                    break;
                            }
                        }

                        break;
                    case Constants.SELF:
                        self = true;
                        break;
                    case Constants.EMPTY_FLAT_VIEW_CREATION:
                        createEmptyFlatViews = TypeUtils.getAnnotationValue(mirror, "value");
                        break;
                    case Constants.ATTRIBUTE_FILTER:
                        addAttributeFilter(filters, mirror, context);
                        break;
                    case Constants.ATTRIBUTE_FILTERS:
                        for (AnnotationMirror value : TypeUtils.<List<AnnotationMirror>>getAnnotationValue(mirror, "value")) {
                            addAttributeFilter(filters, value, context);
                        }
                        break;
                    default:
                        break;
                }
            }
            if (kind == null) {
                kind = MappingKind.MAPPING;
                mapping = EntityViewTypeUtils.getAttributeName(element);
            }
        }

        this.mapping = mapping;
        this.kind = kind;
        this.filters = filters;
        this.optionalParameters = optionalParameters;
        if (kind == MappingKind.PARAMETER || kind == MappingKind.SUBQUERY) {
            this.subviewElement = null;
        } else {
            this.subviewElement = getSubview(modelType, context);
        }
        if (subviewElement != null) {
            this.generatedTypePrefix = TypeUtils.getDerivedTypeName(context.getTypeElement(modelType));
            this.subviewInfo = EntityViewTypeUtils.getSubviewInfo(subviewElement, context);
        } else {
            this.generatedTypePrefix = modelType;
            this.subviewInfo = null;
        }
        if (subviewInfo != null && subviewInfo.getEntityViewIdElement() == null && subviewInfo.hasEmptyConstructor()) {
            this.createEmptyFlatViews = createEmptyFlatViews;
        } else {
            this.createEmptyFlatViews = false;
        }

        if (version) {
            attributeName = "$$_version";
            setter = null;
            typeMirror = parent.getEntityVersionAttribute().asType();
            supportsDirtyTracking = true;
            idMember = false;
            versionMember = true;
//        } else if (element.getKind() == ElementKind.FIELD) {
//            attributeName = element.getSimpleName().toString();
//            if (element.getModifiers().contains(Modifier.FINAL)) {
//                setter = null;
//            } else {
//                setter = element;
//                if (updatable == null) {
//                    updatable = true;
//                }
//            }
//            typeKind = element.asType().getKind();
//            supportsDirtyTracking = subviewElement != null || EntityViewTypeUtils.getMutability(type) != EntityViewTypeUtils.Mutability.MUTABLE;
//            idMember = TypeUtils.containsAnnotation(element, Constants.ID_MAPPING);
//            versionMember = mapping != null && (parent.getEntityVersionAttribute() != null && mapping.equals(parent.getEntityVersionAttribute().getSimpleName().toString()) || context.matchesDefaultVersionAttribute(element));
//            self = false;
        } else if (element.getKind() == ElementKind.PARAMETER) {
            attributeName = element.getSimpleName().toString();
            setter = null;
            typeMirror = element.asType();
            supportsDirtyTracking = false;
            idMember = false;
            versionMember = false;
        } else if (element.getKind() == ElementKind.METHOD) {
            String name = element.getSimpleName().toString();
            String setterName;
            if (name.startsWith("get")) {
                attributeName = EntityViewTypeUtils.firstToLower(3, name);
                setterName = "set" + name.substring(3);
            } else if (name.startsWith("is")) {
                attributeName = EntityViewTypeUtils.firstToLower(2, name);
                setterName = "set" + name.substring(2);
            } else {
                attributeName = EntityViewTypeUtils.firstToLower(0, name);
                setterName = name;
                parent.getContext().logMessage(Diagnostic.Kind.WARNING, "Invalid method name for attribute:" + element);
            }

            // TODO: Try to move setter matching to AnnotationMetaEntityView as pre-step
            Element setter = null;
            for (Element otherElement : element.getEnclosingElement().getEnclosedElements()) {
                if (setterName.equals(otherElement.getSimpleName().toString())) {
                    setter = otherElement;
                    break;
                }
            }

            List<TypeMirror> superClasses = new ArrayList<>();
            superClasses.add(parent.getTypeElement().asType());
            for (int i = 0; i < superClasses.size(); i++) {
                TypeMirror superClass = superClasses.get(i);
                final Element superClassElement = ((DeclaredType) superClass).asElement();
                for (Element enclosedElement : superClassElement.getEnclosedElements()) {
                    if (setterName.equals(enclosedElement.getSimpleName().toString())) {
                        setter = enclosedElement;
                        break;
                    }
                }

                superClass = ((TypeElement) superClassElement).getSuperclass();
                if (superClass.getKind() == TypeKind.DECLARED) {
                    superClasses.add(superClass);
                }
                superClasses.addAll(((TypeElement) superClassElement).getInterfaces());
            }
            this.setter = setter;
            if (setter != null && updatable == null) {
                updatable = true;
            }
            typeMirror = ((ExecutableElement) element).getReturnType();
            supportsDirtyTracking = subviewElement != null || EntityViewTypeUtils.getMutability(modelType) != EntityViewTypeUtils.Mutability.MUTABLE;
            idMember = TypeUtils.containsAnnotation(element, Constants.ID_MAPPING);
            versionMember = mapping != null && (parent.getEntityVersionAttribute() != null && mapping.equals(parent.getEntityVersionAttribute().getSimpleName().toString()) || context.matchesDefaultVersionAttribute(element));
            self = false;
        } else {
            attributeName = null;
            setter = null;
            typeMirror = null;
            supportsDirtyTracking = false;
            idMember = false;
            versionMember = false;
            self = false;
            parent.getContext().logMessage(Diagnostic.Kind.WARNING, "Invalid unknown attribute element kind " + element.getKind() + " for attribute: " + element);
        }
        if (updatable == null) {
            updatable = false;
        }
        if (mutable == null) {
            mutable = updatable || subviewElement != null && EntityViewTypeUtils.isMutable(subviewElement, context);
        }
        this.self = self;
        this.updatable = updatable;
        this.mutable = updatable || mutable;
    }

    public AnnotationMetaAttribute(AnnotationMetaEntityView parent, Element element, String modelType, String declaredJavaType, Context context) {
        this(parent, element, modelType, declaredJavaType, null, context, false);
    }

    protected AnnotationMetaAttribute(AnnotationMetaEntityView parent, Context context) {
        this(parent, null, TypeUtils.getType(parent.getEntityVersionAttribute(), context), TypeUtils.getRealType(parent.getEntityVersionAttribute(), context), null, context, true);
    }

    private static void addAttributeFilter(Map<String, AttributeFilter> filters, AnnotationMirror mirror, Context context) {
        String name = "";
        TypeMirror type = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
            switch (entry.getKey().getSimpleName().toString()) {
                case "name":
                    name = (String) entry.getValue().getValue();
                    break;
                case "value":
                    type = (TypeMirror) entry.getValue().getValue();
                    break;
                default:
                    break;
            }
        }
        filters.put(name, new AttributeFilter(name, (TypeElement) ((DeclaredType) type).asElement(), context));
    }

    protected static TypeElement getSubview(String realType, Context context) {
        //CHECKSTYLE:OFF: FallThrough
        //CHECKSTYLE:OFF: MissingSwitchDefault
        switch (realType) {
            case "int":
            case "boolean":
            case "byte":
            case "char":
            case "double":
            case "float":
            case "long":
            case "short":
                return null;
        }
        //CHECKSTYLE:ON: FallThrough
        //CHECKSTYLE:ON: MissingSwitchDefault
        TypeElement typeElement = context.getTypeElement(realType);
        if (typeElement == null || !TypeUtils.containsAnnotation(typeElement, Constants.ENTITY_VIEW)) {
            return null;
        }
        return typeElement;
    }

    @Override
    public void appendDefaultValue(StringBuilder sb, boolean createEmpty, boolean createConstructor, ImportContext importContext) {
        if (createEmpty && createEmptyFlatViews) {
            if (!modelType.equals(declaredJavaType)) {
                sb.append("(").append(declaredJavaType).append(") ");
            }
            String attributeImplementationType = importContext.importType(getGeneratedTypePrefix() + ImplementationClassWriter.IMPL_CLASS_NAME_SUFFIX);
            sb.append("new ").append(attributeImplementationType).append("((")
                    .append(attributeImplementationType).append(") null, ");
            if (createConstructor) {
                sb.append(BuilderClassWriter.OPTIONAL_PARAMS).append(")");
            } else {
                sb.append(importContext.importType(Collections.class.getName())).append(".emptyMap())");
            }
        } else {
            sb.append(TypeUtils.getDefaultValue(typeMirror.getKind()));
        }
    }

    @Override
    public boolean isCreateEmptyFlatViews() {
        return createEmptyFlatViews;
    }

    public Map<String, TypeElement> getOptionalParameters() {
        return optionalParameters;
    }

    public boolean isIdMember() {
        return idMember;
    }

    public boolean isVersion() {
        return versionMember;
    }

    protected String getDerivedTypeName() {
        return derivedTypeName;
    }

    @Override
    public void appendMetamodelAttributeType(StringBuilder sb, ImportContext importContext) {
        sb.append(importContext.importType(getMetaType()))
                .append('<')
                .append(importContext.importType(parent.getQualifiedName()))
                .append(", ");
        appendElementType(sb, importContext);
        sb.append('>');
    }

    @Override
    public void appendElementType(StringBuilder sb, ImportContext importContext) {
        sb.append(importContext.importType(getModelType()));
    }

    @Override
    public void appendMetamodelAttributeDeclarationString(StringBuilder sb, ImportContext importContext) {
        sb.append("    public static volatile ");
        if (isSubview()) {
            if (isMultiCollection()) {
                sb.append(parent.metamodelImportType(generatedTypePrefix + MultiRelationClassWriter.MULTI_RELATION_CLASS_NAME_SUFFIX))
                        .append('<');
                sb.append(importContext.importType(parent.getQualifiedName()))
                        .append(", ");
                appendElementType(sb, parent.getMetamodelImportContext());
                sb.append(", ");
            } else {
                sb.append(parent.metamodelImportType(generatedTypePrefix + RelationClassWriter.RELATION_CLASS_NAME_SUFFIX))
                        .append('<')
                        .append(importContext.importType(parent.getQualifiedName()))
                        .append(", ");
            }
        }
        appendMetamodelAttributeType(sb, parent.getMetamodelImportContext());
        if (isSubview()) {
            sb.append('>');
        }
        sb.append(' ')
                .append(getPropertyName())
                .append(';');
    }

    @Override
    public void appendMetamodelAttributeNameDeclarationString(StringBuilder sb, ImportContext importContext) {
        String propertyName = getPropertyName();
        sb.append("    public static final ")
                .append(importContext.importType(String.class.getName()))
                .append(" ");

        for (int i = 0; i < propertyName.length(); i++) {
            final char c = propertyName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        sb.append(" = ")
                .append("\"")
                .append(propertyName)
                .append("\"")
                .append(";");
    }

    @Override
    public void appendImplementationAttributeDeclarationString(StringBuilder sb) {
        sb.append("    private ");

        if (setter == null && (!idMember && !versionMember || !parent.isCreatable() && !parent.isUpdatable())) {
            sb.append("final ");
        }

        sb.append(getImplementationTypeString());
        sb.append(' ').append(getPropertyName()).append(";");
    }

    @Override
    public void appendImplementationAttributeGetterAndSetterString(StringBuilder sb, Context context) {
        sb.append("    @Override")
                .append(NEW_LINE)
                .append("    public ");

        sb.append(getImplementationTypeString());

        sb.append(' ')
                .append(element.getSimpleName().toString())
                .append("() {")
                .append(NEW_LINE)
                .append("        return ")
                .append(getPropertyName())
                .append(";")
                .append(NEW_LINE)
                .append("    }");

        if (setter != null) {
            sb.append(NEW_LINE)
                    .append("    @Override")
                    .append(NEW_LINE)
                    .append("    public void ")
                    .append(setter.getSimpleName().toString())
                    .append('(');

            sb.append(getImplementationTypeString());

            sb.append(' ')
                    .append(getPropertyName())
                    .append(") {")
                    .append(NEW_LINE);

            if (idMember) {
                if (parent.isCreatable()) {

                    sb.append("        if ($$_kind != (byte) 2) {").append(NEW_LINE);
                    sb.append("            throw new IllegalArgumentException(\"Updating the id attribute '")
                            .append(getPropertyName())
                            .append("' is only allowed for new entity view objects created via EntityViewManager.create()!\");")
                            .append(NEW_LINE);
                    sb.append("        }").append(NEW_LINE);
                } else if (parent.isUpdatable()) {
                    sb.append("        throw new IllegalArgumentException(\"Updating the id attribute '")
                            .append(getPropertyName())
                            .append("' is only allowed for new entity view objects created via EntityViewManager.create()!\");")
                            .append(NEW_LINE);
                }
            }

            List<Element> entityIdAttributes = Collections.emptyList();
            if (dirtyStateIndex != -1 && !updatable && (parent.isCreatable() || parent.isUpdatable())) {
                sb.append("        Object tmp;").append(NEW_LINE);
                sb.append("        if (").append(getPropertyName()).append(" != this.").append(getPropertyName());

                if (this instanceof AnnotationMetaCollection) {
                    // TODO: We could theoretically support collections too by looking into them and asserting equality element-wise
                } else {
                    if (subviewElement != null) {
                        if (subviewInfo != null && subviewInfo.getEntityViewIdElement() != null) {
                            String idMethodName = subviewInfo.getEntityViewIdElement().getSimpleName().toString();
                            sb.append(" && (").append(getPropertyName()).append(" == null || (tmp = ").append(getPropertyName()).append('.').append(idMethodName);
                            sb.append("()) == null || !java.util.Objects.equals(tmp, this.").append(getPropertyName()).append('.').append(idMethodName);
                            sb.append("()))");
                        }
                    } else {
                        entityIdAttributes = EntityViewTypeUtils.getEntityIdAttributes(modelType, context);
                        if (!entityIdAttributes.isEmpty()) {
                            for (Element entityIdAttribute : entityIdAttributes) {
                                String idAttributeName;
                                if (entityIdAttribute.getKind() == ElementKind.METHOD) {
                                    String methodName = entityIdAttribute.getSimpleName().toString();
                                    if (methodName.startsWith("is")) {
                                        idAttributeName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                                    } else {
                                        idAttributeName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                                    }
                                } else {
                                    idAttributeName = entityIdAttribute.getSimpleName().toString();
                                }
                                String idMethodName = "$$_" + entityIdAttribute.getEnclosingElement().getSimpleName().toString() + "_" + idAttributeName;
                                sb.append(" && (").append(getPropertyName()).append(" == null || (tmp = ").append(idMethodName).append('(').append(getPropertyName());
                                sb.append(")) == null || !java.util.Objects.equals(tmp, ").append(idMethodName).append("(this.").append(getPropertyName());
                                sb.append(")))");
                            }
                        }
                    }
                }
                sb.append(") {").append(NEW_LINE);
                sb.append("            throw new IllegalArgumentException(\"Updating the mutable-only attribute '").append(getPropertyName()).append("' with a value that has not the same identity is not allowed! Consider making the attribute updatable or update the value directly instead of replacing it!\");").append(NEW_LINE);
                sb.append("        }").append(NEW_LINE);
            }

            boolean renderSetter = true;
            boolean needsMethodAttribute = true;
            if (updatable && (parent.isUpdatable() || parent.isCreatable())) {
                if (this instanceof AnnotationMetaCollection) {
                    if (context.isStrictCascadingCheck()) {
                        sb.append("        throw new IllegalArgumentException(\"Replacing a collection that PERSIST or UPDATE cascades is prohibited by default! Instead, replace the contents by doing clear() and addAll()!\");").append(NEW_LINE);
                        renderSetter = false;
                    }
                } else {
                    if (subviewElement != null) {
                        sb.append("        ").append(parent.implementationImportType(Constants.METHOD_ATTRIBUTE)).append("<?, ?> m = (").append(parent.implementationImportType(Constants.METHOD_ATTRIBUTE)).append("<?, ?>) ")
                                .append(parent.implementationImportType(derivedTypeName + MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)).append(".").append(getPropertyName()).append(".attr();").append(NEW_LINE);
                        needsMethodAttribute = false;
                        sb.append("        if (").append(getPropertyName()).append(" != null) {").append(NEW_LINE);
                        sb.append("            Class<?> c;").append(NEW_LINE);
                        sb.append("            boolean isNew;").append(NEW_LINE);
                        sb.append("            if (").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(") {").append(NEW_LINE);
                        sb.append("                c = ((").append(parent.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(") ").append(getPropertyName()).append(").$$_getEntityViewClass();").append(NEW_LINE);
                        sb.append("                isNew = ((").append(parent.implementationImportType(Constants.ENTITY_VIEW_PROXY)).append(") ").append(getPropertyName()).append(").$$_isNew();").append(NEW_LINE);
                        sb.append("            } else {").append(NEW_LINE);
                        sb.append("                c = ").append(getPropertyName()).append(".getClass();").append(NEW_LINE);
                        sb.append("                isNew = false;").append(NEW_LINE);
                        sb.append("            }").append(NEW_LINE);
                        sb.append("            if (!m.getAllowedSubtypes().contains(c)) {").append(NEW_LINE);
                        sb.append("                throw new IllegalArgumentException(\"Allowed subtypes for attribute '").append(getPropertyName()).append("' are \" + m.getAllowedSubtypes() + \" but got an instance of: \" + c.getName());").append(NEW_LINE);
                        sb.append("            }").append(NEW_LINE);
                        if (context.isStrictCascadingCheck()) {
                            sb.append("            if (this != (Object) ").append(getPropertyName()).append(" && !isNew && m.getParentRequiringUpdateSubtypes().contains(c) && !((").append(parent.implementationImportType(Constants.DIRTY_TRACKER)).append(") ").append(getPropertyName()).append(").$$_hasParent()) {").append(NEW_LINE);
                            sb.append("                throw new IllegalArgumentException(\"Setting instances of type [\" + c.getName() + \"] on attribute '").append(getPropertyName()).append("' is not allowed until they are assigned to an attribute that cascades the type!")
                                    .append(" If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { UPDATE }). You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false\");").append(NEW_LINE);
                            sb.append("            }").append(NEW_LINE);

                            sb.append("            if (this != (Object) ").append(getPropertyName()).append(" && isNew && m.getParentRequiringCreateSubtypes().contains(c) && !((").append(parent.implementationImportType(Constants.DIRTY_TRACKER)).append(") ").append(getPropertyName()).append(").$$_hasParent()) {").append(NEW_LINE);
                            sb.append("                throw new IllegalArgumentException(\"Setting instances of type [\" + c.getName() + \"] on attribute '").append(getPropertyName()).append("' is not allowed until they are assigned to an attribute that cascades the type!")
                                    .append(" If you want this attribute to cascade, annotate it with @UpdatableMapping(cascade = { PERSIST }). You can also turn off strict cascading checks by setting ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK to false\");").append(NEW_LINE);
                            sb.append("            }").append(NEW_LINE);
                        }
                        sb.append("        }").append(NEW_LINE);
                    }
                }
            }

            if (renderSetter) {
                if (dirtyStateIndex != -1 && (parent.isUpdatable() || parent.isCreatable())) {
                    if (this instanceof AnnotationMetaCollection) {
                        sb.append("        if (this.").append(getPropertyName()).append(" != ").append(getPropertyName()).append(" && this.").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") {").append(NEW_LINE);
                        sb.append("            ((").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") this.").append(getPropertyName()).append(").$$_unsetParent();").append(NEW_LINE);
                        sb.append("        }").append(NEW_LINE);
                    } else if (subviewElement != null) {
                        if (needsMethodAttribute) {
                            sb.append("        ").append(parent.implementationImportType(Constants.METHOD_ATTRIBUTE)).append("<?, ?> m = (").append(parent.implementationImportType(Constants.METHOD_ATTRIBUTE)).append("<?, ?>) ")
                                    .append(parent.implementationImportType(derivedTypeName + MetamodelClassWriter.META_MODEL_CLASS_NAME_SUFFIX)).append(".").append(getPropertyName()).append(".attr();").append(NEW_LINE);
                        }
                        if (kind != MappingKind.CORRELATED) {
                            sb.append("        if (!m.isPersistCascaded() && !m.isUpdateCascaded() && this.").append(getPropertyName()).append(" != ").append(getPropertyName()).append(" && this.").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.MUTABLE_STATE_TRACKABLE)).append(") {").append(NEW_LINE);
                            sb.append("            ((").append(parent.implementationImportType(Constants.MUTABLE_STATE_TRACKABLE)).append(") this.").append(getPropertyName()).append(").$$_removeReadOnlyParent(this, ").append(dirtyStateIndex).append(");").append(NEW_LINE);
                            sb.append("        } else ");
                        } else {
                            sb.append("        ");
                        }
                        sb.append("if (this.").append(getPropertyName()).append(" != ").append(getPropertyName()).append(" && this.").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") {").append(NEW_LINE);
                        sb.append("            ((").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") this.").append(getPropertyName()).append(").$$_unsetParent();").append(NEW_LINE);
                        sb.append("        }").append(NEW_LINE);
                    }

                    sb.append("        if (this.$$_mutableState != null) {").append(NEW_LINE);
                    sb.append("            this.$$_mutableState[").append(dirtyStateIndex).append("] = ").append(getPropertyName()).append(";").append(NEW_LINE);
                    sb.append("        }").append(NEW_LINE);
                    sb.append("        this.$$_markDirty(").append(dirtyStateIndex).append(");").append(NEW_LINE);

                    if (this instanceof AnnotationMetaCollection || subviewElement != null) {
                        sb.append("        if (").append(getPropertyName()).append(" != null && this.").append(getPropertyName()).append(" != ").append(getPropertyName()).append(") {").append(NEW_LINE);
                        if (this instanceof AnnotationMetaCollection) {
                            sb.append("            ");
                        } else {
                            if (kind != MappingKind.CORRELATED) {
                                sb.append("            if (!m.isPersistCascaded() && !m.isUpdateCascaded() && ").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.MUTABLE_STATE_TRACKABLE)).append(") {").append(NEW_LINE);
                                sb.append("                ((").append(parent.implementationImportType(Constants.MUTABLE_STATE_TRACKABLE)).append(") ").append(getPropertyName()).append(").$$_addReadOnlyParent(this, ").append(dirtyStateIndex).append(");").append(NEW_LINE);
                                sb.append("            } else ");
                            } else {
                                sb.append("            ");
                            }
                        }
                        sb.append("if (").append(getPropertyName()).append(" instanceof ").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") {").append(NEW_LINE);
                        sb.append("                ((").append(parent.implementationImportType(Constants.BASIC_DIRTY_TRACKER)).append(") ").append(getPropertyName()).append(").$$_setParent(this, ").append(dirtyStateIndex).append(");").append(NEW_LINE);
                        sb.append("            }").append(NEW_LINE);
                        sb.append("        }").append(NEW_LINE);
                    }
                }

                if (!idMember || parent.isCreatable() || !parent.isUpdatable()) {
                    sb.append("        this.")
                            .append(getPropertyName())
                            .append(" = ")
                            .append(getPropertyName())
                            .append(";")
                            .append(NEW_LINE);
                }
            }

            sb.append("    }");

            if (!entityIdAttributes.isEmpty() && parent.addAccessorForType(modelType)) {
                for (Element entityIdAttribute : entityIdAttributes) {
                    String idAttributeName;
                    if (entityIdAttribute.getKind() == ElementKind.METHOD) {
                        String methodName = entityIdAttribute.getSimpleName().toString();
                        if (methodName.startsWith("is")) {
                            idAttributeName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                        } else {
                            idAttributeName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                        }
                    } else {
                        idAttributeName = entityIdAttribute.getSimpleName().toString();
                    }
                    String idMethodName = "$$_" + entityIdAttribute.getEnclosingElement().getSimpleName().toString() + "_" + idAttributeName;
                    String accessor = idMethodName + "_accessor";
                    sb.append(NEW_LINE);
                    if (entityIdAttribute.getKind() == ElementKind.METHOD) {
                        sb.append("    private static final ").append(parent.implementationImportType(Method.class.getName())).append(" ").append(accessor).append(";").append(NEW_LINE);
                        sb.append("    static {").append(NEW_LINE);
                        sb.append("        try {").append(NEW_LINE);
                        sb.append("            Method m = ").append(parent.implementationImportType(modelType)).append(".class.getDeclaredMethod(\"").append(entityIdAttribute.getSimpleName()).append("\");").append(NEW_LINE);
                        sb.append("            m.setAccessible(true);").append(NEW_LINE);
                        sb.append("            ").append(accessor).append(" = m;").append(NEW_LINE);
                        sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                        sb.append("            throw new RuntimeException(\"Could not initialize accessor!\", ex);").append(NEW_LINE);
                        sb.append("        }").append(NEW_LINE);
                        sb.append("    }").append(NEW_LINE);
                    } else {
                        sb.append("    private static final ").append(parent.implementationImportType(Field.class.getName())).append(" ").append(accessor).append(";").append(NEW_LINE);
                        sb.append("    static {").append(NEW_LINE);
                        sb.append("        try {").append(NEW_LINE);
                        sb.append("            Field f = ").append(parent.implementationImportType(modelType)).append(".class.getDeclaredField(\"").append(entityIdAttribute.getSimpleName()).append("\");").append(NEW_LINE);
                        sb.append("            f.setAccessible(true);").append(NEW_LINE);
                        sb.append("            ").append(accessor).append(" = f;").append(NEW_LINE);
                        sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                        sb.append("            throw new RuntimeException(\"Could not initialize accessor!\", ex);").append(NEW_LINE);
                        sb.append("        }").append(NEW_LINE);
                        sb.append("    }").append(NEW_LINE);
                    }
                    sb.append("    private Object ").append(idAttributeName).append("(Object o) {").append(NEW_LINE);
                    if (entityIdAttribute.getKind() == ElementKind.METHOD) {
                        sb.append("        try {").append(NEW_LINE);
                        sb.append("            return ").append(idAttributeName).append(".invoke(o);").append(";").append(NEW_LINE);
                        sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                        sb.append("            throw new RuntimeException(\"Could not access id!\", ex);").append(NEW_LINE);
                        sb.append("        }");
                    } else {
                        sb.append("        try {").append(NEW_LINE);
                        sb.append("            return ").append(idAttributeName).append(".get(o);").append(";").append(NEW_LINE);
                        sb.append("        } catch (Exception ex) {").append(NEW_LINE);
                        sb.append("            throw new RuntimeException(\"Could not access id!\", ex);").append(NEW_LINE);
                        sb.append("        }");
                    }
                    sb.append("    }");
                }
            }
        }
    }

    @Override
    public void appendBuilderAttributeDeclarationString(StringBuilder sb) {
        if (element != null && element.getKind() == ElementKind.PARAMETER) {
            sb.append("    ");
        }
        sb.append("    protected ");
        sb.append(getBuilderImplementationTypeString());
        sb.append(' ').append(getPropertyName()).append(";");
    }

    @Override
    public void appendBuilderAttributeGetterAndSetterString(StringBuilder sb) {
        ElementKind kind = element == null ? null : element.getKind();
        sb.append("    public ").append(getBuilderImplementationTypeString()).append(' ');

        if (kind == ElementKind.METHOD) {
            sb.append(element.getSimpleName().toString());
        } else {
            if ("boolean".equals(modelType)) {
                sb.append("is");
            } else {
                sb.append("get");
            }
            if (kind == ElementKind.PARAMETER) {
                sb.append("Param");
            }
            sb.append(Character.toUpperCase(getPropertyName().charAt(0)));
            sb.append(getPropertyName(), 1, getPropertyName().length());
        }
        sb.append("() {")
                .append(NEW_LINE)
                .append("        return ")
                .append(getPropertyName())
                .append(";")
                .append(NEW_LINE)
                .append("    }");

        sb.append(NEW_LINE);
        sb.append("    public void set");
        if (kind == ElementKind.PARAMETER) {
            sb.append("Param");
        }
        sb.append(Character.toUpperCase(getPropertyName().charAt(0)));
        sb.append(getPropertyName(), 1, getPropertyName().length());
        sb.append('(');

        sb.append(getBuilderImplementationTypeString());

        sb.append(' ')
                .append(getPropertyName())
                .append(") {")
                .append(NEW_LINE)
                .append("        this.")
                .append(getPropertyName())
                .append(" = ")
                .append(getPropertyName())
                .append(";")
                .append(NEW_LINE)
                .append("    }");
    }

    @Override
    public String getImplementationTypeString() {
        return implementationTypeString;
    }

    @Override
    public String getBuilderImplementationTypeString() {
        if (convertedModelType == null) {
            return getImplementationTypeString();
        } else {
            return parent.builderImportType(modelType);
        }
    }

    @Override
    public Collection<AttributeFilter> getFilters() {
        return filters.values();
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public MappingKind getKind() {
        return kind;
    }

    @Override
    public String getMapping() {
        return mapping;
    }

    @Override
    public int getAttributeIndex() {
        return attributeIndex;
    }

    @Override
    public void setAttributeIndex(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    @Override
    public int getDirtyStateIndex() {
        return dirtyStateIndex;
    }

    @Override
    public void setDirtyStateIndex(int dirtyStateIndex) {
        this.dirtyStateIndex = dirtyStateIndex;
    }

    @Override
    public Element getSetter() {
        return setter;
    }

    @Override
    public boolean supportsDirtyTracking() {
        return supportsDirtyTracking;
    }

    @Override
    public String getPropertyName() {
        return attributeName;
    }

    public MetaEntityView getHostingEntity() {
        return parent;
    }

    @Override
    public boolean isSubview() {
        return subviewElement != null;
    }

    @Override
    public boolean isMultiCollection() {
        return false;
    }

    @Override
    public TypeElement getSubviewElement() {
        return subviewElement;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return mutable;
    }

    @Override
    public boolean isSelf() {
        return self;
    }

    @Override
    public boolean isPrimitive() {
        return typeMirror.getKind().isPrimitive();
    }

    @Override
    public abstract String getMetaType();

    @Override
    public String getModelType() {
        return modelType;
    }

    @Override
    public String getGeneratedTypePrefix() {
        return generatedTypePrefix;
    }

    @Override
    public String getDeclaredJavaType() {
        return declaredJavaType;
    }

    @Override
    public String getConvertedModelType() {
        return convertedModelType;
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }
}
