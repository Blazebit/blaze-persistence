/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@Table(name = "sing_list_tbl")
public class SingularListEntity extends LongSequenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<String> list = new ArrayList<>();

    public SingularListEntity() {
    }

    public SingularListEntity(Long id) {
        super(id);
    }

    public SingularListEntity(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "list_val")
    @Convert(converter = ListConverter.class)
    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public static class ListConverter implements AttributeConverter<List<String>, String> {
        @Override
        public String convertToDatabaseColumn(List<String> strings) {
            if (strings == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (String string : strings) {
                sb.append(string);
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }

        @Override
        public List<String> convertToEntityAttribute(String s) {
            if (s == null) {
                return null;
            }
            return new ArrayList<>(Arrays.asList(s.split(",")));
        }
    }
}
