package com.blazebit.persistence.criteria.impl.path;

import java.util.Map;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyBasePath<K, V> extends AbstractPath<Map<K, V>> implements Path<Map<K, V>> {

    private final MapAttribute<?, K, V> mapAttribute;
    private final MapAttributeJoin<?, K, V> mapJoin;

    public MapKeyBasePath(BlazeCriteriaBuilderImpl criteriaBuilder, Class<Map<K, V>> javaType, MapAttributeJoin<?, K, V> mapJoin, MapAttribute<?, K, V> attribute) {
        super(criteriaBuilder, javaType, null);
        this.mapJoin = mapJoin;
        this.mapAttribute = attribute;
    }

    @Override
    public void prepareAlias(RenderContext context) {
        mapJoin.prepareAlias(context);
    }

    @Override
    public MapAttribute<?, K, V> getAttribute() {
        return mapAttribute;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Bindable<Map<K, V>> getModel() {
        return (Bindable<Map<K, V>>) mapAttribute;
    }

    @Override
    public AbstractPath<?> getParentPath() {
        return mapJoin.getParentPath();
    }

    @Override
    public String getPathExpression() {
        return mapJoin.getPathExpression();
    }

    @Override
    protected boolean isDereferencable() {
        return false;
    }

    @Override
    protected Attribute findAttribute(String attributeName) {
        throw new IllegalArgumentException("Map [" + mapJoin.getPathExpression() + "] cannot be dereferenced");
    }

}
