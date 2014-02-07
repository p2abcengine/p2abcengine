//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.cryptoEngine.uprove.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class UProveLauncherTest {

    @Ignore// HGK - UProve Service could be started externally - so done try to start and stop..
    @Test
    public void test() {
        UProveUtils util = new UProveUtils();
        File f = util.getPathToUProveExe();
        String pathToUProveExe = f.getAbsolutePath();
        UProveLauncher uproveLauncher = new UProveLauncher(null,
                pathToUProveExe);
        uproveLauncher.start(util.getIssuerServicePort(), "foo");
        uproveLauncher.waitFor(2);
        int exitCode = uproveLauncher.stop();
        assertEquals("U-Prove exe must have exit code 0", 0, exitCode);
    }

}
