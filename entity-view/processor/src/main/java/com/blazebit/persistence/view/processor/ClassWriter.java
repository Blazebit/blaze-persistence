/*
 * Copyright 2014 - 2023 Blazebit.
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

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public abstract class ClassWriter implements Runnable {

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
    };
    private static final ThreadLocal<StringBuilder> THREAD_LOCAL_STRING_BUILDER = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(64 * 1024);
        }
    };
    private final FileObject fileObject;
    private final MetaEntityView entity;
    private final ImportContext importContext;
    private final Context context;
    private final Collection<Runnable> mainThreadQueue;
    private final LongAdder elapsedTime;

    public ClassWriter(FileObject fileObject, MetaEntityView entity, ImportContext importContext, Context context, Collection<Runnable> mainThreadQueue, LongAdder elapsedTime) {
        this.fileObject = fileObject;
        this.entity = entity;
        this.importContext = importContext;
        this.context = context;
        this.mainThreadQueue = mainThreadQueue;
        this.elapsedTime = elapsedTime;
    }

    @Override
    public void run() {
        StringBuilder sb = THREAD_LOCAL_STRING_BUILDER.get();
        sb.setLength(0);
        long start = System.nanoTime();
        String basePackage = getPackageName();

        PrintWriter pw = null;
        try {
            OutputStream os = fileObject.openOutputStream();
            pw = new PrintWriter(os);

            if (!basePackage.isEmpty()) {
                pw.println("package " + basePackage + ";");
                pw.println();
            }
            generateBody(sb, entity, context);
            if (importContext != null) {
                pw.println(importContext.generateImports());
            }
            pw.println(sb);

            pw.flush();
        } catch (FilerException filerEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem with Filer: " + filerEx.getMessage());
        } catch (IOException ioEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem opening file to write [" + fileObject.getName() + "]: " + ioEx.getMessage());
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            context.logMessage(Diagnostic.Kind.ERROR, "Error during file generation [" + fileObject.getName() + "]: " + sw);
        }
        elapsedTime.add(System.nanoTime() - start);
        mainThreadQueue.add(new PrintWriterCloser(pw));
    }

    protected String getPackageName() {
        return entity.getPackageName();
    }

    protected abstract void generateBody(StringBuilder sb, MetaEntityView entity, Context context);

    public static FileObject createFile(String basePackage, String simpleName, Context context, Element... originatingElements) {
        Element[] elements;
        Filer filer = context.getProcessingEnvironment().getFiler();
        if (originatingElements.length > 0 && filer.getClass().getName().startsWith("org.gradle.api")) {
            // gradle filer only support single originating element for isolating processors
            elements = new Element[1];
            elements[0] = originatingElements[0];
        } else {
            // other compilers like the IntelliJ compiler support multiple
            elements = originatingElements;
        }
        String fullyQualifiedClassName = getFullyQualifiedClassName(basePackage, simpleName);
        try {
            return filer.createSourceFile(fullyQualifiedClassName, elements);
        } catch (FilerException filerEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem with Filer: " + filerEx.getMessage());
        } catch (IOException ioEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem opening file to write " + simpleName + ioEx.getMessage());
        }
        return null;
    }

    private static String getFullyQualifiedClassName(String metaModelPackage, String simpleName) {
        String fullyQualifiedClassName = "";
        if (!metaModelPackage.isEmpty()) {
            fullyQualifiedClassName = fullyQualifiedClassName + metaModelPackage + ".";
        }
        fullyQualifiedClassName = fullyQualifiedClassName + simpleName;
        return fullyQualifiedClassName;
    }

    public static String writeGeneratedAnnotation(ImportContext importContext, Context context) {
        StringBuilder generatedAnnotation = new StringBuilder();
        writeGeneratedAnnotation(generatedAnnotation, importContext, context);
        return generatedAnnotation.toString();
    }

    public static void writeGeneratedAnnotation(StringBuilder generatedAnnotation, ImportContext importContext, Context context) {
        generatedAnnotation.append("@");
        if (importContext == null) {
            generatedAnnotation.append(context.getGeneratedAnnotation().getQualifiedName().toString());
        } else {
            generatedAnnotation.append(importContext.importType(context.getGeneratedAnnotation().getQualifiedName().toString()));
        }
        generatedAnnotation.append("(value = \"")
            .append(EntityViewAnnotationProcessor.class.getName());
        if (context.addGeneratedDate()) {
            generatedAnnotation.append("\", date = \"")
                .append(SIMPLE_DATE_FORMAT.get().format(new Date()))
                .append("\")");
        } else {
            generatedAnnotation.append("\")");
        }
    }

    public static String writeSuppressWarnings() {
        return "@SuppressWarnings({ \"deprecation\", \"rawtypes\", \"unchecked\" })";
    }
}
