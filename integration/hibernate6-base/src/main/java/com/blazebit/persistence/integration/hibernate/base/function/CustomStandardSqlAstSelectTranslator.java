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

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.spi.StandardSqlAstSelectTranslator;
import org.hibernate.sql.ast.tree.expression.CaseSearchedExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CustomStandardSqlAstSelectTranslator extends StandardSqlAstSelectTranslator implements SqlAppender {

    private final StringBuilder sb;

    public CustomStandardSqlAstSelectTranslator(SessionFactoryImplementor sessionFactory) {
        super(sessionFactory);
        this.sb = new StringBuilder();
    }

    @Override
    protected SqlAppender getSqlAppender() {
        return this;
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }

    @Override
    public String getSql() {
        return sb.toString();
    }

    @Override
    public void appendSql(char fragment) {
        sb.append(fragment);
    }

    @Override
    public void appendSql(String fragment) {
        sb.append(fragment);
    }

    @Override
    public void visitCaseSearchedExpression(CaseSearchedExpression caseSearchedExpression) {
        getDialect().getCaseExpressionWalker().visitCaseSearchedExpression( caseSearchedExpression, sb, this );
    }
}
