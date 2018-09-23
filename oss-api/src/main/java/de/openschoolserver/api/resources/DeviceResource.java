/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resources;


import io.dropwizard.auth.Auth;




import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Printer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static de.openschoolserver.api.resources.Resource.*;

@Path("devices")
@Api(value = "devices")
public interface DeviceResource {

	/*
	 * GET devices/<deviceId>
	 */
    @GET
    @Path("{deviceId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get device by id")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Device not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("device.manage")
    Device getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId
    );

    /*
	 * GET devices/byHWConf/{hwconfId}
	 */
    @GET
    @Path("byHWConf/{hwconfId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get device by hwconfId.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Device not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("device.manage")
    List<Device> getByHWConf(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("hwconfId") Long id
    );
    
    /*
     * GET devices/getAll
     */
    @GET
    @Path("all")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get all devices")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    List<Device> getAll(
            @ApiParam(hidden = true) @Auth Session session
    );
    
    /*
     * GET devices/getAll
     */
    @GET
    @Path("allUsedDevices/{saltClientOnly}")
    @Produces(TEXT)
    @ApiOperation(value = "Get the FQHNs of all devices on which a user is logged in. If saltClientOnly set 1 only salt clients will be listed.")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    String getAllUsedDevices(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("saltClientOnly") Long saltClientOnly
    );

    /*
     * GET search/{search}
     */
    @GET
    @Path("search/{search}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Search for device by name or IP or MAC address by substring.")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.search")
    List<Device> search(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("search") String search
    );
    
    /*
   	 * POST devices/getDevices
   	 */
       @POST
       @Path("getDevices")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Gets a list of device objects to the list of deviceIds.")
       @ApiResponses(value = {
               @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @PermitAll
       List<Device> getDevices(
               @ApiParam(hidden = true) @Auth Session session,
               List<Long> deviceIds
       );
    
    /*
     * GET devices/byIP/<IPAddress>
     */
    @GET
    @Path("byIP/{IP}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get device by MAC address")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.search")
    Device getByIP(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP
    );

    /*
     * GET devices/hostnameByIP/<IPAddress>
     */
    @GET
    @Path("hostnameByIP/{IP}")
    @Produces(TEXT)
    @ApiOperation(value = "Get device by MAC address")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getHostnameByIP(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP
    );

    /*
     * GET devices/byMAC/<MACAddress>
     */
    @GET
    @Path("byMAC/{MAC}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get device by MAC address")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.search")
    Device getByMAC(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("MAC") String MAC
    );

    /*
     * GET devices/hostnameByIP/<IPAddress>
     */
    @GET
    @Path("hostnameByMAC/{MAC}")
    @Produces(TEXT)
    @ApiOperation(value = "Get device by MAC address")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getHostnameByMAC(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("MAC") String MAC
    );

    /*
     * GET devices/byName/<Name>
     */
    @GET
    @Path("byName/{Name}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get device by Name")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.search")
    Device getByName(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("Name") String Name
    );

    /*
     * GET devices/{deviceId}/defaultPrinter
     */
    @GET
    @Path("{deviceId}/defaultPrinter")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get default printer Name")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    Printer getDefaultPrinter(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId
    );
    
    /*
     * GET devices/{deviceId}/defaultPrinter
     */
    @GET
    @Path("byIP/{IP}/defaultPrinter")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get default printer Name")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getDefaultPrinter(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP
    );

    /*
     * PUT devices/{deviceId}/defaultPrinter/{defaultPrinterId}
     */
    @PUT
    @Path("{deviceId}/defaultPrinter/{defaultPrinterId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Set default printer Name")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    OssResponse setDefaultPrinter(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId,
            @PathParam("defaulPrinterId") long defaultPrinterId
    );

    /*
     * GET devices/{deviceId}/availablePrinters
     */
    @GET
    @Path("{deviceId}/availablePrinters")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the list of name of the available printers")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<Printer> getAvailablePrinters(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId
    );
    
    /*
     * GET devices/{deviceId}/availablePrinters
     */
    @GET
    @Path("byIP/{IP}/availablePrinters")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the list of name of the available printers")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getAvailablePrinters(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP
    );
    
    /*
     * PUT devices/{deviceId}/availablePrinters
     */
    @PUT
    @Path("{deviceId}/availablePrinters")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Set the list of name of the available printers")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    OssResponse setAvailablePrinters(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId,
            List<Long> availablePrinters
    );
    
    /*
     * GET devices/loggedInUsers/{IP-Address}
     */
    @GET
    @Path("loggedInUsers/{IP}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the logged on users on a device defined by IP.")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("room.manage")
    List<String> getLoggedInUsers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP
    );
    
    /*
     * GET devices/loggedIn/{IP-Address}
     */
    @GET
    @Path("loggedIn/{IP}")
    @Produces(TEXT)
    @ApiOperation(value = "Get the first logged on user on a device defined by IP.")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    String getFirstLoggedInUser(
            @PathParam("IP") String IP
    );
    /*
     * PUT devices/loggedInUsers/{IP-Address}/{userName}
     */
    @PUT
    @Path("loggedInUsers/{IP}/{userName}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Set the logged on user on a device defined by IP. All other users logged on users will be removed.")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    OssResponse setLoggedInUsers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP,
            @PathParam("userName") String userName
    );
    
    /*
     * DELETE devices/loggedInUsers/{IP-Address}/{userName}
     */
    @DELETE
    @Path("loggedInUsers/{IP}/{userName}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the logged on users on a device defined by IP.")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    OssResponse removeLoggedInUser(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("IP") String IP,
            @PathParam("userName") String userName
    );
    
    /*
     * GET devices/{deviceId}/loggedInUsers
     */
    @GET
    @Path("{deviceId}/loggedInUsers")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the logged on users on a device defined by the deviceId.")
        @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    List<String> getLoggedInUsers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") long deviceId
    );
    
    /*
     * GET devices/refreshConfig
     */
    @PUT
    @Path("refreshConfig")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Refresh the DHCP DNS and SALT Configuration.")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.add")
    void refreshConfig(
            @ApiParam(hidden = true) @Auth Session session
    );
    
    /*
     * PUSH devices/modify
     */
    @POST
    @Path("modify")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Modify the configuration of one device.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.modify")
    OssResponse modify(
    		@ApiParam(hidden = true) @Auth Session session,
            Device device
    );
    
    /*
     * DELETE 
     */
    @DELETE
    @Path("{deviceId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Removes a device.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.delete")
    OssResponse delete(
    		@ApiParam(hidden = true) @Auth Session session,
    		@PathParam("deviceId") long deviceId
    );
    
    @POST
    @Path("import")
    @Produces(JSON_UTF8)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation( 	value =	"Import devices from a CSV file. This MUST have following format:\\n" ,
    				notes = "* Separator is the semicolon ';'.<br>" +
    						"* A header line must be provided.<br>" +
    						"* The header line is case insensitive.<br>" +
    						"* The fields Room and MAC are mandatory.<br>" +
    						"* The import is only allowed in existing rooms.<br>")
    @ApiResponses(value = {
                @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("device.manage")
    OssResponse importDevices(
    		@ApiParam(hidden = true) @Auth Session session,
            @FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
    );
    
    /*
    * GET devices/{deviceId}/actions
    */
   @GET
   @Path("{deviceId}/actions")
   @Produces(JSON_UTF8)
   @ApiOperation(value = "Delivers a list of available actions for a device.")
   @ApiResponses(value = {
           // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
           @ApiResponse(code = 500, message = "Server broken, please contact administrator")
   })
   @RolesAllowed("device.manage")
   List<String> getAvailableDeviceActions(
           @ApiParam(hidden = true) @Auth Session session,
           @PathParam("deviceId") Long deviceId
   );

   /*
    * PUT devices/{deviceId}/{action}
    */
   @PUT
   @Path("{deviceId}/actions/{action}")
   @Produces(JSON_UTF8)
   @ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, unlockInput, lockInput, cleanUpLoggedIn.")
   @ApiResponses(value = {
           // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
           @ApiResponse(code = 500, message = "Server broken, please contact administrator")
   })
   @RolesAllowed("device.manage")
   OssResponse manageDevice(
           @ApiParam(hidden = true) @Auth Session session,
           @PathParam("deviceId") Long deviceId,
           @PathParam("action") String action
   );
   
   /*
    * POST devices/{deviceId}/actionWithMap/{action}
    */
   @POST
   @Path("{deviceId}/actionWithMap/{action}")
   @Produces(JSON_UTF8)
   @ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout."
   		+ "This version of call allows to send a map with some parametrs:"
   		+ "graceTime : seconds to wait befor execute action."
   		+ "message : the message to shown befor/during execute the action.")
   @ApiResponses(value = {
           // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
           @ApiResponse(code = 500, message = "Server broken, please contact administrator")
   })
   @RolesAllowed("device.manage")
   OssResponse manageDevice(
           @ApiParam(hidden = true) @Auth Session session,
           @PathParam("deviceId") Long deviceId,
           @PathParam("action") String action,
           Map<String, String> actionContent
   );
   
   @DELETE
   @Path("cleanUpLoggedIn")
   @Produces(JSON_UTF8)
   @ApiOperation(value = "Cleans up all logged in users on all devices")
   @ApiResponses(value = {
           // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
           @ApiResponse(code = 500, message = "Server broken, please contact administrator")
   })
   @RolesAllowed("device.manage")
   OssResponse cleanUpLoggedIn(
           @ApiParam(hidden = true) @Auth Session session
   );

}
