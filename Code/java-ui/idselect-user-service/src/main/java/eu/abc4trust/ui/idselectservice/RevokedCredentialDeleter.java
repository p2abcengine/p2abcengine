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
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.smartcard.AbcSmartcardDetector;

public class RevokedCredentialDeleter implements Runnable {

    private final long sleepTime;
    private final Logger logger;
    private final UserAbcEngine engine;
    private volatile boolean alive;
    private volatile Exception exception;

    public RevokedCredentialDeleter(UserAbcEngine engine, long sleepTime) {
        super();
        this.engine = engine;
        this.sleepTime = sleepTime;
        this.logger = Logger.getLogger(AbcSmartcardDetector.class
                .getCanonicalName());
    }

    @Override
    public void run() {
        this.alive = true;
        boolean done = Thread.currentThread().isInterrupted();
        while (!done) {
            try {
                this.logger.log(Level.INFO, "Checking for revoked credentials");
                try {
                    for (URI credUri : this.engine.listCredentials(UserService.USER_NAME)) {
                        if (this.engine.isRevoked(UserService.USER_NAME, credUri)) {
                            this.logger.log(Level.INFO,
                                    "Deleting revoked credential: " + credUri);
                            this.engine.deleteCredential(UserService.USER_NAME, credUri);
                            this.logger.log(Level.INFO,
                                    "Deleted revoked credential: " + credUri);
                        }
                    }
                    Thread.sleep(this.sleepTime);
                    done = Thread.currentThread().isInterrupted();
                } catch (CredentialManagerException ex) {
                    done = true;
                    this.exception = ex;
                } catch (CryptoEngineException ex) {
                    done = true;
                    this.exception = ex;
                }
            } catch (InterruptedException ex) {
                done = true;
            }
        }
        this.alive = false;
    }

    public synchronized boolean isAlive() {
        return this.alive;
    }

    public synchronized Exception getException() {
        return this.exception;
    }

    public void stop() {
        Thread.currentThread().interrupt();
    }
}
