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

package com.blazebit.persistence.integration.datanucleus.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.datanucleus.store.rdbms.mapping.java.JavaTypeMapping;
import org.datanucleus.store.rdbms.mapping.java.TemporalMapping;
import org.datanucleus.store.rdbms.sql.SQLText;
import org.datanucleus.store.rdbms.sql.expression.AggregateNumericExpression;
import org.datanucleus.store.rdbms.sql.expression.AggregateStringExpression;
import org.datanucleus.store.rdbms.sql.expression.AggregateTemporalExpression;
import org.datanucleus.store.rdbms.sql.expression.BooleanExpression;
import org.datanucleus.store.rdbms.sql.expression.ByteExpression;
import org.datanucleus.store.rdbms.sql.expression.CharacterExpression;
import org.datanucleus.store.rdbms.sql.expression.NumericExpression;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.expression.StringExpression;
import org.datanucleus.store.rdbms.sql.expression.TemporalExpression;
import org.datanucleus.store.rdbms.sql.method.AbstractSQLMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DataNucleusJpqlFunctionAdapter extends AbstractSQLMethod {
    
    private static final Logger LOG = Logger.getLogger(DataNucleusJpqlFunctionAdapter.class.getName());
    private final JpqlFunction function;
    private final boolean aggregate;

    public DataNucleusJpqlFunctionAdapter(JpqlFunction function, boolean aggregate) {
        this.function = function;
        this.aggregate = aggregate;
    }

    public JpqlFunction unwrap() {
        return function;
    }

    @Override
    public SQLExpression getExpression(SQLExpression expr, List<SQLExpression> args) {
        // NOTE: expr will be the first argument for class methods like getMonth!
        int argsSize = args.size();
        List<String> newArgs = new ArrayList<String>(argsSize + (expr == null ? 0 : 1));
        Class<?> firstArgumentType = null;
        JavaTypeMapping firstArgumentTypeMapping = null;
        
        if (expr != null) {
            firstArgumentTypeMapping = expr.getJavaTypeMapping();
            firstArgumentType = firstArgumentTypeMapping.getJavaType();
            newArgs.add(expr.toSQLText().toSQL());
            for (int i = 0; i < argsSize; i++) {
                newArgs.add(args.get(i).toSQLText().toSQL());
            }
        } else if (argsSize > 0) {
            firstArgumentTypeMapping = args.get(0).getJavaTypeMapping();
            firstArgumentType = firstArgumentTypeMapping.getJavaType();
            newArgs.add(args.get(0).toSQLText().toSQL());
            for (int i = 1; i < argsSize; i++) {
                newArgs.add(args.get(i).toSQLText().toSQL());
            }
        }
        
        Class<?> returnType = function.getReturnType(firstArgumentType);
        JavaTypeMapping returnTypeMapping;
        
        if (returnType == firstArgumentType) {
            returnTypeMapping = firstArgumentTypeMapping;
        } else {
            returnTypeMapping = getMappingForClass(returnType);
        }
        
        if (returnTypeMapping == null) {
            throw new IllegalArgumentException("Invalid return type null returned from function: " + function);
        }
        
        final DataNucleusFunctionRenderContext context = new DataNucleusFunctionRenderContext(newArgs);
        function.render(context);
        
        final SQLText sqlText = new CustomSQLText(context.renderToString(), expr, args);
        
        if (aggregate) {
            if (returnTypeMapping instanceof TemporalMapping) {
                return new AggregateTemporalExpression(stmt, returnTypeMapping, "", null) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (Number.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new AggregateNumericExpression(stmt, returnTypeMapping, "") {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (String.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new AggregateStringExpression(stmt, returnTypeMapping, "", null) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else {
                LOG.warning("Aggregate type [" + returnType + "] could not be represented as aggregate. Please report this so we can support the type! Falling back to normal expression.");
            }
        } else {
            if (returnTypeMapping instanceof TemporalMapping) {
                return new TemporalExpression(stmt, returnTypeMapping, "", null) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (Byte.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new ByteExpression(stmt, null, returnTypeMapping) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (Number.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new NumericExpression(stmt, returnTypeMapping, "") {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (String.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new StringExpression(stmt, null, returnTypeMapping) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (Character.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new CharacterExpression(stmt, null, returnTypeMapping) {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else if (Boolean.class.isAssignableFrom(returnTypeMapping.getJavaType())) {
                return new BooleanExpression(stmt, returnTypeMapping, "") {
                    @Override
                    public SQLText toSQLText() {
                        return sqlText;
                    }
                };
            } else {
                LOG.warning("Type [" + returnType + "] could not be represented as aggregate. Please report this so we can support the type! Falling back to normal expression.");
            }
        }
        
        return new SQLExpression(stmt, null, returnTypeMapping) {
            @Override
            public SQLText toSQLText() {
                return sqlText;
            }
        };
    }

}
