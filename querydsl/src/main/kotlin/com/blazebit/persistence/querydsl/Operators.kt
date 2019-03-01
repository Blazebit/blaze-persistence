package com.blazebit.persistence.querydsl

import com.mysema.query.types.OperatorImpl

class Operators {
    companion object {
        private val NS = Operators::class.java.name

        val GREATEST = OperatorImpl<Number>(NS, "GREATEST")
        val LEAST = OperatorImpl<Number>(NS, "LEAST")

        val TREAT_BOOLEAN = OperatorImpl<Number>(NS, "TREAT_BOOLEAN")
        val TREAT_BYTE = OperatorImpl<Number>(NS, "TREAT_BYTE")
        val TREAT_SHORT = OperatorImpl<Number>(NS, "TREAT_SHORT")
        val TREAT_INTEGER = OperatorImpl<Number>(NS, "TREAT_INTEGER")
        val TREAT_LONG = OperatorImpl<Number>(NS, "TREAT_LONG")
        val TREAT_FLOAT = OperatorImpl<Number>(NS, "TREAT_FLOAT")
        val TREAT_DOUBLE = OperatorImpl<Number>(NS, "TREAT_DOUBLE")
        val TREAT_CHARACTER = OperatorImpl<Number>(NS, "TREAT_CHARACTER")
        val TREAT_STRING = OperatorImpl<Number>(NS, "TREAT_STRING")
        val TREAT_BIGINTEGER = OperatorImpl<Number>(NS, "TREAT_BIGINTEGER")
        val TREAT_BIGDECIMAL = OperatorImpl<Number>(NS, "TREAT_BIGDECIMAL")
        val TREAT_TIME = OperatorImpl<Number>(NS, "TREAT_TIME")
        val TREAT_DATE = OperatorImpl<Number>(NS, "TREAT_DATE")
        val TREAT_TIMESTAMP = OperatorImpl<Number>(NS, "TREAT_TIMESTAMP")
        val TREAT_CALENDAR = OperatorImpl<Number>(NS, "TREAT_CALENDAR")

        val LIMIT = OperatorImpl<Number>(NS, "LIMIT")
        val PAGE_POSITION = OperatorImpl<Number>(NS, "PAGE_POSITION")
        val GROUP_CONCAT = OperatorImpl<Number>(NS, "GROUP_CONCAT")
        val COUNT_TUPLE = OperatorImpl<Number>(NS, "COUNT_TUPLE")

        val SET_UNION = OperatorImpl<Number>(NS, "SET_UNION")
        val SET_UNION_ALL = OperatorImpl<Number>(NS, "SET_UNION_ALL")
        val SET_INTERSECT = OperatorImpl<Number>(NS, "SET_INTERSECT")
        val SET_INTERSECT_ALL = OperatorImpl<Number>(NS, "SET_INTERSECT_ALL")
        val SET_EXCEPT = OperatorImpl<Number>(NS, "SET_EXCEPT")
        val SET_EXCEPT_ALL = OperatorImpl<Number>(NS, "SET_EXCEPT_ALL")
        val COMPARE_ROW_VALUE = OperatorImpl<Number>(NS, "COMPARE_ROW_VALUE")
        val SUBQUERY = OperatorImpl<Number>(NS, "SUBQUERY")
        val ENTITY_FUNCTION = OperatorImpl<Number>(NS, "ENTITY_FUNCTION")
    }
}