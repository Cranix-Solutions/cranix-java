/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao.controller;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.openschoolserver.dao.Announcement;
import de.openschoolserver.dao.Category;
import de.openschoolserver.dao.Contact;
import de.openschoolserver.dao.FAQ;
import de.openschoolserver.dao.Group;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.User;
/**
 * @author varkoly
 *
 */
public class InformationController extends Controller {

	/**
	 * @param session
	 */
	public InformationController(Session session) {
		super(session);
	}

	/**
	 * Creates a new announcement
	 * @param announcement
	 * @return The result in form of OssResponse
	 * @see Announcement
	 */
	public OssResponse addAnnouncement(Announcement announcement) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Announcement> violation : factory.getValidator().validate(announcement) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		EntityManager em = getEntityManager();
		announcement.setOwner(user);
		announcement.setCategories( new ArrayList<Category>() );
		Category category;
		try {
			em.getTransaction().begin();
			em.persist(announcement);
			user.getMyAnnouncements().add(announcement);
			em.merge(user);
			for( Long categoryId : announcement.getCategoryIds() ) {
				try {
					category = em.find(Category.class, categoryId);
					category.getAnnouncements().add(announcement);
					announcement.getCategories().add(category);
					em.merge(category);
					em.merge(announcement);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			em.getTransaction().commit();
			logger.debug("Created Announcement:" + announcement);
			return new OssResponse(this.getSession(),"OK", "Announcement was created succesfully.",announcement.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Creates a new contact
	 * @param contact 
	 * @return The result in form of OssResponse
	 * @see Contact
	 */
	public OssResponse addContact(Contact contact) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Contact> violation : factory.getValidator().validate(contact) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		EntityManager em = getEntityManager();
		contact.setOwner(user);
		Category category;
		try {
			em.getTransaction().begin();
			em.persist(contact);
			user.getMyContacts().add(contact);
			em.merge(user);
			for( Long categoryId : contact.getCategoryIds() ) {
				try {
					category = em.find(Category.class, categoryId);
					category.getContacts().add(contact);
					contact.getCategories().add(category);
					em.merge(category);
					em.merge(contact);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			em.getTransaction().commit();
			logger.debug("Created Contact:" + contact);
			return new OssResponse(this.getSession(),"OK", "Contact was created succesfully.",contact.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Creates a new FAQ
	 * @param faq 
	 * @return The result in form of OssResponse
	 * @see FAQ
	 */
	public OssResponse addFAQ(FAQ faq) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<FAQ> violation : factory.getValidator().validate(faq) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		EntityManager em = getEntityManager();
		faq.setOwner(user);
		Category category;
		try {
			em.getTransaction().begin();
			em.persist(faq);
			user.getMyFAQs().add(faq);
			em.merge(user);
			for( Long categoryId : faq.getCategoryIds() ) {
				try {
					category = em.find(Category.class, categoryId);
					category.getFaqs().add(faq);
					faq.getCategories().add(category);
					em.merge(category);
					em.merge(faq);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			em.getTransaction().commit();
			logger.debug("Created FAQ:" + faq);
			return new OssResponse(this.getSession(),"OK", "FAQ was created succesfully.",faq.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Delivers the valid announcements corresponding to the session user.
	 * @return
	 * @see Announcement
	 */
	public List<Announcement> getAnnouncements() {
		List<Announcement> announcements = new ArrayList<Announcement>();
		User user;
		EntityManager em = getEntityManager();
		try {
			user = em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			em.close();
			return null;
		} finally {
			em.close();
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Announcement announcement : category.getAnnouncements() ) {
					if( announcement.getValidFrom().before(this.now()) &&
					    announcement.getValidUntil().after(this.now())
					)
					{
						announcement.setCategories(categories);
						announcement.setText("");
						announcements.add(announcement);
					}
				}
			}
		}
		
		return announcements;
	}

	public List<Announcement> getNewAnnouncements() {
		List<Announcement> announcements = new ArrayList<Announcement>();
		User user;
		EntityManager em = getEntityManager();
		try {
			user = em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			em.close();
			return null;
		} finally {
			em.close();
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Announcement announcement : category.getAnnouncements() ) {
					if( announcement.getValidFrom().before(this.now()) &&
						announcement.getValidUntil().after(this.now()) &&
						! user.getReadAnnouncements().contains(announcement) )
					{
						announcement.setCategories(categories);
						announcements.add(announcement);
					}
				}
			}
		}
		return announcements;
	}

	public OssResponse setAnnouncementHaveSeen(Long announcementId) {
		EntityManager em = getEntityManager();
		try {
			Announcement announcement = em.find(Announcement.class, announcementId);
			User user = this.session.getUser();
			announcement.getHaveSeenUsers().add(user);
			user.getReadAnnouncements().add(announcement);
			em.getTransaction().begin();
			em.merge(user);
			em.merge(announcement);
			em.getTransaction().commit();
		}catch (Exception e) {
			logger.error("setAnnouncementHaveSeen:" + this.getSession().getUserId() + " " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR","Annoncement could not be set as seen.");
		} finally {
			em.close();
		}
		return new OssResponse(this.getSession(),"OK","Annoncement was set as seen.");
	}

	/**
	 * Delivers the list of FAQs corresponding to the session user.
	 * @return
	 */
	public List<FAQ> getFAQs() {
		List<FAQ> faqs = new ArrayList<FAQ>();
		User user;
		EntityManager em = getEntityManager();
		try {
			user = em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			em.close();
			return null;
		} finally {
			em.close();
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(FAQ faq : category.getFaqs() ) {
					faq.setCategories(categories);
					faq.setText("");
					faqs.add(faq);
				}
			}
		}
		return faqs;
	}


	/**
	 * Delivers the list of contacts corresponding to the session user
	 * @return
	 */
	public List<Contact> getContacts() {
		List<Contact> contacts = new ArrayList<Contact>();
		User user;
		EntityManager em = getEntityManager();
		try {
			user = em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			em.close();
			return null;
		} finally {
			em.close();
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Contact contact : category.getContacts() ) {
					contact.setCategories(categories);
					contacts.add(contact);
				}
			}
		}
		return contacts;
	}

	public Announcement getAnnouncementById(Long AnnouncementId) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Announcement.class, AnnouncementId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
			em.close();
		}
	}

	public Contact getContactById(Long ContactId) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Contact.class, ContactId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
			em.close();
		}
	}

	public FAQ getFAQById(Long FAQId) {
		EntityManager em = getEntityManager();
		try {
			return em.find(FAQ.class, FAQId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
			em.close();
		}
	}

	public OssResponse modifyAnnouncement(Announcement announcement) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Announcement> violation : factory.getValidator().validate(announcement) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		EntityManager em = getEntityManager();
		if( !this.mayModify(announcement) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to modify this Announcement");
		}
		try {
			em.getTransaction().begin();
			Announcement oldAnnouncement = em.find(Announcement.class, announcement.getId());
			for( User user : oldAnnouncement.getHaveSeenUsers() ) {
				user.getReadAnnouncements().remove(oldAnnouncement);
				em.merge(user);
				//TODO check if there are relay changes.
			}
			oldAnnouncement.setIssue(announcement.getIssue());
			oldAnnouncement.setKeywords(announcement.getKeywords());
			oldAnnouncement.setText(announcement.getText());
			oldAnnouncement.setValidFrom(announcement.getValidFrom());
			oldAnnouncement.setValidUntil(announcement.getValidUntil());
			oldAnnouncement.setHaveSeenUsers(new ArrayList<User>());
			em.merge(announcement);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "Announcement was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	public OssResponse modifyContact(Contact contact) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Contact> violation : factory.getValidator().validate(contact) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		EntityManager em = getEntityManager();
		if( !this.mayModify(contact) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to modify this contact");
		}
		try {
			em.getTransaction().begin();
			Contact oldContact = em.find(Contact.class, contact.getId());
			oldContact.setName(contact.getName());
			oldContact.setEmail(contact.getEmail());
			oldContact.setPhone(contact.getPhone());
			oldContact.setTitle(contact.getTitle());
			oldContact.setIssue(contact.getIssue());
			em.merge(oldContact);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "Contact was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	public OssResponse modifyFAQ(FAQ faq) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<FAQ> violation : factory.getValidator().validate(faq) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new OssResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		EntityManager em = getEntityManager();
		if( !this.mayModify(faq) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to modify this FAQ ");
		}
		try {
			em.getTransaction().begin();
			FAQ oldFaq = em.find(FAQ.class, faq.getId());
			oldFaq.setIssue(faq.getIssue());
			oldFaq.setTitle(faq.getTitle());
			oldFaq.setText(faq.getText());
			em.merge(oldFaq);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "FAQ was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Remove a announcement.
	 * @param faqId The technical id of the announcement.
	 * @return The result in form of OssResponse
	 */
	public OssResponse deleteAnnouncement(Long announcementId) {
		EntityManager em = getEntityManager();
		Announcement announcement;
		try {
			announcement = em.find(Announcement.class, announcementId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(announcement) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to delete this Announcement");
		}
		try {
			em.getTransaction().begin();
			em.merge(announcement);
			for( Category category : announcement.getCategories() ) {
				category.getAnnouncements().remove(announcement);
				em.merge(category);
			}
			em.remove(announcement);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "Announcement was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Remove a contact.
	 * @param faqId The technical id of the contact.
	 * @return The result in form of OssResponse
	 */
	public OssResponse deleteContact(Long contactId) {
		EntityManager em = getEntityManager();
		Contact contact;
		try {
			contact = em.find(Contact.class, contactId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(contact) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to delete this contact");
		}
		try {
			em.getTransaction().begin();
			em.merge(contact);
			for( Category category : contact.getCategories() ) {
				category.getContacts().remove(contact);
				em.merge(category);
			}
			em.remove(contact);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "Contact was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}

	/**
	 * Remove a FAQ.
	 * @param faqId The technical id of the faq.
	 * @return The result in form of OssResponse
	 */
	public OssResponse deleteFAQ(Long faqId) {
		EntityManager em = getEntityManager();
		FAQ faq;
		try {
			faq = em.find(FAQ.class, faqId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(faq) )
		{
			return new OssResponse(this.getSession(),"ERROR", "You have no rights to delete this FAQ");
		}
		try {
			em.getTransaction().begin();
			em.merge(faq);
			for( Category category : faq.getCategories() ) {
				category.getFaqs().remove(faq);
				em.merge(category);
			}
			em.remove(faq);
			em.getTransaction().commit();
			return new OssResponse(this.getSession(),"OK", "FAQ was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new OssResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
	}


	/**
	 * Get the list of information categories, the user have access on it.
	 * These are the own categories and the public categories by type informations.
	 * @return The list of the found categories.
	 */
	public List<Category> getInfoCategories() {
		CategoryController categoryController = new CategoryController(this.session);
		boolean isSuperuser = this.isSuperuser();
		List<Category> categories = new ArrayList<Category>();
		for(Category category : this.session.getUser().getOwnedCategories() ) {
			categories.add(category);
		}
		for(Category category : categoryController.getByType("informations") ) {
			if(isSuperuser ||  category.isPublicAccess() ) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		for(Category category : categoryController.getByType("smartRoom") ) {
			logger.debug("getInfoCategories smartRoom:" + category );
			if( isSuperuser ||  category.isPublicAccess() ) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		return categories;
	}

}
