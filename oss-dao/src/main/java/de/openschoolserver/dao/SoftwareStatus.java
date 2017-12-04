/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * The persistent class for the SoftwareStatus database table.
 * 
 */
@Entity
@Table(name="SoftwareStatus")
@NamedQueries({
	@NamedQuery(name="SoftwareStatus.findAll",		query="SELECT s FROM SoftwareStatus s"),
	@NamedQuery(name="SoftwareStatus.findByStatus", query="SELECT s FROM SoftwareStatus s WHERE s.status = :STATUS"),
	@NamedQuery(name="SoftwareStatus.getAllForOne", query="SELECT ss, sv FROM SoftwareStatus ss JOIN SoftwareVersion sv ON ss.version_id=sv.id WHERE ss.deviceId= :DEVICE AND sv.softwareId= :SOFTWARE"),
	@NamedQuery(name="SoftwareStatus.getForOne",	query="SELECT ss, sv FROM SoftwareStatus ss JOIN SoftwareVersion sv ON ss.version_id=sv.id WHERE ss.deviceId= :DEVICE AND sv.softwareId= :SOFTWARE AND sv.version = :VERSION"),
})

public class SoftwareStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SOFTWARESTATUS_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWARESTATUS_ID_GENERATOR")
	private long id;
	
	private String status;

	//bi-directional many-to-one association to SoftwareVersion
	@ManyToOne
	@JoinColumn(name="version_id")
    @JsonIgnore
	private SoftwareVersion softwareVersion;

	@Column(name = "version_id", insertable = false, updatable = false)
    private Long versionId;

	//bi-directional many-to-one association to device
	@ManyToOne
	@JoinColumn(name="device_id")
    @JsonIgnore
	private Device device;

	@Column(name = "device_id", insertable = false, updatable = false)
    private Long deviceId;
	
	@Transient
	private String deviceName;
	 
	@Transient
	private String softwareName;
	
	@Transient
	private boolean manually;
	
	@Transient
	private Long softwareId;
	
	@Transient
	private String version;
	
	public SoftwareStatus() {
	}
	
	public SoftwareStatus(Device d, SoftwareVersion sv, String status) {
		this.device = d;
		this.softwareVersion = sv;
		this.status = status;
	}
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public SoftwareVersion getSoftwareVersion() {
		return this.softwareVersion;
	}

	public void setSoftwareVersion(SoftwareVersion softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	public boolean isManually() {
		return manually;
	}

	public void setManually(boolean manually) {
		this.manually = manually;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}