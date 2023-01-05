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

package com.blazebit.persistence.view.testsuite.subview.treat.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
@Table(name = "container")
public class Container {
    @ManyToOne
    private BaseContainerItem item;

    @Id
    private Long id;

    public Container() {
    }

    public Container(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public BaseContainerItem getItem() {
        return item;
    }

    public void setItem(BaseContainerItem item) {
        this.item = item;
    }
}
