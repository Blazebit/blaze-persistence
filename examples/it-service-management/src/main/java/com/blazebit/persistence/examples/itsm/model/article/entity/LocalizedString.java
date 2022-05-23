/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.article.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

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
