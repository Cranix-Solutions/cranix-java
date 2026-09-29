package de.cranix.dao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * A version of a document. Every uploaded file creates a new immutable version.
 * The version marked as current delivers the content of the document.
 */
@Entity
@Table(
        name="DocumentVersions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = { "document_id", "versionNumber" })
        }
)
public class DocumentVersion extends AbstractEntity {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="document_id", columnDefinition ="BIGINT UNSIGNED")
    private Document document;

    @Column(name = "versionNumber")
    private Integer versionNumber;

    @Column(name = "fileName", length = 256)
    @Size(max=256, message="fileName must not be longer then 256 characters.")
    private String fileName;

    @Column(name = "mimeType", length = 64)
    @Size(max=64, message="mimeType must not be longer then 64 characters.")
    private String mimeType;

    @Column(name = "documentSize", columnDefinition ="BIGINT UNSIGNED")
    private Long size;

    @Column(name = "checkSum", length = 64)
    @Size(max=64, message="checkSum must not be longer then 64 characters.")
    private String checkSum;

    @Column(name = "filePath", length = 512)
    @JsonIgnore
    @Size(max=512, message="filePath must not be longer then 512 characters.")
    private String filePath;

    @Column(name = "comment", length = 256)
    @Size(max=256, message="comment must not be longer then 256 characters.")
    private String comment = "";

    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "isCurrent", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean isCurrent = false;

    public DocumentVersion() {
        super();
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean current) {
        isCurrent = current;
    }
}