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

package eu.abc4trust.abce.integrationtests.user.credentialmanager;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.abce.internal.user.credentialManager.ImageCache;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheImpl;
import eu.abc4trust.abce.internal.user.credentialManager.ImageCacheStorage;
import eu.abc4trust.abce.internal.user.credentialManager.PersistentImageCacheStorage;
import eu.abc4trust.abce.testharness.ImagePathBuilder;
import eu.abc4trust.util.ImageTestUtil;
import eu.abc4trust.util.TemporaryFileFactory;

public class ImageCacheImplTest {

    @Test
    public void storeAndGetImageTest() throws Exception {
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        URI image = ImagePathBuilder.TEST_IMAGE_JPG;
        URL url = imCache.storeImage(image);
        File urlFile = new File(url.getPath());
        File file1 = new File(ImagePathBuilder.TEST_IMAGE_JPG_STRING);
        assertTrue(ImageTestUtil.compareImages(file1, urlFile));
    }

    @Test
    @Ignore
    public void storeAndGetImageFailureTest() throws Exception {
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        URI image = URI.create("http://error");
        URL url = imCache.storeImage(image);
        File urlFile = new File(url.getPath());
        assertTrue(ImageTestUtil.compareImages(new File(ImagePathBuilder.DEFAULT_IMAGE), urlFile));
    }

    @Test
    public void getDefaultImageTest() throws Exception {
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        URL url = imCache.getDefaultImage();
        File urlFile = new File(url.getPath());
        assertTrue(ImageTestUtil.compareImages(new File(ImagePathBuilder.DEFAULT_IMAGE), urlFile));
    }

    @Test
    public void testFiletype() throws Exception {
        Random random = new Random(42);
        ImageCacheStorage imStore = new PersistentImageCacheStorage(
                TemporaryFileFactory.createTemporaryDir(), random,
                ImagePathBuilder.DEFAULT_IMAGE);

        ImageCache imCache = new ImageCacheImpl(imStore);

        URI imageJpg = ImagePathBuilder.TEST_IMAGE_JPG;
        URL urlJpg = imCache.storeImage(imageJpg);
        assertTrue(urlJpg.getFile().endsWith(".jpg"));

        URI imagePng = ImagePathBuilder.TEST_IMAGE_PNG;
        URL urlPng = imCache.storeImage(imagePng);
        assertTrue(urlPng.getFile().endsWith(".png"));
    }
}
