/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;

import javax.persistence.*;
import java.sql.Time;


/**
 * The persistent class for the AccessInRoom database table.
 *
 */
@Entity
public class AccessInRoomPIT implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private long id;

	private String  pointintime;

	@Convert(converter=BooleanToStringConverter.class)
	private Boolean monday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean tusday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean wednesday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean thursday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean friday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean saturday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean sunday;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean holiday;

	public AccessInRoomPIT() {
		this.pointintime = "06:00";
		this.monday = true;
		this.tusday = true;
		this.wednesday = true;
		this.thursday = true;
		this.friday = true;
		this.saturday = false;
		this.sunday = false;
		this.holiday = false;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPointInTime() {
		return this.pointintime;
	}

	public void setPointInTime(String pointintime) {
		this.pointintime = pointintime;
	}

	public Boolean getMonday() {
		return this.monday;
	}

	public void setMonday(Boolean monday) {
		this.monday = monday;
	}
	public Boolean getTusday() {
		return this.tusday;
	}

	public void setTusday(Boolean tusday) {
		this.tusday = tusday;
	}
	public Boolean getWednesday() {
		return this.wednesday;
	}

	public void setWednesday(Boolean wednesday) {
		this.wednesday = wednesday;
	}
	public Boolean getThursday() {
		return this.thursday;
	}

	public void setThursday(Boolean thursday) {
		this.thursday = thursday;
	}
	public Boolean getFriday() {
		return this.friday;
	}

	public void setFriday(Boolean friday) {
		this.friday = friday;
	}
	public Boolean getSaturday() {
		return this.saturday;
	}

	public void setSaturday(Boolean saturday) {
		this.saturday = saturday;
	}
	public Boolean getSunday() {
		return this.sunday;
	}

	public void setSunday(Boolean sunday) {
		this.sunday = sunday;
	}
	public Boolean getHoliday() {
		return this.holiday;
	}

	public void setHoliday(Boolean holiday) {
		this.holiday = holiday;
	}
}