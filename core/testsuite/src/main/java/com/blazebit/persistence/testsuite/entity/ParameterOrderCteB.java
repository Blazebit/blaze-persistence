/*
 * Copyright 2014 - 2021 Blazebit.
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
import javax.persistence.IdClass;
import java.io.Serializable;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
@Entity
@CTE
@IdClass(ParameterOrderCteB.ParameterOrderCteBId.class)
public class ParameterOrderCteB {

    public static class ParameterOrderCteBId implements Serializable {
        private Short seven;

        private Integer eight;

        private Long nine;

        public Short getSeven() {
            return seven;
        }

        public void setSeven(Short seven) {
            this.seven = seven;
        }

        public Integer getEight() {
            return eight;
        }

        public void setEight(Integer eight) {
            this.eight = eight;
        }

        public Long getNine() {
            return nine;
        }

        public void setNine(Long nine) {
            this.nine = nine;
        }
    }

    private Short seven;

    private Integer eight;

    private Long nine;

    @Id
    public Short getSeven() {
        return seven;
    }

    public void setSeven(Short seven) {
        this.seven = seven;
    }

    @Id
    public Integer getEight() {
        return eight;
    }

    public void setEight(Integer eight) {
        this.eight = eight;
    }

    @Id
    public Long getNine() {
        return nine;
    }

    public void setNine(Long nine) {
        this.nine = nine;
    }
}
