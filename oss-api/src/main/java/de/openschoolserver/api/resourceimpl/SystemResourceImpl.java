package de.openschoolserver.api.resourceimpl;

import java.util.List;

import java.util.Map;

import de.openschoolserver.api.resources.SystemResource;
import de.openschoolserver.dao.Response;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.controller.SystemController;

public class SystemResourceImpl implements SystemResource {

	@Override
	public List<Map<String, String>> getStatus(Session session) {
		SystemController systemController = new SystemController(session);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getEnumerates(Session session, String type) {
		SystemController systemController = new SystemController(session);
		return systemController.getEnumerates(type);
	}

	@Override
	public Response addEnumerate(Session session, String type, String value) {
		SystemController systemController = new SystemController(session);
		return systemController.addEnumerate(type, value);
	}

	@Override
	public Response removeEnumerate(Session session, String type, String value) {
		SystemController systemController = new SystemController(session);
		return systemController.removeEnumerate(type, value);
	}

	@Override
	public List<Map<String, String>> getConfig(Session session) {
		SystemController systemController = new SystemController(session);
		return systemController.getConfig();
	}

	@Override
	public String getConfig(Session session, String key) {
		SystemController systemController = new SystemController(session);
		return systemController.getConfigValue(key);
	}

	@Override
	public Response setConfig(Session session, String key, String value) {
		SystemController systemController = new SystemController(session);
		if( systemController.setConfigValue(key, value) )
			return new Response(session,"OK","Global configuration value was set succesfully."); 
		else
			return new Response(session,"ERROR","Global configuration value could not be set.");
	}

}
