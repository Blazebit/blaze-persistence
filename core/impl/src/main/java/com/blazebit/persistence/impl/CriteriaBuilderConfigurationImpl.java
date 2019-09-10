/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.dialect.DB2DbmsDialect;
import com.blazebit.persistence.impl.dialect.DefaultDbmsDialect;
import com.blazebit.persistence.impl.dialect.H2DbmsDialect;
import com.blazebit.persistence.impl.dialect.MSSQLDbmsDialect;
import com.blazebit.persistence.impl.dialect.MySQL8DbmsDialect;
import com.blazebit.persistence.impl.dialect.MySQLDbmsDialect;
import com.blazebit.persistence.impl.dialect.OracleDbmsDialect;
import com.blazebit.persistence.impl.dialect.PostgreSQLDbmsDialect;
import com.blazebit.persistence.impl.function.dateadd.day.DB2DayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.DayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.H2DayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.MSSQLDayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.MySQLDayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.OracleDayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.day.PostgreSQLDayAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.DB2HourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.H2HourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.HourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.MSSQLHourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.MySQLHourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.OracleHourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.hour.PostgreSQLHourAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.DB2MicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.H2MicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.MSSQLMicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.MicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.MySQLMicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.OracleMicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.microseconds.PostgreSQLMicrosecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.DB2MillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.H2MillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.MSSQLMillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.MillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.MySQLMillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.OracleMillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.milliseconds.PostgreSQLMillisecondsAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.DB2MinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.H2MinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.MSSQLMinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.MinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.MySQLMinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.OracleMinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.minute.PostgreSQLMinuteAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.DB2MonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.H2MonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.MSSQLMonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.MonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.MySQLMonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.OracleMonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.month.PostgreSQLMonthAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.DB2QuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.H2QuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.MSSQLQuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.MySQLQuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.OracleQuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.PostgreSQLQuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.quarter.QuarterAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.DB2SecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.H2SecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.MSSQLSecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.MySQLSecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.OracleSecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.PostgreSQLSecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.second.SecondAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.DB2WeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.H2WeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.MSSQLWeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.MySQLWeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.OracleWeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.PostgreSQLWeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.week.WeekAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.DB2YearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.H2YearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.MSSQLYearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.MySQLYearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.OracleYearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.PostgreSQLYearAddFunction;
import com.blazebit.persistence.impl.function.dateadd.year.YearAddFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.AccessDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.DB2DayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.DayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.MySQLDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.OracleDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.PostgreSQLDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.SQLServerDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofweek.SybaseDayOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.AccessDayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.DB2DayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.DayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.MySQLDayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.OracleDayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.SQLServerDayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.dayofyear.SybaseDayOfYearFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.DB2MicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.MicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.MySQLMicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.PostgreSQLMicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.SQLServerMicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.microsecond.SybaseMicrosecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.DB2MillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.MillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.MySQLMillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.OracleMillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.PostgreSQLMillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.SQLServerMillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.millisecond.SybaseMillisecondFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.AccessQuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.DB2QuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.OracleQuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.QuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.SQLServerQuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.SqliteQuarterFunction;
import com.blazebit.persistence.impl.function.datetime.quarter.SybaseQuarterFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.AccessIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.DB2IsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.H2IsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.MySQLIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.OracleIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.SQLServerIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.SqliteIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.SybaseIsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.isoweek.IsoWeekFunction;
import com.blazebit.persistence.impl.function.datetime.week.DB2WeekInYearFunction;
import com.blazebit.persistence.impl.function.datetime.week.MySQLWeekInYearFunction;
import com.blazebit.persistence.impl.function.datetime.week.OracleWeekInYearFunction;
import com.blazebit.persistence.impl.function.datetime.week.SQLServerWeekInYearFunction;
import com.blazebit.persistence.impl.function.datetime.week.WeekInYearFunction;
import com.blazebit.persistence.impl.function.datetime.yearofweek.DB2YearOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearofweek.MSSQLYearOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearofweek.MySQLYearOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearofweek.OracleYearOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearofweek.YearOfWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.DB2YearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.H2YearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.MySQLYearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.OracleYearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.PostgreSQLYearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.SQLServerYearWeekFunction;
import com.blazebit.persistence.impl.function.datetime.yearweek.YearWeekFunction;
import com.blazebit.persistence.impl.function.entity.EntityFunction;
import com.blazebit.persistence.impl.function.every.EveryFunction;
import com.blazebit.persistence.impl.function.every.FallbackEveryFunction;
import com.blazebit.persistence.impl.function.oragg.FallbackOrAggFunction;
import com.blazebit.persistence.impl.function.oragg.OrAggFunction;
import com.blazebit.persistence.impl.function.subquery.SubqueryFunction;
import com.blazebit.persistence.impl.function.trunc.day.TruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.hour.TruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.TruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.TruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.minute.TruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.month.TruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.TruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.second.TruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.week.TruncWeekFunction;
import com.blazebit.persistence.impl.function.trunc.year.TruncYearFunction;
import com.blazebit.persistence.impl.function.window.avg.AvgFunction;
import com.blazebit.persistence.impl.function.window.count.CountFunction;
import com.blazebit.persistence.impl.function.window.cumedist.CumeDistFunction;
import com.blazebit.persistence.impl.function.window.denserank.DenseRankFunction;
import com.blazebit.persistence.impl.function.window.every.FallbackWindowEveryFunction;
import com.blazebit.persistence.impl.function.window.every.WindowEveryFunction;
import com.blazebit.persistence.impl.function.window.first.FirstValueFunction;
import com.blazebit.persistence.impl.function.window.groupconcat.DB2GroupConcatWindowFunction;
import com.blazebit.persistence.impl.function.window.groupconcat.H2GroupConcatWindowFunction;
import com.blazebit.persistence.impl.function.window.groupconcat.MySQLGroupConcatWindowFunction;
import com.blazebit.persistence.impl.function.window.groupconcat.OracleListaggGroupConcatWindowFunction;
import com.blazebit.persistence.impl.function.window.groupconcat.PostgreSQLGroupConcatWindowFunction;
import com.blazebit.persistence.impl.function.window.lag.LagFunction;
import com.blazebit.persistence.impl.function.window.last.LastValueFunction;
import com.blazebit.persistence.impl.function.window.lead.LeadFunction;
import com.blazebit.persistence.impl.function.window.max.MaxFunction;
import com.blazebit.persistence.impl.function.window.min.MinFunction;
import com.blazebit.persistence.impl.function.window.nth.NthValueFunction;
import com.blazebit.persistence.impl.function.window.ntile.NtileFunction;
import com.blazebit.persistence.impl.function.window.oragg.FallbackWindowOrAggFunction;
import com.blazebit.persistence.impl.function.window.oragg.WindowOrAggFunction;
import com.blazebit.persistence.impl.function.window.percentrank.PercentRankFunction;
import com.blazebit.persistence.impl.function.window.rank.RankFunction;
import com.blazebit.persistence.impl.function.window.row.RowNumberFunction;
import com.blazebit.persistence.impl.function.window.sum.SumFunction;
import com.blazebit.persistence.impl.function.trunc.day.DB2TruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.day.H2TruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.day.MSSQLTruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.day.MySQLTruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.day.OracleTruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.day.PostgreSQLTruncDayFunction;
import com.blazebit.persistence.impl.function.trunc.hour.DB2TruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.hour.H2TruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.hour.MSSQLTruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.hour.MySQLTruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.hour.OracleTruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.hour.PostgreSQLTruncHourFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.DB2TruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.H2TruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.MSSQLTruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.MySQLTruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.OracleTruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.microseconds.PostgreSQLTruncMicrosecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.DB2TruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.H2TruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.MSSQLTruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.MySQLTruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.OracleTruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.milliseconds.PostgreSQLTruncMillisecondsFunction;
import com.blazebit.persistence.impl.function.trunc.minute.DB2TruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.minute.H2TruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.minute.MSSQLTruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.minute.MySQLTruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.minute.OracleTruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.minute.PostgreSQLTruncMinuteFunction;
import com.blazebit.persistence.impl.function.trunc.month.DB2TruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.month.H2TruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.month.MSSQLTruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.month.MySQLTruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.month.OracleTruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.month.PostgreSQLTruncMonthFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.DB2TruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.H2TruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.MSSQLTruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.MySQLTruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.OracleTruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.quarter.PostgreSQLTruncQuarterFunction;
import com.blazebit.persistence.impl.function.trunc.second.DB2TruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.second.H2TruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.second.MSSQLTruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.second.MySQLTruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.second.OracleTruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.second.PostgreSQLTruncSecondFunction;
import com.blazebit.persistence.impl.function.trunc.week.MSSQLTruncWeekFunction;
import com.blazebit.persistence.impl.function.trunc.week.MySQLTruncWeekFunction;
import com.blazebit.persistence.impl.function.trunc.week.OracleTruncWeekFunction;
import com.blazebit.persistence.impl.function.trunc.year.DB2TruncYearFunction;
import com.blazebit.persistence.impl.function.trunc.year.H2TruncYearFunction;
import com.blazebit.persistence.impl.function.trunc.year.MSSQLTruncYearFunction;
import com.blazebit.persistence.impl.function.trunc.year.MySQLTruncYearFunction;
import com.blazebit.persistence.impl.function.trunc.year.OracleTruncYearFunction;
import com.blazebit.persistence.impl.function.trunc.year.PostgreSQLTruncYearFunction;
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;

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
        jpqlFunctionGroup.add("mysql8", new LimitFunction(dbmsDialects.get("mysql8")));
        jpqlFunctionGroup.add("oracle", new LimitFunction(dbmsDialects.get("oracle")));
        jpqlFunctionGroup.add("db2", new LimitFunction(dbmsDialects.get("db2")));
        jpqlFunctionGroup.add("sybase", null); // Does not support limit
        jpqlFunctionGroup.add("microsoft", new LimitFunction(dbmsDialects.get("microsoft")));
        registerFunction(jpqlFunctionGroup);
        
        // page_position

        jpqlFunctionGroup = new JpqlFunctionGroup(PagePositionFunction.FUNCTION_NAME, false);
        jpqlFunctionGroup.add(null, new PagePositionFunction());
        jpqlFunctionGroup.add("mysql", new MySQLPagePositionFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLPagePositionFunction());
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
        registerNamedType("TimeZone", TimeZone.class);
        registerNamedType("Calendar", Calendar.class);
        registerNamedType("GregorianCalendar", GregorianCalendar.class);

        registerNamedType("Class", java.lang.Class.class);
        registerNamedType("Currency", java.util.Currency.class);
        registerNamedType("Locale", java.util.Locale.class);
        registerNamedType("UUID", java.util.UUID.class);
        registerNamedType("URL", java.net.URL.class);

        // Java 8 time types
        try {
            registerNamedType("LocalDate", Class.forName("java.time.LocalDate"));
            registerNamedType("LocalTime", Class.forName("java.time.LocalTime"));
            registerNamedType("LocalDateTime", Class.forName("java.time.LocalDateTime"));
            registerNamedType("OffsetTime", Class.forName("java.time.OffsetTime"));
            registerNamedType("OffsetDateTime", Class.forName("java.time.OffsetDateTime"));
            registerNamedType("ZonedDateTime", Class.forName("java.time.ZonedDateTime"));
            registerNamedType("Duration", Class.forName("java.time.Duration"));
            registerNamedType("Instant", Class.forName("java.time.Instant"));
            registerNamedType("MonthDay", Class.forName("java.time.MonthDay"));
            registerNamedType("Year", Class.forName("java.time.Year"));
            registerNamedType("YearMonth", Class.forName("java.time.YearMonth"));
            registerNamedType("Period", Class.forName("java.time.Period"));
            registerNamedType("ZoneId", Class.forName("java.time.ZoneId"));
            registerNamedType("ZoneOffset", Class.forName("java.time.ZoneOffset"));
        } catch (ClassNotFoundException ex) {
            // If they aren't found, we ignore them
        }

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
        jpqlFunctionGroup.add("mysql8", new MySQLGroupConcatFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLGroupConcatFunction());
        registerFunction(jpqlFunctionGroup);

        // window_group_concat

        jpqlFunctionGroup = new JpqlFunctionGroup("window_group_concat", false);
        jpqlFunctionGroup.add("db2", new DB2GroupConcatWindowFunction(dbmsDialects.get("db2")));
        jpqlFunctionGroup.add("oracle", new OracleListaggGroupConcatWindowFunction(dbmsDialects.get("oracle")));
        jpqlFunctionGroup.add("h2", new H2GroupConcatWindowFunction(dbmsDialects.get("h2")));
        jpqlFunctionGroup.add("mysql", new MySQLGroupConcatWindowFunction(dbmsDialects.get("mysql")));
        jpqlFunctionGroup.add("mysql8", new MySQLGroupConcatWindowFunction(dbmsDialects.get("mysql8")));
        jpqlFunctionGroup.add("postgresql", new PostgreSQLGroupConcatWindowFunction(dbmsDialects.get("postgresql")));
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

        jpqlFunctionGroup = new JpqlFunctionGroup("year_of_week", false);
        jpqlFunctionGroup.add(null, new YearOfWeekFunction());
        jpqlFunctionGroup.add("db2", new DB2YearOfWeekFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLYearOfWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLYearOfWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLYearOfWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleYearOfWeekFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("year_week", false);
        jpqlFunctionGroup.add(null, new YearWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLYearWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLYearWeekFunction());
        jpqlFunctionGroup.add("db2", new DB2YearWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleYearWeekFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLYearWeekFunction());
        jpqlFunctionGroup.add("h2", new H2YearWeekFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerYearWeekFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("month", false);
        jpqlFunctionGroup.add(null, new MonthFunction());
        jpqlFunctionGroup.add("access", new AccessMonthFunction());
        jpqlFunctionGroup.add("db2", new DB2MonthFunction());
        jpqlFunctionGroup.add("derby", new DerbyMonthFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMonthFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMonthFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("week", false);
        jpqlFunctionGroup.add(null, new IsoWeekFunction());
        jpqlFunctionGroup.add("access", new AccessIsoWeekFunction());
        jpqlFunctionGroup.add("db2", new DB2IsoWeekFunction());
        jpqlFunctionGroup.add("h2", new H2IsoWeekFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerIsoWeekFunction());
        jpqlFunctionGroup.add("sybase", new SybaseIsoWeekFunction());
        jpqlFunctionGroup.add("sqlite", new SqliteIsoWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLIsoWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLIsoWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleIsoWeekFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("iso_week", false);
        jpqlFunctionGroup.add(null, new IsoWeekFunction());
        jpqlFunctionGroup.add("access", new AccessIsoWeekFunction());
        jpqlFunctionGroup.add("db2", new DB2IsoWeekFunction());
        jpqlFunctionGroup.add("h2", new H2IsoWeekFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerIsoWeekFunction());
        jpqlFunctionGroup.add("sybase", new SybaseIsoWeekFunction());
        jpqlFunctionGroup.add("sqlite", new SqliteIsoWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLIsoWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLIsoWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleIsoWeekFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("week_in_year", false);
        jpqlFunctionGroup.add(null, new WeekInYearFunction());
        jpqlFunctionGroup.add("db2", new DB2WeekInYearFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerWeekInYearFunction());
        jpqlFunctionGroup.add("mysql", new MySQLWeekInYearFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLWeekInYearFunction());
        jpqlFunctionGroup.add("oracle", new OracleWeekInYearFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("quarter", false);
        jpqlFunctionGroup.add(null, new QuarterFunction());
        jpqlFunctionGroup.add("access", new AccessQuarterFunction());
        jpqlFunctionGroup.add("db2", new DB2QuarterFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerQuarterFunction());
        jpqlFunctionGroup.add("sybase", new SybaseQuarterFunction());
        jpqlFunctionGroup.add("sqlite", new SqliteQuarterFunction());
        jpqlFunctionGroup.add("oracle", new OracleQuarterFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("day", false);
        jpqlFunctionGroup.add(null, new DayFunction());
        jpqlFunctionGroup.add("access", new AccessDayFunction());
        jpqlFunctionGroup.add("db2", new DB2DayFunction());
        jpqlFunctionGroup.add("derby", new DerbyDayFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerDayFunction());
        jpqlFunctionGroup.add("sybase", new SybaseDayFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("dayofyear", false);
        jpqlFunctionGroup.add(null, new DayOfYearFunction());
        jpqlFunctionGroup.add("postgresql", new DayOfYearFunction());
        jpqlFunctionGroup.add("access", new AccessDayOfYearFunction());
        jpqlFunctionGroup.add("db2", new DB2DayOfYearFunction());
        jpqlFunctionGroup.add("mysql", new MySQLDayOfYearFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLDayOfYearFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerDayOfYearFunction());
        jpqlFunctionGroup.add("sybase", new SybaseDayOfYearFunction());
        jpqlFunctionGroup.add("oracle", new OracleDayOfYearFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("dayofweek", false);
        jpqlFunctionGroup.add(null, new DayOfWeekFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLDayOfWeekFunction());
        jpqlFunctionGroup.add("access", new AccessDayOfWeekFunction());
        jpqlFunctionGroup.add("db2", new DB2DayOfWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLDayOfWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLDayOfWeekFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerDayOfWeekFunction());
        jpqlFunctionGroup.add("sybase", new SybaseDayOfWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleDayOfWeekFunction());
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

        jpqlFunctionGroup = new JpqlFunctionGroup("millisecond", false);
        jpqlFunctionGroup.add(null, new MillisecondFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMillisecondFunction());
        jpqlFunctionGroup.add("db2", new DB2MillisecondFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMillisecondFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMillisecondFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMillisecondFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMillisecondFunction());
        jpqlFunctionGroup.add("oracle", new OracleMillisecondFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("microsecond", false);
        jpqlFunctionGroup.add(null, new MicrosecondFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMicrosecondFunction());
        jpqlFunctionGroup.add("db2", new DB2MicrosecondFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMicrosecondFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMicrosecondFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMicrosecondFunction());
        jpqlFunctionGroup.add("sybase", new SybaseMicrosecondFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("epoch", false);
        jpqlFunctionGroup.add(null, new DefaultEpochFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLEpochFunction());
        jpqlFunctionGroup.add("oracle", new OracleEpochFunction());
        jpqlFunctionGroup.add("db2", new DB2EpochFunction());
        jpqlFunctionGroup.add("mysql", new MySQLEpochFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLEpochFunction());
        registerFunction(jpqlFunctionGroup);

        // dateadd

        jpqlFunctionGroup = new JpqlFunctionGroup(DayAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new DayAddFunction());
        jpqlFunctionGroup.add("db2", new DB2DayAddFunction());
        jpqlFunctionGroup.add("h2", new H2DayAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLDayAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLDayAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLDayAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLDayAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleDayAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(HourAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new HourAddFunction());
        jpqlFunctionGroup.add("db2", new DB2HourAddFunction());
        jpqlFunctionGroup.add("h2", new H2HourAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLHourAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLHourAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLHourAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLHourAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleHourAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(MicrosecondsAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new MicrosecondsAddFunction());
        jpqlFunctionGroup.add("db2", new DB2MicrosecondsAddFunction());
        jpqlFunctionGroup.add("h2", new H2MicrosecondsAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLMicrosecondsAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMicrosecondsAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMicrosecondsAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMicrosecondsAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleMicrosecondsAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(MillisecondsAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new MillisecondsAddFunction());
        jpqlFunctionGroup.add("db2", new DB2MillisecondsAddFunction());
        jpqlFunctionGroup.add("h2", new H2MillisecondsAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLMillisecondsAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMillisecondsAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMillisecondsAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMillisecondsAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleMillisecondsAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(MinuteAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new MinuteAddFunction());
        jpqlFunctionGroup.add("db2", new DB2MinuteAddFunction());
        jpqlFunctionGroup.add("h2", new H2MinuteAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLMinuteAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMinuteAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMinuteAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMinuteAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleMinuteAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(MonthAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new MonthAddFunction());
        jpqlFunctionGroup.add("db2", new DB2MonthAddFunction());
        jpqlFunctionGroup.add("h2", new H2MonthAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLMonthAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMonthAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMonthAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMonthAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleMonthAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(QuarterAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new QuarterAddFunction());
        jpqlFunctionGroup.add("db2", new DB2QuarterAddFunction());
        jpqlFunctionGroup.add("h2", new H2QuarterAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLQuarterAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLQuarterAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLQuarterAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLQuarterAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleQuarterAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(SecondAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new SecondAddFunction());
        jpqlFunctionGroup.add("db2", new DB2SecondAddFunction());
        jpqlFunctionGroup.add("h2", new H2SecondAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLSecondAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLSecondAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLSecondAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLSecondAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleSecondAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(WeekAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new WeekAddFunction());
        jpqlFunctionGroup.add("db2", new DB2WeekAddFunction());
        jpqlFunctionGroup.add("h2", new H2WeekAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLWeekAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLWeekAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLWeekAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLWeekAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleWeekAddFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(YearAddFunction.NAME, false);
        jpqlFunctionGroup.add(null, new YearAddFunction());
        jpqlFunctionGroup.add("db2", new DB2YearAddFunction());
        jpqlFunctionGroup.add("h2", new H2YearAddFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLYearAddFunction());
        jpqlFunctionGroup.add("mysql", new MySQLYearAddFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLYearAddFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLYearAddFunction());
        jpqlFunctionGroup.add("oracle", new OracleYearAddFunction());
        registerFunction(jpqlFunctionGroup);

        // datediff

        jpqlFunctionGroup = new JpqlFunctionGroup("year_diff", false);
        jpqlFunctionGroup.add("access", new AccessYearDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2YearDiffFunction());
        jpqlFunctionGroup.add("h2", new DefaultYearDiffFunction());
        jpqlFunctionGroup.add("microsoft", new DefaultYearDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLYearDiffFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLYearDiffFunction());
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
        jpqlFunctionGroup.add("mysql8", new MySQLMonthDiffFunction());
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
        jpqlFunctionGroup.add("mysql8", new MySQLDayDiffFunction());
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
        jpqlFunctionGroup.add("mysql8", new MySQLHourDiffFunction());
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
        jpqlFunctionGroup.add("mysql8", new MySQLMinuteDiffFunction());
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
        jpqlFunctionGroup.add("mysql8", new MySQLSecondDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLSecondDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleSecondDiffFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup("millisecond_diff", false);
        jpqlFunctionGroup.add(null, new DefaultMillisecondDiffFunction());
        jpqlFunctionGroup.add("access", new AccessMillisecondDiffFunction());
        jpqlFunctionGroup.add("db2", new DB2MillisecondDiffFunction());
        jpqlFunctionGroup.add("microsoft", new SQLServerMillisecondDiffFunction());
        jpqlFunctionGroup.add("mysql", new MySQLMillisecondDiffFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLMillisecondDiffFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLMillisecondDiffFunction());
        jpqlFunctionGroup.add("oracle", new OracleMillisecondDiffFunction());
        registerFunction(jpqlFunctionGroup);

        // date trunc

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncDayFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncDayFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncDayFunction());
        jpqlFunctionGroup.add("h2", new H2TruncDayFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncDayFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncDayFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncDayFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncDayFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncDayFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncHourFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncHourFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncHourFunction());
        jpqlFunctionGroup.add("h2", new H2TruncHourFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncHourFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncHourFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncHourFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncHourFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncHourFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncMicrosecondsFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncMicrosecondsFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncMicrosecondsFunction());
        jpqlFunctionGroup.add("h2", new H2TruncMicrosecondsFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncMicrosecondsFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncMicrosecondsFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncMicrosecondsFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncMicrosecondsFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncMicrosecondsFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncMillisecondsFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncMillisecondsFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncMillisecondsFunction());
        jpqlFunctionGroup.add("h2", new H2TruncMillisecondsFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncMillisecondsFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncMillisecondsFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncMillisecondsFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncMillisecondsFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncMillisecondsFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncMinuteFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncMinuteFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncMinuteFunction());
        jpqlFunctionGroup.add("h2", new H2TruncMinuteFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncMinuteFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncMinuteFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncMinuteFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncMinuteFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncMinuteFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncMonthFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncMonthFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncMonthFunction());
        jpqlFunctionGroup.add("h2", new H2TruncMonthFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncMonthFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncMonthFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncMonthFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncMonthFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncMonthFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncQuarterFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncQuarterFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncQuarterFunction());
        jpqlFunctionGroup.add("h2", new H2TruncQuarterFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncQuarterFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncQuarterFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncQuarterFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncQuarterFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncQuarterFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncSecondFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncSecondFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncSecondFunction());
        jpqlFunctionGroup.add("h2", new H2TruncSecondFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncSecondFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncSecondFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncSecondFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncSecondFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncSecondFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncWeekFunction.NAME, false);
        jpqlFunctionGroup.add(null, new TruncWeekFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncWeekFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncWeekFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncWeekFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncWeekFunction());
        registerFunction(jpqlFunctionGroup);

        jpqlFunctionGroup = new JpqlFunctionGroup(TruncYearFunction.NAME, false);
        jpqlFunctionGroup.add(null, new PostgreSQLTruncYearFunction());
        jpqlFunctionGroup.add("db2", new DB2TruncYearFunction());
        jpqlFunctionGroup.add("h2", new H2TruncYearFunction());
        jpqlFunctionGroup.add("microsoft", new MSSQLTruncYearFunction());
        jpqlFunctionGroup.add("mysql", new MySQLTruncYearFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLTruncYearFunction());
        jpqlFunctionGroup.add("oracle", new OracleTruncYearFunction());
        jpqlFunctionGroup.add("postgresql", new PostgreSQLTruncYearFunction());
        registerFunction(jpqlFunctionGroup);

        // count

        jpqlFunctionGroup = new JpqlFunctionGroup(AbstractCountFunction.FUNCTION_NAME, true);
        jpqlFunctionGroup.add(null, new CountTupleFunction());
        jpqlFunctionGroup.add("mysql", new MySQLCountTupleFunction());
        jpqlFunctionGroup.add("mysql8", new MySQLCountTupleFunction());
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

        // every

        jpqlFunctionGroup = new JpqlFunctionGroup(EveryFunction.FUNCTION_NAME, true);
        jpqlFunctionGroup.add(null, EveryFunction.INSTANCE);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                            EveryFunction.INSTANCE :
                            FallbackEveryFunction.INSTANCE);
        }
        registerFunction(jpqlFunctionGroup);

        // andagg

        jpqlFunctionGroup = new JpqlFunctionGroup("AND_AGG", true);
        jpqlFunctionGroup.add(null, EveryFunction.INSTANCE);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                            EveryFunction.INSTANCE :
                            FallbackEveryFunction.INSTANCE);
        }
        registerFunction(jpqlFunctionGroup);

        // oragg

        jpqlFunctionGroup = new JpqlFunctionGroup(OrAggFunction.FUNCTION_NAME, true);
        jpqlFunctionGroup.add(null, OrAggFunction.INSTANCE);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                            OrAggFunction.INSTANCE :
                            FallbackOrAggFunction.INSTANCE);
        }
        registerFunction(jpqlFunctionGroup);


        // window every

        jpqlFunctionGroup = new JpqlFunctionGroup(WindowEveryFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                            new WindowEveryFunction(dialectEntry.getValue()) :
                            new FallbackWindowEveryFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window andagg

        jpqlFunctionGroup = new JpqlFunctionGroup("AND_AGG", false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                            new WindowEveryFunction(dialectEntry.getValue()) :
                            new FallbackWindowEveryFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window oragg

        jpqlFunctionGroup = new JpqlFunctionGroup(WindowOrAggFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(),
                    dialectEntry.getValue().supportsBooleanAggregation() ?
                        new WindowOrAggFunction(dialectEntry.getValue()) :
                        new FallbackWindowOrAggFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window sum

        jpqlFunctionGroup = new JpqlFunctionGroup(SumFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new SumFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window avg

        jpqlFunctionGroup = new JpqlFunctionGroup(AvgFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new AvgFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window min

        jpqlFunctionGroup = new JpqlFunctionGroup(MinFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new MinFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window max

        jpqlFunctionGroup = new JpqlFunctionGroup(MaxFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new MaxFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // window count

        jpqlFunctionGroup = new JpqlFunctionGroup(CountFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new CountFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // row number

        jpqlFunctionGroup = new JpqlFunctionGroup(RowNumberFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new RowNumberFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // rank

        jpqlFunctionGroup = new JpqlFunctionGroup(RankFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new RankFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // dense_rank

        jpqlFunctionGroup = new JpqlFunctionGroup(DenseRankFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new DenseRankFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // PERCENT_RANK

        jpqlFunctionGroup = new JpqlFunctionGroup(PercentRankFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new PercentRankFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // CUME_DIST

        jpqlFunctionGroup = new JpqlFunctionGroup(CumeDistFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new CumeDistFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // NTILE

        jpqlFunctionGroup = new JpqlFunctionGroup(NtileFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new NtileFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // LAG

        jpqlFunctionGroup = new JpqlFunctionGroup(LagFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new LagFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // LEAD

        jpqlFunctionGroup = new JpqlFunctionGroup(LeadFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new LeadFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // FIRST_VALUE

        jpqlFunctionGroup = new JpqlFunctionGroup(FirstValueFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new FirstValueFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // LAST_VALUE

        jpqlFunctionGroup = new JpqlFunctionGroup(LastValueFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new LastValueFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);

        // NTH_VALUE

        jpqlFunctionGroup = new JpqlFunctionGroup(NthValueFunction.FUNCTION_NAME, false);
        for (Map.Entry<String, DbmsDialect> dialectEntry : this.dbmsDialects.entrySet()) {
            jpqlFunctionGroup.add(dialectEntry.getKey(), new NthValueFunction(dialectEntry.getValue()));
        }
        registerFunction(jpqlFunctionGroup);
    }

    private void loadDbmsDialects() {
        registerDialect(null, new DefaultDbmsDialect());
        registerDialect("mysql", new MySQLDbmsDialect());
        registerDialect("mysql8", new MySQL8DbmsDialect());
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

    @Override
    public CriteriaBuilderConfiguration registerNamedType(String name, Class<?> type) {
        treatTypes.put(name, type);
        registerFunction(new JpqlFunctionGroup("treat_" + name.toLowerCase(), new TreatFunction(type)));
        return this;
    }

    @Override
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
