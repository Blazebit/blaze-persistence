/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@MappedSuperclass
public abstract class LocalizedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String name;

    @Embedded
    private LocalizedString title = new LocalizedString();

    @Embedded
    private LocalizedString description = new LocalizedString();

    protected LocalizedEntity() {
    }

    /**
     * Instantiates a new queue.
     *
     * @param name
     *            the name
     */
    protected LocalizedEntity(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public LocalizedString getTitle() {
        if (this.title == null) {
            this.title = new LocalizedString();
        }
        return this.title;
    }

    /**
     * Sets the title.
     *
     * @param title
     *            the new title
     */
    public void setTitle(LocalizedString title) {
        this.title = title;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public LocalizedString getDescription() {
        if (this.description == null) {
            this.description = new LocalizedString();
        }
        return this.description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(LocalizedString description) {
        this.description = description;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocalizedEntity)) {
            return false;
        }
        LocalizedEntity other = (LocalizedEntity) obj;
        return Objects.equals(this.name, other.name);
    }

}
