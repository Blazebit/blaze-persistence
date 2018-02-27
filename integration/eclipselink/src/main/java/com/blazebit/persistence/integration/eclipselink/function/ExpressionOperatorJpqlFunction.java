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

package com.blazebit.persistence.integration.eclipselink.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.eclipse.persistence.expressions.ExpressionOperator;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ExpressionOperatorJpqlFunction implements JpqlFunction {

    private final ExpressionOperator function;

    public ExpressionOperatorJpqlFunction(ExpressionOperator function) {
        this.function = function;
    }

    @Override
    public boolean hasArguments() {
        // Not sure how to determine that
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        // Not sure how to determine that
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        if (firstArgumentType == null) {
            return null;
        }
        // Eclipselink apparently does not support resolving the type...
        // So we have to hack something up
        int selector = function.getSelector();

        // Aggregate
        if (selector == ExpressionOperator.Count) {
            return Long.class;
        } else if (selector == ExpressionOperator.Sum) {
            if (firstArgumentType == BigInteger.class || firstArgumentType == BigDecimal.class) {
                return firstArgumentType;
            } else if (firstArgumentType == Float.class || firstArgumentType == Double.class) {
                return Double.class;
            }
            return Long.class;
        } else if (selector == ExpressionOperator.Average) {
            return Double.class;
        } else if (selector == ExpressionOperator.Maximum || selector == ExpressionOperator.Minimum) {
            return firstArgumentType;
        } else if (selector == ExpressionOperator.StandardDeviation || selector == ExpressionOperator.Variance) {
            if (firstArgumentType == Float.class || firstArgumentType == Double.class) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        }


        if (selector == ExpressionOperator.Coalesce
                || selector == ExpressionOperator.NullIf
                || selector == ExpressionOperator.Decode
                || selector == ExpressionOperator.Case
                || selector == ExpressionOperator.CaseCondition) {
            return firstArgumentType;
        }

        // General
        if (selector == ExpressionOperator.ToUpperCase
                || selector == ExpressionOperator.ToLowerCase
                || selector == ExpressionOperator.Chr
                || selector == ExpressionOperator.Concat
                || selector == ExpressionOperator.Initcap
                || selector == ExpressionOperator.Soundex
                || selector == ExpressionOperator.LeftPad
                || selector == ExpressionOperator.LeftTrim
                || selector == ExpressionOperator.Replace
                || selector == ExpressionOperator.RightPad
                || selector == ExpressionOperator.RightTrim
                || selector == ExpressionOperator.Substring
                || selector == ExpressionOperator.Translate
                || selector == ExpressionOperator.Trim
                || selector == ExpressionOperator.Ascii
                || selector == ExpressionOperator.Reverse
                || selector == ExpressionOperator.Replicate
                || selector == ExpressionOperator.Right
                || selector == ExpressionOperator.ToChar
                || selector == ExpressionOperator.ToCharWithFormat
                || selector == ExpressionOperator.RightTrim2
                || selector == ExpressionOperator.Trim2
                || selector == ExpressionOperator.LeftTrim2
                || selector == ExpressionOperator.SubstringSingleArg) {
            return String.class;
        } else if (selector == ExpressionOperator.Instring
                || selector == ExpressionOperator.Length
                || selector == ExpressionOperator.CharIndex
                || selector == ExpressionOperator.CharLength
                || selector == ExpressionOperator.Locate
                || selector == ExpressionOperator.Locate2
                || selector == ExpressionOperator.Extract) {
            return Integer.class;
        } else if (selector == ExpressionOperator.ToNumber) {
            return BigDecimal.class;
//        } else if (selector == HexToRaw) {
//        } else if (selector == Difference) {
//        } else if (selector == Any) {
//        } else if (selector == Some) {
//        } else if (selector == All) {
//        } else if (selector == Cast) {
        }

        // Date
        if (selector == ExpressionOperator.AddMonths) {
            return firstArgumentType;
        } else if (selector == ExpressionOperator.DateToString
                || selector == ExpressionOperator.DateName) {
            return String.class;
        } else if (selector == ExpressionOperator.LastDay
                || selector == ExpressionOperator.NextDay
                || selector == ExpressionOperator.RoundDate
                || selector == ExpressionOperator.ToDate
                || selector == ExpressionOperator.Today
                || selector == ExpressionOperator.AddDate
                || selector == ExpressionOperator.DateDifference
                || selector == ExpressionOperator.TruncateDate
                || selector == ExpressionOperator.NewTime
                || selector == ExpressionOperator.CurrentDate) {
            return java.sql.Date.class;
        } else if (selector == ExpressionOperator.MonthsBetween
                || selector == ExpressionOperator.DatePart) {
            return Integer.class;
        } else if (selector == ExpressionOperator.Nvl) {
            return firstArgumentType;
        } else if (selector == ExpressionOperator.CurrentTime) {
            return java.sql.Time.class;
        }

        // Math
        if (selector == ExpressionOperator.Ceil
                || selector == ExpressionOperator.Floor
                || selector == ExpressionOperator.Exp
                || selector == ExpressionOperator.Abs
                || selector == ExpressionOperator.Mod
                || selector == ExpressionOperator.Power
                || selector == ExpressionOperator.Round
                || selector == ExpressionOperator.Trunc
                || selector == ExpressionOperator.Greatest
                || selector == ExpressionOperator.Least
                || selector == ExpressionOperator.Add
                || selector == ExpressionOperator.Subtract
                || selector == ExpressionOperator.Multiply
                || selector == ExpressionOperator.Negate
                || selector == ExpressionOperator.Divide) {
            return firstArgumentType;
        } else if (selector == ExpressionOperator.Cos
                || selector == ExpressionOperator.Cosh
                || selector == ExpressionOperator.Acos
                || selector == ExpressionOperator.Asin
                || selector == ExpressionOperator.Atan
                || selector == ExpressionOperator.Sqrt
                || selector == ExpressionOperator.Ln
                || selector == ExpressionOperator.Log
                || selector == ExpressionOperator.Sin
                || selector == ExpressionOperator.Sinh
                || selector == ExpressionOperator.Tan
                || selector == ExpressionOperator.Tanh
                || selector == ExpressionOperator.Atan2
                || selector == ExpressionOperator.Cot) {
            // Maybe double?
            return firstArgumentType;
        } else if (selector == ExpressionOperator.Sign) {
            return Integer.class;
        }

        // Predicates

        if (selector == ExpressionOperator.Equal
                || selector == ExpressionOperator.NotEqual
                || selector == ExpressionOperator.EqualOuterJoin
                || selector == ExpressionOperator.LessThan
                || selector == ExpressionOperator.LessThanEqual
                || selector == ExpressionOperator.GreaterThan
                || selector == ExpressionOperator.GreaterThanEqual
                || selector == ExpressionOperator.Like
                || selector == ExpressionOperator.NotLike
                || selector == ExpressionOperator.In
                || selector == ExpressionOperator.InSubQuery
                || selector == ExpressionOperator.NotIn
                || selector == ExpressionOperator.NotInSubQuery
                || selector == ExpressionOperator.Between
                || selector == ExpressionOperator.NotBetween
                || selector == ExpressionOperator.IsNull
                || selector == ExpressionOperator.NotNull
                || selector == ExpressionOperator.Exists
                || selector == ExpressionOperator.NotExists
                || selector == ExpressionOperator.LikeEscape
                || selector == ExpressionOperator.NotLikeEscape
                || selector == ExpressionOperator.Regexp) {
            return Boolean.class;
        }
        
        return null;
    }

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }
    
}
