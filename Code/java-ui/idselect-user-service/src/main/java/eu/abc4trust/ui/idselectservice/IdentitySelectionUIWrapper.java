//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ui.idselectservice;

import com.google.inject.Inject;

import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.returnTypes.UiIssuanceArguments;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;

public class IdentitySelectionUIWrapper implements IdentitySelectionUi {

  public boolean done = false;
  public boolean canceled = false;


  private UiPresentationArguments uiPresentationArguments;
  private UiPresentationReturn uiPresentationReturn;

  private boolean hasPresentationChoices = false;
  private boolean presentationTokenSelected = false;

  private UiIssuanceArguments uiIssuanceArguments;
  private UiIssuanceReturn uiIssuanceReturn;

//  public SelectIssuanceTokenDescription selectIssuanceTokenDescription;
//  public SelectPresentationTokenDescription selectPresentationTokenDescription;
//
  private boolean hasIssuanceChoices = false;
  private boolean issuanceTokenSelected = false;

  private Exception e;

  @Inject
  public IdentitySelectionUIWrapper() {
//    this.done = false;
//    // this.presToken = null;
//    // this.issToken = null;
//    this.hasPresentationChoices = false;
//    this.presentationTokenSelected = false;
//    this.hasIssuanceChoices = false;
//    this.issuanceTokenSelected = false;
  }

  public void setException(Exception e) {
    this.e = e;
  }

  public Exception getException() {
    return this.e;
  }

  public boolean hasPresentationChoices() {
    return this.hasPresentationChoices;
  }

  public boolean hasIssuanceChoices() {
    return this.hasIssuanceChoices;
  }

  public UiPresentationArguments getUiPresentationArguments() {
    return uiPresentationArguments;
  }

  public UiPresentationReturn getUiPresentationReturn() {
    return uiPresentationReturn;
  }

  public UiIssuanceArguments getUiIssuanceArguments() {
    return uiIssuanceArguments;
  }

  public UiIssuanceReturn getUiIssuanceReturn() {
    return uiIssuanceReturn;
  }

  @Override
  public UiIssuanceReturn selectIssuanceTokenDescription(UiIssuanceArguments uiIssuanceArguments) throws IdentitySelectionException {
    System.out.println("ABC engine called selectIssuanceTokenDescription in idSelectionWrapper");
    this.uiIssuanceArguments = uiIssuanceArguments;
    // tell userservice - that it can continue
    this.hasIssuanceChoices = true;
    
    try {// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been
         // made
      System.out.println("idselectionwrapper/issuance going to sleep");
      while (!this.issuanceTokenSelected) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      System.out.println("idSelection/issuance got interrupted");
    }
    System.out.println("idselectionwrapper/issuance waking up!");
    // error handling
    if(e != null) {
      throw new IdentitySelectionException(e);
    } else {
      if (this.canceled) {
        IdentitySelectionException e = new IdentitySelectionException();
        e.errorMessages.add("User Cancelled IdentitySelection");
        throw e;
      }
    }
    // The idSelectionWrapper has got a choice it can return
    // NOT HERE - mark done in UserService!
    // done = true;
    return this.uiIssuanceReturn;
  }

  public void setUiIssuanceReturn(UiIssuanceReturn uiIssuanceReturn) {
    // The UI gave the idSelectionWrapper some input, time to wake up
    this.uiIssuanceReturn = uiIssuanceReturn;
    this.issuanceTokenSelected = true;
    if (uiIssuanceReturn == null) {
      System.out.println("CANCEL !!!");
      this.canceled = true;
    }

  }


  @Override
  public UiPresentationReturn selectPresentationTokenDescription(
      UiPresentationArguments uiPresentationArguments) throws IdentitySelectionException {
    System.out.println("selectPresentationTokenDescription called!!!!!!");

    this.uiPresentationArguments = uiPresentationArguments;
    // tell userservice - that it can continue
    this.hasPresentationChoices = true;

    try {// The idSelectionWrapper is ready to produce JSON, going to sleep until a choice has been
         // made
      System.out
          .println(">> idSelectionwrapper going to sleep, waiting for a choice made by the user");
      while (!this.presentationTokenSelected) {
        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      System.out.println("idSelection got interrupted");
    }

    // error handling
    if(e != null) {
      throw new IdentitySelectionException(e);
    } else {
      if (this.canceled) {
        IdentitySelectionException e = new IdentitySelectionException();
        e.errorMessages.add("User Cancelled IdentitySelection");
        throw e;
      }
    }
    System.out.println(">> idSelectionwrapper got a choice from the user and is now done");
    System.out.println(">> " + this.uiPresentationReturn);
    System.out.println(">> - chosenPolicy            : " + this.uiPresentationReturn.chosenPolicy);
    System.out.println(">> - chosenPresentationToken : " + this.uiPresentationReturn.chosenPresentationToken);
    System.out.println(">> - chosenPseudonymList     : " + this.uiPresentationReturn.chosenPseudonymList);
    System.out.println(">> - chosenInspectors        : " + this.uiPresentationReturn.chosenInspectors);
    System.out.println(">> - metadataToChange        : " + this.uiPresentationReturn.metadataToChange);
    // The idSelectionWrapper has got a choice it can return
    // NOT HERE - mark done in UserService!
    // done = true;
    return this.uiPresentationReturn;
  }

  public void setUiPresentationReturn(UiPresentationReturn uiPresentationReturn) {
    // The UI gave the idSelectionWrapper some input, time to wake up
    this.uiPresentationReturn = uiPresentationReturn;
    this.presentationTokenSelected = true;
    if (uiPresentationReturn == null) {
      this.canceled = true;
    }
  }
}
