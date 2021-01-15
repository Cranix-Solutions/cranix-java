/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import de.cranix.dao.Device;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;
import de.cranix.dao.PrintersOfManufacturer;
import de.cranix.dao.Session;
import de.cranix.services.CloneToolService;
import de.cranix.services.PrinterService;
import de.cranix.helper.CrxEntityManagerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import static de.cranix.helper.CranixConstants.*;


@Path("printers")
@Api(value = "printers")
public class PrinterResource {

	public PrinterResource() { }

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new printer.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse addPrinter(
		@ApiParam(hidden = true) @Auth  Session session,
		@FormDataParam("name")		String  name,
		@FormDataParam("mac")		String  mac,
		@FormDataParam("roomId")	Long    roomId,
		@FormDataParam("ip")		String  ip,
		@FormDataParam("model")		String  model,
		@FormDataParam("windowsDriver") boolean windowsDriver,
		@FormDataParam("file")	 final InputStream fileInputStream,
		@FormDataParam("file")	 final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).addPrinter(name.toLowerCase().trim(),mac,roomId,ip,model,windowsDriver,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@POST
	@Path("addQueue")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new printer.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse addPrinterQueue(
		@ApiParam(hidden = true) @Auth  Session session,
		@FormDataParam("name")	  String  name,
		@FormDataParam("deviceId")  Long    deviceId,
		@FormDataParam("model")String  model,
		@FormDataParam("windowsDriver") boolean windowsDriver,
		@FormDataParam("file")	  final InputStream fileInputStream,
		@FormDataParam("file")	  final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).addPrinterQueue(session,name.toLowerCase().trim(),deviceId,model,windowsDriver,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@POST
	@Path("{printerId}/setDriver")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new printer.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse setDriver(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")	Long printerId,
		@FormDataParam("model")	String  model,
		@FormDataParam("file")  final InputStream fileInputStream,
		@FormDataParam("file")  final FormDataContentDisposition contentDispositionHeader
	) {

		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).setDriver(printerId,model,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the lis of printers.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public List<Printer> getPrinters( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Printer> resp = new PrinterService(session,em).getPrinters();
		em.close();
		return resp;
	}

	@GET
	@Path("allDevices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets thes lis of all printer devices.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public List<Device> getPrinterDevices( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Device> resp = new CloneToolService(session,em).getByName("Printer").getDevices();
		em.close();
		return resp;
	}

	@GET
	@Path("{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the a printer by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public Printer getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")	Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Printer resp = new PrinterService(session,em).getById(printerId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")		Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).deletePrinter(printerId);
		em.close();
		return resp;
	}

	@PUT
	@Path("{printerId}/reset")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Resets a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse resetPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")		Long printerId
	)  {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).resetPrinter(printerId);
		em.close();
		return resp;
	}

	@PUT
	@Path("{printerId}/enable")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Enable a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse enablePrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")		Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).enablePrinter(printerId);
		em.close();
		return resp;
	}

	@PUT
	@Path("{printerId}/disable")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Disable a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse disablePrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerId")		Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).disablePrinter(printerId);
		em.close();
		return resp;
	}

	@PUT
	@Path("{printerId}/activateWindowsDriver")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a new group or user to a giwen AdHocLan room")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No device was found"),
			@ApiResponse(code = 405, message = "Device is not a Printer."),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse activateWindowsDriver(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("printerId")		Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).activateWindowsDriver(printerId);
		em.close();
		return resp;
	}

	/*
	 * Some functions byName instead of by id.
	 * Some of these can be useless. Avoid to use it.
	 */
	@DELETE
	@Path("byName/{printerName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse deletePrinter(
		@ApiParam(hidden = true)	@Auth Session session,
		@PathParam("printerName")	String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).deletePrinter(printerName);
		em.close();
		return resp;
	}

	@PUT
	@Path("byName/{printerName}/reset")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Resets a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse resetPrinter(
		@ApiParam(hidden = true)	@Auth Session session,
		@PathParam("printerName")	String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).resetPrinter(printerName);
		em.close();
		return resp;
	}

	@PUT
	@Path("byName/{printerName}/enable")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Enable a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse enablePrinter(
		@ApiParam(hidden = true)	@Auth Session session,
		@PathParam("printerName")	String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).enablePrinter(printerName);
		em.close();
		return resp;
	}

	@PUT
	@Path("byName/{printerName}/disable")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Disable a printer")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public CrxResponse disablePrinter(
		@ApiParam(hidden = true)	@Auth Session session,
		@PathParam("printerName")	String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).disablePrinter(printerName);
		em.close();
		return resp;
	}

	@PUT
	@Path("byName/{printerName}/activateWindowsDriver")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a new group or user to a giwen AdHocLan room")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.add")
	public CrxResponse activateWindowsDriver(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("printerName")		String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new PrinterService(session,em).activateWindowsDriver(printerName);
		em.close();
		return resp;
	}

	@GET
	@Path("availableDrivers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of the available drivers sorted by printer manufacturer",
			notes = "The result is a hashmap of arrays:"
			+ "{<br>"
			+ "&nbsp;&nbsp;&nbsp; manufacturer1: [ Model1, Model2, Model3 ],<br>"
			+ "&nbsp;&nbsp;&nbsp; manufacturer2: [ Model4, Model5, Model6 ]<br>"
			+ "}<br>"
			+ "The selected model ")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public Map<String,String[]> getAvailableDrivers( @ApiParam(hidden = true) @Auth Session session)
	{
		return PrinterService.getAvailableDrivers();
	}

	@GET
	@Path("getDrivers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of the available drivers of a manufacturer",
			notes = "The result is a hashmap of arrays:"
			+ "{<br>"
			+ "&nbsp;&nbsp;&nbsp; manufacturer1: [ Model1, Model2, Model3 ],<br>"
			+ "&nbsp;&nbsp;&nbsp; manufacturer2: [ Model4, Model5, Model6 ]<br>"
			+ "}<br>"
			+ "The selected model ")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No device was found"),
		@ApiResponse(code = 405, message = "Device is not a Printer."),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("printers.manage")
	public List<PrintersOfManufacturer> getDrivers( @ApiParam(hidden = true) @Auth Session session) {
		return PrinterService.getDrivers();
	}
}
