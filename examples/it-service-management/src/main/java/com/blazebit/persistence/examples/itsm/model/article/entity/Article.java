/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.entity;

import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.envers.Audited;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@Audited
public class Article implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String slug;

    @Embedded
    private LocalizedString title = new LocalizedString();

    @Embedded
    private LocalizedString content = new LocalizedString();

    @ManyToOne
    private Person author;

    public Long getId() {
        return this.id;
    }

    public LocalizedString getTitle() {
        return this.title;
    }

    public LocalizedString getContent() {
        return this.content;
    }

    public Person getAuthor() {
        return this.author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }
}
