/*
 * Copyright 2014 - 2024 Blazebit.
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
import org.hibernate.metamodel.mapping.SqlExpressible;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.function.AbstractSqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.spi.TypeConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateSqmFunctionDescriptorAdapter implements JpqlFunction {

    private static final Method GENERATE_SQM_EXPRESSION;
    private static final Method RESOLVE_FUNCTION_RETURN_TYPE;
    private static final Constructor<QueryLiteral> QUERY_LITERAL_CONSTRUCTOR;

    static {
        Method generateSqmExpression;
        try {
            generateSqmExpression = SqmFunctionDescriptor.class.getMethod("generateSqmExpression", List.class, ReturnableType.class, QueryEngine.class);
        } catch (NoSuchMethodException e1) {
            try {
                generateSqmExpression = SqmFunctionDescriptor.class.getMethod("generateSqmExpression", List.class, ReturnableType.class, QueryEngine.class, TypeConfiguration.class);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException("Could not find method to generate SQM expression for functions. Please report your version of hibernate so we can provide support for it!", e1);
            }
        }
        GENERATE_SQM_EXPRESSION = generateSqmExpression;
        Method resolveFunctionReturnType;
        try {
            resolveFunctionReturnType = FunctionReturnTypeResolver.class.getMethod("resolveFunctionReturnType", ReturnableType.class, Supplier.class, List.class, TypeConfiguration.class);
        } catch (NoSuchMethodException e1) {
            try {
                resolveFunctionReturnType = FunctionReturnTypeResolver.class.getMethod("resolveFunctionReturnType", ReturnableType.class, List.class, TypeConfiguration.class);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException("Could not find method to resolve function return type. Please report your version of hibernate so we can provide support for it!", e1);
            }
        }
        RESOLVE_FUNCTION_RETURN_TYPE = resolveFunctionReturnType;
        Constructor<QueryLiteral> queryLiteralConstructor;
        try {
            queryLiteralConstructor = QueryLiteral.class.getConstructor(Object.class, SqlExpressible.class);
        } catch (NoSuchMethodException e1) {
            try {
                queryLiteralConstructor = QueryLiteral.class.getConstructor(Object.class, BasicValuedMapping.class);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException("Could not find constructor for QueryLiteral. Please report your version of hibernate so we can provide support for it!", e1);
            }
        }
        QUERY_LITERAL_CONSTRUCTOR = queryLiteralConstructor;
    }

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
        SqmExpressible<?> type = sfi.getTypeConfiguration().getBasicTypeForJavaType(firstArgumentType);

        if (type == null) {
            final JavaType<Object> javaType = sfi.getTypeConfiguration().getJavaTypeRegistry().getDescriptor(firstArgumentType);
            type = new ReturnableType<Object>() {
                @Override
                public JavaType<Object> getExpressibleJavaType() {
                    return javaType;
                }

                @Override
                public Class<Object> getBindableJavaType() {
                    return (Class<Object>) firstArgumentType;
                }

                @Override
                public PersistenceType getPersistenceType() {
                    return PersistenceType.BASIC;
                }

                @Override
                public Class<Object> getJavaType() {
                    return (Class<Object>) firstArgumentType;
                }
            };
        }

        List<SqmTypedNode<?>> arguments = new ArrayList<>(1);
        arguments.add(new CustomSqmTypedNode<>(type));
        if ( function instanceof AbstractSqmFunctionDescriptor ) {
            try {
                FunctionReturnTypeResolver returnTypeResolver = ((AbstractSqmFunctionDescriptor) function).getReturnTypeResolver();
                ReturnableType<?> returnableType;
                if (RESOLVE_FUNCTION_RETURN_TYPE.getParameterCount() == 4) {
                    returnableType = (ReturnableType<?>) RESOLVE_FUNCTION_RETURN_TYPE.invoke(
                            returnTypeResolver,
                            null,
                            null,
                            arguments,
                            sfi.getTypeConfiguration()
                    );
                } else {
                    returnableType = (ReturnableType<?>) RESOLVE_FUNCTION_RETURN_TYPE.invoke(
                            returnTypeResolver,
                            null,
                            arguments,
                            sfi.getTypeConfiguration()
                    );
                }
                if (returnableType != null) {
                    return returnableType.getBindableJavaType();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not resolve function return type. Please report your version of hibernate so we can provide support for it!", e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                }
                throw new RuntimeException("Could not resolve function return type", e);
            }
        }
        try {
            SqmExpressible<?> expressionType;
            if (GENERATE_SQM_EXPRESSION.getParameterCount() == 4) {
                expressionType = ((SelfRenderingSqmFunction<?>) GENERATE_SQM_EXPRESSION.invoke(function, arguments, null, sfi.getQueryEngine(), sfi.getTypeConfiguration())).getNodeType();
            } else {
                expressionType = ((SelfRenderingSqmFunction<?>) GENERATE_SQM_EXPRESSION.invoke(function, arguments, null, sfi.getQueryEngine())).getNodeType();
            }
            return expressionType == null ? null : expressionType.getBindableJavaType();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not generate SQM expression for function. Please report your version of hibernate so we can provide support for it!", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
            throw new RuntimeException("Could not generate SQM expression", e);
        }

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

        private final SqmExpressible<T> type;

        private CustomSqmTypedNode(SqmExpressible<T> type) {
            this.type = type;
        }

        public <X> X accept(SemanticQueryWalker<X> walker) {
            try {
                return (X) QUERY_LITERAL_CONSTRUCTOR.newInstance(null, type);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SqmTypedNode<T> copy(SqmCopyContext context) {
            return this;
        }

        @Override
        public SqmExpressible<T> getNodeType() {
            return type;
        }

        @Override
        public NodeBuilder nodeBuilder() {
            return null;
        }

        @Override
        public void appendHqlString(StringBuilder sb) {
            // No-op
        }
    }

}
