/*
 * Copyright 2014 - 2019 Blazebit.
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

import javax.annotation.processing.FilerException;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class ClassWriterUtils {

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
    };

    private ClassWriterUtils() {
    }

    public static void writeFile(StringBuilder sb, String basePackage, String simpleName, ImportContext importContext, Context context) {
        try {
            FileObject fo = context.getProcessingEnvironment().getFiler().createSourceFile(
                    getFullyQualifiedClassName(basePackage, simpleName)
            );
            OutputStream os = fo.openOutputStream();
            PrintWriter pw = new PrintWriter(os);

            if (!basePackage.isEmpty()) {
                pw.println("package " + basePackage + ";");
                pw.println();
            }
            if (importContext != null) {
                pw.println(importContext.generateImports());
            }
            pw.println(sb);

            pw.flush();
            pw.close();
        } catch (FilerException filerEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem with Filer: " + filerEx.getMessage());
        } catch (IOException ioEx) {
            context.logMessage(Diagnostic.Kind.ERROR, "Problem opening file to write " + simpleName + ioEx.getMessage());
        }
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
        return "@SuppressWarnings({ \"deprecation\", \"rawtypes\" })";
    }
}
