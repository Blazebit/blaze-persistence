package com.blazebit.persistence.criteria.impl.path;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.Map;

import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyAttribute<K> implements SingularAttribute<Map<K, ?>, K>, Bindable<K>, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final MapAttribute<?, K, ?> attribute;
    private final Type<K> jpaType;
    private final BindableType jpaBindableType;
    private final Class<K> jpaBinableJavaType;
    private final PersistentAttributeType persistentAttributeType;

    public MapKeyAttribute(BlazeCriteriaBuilderImpl criteriaBuilder, MapAttribute<?, K, ?> attribute) {
        this.attribute = attribute;
        this.jpaType = attribute.getKeyType();
        this.jpaBinableJavaType = attribute.getKeyJavaType();
        this.jpaBindableType = Type.PersistenceType.ENTITY
            .equals(jpaType.getPersistenceType()) ? BindableType.ENTITY_TYPE : BindableType.SINGULAR_ATTRIBUTE;

        this.persistentAttributeType = Type.PersistenceType.ENTITY
            .equals(jpaType.getPersistenceType()) ? PersistentAttributeType.MANY_TO_ONE : Type.PersistenceType.EMBEDDABLE
                .equals(jpaType.getPersistenceType()) ? PersistentAttributeType.EMBEDDED : PersistentAttributeType.BASIC;
    }

    @Override
    public String getName() {
        return "map-key";
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
        return persistentAttributeType;
    }

    @Override
    public ManagedType<Map<K, ?>> getDeclaringType() {
        return null;
    }

    @Override
    public Class<K> getJavaType() {
        return attribute.getKeyJavaType();
    }

    @Override
    public Member getJavaMember() {
        return null;
    }

    @Override
    public boolean isAssociation() {
        return persistentAttributeType == PersistentAttributeType.MANY_TO_ONE;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public Type<K> getType() {
        return jpaType;
    }

    @Override
    public BindableType getBindableType() {
        return jpaBindableType;
    }

    @Override
    public Class<K> getBindableJavaType() {
        return jpaBinableJavaType;
    }
}
