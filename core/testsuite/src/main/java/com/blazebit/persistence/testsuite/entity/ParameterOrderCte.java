/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.CTE;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
@Entity
@CTE
public class ParameterOrderCte {

    private Short four;

    private Integer five;

    private Long six;

    @Id
    public Short getFour() {
        return four;
    }

    public void setFour(Short four) {
        this.four = four;
    }

    public Integer getFive() {
        return five;
    }

    public void setFive(Integer five) {
        this.five = five;
    }

    public Long getSix() {
        return six;
    }

    public void setSix(Long six) {
        this.six = six;
    }
}
