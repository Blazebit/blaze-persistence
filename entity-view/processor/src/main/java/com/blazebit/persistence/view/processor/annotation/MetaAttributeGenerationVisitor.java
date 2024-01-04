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

package com.blazebit.persistence.view.processor.annotation;

import com.blazebit.persistence.view.processor.Constants;
import com.blazebit.persistence.view.processor.Context;
import com.blazebit.persistence.view.processor.TypeUtils;
import com.blazebit.persistence.view.processor.convert.TypeConverter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MetaAttributeGenerationVisitor extends SimpleTypeVisitor6<AnnotationMetaAttribute, Element> {

    private final AnnotationMetaEntityView entity;
    private final Context context;
    private TypeVariable processingTypeVariable;

    MetaAttributeGenerationVisitor(AnnotationMetaEntityView entity, Context context) {
        this.entity = entity;
        this.context = context;
    }

    @Override
    public AnnotationMetaAttribute visitPrimitive(PrimitiveType t, Element element) {
        String type = TypeUtils.toWrapperTypeString(t);
        String realType = TypeUtils.toTypeString((DeclaredType) entity.getTypeElement().asType(), t, context);
        return new AnnotationMetaSingularAttribute(entity, element, type, realType, null, context);
    }

    @Override
    public AnnotationMetaAttribute visitArray(ArrayType t, Element element) {
        String type = TypeUtils.toWrapperTypeString(t);
        String realType = TypeUtils.toTypeString((DeclaredType) entity.getTypeElement().asType(), t.getComponentType(), entity.getContext()) + "[]";
        return new AnnotationMetaSingularAttribute(entity, element, type, realType, null, context);
    }

    @Override
    public AnnotationMetaAttribute visitTypeVariable(TypeVariable t, Element element) {
        TypeMirror mirror = TypeUtils.asMemberOf(context, (DeclaredType) entity.getTypeElement().asType(), element);
        TypeVariable old = processingTypeVariable;
        processingTypeVariable = t;
        try {
            return mirror.accept(this, element);
        } finally {
            processingTypeVariable = old;
        }
    }

    @Override
    public AnnotationMetaAttribute visitDeclared(DeclaredType declaredType, Element element) {
        DeclaredType entityDeclaredType = (DeclaredType) entity.getTypeElement().asType();
        TypeElement returnedElement = (TypeElement) context.getTypeUtils().asElement(declaredType);
        String fqNameOfReturnType = returnedElement.getQualifiedName().toString();
        String collection = Constants.COLLECTIONS.get(fqNameOfReturnType);
        if (collection != null && TypeUtils.getAnnotationMirror(element, Constants.MAPPING_SINGULAR) == null && TypeUtils.getAnnotationMirror(element, Constants.MAPPING_PARAMETER) == null) {
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() == 0) {
                context.logMessage(Diagnostic.Kind.ERROR, "Unable to determine type arguments for " + declaredType);
            }
            TypeMirror elementTypeMirror = typeArguments.get(typeArguments.size() - 1);
            String elementType = TypeUtils.extractClosestRealTypeAsString(elementTypeMirror, context);
            String realElementType = TypeUtils.toTypeString(entityDeclaredType, elementTypeMirror, context);
            String elementCollectionType = null;
            String realElementCollectionType = null;
            if (Constants.COLLECTIONS.containsKey(elementType)) {
                // Multi-collection
                if (collection.equals(Constants.METHOD_MAP_ATTRIBUTE)) {
                    collection = Constants.METHOD_MULTI_MAP_ATTRIBUTE;
                } else {
                    collection = Constants.METHOD_MULTI_LIST_ATTRIBUTE;
                }
                elementCollectionType = elementType;
                List<? extends TypeMirror> elementTypeArguments = ((DeclaredType) elementTypeMirror).getTypeArguments();
                realElementCollectionType = TypeUtils.toTypeString(entityDeclaredType, elementTypeMirror, context);
                elementTypeMirror = elementTypeArguments.get(elementTypeArguments.size() - 1);
                elementType = TypeUtils.extractClosestRealTypeAsString(elementTypeMirror, context);
            }
            if (collection.equals(Constants.METHOD_MAP_ATTRIBUTE) || collection.equals(Constants.METHOD_MULTI_MAP_ATTRIBUTE)) {
                TypeMirror keyTypeMirror = typeArguments.get(0);
                String keyType = TypeUtils.extractClosestRealTypeAsString(keyTypeMirror, context);
                String realKeyType = TypeUtils.toTypeString(entityDeclaredType, keyTypeMirror, context);
                return new AnnotationMetaMap(entity, element, collection, returnedElement.toString(), elementCollectionType, keyType, realKeyType, elementType, realElementType, context);
            } else {
                return new AnnotationMetaCollection(entity, element, collection, returnedElement.toString(), elementCollectionType, elementType, realElementType, context);
            }
        } else if (!Constants.SPECIAL.contains(fqNameOfReturnType)) {
            String modelType = returnedElement.getQualifiedName().toString();
            TypeMirror declaredTypeMirror;
            if (element instanceof ExecutableElement) {
                declaredTypeMirror = ((ExecutableElement) element).getReturnType();
            } else {
                declaredTypeMirror = element.asType();
            }
            String declaredJavaType = TypeUtils.toTypeString(entityDeclaredType, declaredTypeMirror, context);
            Map<String, TypeConverter> converters = context.getConverter(fqNameOfReturnType);
            if (!converters.isEmpty()) {
                // Try find a converter matching the entity model type
                TypeConverter typeConverter = converters.get(declaredJavaType);
                if (typeConverter == null) {
                    typeConverter = converters.get("java.lang.Object");
                }

                if (typeConverter != null) {
                    String convertedModelType = modelType;
                    modelType = typeConverter.getUnderlyingType(entityDeclaredType, declaredTypeMirror, context);
                    return new AnnotationMetaSingularAttribute(entity, element, modelType, declaredJavaType, convertedModelType, context);
                }
            }
            if (!declaredTypeMirror.getKind().isPrimitive() && declaredTypeMirror.getKind() != TypeKind.TYPEVAR) {
                modelType = declaredJavaType;
            }
            return new AnnotationMetaSingularAttribute(entity, element, modelType, declaredJavaType, null, context);
        }
        return null;
    }

    @Override
    public AnnotationMetaAttribute visitExecutable(ExecutableType t, Element p) {
        if (!p.getKind().equals(ElementKind.METHOD)) {
            return null;
        }

        String string = p.getSimpleName().toString();
        if ((string.startsWith("get") || string.startsWith("is")) && !t.getReturnType().getKind().equals(TypeKind.VOID) && t.getParameterTypes().isEmpty()) {
            TypeMirror returnType = t.getReturnType();
            if (returnType == processingTypeVariable) {
                return context.getTypeUtils().erasure(returnType).accept(this, p);
            } else {
                return returnType.accept(this, p);
            }
        }
        return null;
    }

}
