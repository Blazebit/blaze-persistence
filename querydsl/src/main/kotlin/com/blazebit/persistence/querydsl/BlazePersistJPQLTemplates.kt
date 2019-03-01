package com.blazebit.persistence.querydsl

import com.mysema.query.jpa.JPQLTemplates
import com.mysema.query.types.Ops
import com.mysema.query.types.Templates

class BlazePersistJPQLTemplates : JPQLTemplates {

    companion object {
        val INSTANCE = BlazePersistJPQLTemplates()
    }

    constructor() : super() {
        add(Operators.GREATEST, "FUNCTION('greatest', {0}, {1})")
        add(Operators.LEAST, "FUNCTION('least', {0}, {1})")

        add(Ops.MathOps.ACOS, "function('acos', {0})")
        add(Ops.MathOps.ASIN, "function('asin', {0})")
        add(Ops.MathOps.ATAN, "function('atan', {0})")
        add(Ops.MathOps.CEIL, "function('ceil', {0})")
        add(Ops.MathOps.COS, "function('cos', {0})")
        add(Ops.MathOps.COSH, "function('cosh', {0})")
        add(Ops.MathOps.COT, "function('cot', {0})")
        add(Ops.MathOps.COTH, "function('coth', {0})")
        add(Ops.MathOps.DEG, "function('degrees', {0})")
        add(Ops.MathOps.TAN, "function('tan', {0})")
        add(Ops.MathOps.TANH, "function('tanh', {0})")
        add(Ops.MathOps.SIGN, "function('sign', {0})")
        add(Ops.MathOps.SIN, "function('sin', {0})")
        add(Ops.MathOps.SINH, "function('sinh', {0})")
        add(Ops.MathOps.ROUND, "function('round', {0})")
        add(Ops.MathOps.ROUND2, "function('round', {0},{1})")
        add(Ops.MathOps.RAD, "function('radians', {0})")
        add(Ops.MathOps.RANDOM, "function('random')")
        add(Ops.MathOps.RANDOM2, "function('random', {0})")
        add(Ops.MathOps.POWER, "function('pow', {0},{1})")
        add(Ops.MathOps.LOG, "function('log', {0},{1})")
        add(Ops.MathOps.LN, "function('ln', {0})")
        add(Ops.MathOps.FLOOR, "function('floor', {0})")
        add(Ops.MathOps.EXP, "function('exp', {0})")

        add(Ops.StringOps.LEFT, "function('left', {0},{1})")
        add(Ops.StringOps.RIGHT, "function('right', {0},{1})")
        add(Ops.StringOps.LTRIM, "function('ltrim', {0})")
        add(Ops.StringOps.RTRIM, "function('rtrim', {0})")
        add(Ops.StringOps.LOCATE, "function('locate', {0},{1})")
        add(Ops.StringOps.LOCATE2, "function('locate', {0},{1},{2s})")
        add(Ops.StringOps.LPAD, "function('lpad', {0},{1})")
        add(Ops.StringOps.RPAD, "function('rpad', {0},{1})")
        add(Ops.StringOps.LPAD2, "function('lpad', {0},{1},'{2s}')")
        add(Ops.StringOps.RPAD2, "function('rpad', {0},{1},'{2s}')")

        add(Ops.DateTimeOps.MILLISECOND, "function('millisecond', {0})")
        add(Ops.DateTimeOps.SECOND, "function('second', {0})")
        add(Ops.DateTimeOps.MINUTE, "function('minute', {0})")
        add(Ops.DateTimeOps.HOUR, "function('hour', {0})")
        add(Ops.DateTimeOps.WEEK, "function('week', {0})")
        add(Ops.DateTimeOps.MONTH, "function('month', {0})")
        add(Ops.DateTimeOps.YEAR, "function('year', {0})")
        add(Ops.DateTimeOps.YEAR_MONTH, "function('yearMonth', {0})")
        add(Ops.DateTimeOps.YEAR_WEEK, "function('yearweek', {0})")
        add(Ops.DateTimeOps.DAY_OF_WEEK, "function('dayofweek', {0})")
        add(Ops.DateTimeOps.DAY_OF_MONTH, "function('day', {0})")
        add(Ops.DateTimeOps.DAY_OF_YEAR, "function('dayofyear', {0})")
        add(Ops.DateTimeOps.YEAR_MONTH, "function('year', {0}) * 100 + function('month', {0})", Templates.Precedence.ARITH_LOW)
        add(Ops.DateTimeOps.YEAR_WEEK, "function('year', {0}) * 100 + function('week', {0})", Templates.Precedence.ARITH_LOW)

        add(Ops.DateTimeOps.TRUNC_YEAR, "function('trunc_year', {0})")
        add(Ops.DateTimeOps.TRUNC_MONTH, "function('trunc_month', {0})")
        add(Ops.DateTimeOps.TRUNC_WEEK, "function('trunc_week', {0})")
        add(Ops.DateTimeOps.TRUNC_DAY, "function('trunc_day', {0})")
        add(Ops.DateTimeOps.TRUNC_HOUR, "function('trunc_hour', {0})")
        add(Ops.DateTimeOps.TRUNC_MINUTE, "function('trunc_minute', {0})")
        add(Ops.DateTimeOps.TRUNC_SECOND, "function('trunc_second', {0})")

        add(Ops.DateTimeOps.DIFF_YEARS, "function('YEAR_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_MONTHS, "function('MONTH_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_WEEKS, "function('WEEK_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_DAYS, "function('DAY_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_HOURS, "function('HOUR_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_MINUTES, "function('MINUTE_DIFF', {0},{1})")
        add(Ops.DateTimeOps.DIFF_SECONDS, "function('SECOND_DIFF', {0},{1})")


        add(Ops.AggOps.BOOLEAN_ALL, "all({0})")
        add(Ops.AggOps.BOOLEAN_ANY, "any({0})")
    }

}