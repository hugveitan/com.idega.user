package com.idega.user.presentation;

import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.builder.data.IBDomain;
import com.idega.user.event.SelectGroupEvent;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.core.ICTreeNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.browser.presentation.IWTreeControl;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;


/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class GroupTreeView extends IWTreeControl {

  private static final String TREEVIEW_PREFIX = "treeviewer/ui/";

  Image folderAndFileIcons[][] = null;
  String folderAndFileIconNames[]={"treeviewer_node_closed.gif","treeviewer_node_open.gif","treeviewer_node_leaf.gif"};
  String classTypeIcons[] = {"domain/","group/","user/"};

  private static final int FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED = 0;
  private static final int FOLDERANDFILE_ICONINDEX_FOLDER_OPEN = 1;
  private static final int FOLDERANDFILE_ICONINDEX_FILE = 2;

  public static final String PRM_OPEN_TREENODES = "ic_opn_trnds";
  public static final String PRM_TREENODE_TO_CLOSE = "ic_cls_trnd";

  String nodeNameTarget = null;
  String nodeActionPrm = null;
  Link _linkPrototype = null;
  String _linkStyle = null;
  boolean _usesOnClick = false;
  private boolean _nowrap = true;
  private Layer _nowrapLayer = null;

  public static final String ONCLICK_FUNCTION_NAME = "treenodeselect";
  public static final String ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME = "iw_node_id";
  public static final String ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME = "iw_node_name";

  private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";

  public GroupTreeView() {
    super();
    folderAndFileIcons = new Image[3][3];
    this.setColumns(2);
    this.setTreeColumnWidth(1,"16");
  }


  public static GroupTreeView getGroupTreeInstance(ICTreeNode node,IWContext iwc){
    GroupTreeView viewer = new GroupTreeView();
    viewer.setRootNode(node);
    return viewer;
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }


  protected void updateIconDimensions(){
    super.updateIconDimensions();
    for (int j = 0; j < folderAndFileIcons.length; j++) {
      for (int i = 0; i < folderAndFileIcons[j].length; i++) {
        Image tmp = folderAndFileIcons[j][i];
        if(tmp != null){
          //tmp.setWidth(iconWidth);
          tmp.setHeight(iconHeight);
          //tmp.setAlignment("top");
          folderAndFileIcons[j][i] = tmp;
        }
      }
    }
  }

  public void initIcons(IWContext iwc){
    super.initIcons(iwc);

    IWBundle bundle = getBundle(iwc);
    for (int j = 0; j < classTypeIcons.length; j++) {
      for (int i = 0; i < folderAndFileIcons.length; i++) {
        if(folderAndFileIcons[j][i] == null){
          folderAndFileIcons[j][i] = bundle.getImage(TREEVIEW_PREFIX+getUI()+classTypeIcons[j]+folderAndFileIconNames[i]);
        }
      }
    }

    updateIconDimensions();
  }




/*
  public void addParameters(Link l, ICTreeNode node, IWContext iwc){

  }
*/

  public PresentationObject getObjectToAddToColumn(int colIndex, ICTreeNode node, IWContext iwc, boolean nodeIsOpen, boolean nodeHasChild, boolean isRootNode){
    //System.out.println("adding into column "+ colIndex + " for node " + node);

    switch (colIndex) {
      case 1:
        if(!node.isLeaf()){
          if(nodeIsOpen){
            if(isRootNode && !showRootNodeTreeIcons()){
              Link l = new Link();
              if(this.getControlEventModel() != null){
                l.addEventModel(this.getControlEventModel());
              }
              if(this.getControlTarget() != null){
                l.setTarget(this.getControlTarget());
              }
              if(node instanceof IBDomain){
                l.setImage(folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN]);
              } else if(node instanceof Group){
                l.setImage(folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN]);
              } else if(node instanceof User){
                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN]);
              }

              if(!nodeIsOpen){ //   || allowRootNodeToClose ){
                this.setLinkToOpenOrCloseNode(l,node,nodeIsOpen);
              }
              return l;
            } else {
              if(node instanceof IBDomain){
                return folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN];
              } else if(node instanceof Group){
                return folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN];
              } else if(node instanceof User){
                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_OPEN];
              }

            }
          } else {
            if(isRootNode && !showRootNodeTreeIcons()){
              Link l = new Link();
              if(this.getControlEventModel() != null){
                l.addEventModel(this.getControlEventModel());
              }
              if(this.getControlTarget() != null){
                l.setTarget(this.getControlTarget());
              }
              if(node instanceof IBDomain){
                l.setImage(folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED]);
              } else if(node instanceof Group){
                l.setImage(folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED]);
              } else if(node instanceof User){
                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED]);
              }
              this.setLinkToOpenOrCloseNode(l,node,nodeIsOpen);
              return l;
            } else {
              if(node instanceof IBDomain){
                return folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED];
              } else if(node instanceof Group){
                return folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED];
              } else if(node instanceof User){
                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FOLDER_CLOSED];
              }

            }
          }
        } else {
          if(isRootNode && !showRootNodeTreeIcons()){
              Link l = new Link();
              if(this.getControlEventModel() != null){
                l.addEventModel(this.getControlEventModel());
              }
              if(this.getControlTarget() != null){
                l.setTarget(this.getControlTarget());
              }
              if(node instanceof IBDomain){
                l.setImage(folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FILE]);
              } else if(node instanceof Group){
                l.setImage(folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FILE]);
              } else if(node instanceof User){
                l.setImage(folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FILE]);
              }
              this.setLinkToOpenOrCloseNode(l,node,nodeIsOpen);
              return l;
            } else {
              if(node instanceof IBDomain){
                return folderAndFileIcons[0][FOLDERANDFILE_ICONINDEX_FILE];
              } else if(node instanceof Group){
                return folderAndFileIcons[1][FOLDERANDFILE_ICONINDEX_FILE];
              } else if(node instanceof User){
                return folderAndFileIcons[2][FOLDERANDFILE_ICONINDEX_FILE];
              }
            }
        }
      case 2:
        Link l = this.getLinkPrototypeClone(node.getNodeName());

        SelectGroupEvent grSelect = new SelectGroupEvent();
        grSelect.setGroupToSelect(node.getNodeID());

        l.addEventModel(grSelect);

        if(_usesOnClick){
          String nodeName = node.getNodeName();
          l.setURL("#");
          l.setOnClick(ONCLICK_FUNCTION_NAME+"('"+nodeName+"','"+node.getNodeID()+"')");
        }
//        else if(nodeActionPrm != null){
//          l.addParameter(nodeActionPrm,node.getNodeID());
//        }
        this.setLinkToMaintainOpenAndClosedNodes(l);
        if(_nowrap){
          return getNoWrapLayerClone(l);
        } else {
          return l;
        }
    }
    return null;
  }

  public void setWrap(){
    _nowrap = false;
  }

  public void setWrap(boolean value){
    _nowrap = value;
  }

  public void setNodeActionParameter(String prm){
    nodeActionPrm = prm;
  }

  public void setTarget(String target){
    nodeNameTarget = target;
  }

  public void setTreeStyle(String style) {
    _linkStyle = style;
  }

  public void setLinkPrototype(Link link){
    _linkPrototype=link;
  }

  private Link getLinkPrototype(){
    if(_linkPrototype==null){
      _linkPrototype=new Link();
    }

    if(nodeNameTarget != null){
      _linkPrototype.setTarget(nodeNameTarget);
    }

    if(this.getControlTarget() != null){
      _linkPrototype.setTarget(this.getControlTarget());
    }

    if(_linkStyle != null){
      _linkPrototype.setFontStyle(_linkStyle);
    }

/*
    if ( _linkStyle != null )
      _linkPrototype.setFontStyle(_linkStyle);
*/
    return _linkPrototype;
  }

  public Layer getNoWrapLayer(){
    if(_nowrapLayer == null){
      _nowrapLayer = new Layer();
      _nowrapLayer.setNoWrap();
    }
    return _nowrapLayer;
  }

  private Link getLinkPrototypeClone(){
    return (Link)getLinkPrototype().clone();
  }

  private Link getLinkPrototypeClone(String text){
    Link l = (Link)getLinkPrototype().clone();
    l.setText(text);
    if(this.getControlEventModel() != null){
      l.addEventModel(this.getControlEventModel());
    } else {
      System.out.println("GROUPTREEVIEW: eventmodel == null");
    }


    if(this.getControlTarget() != null){
      l.setTarget(this.getControlTarget());
    } else {
      System.out.println("GROUPTREEVIEW: controlTarget == null");
    }
    return l;
  }

  private Layer getNoWrapLayerClone(){
    Layer l = (Layer)getNoWrapLayer().clone();
    return l;
  }

  private Layer getNoWrapLayerClone(PresentationObject obj){
    Layer l = getNoWrapLayerClone();
    l.add(obj);
    return l;
  }

  private Link getLinkPrototypeClone(Image image){
    Link l = (Link)getLinkPrototype().clone();
    l.setImage(image);
    if(this.getControlEventModel() != null){
      l.addEventModel(this.getControlEventModel());
    }
    return l;
  }


//  public void setToUseOnClick(){
//    setToUseOnClick(ONCLICK_DEFAULT_NODE_NAME_PARAMETER_NAME,ONCLICK_DEFAULT_NODE_ID_PARAMETER_NAME);
//  }
//
//  public void setToUseOnClick(String NodeNameParameterName,String NodeIDParameterName){
//    _usesOnClick=true;
//    getAssociatedScript().addFunction(ONCLICK_FUNCTION_NAME,"function "+ONCLICK_FUNCTION_NAME+"("+NodeNameParameterName+","+NodeIDParameterName+"){ }");
//
//  }
//
//  public void setOnClick(String action){
//     this.getAssociatedScript().addToFunction(ONCLICK_FUNCTION_NAME,action);
//  }


  public void setControlTarget(String controlTarget){
    super.setControlTarget(controlTarget);
    nodeNameTarget = null;
  }

}