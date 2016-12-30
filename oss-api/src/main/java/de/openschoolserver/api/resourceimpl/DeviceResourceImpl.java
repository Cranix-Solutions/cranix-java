/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resourceimpl;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import de.openschoolserver.api.resources.DeviceResource;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.controller.DeviceController;

public class DeviceResourceImpl implements DeviceResource {

	@Override
	public Device getById(Session session, int deviceId) {
	    final DeviceController deviceController = new DeviceController(session);
	    final Device device = deviceController.getById(deviceId);
	    if (device == null) {
	            throw new WebApplicationException(404);
	    }
	    return device;
	}


	@Override
	public List<Device> getAll(Session session) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final List<Device> devices = deviceController.getAll();
		if (devices == null) {
	            throw new WebApplicationException(404);
	    }
		return devices;
	}

	@Override
	public boolean add(Session session, List<Device> devices) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final boolean result = deviceController.add(devices);
		if (! result) {
	            throw new WebApplicationException(404);
	    }
		return true;
	}

	@Override
	public boolean delete(Session session, List<Integer> deviceIds) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final boolean result = deviceController.delete(deviceIds);
		if (! result) {
	            throw new WebApplicationException(404);
	    }
		return false;
	}

	@Override
	public Device getByIP(Session session, String IP) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final Device device = deviceController.getByIP(IP);
		if (device == null) {
	            throw new WebApplicationException(404);
	    }
		return device;
	}

	@Override
	public Device getByMAC(Session session, String MAC) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final Device device = deviceController.getByMAC(MAC);
		if (device == null) {
	            throw new WebApplicationException(404);
	    }
		return device;
	}


	@Override
	public List<Device> getByType(Session session, String type) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final List<Device> devices = deviceController.getByTpe(type);
		if (devices == null) {
	            throw new WebApplicationException(404);
	    }
		return devices;
	}


	@Override
	public Device getByName(Session session, String Name) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final Device device = deviceController.getByName(Name);
		return device;
	}


	@Override
	public String getDefaultPrinter(Session session, int deviceId) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final String printer = deviceController.getDefaultPrinter(deviceId);
		return printer;
	}


	@Override
	public List<String> getAvailablePrinters(Session session, int deviceId) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final List<String> printers = deviceController.getAvailablePrinters(deviceId);
		return printers;
	}


	@Override
	public List<String> getLoggedInUsers(Session session, String IP) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final List<String> users = deviceController.getLoggedInUsers(IP);
		return users;
	}

	@Override
	public List<String> getLoggedInUsers(Session session, int deviceID) {
		// TODO Auto-generated method stub
		final DeviceController deviceController = new DeviceController(session);
		final List<String> users = deviceController.getLoggedInUsers(deviceID);
		return users;
	}
}
