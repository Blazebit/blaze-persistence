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

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Embeddable
public class IndexedEmbeddable {

    @Column(length = 10)
    private String value;
    @Column(length = 10)
    private String value2;

    public IndexedEmbeddable() {
    }

    public IndexedEmbeddable(String value, String value2) {
        this.value = value;
        this.value2 = value2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
