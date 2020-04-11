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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@SupportedAnnotationTypes({
        Constants.ENTITY_VIEW
})
@SupportedOptions({
        EntityViewAnnotationProcessor.DEBUG_OPTION,
        EntityViewAnnotationProcessor.ADD_GENERATION_DATE,
        EntityViewAnnotationProcessor.ADD_GENERATED_ANNOTATION,
        EntityViewAnnotationProcessor.ADD_SUPPRESS_WARNINGS_ANNOTATION,
        EntityViewAnnotationProcessor.STRICT_CASCADING_CHECK,
        EntityViewAnnotationProcessor.DEFAULT_VERSION_ATTRIBUTE_NAME,
        EntityViewAnnotationProcessor.DEFAULT_VERSION_ATTRIBUTE_TYPE,
        EntityViewAnnotationProcessor.GENERATE_IMPLEMENTATIONS,
        EntityViewAnnotationProcessor.GENERATE_BUILDERS,
        EntityViewAnnotationProcessor.CREATE_EMPTY_FLAT_VIEWS,
})
public class EntityViewAnnotationProcessor extends AbstractProcessor {

    public static final String DEBUG_OPTION = "debug";
    public static final String ADD_GENERATION_DATE = "addGenerationDate";
    public static final String ADD_GENERATED_ANNOTATION = "addGeneratedAnnotation";
    public static final String ADD_SUPPRESS_WARNINGS_ANNOTATION = "addSuppressWarningsAnnotation";
    public static final String STRICT_CASCADING_CHECK = "strictCascadingCheck";
    public static final String DEFAULT_VERSION_ATTRIBUTE_NAME = "defaultVersionAttributeName";
    public static final String DEFAULT_VERSION_ATTRIBUTE_TYPE = "defaultVersionAttributeType";
    public static final String GENERATE_IMPLEMENTATIONS = "generateImplementations";
    public static final String GENERATE_BUILDERS = "generateBuilders";
    public static final String CREATE_EMPTY_FLAT_VIEWS = "createEmptyFlatViews";

    private Context context;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        context = new Context(processingEnvironment);
        context.logMessage(Diagnostic.Kind.NOTE, "Blaze-Persistence Entity-View Annotation Processor");

        context.setAddGeneratedAnnotation(getOption(processingEnvironment, ADD_GENERATED_ANNOTATION, true));
        context.setAddGenerationDate(getOption(processingEnvironment, ADD_GENERATION_DATE, false));
        context.setAddSuppressWarningsAnnotation(getOption(processingEnvironment, ADD_SUPPRESS_WARNINGS_ANNOTATION, false));
        context.setStrictCascadingCheck(getOption(processingEnvironment, STRICT_CASCADING_CHECK, true));
        context.setGenerateImplementations(getOption(processingEnvironment, GENERATE_IMPLEMENTATIONS, true));
        context.setGenerateBuilders(getOption(processingEnvironment, GENERATE_BUILDERS, true));
        context.setCreateEmptyFlatViews(getOption(processingEnvironment, CREATE_EMPTY_FLAT_VIEWS, true));

        context.setDefaultVersionAttributeName(processingEnvironment.getOptions().get(DEFAULT_VERSION_ATTRIBUTE_NAME));
        context.setDefaultVersionAttributeType(processingEnvironment.getOptions().get(DEFAULT_VERSION_ATTRIBUTE_TYPE));
    }

    private static Boolean getOption(ProcessingEnvironment processingEnvironment, String option, boolean defaultValue) {
        String tmp = processingEnvironment.getOptions().get(option);
        if (tmp == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(tmp);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver() && !roundEnv.errorRaised() && !annotations.isEmpty()) {
            execute(annotations, roundEnv);
            return false;
        } else {
            return false;
        }
    }

    private void execute(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        discoverEntityViews(roundEnvironment.getRootElements());
        createMetaModelClasses();
    }

    private void discoverEntityViews(Collection<? extends Element> elements) {
        for (Element element : elements) {
            if (isEntityView(element)) {
                context.logMessage(Diagnostic.Kind.OTHER, "Processing annotated class " + element.toString());
                handleRootElementAnnotationMirrors(element);
            }
            discoverEntityViews(element.getEnclosedElements());
        }
    }

    private void createMetaModelClasses() {
        StringBuilder sb = new StringBuilder(64 * 1024);
        for (MetaEntityView entityView : context.getMetaEntityViews()) {
            if (context.isAlreadyGenerated(entityView.getQualifiedName()) || !entityView.isValid()) {
                continue;
            }
            context.logMessage(Diagnostic.Kind.OTHER, "Writing meta model for entity view " + entityView);
            MetamodelClassWriter.writeFile(sb, entityView, context);
            if (context.isGenerateImplementations()) {
                ForeignPackageAdapterClassWriter.writeFiles(sb, entityView, context);
                ImplementationClassWriter.writeFile(sb, entityView, context);
                if (context.isGenerateBuilders()) {
                    BuilderClassWriter.writeFile(sb, entityView, context);
                }
            }
            context.markGenerated(entityView.getQualifiedName());
        }
    }

    private boolean isEntityView(Element element) {
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            return TypeUtils.containsAnnotation(element, Constants.ENTITY_VIEW);
        }
        return false;
    }

    private void handleRootElementAnnotationMirrors(final Element element) {
        if (!ElementKind.CLASS.equals(element.getKind()) && !ElementKind.INTERFACE.equals(element.getKind())) {
            return;
        }

        AnnotationMetaEntityView metaEntity = new AnnotationMetaEntityView((TypeElement) element, context);
        context.addMetaEntityViewToContext(metaEntity.getQualifiedName(), metaEntity);
    }
}
