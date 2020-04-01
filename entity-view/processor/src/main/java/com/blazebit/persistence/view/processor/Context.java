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

import com.blazebit.persistence.view.processor.annotation.AnnotationMetaEntityView;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    private boolean addGeneratedAnnotation = true;
    private boolean addGenerationDate;
    private boolean addSuppressWarningsAnnotation;

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

    public void logMessage(Diagnostic.Kind type, String message) {
        if (!logDebug && type.equals(Diagnostic.Kind.OTHER)) {
            return;
        }
        pe.getMessager().printMessage(type, message);
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
}
