package de.cranix.services;

import de.cranix.dao.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static de.cranix.helper.CranixConstants.cranixDocumentsDir;

/**
 * Service to manage documents and their versions.
 * The access rights are stored in DocumentRight entities, the binary content
 * on the filesystem under {@code <DOCUMENTS_DIR>/<documentId>/v<versionNumber>}.
 */
public class DocumentService extends Service {

    Logger logger = LoggerFactory.getLogger(DocumentService.class);

    public DocumentService(Session session, EntityManager em) {
        super(session, em);
    }

    /* ------------------------------------------------------------------ */
    /* Storage helpers                                                     */
    /* ------------------------------------------------------------------ */

    private String getDocumentsDir() {
        String dir = this.getConfigValue("DOCUMENTS_DIR");
        if (dir == null || dir.isEmpty()) {
            dir = cranixDocumentsDir;
        }
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        return dir;
    }

    private Path getDocumentDir(Long documentId) {
        return Paths.get(getDocumentsDir(), documentId.toString());
    }

    private Path getVersionPath(Long documentId, int versionNumber) {
        return getDocumentDir(documentId).resolve("v" + versionNumber);
    }

    private String sanitizeFileName(String fileName) {
        String name = new String(fileName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^a-zA-Z0-9._\\- ]", "_");
        if (name.isEmpty()) {
            name = "document";
        }
        return name;
    }

    private String getMimeType(Path path, String contentType, String fileName) {
        if (contentType != null && contentType.contains("/")) {
            return contentType;
        }
        if (fileName != null) {
            String type = URLConnection.guessContentTypeFromName(fileName);
            if (type != null && !type.isEmpty()) {
                return type;
            }
        }
        try {
            String type = Files.probeContentType(path);
            return (type == null || type.isEmpty()) ? "application/octet-stream" : type;
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private String checkSum(Path path) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            try (InputStream is = Files.newInputStream(path)) {
                int read;
                while ((read = is.read(buffer)) > 0) {
                    md.update(buffer, 0, read);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("checkSum: " + e.getMessage());
            return "";
        }
    }

    /* ------------------------------------------------------------------ */
    /* Access control                                                      */
    /* ------------------------------------------------------------------ */

    public boolean isOwnerOrSuperuser(Document document) {
        return this.isSuperuser() || this.session.getUser().equals(document.getCreator());
    }

    private boolean hasRight(Document document, User user, boolean needWrite) {
        for (DocumentRight right : document.getRights()) {
            if (needWrite && !Boolean.TRUE.equals(right.getMayWrite())) {
                continue;
            }
            if (right.getUser() != null && right.getUser().equals(user)) {
                return true;
            }
            if (right.getGroup() != null) {
                for (Group group : user.getGroups()) {
                    if (group.equals(right.getGroup())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean mayRead(Document document) {
        return this.isOwnerOrSuperuser(document) || hasRight(document, this.session.getUser(), false);
    }

    public boolean mayWrite(Document document) {
        return this.isOwnerOrSuperuser(document) || hasRight(document, this.session.getUser(), true);
    }

    private void loadLazyCollections(Document document) {
        document.getRights().size();
        document.getVersions().size();
    }

    /* ------------------------------------------------------------------ */
    /* Folder management                                                   */
    /* ------------------------------------------------------------------ */

    public boolean isFolderCreator(DocumentFolder folder) {
        return this.isSuperuser() || this.session.getUser().equals(folder.getCreator());
    }

    private List<DocumentFolder> getOwnFolders() {
        List<DocumentFolder> folders = new ArrayList<>();
        for (DocumentFolder folder : (List<DocumentFolder>) em.createNamedQuery("DocumentFolder.findAll").getResultList()) {
            if (this.isFolderCreator(folder)) {
                folders.add(folder);
            }
        }
        return folders;
    }

    public CrxResponse createFolder(String name, String description, Long parentFolderId) {
        if (name == null || name.isEmpty()) {
            return new CrxResponse("ERROR", "A folder name must be set.");
        }
        DocumentFolder folder = new DocumentFolder();
        folder.setName(name);
        folder.setDescription(description == null ? "" : description);
        folder.setCreator(this.session.getUser());
        if (parentFolderId != null && parentFolderId > 0) {
            DocumentFolder parent = em.find(DocumentFolder.class, parentFolderId);
            if (parent == null) {
                return new CrxResponse("ERROR", "Parent folder was not found.");
            }
            if (!this.isFolderCreator(parent)) {
                throw new WebApplicationException(403);
            }
            folder.setParent(parent);
        }
        for (DocumentFolder existing : this.getOwnFolders()) {
            Long existingParent = existing.getParent() != null ? existing.getParent().getId() : null;
            boolean sameParent = (parentFolderId == null && existingParent == null)
                    || (parentFolderId != null && parentFolderId.equals(existingParent));
            if (sameParent && existing.getName().equals(folder.getName())) {
                return new CrxResponse("ERROR", "A folder with this name already exists in the parent folder.");
            }
        }
        try {
            em.getTransaction().begin();
            em.persist(folder);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Folder was created successfully.", folder.getId());
        } catch (Exception e) {
            logger.error("createFolder: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Folder was not created: " + e.getMessage());
        }
    }

    public List<DocumentFolder> getFolders() {
        return this.getOwnFolders();
    }

    public DocumentFolder getFolder(Long folderId) {
        DocumentFolder folder = em.find(DocumentFolder.class, folderId);
        if (folder == null) {
            throw new WebApplicationException(404);
        }
        if (!this.isFolderCreator(folder)) {
            throw new WebApplicationException(403);
        }
        return folder;
    }

    public List<DocumentFolder> getSubFolders(Long folderId) {
        DocumentFolder folder = this.getFolder(folderId);
        return folder.getSubFolders();
    }

    public List<Document> getDocumentsOfFolder(Long folderId) {
        DocumentFolder folder = this.getFolder(folderId);
        List<Document> documents = new ArrayList<>();
        for (Document document : folder.getDocuments()) {
            if (this.mayRead(document)) {
                this.loadLazyCollections(document);
                documents.add(document);
            }
        }
        return documents;
    }

    public CrxResponse patchFolder(Long folderId, DocumentFolder folder) {
        DocumentFolder oldFolder = em.find(DocumentFolder.class, folderId);
        if (oldFolder == null) {
            return new CrxResponse("ERROR", "Folder was not found.");
        }
        if (!this.isFolderCreator(oldFolder)) {
            throw new WebApplicationException(403);
        }
        if (folder.getName() != null && !folder.getName().isEmpty()) {
            oldFolder.setName(folder.getName());
        }
        if (folder.getDescription() != null) {
            oldFolder.setDescription(folder.getDescription());
        }
        try {
            em.getTransaction().begin();
            em.merge(oldFolder);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Folder was modified successfully.");
        } catch (Exception e) {
            logger.error("patchFolder: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Folder was not modified: " + e.getMessage());
        }
    }

    public CrxResponse deleteFolder(Long folderId) {
        DocumentFolder folder = em.find(DocumentFolder.class, folderId);
        if (folder == null) {
            return new CrxResponse("ERROR", "Folder was not found.");
        }
        if (!this.isFolderCreator(folder)) {
            throw new WebApplicationException(403);
        }
        try {
            em.getTransaction().begin();
            this.deleteFolderTree(folder);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Folder was deleted successfully.");
        } catch (Exception e) {
            logger.error("deleteFolder: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Folder was not deleted: " + e.getMessage());
        }
    }

    private void deleteFolderTree(DocumentFolder folder) {
        for (DocumentFolder sub : new ArrayList<>(folder.getSubFolders())) {
            this.deleteFolderTree(sub);
        }
        for (Document document : new ArrayList<>(folder.getDocuments())) {
            this.deleteDocumentFiles(document);
            em.remove(document);
        }
        em.remove(folder);
    }

    public CrxResponse moveDocument(Long documentId, Long folderId) {
        Document document = em.find(Document.class, documentId);
        if (document == null) {
            return new CrxResponse("ERROR", "Document was not found.");
        }
        if (!this.mayWrite(document)) {
            throw new WebApplicationException(403);
        }
        DocumentFolder folder = em.find(DocumentFolder.class, folderId);
        if (folder == null) {
            return new CrxResponse("ERROR", "Folder was not found.");
        }
        if (!this.isFolderCreator(folder)) {
            throw new WebApplicationException(403);
        }
        try {
            em.getTransaction().begin();
            document.setFolder(folder);
            em.merge(document);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Document was moved successfully.");
        } catch (Exception e) {
            logger.error("moveDocument: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Document was not moved: " + e.getMessage());
        }
    }

    public CrxResponse moveToRoot(Long documentId) {
        Document document = em.find(Document.class, documentId);
        if (document == null) {
            return new CrxResponse("ERROR", "Document was not found.");
        }
        if (!this.mayWrite(document)) {
            throw new WebApplicationException(403);
        }
        try {
            em.getTransaction().begin();
            document.setFolder(null);
            em.merge(document);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Document was moved to root successfully.");
        } catch (Exception e) {
            logger.error("moveToRoot: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Document was not moved: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Document management                                                 */
    /* ------------------------------------------------------------------ */

public CrxResponse add(String name, String description, String tags, Long folderId,
                       List<DocumentRight> rights,
                       String contentType,
                       InputStream fileInputStream,
                       FormDataContentDisposition contentDispositionHeader) {
        if (contentDispositionHeader == null || contentDispositionHeader.getFileName() == null
                || contentDispositionHeader.getFileName().isEmpty()) {
            return new CrxResponse("ERROR", "A file must be uploaded.");
        }
        String fileName = this.sanitizeFileName(contentDispositionHeader.getFileName());
        Document document = new Document(session);
        document.setCreator(this.session.getUser());
        document.setName((name == null || name.isEmpty()) ? fileName : name);
        document.setDescription(description == null ? "" : description);
        document.setTags(tags == null ? "" : tags);
        if (folderId != null) {
            DocumentFolder folder = em.find(DocumentFolder.class, folderId);
            if (folder == null) {
                return new CrxResponse("ERROR", "Folder was not found.");
            }
            if (!this.isFolderCreator(folder)) {
                throw new WebApplicationException(403);
            }
            document.setFolder(folder);
        }
        Path versionPath = null;
        try {
            em.getTransaction().begin();
            em.persist(document);
            em.flush();
            versionPath = this.getVersionPath(document.getId(), 1);
            Files.createDirectories(versionPath.getParent());
            Files.copy(fileInputStream, versionPath, StandardCopyOption.REPLACE_EXISTING);
            DocumentVersion version = new DocumentVersion();
            version.setCreator(this.session.getUser());
            version.setVersionNumber(1);
            version.setFileName(fileName);
            version.setMimeType(this.getMimeType(versionPath, contentType, fileName));
            version.setSize(Files.size(versionPath));
            version.setCheckSum(this.checkSum(versionPath));
            version.setFilePath(versionPath.toString());
            version.setIsCurrent(true);
            document.addVersion(version);
            if (rights != null) {
                for (DocumentRight right : rights) {
                    CrxResponse resolved = this.resolveRight(document, right);
                    if (resolved == null) {
                        document.addRight(right);
                        right.setCreator(this.session.getUser());
                    }
                }
            }
            em.getTransaction().commit();
            return new CrxResponse("OK", "Document was created successfully.", document.getId());
        } catch (Exception e) {
            logger.error("add document: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (versionPath != null) {
                try {
                    Files.deleteIfExists(versionPath);
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
            return new CrxResponse("ERROR", "Document was not created: " + e.getMessage());
        }
    }

    public List<Document> getDocuments() {
        List<Document> documents = new ArrayList<>();
        for (Document document : (List<Document>) em.createNamedQuery("Document.findAll").getResultList()) {
            if (this.mayRead(document)) {
                this.loadLazyCollections(document);
                documents.add(document);
            }
        }
        return documents;
    }

    public List<Document> search(Document filter) {
        if (filter != null) {
            if (filter.getFolderId() != null && filter.getFolderId() == 0) {
                filter.setFolderId(null);
            }
            if (filter.getUserId() != null && filter.getUserId() == 0) {
                filter.setUserId(null);
            }
            if (filter.getGroupId() != null && filter.getGroupId() == 0) {
                filter.setGroupId(null);
            }
        }
        String searchTerm = "";
        if (filter != null) {
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                searchTerm = filter.getName();
            } else if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                searchTerm = filter.getTags();
            } else if (filter.getDescription() != null && !filter.getDescription().isEmpty()) {
                searchTerm = filter.getDescription();
            }
        }
        List<Long> groupIds = new ArrayList<>();
        if (filter != null && filter.getUserId() != null) {
            User user = em.find(User.class, filter.getUserId());
            if (user != null) {
                for (Group group : user.getGroups()) {
                    groupIds.add(group.getId());
                }
            }
        }
        logger.debug("searchTerm: " + searchTerm);
        List<Document> documents = new ArrayList<>();
        for (Document document : (List<Document>) em.createNamedQuery("Document.search")
                .setParameter("search", "%" + searchTerm + "%").getResultList()) {
            logger.debug("Document: " + document.getName());
            if (this.mayRead(document)) {
                if (filter != null && filter.getFolderId() != null) {
                    if (document.getFolder() == null
                            || !document.getFolder().getId().equals(filter.getFolderId())) {
			    logger.debug("Not in folder" + document.getFolder().getId() + " " + filter.getFolderId());
                        continue;
                    }
                }
                if (filter != null && (filter.getUserId() != null || filter.getGroupId() != null)) {
                    if (!this.hasRightForTarget(document, filter.getUserId(), filter.getGroupId(), groupIds)) {
                        logger.debug("Has no right");
                        continue;
                    }
                }
                this.loadLazyCollections(document);
                documents.add(document);
            }
        }
        return documents;
    }

    private boolean hasRightForTarget(Document document, Long userId, Long groupId, List<Long> groupIds) {
        for (DocumentRight right : document.getRights()) {
            if (right.getUserId() != null && right.getUserId().equals(userId)) {
                return true;
            }
            if (right.getGroupId() != null
                    && (right.getGroupId().equals(groupId) || groupIds.contains(right.getGroupId()))) {
                return true;
            }
        }
        return false;
    }

    public Document getById(Long id) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayRead(document)) {
            throw new WebApplicationException(403);
        }
        this.loadLazyCollections(document);
        return document;
    }

    public CrxResponse patch(Document document) {
        Document oldDocument = em.find(Document.class, document.getId());
        if (oldDocument == null) {
            return new CrxResponse("ERROR", "Document was not found.");
        }
        if (!this.mayWrite(oldDocument)) {
            throw new WebApplicationException(403);
        }
        if (document.getName() != null && !document.getName().isEmpty()) {
            oldDocument.setName(document.getName());
        }
        if (document.getDescription() != null) {
            oldDocument.setDescription(document.getDescription());
        }
        if (document.getTags() != null) {
            oldDocument.setTags(document.getTags());
        }
        try {
            em.getTransaction().begin();
            em.merge(oldDocument);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Document was modified successfully.");
        } catch (Exception e) {
            logger.error("patch document: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Document was not modified: " + e.getMessage());
        }
    }

    public CrxResponse delete(Long id) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            return new CrxResponse("ERROR", "Document was not found.");
        }
        if (!this.isOwnerOrSuperuser(document)) {
            throw new WebApplicationException(403);
        }
        try {
            this.deleteDocumentFiles(document);
            em.getTransaction().begin();
            em.remove(document);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Document was deleted successfully.");
        } catch (Exception e) {
            logger.error("delete document: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Document was not deleted: " + e.getMessage());
        }
    }

    private void deleteDocumentFiles(Document document) {
        Path dir = this.getDocumentDir(document.getId());
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.error("delete document file: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.error("delete document files: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Content and versions                                                */
    /* ------------------------------------------------------------------ */

    public Response getContent(Long id) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayRead(document)) {
            throw new WebApplicationException(403);
        }
        DocumentVersion version = document.getCurrentVersionObject();
        if (version == null) {
            throw new WebApplicationException(404);
        }
        return this.buildContentResponse(version);
    }

    public List<DocumentVersion> getVersions(Long id) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayRead(document)) {
            throw new WebApplicationException(403);
        }
        return document.getVersions();
    }

    public Response getVersionContent(Long id, int versionNumber) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayRead(document)) {
            throw new WebApplicationException(403);
        }
        DocumentVersion version = this.getVersion(document, versionNumber);
        if (version == null) {
            throw new WebApplicationException(404);
        }
        return this.buildContentResponse(version);
    }

public CrxResponse addVersion(Long id, String comment,
                              String contentType,
                              InputStream fileInputStream,
                              FormDataContentDisposition contentDispositionHeader) {
        if (contentDispositionHeader == null || contentDispositionHeader.getFileName() == null
                || contentDispositionHeader.getFileName().isEmpty()) {
            return new CrxResponse("ERROR", "A file must be uploaded.");
        }
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayWrite(document)) {
            throw new WebApplicationException(403);
        }
        String fileName = this.sanitizeFileName(contentDispositionHeader.getFileName());
        int versionNumber = 0;
        for (DocumentVersion version : document.getVersions()) {
            if (version.getVersionNumber() > versionNumber) {
                versionNumber = version.getVersionNumber();
            }
        }
        versionNumber++;
        Path versionPath = this.getVersionPath(id, versionNumber);
        try {
            Files.createDirectories(versionPath.getParent());
            Files.copy(fileInputStream, versionPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("addVersion: " + e.getMessage());
            return new CrxResponse("ERROR", "New version was not stored: " + e.getMessage());
        }
        try {
            em.getTransaction().begin();
            for (DocumentVersion version : document.getVersions()) {
                if (Boolean.TRUE.equals(version.getIsCurrent())) {
                    version.setIsCurrent(false);
                }
            }
            DocumentVersion newVersion = new DocumentVersion();
            newVersion.setCreator(this.session.getUser());
            newVersion.setVersionNumber(versionNumber);
            newVersion.setFileName(fileName);
            newVersion.setMimeType(this.getMimeType(versionPath, contentType, fileName));
            newVersion.setSize(Files.size(versionPath));
            newVersion.setCheckSum(this.checkSum(versionPath));
            newVersion.setFilePath(versionPath.toString());
            newVersion.setComment(comment == null ? "" : comment);
            newVersion.setIsCurrent(true);
            document.addVersion(newVersion);
            em.getTransaction().commit();
            return new CrxResponse("OK", "New version %d was created successfully.", newVersion.getId());
        } catch (Exception e) {
            logger.error("addVersion: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            try {
                Files.deleteIfExists(versionPath);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
            return new CrxResponse("ERROR", "New version was not created: " + e.getMessage());
        }
    }

    public CrxResponse restore(Long id, int versionNumber) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayWrite(document)) {
            throw new WebApplicationException(403);
        }
        DocumentVersion target = this.getVersion(document, versionNumber);
        if (target == null) {
            throw new WebApplicationException(404);
        }
        try {
            em.getTransaction().begin();
            for (DocumentVersion version : document.getVersions()) {
                version.setIsCurrent(version.getVersionNumber() == versionNumber);
            }
            em.getTransaction().commit();
            return new CrxResponse("OK", "Version %d was restored successfully.", (long) versionNumber);
        } catch (Exception e) {
            logger.error("restore version: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Version was not restored: " + e.getMessage());
        }
    }

    private DocumentVersion getVersion(Document document, int versionNumber) {
        for (DocumentVersion version : document.getVersions()) {
            if (version.getVersionNumber() == versionNumber) {
                return version;
            }
        }
        return null;
    }

    private Response buildContentResponse(DocumentVersion version) {
        File file = new File(version.getFilePath());
        if (!file.exists()) {
            throw new WebApplicationException(404);
        }
        String mimeType = version.getMimeType();
        if (mimeType == null || mimeType.isEmpty() || !mimeType.contains("/")) {
            mimeType = this.getMimeType(file.toPath(), null, version.getFileName());
        }
        ResponseBuilder response = Response.ok((Object) file)
                .header("Content-Disposition", "attachment; filename=\"" + version.getFileName() + "\"")
                .type(mimeType);
        return response.build();
    }

    /* ------------------------------------------------------------------ */
    /* Access rights                                                       */
    /* ------------------------------------------------------------------ */

    public List<DocumentRight> getRights(Long id) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.mayRead(document)) {
            throw new WebApplicationException(403);
        }
        return document.getRights();
    }

    public CrxResponse addRight(Long id, DocumentRight right) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.isOwnerOrSuperuser(document)) {
            throw new WebApplicationException(403);
        }
        CrxResponse resolved = this.resolveRight(document, right);
        if (resolved != null) {
            return resolved;
        }
        try {
            em.getTransaction().begin();
            right.setCreator(this.session.getUser());
            if (right.getMayWrite() == null) {
                right.setMayWrite(false);
            }
            document.addRight(right);
            em.persist(right);
            em.merge(document);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Right was granted successfully.", right.getId());
        } catch (Exception e) {
            logger.error("addRight: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Right was not granted: " + e.getMessage());
        }
    }

    public CrxResponse removeRight(Long id, Long rightId) {
        Document document = em.find(Document.class, id);
        if (document == null) {
            throw new WebApplicationException(404);
        }
        if (!this.isOwnerOrSuperuser(document)) {
            throw new WebApplicationException(403);
        }
        DocumentRight right = em.find(DocumentRight.class, rightId);
        if (right == null || right.getDocument() == null || !right.getDocument().getId().equals(id)) {
            return new CrxResponse("ERROR", "Right was not found.");
        }
        try {
            em.getTransaction().begin();
            document.removeRight(right);
            em.remove(right);
            em.merge(document);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Right was removed successfully.");
        } catch (Exception e) {
            logger.error("removeRight: " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", "Right was not removed: " + e.getMessage());
        }
    }

    /**
     * Resolves the transient userId/groupId of a right to the corresponding
     * user or group entity and checks that exactly one of them is set and
     * that the right does not already exist for the document.
     *
     * @param document the document the right belongs to
     * @param right    the right to resolve
     * @return null if the right could be resolved, otherwise an error response.
     */
    private CrxResponse resolveRight(Document document, DocumentRight right) {
        if (right.getUserId() != null && right.getUserId() == 0) {
            right.setUserId(null);
        }
        if (right.getGroupId() != null && right.getGroupId() == 0) {
            right.setGroupId(null);
        }
        if (right.getUserId() != null && right.getGroupId() != null) {
            return new CrxResponse("ERROR", "Either a user or a group must be set, not both.");
        }
        if (right.getUserId() == null && right.getGroupId() == null) {
            return new CrxResponse("ERROR", "A user or a group must be set.");
        }
        if (right.getUserId() != null) {
            User user = em.find(User.class, right.getUserId());
            if (user == null) {
                return new CrxResponse("ERROR", "User was not found.");
            }
            right.setUser(user);
            right.setGroup(null);
        } else {
            Group group = em.find(Group.class, right.getGroupId());
            if (group == null) {
                return new CrxResponse("ERROR", "Group was not found.");
            }
            right.setGroup(group);
            right.setUser(null);
        }
        for (DocumentRight existing : document.getRights()) {
            if (right.getUserId() != null && right.getUserId().equals(existing.getUserId())) {
                return new CrxResponse("ERROR", "This right already exists for the user.");
            }
            if (right.getGroupId() != null && right.getGroupId().equals(existing.getGroupId())) {
                return new CrxResponse("ERROR", "This right already exists for the group.");
            }
        }
        return null;
    }
}
