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

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.metamodel.mapping.MappingModelExpressable;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmExpressable;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.BasicType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class HibernateSQLFunctionAdapter implements JpqlFunction {

    private final SessionFactoryImplementor sfi;
    private final SqmFunctionDescriptor function;

    public HibernateSQLFunctionAdapter(SessionFactoryImplementor sfi, SqmFunctionDescriptor function) {
        this.sfi = sfi;
        this.function = function;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        if (firstArgumentType == null) {
            return null;
        }
        BasicType<?> type = sfi.getTypeConfiguration().getBasicTypeForJavaType(firstArgumentType);

        if (type != null) {
            List<SqmTypedNode<?>> arguments = new ArrayList<>(1);
            arguments.add(new CustomSqmTypedNode(type));
            MappingModelExpressable expressionType = function.generateSqmExpression(arguments, null, null, null)
                    .convertToSqlAst(null)
                    .getExpressionType();

            if ( expressionType instanceof BasicValuedMapping) {
                return ((BasicValuedMapping) expressionType).getBasicType().getReturnedClass();
            } else {
                return null;
            }
        }

        return null;
    }

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class CustomSqmTypedNode implements SqmTypedNode, SqmVisitableNode {

        private final BasicType<?> type;

        private CustomSqmTypedNode(BasicType<?> type) {
            this.type = type;
        }

        public <X> X accept(SemanticQueryWalker<X> walker) {
            return (X) new QueryLiteral(null, type);
        }

        @Override
        public SqmExpressable getNodeType() {
            return type;
        }

        @Override
        public NodeBuilder nodeBuilder() {
            return null;
        }
    }

}
