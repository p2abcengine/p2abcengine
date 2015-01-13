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

package eu.abc4trust.guice.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.junit.Test;

import eu.abc4trust.util.TemporaryFileFactory;

public class ConfigurationParserTest {

    @Test
    public void testPropertiesConfiguration() throws IOException {
        File tmp = TemporaryFileFactory.createTemporaryFile();
        String filename = tmp.getAbsolutePath();

        String credentialFilename = "credentials.file";
        String keystorageFilename = "keystorage.file";
        String tokensFilename = "tokens.file";
        String pseudonymsFilename = "pseudonyms.file";
        String defaultImagePath = "default.jpg";
        String imageCacheDir = "image_cache_dir";

        FileWriter writer = new FileWriter(tmp);
        writer.write(" credentialstorage_file" + "=" + credentialFilename
                + "\n");
        writer.write("keystorage_file" + " = " + keystorageFilename + "\n\n");
        writer.write("  pseudonyms_file" + "   =    " + pseudonymsFilename
                + "    \n");
        writer.write("tokenstorage_file" + "=" + tokensFilename + "\n");
        writer.write("default_image_path" + "=" + defaultImagePath + "\n");
        writer.write("image_cache_dir" + "=" + imageCacheDir + "\n");
        writer.close();

        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(filename);

            config.setReloadingStrategy(new FileChangedReloadingStrategy());

            ConfigurationParser configurationParser = new ConfigurationParser(config);
            AbceConfigurationImpl abceConfiguration = new AbceConfigurationImpl();
            configurationParser.loadConfiguration(abceConfiguration);
            assertEquals(abceConfiguration.getCredentialFile().getName(),
                    credentialFilename);
            assertEquals(abceConfiguration.getKeyStorageFile().getName(),
                    keystorageFilename);
            assertEquals(abceConfiguration.getPseudonymsFile().getName(),
                    pseudonymsFilename);
            assertEquals(abceConfiguration.getTokensFile().getName(),
                    tokensFilename);
            assertEquals(abceConfiguration.getDefaultImagePath(),
                    defaultImagePath);
            assertEquals(abceConfiguration.getImageCacheDir().getName(),
                    imageCacheDir);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Could not load property configuration",
                    e);
        }
    }
}
