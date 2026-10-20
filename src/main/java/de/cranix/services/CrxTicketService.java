package de.cranix.services;

import de.cranix.dao.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CrxTicketService extends Service {

    private boolean use = false;
    private boolean worker = false;
    private boolean manager = false;

    public CrxTicketService(Session session, EntityManager em) {
        super(session, em);
        this.use = this.isAllowed("crxticket.use");
        this.worker = this.isAllowed("crxticket.worker");
        this.manager = this.isAllowed("crxticket.manager");
    }

    private boolean isStaff() {
        return this.worker || this.manager;
    }

    /**
     * Checks if the session user is allowed to read the ticket.
     * Managers can see all tickets, workers the open tickets which are not
     * assigned or assigned to them, users only their own tickets.
     */
    public boolean canAccess(CrxTicket ticket) {
        if (ticket == null) {
            return false;
        }
        Long userId = this.session.getUserId();
        if (this.manager) {
            return true;
        }
        if (ticket.getCreatorId() != null && ticket.getCreatorId().equals(userId)) {
            return true;
        }
        if (this.worker) {
            return ticket.getAssignee() == null || ticket.getAssignee().getId().equals(userId);
        }
        return false;
    }

    /**
     * Checks if the session user is allowed to close the ticket.
     */
    public boolean canClose(CrxTicket ticket) {
        if (ticket == null) {
            return false;
        }
        if (this.worker || this.manager) {
            return true;
        }
        return ticket.getCreatorId() != null && ticket.getCreatorId().equals(this.session.getUserId());
    }

    /**
     * The modified time of a ticket must always reflect the creation time of
     * its last article.
     */
    private Date lastArticleTime(CrxTicket ticket) {
        Date last = ticket.getCreated();
        if (ticket.getCrxTicketArticleList() != null) {
            for (CrxTicketArticle article : ticket.getCrxTicketArticleList()) {
                if (article.getCreated() != null && (last == null || article.getCreated().after(last))) {
                    last = article.getCreated();
                }
            }
        }
        return last != null ? last : new Date();
    }

    private List<Device> resolveDevices(List<Device> devices) {
        List<Device> result = new ArrayList<>();
        if (devices != null) {
            for (Device device : devices) {
                if (device != null && device.getId() != null) {
                    Device managed = em.find(Device.class, device.getId());
                    if (managed != null) {
                        result.add(managed);
                    }
                }
            }
        }
        return result;
    }

    private List<Room> resolveRooms(List<Room> rooms) {
        List<Room> result = new ArrayList<>();
        if (rooms != null) {
            for (Room room : rooms) {
                if (room != null && room.getId() != null) {
                    Room managed = em.find(Room.class, room.getId());
                    if (managed != null) {
                        result.add(managed);
                    }
                }
            }
        }
        return result;
    }

    private List<Printer> resolvePrinters(List<Printer> printers) {
        List<Printer> result = new ArrayList<>();
        if (printers != null) {
            for (Printer printer : printers) {
                if (printer != null && printer.getId() != null) {
                    Printer managed = em.find(Printer.class, printer.getId());
                    if (managed != null) {
                        result.add(managed);
                    }
                }
            }
        }
        return result;
    }

    public CrxResponse add(CrxTicket ticket) {
        if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
            return new CrxResponse("ERROR", "The title of the ticket must not be empty.");
        }
        try {
            ticket.setDevices(resolveDevices(ticket.getDevices()));
            ticket.setRooms(resolveRooms(ticket.getRooms()));
            ticket.setPrinter(resolvePrinters(ticket.getPrinter()));
            em.getTransaction().begin();
            ticket.setCreator(this.session.getUser());
            ticket.setAssignee(null);
            ticket.setTicketStatus("N");
            CrxTicketArticle article = new CrxTicketArticle(this.session);
            article.setText(ticket.getText());
            article.setAttachment(ticket.getAttachment());
            article.setAttachmentName(ticket.getAttachmentName());
            ticket.addArticle(article);
            ticket.setModified(article.getCreated());
            em.persist(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Ticket was created", ticket.getId());
    }

    public CrxTicket getById(Long id) {
        return em.find(CrxTicket.class, id);
    }

    public CrxResponse close(CrxTicket ticket) {
        if (ticket == null) {
            return new CrxResponse("ERROR", "Ticket was not found.");
        }
        if (!canClose(ticket)) {
            return new CrxResponse("ERROR", "You are not allowed to close this ticket.");
        }
        try {
            Date last = lastArticleTime(ticket);
            em.getTransaction().begin();
            em.createQuery("UPDATE CrxTicket t SET t.ticketStatus = :status, t.modified = :modified WHERE t.id = :id")
                    .setParameter("status", "D")
                    .setParameter("modified", last)
                    .setParameter("id", ticket.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Ticket was closed successfully.");
    }

    public CrxResponse close(Long ticketId) {
        return this.close(this.getById(ticketId));
    }

    public CrxResponse addArticle(Long id, CrxTicketArticle article) {
        try {
            CrxTicket ticket = getById(id);
            if (ticket == null) {
                return new CrxResponse("ERROR", "Ticket was not found.");
            }
            if (!canAccess(ticket)) {
                return new CrxResponse("ERROR", "You are not allowed to answer this ticket");
            }
            boolean creator = ticket.getCreatorId() != null &&
                    ticket.getCreatorId().equals(this.session.getUserId());
            if (creator) {
                //Now a response is arrived from the creator
                if (!ticket.getTicketStatus().equals("N")) {
                    ticket.setTicketStatus("R");
                }
            } else if (isStaff()) {
                //Worker or manager answered a ticket
                ticket.setTicketStatus("W");
            } else {
                return new CrxResponse("ERROR", "You are not allowed to answer this ticket");
            }
            article.setCreator(this.session.getUser());
            article.setCreated(new Date());
            ticket.addArticle(article);
            ticket.setModified(article.getCreated());
            em.getTransaction().begin();
            em.merge(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Article was created successfully.");
    }

    public CrxResponse assignTicket(Long ticketId, Long userId) {
        if (!isStaff()) {
            return new CrxResponse("ERROR", "You are not allowed to assign tickets.");
        }
        try {
            CrxTicket ticket = getById(ticketId);
            if (ticket == null) {
                return new CrxResponse("ERROR", "Ticket was not found.");
            }
            User assignee = em.find(User.class, userId);
            if (assignee == null) {
                return new CrxResponse("ERROR", "User was not found.");
            }
            ticket.setAssignee(assignee);
            ticket.setAssigneeId(assignee.getId());
            Date last = lastArticleTime(ticket);
            em.getTransaction().begin();
            em.createQuery("UPDATE CrxTicket t SET t.assignee = :assignee, t.modified = :modified WHERE t.id = :id")
                    .setParameter("assignee", assignee)
                    .setParameter("modified", last)
                    .setParameter("id", ticket.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Now you are the assignee");
    }

    /**
     * Returns the tickets created by the session user.
     */
    public List<CrxTicket> getMyTickets(String status) {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        List<CrxTicket> tickets = new ArrayList<>();
        Long creatorId = this.session.getUserId();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (creatorId.equals(ticket.getCreatorId()) && status.contains(ticket.getTicketStatus())) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    /**
     * Returns the tickets the session user can access. This is used by the
     * management list of the workers and managers.
     */
    public List<CrxTicket> getAllTickets(String status) {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        List<CrxTicket> tickets = new ArrayList<>();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (status.contains(ticket.getTicketStatus()) && canAccess(ticket)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public List<CrxTicket> getTicketsForMe(String status) {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        List<CrxTicket> tickets = new ArrayList<>();
        Long assigneeId = this.session.getUserId();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (!status.contains(ticket.getTicketStatus())) {
                continue;
            }
            if (this.manager) {
                tickets.add(ticket);
            } else if (ticket.getAssignee() == null || ticket.getAssignee().getId().equals(assigneeId)) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public CrxResponse delete(Long id) {
        if (!this.manager) {
            return new CrxResponse("ERROR", "You are not allowed to delete tickets.");
        }
        try {
            CrxTicket ticket = getById(id);
            if (ticket == null) {
                return new CrxResponse("ERROR", "Ticket was not found.");
            }
            em.getTransaction().begin();
            em.remove(ticket);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Ticket was deleted successfully");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public List<CrxResponse> applyAction(CrxActionMap actionMap) {
        List<CrxResponse> responses = new ArrayList<>();
        if (actionMap == null || actionMap.getName() == null || actionMap.getObjectIds() == null) {
            responses.add(new CrxResponse("ERROR", "Invalid action."));
            return responses;
        }
        switch (actionMap.getName().toLowerCase()) {
            case "delete": {
                if (!this.manager) {
                    responses.add(new CrxResponse("ERROR", "You are not allowed to delete tickets."));
                    return responses;
                }
                for (Long id : actionMap.getObjectIds()) {
                    responses.add(delete(id));
                }
                break;
            }
            case "close": {
                for (Long id : actionMap.getObjectIds()) {
                    responses.add(close(id));
                }
                break;
            }
            default: {
                responses.add(new CrxResponse("ERROR", "Unknown action: " + actionMap.getName()));
            }
        }
        return responses;
    }

    public Object getStatus() {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        HashMap<String, Integer> result = new HashMap<>();
        result.put("N", 0);
        result.put("R", 0);
        result.put("W", 0);
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            String status = ticket.getTicketStatus();
            if (status.equals("D")) {
                continue;
            }
            if (canAccess(ticket)) {
                result.put(status, result.get(status) + 1);
            }
        }
        return result;
    }
}
