/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Entity
@Table(name = "json_document")
public class JsonDocument {
    private Long id;
    private String content;

    public JsonDocument() {
    }

    public JsonDocument(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
