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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BlazeCriteria {

    private BlazeCriteria() {
    }

    public static BlazeCriteriaBuilder get(EntityManager em, CriteriaBuilderFactory cbf) {
        return new BlazeCriteriaBuilderImpl(em, cbf);
    }

    public static <T> BlazeCriteriaQuery<T> get(EntityManager em, CriteriaBuilderFactory cbf, Class<T> clazz) {
        return new BlazeCriteriaBuilderImpl(em, cbf).createQuery(clazz);
    }
}
