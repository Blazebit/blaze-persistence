/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.SqlExpressible;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmBindableType;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.function.AbstractSqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.sql.SqmToSqlAstConverter;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmRenderContext;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.descriptor.java.JavaType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateSqmFunctionDescriptorAdapter implements JpqlFunction {

    private final SessionFactoryImplementor sfi;
    private final SqmFunctionDescriptor function;

    public HibernateSqmFunctionDescriptorAdapter(SessionFactoryImplementor sfi, SqmFunctionDescriptor function) {
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
        SqmBindableType<?> type = sfi.getTypeConfiguration().getBasicTypeForJavaType(firstArgumentType);

        if (type == null) {
            final JavaType<Object> javaType = sfi.getTypeConfiguration().getJavaTypeRegistry().getDescriptor(firstArgumentType);
            type = sfi.getTypeConfiguration().getBasicTypeRegistry().resolve(
                    javaType,
                    javaType.getRecommendedJdbcType( sfi.getTypeConfiguration().getCurrentBaseSqlTypeIndicators() )
            );
        }

        List<SqmTypedNode<?>> arguments = new ArrayList<>(1);
        arguments.add(new CustomSqmTypedNode<>(type));
        if ( function instanceof AbstractSqmFunctionDescriptor ) {
            FunctionReturnTypeResolver returnTypeResolver = ((AbstractSqmFunctionDescriptor) function).getReturnTypeResolver();
            ReturnableType<?> returnableType = returnTypeResolver.resolveFunctionReturnType(
                    null,
                    (SqmToSqlAstConverter) null,
                    arguments,
                    sfi.getTypeConfiguration()
            );
            if (returnableType != null) {
                return returnableType.getJavaType();
            }
        }
        SqmExpressible<?> expressionType = function.generateSqmExpression( arguments, null, sfi.getQueryEngine() ).getNodeType();
        return expressionType == null ? null : expressionType.getExpressibleJavaType().getJavaTypeClass();
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
    private static class CustomSqmTypedNode<T> implements SqmTypedNode<T>, SqmVisitableNode {

        private final SqmBindableType<T> type;

        private CustomSqmTypedNode(SqmBindableType<T> type) {
            this.type = type;
        }

        public <X> X accept(SemanticQueryWalker<X> walker) {
            //noinspection unchecked
            return (X) new QueryLiteral<>( null, (SqlExpressible) type );
        }

        @Override
        public SqmTypedNode<T> copy(SqmCopyContext context) {
            return this;
        }

        @Override
        public SqmBindableType<T> getNodeType() {
            return type;
        }

        @Override
        public NodeBuilder nodeBuilder() {
            return null;
        }

        @Override
        public void appendHqlString(StringBuilder sb, SqmRenderContext  renderContext) {
            // No-op
        }

        public boolean isCompatible(Object o) {
            return false;
        }

        public int cacheHashCode() {
            return 0;
        }
    }

}
