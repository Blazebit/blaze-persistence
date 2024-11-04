/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.integration.datanucleus.function.DataNucleusEntityManagerFactoryIntegrator;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.api.jpa.metadata.JPAAnnotationReader;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.PackageMetaData;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CTEAnnotationReader extends JPAAnnotationReader {

    private final boolean isDataNucleus4;
    
    public CTEAnnotationReader(MetaDataManager mgr) {
        super(mgr);
        String[] supportedAnnotationPacakges = new String[supportedPackages.length + 1];
        System.arraycopy(supportedPackages, 0, supportedAnnotationPacakges, 0, supportedPackages.length);
        supportedAnnotationPacakges[supportedAnnotationPacakges.length - 1] = "com.blazebit.persistence";
        setSupportedAnnotationPackages(supportedAnnotationPacakges);
        isDataNucleus4 = DataNucleusEntityManagerFactoryIntegrator.MAJOR < 5;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractClassMetaData getMetaDataForClass(Class cls, PackageMetaData pmd, ClassLoaderResolver clr) {
        AbstractClassMetaData cmd = super.getMetaDataForClass(cls, pmd, clr);
        
        if (cmd == null) {
            return null;
        }
        
        if (!cls.isAnnotationPresent(CTE.class)) {
            return cmd;
        }

        if (isDataNucleus4) {
            cmd.setIdentityType(IdentityType.NONDURABLE);
            cmd.addExtension("view-definition", "--");

            for (int i = 0; i < cmd.getNoOfMembers(); i++) {
                AbstractMemberMetaData mmd = cmd.getMetaDataForMemberAtRelativePosition(i);
                if (mmd.isPrimaryKey()) {
                    mmd.setPrimaryKey(false);
                }
            }
        } else {
            cmd.setIdentityType(IdentityType.APPLICATION);
            cmd.addExtension("view-definition", "--");
        }
        return cmd;
    }

}
