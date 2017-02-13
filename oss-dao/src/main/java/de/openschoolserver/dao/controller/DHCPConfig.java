/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao.controller;

import java.io.IOException;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.RoomConfig;
import de.openschoolserver.dao.DeviceConfig;
import de.openschoolserver.dao.tools.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class DHCPConfig extends Controller {
	
	private Path DHCP_CONFIG   = Paths.get("/etc/dhcpd.conf");
	private Path DHCP_TEMPLATE = Paths.get("/usr/share/oss/templates/dhcpd.conf");
	private List<String>       dhcpConfigFile;
	
	public DHCPConfig(Session session) {
		super(session);
		try {
			dhcpConfigFile = Files.readAllLines(DHCP_TEMPLATE);
		}
		catch( IOException e ) { 
			e.printStackTrace();
		}
	}
	
	public void Create() {
		EntityManager em = getEntityManager();
		Query query = em.createNamedQuery("Room.findAll");
		for( Room room : (List<Room>) query.getResultList() ) {
			Query subQuery = em.createNamedQuery("Room.getDeviceCount");
			subQuery.setParameter("id", room.getId());
			if( (Long) subQuery.getSingleResult() < 1)
				continue;
			dhcpConfigFile.add("group {");
			dhcpConfigFile.add("  #Room" + room.getName());
			//TODO add dhcp options and statements from RoomConfig
			WriteRoom(room);
			dhcpConfigFile.add("}");
		}
		try {
		    Files.write(DHCP_CONFIG, dhcpConfigFile );
		    String[] program = new String[3];
		    //TODO write a own class for controlling systemctl
		    program[0] = "systemctl";
		    program[1] = "restart";
		    program[2] = "dhcpd";
		    StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			OSSShellTools.exec(program, reply, error, null);
		}
		catch( IOException e ) { 
			e.printStackTrace();
		}
	}
	
	private void WriteRoom(Room room) {
		for( Device device : room.getDevices() ){
			dhcpConfigFile.add("    host " + device.getName() + " {");
			dhcpConfigFile.add("      hardware ethernet " + device.getMac() + ";");
			dhcpConfigFile.add("      fixed-address " + device.getIp() + ";");
			//TODO add dhcp options and statements from DeviceConfig
			dhcpConfigFile.add("    }");
			if( IPv4.validateIPAddress(device.getWlanIp()) ){
				dhcpConfigFile.add("    host " + device.getName() + "-wlan {");
				dhcpConfigFile.add("      hardware ethernet " + device.getWlanMac() + ";");
				dhcpConfigFile.add("      fixed-address " + device.getWlanIp() + ";");
				//TODO add dhcp options and statements from DeviceConfig
				dhcpConfigFile.add("    }");	
			}
		}
	}
}
