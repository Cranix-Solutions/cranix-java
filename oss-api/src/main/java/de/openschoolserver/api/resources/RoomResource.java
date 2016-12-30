/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resources;


import io.dropwizard.auth.Auth;

import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;

import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;

import java.util.List;
import java.util.Map;

import static de.openschoolserver.api.resources.Resource.JSON_UTF8;

@Path("rooms")
@Api(value = "rooms")
public interface RoomResource {

	/*
	 * GET rooms/<roomId>
	 */
    @GET
    @Path("{roomId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "get room by id")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Room not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    Room getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomId") int roomId
    );

    /*
     * GET rooms/getall
     */
    @GET
    @Path("getall")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "get all rooms")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<Room> getAll(
            @ApiParam(hidden = true) @Auth Session session
    );

    /*
     * POST rooms/add { hash }
     */
    @POST
    @Path("add")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "create new room")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    boolean add(
            @ApiParam(hidden = true) @Auth Session session,
            Room room
    );
    
    /*
     * GET rooms/delete/{roomId}
     */
    @GET
    @Path("{roomId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "delete room by id")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Room not found"),
        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    boolean delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomId") int roomId
    );

    
    /*
     * GET rooms/getall
     */
    @GET
    @Path("{roomId}/getAvailableIPAddresses")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "get all available ip-adresses of the room")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<String> getAvailableIPAddresses(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomId") int roomId
    );

    /*
     * GET rooms/getNextRoomIP
     */
    @GET
    @Path("getNextRoomIP/{netMask}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "create new room")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getNextRoomIP(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("netMask") int netMask
    );
    
    /*
     * GET rooms/{roomID}/getLoggedInUsers
     */
    @GET
    @Path("{roomID}/getLoggedInUsers")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "create new room")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<Map<String, String>> getLoggedInUsers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomID") int roomID
    );
}
