//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.abce.benchmarks;

import java.util.logging.Logger;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.SystemParameters;

public class SystemParametersHelper {
    private static final Logger logger = Logger
            .getLogger(SystemParametersHelper.class.toString());

    public static SystemParameters checkAndLoadSystemParametersIfAbsent(
            KeyManager keyManager, String systemParametersStoragePath)
                    throws KeyManagerException {
        SystemParameters systemParameters = checkIfSystemParametersAreLoaded(keyManager);

        if (systemParameters != null) {
            return systemParameters;
        }

        systemParameters = loadSystemParameters(keyManager,
                systemParametersStoragePath);

        if (systemParameters != null) {
            return systemParameters;
        }

        return null;
    }

    /**
     * Check if system parameters are available in the 'file system' Return true
     * if the system parameters are loaded from disk. And false otherwise.
     * 
     * @param fileStoragePrefix
     * @param keyManager
     * @param systemParametersStoragePath
     * @return
     * @throws KeyManagerException
     */
    private static SystemParameters loadSystemParameters(KeyManager keyManager,
            String systemParametersStoragePath) throws KeyManagerException {
      // TODO(enr): systemParametersStoragePath is ignored
      SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_2048();
      keyManager.storeSystemParameters(systemParameters);
      return systemParameters;
    }

    private static SystemParameters checkIfSystemParametersAreLoaded(
            KeyManager keyManager) throws KeyManagerException {
        SystemParameters systemParameters = null;
        if (keyManager.hasSystemParameters()) {
            logger.info("- system parameters already in keyManager");
            systemParameters = keyManager.getSystemParameters();
            return systemParameters;
        }
        return null;
    }
}
