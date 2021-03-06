/*
 * Created on Aug 1, 2003
 *
 */
package com.idega.user.presentation;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.event.IWPageEventListener;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.TextSoap;
/**
 * UserSearcher small adjustable search block, used to search for users in database.
 * 
 * @author aron 
 * @version 1.0
 */
public class UserSearcher extends Block implements IWPageEventListener {
	private static final String SEARCH_PERSONAL_ID = "usrch_search_pid";
	private static final String SEARCH_LAST_NAME = "usrch_search_lname";
	private static final String SEARCH_MIDDLE_NAME = "usrch_search_mname";
	private static final String SEARCH_FIRST_NAME = "usrch_search_fname";
	public static final String SEARCH_COMMITTED = "mbe_act_search";
	public static final String SEARCH_CLEARED = "mbe_act_clear";
	public static final String NEW_USER = "usrch_new_user";
	public final static String STYLENAME_TEXT = "Text";
	public final static String STYLENAME_HEADER = "Header";
	public final static String STYLENAME_BUTTON = "Button";
	public final static String STYLENAME_WARNING = "Warning";
	public final static String STYLENAME_INTERFACE = "Interface";
	public final static String BUTTON_PRESSED = "usrch_search_button_pressed";
	public final static int SEARCH_PRESSED = 1;
	public final static int NEW_PRESSED = 2;
	private String textFontStyleName = null;
	private String headerFontStyleName = null;
	private String buttonStyleName = null;
	private String warningStyleName = null;
	private String interfaceStyleName = null;
	private String textFontStyle = "font-weight:plain;";
	private String headerFontStyle = "font-weight:bold;";
	private String warningFontStyle = "font-weight:bold;font-color:#FF0000";
	private String buttonStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	private String interfaceStyle =
		"color:#000000;font-size:10px;font-family:Verdana,Arial,Helvetica,sans-serif;font-weight:normal;border-width:1px;border-style:solid;border-color:#000000;";
	/** Parameter for user id */
	private static final String PRM_USER_ID = "usrch_user_id_";
	/** The userID is the handled users ID. */
	private Integer userID = null;
	/** The user currently handled */
	private User user = null;
	/** A Collection of users complying to search */
	private Collection usersFound = null;
	/** flag telling if we have more than one user */
	private boolean hasManyUsers = false;
	/** Determines if we should allow search by users first name*/
	private boolean showFirstNameInSearch = true;
	/** Determines if we should allow search by users middle name*/
	private boolean showMiddleNameInSearch = true;
	/** Determines if we should allow search by users last name*/
	private boolean showLastNameInSearch = true;
	/** Determines if we should allow search by users personal ID*/
	private boolean showPersonalIDInSearch = true;
	/** Maximum search result rows */
	private int maxFoundUserRows = 20;
	/** Maximum search result columns */
	private int maxFoundUserCols = 3;
	/** The dynamic bundle identifier*/
	private String bundleIdentifer = null;
	/** The  static bundle identifier used in this package */
	private static String BUNDLE_IDENTIFIER = "is.idega.idegaweb.member";
	/** The resource bundle */
	private IWResourceBundle iwrb;
	/** flag for process method */
	private boolean processed = false;
	/** list of maintainparameters */
	private List maintainedParameters = new Vector();
	/** personalID input length */
	private int personalIDLength = 10;
	/** firstname input length */
	private int firstNameLength = 10;
	/** middlename input length */
	private int middleNameLength = 10;
	/**lastname input length */
	private int lastNameLength = 10;
	/** stacked view flag : if stacked heading appears above inputs*/
	private boolean stacked = true;
	/** First letter in names case insensitive*/
	private boolean firstLetterCaseInsensitive = true;
	/** Skip results if only one found */
	private boolean skipResultsForOneFound = true;
	/** Contained in own form */
	private boolean OwnFormContainer = true;
	/** Flag for showing reset button */
	private boolean showResetButton = true;
	private boolean showMultipleResetButton = false;
	/** Flag for hiding buttons */
	private boolean showButtons = true;
	
	private boolean showNewUserButton = true;
	
	/** Flag for forgiving ssn search */
	private boolean useFlexiblePersonalID = true;
	/** unique identifier */
	private String uniqueIdentifier = "unique";
	/** flag for showing result overflow */
	private boolean showOverFlowMessage = true;
	/**Collection of objects for the button area */
	private Collection addedButtons = null;
	private Collection otherClearIdentifiers = null;
	private boolean constrainToUniqueSearch = true;
	
	private Collection monitoredSearchIdentifiers =  null;
	private Map monitorMap = null;
	
	/** flag for making links do form submit */
	private boolean setToFormSubmit = false;
	
	private String legalNonDigitPIDLetters = null;
	
	private Integer userInfoPage;
	
	public void setUserInfoPage(Integer userInfoPage) {
		this.userInfoPage = userInfoPage;
	}
	
	public Integer getUserInfoPage() {
		return userInfoPage;
	}
	
	protected UserSession getUserSession(IWUserContext iwuc) {
		try {
			return (UserSession) IBOLookup.getSessionInstance(iwuc, UserSession.class);
		}
		catch (IBOLookupException e) {
			throw new IBORuntimeException(e);
		}
	}

	private void initStyleNames() {
		if (this.textFontStyleName == null) {
			this.textFontStyleName = getStyleName(STYLENAME_TEXT);
		}
		if (this.headerFontStyleName == null) {
			this.headerFontStyleName = getStyleName(STYLENAME_HEADER);
		}
		if (this.buttonStyleName == null) {
			this.buttonStyleName = getStyleName(STYLENAME_BUTTON);
		}
		if (this.warningStyleName == null) {
			this.warningStyleName = getStyleName(STYLENAME_WARNING);
		}
		if (this.interfaceStyleName == null) {
			this.interfaceStyleName = getStyleName(STYLENAME_INTERFACE);
		}
	}
	@Override
	public void main(IWContext iwc) throws Exception {
		//debugParameters(iwc);
		initStyleNames();
		this.iwrb = getResourceBundle(iwc);
		String message = null;
		try {
			process(iwc, false);
		}
		catch (RemoteException e) {
			e.printStackTrace();
			message = this.iwrb.getLocalizedString("usrch.service_available", "Search service not available");
		}
		catch (FinderException e) {
			//e.printStackTrace();
			message = this.iwrb.getLocalizedString("usrch.no_user_found", "No user found");
		}
		Table T = new Table();		
		T.add(presentateCurrentUserSearch(iwc), 1, 2);
		if (!this.skipResultsForOneFound || this.hasManyUsers) {
			T.add(presentateFoundUsers(iwc), 1, 3);
		}
		if (message != null) {
			Text tMessage = new Text(message);
			tMessage.setStyleAttribute("color:red");
			T.add(tMessage, 1, 1);
		}
		if (this.OwnFormContainer) {
			Form form = new Form();
			form.setEventListener(getListenerClass());
			if (this.showNewUserButton) {
				HiddenInput input = new HiddenInput("newStuff", "-1");
				form.add(input);
				input.setOnSubmitFunction("newUser", checkEmptyFieldScript());
			}
			form.add(T);
			add(form);
		}
		else {
			add(T);
		}
	}
	
	public void process(IWContext iwc) throws FinderException, RemoteException {
		process(iwc, false);
	}
	
	/**
	 * Main processing method, searches if search has ben committed, or looks up the user chosen
	 * is called by main(),
	 * @param iwc
	 */
	public void process(IWContext iwc, boolean save) throws FinderException, RemoteException {
		if (this.processed) {
			return;
		}
		String searchIdentifier = this.constrainToUniqueSearch ? this.uniqueIdentifier : "";
		
		if (iwc.isParameterSet(PRM_USER_ID + this.uniqueIdentifier)) {
			this.userID = Integer.valueOf((iwc.getParameter(PRM_USER_ID + this.uniqueIdentifier)).trim());
		}
		if (iwc.isParameterSet(SEARCH_COMMITTED + searchIdentifier)) {
			processSearch(iwc);
		}
		if(iwc.isParameterSet(NEW_USER) && save) {
			String first = iwc.getParameter(SEARCH_FIRST_NAME + this.uniqueIdentifier);
			String middle = iwc.getParameter(SEARCH_MIDDLE_NAME + this.uniqueIdentifier);
			String last = iwc.getParameter(SEARCH_LAST_NAME + this.uniqueIdentifier);
			String pid = iwc.getParameter(SEARCH_PERSONAL_ID + this.uniqueIdentifier);
			try {
				User user = getUserBusiness(iwc).getUser(pid);
				this.userID = (Integer) user.getPrimaryKey();
			}
			catch(FinderException fe) {
					try {
						if(first != null && last != null && pid != null && first.length() > 0 && last.length() > 0 && pid.length() > 0) {
							this.userID = processSave(iwc, first, middle, last, pid);//calles the extended method
						}
					}
					catch (CreateException ce) {
						add(ce.getMessage());
						add(new Break(2));
					}
				
			}
			
		}
		if (this.userID != null && this.userID.intValue()>0) {
			try {
				UserHome home = (UserHome) IDOLookup.getHome(User.class);
				this.user = home.findByPrimaryKey(this.userID);
				
				getUserSession(iwc).setUser(this.user);
			}
			catch (IDOLookupException e) {
				throw new FinderException(e.getMessage());
			}
		}
		digMonitors(iwc);
		this.processed = true;
	}
	//added by ac
	protected UserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (UserBusiness) IBOLookup.getServiceInstance(iwac, UserBusiness.class);
		}
		catch (IBOLookupException ible) {
			throw new IBORuntimeException(ible);
		}
	}
	
	//added by ac  
	protected Integer processSave(IWContext iwc, String firstName, String middleName, String lastName, String personalID) throws CreateException {
		UserBusiness business = getUserBusiness(iwc);
		try {
			User user = business.createUser(firstName, middleName, lastName, personalID);
			return (Integer) user.getPrimaryKey();
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}
	
	private void digMonitors(IWContext iwc){
		
		if(this.monitoredSearchIdentifiers!=null && !this.monitoredSearchIdentifiers.isEmpty()){
			this.monitorMap = new Hashtable();
			for (Iterator iter = this.monitoredSearchIdentifiers.iterator(); iter.hasNext();) {
				String identifier = (String) iter.next();
				boolean addSearchPrm = false;
				if(iwc.isParameterSet(SEARCH_FIRST_NAME+identifier)){
					this.monitorMap.put(SEARCH_FIRST_NAME+identifier,iwc.getParameter(SEARCH_FIRST_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_MIDDLE_NAME+identifier)){
					this.monitorMap.put(SEARCH_MIDDLE_NAME+identifier,iwc.getParameter(SEARCH_MIDDLE_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_LAST_NAME+identifier)){
					this.monitorMap.put(SEARCH_LAST_NAME+identifier,iwc.getParameter(SEARCH_LAST_NAME+identifier));
					addSearchPrm = true;
				}
				if(iwc.isParameterSet(SEARCH_PERSONAL_ID+identifier)){
					this.monitorMap.put(SEARCH_PERSONAL_ID+identifier,iwc.getParameter(SEARCH_PERSONAL_ID+identifier));
					addSearchPrm = true;
				}
				if(addSearchPrm){
					this.monitorMap.put(SEARCH_COMMITTED + (this.constrainToUniqueSearch ? identifier : ""),"true");	
				
				}
				
			}
		}
	}

	
	public boolean isClearedButtonPushed(IWContext iwc){
		return iwc.getParameter(SEARCH_CLEARED) != null;
	}
	
	private void processSearch(IWContext iwc) throws IDOLookupException, FinderException, RemoteException {
		UserHome home = (UserHome) IDOLookup.getHome(User.class);
		String first = iwc.getParameter(SEARCH_FIRST_NAME + this.uniqueIdentifier);
		String middle = iwc.getParameter(SEARCH_MIDDLE_NAME + this.uniqueIdentifier);
		String last = iwc.getParameter(SEARCH_LAST_NAME + this.uniqueIdentifier);
		String pid = iwc.getParameter(SEARCH_PERSONAL_ID + this.uniqueIdentifier);
		
		if (this.firstLetterCaseInsensitive) {
			if (first != null) {
				first = TextSoap.capitalize(first);
			}
			if (middle != null) {
				middle = TextSoap.capitalize(middle);
			}
			if (last != null) {
				last = TextSoap.capitalize(last);
			}
		}
		// dont allow empty search
		if ((pid != null && pid.length() > 0)
			|| (first != null && first.length() > 0)
			|| (middle != null && middle.length() > 0)
			|| (last != null && last.length() > 0)){
			// forgiving search criteria
			if(this.useFlexiblePersonalID){
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<pid.length(); i++) {
					char ch = pid.charAt(i);
					if (Character.isDigit(ch)){
						sb.append(ch);
					}
					else if(this.legalNonDigitPIDLetters!=null){
						if(this.legalNonDigitPIDLetters.indexOf(ch)>=0){
							// non digit letters turned to uppercase
							sb.append(Character.toUpperCase(ch));
						}
					}
				}
				//sb.insert(0,"%");
				pid = sb.toString();
			}
			this.usersFound =home.findUsersByConditions(first, middle, last, pid, null, null, -1, -1, -1, -1, null, null, true, false);
		}
		else{
			this.user = null;
		}
		//System.out.println("users found " + usersFound.size());
		if (this.user == null && this.usersFound != null) {
			// if some users found
			if (!this.usersFound.isEmpty()) {
				this.hasManyUsers = this.usersFound.size() > 1;
				if (!this.hasManyUsers) {
					this.user = (User) this.usersFound.iterator().next();
					getUserSession(iwc).setUser(this.user);
				}
			}
			// if no user found
			else {
				throw new FinderException("No user was found");
			}
		}
	}
	/**
		 * Presentates the users personal info
		 * @param iwc the current context
		 */
	private Table presentateCurrentUserSearch(IWContext iwc) {
		Table searchTable = new Table();
		int row = 1;
		int col = 1;
		Vector clearFields = new Vector();
				
		if (this.showPersonalIDInSearch) {
			Text tPersonalID = new Text(this.iwrb.getLocalizedString(SEARCH_PERSONAL_ID, "Personal ID"));
			tPersonalID.setStyleClass(this.headerFontStyleName);
			tPersonalID.setStyleAttribute(this.headerFontStyle);			
			searchTable.add(tPersonalID, col, row);
			TextInput input = new TextInput(SEARCH_PERSONAL_ID + this.uniqueIdentifier);
			input.setStyleClass(this.interfaceStyleName);
			input.setLength(this.personalIDLength);
			if (this.user != null && this.user.getPersonalID() != null) {
				input.setContent(this.user.getPersonalID());
			}
			if (this.stacked) {
				searchTable.add(input, col++, row + 1);
			}
			else {
				searchTable.add(input, ++col, row);
			}
			clearFields.add(SEARCH_PERSONAL_ID);
		}
		if (this.showLastNameInSearch) {
			Text tLastName = new Text(this.iwrb.getLocalizedString(SEARCH_LAST_NAME, "Last name"));
			tLastName.setStyleClass(this.headerFontStyleName);
			tLastName.setStyleAttribute(this.headerFontStyle);			
			searchTable.add(tLastName, col, row);
			TextInput input = new TextInput(SEARCH_LAST_NAME + this.uniqueIdentifier);
			input.setStyleClass(this.interfaceStyleName);
			input.setLength(this.lastNameLength);
			if (this.user != null && this.user.getLastName() != null) {
				input.setContent(this.user.getLastName());
			}
			if (this.stacked) {
				searchTable.add(input, col++, row + 1);
			}
			else {
				searchTable.add(input, ++col, row);
			}
			clearFields.add(SEARCH_LAST_NAME);
		}
		if (this.showMiddleNameInSearch) {
			Text tMiddleName = new Text(this.iwrb.getLocalizedString(SEARCH_MIDDLE_NAME, "Middle name"));
			tMiddleName.setStyleClass(this.headerFontStyleName);
			tMiddleName.setStyleAttribute(this.headerFontStyle);
			
			searchTable.add(tMiddleName, col, row);
			TextInput input = new TextInput(SEARCH_MIDDLE_NAME + this.uniqueIdentifier);
			input.setStyleClass(this.interfaceStyleName);
			input.setLength(this.middleNameLength);
			if (this.user != null && this.user.getMiddleName() != null) {
				input.setContent(this.user.getMiddleName());
			}
			if (this.stacked) {
				searchTable.add(input, col++, row + 1);
			}
			else {
				searchTable.add(input, ++col, row);
			}
			clearFields.add(SEARCH_MIDDLE_NAME);
		}
		if (this.showFirstNameInSearch) {
			Text tFirstName = new Text(this.iwrb.getLocalizedString(SEARCH_FIRST_NAME, "First name"));
			tFirstName.setStyleClass(this.headerFontStyleName);
			tFirstName.setStyleAttribute(this.headerFontStyle);
			searchTable.add(tFirstName, col, row);
			TextInput input = new TextInput(SEARCH_FIRST_NAME + this.uniqueIdentifier);
			input.setStyleClass(this.interfaceStyleName);
			input.setLength(this.firstNameLength);
			if (this.user != null) {
				input.setContent(this.user.getFirstName());
			}
			if (this.stacked) {
				searchTable.add(input, col++, row + 1);
			}
			else {
				searchTable.add(input, ++col, row);
			}
			clearFields.add(SEARCH_FIRST_NAME);
		}
		if (this.showButtons) {
			HiddenInput hidden = new HiddenInput(BUTTON_PRESSED, "-1");
			searchTable.add(hidden, col, row + 1);
			SubmitButton search =
				new SubmitButton(
					this.iwrb.getLocalizedString(SEARCH_COMMITTED, "Search"),
					SEARCH_COMMITTED + (this.constrainToUniqueSearch ? this.uniqueIdentifier : ""),
					"true");
			search.setValueOnClick(BUTTON_PRESSED, String.valueOf(SEARCH_PRESSED));
			search.setStyleClass(this.buttonStyleName);
			if (this.stacked) {
				searchTable.add(search, col++, row + 1);
			}
			else {
				searchTable.add(search, 1, row + 1);
			}
			if (this.addedButtons != null && !this.addedButtons.isEmpty()) {
				for (Iterator iter = this.addedButtons.iterator(); iter.hasNext();) {
					PresentationObject element = (PresentationObject) iter.next();
					if (this.stacked) {
						searchTable.add(element, col++, row + 1);
					}
					else {
						searchTable.add(element, 1, row + 1);
					}
				}
			}
		
			//new button added - ac 
			if (this.showNewUserButton) {
				
				SubmitButton newUserButton = new SubmitButton(NEW_USER, this.iwrb.getLocalizedString("new","New"));
				newUserButton.setValueOnClick(BUTTON_PRESSED, String.valueOf(NEW_PRESSED));
				newUserButton.setStyleClass(this.buttonStyleName);
				searchTable.add(newUserButton, col++, row + 1);
			}
			
			if (this.showResetButton) {
				String clearAction = "";
				for (Iterator iter = clearFields.iterator(); iter.hasNext();) {
					String field = (String) iter.next();
					clearAction += getClearActionPart(field, this.uniqueIdentifier,"''");
				}
				clearAction +=getClearActionObjectTest(PRM_USER_ID,this.uniqueIdentifier);
				clearAction += getClearActionPart(PRM_USER_ID,this.uniqueIdentifier,"-1");
				SubmitButton reset = new SubmitButton(SEARCH_CLEARED, this.iwrb.getLocalizedString("clear", "Clear"));
				reset.setStyleClass(this.buttonStyleName);
				reset.setOnClick(clearAction + "return false;");
				searchTable.add(reset, col++, row + 1);
			}
			if (this.showMultipleResetButton) {
				addClearButtonIdentifiers(this.uniqueIdentifier);
				String otherClearActions = "";
				for (Iterator iter = this.otherClearIdentifiers.iterator(); iter.hasNext();) {
					String identifier = (String) iter.next();
					for (Iterator iter2 = clearFields.iterator(); iter2.hasNext();) {
						String field = (String) iter2.next();
						otherClearActions += getClearActionPart(field, identifier,"''");
					}
					otherClearActions +=getClearActionObjectTest(PRM_USER_ID,identifier);
					otherClearActions +=getClearActionPart(PRM_USER_ID,identifier,"-1");
				}
			
			SubmitButton resetmultiple = new SubmitButton(SEARCH_CLEARED, this.iwrb.getLocalizedString("clear_all", "Clear All"));
			resetmultiple.setStyleClass(this.buttonStyleName);
			resetmultiple.setOnClick(otherClearActions + "return false;");
			searchTable.add(resetmultiple, col++, row + 1);
		}
	}
	return searchTable;
}
private String getClearActionPart(String field, String identifier,String value) {
	return "this.form." + field + identifier + ".value ="+value+" ;";
}

private String getClearActionObjectTest(String field,String identifier){
	return "if(this.form." + field + identifier + ")";
}
/**
	 * Presentates the users found by search
	 * @param iwc the context
	*/
private Table presentateFoundUsers(IWContext iwc) {
	Table T = new Table();
	if (this.usersFound != null && !this.usersFound.isEmpty()) {
		Iterator iter = this.usersFound.iterator();
		T.setCellspacing(4);
		Link userLink;
		int row = 1;
		int col = 1;
		int colAdd = 1;


		HiddenInput userPk = new HiddenInput(getUniqueUserParameterName(this.uniqueIdentifier));
		if (this.setToFormSubmit) {
			getParentForm().add(userPk);
			addParameters(getParentForm());				
		}
						
		while (iter.hasNext()) {
			User u = (User) iter.next();
			T.add(PersonalIDFormatter.format(u.getPersonalID(),iwc.getCurrentLocale()), colAdd, row);
			userLink = new Link(u.getName());
			
			if (getUserInfoPage() == null) {
				//Added by Roar 29.10.03
				if (this.setToFormSubmit){
					userLink.setToFormSubmit(getParentForm());	
					userLink.setOnClick("findObj('"+ userPk.getID() +"').value='"+ u.getPrimaryKey() +"';");
				}
				
				userLink.addParameter(getUniqueUserParameter((Integer) u.getPrimaryKey()));
				userLink.setEventListener(getListenerClass());
				addParameters(userLink);
			} else {
				userLink.setPage(getUserInfoPage());
				userLink.addParameter("mbe_userid", u.getId());
				userLink.addParameter(PRM_USER_ID, u.getId());
			}
			T.add(userLink, colAdd + 1, row);
			row++;
			if (row == this.maxFoundUserRows) {
				col++;
				colAdd += 2;
				row = 1;
			}
			if (col == this.maxFoundUserCols) {
				break;
			}
		}
		if (this.showOverFlowMessage && iter.hasNext()) {
			int lastRow = T.getRows() + 1;
			T.mergeCells(1, lastRow, this.maxFoundUserCols, lastRow);
			Text tOverflowMessage =
				new Text(
					this.iwrb.getLocalizedString(
						"usrch_overflow_message",
						"There are more hits in your search than shown, you have to narrow down your searchcriteria"));
			tOverflowMessage.setStyleClass(this.warningStyleName);
			T.add(tOverflowMessage, 1, lastRow);
		}
	}
	return T;
}
public void addClearButtonIdentifiers(String identifier) {
	if (this.otherClearIdentifiers == null) {
		this.otherClearIdentifiers = new Vector();
	}
	this.otherClearIdentifiers.add(identifier);
}
private void addParameters(Link link) {
	for (Iterator iter = this.maintainedParameters.iterator(); iter.hasNext();) {
		Parameter element = (Parameter) iter.next();
		link.addParameter(element);
	}
	if(this.monitorMap!=null){
		for (Iterator iter = this.monitorMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			link.addParameter((String)entry.getKey(),(String)entry.getValue());
		}
	}
	link.setEventListener(getListenerClass());
}

private void addParameters(Form form) {
	for (Iterator iter = this.maintainedParameters.iterator(); iter.hasNext();) {
		Parameter element = (Parameter) iter.next();
		form.addParameter(element.getName(), element.getValueAsString());
	}
	if(this.monitorMap!=null){
		for (Iterator iter = this.monitorMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			form.addParameter((String)entry.getKey(),(String)entry.getValue());
		}
	}
}


/**
 * Flags the first name field in the user search
 * @param b
 */
public void setShowFirstNameInSearch(boolean b) {
	this.showFirstNameInSearch = b;
}
/**
 * Flags the last name in the user search
 * @param b
 */
public void setShowLastNameInSearch(boolean b) {
	this.showLastNameInSearch = b;
}
/**
 * Flags the middle name in the user search
 * @param b
 */
public void setShowMiddleNameInSearch(boolean b) {
	this.showMiddleNameInSearch = b;
}
/**
 * Flags the personal id in the user search
 * @param b
 */
public void setShowPersonalIDInSearch(boolean b) {
	this.showPersonalIDInSearch = b;
}
/**
 * Flag telling if  the search found more than one user
 * @return
 */
public boolean isHasManyUsers() {
	return this.hasManyUsers;
}
/**
 * Gets the number of maximum allowed result columns 
 * @return
 */
public int getMaxFoundUserCols() {
	return this.maxFoundUserCols;
}
/**
 * Gets the number of maximum allowed result rows
 * @return
 */
public int getMaxFoundUserRows() {
	return this.maxFoundUserRows;
}
/**
 * Gets the selected user
 * @return User
 */
public User getUser() {
	return this.user;
}
/**
 * Gets the collection of users found by searc
 * @return
 */
public Collection getUsersFound() {
	return this.usersFound;
}
/**
 * Set the maximum number of columns showing search results
 * @param cols
 */
public void setMaxFoundUserCols(int cols) {
	this.maxFoundUserCols = cols;
}
/**
 * Sets the maximum number of rows showing search results
 * @param i
 */
public void setMaxFoundUserRows(int rows) {
	this.maxFoundUserRows = rows;
}
/**
 * Manually set the found user
 * @param user
 */
public void setUser(User user) {
	this.user = user;
}
/**
 * Manually set the found user collection
 * @param collection
 */
public void setUsersFound(Collection collection) {
	this.usersFound = collection;
}
/**
 * Add maintainedparameters
 * @param parameter
 */
public void maintainParameter(Parameter parameter) {
	this.maintainedParameters.add(parameter);
}
/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#getBundleIdentifier()
	 */
@Override
public String getBundleIdentifier() {
	if (this.bundleIdentifer != null) {
		return this.bundleIdentifer;
	}
	return BUNDLE_IDENTIFIER;
}
/**
 * Sets the dynamic bundle identifier
 * @param string
 */
public void setBundleIdentifer(String string) {
	this.bundleIdentifer = string;
}
/**
 * Gets the unique user id parameter  to be used for chosen user
 * @param userID
 * @return Parameter
 */
public Parameter getUniqueUserParameter(Integer userID) {
	return new Parameter(getUniqueUserParameterName(this.uniqueIdentifier), userID.toString());
}
/**
 * Gets the unique user id parameter name to be used for chosen user
 * @param uniqueIdentifier
 * @return parameter name
 */
public static String getUniqueUserParameterName(String uniqueIdentifier) {
	return PRM_USER_ID + uniqueIdentifier;
}



/**
* This method checkes if the fields for first name, last name and ssn... 
*...are filled in, but only if the new user button is clicked 
*/
// - added by anna - april 2005
public String checkEmptyFieldScript() {
	StringBuffer buffer = new StringBuffer();
	String message = null;
	buffer.append("\nfunction newUser(){\n\t");
	
	buffer.append("\n\t var pressed = ").append("findObj('").append(BUTTON_PRESSED).append("').value;");
	
	buffer.append("\n\t if (pressed == ").append(NEW_PRESSED).append("){");
	
	buffer.append("\n\t\t var personalID = ").append("findObj('").append(SEARCH_PERSONAL_ID + this.uniqueIdentifier).append("').value;");
	buffer.append("\n\t\t var lastName = ").append("findObj('").append(SEARCH_LAST_NAME + this.uniqueIdentifier).append("').value;");
	buffer.append("\n\t\t var firstName = ").append("findObj('").append(SEARCH_FIRST_NAME + this.uniqueIdentifier).append("').value;");
	
	buffer.append("\n\n\t\t if (personalID == '') {");
	
	message = this.iwrb.getLocalizedString("user_searcher.must_fill_out_personal_id", "Please fill out the personal id field");
	buffer.append("\n\t\t\t alert('").append(message).append("');");
	buffer.append("\n\t\t\t return false;");
	buffer.append("\n\t\t }");
	
	buffer.append("\n\n\t\t if (personalID.length != 12) {");
	
	message = this.iwrb.getLocalizedString("user_searcher.invalid_personal_id", "Invalid personal ID");
	buffer.append("\n\t\t\t alert('").append(message).append("');");
	buffer.append("\n\t\t\t return false;");
	buffer.append("\n\t\t }");
	
	buffer.append("\n\n\t\t if (lastName == '') {");
	message = this.iwrb.getLocalizedString("user_searcher.must_fill_out_last_name", "Please fill out the last name field");
	buffer.append("\n\t\t\t alert('").append(message).append("');");
	buffer.append("\n\t\t\t return false;");
	buffer.append("\n\t\t }");
	
	buffer.append("\n\n\t\t if (firstName == '') {");
	message = this.iwrb.getLocalizedString("user_searcher.must_fill_out_first_name", "Please fill out the first name field");
	buffer.append("\n\t\t\t alert('").append(message).append("');");
	buffer.append("\n\t\t\t return false;");
	buffer.append("\n\t\t }");
	
	message = this.iwrb.getLocalizedString("user_searcher.are_you_sure_you_want_to_save", "Are you sure you want to save the new user?");
	buffer.append("\n\t\t return confirm('").append(message).append("');");
	buffer.append("\n\t}");
	
	buffer.append("\n\t else return true;");
	
	buffer.append("\n}\n");
	
	return buffer.toString();
}
/* (non-Javadoc)
 * @see com.idega.presentation.Block#getStyleNames()
 */
@Override
public Map getStyleNames() {
	HashMap map = new HashMap();
	map.put(STYLENAME_HEADER, this.headerFontStyle);
	map.put(STYLENAME_TEXT, this.textFontStyle);
	map.put(STYLENAME_BUTTON, this.buttonStyle);
	map.put(STYLENAME_WARNING, this.warningFontStyle);
	map.put(STYLENAME_INTERFACE, this.interfaceStyle);
	return map;
}
/**
 * Gets the input length of the first name input
 * @return
 */
public int getFirstNameLength() {
	return this.firstNameLength;
}
/**
 * Gets the heading font style
 * @return font style
 */
public String getHeaderFontStyle() {
	return this.headerFontStyle;
}
/**
 * Gets the inputlength of the last name input
 * @return length
 */
public int getLastNameLength() {
	return this.lastNameLength;
}
/**
 * Gets the input length of the middle name input
 * @return length
 */
public int getMiddleNameLength() {
	return this.middleNameLength;
}
/**
 * Gets the inputlength of the personal id input
 * @return length
 */
public int getPersonalIDLength() {
	return this.personalIDLength;
}
/**
 * Gets flag for first name input appearance
 * @return flag
 */
public boolean isShowFirstNameInSearch() {
	return this.showFirstNameInSearch;
}
/**
 * Gets flag for last name input appearance
 * @return flag
 */
public boolean isShowLastNameInSearch() {
	return this.showLastNameInSearch;
}
/**
 * Gets flag for middle name input appearance
 * @return flag
 */
public boolean isShowMiddleNameInSearch() {
	return this.showMiddleNameInSearch;
}
/**
 * Gets flag for personal ID appearance
 * @return flag
 */
public boolean isShowPersonalIDInSearch() {
	return this.showPersonalIDInSearch;
}
/**
 * Gets status of stacked flag
 * @return flag <code>boolean</code>
 */
public boolean isStacked() {
	return this.stacked;
}
/**
 * Gets the normal text font style
 * @return font style
 */
public String getTextFontStyle() {
	return this.textFontStyle;
}
/**
 * Sets the length of the first name input
 * @param length
 */
public void setFirstNameLength(int length) {
	this.firstNameLength = length;
}
/**
 * Sets the heading font style
 * @param style
 */
public void setHeaderFontStyle(String style) {
	this.headerFontStyle = style;
}
/**
 * Sets the length of the last name input
 * @param length
 */
public void setLastNameLength(int length) {
	this.lastNameLength = length;
}
/**
 * Sets the length of the middle name input
 * @param length
 */
public void setMiddleNameLength(int length) {
	this.middleNameLength = length;
}
/**
 * Sets the  length of the personalID input
 * @param length
 */
public void setPersonalIDLength(int length) {
	this.personalIDLength = length;
}
/**
 * Flags if searcher should be presented with stacked headers and inputs
 * @param flag
 */
public void setStacked(boolean flag) {
	this.stacked = flag;
}
/**
 * Set normal text font style
 * @param style
 */
public void setTextFontStyle(String style) {
	this.textFontStyle = style;
}
/**
 * Returns the status of the flag , concerning the searcher own form
 * @return flag status
 */
public boolean isOwnFormContainer() {
	return this.OwnFormContainer;
}
/**
 * Flags if the searcher should provide its own form
 * @param flag
 */
public void setOwnFormContainer(boolean flag) {
	this.OwnFormContainer = flag;
}
/**
 * Gets the unique identifier for this searcher instance
 * @return unique identifier
 */
public String getUniqueIdentifier() {
	return this.uniqueIdentifier;
}
/**
 * Sets a unique identifier, convenient when using many instances on same page
 * @param identifier
 */
public void setUniqueIdentifier(String identifier) {
	this.uniqueIdentifier = identifier;
}
/**
 * @return
 */
public String getButtonStyle() {
	return this.buttonStyle;
}
/**
 * @return
 */
public String getButtonStyleName() {
	return this.buttonStyleName;
}
/**
 * @return
 */
public String getHeaderFontStyleName() {
	return this.headerFontStyleName;
}
/**
 * Gets the button style
 * @return style
 */
public String getTextFontStyleName() {
	return this.textFontStyleName;
}
/**
 * Sets the button style
 * @param string
 */
public void setButtonStyle(String string) {
	this.buttonStyle = string;
}
/**
 * Sets the button style name to use
 * @param string
 */
public void setButtonStyleName(String string) {
	this.buttonStyleName = string;
}
/**
 * Sets the header font style name to use
 * @param string
 */
public void setHeaderFontStyleName(String string) {
	this.headerFontStyleName = string;
}
/**
 * Sets the normal font style name to use
 * @param string
 */
public void setTextFontStyleName(String string) {
	this.textFontStyleName = string;
}
/**
 * Flag status, skips result list if only one user found
 * @return
 */
public boolean isSkipResultsForOneFound() {
	return this.skipResultsForOneFound;
}
/**
 * Sets flag for skipping result list if only one user found
 * @param 
 */
public void setSkipResultsForOneFound(boolean flag) {
	this.skipResultsForOneFound = flag;
}
/**
 * @return
 */
public boolean isShowResetButton() {
	return this.showResetButton;
}
/**
 * @param b
 */
public void setShowResetButton(boolean b) {
	this.showResetButton = b;
}
/**
 * @return
 */
public boolean isShowOverFlowMessage() {
	return this.showOverFlowMessage;
}
/**
 * @param b
 */
public void setShowOverFlowMessage(boolean b) {
	this.showOverFlowMessage = b;
}
public void addButtonObject(PresentationObject obj) {
	if (this.addedButtons == null) {
		this.addedButtons = new Vector();
	}
	this.addedButtons.add(obj);
}
/**
 * @return
 */
public boolean isShowButtons() {
	return this.showButtons;
}
/**
 * @param b
 */
public void setShowButtons(boolean b) {
	this.showButtons = b;
}
/**
 * @return
 */
public boolean isConstrainToUniqueSearch() {
	return this.constrainToUniqueSearch;
}
/**
 * @param constrainToUniqueSearch
 */
public void setConstrainToUniqueSearch(boolean constrainToUniqueSearch) {
	this.constrainToUniqueSearch = constrainToUniqueSearch;
}
/**
 * @return
 */
public boolean isShowMultipleResetButton() {
	return this.showMultipleResetButton;
}
/**
 * @param showMultipleResetButton
 */
public void setShowMultipleResetButton(boolean showMultipleResetButton) {
	this.showMultipleResetButton = showMultipleResetButton;
}

public void addMonitoredSearchIdentifier(String identifier){
	if(this.monitoredSearchIdentifiers==null) {
		this.monitoredSearchIdentifiers = new Vector();
	}
	this.monitoredSearchIdentifiers.add(identifier);
}

public void setToFormSubmit(boolean b){
	this.setToFormSubmit = b;
}

public boolean getToFormSubmit(){
	return this.setToFormSubmit;
}

public void setUseFlexiblePersonalID(boolean flag){
	this.useFlexiblePersonalID = flag;
}

public boolean isUseFlexiblePersonalID(){
	return this.useFlexiblePersonalID;
}
	/**
	 * @return Returns the legalNonDigitPIDLetters.
	 */
	public String getLegalNonDigitPIDLetters() {
		return this.legalNonDigitPIDLetters;
	}

	/**
	 * @param legalNonDigitPIDLetters The legalNonDigitPIDLetters to set.
	 */
	public void setLegalNonDigitPIDLetters(String legalNonDigitPIDLetters) {
		this.legalNonDigitPIDLetters = legalNonDigitPIDLetters;
	}

	public boolean actionPerformed(IWContext iwc) throws IWException {
		try {
			process(iwc, true);
			return true;
		}
		catch (IDOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected Class getListenerClass() {
		return UserSearcher.class;
	}
}