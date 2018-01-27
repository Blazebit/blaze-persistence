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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "project_leader_type")
public abstract class ProjectLeader<P extends Project<? extends ProjectLeader<?>>> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private P currentProject;
    private Set<P> leadedProjects = new HashSet<P>();
    
    public ProjectLeader() {
    }

    public ProjectLeader(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true, targetEntity = Project.class)
    public P getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(P currentProject) {
        this.currentProject = currentProject;
    }

    @OneToMany(mappedBy = "leader", targetEntity = Project.class)
    public Set<P> getLeadedProjects() {
        return leadedProjects;
    }

    public void setLeadedProjects(Set<P> leadedProjects) {
        this.leadedProjects = leadedProjects;
    }

}
