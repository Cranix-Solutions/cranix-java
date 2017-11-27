package de.openschoolserver.api;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.controller.DeviceController;
import de.openschoolserver.dao.controller.SessionController;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import java.io.InputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;

public class OssLoggedIn extends Application<ServerConfiguration>{

    @Override
    public String getName() {
        return "OSS GetLoggedIn";
    }

    public static void main(String[] args) throws Exception {
    	new OssLoggedIn().run(args);
    }

	@Override
	public void run(ServerConfiguration configuration, Environment environment) throws Exception {

        Session session = new Session();
        SessionController sessionController = new SessionController(session);
        String token = sessionController.getProperty("de.openschoolserver.api.auth.localhost");
        session = sessionController.getByToken(token);
        final DeviceController deviceController = new DeviceController(session);
        BufferedReader in = new BufferedReader(new  InputStreamReader(System.in));
        while( true ){
       	 try {
       		 String ip = in.readLine();
       		 Device device = deviceController.getByIP(ip);
       			if( device != null && device.getLoggedIn() != null ) {
       				System.out.println("OK user=\"" + device.getLoggedIn().get(0).getUid()+ "\"");
       			} else {
       				System.out.println("ERR user=\"No user logged in " + ip + "\"");
       			}
       	 } catch (IOException e) {
       		 System.err.println("IO ERROR: " + e.getMessage());
       	 }
        }
		
	}
}
