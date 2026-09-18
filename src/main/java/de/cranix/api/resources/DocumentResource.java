package de.cranix.api.resources;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Document;
import de.cranix.dao.DocumentFolder;
import de.cranix.dao.DocumentRight;
import de.cranix.dao.DocumentVersion;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.DocumentService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("documents")
@Api(value = "documents")
@Produces(JSON_UTF8)
public class DocumentResource {

    public DocumentResource() {
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Creates a new document. Only the metadata and the initial version are stored.<br>" +
            "Access rights for other users or groups can be granted via POST /documents/{documentId}/rights.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.add")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            @FormDataParam("name") String name,
            @FormDataParam("description") String description,
            @FormDataParam("tags") String tags,
            @FormDataParam("folderId") Long folderId,
            @FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("file") final FormDataBodyPart filePart
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String contentType = filePart != null && filePart.getMediaType() != null ? filePart.getMediaType().toString() : null;
        CrxResponse resp = new DocumentService(session, em).add(name, description, tags, folderId, null, contentType, fileInputStream, contentDispositionHeader);
        em.close();
        return resp;
    }

    @GET
    @ApiOperation(value = "Gets all documents the session user may read.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<Document> get(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Document> resp = new DocumentService(session, em).getDocuments();
        em.close();
        return resp;
    }

    @POST
    @Path("search")
    @ApiOperation(value = "Searches for documents. The filter must contain at least one of the fields " +
            "name, description or tags.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<Document> search(
            @ApiParam(hidden = true) @Auth Session session,
            Document filter
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Document> resp = new DocumentService(session, em).search(filter);
        em.close();
        return resp;
    }

    @GET
    @Path("{documentId}")
    @ApiOperation(value = "Gets the metadata of a document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public Document getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Document resp = new DocumentService(session, em).getById(documentId);
        em.close();
        return resp;
    }

    @GET
    @Path("{documentId}/content")
    @ApiOperation(value = "Downloads the current version of the document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public Response getContent(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Response resp = new DocumentService(session, em).getContent(documentId);
        em.close();
        return resp;
    }

    @POST
    @Path("{documentId}/versions")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Creates a new version of the document and makes it the current one.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse addVersion(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            @FormDataParam("comment") String comment,
            @FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader,
            @FormDataParam("file") final FormDataBodyPart filePart
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String contentType = filePart != null && filePart.getMediaType() != null ? filePart.getMediaType().toString() : null;
        CrxResponse resp = new DocumentService(session, em).addVersion(documentId, comment, contentType, fileInputStream, contentDispositionHeader);
        em.close();
        return resp;
    }

    @GET
    @Path("{documentId}/versions")
    @ApiOperation(value = "Gets all versions of a document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<DocumentVersion> getVersions(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<DocumentVersion> resp = new DocumentService(session, em).getVersions(documentId);
        em.close();
        return resp;
    }

    @GET
    @Path("{documentId}/versions/{versionNumber}/content")
    @ApiOperation(value = "Downloads a specific version of the document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public Response getVersionContent(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            @PathParam("versionNumber") Integer versionNumber
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Response resp = new DocumentService(session, em).getVersionContent(documentId, versionNumber);
        em.close();
        return resp;
    }

    @PUT
    @Path("{documentId}/versions/{versionNumber}/restore")
    @ApiOperation(value = "Makes a specific version the current version of the document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse restore(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            @PathParam("versionNumber") Integer versionNumber
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).restore(documentId, versionNumber);
        em.close();
        return resp;
    }

    @PATCH
    @Path("{documentId}")
    @ApiOperation(value = "Modifies the metadata of a document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse patch(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            Document document
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        document.setId(documentId);
        CrxResponse resp = new DocumentService(session, em).patch(document);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{documentId}")
    @ApiOperation(value = "Deletes a document with all its versions and access rights.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.delete")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).delete(documentId);
        em.close();
        return resp;
    }

    @GET
    @Path("{documentId}/rights")
    @ApiOperation(value = "Gets the access rights of a document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<DocumentRight> getRights(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<DocumentRight> resp = new DocumentService(session, em).getRights(documentId);
        em.close();
        return resp;
    }

    @POST
    @Path("{documentId}/rights")
    @ApiOperation(value = "Grants a user or a group the access to a document.<br>" +
            "The body must contain a userId or a groupId and optionally mayWrite.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse addRight(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            DocumentRight right
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).addRight(documentId, right);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{documentId}/rights/{rightId}")
    @ApiOperation(value = "Removes the access right of a user or group from a document.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse removeRight(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            @PathParam("rightId") Long rightId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).removeRight(documentId, rightId);
        em.close();
        return resp;
    }

    /* ------------------------------------------------------------------ */
    /* Folder management                                                   */
    /* ------------------------------------------------------------------ */

    @POST
    @Path("folders")
    @ApiOperation(value = "Creates a private folder. A folder is visible only to its creator. " +
            "The name must be unique within the parent folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.add")
    public CrxResponse createFolder(
            @ApiParam(hidden = true) @Auth Session session,
            DocumentFolder folder
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em)
                .createFolder(folder.getName(), folder.getDescription(), folder.getParentFolderId());
        em.close();
        return resp;
    }

    @GET
    @Path("folders")
    @ApiOperation(value = "Gets all folders of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<DocumentFolder> getFolders(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<DocumentFolder> resp = new DocumentService(session, em).getFolders();
        em.close();
        return resp;
    }

    @GET
    @Path("folders/{folderId}")
    @ApiOperation(value = "Gets the metadata of a folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public DocumentFolder getFolder(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("folderId") Long folderId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        DocumentFolder resp = new DocumentService(session, em).getFolder(folderId);
        em.close();
        return resp;
    }

    @PATCH
    @Path("folders/{folderId}")
    @ApiOperation(value = "Modifies the metadata of a folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse patchFolder(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("folderId") Long folderId,
            DocumentFolder folder
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        folder.setId(folderId);
        CrxResponse resp = new DocumentService(session, em).patchFolder(folderId, folder);
        em.close();
        return resp;
    }

    @DELETE
    @Path("folders/{folderId}")
    @ApiOperation(value = "Deletes a folder with all its subfolders, documents, versions and access rights.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.delete")
    public CrxResponse deleteFolder(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("folderId") Long folderId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).deleteFolder(folderId);
        em.close();
        return resp;
    }

    @GET
    @Path("folders/{folderId}/subfolders")
    @ApiOperation(value = "Gets the subfolders of a folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<DocumentFolder> getSubFolders(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("folderId") Long folderId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<DocumentFolder> resp = new DocumentService(session, em).getSubFolders(folderId);
        em.close();
        return resp;
    }

    @GET
    @Path("folders/{folderId}/documents")
    @ApiOperation(value = "Gets the documents of a folder the session user may read.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.search")
    public List<Document> getDocumentsOfFolder(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("folderId") Long folderId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Document> resp = new DocumentService(session, em).getDocumentsOfFolder(folderId);
        em.close();
        return resp;
    }

    /* ------------------------------------------------------------------ */
    /* Moving documents between folders                                    */
    /* ------------------------------------------------------------------ */

    @PUT
    @Path("{documentId}/folder/{folderId}")
    @ApiOperation(value = "Moves a document into a folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse moveDocument(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId,
            @PathParam("folderId") Long folderId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).moveDocument(documentId, folderId);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{documentId}/folder")
    @ApiOperation(value = "Moves a document to the root level, i.e. removes it from its folder.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("documents.modify")
    public CrxResponse moveDocumentToRoot(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("documentId") Long documentId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new DocumentService(session, em).moveToRoot(documentId);
        em.close();
        return resp;
    }
}