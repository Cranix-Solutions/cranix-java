/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;


/**
 * The persistent class for the AccessInRoom database table.
 * 
 */
@Entity
@Table(name="AccessInRoom")
@NamedQueries( {
	@NamedQuery(name="AccessInRoom.findAll",            query="SELECT a FROM AccessInRoom a"),
	@NamedQuery(name="AccessInRoom.findByRoom",         query="SELECT a FROM AccessInRoom a WHERE a.room = :room"),
	@NamedQuery(name="AccessInRoom.findActualAccesses", query="SELECT a FROM AccessInRoom a WHERE a.accessPIT.pointintime = :time")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class AccessInRoom implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private long id;

	private String accesstype;

	//uni-directional many-to-one association to Room
	@ManyToOne
	@JsonIgnore
	private Room room;
	
	@Column(name="room_id", insertable=false, updatable=false)
	private Long roomId;

	@OneToOne(mappedBy="accessinroom", cascade=CascadeType.REMOVE )
	private AccessInRoomFW fwAccess;

	@OneToOne(mappedBy="accessinroom", cascade=CascadeType.REMOVE )
	private AccessInRoomACT actAccess;

	@OneToOne(mappedBy="accessinroom", cascade=CascadeType.REMOVE )
	private AccessInRoomPIT accessPIT;

	public AccessInRoom() {
		this.accesstype = "DEFAULT";
		this.fwAccess   = null;
		this.actAccess  = null;
		this.accessPIT  = null;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getRoomId() {
		return this.roomId;
	}

	public void setRoomId(long roomid) {
		this.roomId = roomid;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessInRoom && obj !=null) {
			return getId() == ((AccessInRoom)obj).getId();
		}
		return super.equals(obj);
	}
	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getAccessType() {
		return this.accesstype;
	}

	public void setAccessType(String accesstype) {
		this.accesstype = accesstype;
	}

	public AccessInRoomFW getAccessInRoomFW() {
		return this.fwAccess;
	}

	public void setAccessInRoomFW(AccessInRoomFW fwAccess) {
		this.fwAccess = fwAccess;
	}

	public AccessInRoomACT getAccessInRoomACT() {
		return this.actAccess;
	}

	public void setAccessInRoomACT(AccessInRoomACT actAccess) {
		this.actAccess = actAccess;
	}

	public AccessInRoomPIT getAccessInRoomPIT() {
		return this.accessPIT;
	}

	public void setAccessInRoomPIT(AccessInRoomPIT accessPIT) {
		this.accessPIT = accessPIT;
	}

}
