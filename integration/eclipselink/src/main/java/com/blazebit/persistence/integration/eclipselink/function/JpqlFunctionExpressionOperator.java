/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.integration.eclipselink.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.expressions.ClassTypeExpression;
import org.eclipse.persistence.internal.expressions.CompoundExpression;
import org.eclipse.persistence.internal.expressions.ConstantExpression;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.FieldExpression;
import org.eclipse.persistence.internal.expressions.FromSubSelectExpression;
import org.eclipse.persistence.internal.expressions.FunctionExpression;
import org.eclipse.persistence.internal.expressions.ObjectExpression;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.expressions.QueryKeyExpression;
import org.eclipse.persistence.internal.expressions.SubSelectExpression;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.DatabaseMapping;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JpqlFunctionExpressionOperator extends ExpressionOperator {
    
    private static final long serialVersionUID = 1L;
    private static final Writer NULL_WRITER = new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    };
    
    private final JpqlFunction function;
    private final AbstractSession session;
    private final Map<Class<?>, String> classTypes;

    public JpqlFunctionExpressionOperator(JpqlFunction function, AbstractSession session, Map<Class<?>, String> classTypes2) {
        this.function = function;
        this.session = session;
        this.classTypes = classTypes2;
    }

    public JpqlFunction unwrap() {
        return function;
    }

    @Override
    public void printDuo(Expression first, Expression second, ExpressionSQLPrinter printer) {
        prepare(Arrays.asList(first, second), printer);
        super.printDuo(first, second, printer);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void printCollection(Vector items, ExpressionSQLPrinter printer) {
        prepare((List<Expression>) items, printer);
        // Certain functions don't allow binding on some platforms.
        if (printer.getPlatform().isDynamicSQLRequiredForFunctions() && !isBindingSupported()) {
            printer.getCall().setUsesBinding(false);
        }
        int dbStringIndex = 0;
        try {
            if (isPrefix()) {
                printer.getWriter().write(getDatabaseStrings()[0]);
                dbStringIndex = 1;
            } else {
                dbStringIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (argumentIndices == null) {
            argumentIndices = new int[items.size()];
            for (int i = 0; i < argumentIndices.length; i++) {
                argumentIndices[i] = i;
            }
        }

        for (int i = 0; i < argumentIndices.length; i++) {
            int index = argumentIndices[i];
            Expression item;
            if (index == -1) {
                item = (Expression)items.elementAt(i);
                Writer w = printer.getWriter();
                try {
                    printer.setWriter(NULL_WRITER);
                    item.printSQL(printer);
                } finally {
                    printer.setWriter(w);
                }
                continue;
            }
            item = (Expression)items.elementAt(index);
            if ((this.selector == Ref) || ((this.selector == Deref) && (item.isObjectExpression()))) {
                DatabaseTable alias = ((ObjectExpression)item).aliasForTable(((ObjectExpression)item).getDescriptor().getTables().firstElement());
                printer.printString(alias.getNameDelimited(printer.getPlatform()));
            } else if ((this.selector == Count) && (item.isExpressionBuilder())) {
                printer.printString("*");
            } else {
                item.printSQL(printer);
            }
            if (dbStringIndex < getDatabaseStrings().length) {
                printer.printString(getDatabaseStrings()[dbStringIndex++]);
            }
        }
        for (;dbStringIndex < getDatabaseStrings().length; dbStringIndex++) {
            printer.printString(getDatabaseStrings()[dbStringIndex]);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void prepare(List<Expression> items, ExpressionSQLPrinter printer) {
        EclipseLinkFunctionRenderContext context;
        // for eclipselink, we need to append one dummy argument for functions without any arguments
        // to make this transparent for the JpqlFunction implementation, we need to remove this dummy argument at this point
        if (function.hasArguments()) {
            context = new EclipseLinkFunctionRenderContext(items, session, printer);
        } else {
            if (items.size() > 1) {
                throw new IllegalStateException("Expected only one dummy argument for function [" + function.getClass() + "] but found " + items.size() + " arguments.");
            }
            context = new EclipseLinkFunctionRenderContext(Collections.<Expression>emptyList(), session, printer);
        }
        function.render(context);
        setArgumentIndices(context.getArgumentIndices());

        if (context.isChunkFirst()) {
            bePrefix();
        } else {
            bePostfix();
        }

        // add cast if necessary
        final Class<?> firstArgumentType;
        if (items.isEmpty()) {
            firstArgumentType = null;
        } else {
            firstArgumentType = getExpressionType(items.get(0));
        }
        final String returnTypeName = classTypes.get(function.getReturnType(firstArgumentType));
        if (returnTypeName != null) {
            final String[] castStrings = cast().getDatabaseStrings();
            final List<String> chunks = context.getChunks();
            if (chunks.isEmpty()) {
                context.addChunk(castStrings[0]);
                bePrefix();
            } else if (context.isChunkFirst()) {
                chunks.set(0, new StringBuilder(castStrings[0]).append(chunks.get(0)).toString());
            } else {
                chunks.add(0, castStrings[0]);
                bePrefix();
            }
            context.addChunk(castStrings[1]);
            context.addChunk(returnTypeName);
            context.addChunk(castStrings[2]);
        }

        printsAs(new Vector(context.getChunks()));
    }

    private Class<?> getExpressionType(Expression expression) {
        if (expression instanceof SubSelectExpression) {
            return getReturnTypeFromSubSelectExpression((SubSelectExpression) expression);
        } else if (expression instanceof ParameterExpression) {
            return (Class<?>) ((ParameterExpression) expression).getType();
        } else if (expression instanceof FunctionExpression) {
            return ((FunctionExpression) expression).getResultType();
        } else if (expression instanceof FieldExpression) {
            return ((FieldExpression) expression).getField().getType();
        } else if (expression instanceof FromSubSelectExpression) {
            return getExpressionType(((FromSubSelectExpression) expression).getSubSelect());
        } else if (expression instanceof QueryKeyExpression) {
            final DatabaseMapping mapping = ((QueryKeyExpression) expression).getMapping();
            return mapping == null ? null : mapping.getAttributeClassification();
        } else if (expression instanceof ClassTypeExpression) {
            return ((ClassTypeExpression) expression).getField().getType();
        } else if (expression instanceof ConstantExpression) {
            return ((ConstantExpression) expression).getValue().getClass();
        } else if (expression instanceof CompoundExpression) {
            return Boolean.class;
        } else {
            return null;
        }
    }

    private Class<?> getReturnTypeFromSubSelectExpression(SubSelectExpression subSelectExpression) {
        try {
            Field returnTypeField = SubSelectExpression.class.getDeclaredField("returnType");
            returnTypeField.setAccessible(true);
            return (Class<?>) returnTypeField.get(subSelectExpression);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
