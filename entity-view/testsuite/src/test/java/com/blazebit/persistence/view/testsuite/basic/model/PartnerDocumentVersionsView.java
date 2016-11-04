package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.entity.Version;

import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 01.10.2016.
 */
@EntityView(Person.class)
public interface PartnerDocumentVersionsView extends IdHolderView<Long> {

    @Mapping("partnerDocument.versions")
    List<Version> getOwnedDocuments();

    @Mapping("partnerDocument.age")
    Long getAge();

    @Mapping("SIZE(ownedDocuments)")
    Long getNumPartners();

    DocumentViewInterface getPartnerDocument();

}
