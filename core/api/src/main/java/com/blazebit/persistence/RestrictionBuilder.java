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
package com.blazebit.persistence;

import java.util.List;

/**
 *
 * @author cpbec
 */
public interface RestrictionBuilder<T> {
    
    // TODO: [expression] [operator] [ALL | ANY | SOME] [subquery]
    
    // Functions TODO: SIZE, UPPER, LOWER, TRIM, CONCAT
    //                 COUNT, AVG, MIN, MAX, SUM
    
    // Operators TODO: IN [subquery], 
    //                 EXISTS [subquery] => Filterable
    
    public T between(Object start, Object end);
    
    public T notBetween(Object start, Object end);
    
    public QuantifiableBinaryPredicateBuilder<T> eq();
    
    public T eq(Object value);
    
    public T eqExpression(String expression);
    
    public QuantifiableBinaryPredicateBuilder<T> notEq();
    
    public T notEq(Object value);
    
    public T notEqExpression(String expression);
    
    public QuantifiableBinaryPredicateBuilder<T> gt();
    
    public T gt(Object value);
    
    public T gtExpression(String expression);
    
    public QuantifiableBinaryPredicateBuilder<T> ge();
    
    public T ge(Object value);
    
    public T geExpression(String expression);
    
    public QuantifiableBinaryPredicateBuilder<T> lt();
    
    public T lt(Object value);
    
    public T ltExpression(String expression);
    
    public QuantifiableBinaryPredicateBuilder<T> le();
    
    public T le(Object value);
    
    public T leExpression(String expression);
    
    //public T in(CriteriaBuilder builder);
    
    //public T notIn(CriteriaBuilder builder);
    
    public T in(List<?> values);
    
    public T notIn(List<?> values);
    
//    public T inElements(String expression);
//    
//    public T inIndices(String expression);
    
    public T isNull();
    
    public T isNotNull();
    
    public T isEmpty();
    
    public T isNotEmpty();
    
    public T isMemberOf(String expression);
    
    public T isNotMemberOf(String expression);
    
    public T like(String value);
    
    public T like(String value, boolean caseSensitive);
    
    public T like(String value, boolean caseSensitive, Character escapeCharacter);
    
    public T likeExpression(String expression);
    
    public T likeExpression(String value, boolean caseSensitive);
    
    public T likeExpression(String expression, boolean caseSensitive, Character escapeCharacter);
    
    public T notLike(String value);
    
    public T notLike(String value, boolean caseSensitive, Character escapeCharacter);
    
    public T notLikeExpression(String expression);
    
    public T notLikeExpression(String expression, boolean caseSensitive, Character escapeCharacter);
}
