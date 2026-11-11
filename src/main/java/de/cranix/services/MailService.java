/* (c) 2026 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.MailAttachment;
import de.cranix.dao.MailFolder;
import de.cranix.dao.MailMessage;
import de.cranix.dao.MailSendRequest;
import de.cranix.dao.Session;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import javax.persistence.EntityManager;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MailService extends Service {

    public MailService(Session session, EntityManager em) {
        super(session, em);
    }

    public String getMailServer() {
        String mailserver = this.getConfigValue("MAILSERVER");
        if (mailserver == null || mailserver.isEmpty()) {
            mailserver = "localhost";
        }
        return mailserver;
    }

    public String getMailDomain() {
        String domain = this.getConfigValue("MAIL_DOMAIN");
        if (domain == null || domain.isEmpty()) {
            domain = this.getConfigValue("DOMAIN");
        }
        return domain;
    }

    public String getUserName() {
        return this.session.getUser().getUid();
    }

    public String getPassword() {
        return this.session.getPassword();
    }

    public String getMailAddress() {
        String domain = this.getMailDomain();
        if (domain == null || domain.isEmpty()) {
            return this.getUserName();
        }
        return this.getUserName() + "@" + domain;
    }

    private int getIntConfig(String key, int defaultValue) {
        try {
            String value = this.getConfigValue(key);
            if (value != null && !value.isEmpty()) {
                return Integer.parseInt(value.trim());
            }
        } catch (Exception e) {
            logger.error("Can not read config " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    private boolean getBoolConfig(String key, boolean defaultValue) {
        String value = this.getConfigValue(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equals("1");
    }

    public int getImapPort() {
        return getIntConfig("IMAP_PORT", 993);
    }

    public int getSmtpPort() {
        return getIntConfig("SMTP_PORT", 465);
    }

    public String getImapSecurity() {
        String value = this.getConfigValue("IMAP_SECURITY");
        return (value == null || value.isEmpty()) ? "ssl" : value.toLowerCase();
    }

    public String getSmtpSecurity() {
        String value = this.getConfigValue("SMTP_SECURITY");
        return (value == null || value.isEmpty()) ? "ssl" : value.toLowerCase();
    }

    public boolean isTrustAll() {
        return getBoolConfig("MAIL_TRUST_ALL", true);
    }

    public Map<String, String> getMailConfig() {
        Map<String, String> config = new LinkedHashMap<String, String>();
        config.put("mailserver", getMailServer());
        config.put("mailaddress", getMailAddress());
        config.put("username", getUserName());
        config.put("imapPort", String.valueOf(getImapPort()));
        config.put("imapSecurity", getImapSecurity());
        config.put("smtpPort", String.valueOf(getSmtpPort()));
        config.put("smtpSecurity", getSmtpSecurity());
        config.put("trustAll", String.valueOf(isTrustAll()));
        return config;
    }

    private String imapProtocol() {
        return getImapSecurity().equals("ssl") ? "imaps" : "imap";
    }

    private String smtpProtocol() {
        return getSmtpSecurity().equals("ssl") ? "smtps" : "smtp";
    }

    private Properties imapProperties() {
        String proto = imapProtocol();
        Properties props = new Properties();
        props.put("mail.store.protocol", proto);
        props.put("mail." + proto + ".host", getMailServer());
        props.put("mail." + proto + ".port", String.valueOf(getImapPort()));
        props.put("mail." + proto + ".connectiontimeout", "10000");
        props.put("mail." + proto + ".timeout", "30000");
        if (isTrustAll()) {
            props.put("mail." + proto + ".ssl.trust", "*");
            props.put("mail." + proto + ".ssl.checkserveridentity", "false");
        }
        if (getImapSecurity().equals("starttls")) {
            props.put("mail.imap.starttls.enable", "true");
        }
        return props;
    }

    private Properties smtpProperties() {
        String proto = smtpProtocol();
        Properties props = new Properties();
        props.put("mail.transport.protocol", proto);
        props.put("mail." + proto + ".host", getMailServer());
        props.put("mail." + proto + ".port", String.valueOf(getSmtpPort()));
        props.put("mail." + proto + ".auth", "true");
        props.put("mail." + proto + ".connectiontimeout", "10000");
        props.put("mail." + proto + ".timeout", "30000");
        if (isTrustAll()) {
            props.put("mail." + proto + ".ssl.trust", "*");
            props.put("mail." + proto + ".ssl.checkserveridentity", "false");
        }
        if (getSmtpSecurity().equals("starttls")) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        return props;
    }

    private jakarta.mail.Session imapSession() {
        return jakarta.mail.Session.getInstance(imapProperties(), null);
    }

    private jakarta.mail.Session smtpSession() {
        return jakarta.mail.Session.getInstance(smtpProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUserName(), getPassword());
            }
        });
    }

    private Store openStore() throws Exception {
        jakarta.mail.Session mailSession = imapSession();
        Store store = mailSession.getStore(imapProtocol());
        store.connect(getMailServer(), getImapPort(), getUserName(), getPassword());
        return store;
    }

    private String mailErrorMessage(Throwable e, int port) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof AuthenticationFailedException) {
                return "Authentication at the mail server failed.";
            }
            if (cause instanceof UnknownHostException) {
                return "The mail server " + getMailServer() + " could not be found.";
            }
            if (cause instanceof SocketTimeoutException) {
                return "Timeout while contacting the mail server " + getMailServer() + ".";
            }
            if (cause instanceof ConnectException || cause instanceof NoRouteToHostException) {
                return "Cannot connect to the mail server " + getMailServer() + ":" + port + ".";
            }
            cause = cause.getCause();
        }
        String message = e.getMessage();
        return (message == null || message.isEmpty()) ? e.toString() : message;
    }

    public static boolean isConnectionError(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConnectException || cause instanceof NoRouteToHostException
                    || cause instanceof UnknownHostException || cause instanceof SocketTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private CrxResponse mailError(Exception e) {
        return new CrxResponse("ERROR", mailErrorMessage(e, getImapPort()));
    }

    private String folderType(String name) {
        String n = name.toLowerCase();
        if (n.equals("inbox")) {
            return "inbox";
        }
        if (n.contains("sent") || n.contains("gesendet")) {
            return "sent";
        }
        if (n.contains("draft") || n.contains("entwurf")) {
            return "drafts";
        }
        if (n.contains("trash") || n.contains("papierkorb") || n.contains("deleted")) {
            return "trash";
        }
        if (n.contains("junk") || n.contains("spam")) {
            return "junk";
        }
        return "folder";
    }

    public List<MailFolder> getFolders() throws Exception {
        List<MailFolder> folders = new ArrayList<MailFolder>();
        Store store = null;
        try {
            store = openStore();
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] folderList = defaultFolder.list("*");
            for (Folder folder : folderList) {
                if ((folder.getType() & Folder.HOLDS_MESSAGES) == 0) {
                    continue;
                }
                int messageCount = 0;
                int unreadCount = 0;
                try {
                    if (folder.isOpen()) {
                        folder.close(false);
                    }
                    folder.open(Folder.READ_ONLY);
                    messageCount = folder.getMessageCount();
                    unreadCount = folder.getUnreadMessageCount();
                    folder.close(false);
                } catch (Exception e) {
                    logger.error("Can not read folder " + folder.getFullName() + ": " + e.getMessage());
                }
                folders.add(new MailFolder(
                        folder.getName(),
                        folder.getFullName(),
                        folderType(folder.getName()),
                        messageCount,
                        unreadCount));
            }
        } catch (Exception e) {
            throw new Exception(mailErrorMessage(e, getImapPort()), e);
        } finally {
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
        return folders;
    }

    private String addressesToString(jakarta.mail.Address[] addresses) {
        StringBuilder sb = new StringBuilder();
        if (addresses == null) {
            return "";
        }
        for (jakarta.mail.Address address : addresses) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            if (address instanceof InternetAddress) {
                sb.append(((InternetAddress) address).toUnicodeString());
            } else {
                sb.append(address.toString());
            }
        }
        return sb.toString();
    }

    private List<String> addressesToList(jakarta.mail.Address[] addresses) {
        List<String> result = new ArrayList<String>();
        if (addresses == null) {
            return result;
        }
        for (jakarta.mail.Address address : addresses) {
            if (address instanceof InternetAddress) {
                result.add(((InternetAddress) address).toUnicodeString());
            } else {
                result.add(address.toString());
            }
        }
        return result;
    }

    private long getUid(Message message) {
        if (message instanceof jakarta.mail.internet.MimeMessage) {
            try {
                UIDFolder uidFolder = (UIDFolder) message.getFolder();
                return uidFolder.getUID(message);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public List<MailMessage> getMessages(String folderName, int limit, int offset) throws Exception {
        List<MailMessage> messages = new ArrayList<MailMessage>();
        Store store = null;
        Folder folder = null;
        try {
            store = openStore();
            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                throw new Exception("Folder not found: " + folderName);
            }
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessageCount();
            int end = count - offset;
            int start = end - limit + 1;
            if (end < 1) {
                return messages;
            }
            if (start < 1) {
                start = 1;
            }
            Message[] msgs = folder.getMessages(start, end);
            for (int i = msgs.length - 1; i >= 0; i--) {
                messages.add(toSummary(folderName, msgs[i]));
            }
        } catch (Exception e) {
            throw new Exception(mailErrorMessage(e, getImapPort()), e);
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
        return messages;
    }

    private MailMessage toSummary(String folderName, Message message) {
        MailMessage mail = new MailMessage();
        mail.setFolder(folderName);
        mail.setUid(getUid(message));
        try {
            mail.setFrom(addressesToString(message.getFrom()));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            mail.setSubject(message.getSubject() == null ? "" : message.getSubject());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            Date date = message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate();
            mail.setDate(date);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            mail.setSeen(message.isSet(Flags.Flag.SEEN));
            mail.setFlagged(message.isSet(Flags.Flag.FLAGGED));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            mail.setSize(message.getSize());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try {
            String contentType = message.getContentType();
            mail.setHasAttachments(contentType != null && contentType.toLowerCase().startsWith("multipart/mixed"));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return mail;
    }

    public MailMessage getMessage(String folderName, long uid) throws Exception {
        Store store = null;
        Folder folder = null;
        try {
            store = openStore();
            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                throw new Exception("Folder not found: " + folderName);
            }
            folder.open(Folder.READ_ONLY);
            Message message = getMessageByUid(folder, uid);
            if (message == null) {
                throw new Exception("Message not found.");
            }
            MailMessage mail = new MailMessage();
            mail.setFolder(folderName);
            mail.setUid(uid);
            try {
                mail.setMessageId(message.getHeader("Message-ID") == null ? "" : message.getHeader("Message-ID")[0]);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            mail.setFrom(addressesToString(message.getFrom()));
            mail.setTo(addressesToList(message.getRecipients(Message.RecipientType.TO)));
            mail.setCc(addressesToList(message.getRecipients(Message.RecipientType.CC)));
            mail.setSubject(message.getSubject() == null ? "" : message.getSubject());
            mail.setDate(message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate());
            mail.setSeen(message.isSet(Flags.Flag.SEEN));
            mail.setFlagged(message.isSet(Flags.Flag.FLAGGED));
            mail.setSize(message.getSize());
            parseMessage(message, mail);
            mail.setHasAttachments(!mail.getAttachments().isEmpty());
            return mail;
        } catch (Exception e) {
            throw new Exception(mailErrorMessage(e, getImapPort()), e);
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }

    private Message getMessageByUid(Folder folder, long uid) throws Exception {
        if (folder instanceof UIDFolder) {
            return ((UIDFolder) folder).getMessageByUID(uid);
        }
        int count = folder.getMessageCount();
        if (uid >= 1 && uid <= count) {
            return folder.getMessage((int) uid);
        }
        return null;
    }

    private boolean isAttachment(Part part) throws Exception {
        String disposition = part.getDisposition();
        String fileName = part.getFileName();
        if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
            return true;
        }
        return fileName != null && !fileName.isEmpty();
    }

    private void parseMessage(Part part, MailMessage mail) throws Exception {
        if (isAttachment(part)) {
            mail.getAttachments().add(new MailAttachment(
                    mail.getAttachments().size(),
                    part.getFileName() == null ? "" : part.getFileName(),
                    part.getContentType() == null ? "" : part.getContentType(),
                    part.getSize()));
            return;
        }
        if (part.isMimeType("text/plain")) {
            Object content = part.getContent();
            if (content instanceof String && mail.getTextBody().isEmpty()) {
                mail.setTextBody((String) content);
            }
            return;
        }
        if (part.isMimeType("text/html")) {
            Object content = part.getContent();
            if (content instanceof String && mail.getHtmlBody().isEmpty()) {
                mail.setHtmlBody((String) content);
            }
            return;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                parseMessage(multipart.getBodyPart(i), mail);
            }
            return;
        }
        if (part.isMimeType("message/rfc822")) {
            Object content = part.getContent();
            if (content instanceof Part) {
                parseMessage((Part) content, mail);
            }
        }
    }

    private void collectAttachmentParts(Part part, List<Part> attachments) throws Exception {
        if (isAttachment(part)) {
            attachments.add(part);
            return;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                collectAttachmentParts(multipart.getBodyPart(i), attachments);
            }
            return;
        }
        if (part.isMimeType("message/rfc822")) {
            Object content = part.getContent();
            if (content instanceof Part) {
                collectAttachmentParts((Part) content, attachments);
            }
        }
    }

    public byte[] getAttachment(String folderName, long uid, int partIndex) throws Exception {
        Store store = null;
        Folder folder = null;
        try {
            store = openStore();
            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                throw new Exception("Folder not found: " + folderName);
            }
            folder.open(Folder.READ_ONLY);
            Message message = getMessageByUid(folder, uid);
            if (message == null) {
                throw new Exception("Message not found.");
            }
            List<Part> attachments = new ArrayList<Part>();
            collectAttachmentParts(message, attachments);
            if (partIndex < 0 || partIndex >= attachments.size()) {
                throw new Exception("Attachment not found.");
            }
            InputStream is = attachments.get(partIndex).getInputStream();
            return is.readAllBytes();
        } catch (Exception e) {
            throw new Exception(mailErrorMessage(e, getImapPort()), e);
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }

    public CrxResponse sendMail(MailSendRequest request, Map<String, byte[]> attachments) {
        try {
            if (request.getTo() == null || request.getTo().trim().isEmpty()) {
                return new CrxResponse("ERROR", "No recipient was given.");
            }
            jakarta.mail.Session mailSession = smtpSession();
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(getMailAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo()));
            if (request.getCc() != null && !request.getCc().trim().isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(request.getCc()));
            }
            if (request.getBcc() != null && !request.getBcc().trim().isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(request.getBcc()));
            }
            message.setSubject(request.getSubject() == null ? "" : request.getSubject(), "UTF-8");
            message.setSentDate(new Date());
            if (attachments == null || attachments.isEmpty()) {
                message.setText(request.getBody() == null ? "" : request.getBody(), "UTF-8", "html");
            } else {
                MimeMultipart multipart = new MimeMultipart();
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(request.getBody() == null ? "" : request.getBody(), "UTF-8", "html");
                multipart.addBodyPart(textPart);
                for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(entry.getValue(), "application/octet-stream");
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(entry.getKey());
                    multipart.addBodyPart(attachmentPart);
                }
                message.setContent(multipart);
            }
            String proto = smtpProtocol();
            Transport transport = mailSession.getTransport(proto);
            transport.connect(getMailServer(), getSmtpPort(), getUserName(), getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            appendToSent(message);
            return new CrxResponse("OK", "The mail was sent successfully.");
        } catch (Exception e) {
            logger.warn("sendMail: " + mailErrorMessage(e, getSmtpPort()));
            logger.debug("sendMail", e);
            return new CrxResponse("ERROR", "The mail could not be sent: " + mailErrorMessage(e, getSmtpPort()));
        }
    }

    private void appendToSent(MimeMessage message) {
        Store store = null;
        Folder sent = null;
        try {
            store = openStore();
            sent = store.getFolder("Sent");
            if (sent == null || !sent.exists()) {
                return;
            }
            sent.open(Folder.READ_WRITE);
            sent.appendMessages(new Message[]{message});
        } catch (Exception e) {
            logger.error("appendToSent: " + e.getMessage());
        } finally {
            try {
                if (sent != null && sent.isOpen()) {
                    sent.close(false);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public CrxResponse deleteMessage(String folderName, long uid) {
        return setFlag(folderName, uid, Flags.Flag.DELETED, true, "The message was deleted successfully.");
    }

    public CrxResponse setSeen(String folderName, long uid, boolean seen) {
        return setFlag(folderName, uid, Flags.Flag.SEEN, seen, "The message was modified successfully.");
    }

    private CrxResponse setFlag(String folderName, long uid, Flags.Flag flag, boolean value, String successMessage) {
        Store store = null;
        Folder folder = null;
        try {
            store = openStore();
            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                return new CrxResponse("ERROR", "Folder not found: " + folderName);
            }
            folder.open(Folder.READ_WRITE);
            Message message = getMessageByUid(folder, uid);
            if (message == null) {
                return new CrxResponse("ERROR", "Message not found.");
            }
            message.setFlag(flag, value);
            return new CrxResponse("OK", successMessage);
        } catch (Exception e) {
            logger.warn("setFlag: " + mailErrorMessage(e, getImapPort()));
            logger.debug("setFlag", e);
            return mailError(e);
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void closeStore(Store store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public CrxResponse createFolder(String folderName, String parentName) {
        Store store = null;
        try {
            if (folderName == null || folderName.trim().isEmpty()) {
                return new CrxResponse("ERROR", "No folder name was given.");
            }
            store = openStore();
            String name = folderName.trim();
            Folder parent = store.getDefaultFolder();
            if (parentName != null && !parentName.trim().isEmpty()) {
                Folder candidate = store.getFolder(parentName.trim());
                if (candidate == null || !candidate.exists()) {
                    return new CrxResponse("ERROR", "Folder not found: " + parentName);
                }
                parent = candidate;
            } else if (name.contains("/")) {
                String path = name.substring(0, name.lastIndexOf('/'));
                Folder candidate = store.getFolder(path);
                if (candidate != null && candidate.exists()) {
                    parent = candidate;
                }
                name = name.substring(name.lastIndexOf('/') + 1);
            }
            Folder target = parent.getFolder(name);
            if (target.exists()) {
                return new CrxResponse("ERROR", "The folder already exists: " + folderName);
            }
            if (!target.create(Folder.HOLDS_MESSAGES)) {
                return new CrxResponse("ERROR", "The folder could not be created: " + folderName);
            }
            return new CrxResponse("OK", "The folder was created successfully.");
        } catch (Exception e) {
            logger.warn("createFolder: " + mailErrorMessage(e, getImapPort()));
            logger.debug("createFolder", e);
            return mailError(e);
        } finally {
            closeStore(store);
        }
    }

    public CrxResponse deleteFolder(String folderName) {
        Store store = null;
        try {
            if (folderName == null || folderName.trim().isEmpty()) {
                return new CrxResponse("ERROR", "No folder name was given.");
            }
            store = openStore();
            Folder folder = store.getFolder(folderName.trim());
            if (folder == null || !folder.exists()) {
                return new CrxResponse("ERROR", "Folder not found: " + folderName);
            }
            if (!folder.delete(false)) {
                return new CrxResponse("ERROR", "The folder could not be deleted: " + folderName);
            }
            return new CrxResponse("OK", "The folder was deleted successfully.");
        } catch (Exception e) {
            logger.warn("deleteFolder: " + mailErrorMessage(e, getImapPort()));
            logger.debug("deleteFolder", e);
            return mailError(e);
        } finally {
            closeStore(store);
        }
    }
}
