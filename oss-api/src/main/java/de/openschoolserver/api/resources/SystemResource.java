/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.openschoolserver.api.resources;

import static de.openschoolserver.api.resources.Resource.*;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import de.openschoolserver.dao.Job;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.MissedTranslation;
import de.openschoolserver.dao.Translation;

@Path("system")
@Api(value = "system")
public interface SystemResource {
    
    @GET
    @Path("status")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the system status.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
	@RolesAllowed("system.status")
    List<Map<String, String>> getStatus(
    		@ApiParam(hidden = true) @Auth Session session
    		);
    
    //Handling of enumerates

    @GET
    @Path("enumerates/{type}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "get session status")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<String> getEnumerates(
    		@ApiParam(hidden = true) @Auth Session session,
            @PathParam("type") String type
    );

    @PUT
    @Path("enumerates/{type}/{value}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Creates a new enumerate")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.enumerates")
    OssResponse addEnumerate(
    		@ApiParam(hidden = true) @Auth Session session,
            @PathParam("type") String type,
            @PathParam("value") String value
    );

    @DELETE
    @Path("enumerates/{type}/{value}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes an enumerate")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.enumerates")
    OssResponse deleteEnumerate(
    		@ApiParam(hidden = true) @Auth Session session,
            @PathParam("type") String type,
            @PathParam("value") String value
    );
    
    // Global Configuration
    
    @GET
    @Path("configuration")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the whole system configuration.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.configuration")
    List<Map<String, String>>  getConfig(
    		@ApiParam(hidden = true) @Auth Session session
    		);

    @GET
    @Path("configuration/{key}")
    @Produces(TEXT)
    @ApiOperation(value = "Gets a system configuration value.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.configuration.read")
    String getConfig(
    		@ApiParam(hidden = true) @Auth Session session,
    		@PathParam("key") String key
    		);

    @PUT
    @Path("configuration/{key}/{value}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Sets a system configuration.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.configuration")
    OssResponse setConfig(
    		@ApiParam(hidden = true) @Auth Session session,
            @PathParam("key") String key,
            @PathParam("value") String value
    );
    
    // Firewall configuration
    @GET
    @Path("firewall/incomingRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    Map<String, String>  getFirewallIncomingRules(
    		@ApiParam(hidden = true) @Auth Session session
    		);

    @POST
    @Path("firewall/incomingRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Sets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    OssResponse  setFirewallIncomingRules(
    		@ApiParam(hidden = true) @Auth Session session,
    		Map<String, String> incomingRules
    		);

    @GET
    @Path("firewall/outgoingRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    List<Map<String, String>>  getFirewallOutgoingRules(
    		@ApiParam(hidden = true) @Auth Session session
    		);

    @POST
    @Path("firewall/outgoingRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Sets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    OssResponse  setFirewallOutgoingRules(
    		@ApiParam(hidden = true) @Auth Session session,
    		List<Map<String, String>> incomingRules
    		);

    @GET
    @Path("firewall/remoteAccessRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    List<Map<String, String>>  getFirewallRemoteAccessRules(
    		@ApiParam(hidden = true) @Auth Session session
    		);

    @POST
    @Path("firewall/remoteAccessRules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Sets the incoming firewall rules.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.firewall")
    OssResponse  setFirewallRemoteAccessRules(
    		@ApiParam(hidden = true) @Auth Session session,
    		List<Map<String, String>> incomingRules
    		);
    
    /*
     * Translations stuff
     */
    @POST
    @Path("translate")
    @Produces(TEXT)
    @ApiOperation(value = "Translate a text into a given language")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String translate(
    		@ApiParam(hidden = true) @Auth Session session,
    		MissedTranslation missedTranslataion
    );
    
    @POST
    @Path("translations")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Add or updates a translation.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("sysadmins.translation")
    OssResponse addTranslation(
    		@ApiParam(hidden = true) @Auth Session session,
    		Translation	translation
    );
    
    @GET
    @Path("missedTranslations/{lang}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the list of the missed translations to a language")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("sysadmins.translation")
    List<String> getMissedTranslations(
    		@ApiParam(hidden = true) @Auth Session session,
    		@PathParam("lang") String lang
    );
    
    /*
     * Registration
     */
    @PUT
    @Path("register")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Register the server againts the update server.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.register")
    OssResponse register(
    		@ApiParam(hidden = true) @Auth Session session
    );
    
    /*
     * Package handling
     */
    @GET
    @Path("packages/{filter}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Searches packages.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.packages")
    List<Map<String,String>> searchPackages(
    		@ApiParam(hidden = true) @Auth Session session,
    		@PathParam("filter") String filter
    		);
    
    @POST
    @Path("packages")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Install packages.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.packages")
    OssResponse installPackages(
    		@ApiParam(hidden = true) @Auth Session session,
    		List<String> packages
    		);

    @POST
    @Path("packages/update")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Update packages.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.packages")
    OssResponse updatePackages(
    		@ApiParam(hidden = true) @Auth Session session,
    		List<String> packages
    		);


    @PUT
    @Path("update")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Install all updates on the system.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.update")
    OssResponse updateSyste(
    		@ApiParam(hidden = true) @Auth Session session
    		);
    
    /*
     * Proxy default handling
     */
    @GET
    @Path("proxy/default")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Delivers the default setting for proxy.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.proxy")
    Map<String,List<String[]>> getProxyDefault(
    		@ApiParam(hidden = true) @Auth Session session
    		);
    
    @POST
    @Path("proxy/default")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Delivers the default setting for proxy.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.proxy")
    OssResponse setProxyDefault(
    		@ApiParam(hidden = true) @Auth Session session,
    		Map<String,List<String[]>> acls
    		);

    /*
     * Job management
     */
    @POST
    @Path("jobs/add")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Creates a new job")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.jobs")
    OssResponse createJob(
		@ApiParam(hidden = true) @Auth Session session,
		Job job
    );

    @POST
    @Path("jobs/search")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Searching for jobs by description and time.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.jobs")
    List<Job> searchJob(
		@ApiParam(hidden = true) @Auth Session session,
		Job job
    );
    
    @GET
    @Path("jobs/{jobId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets the job with all parameters inclusive log.")
    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    @RolesAllowed("system.jobs")
    Job getJob(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId
    );

    @PUT
    @Path("jobs/{jobId}/exit/{exitValue}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Set the exit value of a job.")
    @ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.jobs")
    OssResponse setJobExitValue(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId,
		@PathParam("exitValue") Integer exitValue
    );

    @PUT
    @Path("jobs/{jobId}/restart")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Set the exit value of a job.")
    @ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("system.jobs")
    OssResponse restartJob(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId
    );

}
