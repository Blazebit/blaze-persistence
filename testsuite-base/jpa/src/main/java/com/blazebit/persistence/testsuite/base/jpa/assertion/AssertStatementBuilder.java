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

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import com.blazebit.persistence.testsuite.base.jpa.RelationalModelAccessor;
import org.junit.Assert;
import org.opentest4j.MultipleFailuresError;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertStatementBuilder {

    protected final RelationalModelAccessor relationalModelAccessor;
    protected final List<String> queries;
    protected final List<AssertStatement> statements = new ArrayList<>();
    protected Object currentBuilder;
    protected boolean validated;

    public AssertStatementBuilder(RelationalModelAccessor relationalModelAccessor, List<String> queries) {
        this.relationalModelAccessor = relationalModelAccessor;
        this.queries = queries;
    }

    void setCurrentBuilder(Object currentBuilder) {
        if (this.currentBuilder != null) {
            throw new IllegalStateException("Unfinished builder: " + this.currentBuilder);
        }
        this.currentBuilder = currentBuilder;
    }

    void unsetCurrentBuilder(Object currentBuilder) {
        if (this.currentBuilder == null) {
            throw new IllegalStateException("Unregistered builder: " + currentBuilder);
        }
        if (this.currentBuilder != currentBuilder) {
            throw new IllegalStateException("Wrong builder unset: " + currentBuilder);
        }
        this.currentBuilder = null;
    }

    void validateBuilderEnded() {
        if (this.currentBuilder != null) {
            throw new IllegalStateException("Unfinished builder: " + currentBuilder);
        }
    }

    public AssertMultiStatementBuilder unordered() {
        failIfValidated();
        return new AssertMultiStatementBuilder(this, relationalModelAccessor);
    }

    public AssertStatementBuilder select() {
        return assertSelect().and();
    }

    public AssertStatementBuilder select(Class<?> entityClass) {
        return assertSelect()
                .forEntity(entityClass)
                .and();
    }

    public AssertSelectStatementBuilder assertSelect() {
        failIfValidated();
        return new AssertSelectStatementBuilder(this, relationalModelAccessor);
    }

    public AssertStatementBuilder insert() {
        return assertInsert().and();
    }

    public AssertStatementBuilder insert(Class<?> entityClass) {
        return assertInsert()
                .forEntity(entityClass)
                .and();
    }

    public AssertInsertStatementBuilder assertInsert() {
        failIfValidated();
        return new AssertInsertStatementBuilder(this, relationalModelAccessor);
    }

    public AssertStatementBuilder update() {
        return assertUpdate().and();
    }

    public AssertStatementBuilder update(Class<?> entityClass) {
        return assertUpdate()
                .forEntity(entityClass)
                .and();
    }

    public AssertUpdateStatementBuilder assertUpdate() {
        failIfValidated();
        return new AssertUpdateStatementBuilder(this, relationalModelAccessor);
    }

    public AssertStatementBuilder delete() {
        return assertDelete().and();
    }

    public AssertStatementBuilder delete(Class<?> entityClass) {
        return assertDelete()
                .forEntity(entityClass)
                .and();
    }

    public AssertDeleteStatementBuilder assertDelete() {
        failIfValidated();
        return new AssertDeleteStatementBuilder(this, relationalModelAccessor);
    }

    protected void failIfValidated() {
        if (validated) {
            throw new AssertionError("Already validated!");
        }
    }

    void addStatement(AssertStatement statement) {
        statements.add(statement);
    }

    public void validate() {
        failIfValidated();
        validated = true;
        if (statements.size() != queries.size()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected query count for queries:");
            for (String q : queries) {
                sb.append("\n").append(q);
            }
            Assert.assertEquals(
                    sb.toString(),
                    statements.size(),
                    queries.size());
        }
        List<Throwable> failures = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            try {
                statements.get(i).validate(queries.get(i));
            } catch (Throwable t) {
                if (t instanceof MultipleFailuresError) {
                    failures.addAll(((MultipleFailuresError) t).getFailures());
                } else {
                    failures.add(t);
                }
            }
        }

        if (!failures.isEmpty()) {
            throw new MultipleFailuresError("Multiple query validations failed", failures);
        }
    }
}
