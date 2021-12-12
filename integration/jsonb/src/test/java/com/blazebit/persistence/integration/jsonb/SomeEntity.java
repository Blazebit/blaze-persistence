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

package com.blazebit.persistence.integration.jsonb;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Entity
public class SomeEntity {
    @Id
    Long id;
    String name;
    @ManyToOne(fetch = FetchType.LAZY)
    SomeEntity parent;
    @Basic
    @Convert(converter = StringListConverter.class)
    List<String> tags;
    @OneToMany(mappedBy = "parent")
    Set<SomeEntity> children;
}
