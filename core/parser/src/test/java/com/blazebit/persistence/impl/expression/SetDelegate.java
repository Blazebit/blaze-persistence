package com.blazebit.persistence.impl.expression;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class SetDelegate<E> implements Set<E> {
    
    protected abstract Set<E> getDelegate();

    public int size() {
        return getDelegate().size();
    }

    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    public boolean contains(Object o) {
        return getDelegate().contains(o);
    }

    public Iterator<E> iterator() {
        return getDelegate().iterator();
    }

    public Object[] toArray() {
        return getDelegate().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return getDelegate().toArray(a);
    }

    public boolean add(E e) {
        return getDelegate().add(e);
    }

    public boolean remove(Object o) {
        return getDelegate().remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return getDelegate().containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return getDelegate().addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return getDelegate().retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return getDelegate().removeAll(c);
    }

    public void clear() {
        getDelegate().clear();
    }
    
}
