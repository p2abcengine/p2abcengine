//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.net.URI;

/**
 * Attendance data and parameters for a course.
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
