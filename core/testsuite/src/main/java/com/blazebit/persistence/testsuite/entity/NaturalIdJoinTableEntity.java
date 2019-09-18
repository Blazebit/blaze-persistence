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

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
@Entity
@Table(name = "nid_join_table")
public class NaturalIdJoinTableEntity extends Ownable implements Serializable {

    private String isbn;

    private BookEntity oneToOneBook;

    private List<BookEntity> oneToManyBook;

    private List<BookEntity> manyToManyBook;

    @OneToMany
    @JoinTable(
            name = "nid_jt_join_table",
            joinColumns = @JoinColumn(name = "base_isbn", referencedColumnName = "isbn"),
            inverseJoinColumns = @JoinColumn(name = "ref_isbn", referencedColumnName = "isbn")
    )
    public List<BookEntity> getOneToManyBook() {
        return oneToManyBook;
    }

    public void setOneToManyBook(List<BookEntity> oneToManyBook) {
        this.oneToManyBook = oneToManyBook;
    }

    @ManyToMany
    @JoinTable(
            name = "nid_jt_join_table",
            joinColumns = @JoinColumn(name = "base_isbn", referencedColumnName = "isbn"),
            inverseJoinColumns = @JoinColumn(name = "ref_isbn", referencedColumnName = "isbn")
    )
    public List<BookEntity> getManyToManyBook() {
        return manyToManyBook;
    }

    public void setManyToManyBook(List<BookEntity> manyToManyBook) {
        this.manyToManyBook = manyToManyBook;
    }

    @Column(unique = true, name = "isbn", length = 50, nullable = false)
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}
