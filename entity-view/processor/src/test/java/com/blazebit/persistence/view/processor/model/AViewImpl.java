package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityViewManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
public class AViewImpl<X extends Serializable> implements AView<X> {

    public static volatile EntityViewManager evm;

    private final int age;
    private final Integer id;
    private String name;
    private final List<String> names;
    private final List<X> test;

    public AViewImpl() {
        this.age = 0;
        this.id = null;
        this.name = null;
        this.names = new ArrayList<>();
        this.test = new ArrayList<>();
    }

    public AViewImpl(Integer id) {
        this.age = 0;
        this.id = id;
        this.name = null;
        this.names = new ArrayList<>();
        this.test = new ArrayList<>();
    }

    public AViewImpl(int age, Integer id, String name, List<String> names, List<X> test) {
        this.age = age;
        this.id = id;
        this.name = name;
        this.names = names;
        this.test = test;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public List<X> getTest() {
        return test;
    }

    @Override
    public EntityViewManager evm() {
        return evm;
    }
}
