package com.idega.user.presentation;

import java.util.Collection;

import com.idega.builder.business.BuilderLogic;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.AbstractChooser;

/**
 * @author gimmi
 */
public class UserChooser extends AbstractChooser {

	private int _userId = -1;
	private Collection userPks;

  public UserChooser(String chooserName) {
    addForm(false);
    setChooserParameter(chooserName);
  }

  public UserChooser(String chooserName,String style) {
    this(chooserName);
    setInputStyle(style);
  }

  public void main(IWContext iwc){
  	if (userPks != null && userPks.size() > 0) {
	  	iwc.setSessionAttribute(UserChooserWindow.AVAILABLE_USER_PKS_SESSION_PARAMETER, userPks);
  	}
    IWBundle iwb = iwc.getApplication().getBundle(BuilderLogic.IW_BUNDLE_IDENTIFIER);
    setChooseButtonImage(iwb.getImage("open.gif","Choose"));
  }


 public void setSelected(String userId){
    super.setChooserValue(userId,userId);
    super.setParameterValue("user_id",userId);
  }
  
 	/**
	 * @see com.idega.presentation.ui.AbstractChooser#getChooserWindowClass()
	 */
	public Class getChooserWindowClass() {
		return UserChooserWindow.class;
	}
	
	public void setValidUserPks(Collection userPks) {
		this.userPks = userPks;	
	}

}