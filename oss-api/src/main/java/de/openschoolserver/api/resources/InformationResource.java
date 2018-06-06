/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resources;

import static de.openschoolserver.api.resources.Resource.*;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.openschoolserver.dao.Announcement;
import de.openschoolserver.dao.Category;
import de.openschoolserver.dao.Contact;
import de.openschoolserver.dao.FAQ;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Session;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("informations")
@Api(value = "informations")
public interface InformationResource {

	@POST
	@Path("announcements")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a ne announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	OssResponse addAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		Announcement annoncement
	);

	@POST
	@Path("contacts")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	OssResponse addContact(
		@ApiParam(hidden = true) @Auth Session session,
		Contact contact
	);

	@POST
	@Path("faqs")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	OssResponse addFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		FAQ faq
	);
   
	@GET
	@Path("announcements")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Announcement> getAnnouncements(
		@ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("newAnnouncements")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Announcement> getNewAnnouncements(
		@ApiParam(hidden = true) @Auth Session session
	);
	
	@PUT
	@Path("announcements/{announcementId}/seem")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Mark the announcement for the user as have seen.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	OssResponse setAnnouncementHaveSeen(
		@ApiParam(hidden = true)      @Auth Session session,
		@PathParam("announcementId")  Long announcementId
	);
	
	@GET
	@Path("contacts")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the contacts corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Contact> getContacts(
		@ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("faqs")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the FAQs corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<FAQ> getFAQs(
		@ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("my/announcements")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the announcements of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	List<Announcement> getMyAnnouncements(
		@ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("my/contacts")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the contacts of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	List<Contact> getMyContacts(
		@ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("my/faqs")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the FAQs of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	List<FAQ> getMyFAQs(
		@ApiParam(hidden = true) @Auth Session session
	);

	@POST
	@Path("announcements/{announcementId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse modifyAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("announcementId") Long announcementId,
		Announcement announcement
	);

	@POST
	@Path("contacts/{contactId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse modifyContact(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("contactId") Long contactId,
		Contact contact
	);

	@POST
	@Path("faqs/{faqId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse modifyFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("faqId") Long faqId,
		FAQ faq
	);

	@DELETE
	@Path("announcements/{announcementId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse deleteAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("announcementId") Long announcementId
	);

	@DELETE
	@Path("contacts/{contactId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse deleteContact(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("contactId") Long contactId
	);

	@DELETE
	@Path("faqs/{faqId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	OssResponse deleteFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("faqId") Long faqId
	);

	@GET
	@Path("categories")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the contacts corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Category> getInformationCategories(
		@ApiParam(hidden = true) @Auth Session session
	);
}
