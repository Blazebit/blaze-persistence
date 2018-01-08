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

package com.blazebit.persistence.impl.eclipselink.function;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.eclipse.persistence.expressions.ExpressionOperator;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.eclipse.persistence.expressions.ExpressionOperator.Abs;
import static org.eclipse.persistence.expressions.ExpressionOperator.Acos;
import static org.eclipse.persistence.expressions.ExpressionOperator.Add;
import static org.eclipse.persistence.expressions.ExpressionOperator.AddDate;
import static org.eclipse.persistence.expressions.ExpressionOperator.AddMonths;
import static org.eclipse.persistence.expressions.ExpressionOperator.Ascii;
import static org.eclipse.persistence.expressions.ExpressionOperator.Asin;
import static org.eclipse.persistence.expressions.ExpressionOperator.Atan;
import static org.eclipse.persistence.expressions.ExpressionOperator.Atan2;
import static org.eclipse.persistence.expressions.ExpressionOperator.Average;
import static org.eclipse.persistence.expressions.ExpressionOperator.Between;
import static org.eclipse.persistence.expressions.ExpressionOperator.Case;
import static org.eclipse.persistence.expressions.ExpressionOperator.CaseCondition;
import static org.eclipse.persistence.expressions.ExpressionOperator.Ceil;
import static org.eclipse.persistence.expressions.ExpressionOperator.CharIndex;
import static org.eclipse.persistence.expressions.ExpressionOperator.CharLength;
import static org.eclipse.persistence.expressions.ExpressionOperator.Chr;
import static org.eclipse.persistence.expressions.ExpressionOperator.Coalesce;
import static org.eclipse.persistence.expressions.ExpressionOperator.Concat;
import static org.eclipse.persistence.expressions.ExpressionOperator.Cos;
import static org.eclipse.persistence.expressions.ExpressionOperator.Cosh;
import static org.eclipse.persistence.expressions.ExpressionOperator.Cot;
import static org.eclipse.persistence.expressions.ExpressionOperator.Count;
import static org.eclipse.persistence.expressions.ExpressionOperator.CurrentDate;
import static org.eclipse.persistence.expressions.ExpressionOperator.CurrentTime;
import static org.eclipse.persistence.expressions.ExpressionOperator.DateDifference;
import static org.eclipse.persistence.expressions.ExpressionOperator.DateName;
import static org.eclipse.persistence.expressions.ExpressionOperator.DatePart;
import static org.eclipse.persistence.expressions.ExpressionOperator.DateToString;
import static org.eclipse.persistence.expressions.ExpressionOperator.Decode;
import static org.eclipse.persistence.expressions.ExpressionOperator.Divide;
import static org.eclipse.persistence.expressions.ExpressionOperator.Equal;
import static org.eclipse.persistence.expressions.ExpressionOperator.EqualOuterJoin;
import static org.eclipse.persistence.expressions.ExpressionOperator.Exists;
import static org.eclipse.persistence.expressions.ExpressionOperator.Exp;
import static org.eclipse.persistence.expressions.ExpressionOperator.Extract;
import static org.eclipse.persistence.expressions.ExpressionOperator.Floor;
import static org.eclipse.persistence.expressions.ExpressionOperator.GreaterThan;
import static org.eclipse.persistence.expressions.ExpressionOperator.GreaterThanEqual;
import static org.eclipse.persistence.expressions.ExpressionOperator.Greatest;
import static org.eclipse.persistence.expressions.ExpressionOperator.In;
import static org.eclipse.persistence.expressions.ExpressionOperator.InSubQuery;
import static org.eclipse.persistence.expressions.ExpressionOperator.Initcap;
import static org.eclipse.persistence.expressions.ExpressionOperator.Instring;
import static org.eclipse.persistence.expressions.ExpressionOperator.IsNull;
import static org.eclipse.persistence.expressions.ExpressionOperator.LastDay;
import static org.eclipse.persistence.expressions.ExpressionOperator.Least;
import static org.eclipse.persistence.expressions.ExpressionOperator.LeftPad;
import static org.eclipse.persistence.expressions.ExpressionOperator.LeftTrim;
import static org.eclipse.persistence.expressions.ExpressionOperator.LeftTrim2;
import static org.eclipse.persistence.expressions.ExpressionOperator.Length;
import static org.eclipse.persistence.expressions.ExpressionOperator.LessThan;
import static org.eclipse.persistence.expressions.ExpressionOperator.LessThanEqual;
import static org.eclipse.persistence.expressions.ExpressionOperator.Like;
import static org.eclipse.persistence.expressions.ExpressionOperator.LikeEscape;
import static org.eclipse.persistence.expressions.ExpressionOperator.Ln;
import static org.eclipse.persistence.expressions.ExpressionOperator.Locate;
import static org.eclipse.persistence.expressions.ExpressionOperator.Locate2;
import static org.eclipse.persistence.expressions.ExpressionOperator.Log;
import static org.eclipse.persistence.expressions.ExpressionOperator.Maximum;
import static org.eclipse.persistence.expressions.ExpressionOperator.Minimum;
import static org.eclipse.persistence.expressions.ExpressionOperator.Mod;
import static org.eclipse.persistence.expressions.ExpressionOperator.MonthsBetween;
import static org.eclipse.persistence.expressions.ExpressionOperator.Multiply;
import static org.eclipse.persistence.expressions.ExpressionOperator.Negate;
import static org.eclipse.persistence.expressions.ExpressionOperator.NewTime;
import static org.eclipse.persistence.expressions.ExpressionOperator.NextDay;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotBetween;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotEqual;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotExists;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotIn;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotInSubQuery;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotLike;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotLikeEscape;
import static org.eclipse.persistence.expressions.ExpressionOperator.NotNull;
import static org.eclipse.persistence.expressions.ExpressionOperator.NullIf;
import static org.eclipse.persistence.expressions.ExpressionOperator.Nvl;
import static org.eclipse.persistence.expressions.ExpressionOperator.Power;
import static org.eclipse.persistence.expressions.ExpressionOperator.Regexp;
import static org.eclipse.persistence.expressions.ExpressionOperator.Replace;
import static org.eclipse.persistence.expressions.ExpressionOperator.Replicate;
import static org.eclipse.persistence.expressions.ExpressionOperator.Reverse;
import static org.eclipse.persistence.expressions.ExpressionOperator.Right;
import static org.eclipse.persistence.expressions.ExpressionOperator.RightPad;
import static org.eclipse.persistence.expressions.ExpressionOperator.RightTrim;
import static org.eclipse.persistence.expressions.ExpressionOperator.RightTrim2;
import static org.eclipse.persistence.expressions.ExpressionOperator.Round;
import static org.eclipse.persistence.expressions.ExpressionOperator.RoundDate;
import static org.eclipse.persistence.expressions.ExpressionOperator.Sign;
import static org.eclipse.persistence.expressions.ExpressionOperator.Sin;
import static org.eclipse.persistence.expressions.ExpressionOperator.Sinh;
import static org.eclipse.persistence.expressions.ExpressionOperator.Soundex;
import static org.eclipse.persistence.expressions.ExpressionOperator.Sqrt;
import static org.eclipse.persistence.expressions.ExpressionOperator.StandardDeviation;
import static org.eclipse.persistence.expressions.ExpressionOperator.Substring;
import static org.eclipse.persistence.expressions.ExpressionOperator.SubstringSingleArg;
import static org.eclipse.persistence.expressions.ExpressionOperator.Subtract;
import static org.eclipse.persistence.expressions.ExpressionOperator.Sum;
import static org.eclipse.persistence.expressions.ExpressionOperator.Tan;
import static org.eclipse.persistence.expressions.ExpressionOperator.Tanh;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToChar;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToCharWithFormat;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToDate;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToLowerCase;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToNumber;
import static org.eclipse.persistence.expressions.ExpressionOperator.ToUpperCase;
import static org.eclipse.persistence.expressions.ExpressionOperator.Today;
import static org.eclipse.persistence.expressions.ExpressionOperator.Translate;
import static org.eclipse.persistence.expressions.ExpressionOperator.Trim;
import static org.eclipse.persistence.expressions.ExpressionOperator.Trim2;
import static org.eclipse.persistence.expressions.ExpressionOperator.Trunc;
import static org.eclipse.persistence.expressions.ExpressionOperator.TruncateDate;
import static org.eclipse.persistence.expressions.ExpressionOperator.Variance;

/**
 *
 * @author Christian Beikov
 * @since 1.0
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
        if (selector == Count) {
            return Long.class;
        } else if (selector == Sum) {
            if (firstArgumentType == BigInteger.class || firstArgumentType == BigDecimal.class) {
                return firstArgumentType;
            } else if (firstArgumentType == Float.class || firstArgumentType == Double.class) {
                return Double.class;
            }
            return Long.class;
        } else if (selector == Average) {
            return Double.class;
        } else if (selector == Maximum || selector == Minimum) {
            return firstArgumentType;
        } else if (selector == StandardDeviation || selector == Variance) {
            if (firstArgumentType == Float.class || firstArgumentType == Double.class) {
                return Double.class;
            } else {
                return BigDecimal.class;
            }
        }


        if (selector == Coalesce
                || selector == NullIf
                || selector == Decode
                || selector == Case
                || selector == CaseCondition) {
            return firstArgumentType;
        }

        // General
        if (selector == ToUpperCase
                || selector == ToLowerCase
                || selector == Chr
                || selector == Concat
                || selector == Initcap
                || selector == Soundex
                || selector == LeftPad
                || selector == LeftTrim
                || selector == Replace
                || selector == RightPad
                || selector == RightTrim
                || selector == Substring
                || selector == Translate
                || selector == Trim
                || selector == Ascii
                || selector == Reverse
                || selector == Replicate
                || selector == Right
                || selector == ToChar
                || selector == ToCharWithFormat
                || selector == RightTrim2
                || selector == Trim2
                || selector == LeftTrim2
                || selector == SubstringSingleArg) {
            return String.class;
        } else if (selector == Instring
                || selector == Length
                || selector == CharIndex
                || selector == CharLength
                || selector == Locate
                || selector == Locate2
                || selector == Extract) {
            return Integer.class;
        } else if (selector == ToNumber) {
            return BigDecimal.class;
//        } else if (selector == HexToRaw) {
//        } else if (selector == Difference) {
//        } else if (selector == Any) {
//        } else if (selector == Some) {
//        } else if (selector == All) {
//        } else if (selector == Cast) {
        }

        // Date
        if (selector == AddMonths) {
            return firstArgumentType;
        } else if (selector == DateToString
                || selector == DateName) {
            return String.class;
        } else if (selector == LastDay
                || selector == NextDay
                || selector == RoundDate
                || selector == ToDate
                || selector == Today
                || selector == AddDate
                || selector == DateDifference
                || selector == TruncateDate
                || selector == NewTime
                || selector == CurrentDate) {
            return java.sql.Date.class;
        } else if (selector == MonthsBetween
                || selector == DatePart) {
            return Integer.class;
        } else if (selector == Nvl) {
            return firstArgumentType;
        } else if (selector == CurrentTime) {
            return java.sql.Time.class;
        }

        // Math
        if (selector == Ceil
                || selector == Floor
                || selector == Exp
                || selector == Abs
                || selector == Mod
                || selector == Power
                || selector == Round
                || selector == Trunc
                || selector == Greatest
                || selector == Least
                || selector == Add
                || selector == Subtract
                || selector == Multiply
                || selector == Negate
                || selector == Divide) {
            return firstArgumentType;
        } else if (selector == Cos
                || selector == Cosh
                || selector == Acos
                || selector == Asin
                || selector == Atan
                || selector == Sqrt
                || selector == Ln
                || selector == Log
                || selector == Sin
                || selector == Sinh
                || selector == Tan
                || selector == Tanh
                || selector == Atan2
                || selector == Cot) {
            // Maybe double?
            return firstArgumentType;
        } else if (selector == Sign) {
            return Integer.class;
        }

        // Predicates

        if (selector == Equal
                || selector == NotEqual
                || selector == EqualOuterJoin
                || selector == LessThan
                || selector == LessThanEqual
                || selector == GreaterThan
                || selector == GreaterThanEqual
                || selector == Like
                || selector == NotLike
                || selector == In
                || selector == InSubQuery
                || selector == NotIn
                || selector == NotInSubQuery
                || selector == Between
                || selector == NotBetween
                || selector == IsNull
                || selector == NotNull
                || selector == Exists
                || selector == NotExists
                || selector == LikeEscape
                || selector == NotLikeEscape
                || selector == Regexp) {
            return Boolean.class;
        }
        
        return null;
    }

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }
    
}
