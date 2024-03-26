package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.filter.StartsWithFilter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@EntityView(AEntity.class)
@ViewFilter(name = "test", value = AView.TestFilter.class)
public interface AView<X extends Serializable> extends IdHolderView<Integer> {

    @AttributeFilter(StartsWithFilter.class)
    String getName();

    void setName(String name);

    List<String> getNames();

    Optional<BView> getOptionalValue();

    @TypeUseAnnotation
    int getAge();

    List<X> getTest();

    X getTest2();

    EntityViewManager evm();

    byte[] getBytes();

    List<Set<String>> getMultiNames();

    @MappingParameter("listMappingParameter")
    List<Object> getListMappingParameter();

    @MappingSingular
    Map<String, String> getMap();

    class TestFilter extends ViewFilterProvider {

        private static final String CONSTANT = "myParam";

        @Override
        public <T extends WhereBuilder<T>> T apply(T whereBuilder) {
            return whereBuilder.setWhereExpression( ":" + CONSTANT );
        }
    }
}
