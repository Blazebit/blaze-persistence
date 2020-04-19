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

package com.blazebit.persistence.spring.data.webflux.impl;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class PageRequest extends org.springframework.data.domain.PageRequest {

    private static final org.springframework.data.domain.Sort UNSORTED;

    static {
        org.springframework.data.domain.Sort unsorted = null;
        try {
            for (Method unsortedCandidate : org.springframework.data.domain.Sort.class.getDeclaredMethods()) {
                if ("unsorted".equals(unsortedCandidate.getName())) {
                    unsorted = (org.springframework.data.domain.Sort) unsortedCandidate.invoke(null);
                    break;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        UNSORTED = unsorted;
    }

    public PageRequest(int page, int size) {
        super(page, size, UNSORTED);
    }

    public PageRequest(int page, int size, String... properties) {
        super(page, size, Sort.asc(properties));
    }

    public PageRequest(int page, int size, org.springframework.data.domain.Sort.Direction direction, String... properties) {
        super(page, size, Sort.of(direction, properties));
    }
}
