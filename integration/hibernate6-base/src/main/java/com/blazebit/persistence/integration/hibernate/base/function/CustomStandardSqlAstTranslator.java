/*
 * Copyright 2014 - 2022 Blazebit.
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
import org.hibernate.sql.ast.spi.StandardSqlAstTranslator;
import org.hibernate.sql.exec.spi.JdbcOperation;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class CustomStandardSqlAstTranslator<T extends JdbcOperation> extends StandardSqlAstTranslator<T> {

    private final StringBuilder sb = new StringBuilder();

    public CustomStandardSqlAstTranslator(SessionFactoryImplementor sessionFactory) {
        super(sessionFactory, null);
    }

    @Override
    public String getSql() {
        return sb.toString();
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }

    @Override
    public void appendSql(String fragment) {
        sb.append(fragment);
    }

    @Override
    public void appendSql(char fragment) {
        sb.append(fragment);
    }

    @Override
    public void appendSql(int value) {
        sb.append(value);
    }

    @Override
    public void appendSql(long value) {
        sb.append(value);
    }

    @Override
    public void appendSql(boolean value) {
        sb.append(value);
    }

    @Override
    public Appendable append(CharSequence csq) {
        sb.append(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public Appendable append(char c) {
        sb.append(c);
        return this;
    }
}
