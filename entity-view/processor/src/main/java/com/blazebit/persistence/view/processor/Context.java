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
import com.blazebit.persistence.view.processor.convert.TypeConverter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Context {

    private static final Object NULL_OBJECT = new Object();
    private final ProcessingEnvironment pe;
    private final boolean logDebug;
    private final TypeElement generatedAnnotation;

    private final Map<String, MetaEntityView> metaEntityViews = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> generatedModelClasses = new ConcurrentHashMap<>();
    private final ConcurrentMap<CharSequence, Object> typeElements = new ConcurrentHashMap<>();
    private final Map<String, TypeElement> optionalParameters;
    private final Map<String, Map<String, TypeConverter>> converters;

    private final Map<String, Map<String, Element>> initializedViewTypes = new HashMap<>();
    private final Map<String, Map<String, Element>> initializedViewFilters = new HashMap<>();
    private final Map<String, Map<String, Element>> initializedEntities = new HashMap<>();

    private final boolean addGeneratedAnnotation;
    private final boolean addGenerationDate;
    private final boolean addSuppressWarningsAnnotation;
    private final boolean strictCascadingCheck;
    private final boolean generateImplementations;
    private final boolean generateBuilders;
    private final boolean createEmptyFlatViews;
    private final boolean generateDeepConstants;
    private final String defaultVersionAttributeName;
    private final String defaultVersionAttributeType;
    private final int threads;

    public Context(ProcessingEnvironment pe) {
        this.pe = pe;
        this.logDebug = Boolean.parseBoolean(pe.getOptions().get(EntityViewAnnotationProcessor.DEBUG_OPTION));

        Map<String, TypeElement> optionalParameters = new HashMap<>();
        Map<String, Map<String, TypeConverter>> converters = new HashMap<>();
        TypeElement java8AndBelowGeneratedAnnotation = pe.getElementUtils().getTypeElement("javax.annotation.Generated");
        if (java8AndBelowGeneratedAnnotation != null) {
            generatedAnnotation = java8AndBelowGeneratedAnnotation;
        } else {
            // Using the new name for this annotation in Java 9 and above
            generatedAnnotation = pe.getElementUtils().getTypeElement("javax.annotation.processing.Generated");
        }
        for (TypeConverter typeConverter : ServiceLoader.load(TypeConverter.class, Context.class.getClassLoader())) {
            typeConverter.addRegistrations(converters);
        }
        this.converters = converters;

        this.addGeneratedAnnotation = getOption(pe, EntityViewAnnotationProcessor.ADD_GENERATED_ANNOTATION, true);
        this.addGenerationDate = getOption(pe, EntityViewAnnotationProcessor.ADD_GENERATION_DATE, false);
        this.addSuppressWarningsAnnotation = getOption(pe, EntityViewAnnotationProcessor.ADD_SUPPRESS_WARNINGS_ANNOTATION, false);
        this.strictCascadingCheck = getOption(pe, EntityViewAnnotationProcessor.STRICT_CASCADING_CHECK, true);
        this.generateImplementations = getOption(pe, EntityViewAnnotationProcessor.GENERATE_IMPLEMENTATIONS, true);
        this.generateBuilders = getOption(pe, EntityViewAnnotationProcessor.GENERATE_BUILDERS, true);
        this.createEmptyFlatViews = getOption(pe, EntityViewAnnotationProcessor.CREATE_EMPTY_FLAT_VIEWS, true);
        this.generateDeepConstants = getOption(pe, EntityViewAnnotationProcessor.GENERATE_DEEP_CONSTANTS, true);

        this.defaultVersionAttributeName = pe.getOptions().get(EntityViewAnnotationProcessor.DEFAULT_VERSION_ATTRIBUTE_NAME);
        this.defaultVersionAttributeType = pe.getOptions().get(EntityViewAnnotationProcessor.DEFAULT_VERSION_ATTRIBUTE_TYPE);
        String threads = pe.getOptions().get(EntityViewAnnotationProcessor.THREADS);
        if (threads == null || threads.isEmpty()) {
            this.threads = Runtime.getRuntime().availableProcessors();
        } else {
            this.threads = 1;
        }

        String s = pe.getOptions().get(EntityViewAnnotationProcessor.OPTIONAL_PARAMETERS);
        if (s != null) {
            for (String part : s.split("\\s*;\\s*")) {
                int idx = part.lastIndexOf('=');
                String name;
                String type;
                if (idx == -1) {
                    name = part;
                    type = "java.lang.Object";
                } else {
                    name = part.substring(0, idx);
                    type = part.substring(idx + 1);
                }
                optionalParameters.put(name, pe.getElementUtils().getTypeElement(type));
            }
        }
        this.optionalParameters = optionalParameters;
    }

    private static Boolean getOption(ProcessingEnvironment processingEnvironment, String option, boolean defaultValue) {
        String tmp = processingEnvironment.getOptions().get(option);
        if (tmp == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(tmp);
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return pe;
    }

    public TypeElement getTypeElement(CharSequence name) {
        Object cached = typeElements.get(name);
        if (cached != null) {
            return cached == NULL_OBJECT ? null : (TypeElement) cached;
        }
//        synchronized (this) {
        TypeElement typeElement = pe.getElementUtils().getTypeElement(name);
        if (typeElement == null) {
            typeElements.put(name, NULL_OBJECT);
        } else {
            typeElements.put(name, typeElement);
        }
        return typeElement;
//        }
    }

    public Elements getElementUtils() {
        return pe.getElementUtils();
    }

    public Types getTypeUtils() {
        return pe.getTypeUtils();
    }

    public Map<String, TypeConverter> getConverter(String fqn) {
        Map<String, TypeConverter> map = converters.get(fqn);
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    public void logMessage(Diagnostic.Kind type, String message) {
        if (!logDebug && type.equals(Diagnostic.Kind.OTHER)) {
            return;
        }
        synchronized (pe) {
            pe.getMessager().printMessage(type, message);
        }
    }

    public boolean matchesDefaultVersionAttribute(Element member) {
        if (getDefaultVersionAttributeName() == null || !getDefaultVersionAttributeName().equals(EntityViewTypeUtils.getAttributeName(member))) {
            return false;
        }
        if (member.getKind() == ElementKind.METHOD) {
            return getDefaultVersionAttributeType() == null || getDefaultVersionAttributeType().equals(((TypeElement) ((DeclaredType) ((ExecutableType) member.asType()).getReturnType()).asElement()).getQualifiedName().toString());
        }
        return getDefaultVersionAttributeType() == null || getDefaultVersionAttributeType().equals(((TypeElement) ((DeclaredType) member.asType()).asElement()).getQualifiedName().toString());
    }

    public void addMetaEntityViewToContext(String qualifiedName, AnnotationMetaEntityView metaEntityView) {
        metaEntityViews.put(qualifiedName, metaEntityView);
    }

    public Collection<MetaEntityView> getMetaEntityViews() {
        return metaEntityViews.values();
    }

    public Map<String, MetaEntityView> getMetaEntityViewMap() {
        return metaEntityViews;
    }

    public boolean markGenerated(String name) {
        return generatedModelClasses.putIfAbsent(name, Boolean.TRUE) == null;
    }

    public TypeElement getGeneratedAnnotation() {
        return generatedAnnotation;
    }

    public boolean addGeneratedAnnotation() {
        return addGeneratedAnnotation;
    }

    public boolean addGeneratedDate() {
        return addGenerationDate;
    }

    public boolean isAddSuppressWarningsAnnotation() {
        return addSuppressWarningsAnnotation;
    }

    public boolean isStrictCascadingCheck() {
        return strictCascadingCheck;
    }

    public boolean isGenerateImplementations() {
        return generateImplementations;
    }

    public boolean isGenerateBuilders() {
        return generateBuilders;
    }

    public String getDefaultVersionAttributeName() {
        return defaultVersionAttributeName;
    }

    public String getDefaultVersionAttributeType() {
        return defaultVersionAttributeType;
    }

    public boolean isCreateEmptyFlatViews() {
        return createEmptyFlatViews;
    }

    public boolean isGenerateDeepConstants() {
        return generateDeepConstants;
    }

    public Map<String, TypeElement> getOptionalParameters() {
        return optionalParameters;
    }

    public int getThreads() {
        return threads;
    }

    public Map<String, Element> initializeEntityViewElement(TypeElement entityView) {
        String entityViewFqn = entityView.toString();
        Map<String, Element> members = initializedViewTypes.get(entityViewFqn);
        if (members != null) {
            return members;
        }

        TypeMirror typeMirror = entityView.getSuperclass();
        if (typeMirror.getKind() != TypeKind.NONE) {
            TypeElement superclassElement = (TypeElement) getTypeUtils().asElement(typeMirror);
            members = copySuperclassMembers(initializeEntityViewElement(superclassElement));
        } else {
            members = new HashMap<>();
        }

        initializeEntityViewInterfaces(entityView, members);
        initializeEnclosingElements(entityView);
        initializeAnnotations(entityView);
        initializeTypeArguments(entityView);
        initializeEntityViewEnclosedElements(entityView, members);
        initializedViewTypes.put(entityViewFqn, members);
        return members;
    }

    private Map<String, Element> copySuperclassMembers(Map<String, Element> superclassMembers) {
        Map<String, Element> members = new HashMap<>(superclassMembers.size());
        for (Map.Entry<String, Element> entry : superclassMembers.entrySet()) {
            if (entry.getValue().getKind() != ElementKind.CONSTRUCTOR) {
                members.put(entry.getKey(), entry.getValue());
            }
        }
        return members;
    }

    private void initializeTypeArguments(Parameterizable parameterizable) {
        for (TypeParameterElement typeParameter : parameterizable.getTypeParameters()) {
            typeParameter.asType().getKind();
            for (TypeMirror bound : typeParameter.getBounds()) {
                bound.getKind();
            }
        }
    }

    private void initializeEntityViewInterfaces(TypeElement entityView, Map<String, Element> members) {
        for (TypeMirror typeMirror : entityView.getInterfaces()) {
            TypeElement typeElement = (TypeElement) getTypeUtils().asElement(typeMirror);
            members.putAll(initializeEntityViewElement(typeElement));
        }
    }

    private void initializeEntityViewEnclosedElements(TypeElement entityView, Map<String, Element> members) {
        for (Element enclosedElement : entityView.getEnclosedElements()) {
            switch (enclosedElement.getKind()) {
                case METHOD:
                    ExecutableElement methodElement = (ExecutableElement) enclosedElement;
                    initializeAnnotations(enclosedElement);
                    initializeTypeArguments(methodElement);
                    methodElement.getReturnType().getKind();
                    members.put(enclosedElement.getSimpleName().toString(), enclosedElement);
                    break;
                case CONSTRUCTOR:
                    ExecutableElement constructorElement = (ExecutableElement) enclosedElement;
                    initializeAnnotations(enclosedElement);
                    initializeTypeArguments(constructorElement);
                    for (VariableElement parameter : constructorElement.getParameters()) {
                        parameter.asType().getKind();
                        initializeAnnotations(parameter);
                    }
                    members.put(enclosedElement.toString(), enclosedElement);
                    break;
                default:
                    members.put(enclosedElement.getSimpleName().toString(), enclosedElement);
                    break;
            }
        }
    }

    private void initializeEnclosingElements(TypeElement typeElement) {
        Element enclosingElement = typeElement.getEnclosingElement();
        while (enclosingElement != null) {
            enclosingElement.getKind();
            enclosingElement = enclosingElement.getEnclosingElement();
        }
    }

    private void initializeAnnotations(Element element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            annotationMirror.getAnnotationType().getKind();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                entry.getKey().getKind();
                Object annotationValue = entry.getValue().getValue();
                if (annotationValue instanceof TypeMirror) {
                    TypeMirror typeMirror = (TypeMirror) annotationValue;
                    typeMirror.getKind();
                    switch (annotationMirror.getAnnotationType().toString()) {
                        case Constants.ENTITY_VIEW:
                            if (entry.getKey().getSimpleName().contentEquals("value")) {
                                initializeEntityElement(typeMirror);
                            }
                            break;
                        case Constants.VIEW_FILTER:
                            initializeViewFilter(typeMirror);
                            break;
                        default:
                            break;
                    }
                } else if (annotationValue instanceof List<?>) {
                    switch (annotationMirror.getAnnotationType().toString()) {
                        case Constants.VIEW_FILTERS:
                            //noinspection unchecked
                            for (AnnotationMirror value : (List<AnnotationMirror>) annotationValue) {
                                value.getAnnotationType().getKind();
                                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> filterEntry : value.getElementValues().entrySet()) {
                                    filterEntry.getKey().getKind();
                                    Object filterValue = filterEntry.getValue().getValue();
                                    if (filterValue instanceof TypeMirror) {
                                        initializeViewFilter((TypeMirror) filterValue);
                                    }
                                }
                            }
                            break;
                        default:
                            for (Object value : (List<?>) annotationValue) {
                                if (value instanceof TypeMirror) {
                                    ((TypeMirror) value).getKind();
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    private void initializeViewFilter(TypeMirror viewFilter) {
        String fqn = viewFilter.toString();
        Map<String, Element> members = initializedViewFilters.get(fqn);
        if (members != null) {
            return;
        }
        members = new HashMap<>();
        TypeElement typeElement = (TypeElement) getTypeUtils().asElement(viewFilter);
        initializeEnclosedElements(typeElement, members);
        initializedViewFilters.put(fqn, members);
    }

    private Map<String, Element> initializeEntityElement(TypeMirror entity) {
        String fqn = entity.toString();
        Map<String, Element> members = initializedEntities.get(fqn);
        if (members != null) {
            return members;
        }
        TypeElement typeElement = (TypeElement) getTypeUtils().asElement(entity);

        TypeMirror typeMirror = typeElement.getSuperclass();
        if (typeMirror.getKind() != TypeKind.NONE) {
            members = copySuperclassMembers(initializeEntityElement(typeMirror));
        } else {
            members = new HashMap<>();
        }

        initializeEnclosingElements(typeElement);
        initializeEnclosedElements(typeElement, members);
        initializedEntities.put(fqn, members);
        return members;
    }

    private void initializeEnclosedElements(Element element, Map<String, Element> members) {
        for (Element enclosedElement : element.getEnclosedElements()) {
            enclosedElement.getKind();
            String name = enclosedElement.getSimpleName().toString();
            members.put(name, enclosedElement);
        }
    }

    public Iterable<? extends Element> getAllMembers(TypeElement typeElement) {
        String fqn = typeElement.toString();
        Map<String, Element> elements = initializedViewTypes.get(fqn);
        if (elements != null) {
            return elements.values();
        }
        elements = initializedEntities.get(fqn);
        if (elements != null) {
            return elements.values();
        }
        elements = initializedViewFilters.get(fqn);
        if (elements != null) {
            return elements.values();
        }
        return TypeUtils.getAllMembers(typeElement, this);
    }
}
