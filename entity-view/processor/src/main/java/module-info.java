/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.view.processor {
    requires transitive com.blazebit.persistence.core;
    requires transitive com.blazebit.persistence.view;
    requires static jdk.compiler;
    provides javax.annotation.processing.Processor with com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor;
    provides com.blazebit.persistence.view.processor.convert.TypeConverter with com.blazebit.persistence.view.processor.convert.OptionalTypeConverter;
    uses com.blazebit.persistence.view.processor.convert.TypeConverter;
}