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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

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
        EntityViewAnnotationProcessor.GENERATE_DEEP_CONSTANTS,
        EntityViewAnnotationProcessor.OPTIONAL_PARAMETERS,
        EntityViewAnnotationProcessor.THREADS,
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
    public static final String GENERATE_DEEP_CONSTANTS = "generateDeepConstants";
    public static final String OPTIONAL_PARAMETERS = "optionalParameters";
    public static final String THREADS = "threads";

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
        int threads = context.getThreads();
        ExecutorService executorService;
        if (threads == 1) {
            executorService = new AbstractExecutorService() {
                @Override
                public void shutdown() {
                }

                @Override
                public List<Runnable> shutdownNow() {
                    return Collections.emptyList();
                }

                @Override
                public boolean isShutdown() {
                    return false;
                }

                @Override
                public boolean isTerminated() {
                    return false;
                }

                @Override
                public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                    return false;
                }

                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            };
        } else {
            executorService = Executors.newFixedThreadPool(threads);
        }
        long initTime = System.nanoTime();
        long start = initTime;
        List<TypeElement> entityViews = new ArrayList<>();
        discoverEntityViews(entityViews, roundEnvironment.getRootElements());
        context.logMessage(Diagnostic.Kind.NOTE, "Annotation processor discovery took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
        start = System.nanoTime();
        List<Future<?>> futures = new ArrayList<>(entityViews.size());
        start(executorService, entityViews, futures);
        await(futures);
        context.logMessage(Diagnostic.Kind.NOTE, "Annotation processor analysis took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
        start = System.nanoTime();
        int views = createMetaModelClasses(executorService);
        context.logMessage(Diagnostic.Kind.NOTE, "Annotation processor generation took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
        context.logMessage(Diagnostic.Kind.NOTE, "Annotation processor processed " + views + " entity views with " + threads + " threads and took overall " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - initTime) + "ms");
    }

    private void start(ExecutorService executorService, List<TypeElement> typeElements, List<Future<?>> futures) {
        for (TypeElement typeElement : typeElements) {
            futures.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    AnnotationMetaEntityView metaEntity = new AnnotationMetaEntityView(typeElement, context);
                    context.addMetaEntityViewToContext(metaEntity.getQualifiedName(), metaEntity);
                }
            }));
        }
    }

    private static void await(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void discoverEntityViews(List<TypeElement> entityViews, Collection<? extends Element> elements) {
        for (Element element : elements) {
            if (isEntityView(element)) {
                context.logMessage(Diagnostic.Kind.OTHER, "Processing annotated class " + element.toString());
                handleRootElementAnnotationMirrors(entityViews, element);
            }
            discoverEntityViews(entityViews, element.getEnclosedElements());
        }
    }

    private int createMetaModelClasses(ExecutorService executorService) {
        final ThreadLocal<StringBuilder> threadLocalStringBuilder = new ThreadLocal<StringBuilder>() {
            @Override
            protected StringBuilder initialValue() {
                return new StringBuilder(64 * 1024);
            }
        };
        LongAdder relationTime = new LongAdder();
        LongAdder multiRelationTime = new LongAdder();
        LongAdder metamodelTime = new LongAdder();
        LongAdder implementationTime = new LongAdder();
        LongAdder builderTime = new LongAdder();
        List<Future<?>> futures = new ArrayList<>();
        int views = 0;
        for (MetaEntityView entityView : context.getMetaEntityViews()) {
            if (!entityView.isValid()) {
                continue;
            }
            if (entityView.getTypeElement().getModifiers().contains(Modifier.ABSTRACT) || entityView.getTypeElement().getKind().isInterface()) {
                views++;
                futures.add(executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = threadLocalStringBuilder.get();
                        long start = System.nanoTime();
                        RelationClassWriter.writeFile(sb, entityView, context);
                        relationTime.add(System.nanoTime() - start);
                    }
                }));
                futures.add(executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = threadLocalStringBuilder.get();
                        long start = System.nanoTime();
                        MultiRelationClassWriter.writeFile(sb, entityView, context);
                        multiRelationTime.add(System.nanoTime() - start);
                    }
                }));
                futures.add(executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = threadLocalStringBuilder.get();
                        long start = System.nanoTime();
                        MetamodelClassWriter.writeFile(sb, entityView, context);
                        metamodelTime.add(System.nanoTime() - start);
                    }
                }));
                if (context.isGenerateImplementations()) {
                    futures.add(executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder sb = threadLocalStringBuilder.get();
                            long start = System.nanoTime();
                            ForeignPackageAdapterClassWriter.writeFiles(sb, entityView, context);
                            ImplementationClassWriter.writeFile(sb, entityView, context);
                            implementationTime.add(System.nanoTime() - start);
                        }
                    }));
                    if (context.isGenerateBuilders()) {
                        futures.add(executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = threadLocalStringBuilder.get();
                                long start = System.nanoTime();
                                BuilderClassWriter.writeFile(sb, entityView, context);
                                builderTime.add(System.nanoTime() - start);
                            }
                        }));
                    }
                }
            }
        }
        await(futures);
        context.logMessage(Diagnostic.Kind.NOTE, "Generating relation classes took overall " + TimeUnit.NANOSECONDS.toMillis(relationTime.sum()) + "ms");
        context.logMessage(Diagnostic.Kind.NOTE, "Generating multi relation classes took overall " + TimeUnit.NANOSECONDS.toMillis(multiRelationTime.sum()) + "ms");
        context.logMessage(Diagnostic.Kind.NOTE, "Generating metamodel classes took overall " + TimeUnit.NANOSECONDS.toMillis(metamodelTime.sum()) + "ms");
        if (context.isGenerateImplementations()) {
            context.logMessage(Diagnostic.Kind.NOTE, "Generating implementation classes took overall " + TimeUnit.NANOSECONDS.toMillis(implementationTime.sum()) + "ms");
        }
        if (context.isGenerateBuilders()) {
            context.logMessage(Diagnostic.Kind.NOTE, "Generating builder classes took overall " + TimeUnit.NANOSECONDS.toMillis(builderTime.sum()) + "ms");
        }
        return views;
    }

    private boolean isEntityView(Element element) {
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            return TypeUtils.containsAnnotation(element, Constants.ENTITY_VIEW);
        }
        return false;
    }

    private void handleRootElementAnnotationMirrors(List<TypeElement> entityViews, final Element element) {
        if (!ElementKind.CLASS.equals(element.getKind()) && !ElementKind.INTERFACE.equals(element.getKind())) {
            return;
        }
        TypeElement typeElement = (TypeElement) element;
        entityViews.add(typeElement);
        context.initializeEntityViewElement(typeElement);
    }
}
