/* (c) 2024 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
/**
 * @author Peter Varkoly <pvarkoly@cephalix.eu>
 *
 */
package de.cranix.api.resources;
import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import de.cranix.services.CalendarService;

@Path("calendar")
@Api(value = "calendar")
@Produces(JSON_UTF8)
public class CrxCalendarResource {
    Logger logger = LoggerFactory.getLogger(UserResource.class);

    public CrxCalendarResource() {
    }

    @GET
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.use","calendar.read"})
    public List<CrxCalendar> getMyAll(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<CrxCalendar> events = new CalendarService(session, em).getMyAll();
        em.close();
        if (events == null) {
            throw new WebApplicationException(404);
        }
        return events;
    }

    @POST
    @Path("filter")
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.use","calendar.read"})
    public List<CrxCalendar> getMyFiltered(
            @ApiParam(hidden = true) @Auth Session session,
            FilterObject map
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<CrxCalendar> events = new CalendarService(session, em).getMyFiltered(map);
        em.close();
        if (events == null) {
            throw new WebApplicationException(404);
        }
        return events;
    }

    @POST
    @ApiOperation(value = "Create new event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("calendar.use")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxCalendar event
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).add(event);
        em.close();
        return crxResponse;
    }

    @PATCH
    @ApiOperation(value = "Modify an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("calendar.use")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            CrxCalendar event
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).modify(event);
        em.close();
        return crxResponse;
    }

    @GET
    @Path("{eventId}")
    @ApiOperation(value = "Deletes an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.use","calendar.read"})
    public CrxCalendar getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("eventId") Long eventId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxCalendar crxCalendar = new CalendarService(session, em).getById(eventId);
        em.close();
        return crxCalendar;
    }
    @DELETE
    @Path("{eventId}")
    @ApiOperation(value = "Deletes an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("calendar.use")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("eventId") Long eventId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).delete(eventId);
        em.close();
        return crxResponse;
    }

    @PUT
    @Path("sync")
    @ApiOperation(value = "Sync events to the filesystem")
    @RolesAllowed("calendar.manage")
    public CrxResponse sync(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).exportCalendar();
        em.close();
        return crxResponse;
    }


}

