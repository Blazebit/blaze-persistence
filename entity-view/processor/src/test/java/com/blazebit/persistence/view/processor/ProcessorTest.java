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

import com.blazebit.persistence.view.processor.model.AView;
import com.blazebit.persistence.view.processor.model.BView;
import com.blazebit.persistence.view.processor.model.sub.BaseView_com_blazebit_persistence_view_processor_model_BView;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ProcessorTest {

    @Test
    public void testInterface() {
        test(AView.class);
    }

    @Test
    public void testAbstractClass() {
        Compilation compilation = test(BView.class);
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile(BaseView_com_blazebit_persistence_view_processor_model_BView.class.getName())
                .hasSourceEquivalentTo(JavaFileObjects.forResource(BaseView_com_blazebit_persistence_view_processor_model_BView.class.getName().replace('.', '/') + ".java"));
    }

    private Compilation test(Class<?>... views) {
        Compiler compiler = Compiler.javac().withProcessors(new EntityViewAnnotationProcessor());
        JavaFileObject[] javaFileObjects = new JavaFileObject[views.length];
        for (int i = 0; i < views.length; i++) {
            javaFileObjects[i] = JavaFileObjects.forResource(views[i].getName().replace('.', '/') + ".java");
        }

        Compilation compilation = compiler.compile(javaFileObjects);
        CompilationSubject.assertThat(compilation).succeeded();

        for (int i = 0; i < views.length; i++) {
            CompilationSubject.assertThat(compilation)
                    .generatedSourceFile(views[i].getName() + "_")
                    .hasSourceEquivalentTo(JavaFileObjects.forResource(views[i].getName().replace('.', '/') + "_.java"));
            CompilationSubject.assertThat(compilation)
                    .generatedSourceFile(views[i].getName() + "Impl")
                    .hasSourceEquivalentTo(JavaFileObjects.forResource(views[i].getName().replace('.', '/') + "Impl.java"));
        }
        return compilation;
    }
}
