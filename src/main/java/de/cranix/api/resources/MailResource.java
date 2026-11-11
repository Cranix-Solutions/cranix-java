/* (c) 2026 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.helper.CranixConstants.cranixBaseDir;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.MailFolder;
import de.cranix.dao.MailMessage;
import de.cranix.dao.MailSendRequest;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.MailService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("mail")
@Api(value = "mail")
@Produces(JSON_UTF8)
public class MailResource {

	Logger logger = LoggerFactory.getLogger(MailResource.class);

	public MailResource() {
	}

	@GET
	@Path("config")
	@ApiOperation(value = "Gets the mail configuration of the current user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public Map<String, String> getConfig(@ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Map<String, String> resp = new MailService(session, em).getMailConfig();
		em.close();
		return resp;
	}

	@GET
	@Path("folders")
	@ApiOperation(value = "Gets the mail folders of the current user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public List<MailFolder> getFolders(@ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).getFolders();
		} catch (Exception e) {
			throw mailError("getFolders", e);
		} finally {
			em.close();
		}
	}

	@GET
	@Path("messages/{folder}")
	@ApiOperation(value = "Gets the messages of a folder.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public List<MailMessage> getMessages(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("folder") String folder,
		@QueryParam("limit") Integer limit,
		@QueryParam("offset") Integer offset
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			int l = (limit == null || limit <= 0) ? 50 : limit;
			int o = (offset == null || offset < 0) ? 0 : offset;
			return new MailService(session, em).getMessages(folder, l, o);
		} catch (Exception e) {
			throw mailError("getMessages", e);
		} finally {
			em.close();
		}
	}

	@GET
	@Path("message/{folder}/{uid}")
	@ApiOperation(value = "Gets a single message inclusive its body and attachments.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public MailMessage getMessage(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("folder") String folder,
		@PathParam("uid") long uid
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).getMessage(folder, uid);
		} catch (Exception e) {
			throw mailError("getMessage", e);
		} finally {
			em.close();
		}
	}

	@GET
	@Path("message/{folder}/{uid}/attachment/{part}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Delivers an attachment of a message.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public Response getAttachment(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("folder") String folder,
		@PathParam("uid") long uid,
		@PathParam("part") int part
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			byte[] content = new MailService(session, em).getAttachment(folder, uid, part);
			return Response.ok(content)
				.header("Content-Disposition", "attachment")
				.build();
		} catch (Exception e) {
			throw mailError("getAttachment", e);
		} finally {
			em.close();
		}
	}

	@POST
	@Path("send")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Sends a mail with optional attachments.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse sendMail(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("to") String to,
		@FormDataParam("cc") String cc,
		@FormDataParam("bcc") String bcc,
		@FormDataParam("subject") String subject,
		@FormDataParam("body") String body,
		@FormDataParam("files") List<FormDataBodyPart> files
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			MailSendRequest request = new MailSendRequest();
			request.setTo(to);
			request.setCc(cc);
			request.setBcc(bcc);
			request.setSubject(subject);
			request.setBody(body);
			Map<String, byte[]> attachments = new LinkedHashMap<String, byte[]>();
			if (files != null) {
				for (FormDataBodyPart file : files) {
					FormDataContentDisposition disposition = file.getFormDataContentDisposition();
					if (disposition == null || disposition.getFileName() == null || disposition.getFileName().isEmpty()) {
						continue;
					}
					InputStream is = file.getValueAs(InputStream.class);
					if (is != null) {
						attachments.put(disposition.getFileName(), is.readAllBytes());
					}
				}
			}
			return new MailService(session, em).sendMail(request, attachments);
		} catch (Exception e) {
			logger.warn("sendMail: " + e.getMessage());
			logger.debug("sendMail", e);
			return new CrxResponse("ERROR", "The mail could not be sent: " + e.getMessage());
		} finally {
			em.close();
		}
	}

	@DELETE
	@Path("message/{folder}/{uid}")
	@ApiOperation(value = "Deletes a message.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse deleteMessage(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("folder") String folder,
		@PathParam("uid") long uid
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).deleteMessage(folder, uid);
		} catch (Exception e) {
			logger.warn("deleteMessage: " + e.getMessage());
			logger.debug("deleteMessage", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	@DELETE
	@Path("messages")
	@ApiOperation(value = "Deletes multiple messages.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse deleteMessages(
		@ApiParam(hidden = true) @Auth Session session,
		@QueryParam("folder") String folder,
		@QueryParam("uids") String uids
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).deleteMessages(folder, parseUids(uids));
		} catch (Exception e) {
			logger.warn("deleteMessages: " + e.getMessage());
			logger.debug("deleteMessages", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	@POST
	@Path("move")
	@ApiOperation(value = "Moves messages into another folder.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse moveMessages(
		@ApiParam(hidden = true) @Auth Session session,
		@QueryParam("folder") String folder,
		@QueryParam("uids") String uids,
		@QueryParam("target") String target
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).moveMessages(folder, parseUids(uids), target);
		} catch (Exception e) {
			logger.warn("moveMessages: " + e.getMessage());
			logger.debug("moveMessages", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	@PUT
	@Path("message/{folder}/{uid}/seen/{seen}")
	@ApiOperation(value = "Sets or removes the seen flag of a message.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse setSeen(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("folder") String folder,
		@PathParam("uid") long uid,
		@PathParam("seen") boolean seen
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).setSeen(folder, uid, seen);
		} catch (Exception e) {
			logger.warn("setSeen: " + e.getMessage());
			logger.debug("setSeen", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	@POST
	@Path("folder")
	@ApiOperation(value = "Creates a mail folder.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse createFolder(
		@ApiParam(hidden = true) @Auth Session session,
		@QueryParam("name") String name,
		@QueryParam("parent") String parent
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).createFolder(name, parent);
		} catch (Exception e) {
			logger.warn("createFolder: " + e.getMessage());
			logger.debug("createFolder", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	@DELETE
	@Path("folder")
	@ApiOperation(value = "Deletes a mail folder.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("mail.use")
	public CrxResponse deleteFolder(
		@ApiParam(hidden = true) @Auth Session session,
		@QueryParam("name") String name
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		try {
			return new MailService(session, em).deleteFolder(name);
		} catch (Exception e) {
			logger.warn("deleteFolder: " + e.getMessage());
			logger.debug("deleteFolder", e);
			return new CrxResponse("ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	private List<Long> parseUids(String uids) {
		List<Long> result = new ArrayList<Long>();
		if (uids != null) {
			for (String uid : uids.split(",")) {
				if (!uid.trim().isEmpty()) {
					result.add(Long.parseLong(uid.trim()));
				}
			}
		}
		return result;
	}

	private javax.ws.rs.WebApplicationException mailError(String method, Exception e) {
		Response.Status status = MailService.isConnectionError(e)
			? Response.Status.SERVICE_UNAVAILABLE
			: Response.Status.INTERNAL_SERVER_ERROR;
		logger.warn(method + ": " + e.getMessage());
		logger.debug(method, e);
		return new javax.ws.rs.WebApplicationException(
			Response.status(status)
				.entity(new CrxResponse("ERROR", e.getMessage()))
				.type(MediaType.APPLICATION_JSON_TYPE)
				.build());
	}
}
