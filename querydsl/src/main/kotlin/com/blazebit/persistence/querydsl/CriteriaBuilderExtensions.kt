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

import com.blazebit.persistence.CriteriaBuilder
import com.blazebit.persistence.CriteriaBuilderFactory
import javax.persistence.EntityManager
import kotlin.reflect.KClass

fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, klass: KClass<T>) : CriteriaBuilder<T> {
    return this.create(entityManager, klass.java)
}


fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, klass: KClass<T>, alias: String) : CriteriaBuilder<T> {
    return this.create(entityManager, klass.java, alias)
}