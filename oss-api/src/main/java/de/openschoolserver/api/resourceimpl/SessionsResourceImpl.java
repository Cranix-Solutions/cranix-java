/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.openschoolserver.api.resourceimpl;

import de.openschoolserver.api.resources.SessionsResource;


import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.controller.SessionController;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.ArrayList;

public class SessionsResourceImpl implements SessionsResource {
	
    Logger logger = LoggerFactory.getLogger(SessionsResourceImpl.class);
    
    @Override
    public Session createSession(UriInfo ui, String username, String password, String device, HttpServletRequest req) {

        if (username == null || password == null ) {
            throw new WebApplicationException(400);
        }
        if( device == null)
        	device = "dummy";

        Session session =  new Session();
        session.setIP(req.getRemoteAddr());
        SessionController sessionController = new SessionController(session);
        return sessionController.createSessionWithUser(username, password, device);
    }

    @Override
    public Session getStatus(Session session) {
        return session;
    }

    @Override
    public void deleteSession(Session session, String token) {
         final SessionController sessionController = new SessionController(session);
         if( session == null || ! session.getToken().equals(token) ) {
        	 logger.info("deletion of session denied " + token);
        	 throw new WebApplicationException(401);
         }
         sessionController.deleteSession(session);
         logger.debug("deleted session " + token);
    }

	@Override
	public String createToken(UriInfo ui, String username, String password, String device, HttpServletRequest req) {
		Session session = createSession(ui, username,password, device, req);
		if( session == null)
			return "";
		else
			return session.getToken();
	}
	
	@Override
	public String getSessionValue(Session session,String key){
		Device defaultPrinter  = null;
		List<Device> availablePrinters = null;
		List<String> data = new ArrayList<String>();
		final SessionController sessionController = new SessionController(session);
		switch(key) {
		  case "defaultPrinter":
			  if( session.getDevice() != null )
				  defaultPrinter = session.getDevice().getDefaultPrinter();
			  if( defaultPrinter != null )
				  return defaultPrinter.getName();
			  defaultPrinter = session.getRoom().getDefaultPrinter();
			  if( defaultPrinter != null )
				  return defaultPrinter.getName();
			  break;
		  case "availablePrinters":
			  if( session.getDevice() != null)
				  availablePrinters = session.getDevice().getAvailablePrinters();
			  if( availablePrinters == null )
				  availablePrinters = session.getRoom().getAvailablePrinters();
			  if( availablePrinters != null ) {
				  for( Device printer : availablePrinters ) {
					  data.add(printer.getName());
				  }
				  return String.join(" ", data);
			   }
			  break;
		  case "dnsName":
			  if( session.getDevice() != null)
				  return session.getDevice().getName();
			  break;
		  case "domainName": 
			  return sessionController.getConfigValue("SCHOOL_DOMAIN");
		}
		return "";
	}
}
