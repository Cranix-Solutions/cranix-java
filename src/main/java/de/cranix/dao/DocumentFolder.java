package de.cranix.dao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * A folder used only to group the documents of its creator.
 * Folders are private to the creating user and carry no further access rights.
 * The folder tree is unbounded: a folder may contain any number of subfolders.
 */
@Entity
@Table(name="DocumentFolders")
@NamedQueries({
        @NamedQuery(name="DocumentFolder.findAll", query="SELECT f FROM DocumentFolder f"),
        @NamedQuery(
                name="DocumentFolder.getByName",
                query="SELECT f FROM DocumentFolder f WHERE f.name = :name"
        )
})
public class DocumentFolder extends AbstractEntity {

    @Column(name = "name", length = 128)
    @Size(max=128, message="Name must not be longer then 128 characters.")
    private String name = "";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description = "";

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="parent_id", columnDefinition ="BIGINT UNSIGNED")
    private DocumentFolder parent;

    @OneToMany(mappedBy="folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy="parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DocumentFolder> subFolders = new ArrayList<>();

    @Transient
    private Long parentFolderId;

    public DocumentFolder() {
        super();
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

    public DocumentFolder getParent() {
        return parent;
    }

    public void setParent(DocumentFolder parent) {
        this.parent = parent;
    }

    public Long getParentFolderId() {
        return this.parent != null ? this.parent.getId() : this.parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<DocumentFolder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<DocumentFolder> subFolders) {
        this.subFolders = subFolders;
    }
}