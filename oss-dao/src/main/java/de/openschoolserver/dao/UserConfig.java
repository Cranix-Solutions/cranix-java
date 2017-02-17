/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;


import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * The persistent class for the UserConfig database table.
 *
 */
@Entity
@Table(name="UserConfig")
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class UserConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        @Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
        private long id;

        private String keyword;

        private String value;

        //bi-directional many-to-one association to User
        @ManyToOne
        @JsonIgnore
        private User user;

        @Override
        public boolean equals(Object obj) {
                if (obj instanceof UserConfig && obj !=null) {
                        return getId() == ((UserConfig)obj).getId();
                }
                return super.equals(obj);
        }

	public UserConfig() {
	}

        public long getId() {
                return this.id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public String getKeyword() {
                return this.keyword;
        }

        public void setKeyword(String keyword) {
                this.keyword = keyword;
        }

        public String getValue() {
                return this.value;
        }

        public void setValue(String value) {
                this.value = value;
        }

        public User getUser() {
                return this.user;
        }

        public void setUser(User user) {
                this.user = user;
        }
}
