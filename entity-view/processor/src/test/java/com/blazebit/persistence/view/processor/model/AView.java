package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@EntityView(AEntity.class)
@ViewFilter(name = "test", value = AView.TestFilter.class)
public interface AView<X extends Serializable> extends IdHolderView<Integer> {
    String getName();

    void setName(String name);

    List<String> getNames();

    int getAge();

    List<X> getTest();

    EntityViewManager evm();

    byte[] getBytes();

    List<Set<String>> getMultiNames();

    class TestFilter extends ViewFilterProvider {

        private static final String CONSTANT = "myParam";

        @Override
        public <T extends WhereBuilder<T>> T apply(T whereBuilder) {
            return whereBuilder.setWhereExpression( ":" + CONSTANT );
        }
    }
}
