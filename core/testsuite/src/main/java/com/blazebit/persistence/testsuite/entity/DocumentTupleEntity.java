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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class DocumentTupleEntity implements Serializable {
    private Document element1;
    private Document element2;

    public DocumentTupleEntity() { }

    public DocumentTupleEntity(Document element1, Document element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Document getElement1() {
        return element1;
    }

    public void setElement1(Document element1) {
        this.element1 = element1;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Document getElement2() {
        return element2;
    }

    public void setElement2(Document element2) {
        this.element2 = element2;
    }
}
