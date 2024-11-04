/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.hotspot.view;

import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedStringView;
import com.blazebit.persistence.examples.itsm.model.hotspot.entity.HotspotConfiguration;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(HotspotConfiguration.class)
@UpdatableEntityView
public interface HotspotConfigurationView {

    @IdMapping
    Long getId();

    String getRedirectUrl();

    void setRedirectUrl(String redirectUrl);

    String getNasIp();

    void setNasIp(String nasIp);

    int getNasPort();

    void setNasPort(int nasPort);

    int getNasCoaPort();

    void setNasCoaPort(int nasCoaPort);

    String getRadiusSecret();

    void setRadiusSecret(String radiusSecret);

    String getLogo();

    void setLogo(String logo);

    @Mapping("login")
    LoginConfigurationView getLoginConfiguration();

    @Mapping("pdf")
    HotspotConfiguration.PdfConfiguration getPdfConfiguration();

    @Mapping("trendoo")
    HotspotConfiguration.TrendooConfiguration getTrendooConfiguration();

    @Mapping("privacy")
    HotspotConfiguration.PrivacyConfiguration getPrivacyConfiguration();

    @EntityView(HotspotConfiguration.LoginConfiguration.class)
    @UpdatableEntityView
    interface LoginConfigurationView {

        HotspotConfiguration.LoginConfiguration.LoginMode getLoginMode();

        void setLoginMode(HotspotConfiguration.LoginConfiguration.LoginMode loginMode);

        HotspotConfiguration.LoginConfiguration.PasswordType getPasswordType();

        void setPasswordType(HotspotConfiguration.LoginConfiguration.PasswordType passwordType);

        int getPasswordLength();

        void setPasswordLength(int passwordLength);

        @Mapping("welcome")
        LocalizedStringView getWelcomeMessage();

        @Mapping("instruction")
        LocalizedStringView getInstructionMessage();

        @Mapping("termsShort")
        LocalizedStringView getTermsShortMessage();

        @Mapping("termsFull")
        LocalizedStringView getTermsFullMessage();

    }

}
