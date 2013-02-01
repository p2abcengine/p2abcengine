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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
//import java.util.Map;

public class UProveLauncher {

    private static final String MONO = "mono";
    private static final String ABC4_TRUST_U_PROVE_EXE = "ABC4Trust-UProve.exe";
    private final String[] NON_WINDOWS_COMMAND;
    private final String[] WINDOWS_COMMAND;

    private Process uproveProcess;
    private boolean startCalled = false;
    private final File workingDirectory;
    private Thread debugOutput;
    private DebugOutputCollector debugOutputCollector;

    /**
     * 
     * @param workingDirectory
     *            provide null to use the same working directory as the parent
     *            process.
     * @param pathToUProveExe
     *            relative or absolute path to the U-Prove executable.
     */
    @Inject
    public UProveLauncher(
            @Named("UProveWorkingDirectory") @Nullable File workingDirectory,
            @Named("PathToUProveExe") String pathToUProveExe) {
        String uproveExe = new File(pathToUProveExe, ABC4_TRUST_U_PROVE_EXE)
        .getAbsolutePath();

        this.NON_WINDOWS_COMMAND = new String[] { MONO, uproveExe };
        this.WINDOWS_COMMAND = new String[] { uproveExe };
        this.workingDirectory = workingDirectory;
    }

    public UProveLauncher() {
        throw new IllegalStateException("Never make manual construct");
    }

    public void start(int port, String name) {
        startCalled = true;
        // System.out.println("UProveLauncher.start - instance : " + this + " - port : " + launchName + " :" + launchPort + " - is stopped == " + stopped + " - uproveProcess " + this.uproveProcess );
        ProcessBuilder processBuilder;
        if (this.isWindows()) {
            processBuilder = new ProcessBuilder(this.WINDOWS_COMMAND);
        } else {
            processBuilder = new ProcessBuilder(this.NON_WINDOWS_COMMAND);
        }
        processBuilder.command().add("" + port);
        //Map<String, String> env = processBuilder.environment();
        //env.clear();

        processBuilder.directory(this.workingDirectory);
        try {
            this.uproveProcess = processBuilder.start();
            InputStream is = this.uproveProcess.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            boolean done = false;
            while (!done) {
                line = br.readLine();
                if (line != null) {
                    System.out.println(line);
                    done = line.equals("Press Enter to exit");
                } else {
                    System.out.println("UProveLauncher - we get null on stdout from process - process has died..");
                    break;
                }
            }
            this.debugOutputCollector = new DebugOutputCollector(
                    this.uproveProcess, name);
            this.debugOutput = new Thread(this.debugOutputCollector);
            this.debugOutput.start();
            // System.out.println("process started");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    public int stop() {
        // System.out.println("UProveLauncher.stop instance : " + this + " : " + launchName + " - port : " + launchPort + " - is stopped == " + stopped + " - process : " + this.uproveProcess);
        if (this.uproveProcess != null) {
            try {
                OutputStream outputStream = this.uproveProcess.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                        outputStream);
                BufferedWriter out = new BufferedWriter(outputStreamWriter);
                out.newLine();
                out.flush();
                int exitCode = -1;
                boolean done = false;
                while (!done) {
                    try {
                        exitCode = this.uproveProcess.exitValue();
                        done = true;
                        break;
                    } catch (IllegalThreadStateException ex) {
                        // System.out.println("process not terminated");
                        this.waitFor(1);
                    }
                }
                this.debugOutputCollector.stop();
                this.debugOutput.interrupt();
                this.uproveProcess = null;
                return exitCode;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(startCalled) {
            return Integer.MIN_VALUE;
        } else {
            return 0;
        }
    }

    public void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getOutput() {
        // System.out.println("UProveLauncher.getOutput for instance : " + this + " : " + launchName + " - port : " + launchPort + " - is stopped == " + stopped + " : " + debugOutputCollector);
      if(uproveProcess!=null && debugOutputCollector!=null) {
          return this.debugOutputCollector.getCurrentDebugOutput();
      } else {
          List<String> notattaced = new ArrayList<String>();
          notattaced.add("Launcher not attached to process - No debug output");
          return notattaced;
      }
    }

}
