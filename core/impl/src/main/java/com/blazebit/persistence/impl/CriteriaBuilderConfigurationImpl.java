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
import com.blazebit.persistence.impl.function.pageposition.MySQLPagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.OraclePagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.TransactSQLPagePositionFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.QueryTransformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderConfigurationImpl implements CriteriaBuilderConfiguration {

    private final List<QueryTransformer> queryTransformers = new ArrayList<QueryTransformer>();
    private final Map<String, Map<String, JpqlFunction>> functions = new HashMap<String, Map<String, JpqlFunction>>();
    private final List<EntityManagerIntegrator> entityManagerEnrichers = new ArrayList<EntityManagerIntegrator>();
    private Properties properties = new Properties();

    public CriteriaBuilderConfigurationImpl() {
        loadDefaultProperties();
        loadQueryTransformers();
        loadEntityManagerIntegrator();
        loadFunctions();
    }
    
    private void loadFunctions() {
        Map<String, JpqlFunction> jpqlFunctions;
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new PagePositionFunction());
        jpqlFunctions.put("mysql", new MySQLPagePositionFunction());
        jpqlFunctions.put("oracle", new OraclePagePositionFunction());
        jpqlFunctions.put("sybase", new TransactSQLPagePositionFunction());
        jpqlFunctions.put("microsoft", new TransactSQLPagePositionFunction());
        functions.put("page_position", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new YearFunction());
        jpqlFunctions.put("access", new AccessYearFunction());
        jpqlFunctions.put("db2", new DB2YearFunction());
        jpqlFunctions.put("derby", new DerbyYearFunction());
        jpqlFunctions.put("microsoft", new SQLServerYearFunction());
        jpqlFunctions.put("sybase", new SybaseYearFunction());
        functions.put("year", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new MonthFunction());
        jpqlFunctions.put("access", new AccessMonthFunction());
        jpqlFunctions.put("db2", new DB2MonthFunction());
        jpqlFunctions.put("derby", new DerbyMonthFunction());
        jpqlFunctions.put("microsoft", new SQLServerMonthFunction());
        jpqlFunctions.put("sybase", new SybaseMonthFunction());
        functions.put("month", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new DayFunction());
        jpqlFunctions.put("access", new AccessDayFunction());
        jpqlFunctions.put("db2", new DB2DayFunction());
        jpqlFunctions.put("derby", new DerbyDayFunction());
        jpqlFunctions.put("microsoft", new SQLServerDayFunction());
        jpqlFunctions.put("sybase", new SybaseDayFunction());
        functions.put("day", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new HourFunction());
        jpqlFunctions.put("access", new AccessHourFunction());
        jpqlFunctions.put("db2", new DB2HourFunction());
        jpqlFunctions.put("derby", new DerbyHourFunction());
        jpqlFunctions.put("microsoft", new SQLServerHourFunction());
        jpqlFunctions.put("sybase", new SybaseHourFunction());
        functions.put("hour", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new MinuteFunction());
        jpqlFunctions.put("access", new AccessMinuteFunction());
        jpqlFunctions.put("db2", new DB2MinuteFunction());
        jpqlFunctions.put("derby", new DerbyMinuteFunction());
        jpqlFunctions.put("microsoft", new SQLServerMinuteFunction());
        jpqlFunctions.put("sybase", new SybaseMinuteFunction());
        functions.put("minute", jpqlFunctions);
        
        jpqlFunctions = new HashMap<String, JpqlFunction>();
        jpqlFunctions.put(null, new SecondFunction());
        jpqlFunctions.put("access", new AccessSecondFunction());
        jpqlFunctions.put("db2", new DB2SecondFunction());
        jpqlFunctions.put("derby", new DerbySecondFunction());
        jpqlFunctions.put("microsoft", new SQLServerSecondFunction());
        jpqlFunctions.put("sybase", new SybaseSecondFunction());
        functions.put("second", jpqlFunctions);
    }

    private void loadDefaultProperties() {
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
            entityManagerEnrichers.add(enricher);
        }
    }
    
    @Override
    public CriteriaBuilderConfiguration registerFunction(String name, JpqlFunction function) {
        return registerFunction(name, null, function);
    }
    
    @Override
    public CriteriaBuilderConfiguration registerFunction(String name, String dbms, JpqlFunction function) {
        String functionName = name.toLowerCase();
        Map<String, JpqlFunction> dbmsFunctions = functions.get(functionName);
        
        if (dbmsFunctions == null) {
            functions.put(functionName, dbmsFunctions = new HashMap<String, JpqlFunction>());
        }
        
        dbmsFunctions.put(dbms == null ? null : dbms.toLowerCase(), function);
        return this;
    }
    
    public Map<String, Map<String, JpqlFunction>> getFunctions() {
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
        entityManagerEnrichers.add(entityManagerEnricher);
        return this;
    }

    @Override
    public List<EntityManagerIntegrator> getEntityManagerIntegrators() {
        return entityManagerEnrichers;
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
        for (Map.Entry entry : properties.entrySet()) {
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
