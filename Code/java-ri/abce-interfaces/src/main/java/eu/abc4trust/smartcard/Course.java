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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.net.URI;

/**
 * Attendance data and parameters for a course.
 * @author enr
 *
 */
public class Course implements Serializable {
  // If updated - change serialVersionUID
  private static final long serialVersionUID = 1L;

  //
  public final URI issuerUri;    
  
  private int keyID;  
  private int minAttendance;
  private int lectureCount;
  private int lastLectureId;
  private boolean activated;
  private int courseID;  
  
  Course(int courseID, URI issuerUri, int minAttendance, int keyID) {
	this.courseID = courseID;
    this.issuerUri = issuerUri;
    this.lastLectureId = 0;
    this.lectureCount = 0;
    this.minAttendance = minAttendance;
    this.keyID = keyID;    
    this.activated = false;
  }

  public int getMinAttendance(){
	  return minAttendance;
  }
  
  public int getLectureCount() {
    return lectureCount;
  }
  
  public int getLastLectureId() {
    return lastLectureId;
  }
  
  public int getKeyID(){
	  return keyID;
  }
  
  public int getCourseID(){
	  return courseID;
  }
  
  public boolean isActivated(){
	  return activated;
  }
  
  public boolean updateLectureId(int lectureId) {
	  System.out.println("lectureId: " + lectureId +", compared to: "+lastLectureId+", and actiavated: "+activated);
    if (! activated) {
      // Don't increment the counter before any credential was issued
      return false;
    }
    if (this.lastLectureId >= lectureId) {
      // Don't increment the counter for past lectures and don't allow double-billing
      // for the current lecture
      return false;
    }
    this.lectureCount++;
    this.lastLectureId = lectureId;
    return true;
  }
  
  public boolean sufficientAttendance() {
    return lectureCount >= minAttendance;
  }
  
  public void activate() {
    activated = true;
  }  
  
  boolean applyBackup(int counterID, int newLectureCount, int newLastLectureID) {
    if( this.courseID != counterID) {
      return false;
    }    
    this.lectureCount = newLectureCount;
    this.lastLectureId = newLastLectureID;
    return true;
  }
}
