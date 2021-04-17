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

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaEntityView;
import com.blazebit.persistence.view.processor.convert.TypeConverter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Context {

    private final ProcessingEnvironment pe;
    private final boolean logDebug;
    private final TypeElement generatedAnnotation;

    private final Map<String, MetaEntityView> metaEntityViews = new HashMap<>();
    private final Collection<String> generatedModelClasses = new HashSet<>();
    private final Map<String, TypeElement> optionalParameters = new HashMap<>();
    private final Map<String, Map<String, TypeConverter>> converters = new HashMap<>();

    private boolean addGeneratedAnnotation;
    private boolean addGenerationDate;
    private boolean addSuppressWarningsAnnotation;
    private boolean strictCascadingCheck;
    private boolean generateImplementations;
    private boolean generateBuilders;
    private boolean createEmptyFlatViews;
    private boolean generateDeepConstants;
    private String defaultVersionAttributeName;
    private String defaultVersionAttributeType;

    public Context(ProcessingEnvironment pe) {
        this.pe = pe;
        this.logDebug = Boolean.parseBoolean(pe.getOptions().get(EntityViewAnnotationProcessor.DEBUG_OPTION));

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
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return pe;
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
        pe.getMessager().printMessage(type, message);
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

    public boolean containsMetaEntityView(String qualifiedName) {
        return metaEntityViews.containsKey(qualifiedName);
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

    public MetaEntityView getMetaEntityView(String fqcn) {
        return metaEntityViews.get(fqcn);
    }

    public void markGenerated(String name) {
        generatedModelClasses.add(name);
    }

    public boolean isAlreadyGenerated(String name) {
        return generatedModelClasses.contains(name);
    }

    public TypeElement getGeneratedAnnotation() {
        return generatedAnnotation;
    }

    public boolean addGeneratedAnnotation() {
        return addGeneratedAnnotation;
    }

    public void setAddGeneratedAnnotation(boolean addGeneratedAnnotation) {
        this.addGeneratedAnnotation = addGeneratedAnnotation;
    }

    public boolean addGeneratedDate() {
        return addGenerationDate;
    }

    public void setAddGenerationDate(boolean addGenerationDate) {
        this.addGenerationDate = addGenerationDate;
    }

    public boolean isAddSuppressWarningsAnnotation() {
        return addSuppressWarningsAnnotation;
    }

    public void setAddSuppressWarningsAnnotation(boolean addSuppressWarningsAnnotation) {
        this.addSuppressWarningsAnnotation = addSuppressWarningsAnnotation;
    }

    public boolean isStrictCascadingCheck() {
        return strictCascadingCheck;
    }

    public void setStrictCascadingCheck(boolean strictCascadingCheck) {
        this.strictCascadingCheck = strictCascadingCheck;
    }

    public boolean isGenerateImplementations() {
        return generateImplementations;
    }

    public void setGenerateImplementations(boolean generateImplementations) {
        this.generateImplementations = generateImplementations;
    }

    public boolean isGenerateBuilders() {
        return generateBuilders;
    }

    public void setGenerateBuilders(boolean generateBuilders) {
        this.generateBuilders = generateBuilders;
    }

    public String getDefaultVersionAttributeName() {
        return defaultVersionAttributeName;
    }

    public void setDefaultVersionAttributeName(String defaultVersionAttributeName) {
        this.defaultVersionAttributeName = defaultVersionAttributeName;
    }

    public String getDefaultVersionAttributeType() {
        return defaultVersionAttributeType;
    }

    public void setDefaultVersionAttributeType(String defaultVersionAttributeType) {
        this.defaultVersionAttributeType = defaultVersionAttributeType;
    }

    public boolean isCreateEmptyFlatViews() {
        return createEmptyFlatViews;
    }

    public void setCreateEmptyFlatViews(boolean createEmptyFlatViews) {
        this.createEmptyFlatViews = createEmptyFlatViews;
    }

    public boolean isGenerateDeepConstants() {
        return generateDeepConstants;
    }

    public void setGenerateDeepConstants(boolean generateDeepConstants) {
        this.generateDeepConstants = generateDeepConstants;
    }

    public Map<String, TypeElement> getOptionalParameters() {
        return optionalParameters;
    }
}
