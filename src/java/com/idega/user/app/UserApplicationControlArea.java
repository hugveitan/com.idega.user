package com.idega.user.app;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import com.idega.presentation.*;
import com.idega.event.*;
import com.idega.presentation.event.TreeViewerEvent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWLocation;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.user.presentation.GroupTreeView;
import com.idega.util.IWColor;
import java.util.Collection;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class UserApplicationControlArea extends Page implements IWBrowserView, StatefullPresentation {

  private IWBundle iwb;
  private StatefullPresentationImplHandler _stateHandler = null;
  private String _controlTarget = null;
  private IWPresentationEvent _contolEvent = null;

  private GroupTreeView groupTree = new GroupTreeView();



  public UserApplicationControlArea() {
    _stateHandler = new StatefullPresentationImplHandler();
    _stateHandler.setPresentationStateClass(UserApplicationControlAreaPS.class);
  }

  public void setControlEventModel(IWPresentationEvent model){
//    System.out.print("UserApplicationControlArea: setControlEventModel(IWPresentationEvent model)");
    _contolEvent = model;

    groupTree.setControlEventModel(model);
  }

  public void setControlTarget(String controlTarget){
//    System.out.print("UserApplicationControlArea: setControlTarget(String controlTarget)");
    _controlTarget = controlTarget;
    groupTree.setControlTarget(controlTarget);
  }

  public Class getPresentationStateClass(){
    return _stateHandler.getPresentationStateClass();
  }

  public IWPresentationState getPresentationState(IWUserContext iwuc){
    return _stateHandler.getPresentationState(this,iwuc);
  }

  public StatefullPresentationImplHandler getStateHandler(){
    return _stateHandler;
  }

  public String getBundleIdentifier(){
    return "com.idega.user";
  }



  public void initializeInMain(IWContext iwc){

    iwb = getBundle(iwc);

    IWLocation location = (IWLocation)this.getLocation().clone();
    location.setSubID(1);
    groupTree.setLocation(location,iwc);


//    IWPresentationState gtState = groupTree.getPresentationState(iwc);
//    if(gtState instanceof IWActionListener){
//      groupTree.addIWActionListener((IWActionListener)gtState);
//    }
//
//
//
//    EventListenerList list = this.getEventListenerList(iwc);
//    IWActionListener[] listeners = (IWActionListener[])list.getListeners(IWActionListener.class);
//    if(listeners != null ){
//      for (int i = 0; i < listeners.length; i++) {
//        groupTree.addIWActionListener(listeners[i]);
//      }
//
//    }




//    UserApplicationControlAreaPS ps = (UserApplicationControlAreaPS)this.getPresentationState(iwc);
//    ps.addInnerListener(TreeViewerEvent.class, (IWActionListener)groupTree.getPresentationState(iwc));

//    groupTree.addIWActionListener((IWActionListener)ps);



/**
 * fix : EventListenerList list = this.getEventListenerList(iwc);
 * fix : this.setIWUserContext(iwc);
 */
//    EventListenerList list = this.getEventListenerList(iwc);

    this.setIWUserContext(iwc);

    IWPresentationState gtState = groupTree.getPresentationState(iwc);
    if(gtState instanceof IWActionListener){
      ((UserApplicationControlAreaPS)this.getPresentationState(iwc)).addIWActionListener((IWActionListener)gtState);
    }

    groupTree.setToShowSuperRootNode(true);
    groupTree.setDefaultOpenLevel(0);
    groupTree.setSuperRootNodeName("idegaWeb");
    Image icon = iwb.getImage("super_root_icon.gif");
    groupTree.setSuperRootNodeIcon(icon);



    ChangeListener[] chListeners = this.getPresentationState(iwc).getChangeListener();
    if(chListeners != null){
      for (int i = 0; i < chListeners.length; i++) {
        gtState.addChangeListener(chListeners[i]);
      }
    }

//    this.debugEventListanerList(iwc);
//    groupTree.debugEventListanerList(iwc);

    this.getParentPage().setBackgroundColor(IWColor.getHexColorString(250,245,240));

  }

  public void main(IWContext iwc) throws Exception {

    this.empty();
    this.add(groupTree);

    Collection topGroups = iwc.getDomain().getTopLevelGroupsUnderDomain();
    if(topGroups != null){
      groupTree.setFirstLevelNodes(topGroups.iterator());
    }

//    groupTree.setControlEventModel(_contolEvent);
//    groupTree.setControlTarget(_controlTarget);

//    this.getParentPage().setBackgroundColor("#d4d0c8");

  }



}