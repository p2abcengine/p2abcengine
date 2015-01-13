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
