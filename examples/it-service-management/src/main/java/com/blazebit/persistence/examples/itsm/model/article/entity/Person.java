/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@Audited
public class Person implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String emailAddress;

    @OneToMany(mappedBy = Article_.AUTHOR)
    private Set<Article> articles = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Set<Article> getArticles() {
        return this.articles;
    }
}
