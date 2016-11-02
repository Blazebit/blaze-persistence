/*
 * Copyright 2014 - 2016 Blazebit.
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
package com.blazebit.persistence.impl.eclipselink.function;

import com.blazebit.persistence.spi.JpqlFunction;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class JpqlFunctionExpressionOperator extends ExpressionOperator {
    
    private static final long serialVersionUID = 1L;
    
    private final JpqlFunction function;

    public JpqlFunctionExpressionOperator(JpqlFunction function) {
        this.function = function;
    }

    @Override
    public void printDuo(Expression first, Expression second, ExpressionSQLPrinter printer) {
        prepare(Arrays.asList(first, second));
        super.printDuo(first, second, printer);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void printCollection(Vector items, ExpressionSQLPrinter printer) {
        prepare((List<Expression>) items);
        super.printCollection(items, printer);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void prepare(List<Expression> items) {
        EclipseLinkFunctionRenderContext context = new EclipseLinkFunctionRenderContext(items);
        function.render(context);
        setArgumentIndices(context.getArgumentIndices());
        printsAs(new Vector(context.getChunks()));
        
        if (context.isChunkFirst()) {
            bePrefix();
        } else {
            bePostfix();
        }
    }
}
