package de.cranix.dao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * A document managed by the document management API.
 * The binary content is stored on the filesystem, one file per version.
 * Access rights are granted via DocumentRight entries for users and groups.
 */
@Entity
@Table(name="Documents")
@NamedQueries({
        @NamedQuery(
                name="Document.search",
                query="SELECT d FROM Document d WHERE d.name LIKE :search OR d.description LIKE :search OR d.tags LIKE :search"
        ),
        @NamedQuery(name="Document.findAll", query="SELECT d FROM Document d")
})
public class Document extends AbstractEntity {

    @Column(name = "name", length = 128)
    @Size(max=128, message="Name must not be longer then 128 characters.")
    private String name = "";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description = "";

    @Column(name = "tags", length = 256)
    @Size(max=256, message="Tags must not be longer then 256 characters.")
    private String tags = "";

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="folder_id", columnDefinition ="BIGINT UNSIGNED")
    private DocumentFolder folder;

    @OneToMany(mappedBy="document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentRight> rights = new ArrayList<>();

    @OneToMany(mappedBy="document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions = new ArrayList<>();

    /*
     * Transient fields to make the life in front end more simply.
     * They mirror the values of the current version.
     */
    @Transient
    private Integer currentVersion;

    @Transient
    private String fileName;

    @Transient
    private String mimeType;

    @Transient
    private Long size;

    @Transient
    private String checkSum;

    @Transient
    private String comment;

    @Transient
    private Long folderId;

    public Document() {
        super();
    }

    public Document(Session session) {
        super(session);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<DocumentRight> getRights() {
        return rights;
    }

    public void setRights(List<DocumentRight> rights) {
        this.rights = rights;
    }

    public void addRight(DocumentRight right) {
        if (!this.rights.contains(right)) {
            this.rights.add(right);
            right.setDocument(this);
        }
    }

    public void removeRight(DocumentRight right) {
        this.rights.remove(right);
        right.setDocument(null);
    }

    public List<DocumentVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DocumentVersion> versions) {
        this.versions = versions;
    }

    public void addVersion(DocumentVersion version) {
        if (!this.versions.contains(version)) {
            this.versions.add(version);
            version.setDocument(this);
        }
    }

    public void removeVersion(DocumentVersion version) {
        this.versions.remove(version);
        version.setDocument(null);
    }

    public Integer getCurrentVersion() {
        if (this.currentVersion == null) {
            DocumentVersion current = this.getCurrentVersionObject();
            this.currentVersion = current != null ? current.getVersionNumber() : null;
        }
        return this.currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    /**
     * Returns the version marked as current or the version with the highest number.
     * @return the current DocumentVersion or null if no version exists.
     */
    public DocumentVersion getCurrentVersionObject() {
        DocumentVersion current = null;
        for (DocumentVersion version : this.versions) {
            if (Boolean.TRUE.equals(version.getIsCurrent())) {
                return version;
            }
            if (current == null || version.getVersionNumber() > current.getVersionNumber()) {
                current = version;
            }
        }
        return current;
    }

    public String getFileName() {
        DocumentVersion current = this.getCurrentVersionObject();
        return current != null ? current.getFileName() : null;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        DocumentVersion current = this.getCurrentVersionObject();
        return current != null ? current.getMimeType() : null;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        DocumentVersion current = this.getCurrentVersionObject();
        return current != null ? current.getSize() : null;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getCheckSum() {
        DocumentVersion current = this.getCurrentVersionObject();
        return current != null ? current.getCheckSum() : null;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getComment() {
        DocumentVersion current = this.getCurrentVersionObject();
        return current != null ? current.getComment() : null;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public DocumentFolder getFolder() {
        return folder;
    }

    public void setFolder(DocumentFolder folder) {
        this.folder = folder;
    }

    public Long getFolderId() {
        return this.folder != null ? this.folder.getId() : this.folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
}