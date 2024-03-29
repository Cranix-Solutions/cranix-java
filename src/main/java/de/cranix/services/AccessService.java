/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Room;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AccessService{

    Logger logger = LoggerFactory.getLogger(AccessService.class);
    ObjectMapper mapper = new ObjectMapper();
    public AccessService() {
    }

    public AccessInRoom[] getAccessStatus() {
        String[] program = new String[3];
        program[0] = "/usr/sbin/crx_manage_room_access.py";
        program[1] = "--get";
        program[2] = "--all";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
        try {
            return mapper.readValue(reply.toString(),AccessInRoom[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AccessInRoom getAccessStatus(Room room) {
        String[] program = new String[4];
        program[0] = "/usr/sbin/crx_manage_room_access.py";
        program[1] = "--id";
        program[2] = room.getId().toString();
        program[3] = "--get";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
        try {
            return mapper.readValue(reply.toString(),AccessInRoom.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setAccessStatus(Room room, AccessInRoom access, Boolean allowedDirect) {
        List<String> attrs = new ArrayList<>();
        attrs.add("/usr/sbin/crx_manage_room_access.py");
        attrs.add("--id");
        attrs.add(room.getId().toString());
        if (! access.getLogin()) {
            attrs.add("--deny_login");
        }
        if (! access.getPrinting()) {
            attrs.add("--deny_printing");
        }
        if (! access.getDirect() ) {
            attrs.add("--deny_direct");
        }
        if (! access.getPortal()) {
            attrs.add("--deny_portal");
        }
        if (! access.getProxy()) {
            attrs.add("--deny_proxy");
        }
        if ( !allowedDirect ) {
            attrs.add("--let_direct");
        }
        String[] program = attrs.toArray(new String[attrs.size()]);
        logger.debug("setAccessStatus: " + attrs);
        logger.debug("setAccessStatus: " + program);
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
    }


    public void setDefaultAccess() {
        String[] program = new String[2];
        program[0] = "/usr/sbin/crx_manage_room_access.py";
        program[1] = "--set_defaults";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
    }

    public void setDefaultAccess(Room room) {
        String[] program = new String[4];
        program[0] = "/usr/sbin/crx_manage_room_access.py";
        program[1] = "--id";
        program[2] = room.getId().toString();
        program[3] = "--set_defaults";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
    }

}
