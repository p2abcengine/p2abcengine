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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentImageCacheStorage implements ImageCacheStorage {

    private final File baseDir;
    private final Random prng;
    private final String defaultImagePath;

    @Inject
    public PersistentImageCacheStorage(
            @Named("ImageCacheBaseDir") File imageCacheDir,
            @Named("RandomNumberGenerator") Random prng,
            @Named("DefaultImagePath") String defaultImagePath) {
        this.baseDir = imageCacheDir;
        this.prng = prng;
        this.defaultImagePath = defaultImagePath;
    }

    @Override
    public URL store(String filetype, InputStream inputStream)
            throws ImageCacheStorageException {
        URL url = null;
        try {
            String filename = this.getUniqueFilename();
            FileOutputStream writer;

            String path = this.baseDir.getAbsolutePath() + File.separatorChar
                    + filename + "." + filetype;

            url = new URL("file:////" + path);

            writer = new FileOutputStream(path);

            int size = 261120;
            byte[] buffer = new byte[size];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                buffer = new byte[size];
            }

            StorageUtil.closeIgnoringException(writer);
        } catch (Exception ex) {
            throw new ImageCacheStorageException(ex);
        }
        return url;
    }

    private String getUniqueFilename() {
        UUID uuid = Generators.randomBasedGenerator(this.prng).generate();
        return uuid.toString();

    }

    @Override
    public String getBasePath() {
        return this.baseDir.getAbsolutePath();
    }

    @Override
    public String getDefaultImagePath() {
        return this.defaultImagePath;
    }

}
