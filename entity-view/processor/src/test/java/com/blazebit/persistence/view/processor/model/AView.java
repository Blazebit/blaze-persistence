package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;

import java.io.Serializable;
import java.util.List;

@EntityView(AView.class)
public interface AView<X extends Serializable> extends IdHolderView<Integer> {
    String getName();

    void setName(String name);

    List<String> getNames();

    int getAge();

    List<X> getTest();

    EntityViewManager evm();
}
