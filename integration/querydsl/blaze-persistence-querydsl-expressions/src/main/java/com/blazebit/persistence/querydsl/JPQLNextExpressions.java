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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLOps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility methods for creating JPQL.next expressions
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class JPQLNextExpressions {

    private static final Map<DatePart, Operator> DATE_ADD_OPS
            = new EnumMap<DatePart, Operator>(DatePart.class);
    private static final Map<DatePart, Operator> DATE_DIFF_OPS
                    = new EnumMap<DatePart, Operator>(DatePart.class);
    private static final Map<DatePart, Operator> DATE_TRUNC_OPS
                            = new EnumMap<DatePart, Operator>(DatePart.class);
    
    static {
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.year, Ops.DateTimeOps.ADD_YEARS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.month, Ops.DateTimeOps.ADD_MONTHS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.week, Ops.DateTimeOps.ADD_WEEKS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.day, Ops.DateTimeOps.ADD_DAYS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.hour, Ops.DateTimeOps.ADD_HOURS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.minute, Ops.DateTimeOps.ADD_MINUTES);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.second, Ops.DateTimeOps.ADD_SECONDS);
        JPQLNextExpressions.DATE_ADD_OPS.put(DatePart.millisecond, null); // TODO

        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.year, Ops.DateTimeOps.DIFF_YEARS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.month, Ops.DateTimeOps.DIFF_MONTHS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.week, Ops.DateTimeOps.DIFF_WEEKS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.day, Ops.DateTimeOps.DIFF_DAYS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.hour, Ops.DateTimeOps.DIFF_HOURS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.minute, Ops.DateTimeOps.DIFF_MINUTES);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.second, Ops.DateTimeOps.DIFF_SECONDS);
        JPQLNextExpressions.DATE_DIFF_OPS.put(DatePart.millisecond, null); // TODO

        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.year, Ops.DateTimeOps.TRUNC_YEAR);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.month, Ops.DateTimeOps.TRUNC_MONTH);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.week, Ops.DateTimeOps.TRUNC_WEEK);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.day, Ops.DateTimeOps.TRUNC_DAY);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.hour, Ops.DateTimeOps.TRUNC_HOUR);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.minute, Ops.DateTimeOps.TRUNC_MINUTE);
        JPQLNextExpressions.DATE_TRUNC_OPS.put(DatePart.second, Ops.DateTimeOps.TRUNC_SECOND);
    }

    protected JPQLNextExpressions() {
    }

    /**
     * Create a new detached JPQLQuery instance with the given projection
     *
     * @param expr projection
     * @param <T> result type
     * @return select(expr)
     * @see JPAExpressions#select(Expression)
     */
    public static <T> BlazeJPAQuery<T> select(Expression<T> expr) {
        return new BlazeJPAQuery<>().select(expr);
    }

    /**
     * Create a new detached JPQLQuery instance with the given projection
     *
     * @param exprs projection
     * @return select(exprs)
     * @see JPAExpressions#select(Expression[])
     */
    public static BlazeJPAQuery<Tuple> select(Expression<?>... exprs) {
        return new BlazeJPAQuery<Void>().select(exprs);
    }


    /**
     * Create a new detached JPQLQuery instance with the given projection
     *
     * @param expr projection
     * @param <T> result type
     * @return select(distinct expr)
     * @see com.querydsl.jpa.JPAExpressions#selectDistinct(Expression)
     */
    public static <T> BlazeJPAQuery<T> selectDistinct(Expression<T> expr) {
        return new BlazeJPAQuery<Void>().select(expr).distinct();
    }

    /**
     * Create a new detached JPQLQuery instance with the given projection
     *
     * @param exprs projection
     * @return select(distinct expr)
     * @see com.querydsl.jpa.JPAExpressions#selectDistinct(Expression[])
     */
    public static BlazeJPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return new BlazeJPAQuery<Void>().select(exprs).distinct();
    }

    /**
     * Create a new detached JPQLQuery instance with the projection zero
     *
     * @return select(0)
     * @see JPAExpressions#selectZero()
     */
    public static BlazeJPAQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    /**
     * Create a new detached JPQLQuery instance with the projection one
     *
     * @return select(1)
     * @see JPAExpressions#selectOne()
     */
    public static BlazeJPAQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    /**
     * Create a new detached JPQLQuery instance with the given projection
     *
     * @param expr projection and source
     * @param <T> result type
     * @return select(expr).from(expr)
     */
    public static <T> BlazeJPAQuery<T> selectFrom(EntityPath<T> expr) {
        return select(expr).from(expr);
    }

    /**
     * Create a avg(col) expression
     *
     * @param col collection
     * @param <A> collection element type
     * @return avg(col)
     * @see JPAExpressions#avg(CollectionExpression)
     */
    public static <A extends Comparable<? super A>> ComparableExpression<A> avg(CollectionExpression<?, A> col) {
        return Expressions.comparableOperation((Class) col.getParameter(0), Ops.QuantOps.AVG_IN_COL, (Expression<?>) col);
    }

    /**
     * Create a max(col) expression
     *
     * @param left collection
     * @param <A> collection element type
     * @return max(col)
     * @see JPAExpressions#max(CollectionExpression)
     */
    public static <A extends Comparable<? super A>> ComparableExpression<A> max(CollectionExpression<?,A> left) {
        return Expressions.comparableOperation((Class) left.getParameter(0), Ops.QuantOps.MAX_IN_COL, (Expression<?>) left);
    }

    /**
     * Create a min(col) expression
     *
     * @param left collection
     * @param <A> collection element type
     * @return min(col)
     * @see JPAExpressions#min(CollectionExpression)
     */
    public static <A extends Comparable<? super A>> ComparableExpression<A> min(CollectionExpression<?,A> left) {
        return Expressions.comparableOperation((Class) left.getParameter(0), Ops.QuantOps.MIN_IN_COL, (Expression<?>) left);
    }

    /**
     * Create a type(path) expression
     *
     * @param path entity
     * @return type(path)
     * @see JPAExpressions#type(EntityPath)
     */
    public static StringExpression type(EntityPath<?> path) {
        return Expressions.stringOperation(JPQLOps.TYPE, path);
    }

    /**
     * Create a dateadd(unit, date, amount) expression
     *
     * @param unit date part
     * @param date date
     * @param amount amount
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#dateadd(com.querydsl.sql.DatePart, DateExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> dateadd(DatePart unit, DateTimeExpression<D> date, int amount) {
        return Expressions.dateTimeOperation(date.getType(), DATE_ADD_OPS.get(unit), date, ConstantImpl.create(amount));
    }

    /**
     * Create a dateadd(unit, date, amount) expression
     *
     * @param unit date part
     * @param date date
     * @param amount amount
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#dateadd(com.querydsl.sql.DatePart, DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> dateadd(DatePart unit, DateExpression<D> date, int amount) {
        return Expressions.dateOperation(date.getType(), DATE_ADD_OPS.get(unit), date, ConstantImpl.create(amount));
    }

    /**
     * Create a dateadd(unit, date, amountExpression) expression
     *
     * @param unit date part
     * @param date date
     * @param amountExpression amountExpression
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#dateadd(com.querydsl.sql.DatePart, DateExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> dateadd(DatePart unit, DateTimeExpression<D> date, Expression<Integer> amountExpression) {
        return Expressions.dateTimeOperation(date.getType(), DATE_ADD_OPS.get(unit), date, amountExpression);
    }

    /**
     * Create a dateadd(unit, date, amountExpression) expression
     *
     * @param unit date part
     * @param date date
     * @param amountExpression amountExpression
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#dateadd(com.querydsl.sql.DatePart, DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> dateadd(DatePart unit, DateExpression<D> date,  Expression<Integer> amountExpression) {
        return Expressions.dateOperation(date.getType(), DATE_ADD_OPS.get(unit), date, amountExpression);
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, DateExpression, DateExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                               DateExpression<D> start, DateExpression<D> end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), start, end);
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, Comparable, DateExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                            D start, DateExpression<D> end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), ConstantImpl.create(start), end);
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, Comparable, DateExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                            DateExpression<D> start, D end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), start, ConstantImpl.create(end));
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, Comparable, DateTimeExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                            DateTimeExpression<D> start, DateTimeExpression<D> end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), start, end);
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, Comparable, DateTimeExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                            D start, DateTimeExpression<D> end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), ConstantImpl.create(start), end);
    }

    /**
     * Get a datediff(unit, start, end) expression
     *
     * @param unit date part
     * @param start start
     * @param end end
     * @param <D> date type
     * @return difference in units
     * @see com.querydsl.sql.SQLExpressions#datediff(com.querydsl.sql.DatePart, Comparable, DateTimeExpression)
     */
    public static <D extends Comparable<?>> NumberExpression<Integer> datediff(DatePart unit,
                                                                            DateTimeExpression<D> start, D end) {
        return Expressions.numberOperation(Integer.class, DATE_DIFF_OPS.get(unit), start, ConstantImpl.create(end));
    }

    /**
     * Truncate the given date expression
     *
     * @param unit date part to truncate to
     * @param expr truncated date
     * @param <D> date type
     * @return date trunc expression
     * @see com.querydsl.sql.SQLExpressions#datetrunc(com.querydsl.sql.DatePart, DateExpression)
     */
    public static <D extends Comparable<?>> DateExpression<D> datetrunc(DatePart unit, DateExpression<D> expr) {
        return Expressions.dateOperation(expr.getType(), DATE_TRUNC_OPS.get(unit), expr);
    }

    /**
     * Truncate the given datetime expression
     *
     * @param unit com.querydsl.sql.DatePart to truncate to
     * @param expr truncated datetime
     * @param <D> date type
     * @return date trunc expression
     * @see com.querydsl.sql.SQLExpressions#datetrunc(com.querydsl.sql.DatePart, DateTimeExpression)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> datetrunc(DatePart unit, DateTimeExpression<D> expr) {
        return Expressions.dateTimeOperation(expr.getType(), DATE_TRUNC_OPS.get(unit), expr);
    }

    /**
     * Add the given amount of years to the date
     *
     * @param date datetime
     * @param years years to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addYears(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addYears(DateTimeExpression<D> date, int years) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_YEARS, date, ConstantImpl.create(years));
    }

    /**
     * Add the given amount of months to the date
     *
     * @param date datetime
     * @param months months to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addYears(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addMonths(DateTimeExpression<D> date, int months) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_MONTHS, date, ConstantImpl.create(months));
    }

    /**
     * Add the given amount of weeks to the date
     *
     * @param date datetime
     * @param weeks weeks to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addWeeks(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addWeeks(DateTimeExpression<D> date, int weeks) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_WEEKS, date, ConstantImpl.create(weeks));
    }

    /**
     * Add the given amount of days to the date
     *
     * @param date datetime
     * @param days days to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addDays(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addDays(DateTimeExpression<D> date, int days) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_DAYS, date, ConstantImpl.create(days));
    }

    /**
     * Add the given amount of hours to the date
     *
     * @param date datetime
     * @param hours hours to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addHours(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addHours(DateTimeExpression<D> date, int hours) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_HOURS, date, ConstantImpl.create(hours));
    }

    /**
     * Add the given amount of minutes to the date
     *
     * @param date datetime
     * @param minutes minutes to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addMinutes(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addMinutes(DateTimeExpression<D> date, int minutes) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_MINUTES, date, ConstantImpl.create(minutes));
    }

    /**
     * Add the given amount of seconds to the date
     *
     * @param date datetime
     * @param seconds seconds to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addSeconds(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addSeconds(DateTimeExpression<D> date, int seconds) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_SECONDS, date, ConstantImpl.create(seconds));
    }

    /**
     * Add the given amount of years to the date
     *
     * @param date date
     * @param years years to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addYears(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addYears(DateExpression<D> date, int years) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_YEARS, date, ConstantImpl.create(years));
    }

    /**
     * Add the given amount of months to the date
     *
     * @param date date
     * @param months months to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addMonths(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addMonths(DateExpression<D> date, int months) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_MONTHS, date, ConstantImpl.create(months));
    }

    /**
     * Add the given amount of weeks to the date
     *
     * @param date date
     * @param weeks weeks to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addWeeks(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addWeeks(DateExpression<D> date, int weeks) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_WEEKS, date, ConstantImpl.create(weeks));
    }

    /**
     * Add the given amount of days to the date
     *
     * @param date date
     * @param days days to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addDays(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addDays(DateExpression<D> date, int days) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_DAYS, date, ConstantImpl.create(days));
    }

    /**
     * Add the given amount of years to the date
     *
     * @param date datetime
     * @param years years to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addYears(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addYears(DateTimeExpression<D> date, Expression<Integer> years) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_YEARS, date, years);
    }

    /**
     * Add the given amount of months to the date
     *
     * @param date datetime
     * @param months months to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addYears(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addMonths(DateTimeExpression<D> date, Expression<Integer> months) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_MONTHS, date, months);
    }

    /**
     * Add the given amount of weeks to the date
     *
     * @param date datetime
     * @param weeks weeks to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addWeeks(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addWeeks(DateTimeExpression<D> date, Expression<Integer> weeks) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_WEEKS, date, weeks);
    }

    /**
     * Add the given amount of days to the date
     *
     * @param date datetime
     * @param days days to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addDays(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addDays(DateTimeExpression<D> date, Expression<Integer> days) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_DAYS, date, days);
    }

    /**
     * Add the given amount of hours to the date
     *
     * @param date datetime
     * @param hours hours to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addHours(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addHours(DateTimeExpression<D> date, Expression<Integer> hours) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_HOURS, date, hours);
    }

    /**
     * Add the given amount of minutes to the date
     *
     * @param date datetime
     * @param minutes minutes to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addMinutes(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addMinutes(DateTimeExpression<D> date, Expression<Integer> minutes) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_MINUTES, date, minutes);
    }

    /**
     * Add the given amount of seconds to the date
     *
     * @param date datetime
     * @param seconds seconds to add
     * @param <D> date type
     * @return converted datetime
     * @see com.querydsl.sql.SQLExpressions#addSeconds(DateTimeExpression, int)
     */
    public static <D extends Comparable<?>> DateTimeExpression<D> addSeconds(DateTimeExpression<D> date, Expression<Integer> seconds) {
        return Expressions.dateTimeOperation(date.getType(), Ops.DateTimeOps.ADD_SECONDS, date, seconds);
    }

    /**
     * Add the given amount of years to the date
     *
     * @param date date
     * @param years years to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addYears(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addYears(DateExpression<D> date, Expression<Integer> years) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_YEARS, date, years);
    }

    /**
     * Add the given amount of months to the date
     *
     * @param date date
     * @param months months to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addMonths(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addMonths(DateExpression<D> date, Expression<Integer> months) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_MONTHS, date, months);
    }

    /**
     * Add the given amount of weeks to the date
     *
     * @param date date
     * @param weeks weeks to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addWeeks(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addWeeks(DateExpression<D> date, Expression<Integer> weeks) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_WEEKS, date, weeks);
    }

    /**
     * Add the given amount of days to the date
     *
     * @param date date
     * @param days days to add
     * @param <D> date type
     * @return converted date
     * @see com.querydsl.sql.SQLExpressions#addDays(DateExpression, int)
     */
    public static <D extends Comparable<?>> DateExpression<D> addDays(DateExpression<D> date, Expression<Integer> days) {
        return Expressions.dateOperation(date.getType(), Ops.DateTimeOps.ADD_DAYS, date, days);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @return all(expr)
     */
    public static FilterableWindowOver<Boolean> all(Expression<Boolean> expr) {
        return new FilterableWindowOver<Boolean>(expr.getType(), Ops.AggOps.BOOLEAN_ALL, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @return any(expr)
     */
    public static FilterableWindowOver<Boolean> any(Expression<Boolean> expr) {
        return new FilterableWindowOver<Boolean>(expr.getType(), Ops.AggOps.BOOLEAN_ANY, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @param <T> number expression type
     * @return sum(expr)
     */
    public static <T extends Number> FilterableWindowOver<T> sum(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), Ops.AggOps.SUM_AGG, expr);
    }

    /**
     * Start a window function expression
     *
     * @return count()
     */
    public static FilterableWindowOver<Long> count() {
        return new FilterableWindowOver<Long>(Long.class, Ops.AggOps.COUNT_ALL_AGG);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @return count(expr)
     */
    public static FilterableWindowOver<Long> count(Expression<?> expr) {
        return new FilterableWindowOver<Long>(Long.class, Ops.AggOps.COUNT_AGG, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @return count(distinct expr)
     */
    public static FilterableWindowOver<Long> countDistinct(Expression<?> expr) {
        return new FilterableWindowOver<Long>(Long.class, Ops.AggOps.COUNT_DISTINCT_AGG, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @param <T> number expression type
     * @return avg(expr)
     */
    public static <T extends Number> FilterableWindowOver<T> avg(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), Ops.AggOps.AVG_AGG, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @param <T> number expression type
     * @return min(expr)
     */
    public static <T extends Comparable<? super T>> FilterableWindowOver<T> min(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), Ops.AggOps.MIN_AGG, expr);
    }

    /**
     * Start a window function expression
     *
     * @param expr expression
     * @param <T> number expression type
     * @return max(expr)
     */
    public static <T extends Comparable<? super T>> FilterableWindowOver<T> max(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), Ops.AggOps.MAX_AGG, expr);
    }

    /**
     * expr evaluated at the row that is one row after the current row within the partition;
     *
     * @param expr expression
     * @param <T> number expression type
     * @return lead(expr)
     */
    public static <T> FilterableWindowOver<T> lead(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), JPQLNextOps.LEAD, expr);
    }

    /**
     * expr evaluated at the row that is one row before the current row within the partition
     *
     * @param expr expression
     * @param <T> number expression type
     * @return lag(expr)
     */
    public static <T> FilterableWindowOver<T> lag(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), JPQLNextOps.LAG, expr);
    }

    /**
     * NTH_VALUE returns the expr value of the nth row in the window defined by the analytic clause.
     * The returned value has the data type of the expr.
     *
     * @param expr measure expression
     * @param <T> number expression type
     * @param n one based row index
     * @return nth_value(expr, n)
     */
    public static <T> FilterableWindowOver<T> nthValue(Expression<T> expr, Number n) {
        return nthValue(expr, ConstantImpl.create(n));
    }

    /**
     * NTH_VALUE returns the expr value of the nth row in the window defined by the analytic clause.
     * The returned value has the data type of the expr
     *
     * @param expr measure expression
     * @param n one based row index
     * @param <T> number expression type
     * @return nth_value(expr, n)
     */
    public static <T> FilterableWindowOver<T> nthValue(Expression<T> expr, Expression<? extends Number> n) {
        return new FilterableWindowOver<T>(expr.getType(), JPQLNextOps.NTH_VALUE, expr, n);
    }

    /**
     * divides an ordered data set into a number of buckets indicated by expr and assigns the
     * appropriate bucket number to each row
     *
     * @param num bucket size
     * @param <T> number expression type
     * @return ntile(num)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number & Comparable> FilterableWindowOver<T> ntile(T num) {
        return new FilterableWindowOver<T>((Class<T>) num.getClass(), JPQLNextOps.NTILE, ConstantImpl.create(num));
    }

    /**
     * rank of the current row with gaps; same as row_number of its first peer
     *
     * @return rank()
     */
    public static FilterableWindowOver<Long> rank() {
        return new FilterableWindowOver<Long>(Long.class, JPQLNextOps.RANK);
    }

    /**
     * rank of the current row without gaps; this function counts peer groups
     *
     * @return dense_rank()
     */
    public static FilterableWindowOver<Long> denseRank() {
        return new FilterableWindowOver<Long>(Long.class, JPQLNextOps.DENSE_RANK);
    }

    /**
     * As an analytic function, for a row r, PERCENT_RANK calculates the rank of r minus 1, divided by
     * 1 less than the number of rows being evaluated (the entire query result set or a partition).
     *
     * @return percent_rank()
     */
    public static FilterableWindowOver<Double> percentRank() {
        return new FilterableWindowOver<Double>(Double.class, JPQLNextOps.PERCENT_RANK);
    }

    /**
     * CUME_DIST calculates the cumulative distribution of a value in a group of values.
     *
     * @return cume_dist()
     */
    public static FilterableWindowOver<Double> cumeDist() {
        return new FilterableWindowOver<Double>(Double.class, JPQLNextOps.CUME_DIST);
    }

    /**
     * number of the current row within its partition, counting from 1
     *
     * @return row_number()
     */
    public static FilterableWindowOver<Long> rowNumber() {
        return new FilterableWindowOver<Long>(Long.class, JPQLNextOps.ROW_NUMBER);
    }

    /**
     * returns value evaluated at the row that is the first row of the window frame
     *
     * @param expr argument
     * @param <T> number expression type
     * @return first_value(expr)
     */
    public static <T> FilterableWindowOver<T> firstValue(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), JPQLNextOps.FIRST_VALUE, expr);
    }

    /**
     * returns value evaluated at the row that is the last row of the window frame
     *
     * @param expr argument
     * @param <T> number expression type
     * @return last_value(expr)
     */
    public static <T> FilterableWindowOver<T> lastValue(Expression<T> expr) {
        return new FilterableWindowOver<T>(expr.getType(), JPQLNextOps.LAST_VALUE, expr);
    }

    /**
     * Returns the greatest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return greatest operation
     */
    public static <T extends Comparable<? super T>> Expression<T> greatest(T a, Expression<T> b) {
        return greatest(Expressions.constant(a), b);
    }

    /**
     * Returns the greatest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return greatest operation
     */
    public static <T extends Comparable<? super T>> Expression<T> greatest(Expression<T> a, T b) {
        return greatest(a, Expressions.constant(b));
    }

    /**
     * Returns the greatest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return greatest operation
     */
    public static <T extends Comparable<? super T>> Expression<T> greatest(Expression<T> a, Expression<T> b) {
        return Expressions.operation(a.getType(), JPQLNextOps.GREATEST, a, b);
    }
    /**
     * Returns the smallest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return least operation
     */
    public static <T extends Comparable<? super T>> Expression<T> least(T a, Expression<T> b) {
        return least(Expressions.constant(a), b);
    }

    /**
     * Returns the smallest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return least operation
     */
    public static <T extends Comparable<? super T>> Expression<T> least(Expression<T> a, T b) {
        return least(a, Expressions.constant(b));
    }

    /**
     * Returns the smallest value of all given arguments.
     *
     * @param a lhs
     * @param b rhs
     * @param <T> type
     * @return least operation
     */
    public static <T extends Comparable<? super T>> Expression<T> least(Expression<T> a, Expression<T> b) {
        return Expressions.operation(a.getType(), JPQLNextOps.LEAST, a, b);
    }

    /**
     * Create a literal expression for the specified value.
     *
     * @param clasz type
     * @param value value to render as literal
     * @param <T> value type
     * @return the literal value
     */
    public static <T extends Comparable<? super T>> Expression<T> literal(Class<T> clasz, T value) {
        return (Expression) Expressions.template(clasz, JPQLNextTemplates.DEFAULT.asLiteral(value));
    }

    /**
     * Create a literal expression for the specified value.
     *
     * @param value value to render as literal
     * @param <T> value type
     * @return the literal value
     */
    public static <T extends Comparable<? super T>> Expression<T> literal(T value) {
        return (Expression) Expressions.template(value.getClass(), JPQLNextTemplates.DEFAULT.asLiteral(value));
    }

    /**
     * Aggregates/concatenates the values produced by expression to a single string separated by {@code separator} in the order defined by the {@code orderSpecifiers}.
     *
     * @param expression Expression to aggregate
     * @param separator Separator to aggregate by
     * @param orderSpecifiers Order specificers for the values
     * @return Group concat expression
     */
    public static StringExpression groupConcat(Expression<?> expression, String separator, OrderSpecifier<?>... orderSpecifiers) {
        return groupConcat(false, expression, ConstantImpl.create(separator), orderSpecifiers);
    }

    /**
     * Aggregates/concatenates the values produced by expression to a single string separated by {@code separator} in the order defined by the {@code orderSpecifiers}.
     *
     * @param expression Expression to aggregate
     * @param separator Separator to aggregate by
     * @param orderSpecifiers Order specificers for the values
     * @return Group concat expression
     */
    public static StringExpression groupConcat(Expression<?> expression, Expression<String> separator, OrderSpecifier<?>... orderSpecifiers) {
        return groupConcat(false, expression, separator, orderSpecifiers);
    }

    /**
     * Aggregates/concatenates the values produced by expression to a single string separated by {@code separator} in the order defined by the {@code orderSpecifiers}.
     *
     * @param distinct Filter unique results
     * @param expression Expression to aggregate
     * @param separator Separator to aggregate by
     * @param orderSpecifiers Order specificers for the values
     * @return Group concat expression
     */
    public static StringExpression groupConcat(boolean distinct, Expression<?> expression, String separator, OrderSpecifier<?>... orderSpecifiers) {
        return groupConcat(distinct, expression, ConstantImpl.create(separator), orderSpecifiers);
    }

    /**
     * Aggregates/concatenates the values produced by expression to a single string separated by {@code separator} in the order defined by the {@code orderSpecifiers}.
     *
     * @param distinct Filter unique results
     * @param expression Expression to aggregate
     * @param separator Separator to aggregate by
     * @param orderSpecifiers Order specificers for the values
     * @return Group concat expression
     */
    public static StringExpression groupConcat(boolean distinct, Expression<?> expression, Expression<String> separator, OrderSpecifier<?>... orderSpecifiers) {
        StringBuilder template = new StringBuilder();
        Expression<?>[] arguments = new Expression[2 + orderSpecifiers.length * 2];
        arguments[0] = expression;
        arguments[1] = separator;

        template.append("GROUP_CONCAT(");
        if (distinct) {
            template.append("'DISTINCT', ");
        }
        template.append("{0}, 'SEPARATOR', '{1s}'"); // {1s}?

        for (int i = 0; i < orderSpecifiers.length; i++) {
            if (i == 0) {
                template.append(", 'ORDER BY");
            }
            OrderSpecifier<?> orderSpecifier = orderSpecifiers[i];
            int expressionIndex = i * 2 + 2;
            int orderIndex = expressionIndex + 1;
            template.append(", {").append(expressionIndex).append("}, {").append(orderIndex).append("s}");
            arguments[expressionIndex] = orderSpecifier.getTarget();
            arguments[orderIndex] = ConstantImpl.create(orderSpecifier.getOrder().name());
        }

        template.append(")");
        return Expressions.stringTemplate(template.toString(), Arrays.asList(arguments));
    }

    /**
     * A cast invocation will always generate a ANSI SQL cast.
     * The SQL data type for a Java type is determined by {@code DbmsDialect.getSqlType()}.
     * By providing a custom DBMS dialect you can override these types.
     *
     * @param result Type to cast the value as
     * @param expression Expression
     * @param <T> Expression type
     * @return The casted expression
     */
    public static <T> Expression<T> cast(Class<T> result, Expression<?> expression) {
        if (Boolean.class.equals(result) || boolean.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_BOOLEAN, expression);
        } else if (Byte.class.equals(result) || byte.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_BYTE, expression);
        } else if (Short.class.equals(result) || short.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_SHORT, expression);
        } else if (Long.class.equals(result) || long.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_LONG, expression);
        } else if (Integer.class.equals(result) || int.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_INTEGER, expression);
        } else if (Float.class.equals(result) || float.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_FLOAT, expression);
        } else if (Double.class.equals(result) || double.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_DOUBLE, expression);
        } else if (Character.class.equals(result) || char.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_CHARACTER, expression);
        } else if (String.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_STRING, expression);
        } else if (BigInteger.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_BIGINTEGER, expression);
        } else if (BigDecimal.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_BIGDECIMAL, expression);
        } else if (Calendar.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_CALENDAR, expression);
        } else if (Timestamp.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_TIMESTAMP, expression);
        } else if (Time.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_TIME, expression);
        } else if (Date.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.CAST_DATE, expression);
        } else {
            throw new IllegalArgumentException("No cast operation for " + result.getName());
        }
    }

    /**
     * A treat invocation will only adjust the type of the expression in the JPQL expression and not cause an explicit cast on the DBMS side.
     * This can be used for cases when the type of an expression is actually known but can’t be inferred.
     *
     * <em>This function is used internally and no user should ever have the need for this!</em>
     *
     * @param result Type to treat the value as
     * @param expression Expression
     * @param <T> Expression type
     * @return The treated expression
     */
    public static <T> Expression<T> treat(Class<T> result, Expression<?> expression) {
        if (Boolean.class.equals(result) || boolean.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_BOOLEAN, expression);
        } else if (Byte.class.equals(result) || byte.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_BYTE, expression);
        } else if (Short.class.equals(result) || short.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_SHORT, expression);
        } else if (Long.class.equals(result) || long.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_LONG, expression);
        } else if (Integer.class.equals(result) || int.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_INTEGER, expression);
        } else if (Float.class.equals(result) || float.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_FLOAT, expression);
        } else if (Double.class.equals(result) || double.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_DOUBLE, expression);
        } else if (Character.class.equals(result) || char.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_CHARACTER, expression);
        } else if (String.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_STRING, expression);
        } else if (BigInteger.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_BIGINTEGER, expression);
        } else if (BigDecimal.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_BIGDECIMAL, expression);
        } else if (Calendar.class.equals(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_CALENDAR, expression);
        } else if (Timestamp.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_TIMESTAMP, expression);
        } else if (Time.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_TIME, expression);
        } else if (Date.class.isAssignableFrom(result)) {
            return Expressions.simpleOperation(result, JPQLNextOps.TREAT_DATE, expression);
        } else {
            throw new IllegalArgumentException("No cast operation for " + result.getName());
        }
    }

    /**
     * Create a CTE bind expression.
     *
     * @param path Path expression to bind
     * @param expression Value to which path should be bound
     * @param <T> Common type for path  and value
     * @return Operation result
     */
    public static <T> Operation<T> bind(Path<? super T> path, Expression<T> expression) {
        return ExpressionUtils.operation(expression.getType(), JPQLNextOps.BIND, expression, path);
    }

}
