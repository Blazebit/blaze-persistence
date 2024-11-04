/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
@Entity
@Table(name = "book_isbn_ref")
public class BookISBNReferenceEntity extends LongSequenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long version;
    private BookEntity book;
    private BookEntity bookNormal;

    public BookISBNReferenceEntity() {
    }

    public BookISBNReferenceEntity(Long id) {
        super(id);
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @OneToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public BookEntity getBookNormal() {
        return bookNormal;
    }

    public void setBookNormal(BookEntity bookPK) {
        this.bookNormal = bookPK;
    }
}
