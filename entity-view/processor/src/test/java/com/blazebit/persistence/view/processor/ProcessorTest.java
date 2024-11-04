/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import com.blazebit.persistence.view.processor.model.AView;
import com.blazebit.persistence.view.processor.model.BView;
import com.blazebit.persistence.view.processor.model.BViewImpl;
import com.blazebit.persistence.view.processor.model.sub.BaseView_com_blazebit_persistence_view_processor_model_BView;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Assert;
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
    public void testAbstractClass() throws Exception {
        Compilation compilation = test(BView.class);
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile(BaseView_com_blazebit_persistence_view_processor_model_BView.class.getName())
                .hasSourceEquivalentTo(JavaFileObjects.forResource(BaseView_com_blazebit_persistence_view_processor_model_BView.class.getName().replace('.', '/') + ".java"));
        BView obj = BViewImpl.class.getConstructor(BViewImpl.class, int.class, Object[].class, BView.class).newInstance(null, 0, new Object[]{ 1, "Test", 1}, null);
        Assert.assertEquals(1, obj.getId());
        Assert.assertEquals("Test", obj.getName());
        Assert.assertEquals("Test", obj.getCapturedName());
        Assert.assertEquals("Test", obj.getPostLoadName());
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
            CompilationSubject.assertThat(compilation)
                    .generatedSourceFile(views[i].getName() + "Builder")
                    .hasSourceEquivalentTo(JavaFileObjects.forResource(views[i].getName().replace('.', '/') + "Builder.java"));
            CompilationSubject.assertThat(compilation)
                .generatedSourceFile(views[i].getName() + "Relation")
                .hasSourceEquivalentTo(JavaFileObjects.forResource(views[i].getName().replace('.', '/') + "Relation.java"));
            CompilationSubject.assertThat(compilation)
                .generatedSourceFile(views[i].getName() + "MultiRelation")
                .hasSourceEquivalentTo(JavaFileObjects.forResource(views[i].getName().replace('.', '/') + "MultiRelation.java"));
        }
        return compilation;
    }
}
