package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.Max;

import javax.validation.constraints.NotNull;


@Entity
@Table(name = "Crx2fas")
@NamedQueries({
        @NamedQuery(name="Crx2fa.findAll", query="SELECT c FROM Crx2fa c")
})
public class Crx2fa extends AbstractEntity {

    /**
     * Type of the Crx2fa:
     * This can be TOTP, SMS, EMAIL
     * At the moment only TOTP is provided.
     */
    @NotNull
    @Column(name = "type", length = 5)
    String crx2faType = "TOTP";

    /** The address where the auth code should be sent.
    * In case of SMS it is the telephone number
    * In case of EMAIL it is a email-address
    * In case of TOTP it is a qrCode
    */
    @Column(name = "address", length = 2000)
    String crx2faAddress = "";

    @Column(name="serial", length = 40)
    String serial ="";

    /**
     * How long is a pin valid
     * By TOTP 30-60 seconds
     * By SMS or EMAIL 5-10 minutes
     * Value is in seconds
     */
    @Column(name = "timeStep")
    Integer timeStep;
    /**
    * Who long is an authorization valid in hours
    */
    @Column(name = "validHours")
    @Max(value = 24, message = "A TOTP session must not be longer valid then 24.")
    Integer validHours = 24;

    public String getCrx2faType() {
        return crx2faType;
    }

    public void setCrx2faType(String crx2faType) {
        this.crx2faType = crx2faType;
    }

    public String getCrx2faAddress() {
        return crx2faAddress;
    }

    public void setCrx2faAddress(String crx2faAddress) {
        this.crx2faAddress = crx2faAddress;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Integer getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(Integer timeStep) {
        this.timeStep = timeStep;
    }

    public Integer getValidHours() {
        return validHours;
    }

    public void setValidHours(Integer validHours) {
        this.validHours = validHours;
    }
}
