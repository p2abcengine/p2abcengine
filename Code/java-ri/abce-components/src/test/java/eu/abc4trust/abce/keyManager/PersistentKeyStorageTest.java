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

package eu.abc4trust.abce.keyManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;

import org.junit.Test;

import eu.abc4trust.keyManager.PersistentKeyStorage;

public class PersistentKeyStorageTest {

    private static final int SEED = 1234567;
    private static final String ERROR_STRING_KEYS_NOT_EQUAL = "The key \"%s\" stored is not the same as the retreived key \"%s\".";
    private static final String EXTENSION = ".key";
    private static final String FILENAME = "persistentKeyStorage";
    private static final String TESTSTRING = "test";

    @Test
    public void testPersistentKeyStorageTest() throws Exception {
        File temp = File.createTempFile(FILENAME, EXTENSION);
        temp.deleteOnExit();
        final PersistentKeyStorage storage = new PersistentKeyStorage(temp);

        URI uri = new URI(TESTSTRING);
        byte[] key = new byte[1024];
        Random random = new Random(SEED);
        random.nextBytes(key);

        storage.addValue(uri, key);

        byte[] storedKey = storage.getValue(uri);

        assertEquals(key.length, storedKey.length);

        for (int inx = 0; inx < storedKey.length; inx++) {
            if (key[inx] != storedKey[inx]) {
                fail(this.formatString(key, storedKey));
            }
        }

    }

    private String formatString(byte[] key, byte[] storedKey) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format(ERROR_STRING_KEYS_NOT_EQUAL, Arrays.toString(key),
                Arrays.toString(storedKey));
        String string = sb.toString();
        formatter.close();
        return string;
    }

    @Test
    public void testPersistentKeyStorageMultipleKeys() throws Exception {
        File temp = File.createTempFile(FILENAME, EXTENSION);
        temp.deleteOnExit();
        final PersistentKeyStorage storage = new PersistentKeyStorage(temp);

        Random random = new Random(SEED);
        int numberOfKeysToUseInTest = 3;

        byte[][] keys = new byte[numberOfKeysToUseInTest][1024];
        URI uri;
        for (int inx = 0; inx < numberOfKeysToUseInTest; inx++) {
            uri = new URI(TESTSTRING + inx);
            random.nextBytes(keys[inx]);
            storage.addValue(uri, keys[inx]);
        }

        for (int inx = numberOfKeysToUseInTest - 1; inx >= 0; inx--) {
            uri = new URI(TESTSTRING + inx);
            byte[] storedKey = storage.getValue(uri);

            assertEquals(this.formatString(keys[inx], storedKey),
                    keys[inx].length,
                    storedKey.length);

            for (int inj = 0; inj < storedKey.length; inj++) {
                if (keys[inx][inj] != storedKey[inj]) {
                    fail(this.formatString(keys[inx], storedKey));
                }
            }
        }

    }

    @Test
    public void testListUris() throws Exception {
        File temp = File.createTempFile(FILENAME, EXTENSION);
        temp.deleteOnExit();
        final PersistentKeyStorage storage = new PersistentKeyStorage(temp);

        Random random = new Random(SEED);
        int numberOfKeysToUseInTest = 3;

        byte[][] keys = new byte[numberOfKeysToUseInTest][1024];
        URI uri;
        for (int inx = 0; inx < numberOfKeysToUseInTest; inx++) {
            uri = new URI(TESTSTRING + inx);
            random.nextBytes(keys[inx]);
            storage.addValue(uri, keys[inx]);
        }

        URI[] uris = storage.listUris();

        for (int inx = 0; inx >= uris.length; inx--) {
            uri = new URI(TESTSTRING + inx);
            if (!uri.equals(uris[inx])) {
                StringBuilder sb = new StringBuilder();
                Formatter formatter = new Formatter(sb);
                formatter.format("Uri %s is not equal to %s",
                        uris[inx].toString(), uri.toString());
                String string = sb.toString();
                formatter.close();
                fail(string);
            }
        }

    }
}
