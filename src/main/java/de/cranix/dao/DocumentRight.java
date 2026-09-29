package de.cranix.dao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

/**
 * Grants access to a document for a user or a group.
 * One of user or group must be set. mayWrite describes the granted right:
 * false means read only, true means read + write.
 */
@Entity
@Table(
        name="DocumentRights",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = { "document_id", "user_id" }),
                @UniqueConstraint(columnNames = { "document_id", "group_id" })
        }
)
public class DocumentRight extends AbstractEntity {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="document_id", columnDefinition ="BIGINT UNSIGNED")
    private Document document;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED")
    private User user;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="group_id", columnDefinition ="BIGINT UNSIGNED")
    private Group group;

    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "mayWrite", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean mayWrite = false;

    @Transient
    private Long userId;

    @Transient
    private Long groupId;

    public DocumentRight() {
        super();
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Long getUserId() {
        return this.user != null ? this.user.getId() : this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return this.group != null ? this.group.getId() : this.groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Boolean getMayWrite() {
        return mayWrite;
    }

    public void setMayWrite(Boolean mayWrite) {
        this.mayWrite = mayWrite;
    }
}