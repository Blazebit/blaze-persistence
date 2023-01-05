/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
