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
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "Root2")
public class Root2 {

    @Id
    private Integer id;

    @OneToMany(mappedBy = "parent")
    @OrderColumn(name = "list_index")
    private List<IndexedNode2> indexedNodesMappedBy;
    @OneToMany(mappedBy = "parent")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode2> keyedNodesMappedBy;

}
