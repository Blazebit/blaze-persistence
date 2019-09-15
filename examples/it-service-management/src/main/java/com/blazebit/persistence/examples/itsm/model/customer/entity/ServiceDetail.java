/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Embeddable
public class ServiceDetail implements Serializable {

    private String serviceHours;

    @Lob
    private String serviceNote;

    private LocalDate installationDate;

    private String switchboardRelease;

    private String switchboardVersion;

    private String number;

    private String password;

    private String cpuCode;

    private Boolean greetingSystem;

    @Lob
    private String switchboardNote;

    private String vpnType;

    private String vpnAddress;

    private String vpnUsername;

    private String vpnPassword;

    @Lob
    private String vpnNote;

    @ManyToOne
    private User technician;

    @ManyToOne
    private Switchboard switchboard;

    @OrderColumn
    @ElementCollection
    private List<SwitchboardAddress> switchboardAddresses = new ArrayList<>();

    @OrderColumn
    @ElementCollection
    private List<SwitchboardInterface> switchboardInterfaces = new ArrayList<>();

    public String getServiceNote() {
        return this.serviceNote;
    }

    public void setServiceNote(String serviceNote) {
        this.serviceNote = serviceNote;
    }

    public LocalDate getInstallationDate() {
        return this.installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public String getSwitchboardRelease() {
        return this.switchboardRelease;
    }

    public void setSwitchboardRelease(String switchboardRelease) {
        this.switchboardRelease = switchboardRelease;
    }

    public String getSwitchboardVersion() {
        return this.switchboardVersion;
    }

    public void setSwitchboardVersion(String switchboardVersion) {
        this.switchboardVersion = switchboardVersion;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCpuCode() {
        return this.cpuCode;
    }

    public void setCpuCode(String cpuCode) {
        this.cpuCode = cpuCode;
    }

    public Boolean getGreetingSystem() {
        return this.greetingSystem;
    }

    public void setGreetingSystem(Boolean greetingSystem) {
        this.greetingSystem = greetingSystem;
    }

    public String getSwitchboardNote() {
        return this.switchboardNote;
    }

    public void setSwitchboardNote(String switchboardNote) {
        this.switchboardNote = switchboardNote;
    }

    public String getVpnType() {
        return this.vpnType;
    }

    public void setVpnType(String vpnType) {
        this.vpnType = vpnType;
    }

    public String getVpnAddress() {
        return this.vpnAddress;
    }

    public void setVpnAddress(String vpnAddress) {
        this.vpnAddress = vpnAddress;
    }

    public String getVpnUsername() {
        return this.vpnUsername;
    }

    public void setVpnUsername(String vpnUsername) {
        this.vpnUsername = vpnUsername;
    }

    public String getVpnPassword() {
        return this.vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }

    public String getVpnNote() {
        return this.vpnNote;
    }

    public void setVpnNote(String vpnNote) {
        this.vpnNote = vpnNote;
    }

    public User getTechnician() {
        return this.technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public String getServiceHours() {
        return this.serviceHours;
    }

    public void setServiceHours(String serviceHours) {
        this.serviceHours = serviceHours;
    }

    public List<SwitchboardAddress> getSwitchboardAddresses() {
        return this.switchboardAddresses;
    }

    public List<SwitchboardInterface> getSwitchboardInterfaces() {
        return this.switchboardInterfaces;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serviceHours, this.switchboardAddresses,
                this.switchboardInterfaces);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ServiceDetail)) {
            return false;
        }
        ServiceDetail other = (ServiceDetail) obj;
        return Objects.equals(this.serviceHours, other.serviceHours)
                && Objects.equals(this.switchboardAddresses,
                        other.switchboardAddresses)
                && Objects.equals(this.switchboardInterfaces,
                        other.switchboardInterfaces);
    }

}
