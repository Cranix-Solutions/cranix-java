package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "Crx2faSessions")
@NamedQueries({
        @NamedQuery(name="Crx2faSessions.findAll", query="SELECT c FROM Crx2faSessions c"),
        @NamedQuery(name="Crx2faSessions.findByCode", query="SELECT c FROM Crx2faSessions c WHERE c.code = :code"),
        @NamedQuery(name="Crx2faSessions.findByToken", query="SELECT c FROM Crx2faSessions c WHERE c.token = :token")
})
public class Crx2faSession extends AbstractEntity{

    @NotNull
    @NotEmpty
    @Column(name="token", length =  64)
    @Size(max=64)
    String token;

    @NotNull
    @NotEmpty
    @Column(name="client",length = 128)
    @Size(max = 128)
    String clientIP;

    @ManyToOne
    Crx2fa myCrx2fa;

    public  Crx2faSession() {}
    public Crx2faSession(Crx2fa crx2fa, String clientIPAddress) {
        Random rand = new Random();
        this.setCreator(crx2fa.getCreator());
        this.clientIP = clientIPAddress;
        this.token =  crx2fa.getCreator().getUid() + "_" + UUID.randomUUID();
        this.token = this.token.length() > 64 ? this.token.substring(0, 63) : this.token;
        this.myCrx2fa = crx2fa;
        this.setCreated(new Date());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Crx2fa getMyCrx2fa() {
        return myCrx2fa;
    }

    public void setMyCrx2fa(Crx2fa myCrx2fa) {
        this.myCrx2fa = myCrx2fa;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Transient
    public Date getValidUntil(){
        return new Date(this.myCrx2fa.getCreated().getTime() + this.myCrx2fa.getValidHours() * 360000L);
    }
    @Transient
    public boolean getValid() {
        return (
                (this.myCrx2fa.getCreated().getTime() + this.myCrx2fa.getValidHours() * 360000L) >
                        System.currentTimeMillis()
        );
    }
}
