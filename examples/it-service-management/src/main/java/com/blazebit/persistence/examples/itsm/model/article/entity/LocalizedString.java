/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Embeddable
public class LocalizedString implements Serializable {

    private String type = "text/plain";

    @ElementCollection
    private Map<Locale, String> localizedValues = new HashMap<>();

    /**
     * Instantiates a new localized string.
     */
    public LocalizedString() {
    }

    /**
     * Instantiates a new localized string.
     *
     * @param type
     *            the type
     */
    public LocalizedString(String type) {
        this.setType(type);
    }

    /**
     * Instantiates a new localized string.
     *
     * @param value
     *            the value
     * @param locale
     *            the locale
     */
    public LocalizedString(String value, Locale locale) {
        this.localizedValues.put(locale, value);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the localized values.
     *
     * @return the localized values
     */
    public Map<Locale, String> getLocalizedValues() {
        return this.localizedValues;
    }

}
