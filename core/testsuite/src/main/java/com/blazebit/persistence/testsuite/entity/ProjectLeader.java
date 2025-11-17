/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
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
