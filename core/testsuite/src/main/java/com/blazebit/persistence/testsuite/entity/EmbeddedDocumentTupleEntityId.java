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
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Embeddable
public class EmbeddedDocumentTupleEntityId implements Serializable {

    private Long element1;
    private Long element2;

    public EmbeddedDocumentTupleEntityId() { }

    public EmbeddedDocumentTupleEntityId(Long element1, Long element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    @Column(nullable = false)
    public Long getElement1() {
        return element1;
    }

    public void setElement1(Long element1) {
        this.element1 = element1;
    }

    @Column(nullable = false)
    public Long getElement2() {
        return element2;
    }

    public void setElement2(Long element2) {
        this.element2 = element2;
    }
}
