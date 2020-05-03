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

import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.metamodel.mapping.MappingModelExpressable;
import org.hibernate.metamodel.model.domain.AllowableFunctionReturnType;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionRenderingSupport;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.SqlAstWalker;
import org.hibernate.sql.ast.spi.AbstractSqlAstWalker;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class HibernateJpqlFunctionAdapter extends AbstractSqmSelfRenderingFunctionDescriptor implements FunctionRenderingSupport {

    private final JpqlFunction function;

    public HibernateJpqlFunctionAdapter(SessionFactoryImplementor sfi, JpqlFunction function) {
        super(null, null, new FunctionReturnTypeResolver() {
            @Override
            public AllowableFunctionReturnType<?> resolveFunctionReturnType(AllowableFunctionReturnType<?> impliedType, List<SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
                Class<?> argumentClass = null;
                if (!arguments.isEmpty()) {
                    final SqmTypedNode<?> specifiedArgument = arguments.get(0);
                    argumentClass = specifiedArgument.getNodeJavaTypeDescriptor().getJavaType();
                }
                Class<?> returnType = function.getReturnType(argumentClass);
                return returnType == null ? null : typeConfiguration.getBasicTypeForJavaType(returnType);
            }

            @Override
            public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
                final SqlAstNode specifiedArgument = arguments.get(0);
                final MappingModelExpressable specifiedArgType = specifiedArgument instanceof Expression
                        ? ( (Expression) specifiedArgument ).getExpressionType()
                        : null;

                Class<?> argumentClass;
                if ( specifiedArgType instanceof BasicValuedMapping ) {
                    argumentClass = ((BasicValuedMapping) specifiedArgType).getBasicType().getReturnedClass();
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
        });
        this.function = function;
    }

    public JpqlFunction unwrap() {
        return function;
    }

    @Override
    public void render(SqlAppender sqlAppender, List<SqlAstNode> sqlAstArguments, SqlAstWalker walker) {
        AbstractSqlAstWalker sqlAstWalker = (AbstractSqlAstWalker) walker;
        CustomStandardSqlAstSelectTranslator customStandardSqlAstSelectTranslator = new CustomStandardSqlAstSelectTranslator(sqlAstWalker.getSessionFactory());
        List<String> sqlArguments = new ArrayList<>(sqlAstArguments.size());
        for (SqlAstNode sqlAstArgument : sqlAstArguments) {
            customStandardSqlAstSelectTranslator.getStringBuilder().setLength(0);
            if (sqlAstArgument instanceof QueryLiteral<?>) {
                QueryLiteral<?> literal = (QueryLiteral<?>) sqlAstArgument;
                sqlArguments.add(
                        literal.getJdbcMapping()
                            .getSqlTypeDescriptor()
                            .getJdbcLiteralFormatter(literal.getJdbcMapping().getJavaTypeDescriptor())
                            .toJdbcLiteral(literal.getLiteralValue(), sqlAstWalker.getDialect(), null)
                );
            } else {
                sqlAstArgument.accept(customStandardSqlAstSelectTranslator);
                sqlArguments.add(customStandardSqlAstSelectTranslator.getSql());
            }
        }

        sqlAstWalker.getParameterBinders().addAll(customStandardSqlAstSelectTranslator.getParameterBinders());
        HibernateFunctionRenderContext context = new HibernateFunctionRenderContext(sqlArguments);
        function.render(context);
        sqlAppender.appendSql(context.renderToString());
    }

}
