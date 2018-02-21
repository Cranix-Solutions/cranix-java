/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao;

import java.io.Serializable;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * The persistent class for the OSSConfig database table.
 *
 */
@Entity
@Table(name="OSSConfig")
@NamedQueries({
	    @NamedQuery(name="OSSConfig.getAllById",    query="SELECT c FROM OSSConfig c WHERE c.objectType = :type AND c.objectId = :id"),
        @NamedQuery(name="OSSConfig.getAllByKey",   query="SELECT c FROM OSSConfig c WHERE c.objectType = :type AND c.keyword  = :keyword"),
        @NamedQuery(name="OSSConfig.get",           query="SELECT c FROM OSSConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword"),
        @NamedQuery(name="OSSConfig.check",         query="SELECT c FROM OSSConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword AND c.value = :value")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class OSSConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        @Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
        private Long id;

        private String objectType;

        private Long  objectId;

        private String keyword;

        private String value;
        
        //bi-directional many-to-one association to User
    	@ManyToOne
    	@JsonIgnore
    	private User creator;

        @Override
        public boolean equals(Object obj) {
                if (obj instanceof OSSConfig && obj !=null) {
                        return getId() == ((OSSConfig)obj).getId();
                }
                return super.equals(obj);
        }

        public OSSConfig() {
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
