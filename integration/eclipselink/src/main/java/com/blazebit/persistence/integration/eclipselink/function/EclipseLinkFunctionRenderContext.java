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

import com.blazebit.persistence.spi.FunctionRenderContext;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.SQLCall;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EclipseLinkFunctionRenderContext implements FunctionRenderContext {
    
    private final List<String> chunks = new ArrayList<String>();
    private final int[] argumentIndices;
    private final List<Expression> arguments;

    private final DatasourceCallMock datasourceCallMock = new DatasourceCallMock();
    private final ExpressionSQLPrinter expressionSQLPrinter;

    private final String[] argumentStrings;
    private int currentIndex;
    private Boolean chunkFirst;

    public EclipseLinkFunctionRenderContext(List<Expression> arguments, AbstractSession session, ExpressionSQLPrinter printer) {
        this.argumentIndices = new int[arguments.size()];
        Arrays.fill(this.argumentIndices, -1);
        this.arguments = arguments;
        this.argumentStrings = new String[arguments.size()];
        // Since there are no public getters we can use to extract the translation row or whether we should print qualified names, we render dummy expressions
        Writer oldWriter = printer.getWriter();
        try {
            StringWriter stringWriter = new StringWriter();
            printer.setWriter(stringWriter);
            printer.printField(new DatabaseField("x.x"));
            boolean printQualifiedNames = stringWriter.getBuffer().indexOf(".") != -1;
            ParameterExpressionMock parameterExpressionMock = new ParameterExpressionMock();
            parameterExpressionMock.printSQL(printer);
            List<?> parameters = printer.getCall().getParameters();
            parameters.remove(parameters.size() - 1);
            List<?> parameterTypes = printer.getCall().getParameterTypes();
            parameterTypes.remove(parameterTypes.size() - 1);
            stringWriter.getBuffer().setLength(0);
            expressionSQLPrinter = new ExpressionSQLPrinter(session, parameterExpressionMock.translationRow, datasourceCallMock, printQualifiedNames, new ExpressionBuilder());
            expressionSQLPrinter.setWriter(stringWriter);
        } finally {
            printer.setWriter(oldWriter);
        }
    }

    @Override
    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public String getArgument(int index) {
        String argumentString = argumentStrings[index];
        if (argumentString == null) {
            StringWriter writer = (StringWriter) expressionSQLPrinter.getWriter();
            writer.getBuffer().setLength(0);
            arguments.get(index).printSQL(expressionSQLPrinter);
            argumentStrings[index] = argumentString = writer.toString();
        }

        return argumentString;
    }

    @Override
    public void addArgument(int index) {
        if (chunkFirst == null) {
            chunkFirst = false;
        }
        argumentIndices[currentIndex++] = index;
    }

    @Override
    public void addChunk(String chunk) {
        if (chunkFirst == null) {
            chunkFirst = true;
        }
        chunks.add(chunk);
    }
    
    public boolean isChunkFirst() {
        return chunkFirst;
    }
    
    public List<String> getChunks() {
        return chunks;
    }
    
    public int[] getArgumentIndices() {
        return argumentIndices;
    }

    /**
     * Avoids adding parameters prematurely.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class DatasourceCallMock extends SQLCall {

        @Override
        public void appendLiteral(Writer writer, Object literal) {
            try {
                writer.write(argumentMarker());
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        }
    }

    /**
     * Captures the translation row.
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class ParameterExpressionMock extends ParameterExpression {
        AbstractRecord translationRow;

        public ParameterExpressionMock() {
            super(new DatabaseField());
        }

        @Override
        public Object getValue(AbstractRecord translationRow, AbstractSession session) {
            this.translationRow = translationRow;
            return null;
        }
    }

}
