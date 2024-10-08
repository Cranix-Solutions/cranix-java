/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import de.cranix.dao.*;

public class AdHocLanService extends Service {

    Logger logger = LoggerFactory.getLogger(AdHocLanService.class);

    public AdHocLanService(Session session, EntityManager em) {
        super(session, em);
    }

    public List<AdHocRoom> getAll() {
        List<AdHocRoom> resp = new ArrayList<AdHocRoom>();
        RoomService roomService = new RoomService(this.session, this.em);
        for (Room room : roomService.getByType("AdHocAccess")) {
            resp.add(this.roomToAdHoc(room));
        }
        return resp;
    }

    public Category getAdHocCategoryOfRoom(Room room) {
        for (Category category : room.getCategories()) {
            logger.debug("getAdHocCategoryOfRoom" + category);
            if (category.getCategoryType().equals("AdHocAccess")) {
                //category.setIds();
                return category;
            }
        }
        return null;
    }

    public Category getAdHocCategoryOfRoom(Long roomId) {
        Room room = this.em.find(Room.class, roomId);
        return ((room == null) ? null : getAdHocCategoryOfRoom(room));
    }

    public Room getAdHocRoomOfGroup(Group group) {
        for (Category category : group.getCategories()) {
            if (category.getCategoryType().equals("AdHocAccess")) {
                for (Room room : category.getRooms()) {
                    if (room.getRoomType().equals("AdHocAccess")) {
                        return room;
                    }
                }
            }
        }
        return null;
    }

    public CrxResponse add(AdHocRoom adHocRoom) {
        Room room = new Room();
        room.setName(adHocRoom.getName());
        room.setDescription(adHocRoom.getDescription());
        room.setNetMask(adHocRoom.getNetMask());
        room.setDevCount(adHocRoom.getDevCount());
        room.setRoomType("AdHocAccess");
        if (adHocRoom.getNetwork() != null) {
            room.setNetwork(adHocRoom.getNetwork());
        }
        if (adHocRoom.getDevicesProUser() != null) {
            room.setPlaces(adHocRoom.getDevicesProUser());
        } else {
            room.setPlaces(adHocRoom.getPlaces());
        }
        room.setRoomControl(adHocRoom.getRoomControl());
        //Search the BYOD HwConf
        if (room.getHwconf() == null) {
            room.setHwconf(new CloneToolService(this.session, this.em).getByName("BYOD"));
        }
        logger.debug("Add AdHocLan: " + room);
        RoomService roomService = new RoomService(this.session, this.em);
        ;
        CrxResponse crxResponseRoom = roomService.add(room);
        logger.debug("Add AdHocLan after creating: " + room);
        if (crxResponseRoom.getCode().equals("ERROR")) {
            return crxResponseRoom;
        }
        Long roomId = crxResponseRoom.getObjectId();
        Category category = new Category();
        category.setCategoryType("AdHocAccess");
        category.setName(room.getName());
        category.setDescription(room.getDescription());
        category.setCreator(this.session.getUser());
        category.setPublicAccess(false);
        category.setStudentsOnly(adHocRoom.isStudentsOnly());
        logger.debug("Add AdHocLan category: " + category);
        CategoryService categoryService = new CategoryService(this.session, this.em);
        ;
        CrxResponse crxResponseCategory = categoryService.add(category);
        if (crxResponseCategory.getCode().equals("ERROR")) {
            roomService.delete(crxResponseRoom.getObjectId(), false);
            return crxResponseCategory;
        }
        Long categoryId = crxResponseCategory.getObjectId();
        crxResponseCategory = categoryService.addMember(categoryId, "Room", roomId);
        logger.debug("Add room to Category:" + categoryId + " " + roomId);
        if (crxResponseCategory.getCode().equals("ERROR")) {
            if (crxResponseRoom.getObjectId() != null) {
                roomService.delete(crxResponseRoom.getObjectId(), false);
            }
            if (crxResponseCategory.getObjectId() != null) {
                categoryService.delete(crxResponseCategory.getObjectId());
            }
            return crxResponseCategory;
        }
        if (adHocRoom.getGroupIds() != null) {
            for (Long id : adHocRoom.getGroupIds()) {
                categoryService.addMember(categoryId, "Group", id);
            }
        }
        if (adHocRoom.getUserIds() != null) {
            for (Long id : adHocRoom.getUserIds()) {
                categoryService.addMember(categoryId, "User", id);
            }
        }
        return crxResponseRoom;
    }

    public CrxResponse delete(Long adHocRoomId) {
        try {
            Category category = this.getAdHocCategoryOfRoom(adHocRoomId);
            //category.setIds();
            logger.debug("Delete adHocRoom:" + category);
            RoomService roomService = new RoomService(this.session, this.em);
            for (Long roomId : category.getRoomIds()) {
                roomService.delete(roomId, true);
            }
            this.em.merge(category);
            this.em.getTransaction().begin();
            for (Object o : category.getFaqs()) {
                this.em.remove(o);
            }
            for (Object o : category.getAnnouncements()) {
                this.em.remove(o);
            }
            for (Object o : category.getContacts()) {
                this.em.remove(o);
            }
            this.em.remove(category);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteMember:" + e.getMessage());
            return new CrxResponse("erease category ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Category was modified");
    }

    /**
     * Modify the setting of an AdHocRoom
     *
     * @param room The AdHocRoom to modify. Following attributes can be modified:
     *             * Description
     *             * The count of the devices an user may register
     *             * The control type in a room
     *             * If the room is only for students
     * @return The result in a CrxResponse object.
     */
    public CrxResponse modify(AdHocRoom room) {
        final RoomService rc = new RoomService(session, em);
        Room oldRoom = rc.getById(room.getId());
        if (!oldRoom.getRoomType().equals("AdHocAccess")) {
            em.close();
            return new CrxResponse("ERROR", "This is not an AdHocLan room");
        } else {
            try {
                em.getTransaction().begin();
                oldRoom.setDescription(room.getDescription());
                oldRoom.setPlaces(room.getPlaces());
                oldRoom.setRoomControl(room.getRoomControl());
                oldRoom.setRoomType("AdHocAccess");
                Category cat = this.getAdHocCategoryOfRoom(oldRoom);
                cat.setStudentsOnly(room.isStudentsOnly());
                cat.setDescription(room.getDescription());
                //Handle group changes:
                cat.setGroups(new ArrayList<Group>());
                cat.setGroupIds(room.getGroupIds());
                for (Long groupId : room.getGroupIds()) {
                    Group g = this.em.find(Group.class, groupId);
                    cat.getGroups().add(g);
                    if( !g.getCategories().contains(cat)) {
                        g.getCategories().add(cat);
                        em.merge(g);
                    }
                }
                em.merge(oldRoom);
                em.merge(cat);
                em.getTransaction().commit();
                return new CrxResponse("OK", "AdHocLan room was modified successfully");
            } catch (Exception e) {
                logger.error("modify:" + e.getMessage());
                return new CrxResponse("ERROR", "AdHocLan room could not be modified.");
            }
        }
    }

    /**
     * Helper script to convert a Room into AdHocRoom
     *
     * @param room The Room object
     * @return The created AdHocRoom object
     */
    public AdHocRoom roomToAdHoc(Room room) {
        //This should work. But casting does not work.
        //AdHocRoom adHocRoom = (AdHocRoom) room;
        AdHocRoom adHocRoom = new AdHocRoom(room);
        Category cat = this.getAdHocCategoryOfRoom(room);
        if (cat != null) {
            //cat.setIds();
            adHocRoom.setStudentsOnly(cat.getStudentsOnly());
            adHocRoom.setGroupIds(cat.getGroupIds());
            adHocRoom.setUserIds(cat.getUserIds());
        }
        return adHocRoom;
    }
}
