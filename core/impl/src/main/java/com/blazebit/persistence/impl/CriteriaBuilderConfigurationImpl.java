/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.function.datetime.day.AccessDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DB2DayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DerbyDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.SQLServerDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.SybaseDayFunction;
import com.blazebit.persistence.impl.function.datetime.hour.AccessHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.DB2HourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.DerbyHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.HourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.SQLServerHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.SybaseHourFunction;
import com.blazebit.persistence.impl.function.datetime.minute.AccessMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.DB2MinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.DerbyMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.MinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.SQLServerMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.SybaseMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.month.AccessMonthFunction;
import com.blazebit.persistence.impl.function.datetime.month.DB2MonthFunction;
import com.blazebit.persistence.impl.function.datetime.month.DerbyMonthFunction;
import com.blazebit.persistence.impl.function.datetime.month.MonthFunction;
import com.blazebit.persistence.impl.function.datetime.month.SQLServerMonthFunction;
import com.blazebit.persistence.impl.function.datetime.month.SybaseMonthFunction;
import com.blazebit.persistence.impl.function.datetime.second.AccessSecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.DB2SecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.DerbySecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SQLServerSecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SybaseSecondFunction;
import com.blazebit.persistence.impl.function.datetime.year.AccessYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.DB2YearFunction;
import com.blazebit.persistence.impl.function.datetime.year.DerbyYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.SQLServerYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.SybaseYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.YearFunction;
import com.blazebit.persistence.impl.function.groupconcat.DB2GroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.H2GroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.MySQLGroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.OracleGroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.PostgreSQLGroupConcatFunction;
import com.blazebit.persistence.impl.function.limit.DB2LimitFunction;
import com.blazebit.persistence.impl.function.limit.LimitFunction;
import com.blazebit.persistence.impl.function.limit.MySQLLimitFunction;
import com.blazebit.persistence.impl.function.limit.OracleLimitFunction;
import com.blazebit.persistence.impl.function.limit.SQL2008LimitFunction;
import com.blazebit.persistence.impl.function.pageposition.MySQLPagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.OraclePagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.TransactSQLPagePositionFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.spi.QueryTransformer;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderConfigurationImpl implements CriteriaBuilderConfiguration {

    private final List<QueryTransformer> queryTransformers = new ArrayList<QueryTransformer>();
    private final Map<String, JpqlFunctionGroup> functions = new HashMap<String, JpqlFunctionGroup>();
    private final List<EntityManagerIntegrator> entityManagerIntegrators = new ArrayList<EntityManagerIntegrator>();
    private Properties properties = new Properties();

    public CriteriaBuilderConfigurationImpl() {
        loadDefaultProperties();
        loadQueryTransformers();
        loadEntityManagerIntegrator();
        loadFunctions();
    }
    
    private void loadFunctions() {
        JpqlFunctionGroup jpqlFunctionGroup;
        
        jpqlFunctionGroup = new JpqlFunctionGroup("limit", false);
        jpqlFunctionGroup.add(null, new LimitFunction());
        jpqlFunctionGroup.add("mysql", new MySQLLimitFunction());
        jpqlFunctionGroup.add("oracle", new OracleLimitFunction());
        jpqlFunctionGroup.add("derby", new SQL2008LimitFunction());
        jpqlFunctionGroup.add("db2", new DB2LimitFunction());
        jpqlFunctionGroup.add("sybase", null); // Does not support limit
        // The function for SQLServer is hard to implement
//        jpqlFunctions.put("microsoft", new SQLServerLimitFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("page_position", false);
        jpqlFunctionGroup.add(null, new PagePositionFunction());
        jpqlFunctionGroup.add("mysql", new MySQLPagePositionFunction());
        jpqlFunctionGroup.add("oracle", new OraclePagePositionFunction());
        jpqlFunctionGroup.add("sybase", new TransactSQLPagePositionFunction());
        jpqlFunctionGroup.add("microsoft", new TransactSQLPagePositionFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("group_concat", true);
        jpqlFunctionGroup.add("db2", new DB2GroupConcatFunction());
        jpqlFunctionGroup.add("oracle", new OracleGroupConcatFunction());
        jpqlFunctionGroup.add("h2", new H2GroupConcatFunction());
        jpqlFunctionGroup.add("mysql", new MySQLGroupConcatFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLGroupConcatFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("year", false);
        jpqlFunctionGroup.add(null, new YearFunction());
        jpqlFunctionGroup.add("access", new AccessYearFunction());
        jpqlFunctionGroup.add("db2", new DB2YearFunction());
        jpqlFunctionGroup.add("derby", new DerbyYearFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerYearFunction());
        jpqlFunctionGroup.add("sybase", new SybaseYearFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("month", false);
        jpqlFunctionGroup.add(null, new MonthFunction());
        jpqlFunctionGroup.add("access", new AccessMonthFunction());
        jpqlFunctionGroup.add("db2", new DB2MonthFunction());
        jpqlFunctionGroup.add("derby", new DerbyMonthFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMonthFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMonthFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("day", false);
        jpqlFunctionGroup.add(null, new DayFunction());
        jpqlFunctionGroup.add("access", new AccessDayFunction());
        jpqlFunctionGroup.add("db2", new DB2DayFunction());
        jpqlFunctionGroup.add("derby", new DerbyDayFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerDayFunction());
        jpqlFunctionGroup.add("sybase", new SybaseDayFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("hour", false);
        jpqlFunctionGroup.add(null, new HourFunction());
        jpqlFunctionGroup.add("access", new AccessHourFunction());
        jpqlFunctionGroup.add("db2", new DB2HourFunction());
        jpqlFunctionGroup.add("derby", new DerbyHourFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerHourFunction());
        jpqlFunctionGroup.add("sybase", new SybaseHourFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("minute", false);
        jpqlFunctionGroup.add(null, new MinuteFunction());
        jpqlFunctionGroup.add("access", new AccessMinuteFunction());
        jpqlFunctionGroup.add("db2", new DB2MinuteFunction());
        jpqlFunctionGroup.add("derby", new DerbyMinuteFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMinuteFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMinuteFunction());
        registerFunction(jpqlFunctionGroup);
        
        jpqlFunctionGroup = new JpqlFunctionGroup("second", false);
        jpqlFunctionGroup.add(null, new SecondFunction());
        jpqlFunctionGroup.add("access", new AccessSecondFunction());
        jpqlFunctionGroup.add("db2", new DB2SecondFunction());
        jpqlFunctionGroup.add("derby", new DerbySecondFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerSecondFunction());
        jpqlFunctionGroup.add("sybase", new SybaseSecondFunction());
        registerFunction(jpqlFunctionGroup);
    }

    private void loadDefaultProperties() {
    	properties.put(ConfigurationProperties.COMPATIBLE_MODE, "false");
    }

    private void loadQueryTransformers() {
        ServiceLoader<QueryTransformer> serviceLoader = ServiceLoader.load(QueryTransformer.class);
        Iterator<QueryTransformer> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            QueryTransformer transformer = iterator.next();
            queryTransformers.add(transformer);
        }
    }

    private void loadEntityManagerIntegrator() {
        ServiceLoader<EntityManagerIntegrator> serviceLoader = ServiceLoader.load(EntityManagerIntegrator.class);
        Iterator<EntityManagerIntegrator> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            EntityManagerIntegrator enricher = iterator.next();
            entityManagerIntegrators.add(enricher);
        }
    }
    
    @Override
    public CriteriaBuilderConfiguration registerFunction(JpqlFunctionGroup jpqlFunctionGroup) {
        String functionName = jpqlFunctionGroup.getName().toLowerCase();
        functions.put(functionName, jpqlFunctionGroup);
        return this;
    }
    
    public Map<String, JpqlFunctionGroup> getFunctions() {
        return functions;
    }
    
    @Override
    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    @Override
    public CriteriaBuilderConfiguration registerQueryTransformer(QueryTransformer transformer) {
        queryTransformers.add(transformer);
        return this;
    }

    @Override
    public List<QueryTransformer> getQueryTransformers() {
        return queryTransformers;
    }

    @Override
    public CriteriaBuilderConfiguration registerEntityManagerIntegrator(EntityManagerIntegrator entityManagerEnricher) {
        entityManagerIntegrators.add(entityManagerEnricher);
        return this;
    }

    @Override
    public List<EntityManagerIntegrator> getEntityManagerIntegrators() {
        return entityManagerIntegrators;
    }

    @Override
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        return new CriteriaBuilderFactoryImpl(this);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public CriteriaBuilderConfiguration setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration addProperties(Properties extraProperties) {
        this.properties.putAll(extraProperties);
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration mergeProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (this.properties.containsKey(entry.getKey())) {
                continue;
            }
            this.properties.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration setProperty(String propertyName, String value) {
        properties.setProperty(propertyName, value);
        return this;
    }
}
