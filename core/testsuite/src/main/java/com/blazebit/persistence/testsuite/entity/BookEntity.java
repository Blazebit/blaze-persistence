/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
@Entity
@Table(name = "book")
@Access(AccessType.FIELD)
public class BookEntity extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(unique = true, name = "isbn", length = 50, nullable = false)
    private String isbn;

    public BookEntity() {
    }

    public BookEntity(Long id) {
        super(id);
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
