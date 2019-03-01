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

package com.blazebit.persistence.querydsl

import com.mysema.query.types.ConstantImpl
import com.mysema.query.types.Expression
import com.mysema.query.types.Ops
import com.mysema.query.types.Predicate
import com.mysema.query.types.expr.*


/**
 * Get a negation of this boolean expression
 *
 * @return !this
 */
operator fun BooleanExpression.not() : BooleanExpression {
    return this.not()
}

/**
 * Get the negation of the expression
 *
 * @return !this
 */
operator fun Predicate.not() : Predicate {
    return this.not()
}

/**
 * Get the negation of this expression
 *
 * @return this * -1
 */
operator fun <T> NumberExpression<T>.unaryMinus() : NumberExpression<T> where T: Comparable<*>, T : Number {
    return this.negate()
}

/**
 * Get the sum of this and right
 *
 * @return this + right
 */
operator fun <T, V> NumberExpression<T>.plus(x : NumberExpression<V>) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.add(x)
}

/**
 * Get the difference of this and right
 *
 * @return this - right
 */
operator fun <T, V> NumberExpression<T>.minus(x : NumberExpression<V>) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.subtract(x)
}

/**
 * Get the result of the operation this / right
 *
 * @return this / right
 */
operator fun <T, V> NumberExpression<T>.div(x : NumberExpression<V>) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.divide(x)
}

/**
 * Get the result of the operation this * right
 *
 * @return this * right
 */
operator fun <T, V> NumberExpression<T>.times(x : NumberExpression<V>) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.multiply(x)
}

operator fun <T> NumberExpression<T>.rem(x : NumberExpression<T>) : NumberExpression<T> where T: Comparable<*>, T : Number {
    return this.mod(x)
}

/**
 * Get the sum of this and right
 *
 * @return this + right
 */
operator fun <T, V> NumberExpression<T>.plus(x : V) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<V>, V : Number {
    return this.add(x)
}

/**
 * Get the difference of this and right
 *
 * @return this - right
 */
operator fun <T, V> NumberExpression<T>.minus(x : V) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.subtract(x)
}

/**
 * Get the result of the operation this / right
 *
 * @return this / right
 */
operator fun <T, V> NumberExpression<T>.div(x : V) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<*>, V : Number {
    return this.divide(x)
}

/**
 * Get the result of the operation this * right
 *
 * @return this * right
 */
operator fun <T, V> NumberExpression<T>.times(x :V) : NumberExpression<T> where T: Comparable<*>, T : Number, V: Comparable<V>, V : Number {
    return this.multiply(x)
}

operator fun <T> NumberExpression<T>.rem(x : T) : NumberExpression<T> where T: Comparable<*>, T : Number {
    return this.mod(x)
}

fun <T> NumberExpression<T>.pow(x : T) : NumberExpression<T> where T: Comparable<*>, T : Number {
    return NumberOperation.create(type, Ops.MathOps.POWER, this, ConstantImpl.create(x))
}

/**
 * Get the concatenation of this and str
 *
 * @return this + str
 */
operator fun StringExpression.plus(x : StringExpression) : StringExpression {
    return this.append(x)
}

/**
 * Get the concatenation of this and str
 *
 * @return this + str
 */
operator fun StringExpression.plus(x : String) : StringExpression {
    return this.append(x)
}

/**
 * Indexed access
 *
 * @param x
 * @return this.get(x)
 * @see java.util.List#get(int)
 */
operator fun <T ,E : SimpleExpression<in T>?> ListExpression<T, E>.get(x : Int) : E {
    return this.get(x)
}

/**
 * Indexed access
 *
 * @param x
 * @return this.get(x)
 * @see java.util.List#get(int)
 */
operator fun <T ,E : SimpleExpression<in T>?> ListExpression<T, E>.get(x : Expression<Int>) : E {
    return this.get(x)
}

/**
 * Get the character at the given index
 *
 * @param x
 * @return this.charAt(x)
 * @see java.lang.String#charAt(int)
 */
operator fun StringExpression.get(x : Int) : SimpleExpression<Char> {
    return this.charAt(x)
}

/**
 * Get the character at the given index
 *
 * @param x
 * @return this.charAt(x)
 * @see java.lang.String#charAt(int)
 */
operator fun StringExpression.get(x : Expression<Int>) : SimpleExpression<Char> {
    return this.charAt(x)
}

fun StringExpression.lpad(length : Int, fill : String? = null) : StringExpression {
    return if (fill == null) {
        StringOperation.create(Ops.StringOps.LPAD, this, ConstantImpl.create(length))
    } else {
        StringOperation.create(Ops.StringOps.LPAD2, this, ConstantImpl.create(length), ConstantImpl.create(fill))
    }
}

fun StringExpression.rpad(length : Int, fill : String? = null) : StringExpression {
    return if (fill == null) {
        StringOperation.create(Ops.StringOps.RPAD, this, ConstantImpl.create(length))
    } else {
        StringOperation.create(Ops.StringOps.RPAD2, this, ConstantImpl.create(length), ConstantImpl.create(fill))
    }
}

fun StringExpression.ltrim() : StringExpression {
    return StringOperation.create(Ops.StringOps.LTRIM, this)
}

fun StringExpression.rtrim() : StringExpression {
    return StringOperation.create(Ops.StringOps.LTRIM, this)
}


fun StringExpression.left(length : Int) : StringExpression {
    return StringOperation.create(Ops.StringOps.LEFT, this, ConstantImpl.create(length))
}

fun StringExpression.right(length : Int) : StringExpression {
    return StringOperation.create(Ops.StringOps.RIGHT, this, ConstantImpl.create(length))
}

fun StringExpression.locate(string : String, start : Int? = 0) : NumberExpression<Int> {
    return if (start == null) {
        NumberOperation.create(Int::class.java, Ops.StringOps.LOCATE, this, ConstantImpl.create(string))
    } else {
        return NumberOperation.create(Int::class.java, Ops.StringOps.LOCATE2, this, ConstantImpl.create(string), ConstantImpl.create(start))
    }
}
/**
 * Get an intersection of this and the given expression
 *
 * @param predicate right hand side of the union
 * @return this and right
 */
infix fun BooleanExpression.and(predicate: Predicate) : BooleanExpression {
    return this.and(predicate)
}

/**
 * Get a union of this and the given expression
 *
 * @param predicate right hand side of the union
 * @return this || right
 */
infix fun BooleanExpression.or(predicate: Predicate) : BooleanExpression {
    return this.or(predicate)
}
