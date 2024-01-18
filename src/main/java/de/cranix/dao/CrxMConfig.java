/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The persistent class for the OSSMConfir database table.
 *
 */
@Entity
@Table(name="CrxMConfig")
@NamedQueries({
		@NamedQuery(name="CrxMConfig.getAllForKey",query="SELECT c FROM CrxMConfig c WHERE c.keyword = :keyword"),
        @NamedQuery(name="CrxMConfig.getAllById",  query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id"),
        @NamedQuery(name="CrxMConfig.getAllByKey", query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.keyword  = :keyword"),
        @NamedQuery(name="CrxMConfig.get",         query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword"),
        @NamedQuery(name="CrxMConfig.getAllObject",query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.keyword = :keyword AND c.value = :value"),
        @NamedQuery(name="CrxMConfig.check",       query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword AND c.value = :value")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class CrxMConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
        @Column(name = "id")
        private Long id;

        private String objectType;

        private Long   objectId;

        private String keyword;

        private String value;
        
        //bi-directional many-to-one association to User
    	@ManyToOne
    	@JsonIgnore
    	private User creator;

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}

        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CrxMConfig other = (CrxMConfig) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

        public CrxMConfig() {
        }

        public Long getId() {
                return this.id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getObjectType() {
                return this.objectType;
        }

        public void setObjectType(String objectType) {
                this.objectType = objectType;
        }

        public Long getObjectId() {
                return this.objectId;
        }

        public void setObjectId(Long objectId) {
                this.objectId = objectId;
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

		public User getCreator() {
			return creator;
		}

		public void setCreator(User creator) {
			this.creator = creator;
		}
}
