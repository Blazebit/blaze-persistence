/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.testsuite.removeunmapped.model;

import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

/**
 *
 * @author Harald Eibensteiner
 * @since 1.4.0
 */
@Entity
public class Template {

    @Id
    @Column(name = "template_id")
    Long id;
    @OneToMany
    @JoinColumn(name = "id1", referencedColumnName = "template_id")
    @Where(clause = "type = 1")
    Set<FileLink> fileLinks;

    public Template() {
    }

    public Template(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<FileLink> getFileLinks() {
        return fileLinks;
    }

    public void setFileLinks(Set<FileLink> fileLinks) {
        this.fileLinks = fileLinks;
    }
}
