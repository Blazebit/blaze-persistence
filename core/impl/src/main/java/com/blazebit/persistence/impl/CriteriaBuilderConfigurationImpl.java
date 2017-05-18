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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.dialect.DB2DbmsDialect;
import com.blazebit.persistence.impl.dialect.DefaultDbmsDialect;
import com.blazebit.persistence.impl.dialect.H2DbmsDialect;
import com.blazebit.persistence.impl.dialect.MSSQLDbmsDialect;
import com.blazebit.persistence.impl.dialect.MySQLDbmsDialect;
import com.blazebit.persistence.impl.dialect.OracleDbmsDialect;
import com.blazebit.persistence.impl.dialect.PostgreSQLDbmsDialect;
import com.blazebit.persistence.impl.function.entity.EntityFunction;
import com.blazebit.persistence.impl.function.subquery.SubqueryFunction;
import com.blazebit.persistence.parser.expression.ConcurrentHashMapExpressionCache;
import com.blazebit.persistence.impl.function.cast.CastFunction;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.function.count.CountTupleEmulationFunction;
import com.blazebit.persistence.impl.function.count.CountTupleFunction;
import com.blazebit.persistence.impl.function.count.MySQLCountTupleFunction;
import com.blazebit.persistence.impl.function.datediff.day.AccessDayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.day.DB2DayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.day.DefaultDayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.day.MySQLDayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.day.OracleDayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.day.PostgreSQLDayDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.AccessHourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.DB2HourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.DefaultHourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.MySQLHourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.OracleHourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.hour.PostgreSQLHourDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.AccessMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.DB2MillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.DefaultMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.MySQLMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.OracleMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.PostgreSQLMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.millisecond.SQLServerMillisecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.AccessMinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.DB2MinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.DefaultMinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.MySQLMinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.OracleMinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.minute.PostgreSQLMinuteDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.AccessMonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.DB2MonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.DefaultMonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.MySQLMonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.OracleMonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.month.PostgreSQLMonthDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.AccessSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.DB2SecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.DefaultSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.MySQLSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.OracleSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.PostgreSQLSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.second.SQLServerSecondDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.AccessYearDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.DB2YearDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.DefaultYearDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.MySQLYearDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.OracleYearDiffFunction;
import com.blazebit.persistence.impl.function.datediff.year.PostgreSQLYearDiffFunction;
import com.blazebit.persistence.impl.function.datetime.day.AccessDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DB2DayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DayFunction;
import com.blazebit.persistence.impl.function.datetime.day.DerbyDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.SQLServerDayFunction;
import com.blazebit.persistence.impl.function.datetime.day.SybaseDayFunction;
import com.blazebit.persistence.impl.function.datetime.epoch.DB2EpochFunction;
import com.blazebit.persistence.impl.function.datetime.epoch.DefaultEpochFunction;
import com.blazebit.persistence.impl.function.datetime.epoch.MySQLEpochFunction;
import com.blazebit.persistence.impl.function.datetime.epoch.OracleEpochFunction;
import com.blazebit.persistence.impl.function.datetime.epoch.PostgreSQLEpochFunction;
import com.blazebit.persistence.impl.function.datetime.hour.AccessHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.DB2HourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.DerbyHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.HourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.OracleHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.SQLServerHourFunction;
import com.blazebit.persistence.impl.function.datetime.hour.SybaseHourFunction;
import com.blazebit.persistence.impl.function.datetime.minute.AccessMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.DB2MinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.DerbyMinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.MinuteFunction;
import com.blazebit.persistence.impl.function.datetime.minute.OracleMinuteFunction;
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
import com.blazebit.persistence.impl.function.datetime.second.OracleSecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SQLServerSecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SecondFunction;
import com.blazebit.persistence.impl.function.datetime.second.SybaseSecondFunction;
import com.blazebit.persistence.impl.function.datetime.year.AccessYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.DB2YearFunction;
import com.blazebit.persistence.impl.function.datetime.year.DerbyYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.SQLServerYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.SybaseYearFunction;
import com.blazebit.persistence.impl.function.datetime.year.YearFunction;
import com.blazebit.persistence.impl.function.greatest.AbstractGreatestFunction;
import com.blazebit.persistence.impl.function.greatest.DefaultGreatestFunction;
import com.blazebit.persistence.impl.function.greatest.MaxGreatestFunction;
import com.blazebit.persistence.impl.function.greatest.SelectMaxUnionGreatestFunction;
import com.blazebit.persistence.impl.function.groupconcat.DB2GroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.H2GroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.MySQLGroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.OracleListaggGroupConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.PostgreSQLGroupConcatFunction;
import com.blazebit.persistence.impl.function.least.AbstractLeastFunction;
import com.blazebit.persistence.impl.function.least.DefaultLeastFunction;
import com.blazebit.persistence.impl.function.least.MinLeastFunction;
import com.blazebit.persistence.impl.function.least.SelectMinUnionLeastFunction;
import com.blazebit.persistence.impl.function.limit.LimitFunction;
import com.blazebit.persistence.impl.function.pageposition.MySQLPagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.OraclePagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.function.pageposition.TransactSQLPagePositionFunction;
import com.blazebit.persistence.impl.function.repeat.AbstractRepeatFunction;
import com.blazebit.persistence.impl.function.repeat.DefaultRepeatFunction;
import com.blazebit.persistence.impl.function.repeat.LpadRepeatFunction;
import com.blazebit.persistence.impl.function.repeat.ReplicateRepeatFunction;
import com.blazebit.persistence.impl.function.rowvalue.DB2RowValueComparisonFunction;
import com.blazebit.persistence.impl.function.rowvalue.RowValueComparisonFunction;
import com.blazebit.persistence.impl.function.set.SetFunction;
import com.blazebit.persistence.impl.function.treat.TreatFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.PackageOpener;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
 * @author Moritz Becker
 * @since 1.0.0
 */
public class CriteriaBuilderConfigurationImpl implements CriteriaBuilderConfiguration {

    private final Map<String, DbmsDialect> dbmsDialects = new HashMap<String, DbmsDialect>();
    private final Map<String, JpqlFunctionGroup> functions = new HashMap<String, JpqlFunctionGroup>();
    private final Map<String, Class<?>> treatTypes = new HashMap<String, Class<?>>();
    private final Map<String, JpqlMacro> macros = new HashMap<String, JpqlMacro>();
    private final List<EntityManagerFactoryIntegrator> entityManagerIntegrators = new ArrayList<EntityManagerFactoryIntegrator>();
    private PackageOpener packageOpener;
    private Properties properties = new Properties();
    private ExtendedQuerySupport extendedQuerySupport;

    public CriteriaBuilderConfigurationImpl(PackageOpener packageOpener) {
        this.packageOpener = packageOpener;
        loadDefaultProperties();
        loadExtendedQuerySupport();
        loadEntityManagerIntegrator();
        loadDbmsDialects();
        loadFunctions();
    }

    // NOTE: When adding a function here, you might want to also add it in AbstractCoreTest so it is recognized
    @SuppressWarnings("checkstyle:methodlength")
    private void loadFunctions() {
        JpqlFunctionGroup jpqlFunctionGroup;
        
        // limit

        jpqlFunctionGroup = new JpqlFunctionGroup("limit", false);
        jpqlFunctionGroup.add(null, new LimitFunction(dbmsDialects.get(null)));
        jpqlFunctionGroup.add("mysql", new LimitFunction(dbmsDialects.get("mysql")));
        jpqlFunctionGroup.add("oracle", new LimitFunction(dbmsDialects.get("oracle")));
        jpqlFunctionGroup.add("db2", new LimitFunction(dbmsDialects.get("db2")));
        jpqlFunctionGroup.add("sybase", null); // Does not support limit
        jpqlFunctionGroup.add("microsoft", new LimitFunction(dbmsDialects.get("microsoft")));
        registerFunction(jpqlFunctionGroup);
        
        // page_position

        jpqlFunctionGroup = new JpqlFunctionGroup(PagePositionFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new PagePositionFunction());
        jpqlFunctionGroup.add("mysql", new MySQLPagePositionFunction());
        jpqlFunctionGroup.add("oracle", new OraclePagePositionFunction());
        jpqlFunctionGroup.add("sybase", new TransactSQLPagePositionFunction());
        jpqlFunctionGroup.add("microsoft", new TransactSQLPagePositionFunction());
        registerFunction(jpqlFunctionGroup);

        // entity_function

        jpqlFunctionGroup = new JpqlFunctionGroup(EntityFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new EntityFunction());
        registerFunction(jpqlFunctionGroup);
        
        // set operations

        for (SetOperationType setType : SetOperationType.values()) {
            // Use a prefix because hibernate uses UNION as keyword
            jpqlFunctionGroup = new JpqlFunctionGroup("set_" + setType.name().toLowerCase(), false);
            
            for (Map.Entry<String, DbmsDialect> dbmsDialectEntry : dbmsDialects.entrySet()) {
                jpqlFunctionGroup.add(dbmsDialectEntry.getKey(), new SetFunction(setType, dbmsDialectEntry.getValue()));
            }
            
            registerFunction(jpqlFunctionGroup);
        }
        
        // treat

        registerNamedType("Boolean", Boolean.class);
        registerNamedType("Byte", Byte.class);
        registerNamedType("Short", Short.class);
        registerNamedType("Integer", Integer.class);
        registerNamedType("Long", Long.class);
        registerNamedType("Float", Float.class);
        registerNamedType("Double", Double.class);

        registerNamedType("Character", Character.class);
        registerNamedType("String", String.class);

        registerNamedType("BigInteger", BigInteger.class);
        registerNamedType("BigDecimal", BigDecimal.class);

        registerNamedType("Time", Time.class);
        registerNamedType("Date", java.sql.Date.class);
        registerNamedType("Timestamp", Timestamp.class);
        registerNamedType("Calendar", Calendar.class);

        // cast

        registerFunction(new JpqlFunctionGroup("cast_boolean"));
        registerFunction(new JpqlFunctionGroup("cast_byte"));
        registerFunction(new JpqlFunctionGroup("cast_short"));
        registerFunction(new JpqlFunctionGroup("cast_integer"));
        registerFunction(new JpqlFunctionGroup("cast_long"));
        registerFunction(new JpqlFunctionGroup("cast_float"));
        registerFunction(new JpqlFunctionGroup("cast_double"));

        registerFunction(new JpqlFunctionGroup("cast_character"));
        registerFunction(new JpqlFunctionGroup("cast_string"));

        registerFunction(new JpqlFunctionGroup("cast_biginteger"));
        registerFunction(new JpqlFunctionGroup("cast_bigdecimal"));

        registerFunction(new JpqlFunctionGroup("cast_time"));
        registerFunction(new JpqlFunctionGroup("cast_date"));
        registerFunction(new JpqlFunctionGroup("cast_timestamp"));
        registerFunction(new JpqlFunctionGroup("cast_calendar"));

        for (Map.Entry<String, DbmsDialect> dbmsDialectEntry : dbmsDialects.entrySet()) {
            functions.get("cast_boolean").add(dbmsDialectEntry.getKey(), new CastFunction(Boolean.class, dbmsDialectEntry.getValue()));
            functions.get("cast_byte").add(dbmsDialectEntry.getKey(), new CastFunction(Byte.class, dbmsDialectEntry.getValue()));
            functions.get("cast_short").add(dbmsDialectEntry.getKey(), new CastFunction(Short.class, dbmsDialectEntry.getValue()));
            functions.get("cast_integer").add(dbmsDialectEntry.getKey(), new CastFunction(Integer.class, dbmsDialectEntry.getValue()));
            functions.get("cast_long").add(dbmsDialectEntry.getKey(), new CastFunction(Long.class, dbmsDialectEntry.getValue()));
            functions.get("cast_float").add(dbmsDialectEntry.getKey(), new CastFunction(Float.class, dbmsDialectEntry.getValue()));
            functions.get("cast_double").add(dbmsDialectEntry.getKey(), new CastFunction(Double.class, dbmsDialectEntry.getValue()));

            functions.get("cast_character").add(dbmsDialectEntry.getKey(), new CastFunction(Character.class, dbmsDialectEntry.getValue()));
            functions.get("cast_string").add(dbmsDialectEntry.getKey(), new CastFunction(String.class, dbmsDialectEntry.getValue()));

            functions.get("cast_biginteger").add(dbmsDialectEntry.getKey(), new CastFunction(BigInteger.class, dbmsDialectEntry.getValue()));
            functions.get("cast_bigdecimal").add(dbmsDialectEntry.getKey(), new CastFunction(BigDecimal.class, dbmsDialectEntry.getValue()));

            functions.get("cast_time").add(dbmsDialectEntry.getKey(), new CastFunction(Time.class, dbmsDialectEntry.getValue()));
            functions.get("cast_date").add(dbmsDialectEntry.getKey(), new CastFunction(java.sql.Date.class, dbmsDialectEntry.getValue()));
            functions.get("cast_timestamp").add(dbmsDialectEntry.getKey(), new CastFunction(Timestamp.class, dbmsDialectEntry.getValue()));
            functions.get("cast_calendar").add(dbmsDialectEntry.getKey(), new CastFunction(Calendar.class, dbmsDialectEntry.getValue()));
        }

        // group_concat
        
        jpqlFunctionGroup = new JpqlFunctionGroup("group_concat", true);
        jpqlFunctionGroup.add("db2", new DB2GroupConcatFunction());
        jpqlFunctionGroup.add("oracle", new OracleListaggGroupConcatFunction());
        jpqlFunctionGroup.add("h2", new H2GroupConcatFunction());
        jpqlFunctionGroup.add("mysql", new MySQLGroupConcatFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLGroupConcatFunction());
        registerFunction(jpqlFunctionGroup);
        
        // datetime

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
        jpqlFunctionGroup.add("oracle", new OracleHourFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("minute", false);
        jpqlFunctionGroup.add(null, new MinuteFunction());
        jpqlFunctionGroup.add("access", new AccessMinuteFunction());
        jpqlFunctionGroup.add("db2", new DB2MinuteFunction());
        jpqlFunctionGroup.add("derby", new DerbyMinuteFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMinuteFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMinuteFunction());
        jpqlFunctionGroup.add("oracle", new OracleMinuteFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("second", false);
        jpqlFunctionGroup.add(null, new SecondFunction());
        jpqlFunctionGroup.add("access", new AccessSecondFunction());
        jpqlFunctionGroup.add("db2", new DB2SecondFunction());
        jpqlFunctionGroup.add("derby", new DerbySecondFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerSecondFunction());
        jpqlFunctionGroup.add("sybase", new SybaseSecondFunction());
        jpqlFunctionGroup.add("oracle", new OracleSecondFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("epoch", false);
        jpqlFunctionGroup.add(null, new DefaultEpochFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLEpochFunction());
        jpqlFunctionGroup.add("oracle", new OracleEpochFunction());
        jpqlFunctionGroup.add("db2", new DB2EpochFunction());
        jpqlFunctionGroup.add("mysql", new MySQLEpochFunction());
        registerFunction(jpqlFunctionGroup);

        // datediff

        jpqlFunctionGroup = new JpqlFunctionGroup("year_diff", false);
        jpqlFunctionGroup.add("access", new AccessYearDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2YearDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultYearDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultYearDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLYearDiffFunction());
        jpqlFunctionGroup.add("sybase", new DefaultYearDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLYearDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleYearDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("month_diff", false);
        jpqlFunctionGroup.add("access", new AccessMonthDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2MonthDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultMonthDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultMonthDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMonthDiffFunction());
        jpqlFunctionGroup.add("sybase", new DefaultMonthDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMonthDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleMonthDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("day_diff", false);
        jpqlFunctionGroup.add("access", new AccessDayDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2DayDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultDayDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultDayDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLDayDiffFunction());
        jpqlFunctionGroup.add("sybase", new DefaultDayDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLDayDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleDayDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("hour_diff", false);
        jpqlFunctionGroup.add("access", new AccessHourDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2HourDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultHourDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultHourDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLHourDiffFunction());
        jpqlFunctionGroup.add("sybase", new DefaultHourDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLHourDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleHourDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("minute_diff", false);
        jpqlFunctionGroup.add("access", new AccessMinuteDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2MinuteDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultMinuteDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultMinuteDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMinuteDiffFunction());
        jpqlFunctionGroup.add("sybase", new DefaultMinuteDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMinuteDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleMinuteDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("second_diff", false);
        jpqlFunctionGroup.add(null, new DefaultSecondDiffFunction());
        jpqlFunctionGroup.add("access", new AccessSecondDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2SecondDiffFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerSecondDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLSecondDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLSecondDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleSecondDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("millisecond_diff", false);
        jpqlFunctionGroup.add(null, new DefaultMillisecondDiffFunction());
        jpqlFunctionGroup.add("access", new AccessMillisecondDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2MillisecondDiffFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMillisecondDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMillisecondDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMillisecondDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleMillisecondDiffFunction());
        registerFunction(jpqlFunctionGroup);

        // count

        jpqlFunctionGroup = new JpqlFunctionGroup(AbstractCountFunction.FUNCTION_NAME, true);
        jpqlFunctionGroup.add(null, new CountTupleFunction());
        jpqlFunctionGroup.add("mysql", new MySQLCountTupleFunction());
        jpqlFunctionGroup.add("db2", new CountTupleEmulationFunction());
        jpqlFunctionGroup.add("microsoft", new CountTupleEmulationFunction("+", "varchar(max)"));
        jpqlFunctionGroup.add("oracle", new CountTupleEmulationFunction());
        jpqlFunctionGroup.add("hsql", new CountTupleEmulationFunction());
        registerFunction(jpqlFunctionGroup);

        // row values
        jpqlFunctionGroup = new JpqlFunctionGroup(RowValueComparisonFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new RowValueComparisonFunction());
        jpqlFunctionGroup.add("db2", new DB2RowValueComparisonFunction());
        registerFunction(jpqlFunctionGroup);

        // greatest

        jpqlFunctionGroup = new JpqlFunctionGroup(AbstractGreatestFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new DefaultGreatestFunction());
        jpqlFunctionGroup.add("db2", new MaxGreatestFunction());
        jpqlFunctionGroup.add("microsoft", new SelectMaxUnionGreatestFunction());
        registerFunction(jpqlFunctionGroup);

        // least

        jpqlFunctionGroup = new JpqlFunctionGroup(AbstractLeastFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new DefaultLeastFunction());
        jpqlFunctionGroup.add("db2", new MinLeastFunction());
        jpqlFunctionGroup.add("microsoft", new SelectMinUnionLeastFunction());
        registerFunction(jpqlFunctionGroup);

        // repeat

        jpqlFunctionGroup = new JpqlFunctionGroup(AbstractRepeatFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new DefaultRepeatFunction());
        jpqlFunctionGroup.add("oracle", new LpadRepeatFunction());
        jpqlFunctionGroup.add("microsoft", new ReplicateRepeatFunction());
        registerFunction(jpqlFunctionGroup);

        // subquery

        jpqlFunctionGroup = new JpqlFunctionGroup(SubqueryFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new SubqueryFunction());
        registerFunction(jpqlFunctionGroup);
    }

    private void loadDbmsDialects() {
        registerDialect(null, new DefaultDbmsDialect());
        registerDialect("mysql", new MySQLDbmsDialect());
        registerDialect("h2", new H2DbmsDialect());
        registerDialect("db2", new DB2DbmsDialect());
        registerDialect("postgresql", new PostgreSQLDbmsDialect());
        registerDialect("oracle", new OracleDbmsDialect());
        registerDialect("microsoft", new MSSQLDbmsDialect());
    }

    private void loadDefaultProperties() {
        properties.put(ConfigurationProperties.COMPATIBLE_MODE, "false");
        properties.put(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE, "true");
        properties.put(ConfigurationProperties.EXPRESSION_CACHE_CLASS, ConcurrentHashMapExpressionCache.class.getName());
        properties.put(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "true");
    }

    private void loadExtendedQuerySupport() {
        ServiceLoader<ExtendedQuerySupport> serviceLoader = ServiceLoader.load(ExtendedQuerySupport.class);
        Iterator<ExtendedQuerySupport> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            extendedQuerySupport = iterator.next();
        }
    }

    private void loadEntityManagerIntegrator() {
        ServiceLoader<EntityManagerFactoryIntegrator> serviceLoader = ServiceLoader.load(EntityManagerFactoryIntegrator.class);
        Iterator<EntityManagerFactoryIntegrator> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            EntityManagerFactoryIntegrator enricher = iterator.next();
            entityManagerIntegrators.add(enricher);
        }
    }

    @Override
    public CriteriaBuilderConfiguration withPackageOpener(PackageOpener packageOpener) {
        this.packageOpener = packageOpener;
        return this;
    }

    PackageOpener getPackageOpener() {
        return packageOpener;
    }

    @Override
    public CriteriaBuilderConfiguration registerFunction(JpqlFunctionGroup jpqlFunctionGroup) {
        String functionName = jpqlFunctionGroup.getName().toLowerCase();
        functions.put(functionName, jpqlFunctionGroup);
        return this;
    }

    @Override
    public CriteriaBuilderConfiguration registerMacro(String macroName, JpqlMacro jpqlMacro) {
        macros.put(macroName.toUpperCase(), jpqlMacro);
        return this;
    }

    public Map<String, JpqlFunctionGroup> getFunctions() {
        return functions;
    }

    @Override
    public JpqlFunctionGroup getFunction(String name) {
        return functions.get(name.toLowerCase());
    }

    @Override
    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    public Map<String, JpqlMacro> getMacros() {
        return macros;
    }

    @Override
    public Set<String> getMacroNames() {
        return macros.keySet();
    }

    public CriteriaBuilderConfiguration registerNamedType(String name, Class<?> type) {
        treatTypes.put(name, type);
        registerFunction(new JpqlFunctionGroup("treat_" + name.toLowerCase(), new TreatFunction(type)));
        return this;
    }

    public Map<String, Class<?>> getNamedTypes() {
        return treatTypes;
    }

    @Override
    public CriteriaBuilderConfiguration registerDialect(String dbms, DbmsDialect dialect) {
        dbmsDialects.put(dbms, dialect);
        return this;
    }
    
    public Map<String, DbmsDialect> getDbmsDialects() {
        return dbmsDialects;
    }

    public ExtendedQuerySupport getExtendedQuerySupport() {
        return extendedQuerySupport;
    }

    @Override
    public CriteriaBuilderConfiguration registerEntityManagerIntegrator(EntityManagerFactoryIntegrator entityManagerEnricher) {
        entityManagerIntegrators.add(entityManagerEnricher);
        return this;
    }

    @Override
    public List<EntityManagerFactoryIntegrator> getEntityManagerIntegrators() {
        return entityManagerIntegrators;
    }

    @Override
    public CriteriaBuilderFactory createCriteriaBuilderFactory(EntityManagerFactory emf) {
        return new CriteriaBuilderFactoryImpl(this, emf);
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
