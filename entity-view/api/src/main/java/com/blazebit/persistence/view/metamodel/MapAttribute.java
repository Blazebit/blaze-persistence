/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.MapInstantiator;
import com.blazebit.persistence.view.RecordingContainer;

import java.util.Map;

/**
 * Instances of the type {@linkplain MapAttribute} represent persistent {@linkplain java.util.Map}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <K> The type of the key of the represented Map
 * @param <V> The type of the value of the represented Map
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MapAttribute<X, K, V> extends PluralAttribute<X, Map<K, V>, V> {

    /**
     * Returns the java type of the key.
     *
     * @return The java type of the key
     */
    public Type<K> getKeyType();

    /**
     * Returns the inheritance subtypes that should be considered for the keys of this map attribute.
     * When the key type of the map attribute is not a subview, this returns an empty set.
     *
     * @return The inheritance subtypes or an empty set
     * @since 1.2.0
     */
    public Map<ManagedViewType<? extends K>, String> getKeyInheritanceSubtypeMappings();

    /**
     * Returns true if the key of this map attribute is a subview, otherwise false.
     *
     * @return True if the key of this map attribute is a subview, otherwise false
     * @since 1.2.0
     */
    public boolean isKeySubview();

    /**
     * Returns the map instantiator for this attribute.
     *
     * @param <R> The recording map type
     * @return The map instantiator
     * @since 1.5.0
     */
    public <R extends Map<K, V> & RecordingContainer<? extends Map<K, V>>> MapInstantiator<Map<K, V>, R> getMapInstantiator();

    /**
     * Returns the key mapping of the attribute.
     *
     * @return The key mapping of the attribute
     * @since 1.5.0
     */
    public String getKeyMapping();

    /**
     * The associations that should be fetched along with the entity mapped by the key of this attribute.
     *
     * @return The association that should be fetched
     * @since 1.5.0
     */
    public String[] getKeyFetches();

    /**
     * Renders the key mapping for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderKeyMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb);
}
