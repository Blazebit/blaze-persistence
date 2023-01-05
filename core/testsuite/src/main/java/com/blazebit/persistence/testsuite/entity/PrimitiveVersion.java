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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Entity
@Table(name = "prim_version")
public class PrimitiveVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private long versionId;
    private PrimitiveDocument document;

    public PrimitiveVersion() {
    }

    @Id
    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long id) {
        this.versionId = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public PrimitiveDocument getDocument() {
        return document;
    }

    public void setDocument(PrimitiveDocument document) {
        this.document = document;
    }

}
