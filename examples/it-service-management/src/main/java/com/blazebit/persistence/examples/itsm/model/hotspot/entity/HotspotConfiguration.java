/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.hotspot.entity;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedString;
import com.blazebit.persistence.examples.itsm.model.hotspot.Configuration;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class HotspotConfiguration extends Configuration {

    private String logo = "axians-logo.png";

    private String nasIp;

    private int nasPort;

    private int nasCoaPort;

    private String radiusIP;

    private String radiusSecret;

    private String redirectUrl = "http://www.axians.it";

    @NotNull
    @Embedded
    private LoginConfiguration login = new LoginConfiguration();

    @NotNull
    @Embedded
    private TrendooConfiguration trendoo = new TrendooConfiguration();

    @NotNull
    @Embedded
    private PdfConfiguration pdf = new PdfConfiguration();

    @NotNull
    @Embedded
    private PrivacyConfiguration privacy = new PrivacyConfiguration();

    public HotspotConfiguration() {
        super("hotspot");
    }

    public String getRedirectUrl() {
        return this.redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getNasIp() {
        return this.nasIp;
    }

    public void setNasIp(String nasIp) {
        this.nasIp = nasIp;
    }

    public int getNasPort() {
        return this.nasPort;
    }

    public void setNasPort(int nasPort) {
        this.nasPort = nasPort;
    }

    public int getNasCoaPort() {
        return this.nasCoaPort;
    }

    public void setNasCoaPort(int nasCoaPort) {
        this.nasCoaPort = nasCoaPort;
    }

    public String getRadiusIP() {
        return this.radiusIP;
    }

    public void setRadiusIP(String radiusIP) {
        this.radiusIP = radiusIP;
    }

    public String getRadiusSecret() {
        return this.radiusSecret;
    }

    public void setRadiusSecret(String radiusSecret) {
        this.radiusSecret = radiusSecret;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public LoginConfiguration getLoginConfiguration() {
        return this.login;
    }

    public TrendooConfiguration getTrendooConfiguration() {
        return this.trendoo;
    }

    public PdfConfiguration getPdfConfiguration() {
        return this.pdf;
    }

    public PrivacyConfiguration getPrivacyConfiguration() {
        return this.privacy;
    }

    @Embeddable
    public static class LoginConfiguration implements Serializable {

        public enum LoginMode {
            USERNAME_AND_PASSWORD, EMAIL_AS_PASSWORD
        }

        public enum PasswordType {
            ALPHA, NUMERIC, ALPHANUMERIC;
        }

        @NotNull
        private LoginMode loginMode = LoginMode.USERNAME_AND_PASSWORD;

        @NotNull
        private PasswordType passwordType = PasswordType.ALPHANUMERIC;

        private int passwordLength = 8;

        @Embedded
        private LocalizedString welcome;

        @Embedded
        private LocalizedString instruction;

        @Embedded
        private LocalizedString termsShort;

        @Embedded
        private LocalizedString termsFull;

        public LoginConfiguration() {
            this.welcome = new LocalizedString("text/html");
            this.welcome.getLocalizedValues().put(Locale.ENGLISH,
                    "Welcome to our <b>Hotspot</b>!");

            this.instruction = new LocalizedString("text/html");
            this.instruction.getLocalizedValues().put(Locale.ENGLISH,
                    "Input your username and password to obtain Internet access.");

            this.termsShort = new LocalizedString("text/html");
            this.termsShort.getLocalizedValues().put(Locale.ENGLISH,
                    "I hereby declare that I have read and understood the policy "
                            + "statement as mentioned in Art. 13 D. Lgs. 196/2003 "
                            + "and I consent to the processing of my personal data "
                            + "for the purpose of Internet access as described in "
                            + "the full terms.");

            this.termsFull = new LocalizedString("text/html");
            this.termsFull.getLocalizedValues().put(Locale.ENGLISH,
                    "[Full terms]");
        }

        public LoginMode getLoginMode() {
            return this.loginMode;
        }

        public void setLoginMode(LoginMode loginMode) {
            this.loginMode = loginMode;
        }

        public PasswordType getPasswordType() {
            return this.passwordType;
        }

        public void setPasswordType(PasswordType passwordType) {
            this.passwordType = passwordType;
        }

        public int getPasswordLength() {
            return this.passwordLength;
        }

        public void setPasswordLength(int passwordLength) {
            this.passwordLength = passwordLength;
        }

        public LocalizedString getWelcomeMessage() {
            return this.welcome;
        }

        public LocalizedString getInstructionMessage() {
            return this.instruction;
        }

        public LocalizedString getTermsShortMessage() {
            return this.termsShort;
        }

        public LocalizedString getTermsFullMessage() {
            return this.termsFull;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.instruction, this.loginMode,
                    this.passwordLength, this.passwordType, this.termsFull,
                    this.termsShort, this.welcome);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof LoginConfiguration)) {
                return false;
            }
            LoginConfiguration other = (LoginConfiguration) obj;
            return Objects.equals(this.instruction, other.instruction)
                    && this.loginMode == other.loginMode
                    && this.passwordLength == other.passwordLength
                    && this.passwordType == other.passwordType
                    && Objects.equals(this.termsFull, other.termsFull)
                    && Objects.equals(this.termsShort, other.termsShort)
                    && Objects.equals(this.welcome, other.welcome);
        }

    }

    @Embeddable
    public static class PdfConfiguration implements Serializable {

        private boolean download = false;

        private String ssid;

        private String loginPage;

        private String helpNumber;

        public boolean isCanBeDownloaded() {
            return this.download;
        }

        public void setDownload(boolean canBeDownloaded) {
            this.download = canBeDownloaded;
        }

        public String getSsid() {
            return this.ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getLoginPage() {
            return this.loginPage;
        }

        public void setLoginPage(String loginPage) {
            this.loginPage = loginPage;
        }

        public String getHelpNumber() {
            return this.helpNumber;
        }

        public void setHelpNumber(String helpNumber) {
            this.helpNumber = helpNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.download, this.helpNumber, this.loginPage,
                    this.ssid);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof PdfConfiguration)) {
                return false;
            }
            PdfConfiguration other = (PdfConfiguration) obj;
            return this.download == other.download
                    && Objects.equals(this.helpNumber, other.helpNumber)
                    && Objects.equals(this.loginPage, other.loginPage)
                    && Objects.equals(this.ssid, other.ssid);
        }

    }

    @Embeddable
    public static class TrendooConfiguration implements Serializable {

        private String username;

        private String password;

        private String endpoint;

        private boolean enabled = false;

        public String getUsername() {
            return this.username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.enabled, this.endpoint, this.password,
                    this.username);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof TrendooConfiguration)) {
                return false;
            }
            TrendooConfiguration other = (TrendooConfiguration) obj;
            return this.enabled == other.enabled
                    && Objects.equals(this.endpoint, other.endpoint)
                    && Objects.equals(this.password, other.password)
                    && Objects.equals(this.username, other.username);
        }

    }

    @Embeddable
    public static class PrivacyConfiguration implements Serializable {

        private String name = "[Company Name]";

        private String phoneNumber = "[Phone Number]";

        private String email = "[E-mail address]";

        private String address = "[Legal Address]";

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return this.phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return this.address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.address, this.email, this.name,
                    this.phoneNumber);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof PrivacyConfiguration)) {
                return false;
            }
            PrivacyConfiguration other = (PrivacyConfiguration) obj;
            return Objects.equals(this.address, other.address)
                    && Objects.equals(this.email, other.email)
                    && Objects.equals(this.name, other.name)
                    && Objects.equals(this.phoneNumber, other.phoneNumber);
        }

    }

}
