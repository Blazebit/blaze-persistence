/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionKind;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.metamodel.model.domain.DomainType;
import org.hibernate.metamodel.model.domain.PersistentAttribute;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.produce.function.StandardFunctionArgumentTypeResolvers;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.AbstractSqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateJpqlFunctionAdapter extends AbstractSqmSelfRenderingFunctionDescriptor {

    private final JpqlFunction function;

    public HibernateJpqlFunctionAdapter(SessionFactoryImplementor sfi, JpqlFunctionKind kind, JpqlFunction function) {
        super(null, determineFunctionKind(kind), null, new FunctionReturnTypeResolver() {
            public ReturnableType<?> resolveFunctionReturnType(
                    ReturnableType<?> impliedType,
                    List<? extends SqmTypedNode<?>> arguments,
                    TypeConfiguration typeConfiguration) {
                return resolveFunctionReturnType(impliedType, (Supplier<MappingModelExpressible<?>>) null, arguments, typeConfiguration);
            }

            public ReturnableType<?> resolveFunctionReturnType(
                    ReturnableType<?> impliedType,
                    Supplier<MappingModelExpressible<?>> inferredTypeSupplier,
                    List<? extends SqmTypedNode<?>> arguments,
                    TypeConfiguration typeConfiguration) {
                Class<?> argumentClass = null;
                if (arguments.isEmpty()) {
                    Class<?> returnType = function.getReturnType(null);
                    return returnType == null ? null : typeConfiguration.getBasicTypeForJavaType(returnType);
                } else {
                    final SqmTypedNode<?> specifiedArgument = arguments.get(0);
                    argumentClass = specifiedArgument.getNodeJavaType() == null ? null : specifiedArgument.getNodeJavaType().getJavaTypeClass();
                    Class<?> returnType = function.getReturnType(argumentClass);
                    if (returnType == null) {
                        return null;
                    } else if (returnType == argumentClass) {
                        SqmExpressible<?> nodeType = specifiedArgument.getNodeType();
                        if (nodeType instanceof PersistentAttribute<?, ?>) {
                            DomainType<?> type = ((PersistentAttribute<?, ?>) nodeType).getValueGraphType();
                            if (type instanceof ReturnableType<?>) {
                                return (ReturnableType<?>) type;
                            }
                            return null;
                        }
                        if (nodeType instanceof ReturnableType<?>) {
                            return (ReturnableType<?>) nodeType;
                        }
                        return null;
                    }
                    return typeConfiguration.getBasicTypeForJavaType(returnType);
                }
            }

            @Override
            public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
                final SqlAstNode specifiedArgument = arguments.get(0);
                final JdbcMappingContainer specifiedArgType = specifiedArgument instanceof Expression
                        ? ( (Expression) specifiedArgument ).getExpressionType()
                        : null;

                Class<?> argumentClass;
                if ( specifiedArgType instanceof BasicValuedMapping ) {
                    argumentClass = ((BasicValuedMapping) specifiedArgType).getExpressibleJavaType().getJavaTypeClass();
                } else {
                    argumentClass = null;
                }

                Class<?> returnType = function.getReturnType(argumentClass);

                if (returnType == null) {
                    return null;
                } else if (argumentClass == returnType) {
                    return (BasicValuedMapping) specifiedArgType;
                }

                return sfi.getTypeConfiguration().getBasicTypeForJavaType(returnType);
            }
            // I hate to do this, but I have to hardcode this for now
        }, "com.blazebit.persistence.impl.function.entity.EntityFunction".equals(function.getClass().getName()) ? StandardFunctionArgumentTypeResolvers.IMPLIED_RESULT_TYPE : null);
        this.function = function;
    }

    private static FunctionKind determineFunctionKind(JpqlFunctionKind kind) {
        switch (kind) {
            case WINDOW:
                return FunctionKind.WINDOW;
            case ORDERED_SET_AGGREGATE:
                return FunctionKind.ORDERED_SET_AGGREGATE;
            case AGGREGATE:
                return FunctionKind.AGGREGATE;
            default:
                return FunctionKind.NORMAL;
        }
    }

    public JpqlFunction unwrap() {
        return function;
    }

    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> walker) {
        render( sqlAppender, sqlAstArguments, null, (ReturnableType<?>) null, walker);
    }

    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> sqlAstArguments,
            Predicate filter,
            SqlAstTranslator<?> walker) {
        render( sqlAppender, sqlAstArguments, filter, (ReturnableType<?>) null, walker );
    }

    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> sqlAstArguments,
            ReturnableType<?> returnableType,
            SqlAstTranslator<?> sqlAstTranslator) {
        render( sqlAppender, sqlAstArguments, null, returnableType, sqlAstTranslator );
    }

    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> sqlAstArguments,
            Predicate filter,
            ReturnableType<?> returnableType,
            SqlAstTranslator<?> sqlAstTranslator) {
        final StringBuilder sqlBuffer = ((AbstractSqlAstTranslator<?>) sqlAstTranslator).getSqlBuffer();
        List<String> sqlArguments = new ArrayList<>(sqlAstArguments.size());
        int startLength = sqlBuffer.length();
        for (SqlAstNode sqlAstArgument : sqlAstArguments) {
            sqlBuffer.setLength(startLength);
            sqlAstTranslator.render(sqlAstArgument, SqlAstNodeRenderingMode.DEFAULT);
            sqlArguments.add(sqlBuffer.substring(startLength));
        }
        if (filter != null) {
            sqlArguments.add("'FILTER'");
            sqlBuffer.setLength(startLength);
            sqlAstTranslator.render(filter, SqlAstNodeRenderingMode.DEFAULT);
            sqlArguments.add(sqlBuffer.substring(startLength));
        }

        sqlBuffer.setLength(startLength);

        HibernateFunctionRenderContext context = new HibernateFunctionRenderContext(sqlArguments);
        function.render(context);
        sqlAppender.appendSql(context.renderToString());
    }

}
