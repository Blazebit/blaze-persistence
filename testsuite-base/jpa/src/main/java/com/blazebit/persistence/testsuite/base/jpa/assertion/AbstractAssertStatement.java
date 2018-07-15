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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractAssertStatement implements AssertStatement {

    protected final List<String> tables;

    public AbstractAssertStatement(List<String> tables) {
        this.tables = tables;
    }

    protected void validateTables(String query) {
        query = query.toLowerCase();

        if (!tables.isEmpty()) {
            List<String> fromElements = getFromElements(query);

            List<String> missingFromElements = new ArrayList<>(tables);
            missingFromElements.removeAll(fromElements);
            fromElements.removeAll(tables);

            Assert.assertTrue(
                    "Expected from elements don't match. Missing " + missingFromElements + ", Unexpected " + fromElements + "\nQuery: " + query,
                    fromElements.isEmpty() && missingFromElements.isEmpty()
            );
        }
    }

    protected static String stripReturningClause(String query) {
        int returningIndex = query.lastIndexOf(" returning ");
        if (returningIndex != -1) {
            query = query.substring(0, returningIndex);
        }
        int outputIndex = query.lastIndexOf(" output ");
        if (outputIndex != -1) {
            query = query.substring(0, outputIndex) + query.substring(query.indexOf(" where "));
        }
        String oldTableStateQualifier = " from old table (";
        int tableStateIndex = query.lastIndexOf(oldTableStateQualifier);
        if (tableStateIndex != -1) {
            query = query.substring(tableStateIndex + oldTableStateQualifier.length(), query.length() - 1);
        }
        String newTableStateQualifier = " from final table (";
        tableStateIndex = query.lastIndexOf(newTableStateQualifier);
        if (tableStateIndex != -1) {
            query = query.substring(tableStateIndex + newTableStateQualifier.length(), query.length() - 1);
        }
        return query;
    }

    protected List<String> getFromElements(String query) {
        try {
            final Statement statement = CCJSqlParserUtil.parse(query);
            List<Table> tables = getTables(statement);
            return tableNames(tables);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> tableNames(Collection<Table> tables) {
        List<String> tableNames = new ArrayList<>(tables.size());
        for (Table t : tables) {
            tableNames.add(t.getName());
        }
        return tableNames;
    }

    protected List<String> getFetchedFromElements(String query) {
        try {
            Statement statement = CCJSqlParserUtil.parse(query);
            final List<Table> tables = getTables(statement);
            final Set<Table> fetchedTables = new HashSet<>();
            if (statement instanceof Select) {
                Select select = (Select) statement;
                if (select.getSelectBody() instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                    for (SelectItem item : plainSelect.getSelectItems()) {
                        if (item instanceof SelectExpressionItem) {
                            ((SelectExpressionItem) item).getExpression().accept(new ExpressionVisitorAdapter() {
                                @Override
                                public void visit(Column column) {
                                    Table t = column.getTable();
                                    Table realTable;
                                    if ((realTable = findTable(tables, t.getName())) == null) {
                                        throw new IllegalStateException("Table '" + t + "' not found in determined tables: " + tables);
                                    }

                                    fetchedTables.add(realTable);
                                }
                            });
                        }
                    }
                }
            }
            return tableNames(fetchedTables);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private Table findTable(List<Table> tables, String alias) {
        for (Table t : tables) {
            if (alias.equals(t.getAlias().getName())) {
                return t;
            }
        }

        return null;
    }

    private List<Table> getTables(Statement statement) {
        if (statement instanceof Select) {
            Select select = (Select) statement;
            if (select.getSelectBody() instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                final List<Table> tables = new ArrayList<>();
                FromItemVisitor visitor = new FromItemVisitorAdapter() {
                    @Override
                    public void visit(Table table) {
                        tables.add(table);
                    }

                    @Override
                    public void visit(SubJoin subjoin) {
                        subjoin.getLeft().accept(this);
                        for (Join join : subjoin.getJoinList()) {
                            join.getRightItem().accept(this);
                        }
                    }
                };
                plainSelect.getFromItem().accept(visitor);
                if (plainSelect.getJoins() != null) {
                    for (Join j : plainSelect.getJoins()) {
                        j.getRightItem().accept(visitor);
                    }
                }
                return tables;
            }
        } else if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            return Collections.singletonList(insert.getTable());
        } else if (statement instanceof Update) {
            Update update = (Update) statement;
            return new ArrayList<>(update.getTables());
        } else if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            return Collections.singletonList(delete.getTable());
        }

        return Collections.emptyList();
    }
}
