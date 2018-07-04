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

    private BookEntity book;
    private BookEntity bookNormal;

    public BookISBNReferenceEntity() {
    }

    public BookISBNReferenceEntity(Long id) {
        super(id);
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
