package de.openschoolserver.dao.controller;

import java.util.ArrayList;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.tools.*;

public class RoomController extends Controller {

	public RoomController(Session session) {
		super(session);
	}
	
	public boolean isNameUnique(String name)
	{
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Room.getRoomByName");
			query.setParameter("name", name);
			List<Room> rooms = query.getResultList();
			return rooms.isEmpty();
		} catch (Exception e) {
			//logger.error(e.getMessage());
			System.err.println(e.getMessage());
			return false;
		} finally {
			em.close();
		}
	}

	public boolean isDescriptionUnique(String description)
	{
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Room.getRoomByDescription");
			query.setParameter("description", description);
			List<Room> rooms = query.getResultList();
			return rooms.isEmpty();
		} catch (Exception e) {
			//logger.error(e.getMessage());
			System.err.println(e.getMessage());
			return false;
		} finally {
			em.close();
		}
	}
	
	public Room getById(int roomId) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Room.class, roomId);
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	public List<Room> getAll() {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Room.findAll"); 
			return query.getResultList();
		} catch (Exception e) {
			//logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return new ArrayList<>();
		} finally {
			em.close();
		}
	}

	public boolean add(Room room){
		EntityManager em = getEntityManager();
		// First we check if the parameter are unique.
		if( ! this.isNameUnique(room.getName())){
			return false;
		}
		if( !this.isDescriptionUnique(room.getDescription())){
			return false;
		}
		try {
			em.getTransaction().begin();
			em.persist(room);
			em.getTransaction().commit();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}

	public boolean delete(int roomId){
		EntityManager em = getEntityManager();
		//TODO we have to implement it
		return false;
	}

	public List<String> getAvailableIPAddresses(int roomId){
		Room room = this.getById(roomId);
		IPv4 net = new IPv4(room.getStartIP() + "/" + room.getNetMask());
		List<String> allIPs  = net.getAvailableIPs(0);
		List<String> usedIPs = new ArrayList<String>();
		for( Device dev : room.getDevices() ){
			usedIPs.add(dev.getIp());
                        if( dev.getWlanIp() != "" ) {
			   usedIPs.add(dev.getWlanIp());
                        }
		}
		allIPs.removeAll(usedIPs);
		return allIPs;
	}

}
