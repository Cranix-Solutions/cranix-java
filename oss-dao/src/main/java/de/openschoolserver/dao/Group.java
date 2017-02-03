/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the Groups database table.
 * 
 */
@Entity
@Table(name="Groups")
@NamedQueries({
	@NamedQuery(name="Group.findAll", query="SELECT g FROM Group g"),
	@NamedQuery(name="Group.getByName",  query="SELECT g FROM Group g WHERE g.name = :name OR g.description = :name"),
	@NamedQuery(name="Group.search", query="SELECT g FROM Group g WHERE g.name LIKE :search OR g.description LIKE :search"),
})
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private long id;

	private String name;

	private String description;

	private String groupType;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="groups")
	private List<User> users;

	public Group() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGroupType() {
		return this.groupType;
	}

	public void setGroupType(String grouptype) {
		this.groupType = grouptype;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}
