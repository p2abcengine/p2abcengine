//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class TimingsLogger {
	private static boolean isLogging = false; //True to enable logging of timings
	private static boolean showIndention = false; //True to enable indention in the log file
	private static boolean isInitialized = false;
	private static int currentId;
	private static long startTime;
	private static HashMap<String, Long> startedFunctions;
	private static FileWriter writer;
	private static final String indentChar = ">";
	
	private static void startLogger() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		String fileName = "Timings " + dateFormat.format(new Date()) + ".csv";
		startedFunctions = new HashMap<String, Long>();

		try {
			writer = new FileWriter(fileName);
			writer.write("ID;Function;Time;Starting/stopping;Duration\n");
			writer.flush();
		} catch (IOException e) {
			System.out.println("Failed to open timings log: " + e);
			isLogging = false; //error
			return;
		}		

		currentId = 0;
		startTime = System.nanoTime();
		isLogging = true;
		isInitialized = true;
	}
	
	private static void stopLogger() {
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Failed to close timings log: " + e);
		}
		isLogging = false;		
	}
	
	/**
	 * Toggle the logger on/off.
	 * 
	 * @return The new state of the logger.
	 */
	public static boolean toogleLogger() {
		if (!isLogging) {
			//Toggle on
			startLogger();
		} else {
			//Toggle off
			stopLogger();
		}
		return isLogging;
	}
	
	public static void resetTimer() {
		startTime = System.nanoTime();
	}
	
	/**
	 * When called, this method saves a line in the timings log showing the
	 * given function and the duration since the logger was started (or
	 * resetTimer was called). When called to indicate that a function is
	 * stopping (starting = false), the duration since the corresponding start
	 * is also logged. Recursion cannot be handled since the function name has
	 * to be unique.
	 * 
	 * @param function
	 * @param starting
	 */
	public static void logTiming(String function, boolean starting) {
		if (!isLogging)
			return;
		
		if (!isInitialized)
			startLogger();
		
		long time = (System.nanoTime() - startTime) / 1000000;
		String line = function + ";" + time + "ms";
		
		if (starting) {
			startedFunctions.put(function, new Long(time));
			line += ";start;\n";
		} else {
			if (startedFunctions.containsKey(function)) {
				long duration = time - startedFunctions.get(function);
				startedFunctions.remove(function);
				line += ";stop;" + duration + "ms\n";
				System.out.println(">> Timing " + function + ": " + duration + " ms");
			} else {
				line += ";stop;n/a\n";
			}
		}

		try {
			String indention;
			if (showIndention) {
				if (starting)
					indention = getIndentionPrefix(startedFunctions.size() - 1);
				else
					indention = getIndentionPrefix(startedFunctions.size());
			} else
				indention = "";
			
			writer.write(currentId + ";" + indention + line);
			writer.flush();
		} catch (IOException e) {
			System.out.println("Failed to write timing to log: " + e);
		}
		
		currentId++;
	}
	
	private static String getIndentionPrefix(int depth) {		
		String indent = "";
		int i;
		for (i=0; i<depth; i++)
			indent += indentChar;
		return indent;
	}
}
