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

package eu.abc4trust.cryptoEngine.uprove.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class DebugOutputCollector implements Runnable {

    private final Process uproveProcess;
    private final List<String> currentDebugOutput;
    private final String name;
    private volatile boolean stopped = false;

    public DebugOutputCollector(Process uproveProcess, String name) {
        this.uproveProcess = uproveProcess;
        this.currentDebugOutput = new LinkedList<String>();
        this.name = name;
    }

    @Override
    public void run() {
        InputStream is = this.uproveProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        InputStream errors = this.uproveProcess.getErrorStream();
        InputStreamReader errorsReader = new InputStreamReader(errors);
        BufferedReader bufferedErrorReader = new BufferedReader(errorsReader);
        String errorLine;
        try {
            while (!this.stopped) {
                if ((line = br.readLine()) != null) {
                    if (this.stopped || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    this.addDebug(line);
                    System.out.println("!>>" + this.name + ">" + line);
                }

                if ((errorLine = bufferedErrorReader.readLine()) != null) {
                    if (this.stopped || Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    this.addDebug(errorLine);
                    System.out.println("!>>" + this.name + ">" + errorLine);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private synchronized void addDebug(String line) {
        this.currentDebugOutput.add(line);
    }

    public synchronized List<String> getCurrentDebugOutput() {
        return this.currentDebugOutput;
    }

    public void stop() {
        this.stopped = true;
        //        Thread.currentThread().interrupt();
    }
}
