/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.core.parser {
    requires java.sql;
    requires jakarta.persistence;
    requires com.blazebit.common.utils;
    requires org.antlr.antlr4.runtime;
    exports com.blazebit.persistence.parser;
    exports com.blazebit.persistence.parser.expression;
    exports com.blazebit.persistence.parser.expression.modifier;
    exports com.blazebit.persistence.parser.predicate;
    exports com.blazebit.persistence.parser.util;
    uses com.blazebit.persistence.parser.util.TypeConverterContributor;
}