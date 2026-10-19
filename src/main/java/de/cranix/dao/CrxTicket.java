package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="CrxTickets")
@NamedQueries({
        @NamedQuery(name="CrxTicket.findAll", query="SELECT t FROM CrxTicket t")
})
public class CrxTicket extends AbstractEntity {

    @ManyToOne(optional = true)
    @JoinColumn(
            name="assignee_id",
            columnDefinition ="BIGINT UNSIGNED"
    )
    User assignee;

    @Transient
    Long assigneeId;

    @Column(name="title", length = 255)
    private String title = "";

    /**
     * Status of a ticket. This can be:
     * N new
     * D done
     * R response arrived
     * W waiting for response
     */
    @Column(name = "ticketStatus", length = 1)
    private String ticketStatus = "N";

    @OneToMany( mappedBy = "crxTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CrxTicketArticle> crxTicketArticleList = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="RoomToTicket",
            joinColumns={@JoinColumn(name="crxticket_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns={@JoinColumn(name="room_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
    )
    List<Room> rooms = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="DeviceToTicket",
            joinColumns={@JoinColumn(name="crxticket_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns={@JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
    )
    List<Device> devices = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="PrinterToTicket",
            joinColumns={@JoinColumn(name="crxticket_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns={@JoinColumn(name="printer_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
    )
    List<Printer> printer = new ArrayList<>();

    @Transient
    String text = "";

    @Transient
    String attachment = "";

    @Transient
    String attachmentName = "";

    public CrxTicket() {
        super();
    }

    public CrxTicket(Session session){
        super(session);
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Long getAssigneeId() {
        if(this.assigneeId == null) {
            return this.assignee != null ? this.assignee.getId() : null;
        }
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public List<CrxTicketArticle> getCrxTicketArticleList() {
        return crxTicketArticleList;
    }

    public void addArticle(CrxTicketArticle article){
        if(!this.crxTicketArticleList.contains(article)){
            this.crxTicketArticleList.add(article);
            article.setCrxTicket(this);
        }
    }

    public void deleteArticle(CrxTicketArticle article){
        if(this.crxTicketArticleList.contains(article)){
            this.crxTicketArticleList.remove(article);
            article.setCrxTicket(null);
        }
    }

    public void setCrxTicketArticleList(List<CrxTicketArticle> crxTicketArticleList) {
        this.crxTicketArticleList = crxTicketArticleList;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Printer> getPrinter() {
        return printer;
    }

    public void setPrinter(List<Printer> printer) {
        this.printer = printer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}