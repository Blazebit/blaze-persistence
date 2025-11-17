/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
@Entity
@Table(name = "nid_join_table")
@Access(AccessType.FIELD)
public class NaturalIdJoinTableEntity extends Ownable implements Serializable {

    private Long version;
    @Column(unique = true, name = "isbn", length = 50, nullable = false)
    private String isbn;
    @OneToMany
    @JoinTable(
        name = "nid_jt_join_table1",
        joinColumns = @JoinColumn(name = "base_isbn", referencedColumnName = "isbn"),
        inverseJoinColumns = @JoinColumn(name = "ref_isbn", referencedColumnName = "isbn")
    )
    private Set<BookEntity> oneToManyBook = new HashSet<>();
    @ManyToMany
    @JoinTable(
        name = "nid_jt_join_table2",
        joinColumns = @JoinColumn(name = "base_isbn", referencedColumnName = "isbn"),
        inverseJoinColumns = @JoinColumn(name = "ref_isbn", referencedColumnName = "isbn")
    )
    @MapKeyColumn(name = "nid_map_key", nullable = false, length = 20)
    private Map<String, BookEntity> manyToManyBook = new HashMap<>();

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Set<BookEntity> getOneToManyBook() {
        return oneToManyBook;
    }

    public void setOneToManyBook(Set<BookEntity> oneToManyBook) {
        this.oneToManyBook = oneToManyBook;
    }

    public Map<String, BookEntity> getManyToManyBook() {
        return manyToManyBook;
    }

    public void setManyToManyBook(Map<String, BookEntity> manyToManyBook) {
        this.manyToManyBook = manyToManyBook;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}
