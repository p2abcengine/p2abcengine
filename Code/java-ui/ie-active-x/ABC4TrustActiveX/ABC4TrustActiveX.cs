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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.Expando;
using System.IO;
using System.Net;
using Microsoft.Win32;
using System.Reflection;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{
    /// <summary>

    /// ABC4TrustActiveX

    /// </summary>

    [ProgId("ABC4TrustActiveX.BrowserHelperObject")]

    [ClassInterface(ClassInterfaceType.AutoDual)]

    [Guid("BC9BB501-2EF6-4293-84DF-58884DB385DC")]

    [ComVisible(true)]

    public class BrowserHelperObject : IOleCommandTarget, IObjectSafety
    {
        #region Registering with regasm
        public static string RegBHO = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Browser Helper Objects";
        public static string RegCmd = "Software\\Microsoft\\Internet Explorer\\Extensions";
        public static string TestRegCmd = "Software\\Microsoft\\Internet Explorer\\MenuExt";
/*
        public static String[] menuList = { "ABC4Trust Menu 1", "ABC4Trust Menu 2" };
        public static String[] guidList = { "{33B7DCAA-33AB-4F4E-9BF1-D084EE44BB7D}"
                    , "{02DDB7A8-62D6-4C3C-A8D3-D4F21D8D4F62}"
                    , "{09134075-64E0-4CA7-9865-7C84BBA223AB}"
                    , "{B11E4FDE-1CC9-4DCB-B207-EAD2398D9782}"
                    , "{B4FB0613-DC59-47FB-859E-51FCE4F97D22}"
                    , "{8AB0206D-342D-40B2-B525-D8F98B775B9E}"
                    , "{BACAD465-E299-48F0-81F7-1B3BA2CF0EC0}" };
        */

        private static string AllowActiveXPrefix = "Software\\Microsoft\\Windows\\CurrentVersion\\Ext\\Stats\\";
        private static string AllowActiveX = AllowActiveXPrefix +
        "\\{BC9BB501-2EF6-4293-84DF-58884DB385DC}\\iexplore\\AllowedDomains"; // {DOMAIN | *}

        private static string[] ActiveXAllowedDomains = { "abc4trust.se", "localhost" };

        [ComRegisterFunction]
        public static void RegisterActiveX(Type type)
        {
            string guid = type.GUID.ToString("B");
            log("BHO", "RegisterActiveX", "Type : " + type + " - GUID : " + guid);

            // BHO
            /*
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO, true);
                if (registryKey == null)
                    registryKey = Registry.LocalMachine.CreateSubKey(RegBHO);
                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                    key = registryKey.CreateSubKey(guid);
                key.SetValue("Alright", 1);
                //key.SetValue("NoExplorer", 1);
                registryKey.Close();
                key.Close();
            }
            */
            // Command
            {
                //                Registry
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegCmd, true);
                if (registryKey == null)
                    registryKey = Registry.LocalMachine.CreateSubKey(RegCmd);

                log("BHO", "RegisterActiveX", "Setup Menu : registryKey : " + registryKey);

                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                    key = registryKey.CreateSubKey(guid);

                log("BHO", "RegisterActiveX", "Create Key for Menu");

                key.SetValue("ButtonText", "ABC4Trust Tools");

                key.SetValue("CLSID", "{1FBA04EE-3024-11d2-8F1F-0000F87ABD16}");
                key.SetValue("ClsidExtension", guid);
                key.SetValue("Icon", "");
                key.SetValue("HotIcon", "");
                key.SetValue("Default Visible", "Yes");
                key.SetValue("MenuText", "&ABC4Trust Tools");
                key.SetValue("ToolTip", "ABC4Trust Tools");
                //key.SetValue("KeyPath", "no");

                //key.SetValue("MenuCustomize", "file");

                key.Close();
                registryKey.Close();

                log("BHO", "RegisterActiveX", "Menu Setup OK...");
            }
/*
            {
                log("BHO", "RegisterActiveX", "Allow Active X : " + AllowActiveX);

                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(AllowActiveX, true);
                if (registryKey == null) {
                    log("BHO", "RegisterActiveX", " - create key");
                    registryKey = Registry.LocalMachine.CreateSubKey(AllowActiveX);
                }
                log("BHO", "RegisterActiveX", " - registry key : " + registryKey);
                foreach(string domain in ActiveXAllowedDomains) {
                    RegistryKey key = registryKey.OpenSubKey(domain);
                    if (key == null) {
                        key = registryKey.CreateSubKey(domain);
                        log("BHO", "RegisterActiveX", " - create allow domain key : " + key);
                    }
                    else
                    {
                        log("BHO", "RegisterActiveX", " - domain already registered : " + key);
                    }
                    key.Close();
                }
                registryKey.Close();
            }
 */ 
        }

        [ComUnregisterFunction]
        public static void UnregisterActiveX(Type type)
        {
            string guid = type.GUID.ToString("B");
            log("BHO", "UnregisterActiveX", "Try to unregister Menu... for " + guid);
            // BHO
            /*
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO, true);
                if (registryKey != null)
                    registryKey.DeleteSubKey(guid, false);
            }
            */ 
            // Command
            {
                {
                    RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegCmd, true);
                    if (registryKey != null) {
                        RegistryKey key = registryKey.OpenSubKey(guid);
                        if (key != null) {
                            registryKey.DeleteSubKey(guid, false);
                        }
                    }
                }
                /*
                for(int i=0; i < menuList.Length; i++) {
                    String menuKey = menuList[i];
                    String menuGuid = guidList[i];

                    RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegCmd, true);
                    if (registryKey != null) {
                        RegistryKey key = registryKey.OpenSubKey(menuGuid); //guid);
                        if (key != null) {
                            log("BHO", "UnregisterActiveX", " - delete menu : " + menuKey + " - with guid : " + menuGuid);
                            registryKey.DeleteSubKey(menuGuid, false);
                        }
                    }
                }
                 */

                log("BHO", "UnregisterActiveX", "Menu Unregistered OK...");
                {

                    RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(AllowActiveXPrefix, true);
                    if (registryKey != null)
                    {
                        log("BHO", "UnregisterActiveX", "Remove Allow Active X : " + AllowActiveXPrefix + " : " + guid);
                        RegistryKey key = registryKey.OpenSubKey(guid);
                        if (key != null)
                        {
                            log("BHO", "UnregisterActiveX", " - remove Allowd domains for.");
                            registryKey.DeleteSubKeyTree(guid, false);
                            log("BHO", "UnregisterActiveX", " - remove Allowd domains for DONE.");
                        }
                        log("BHO", "UnregisterActiveX", "Remove Allow Active X - OK...");
                    }
                }
            }
        }
        #endregion


        #region Implementation of IOleCommandTarget
        public int QueryStatus(IntPtr pguidCmdGroup, uint cCmds, ref OLECMD prgCmds, IntPtr pCmdText)
        {
            log("BHO.IOleCommandTarget", "QueryStatus", "...");

            return 0;
        }
        public int Exec(IntPtr pguidCmdGroup, uint nCmdID, uint nCmdexecopt, IntPtr pvaIn, IntPtr pvaOut)
        {
//            log("BHO.IOleCommandTarget", "Exec", "Params : " + pguidCmdGroup + " " + nCmdID + " " + nCmdexecopt + " " + pvaIn + " " + pvaOut);
            if(nCmdID != 0) 
            {
                // not called from menu!
//                log("BHO.IOleCommandTarget", "Exec", "Exec was not called from Menu! nCmdID != 0 - was : " + nCmdID);
                return 0;
            }
            log("BHO.IOleCommandTarget", "Exec", "Params : " + pguidCmdGroup + " " + nCmdID + " " + nCmdexecopt + " " + pvaIn + " " + pvaOut);

            log("BHO.IOleCommandTarget", "Exec", "Exec Was called from Menu! nCmdID == 0");

            ToolsForm toolsForm = new ToolsForm();

            string pin = null;
            var res = toolsForm.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK)
            {
                // pin not entered
                log("BHO.IOleCommandTarget", "Exec", " - user canceled..." + res);
            }
            else
            {
                int tag = int.Parse("" + toolsForm.tag);
                log("BHO.IOleCommandTarget", "Exec", " - user selected : " + toolsForm.tag + " : " + toolsForm.sender + " - select button/tag : " + tag);
                switch (tag)
                {
                    case 1 :
                        // Manager credentials
                        ManageCredentials();
                        break;

/*
                    case 2:
                        // Backup Smartcard
                        MessageBox.Show("Backup Smartcard",
                            "Not Implemented Yet!",
                            MessageBoxButtons.OK,
                            MessageBoxIcon.Exclamation,
                            MessageBoxDefaultButton.Button1);
                        break;
                    case 3:
                        // Restore Smartcard
                        MessageBox.Show("Restore Smartcard",
                            "Not Implemented Yet!",
                            MessageBoxButtons.OK,
                            MessageBoxIcon.Exclamation,
                            MessageBoxDefaultButton.Button1);
                        break;
 */ 
                    case 4:
                        // Change Pin
                        ChangePin();
                        break;
                    case 5:
                        // Unlock Smartcard
                        UnlockCard();
                        break;
                    case 6:
                        // Debug Info
                        ShowDebugInfo();
                        break;
                    case 7:
                        // Debug Info
                        CheckRevocationStatus();
                        break;
                    default:
                        MessageBox.Show("Select User Selected - event args : " + toolsForm.e + " - sender : " + toolsForm.sender + " - tag " + toolsForm.tag);
                        break;

                }
            }

            log("BHO.IOleCommandTarget", "Exec", " - exec DONE!! " + res);
            return 0;
        }
        #endregion

        #region Implementation of IObjectSafety

        private const int INTERFACESAFE_FOR_UNTRUSTED_CALLER = 0x00000001;
        private const int INTERFACESAFE_FOR_UNTRUSTED_DATA = 0x00000002;
        private const int S_OK = 0;

        public int GetInterfaceSafetyOptions(ref Guid riid, out int pdwSupportedOptions, out int pdwEnabledOptions)
        {
            pdwSupportedOptions = INTERFACESAFE_FOR_UNTRUSTED_CALLER | INTERFACESAFE_FOR_UNTRUSTED_DATA;
            pdwEnabledOptions = INTERFACESAFE_FOR_UNTRUSTED_CALLER | INTERFACESAFE_FOR_UNTRUSTED_DATA;
//            log("BHO.IObjectSafety", "GetInterfaceSafetyOptions", "Params : " + riid + " " + pdwSupportedOptions + " " + pdwEnabledOptions);
            return S_OK;
        }

        public int SetInterfaceSafetyOptions(ref Guid riid, int dwOptionSetMask, int dwEnabledOptions)
        {
//            log("BHO.IObjectSafety", "SetInterfaceSafetyOptions", "Params : " + riid + " " + dwOptionSetMask + " " + dwEnabledOptions);
            return S_OK;
        }
        #endregion



        [ComVisible(true)]
        public String SayHello()
        {

            return "Hello World!";

        }
/*
        [ComVisible(true)]
        public ReturObject Test(string s)
        {
            log("BHO", "Test", "Echo : " + s);
            //Process.Start("notepad.exe");
            return new ReturObject
            {
                message =
                    "This is a string from the Browser Helper Object" + Environment.NewLine + "The input parameter was: " +
                    s,
                status = 42
            };
        }

        [ComVisible(true)]
        public ReturObject ShowSite(string url)
        {
            log("BHO", "ShowSite", "Open : " + url);
            var form = new WebForm { URL = url };
            form.ShowDialog();
            return new ReturObject { message = "ShowSite OK!", status = 4224 };
        }
*/
        public static string USER_ABCE_SERVICE = "http://localhost:9300/idselect-user-service";
        public static string USER_UI_SERVICE = "http://localhost:9093/user-ui";

        public static bool enableLogging = true;
        private static StreamWriter logWriter = null;
        public static void log(string clazz, string method, string msg)
        {
            try
            {
                if (enableLogging)
                {
                    if (logWriter == null) initLogger();

                    logWriter.WriteLine(clazz + "::" + method + " : " + msg);

                }
            }
            catch(Exception ignore)
            {
                // MessageBox.Show("LOG FAILED : " + clazz + "::" + method + " : " + msg, "Log failed!");
            }

        }
        private static void initLogger()
        {
            string homePath = (Environment.OSVersion.Platform == PlatformID.Unix ||
                   Environment.OSVersion.Platform == PlatformID.MacOSX)
                ? Environment.GetEnvironmentVariable("HOME")
            : Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");

//            homePath = "C:\\Users\\default.Win7x64";
            logWriter = File.AppendText(homePath + "\\abc4trust_activex.txt");
            logWriter.AutoFlush = true;

        }


        // *****************************************************

        private ReturObject CheckSmartCard(string sessionID)
        {
            log("BHO", "CheckSmartCard", "SessionID : " + sessionID);

            HttpWebResponse abcResponse = null;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
            abcRequest.Method = "GET";
            try
            {
                log("BHO", "CheckSmartCard", " - call rest : " + USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                if (abcResponse.StatusCode == HttpStatusCode.NoContent)
                {
                    log("BHO", "CheckSmartCard", " - ' no content ' we have same card! " + abcResponse.StatusCode);
                    // we have card...
                    return null;
                }
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        int statusCodeX = (int)response.StatusCode;
                        log("BHO", "CheckSmartCard", " - statusCode calling checkSmartcard : " + statusCodeX);
                    }
                    else
                    {
                        log("BHO", "CheckSmartCard", "Uknown Response calling checkSmartcard : " + e);
                        return new ReturObject { status = 400, message = "Uknown Response calling checkSmartcard : " + e };
                    }
                }
                else
                {
                    log("BHO", "CheckSmartCard", "could not contact User ABC Service : " + e);
                    return new ReturObject { status = 400, message = "User ABCE Service Not Running" };
                }

                // could not contact User ABC Service
            }

            // smartcard status
            abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/smartcardStatus");
            abcRequest.Method = "GET";
            try
            {
                log("BHO", "CheckSmartCard", " - call rest smartcardStatus");
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                log("BHO", "CheckSmartCard", " - status ok : " + abcResponse.StatusCode);
            }
            catch (Exception e)
            {
                // could not contact User ABC Service
                log("BHO", "CheckSmartCard", "Internal User ABCE Service Error : " + e);
                return new ReturObject { status = 400, message = "Internal User ABCE Service Error" };
            }

            // todo : pin dialog
            log("BHO", "CheckSmartCard", " - show dialog!!");
            PinForm pf = new PinForm();

            string pin = null;
            var res = pf.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK)
            {
                // pin not entered
                log("BHO", "CheckSmartCard", " - user canceled..." + res);
                return new ReturObject { status = 400, message = "Pin not entered" };
            }
            else
            {
                pin = pf.getPin();
                if(pin.Length != 4) 
                {
                    return new ReturObject { status = 400, message = "Pin must be 4 digits." };
                }
                log("BHO", "CheckSmartCard", " - user OK! " + res);
            }

            log("BHO", "CheckSmartCard", " - show dialog DONE!! " + res);

            // unlock
            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] pinBytes = encoding.GetBytes(pin);

            abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/unlockSmartcards/" + sessionID);
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                log("BHO", "CheckSmartCard", " - rest call 'unlockSmartard'");
                abcRequestStream.Write(pinBytes, 0, pinBytes.Length);
                abcRequestStream.Close();
                abcRequestStream.Dispose();

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                statusCode = (int)abcResponse.StatusCode;
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        statusCode = (int)response.StatusCode;
                    }
                    else
                    {
                        return new ReturObject { status = 400, message = "Uknown Response calling unlock : : " + e };
                    }
                }
                else
                {
                    return new ReturObject { status = 400, message = "Uknown Response calling unlock, not ProtocolError: " + e };
                }
            }
            finally
            {
                if (abcResponse != null) abcResponse.Close();
            }

            if (statusCode >= 200 && statusCode <= 204)
            {
                // PIN OK!
                log("BHO", "CheckSmartCard", " - PIN OK!");
                return null;
            }
            if (statusCode == 401)
            {
                log("BHO", "CheckSmartCard", "Smartcard Not Accepted - wrong PIN entered. Try again");
                return new ReturObject { status = statusCode, message = "Smartcard Not Accepted - wrong PIN entered. Try again" };
            }
            if (statusCode == 403)
            {
                log("BHO", "CheckSmartCard", "Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code");
                return new ReturObject { status = statusCode, message = "Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code" };
            }
            log("BHO", "CheckSmartCard", "Smartcard Not Accepted - Unknown cause " + statusCode);
            return new ReturObject { status = statusCode, message = "Smartcard Not Accepted - Unknown cause" };
        }

        [ComVisible(true)]
        public ReturObject IssueNoCookie(string language, string start_url, string step_url, string status_url)
        {
            return Issue(language, start_url, step_url, status_url, null);
        }

        [ComVisible(true)]
        public ReturObject Issue(string language, string start_url, string step_url, string status_url, string optional_cookie)
        {

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "IssuanceIE" + milliseconds;

            log("BHO", "Issue", "Start : " + language + " " + start_url + " " + step_url + " " + status_url);

            // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error != null) return error;

            // These URLs are for re-isuance. Encode/Escape them before handing over to ABCE service.
            var start_request_mod = Uri.EscapeUriString(start_url);
            var step_request_mod = Uri.EscapeUriString(step_url);

            //
            //
            var xmlPolicyRequest = (HttpWebRequest)WebRequest.Create(start_url);
            xmlPolicyRequest.Method = "POST";
            xmlPolicyRequest.Accept = "text/xml,application/xml";
            xmlPolicyRequest.KeepAlive = false;
            xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout = 5000;
            xmlPolicyRequest.ServicePoint.MaxIdleTime = 5000;
            xmlPolicyRequest.ServicePoint.ConnectionLimit = 400;
            log("BHO", "Issue", "- xmlPolicyRequest.ServicePoint : " + xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlPolicyRequest.ServicePoint.ConnectionLimit);

            // set cookie
            if (! String.IsNullOrEmpty(optional_cookie))
            {
                xmlPolicyRequest.Headers.Add("Cookie", optional_cookie);
                log("BHO", "Issue", "- Set Cookie : " + optional_cookie);
            }
            else
            {
                log("BHO", "Issue", "- No Cookie");
            }

            HttpWebResponse xmlResponse;
            try
            {
                log("BHO", "Issue", "- Try to get initial issuance message - Timeout : " + xmlPolicyRequest.ReadWriteTimeout);
                xmlResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                log("BHO", "Issue", "- Contacted Issuer...");
            }
            catch (WebException e)
            {
                log("BHO", "Issuer", "- WebException - getting Issuance Policy : " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        return new ReturObject { status = 500, message = "Unhandled error : " + e.Status };
                    }

                }
                else
                {
                    return new ReturObject { status = 500, message = "Could not contact issuanceserver." };
                }
            }
            if (xmlResponse.StatusCode != HttpStatusCode.OK)
            {
                log("BHO", "Issuer", "- Failed to obtain issuance policy from issuance server " + xmlResponse.StatusCode + " " + xmlResponse);

                return new ReturObject { status = (int)xmlResponse.StatusCode, message = "Failed to obtain issuance policy from issuance server" };
            }
            log("BHO", "Issuer", "- OK From Issuance Policy from Server " + xmlResponse.StatusCode + " " + xmlResponse);

            var xmlFromStream = xmlResponse.GetResponseStream();
            log("BHO", "Issuer", "- got Response Stream! " + xmlFromStream);

            while (true)
            {
                // read from issuer - write to local abc
                var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/issuanceProtocolStep/" + sessionID +
                   "?startRequest=" + start_request_mod +
                    "&stepRequest=" + step_request_mod);
                abcRequest.Method = "POST";
                abcRequest.Accept = "text/xml,application/xml";
                abcRequest.ContentType = "application/xml";
                Stream abcToStream; // = abcRequest.GetRequestStream();
                try
                {
                    abcToStream = abcRequest.GetRequestStream();
                }
                catch (WebException e)
                {
                    log("BHO", "Issuer", "- WebException - on step - contacting ABCE : " + e);
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            int statusCode = (int)response.StatusCode;
                            if(statusCode == 501) 
                            {
                                // specific check for insufficient storage. 
                                MessageBox.Show("Insufficient storage on card. Please try to use check revocation status from the ABC4Trust menu.",
                                    "Smart Card Error!",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Exclamation,
                                    MessageBoxDefaultButton.Button1);

                                return new ReturObject { status = 501, message = "Insufficient storage on card." };
                            }
                            else
                            {
                                return new ReturObject { status = statusCode, message = response.StatusDescription };
                            }
                        }
                        else
                        {
                            return new ReturObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ReturObject { status = 400, message = "User ABCE Not Running (Issuance)" };
                    }
                } 

                log("BHO", "Issuer", "- got Request Stream! " + abcToStream);
                MemoryStream issuer2ABCEStream = new MemoryStream();
                byte[] b = new byte[1];
                using (BinaryWriter writer = new BinaryWriter(issuer2ABCEStream))
                {
                    while (xmlFromStream.Read(b, 0, 1) != 0)
                    {
                        writer.Write(b);
                    }
                    writer.Close();
                }
                xmlFromStream.Close();
                xmlFromStream.Dispose();

                issuer2ABCEStream.Close();
                byte[] issuer2ABCEBytes = issuer2ABCEStream.ToArray();
//                var issuer2ABCEString = new System.Text.UTF8Encoding().GetString(issuer2ABCEBytes);
//                log("BHO", "Issuer", "- xml from Issuer - length : " + issuer2ABCEBytes.Length + " : " + issuer2ABCEString);
                log("BHO", "Issuer", "- xml from Issuer - length : " + issuer2ABCEBytes.Length);

                abcToStream.Write(issuer2ABCEBytes, 0, issuer2ABCEBytes.Length);
                abcToStream.Close();

                log("BHO", "Issuer", "- get status from ABCE UserService...");
                HttpWebResponse abcResponse;
                try 
                {
                    abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                }
                catch (WebException e)
                {
                    log("BHO", "Issuer", "- WebException - on step - sending to ABCE : " + e);
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            int statusCode = (int)response.StatusCode;
                            if(statusCode == 501) 
                            {
                                // specific check for insufficient storage. 
                                MessageBox.Show("Insufficient storage on card. Please try to use check revocation status from the ABC4Trust menu.",
                                    "Smart Card Error!",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Exclamation,
                                    MessageBoxDefaultButton.Button1);

                                return new ReturObject { status = 501, message = "Insufficient storage on card." };
                            }
                            else
                            {
                                return new ReturObject { status = statusCode, message = response.StatusDescription };
                            }
                        }
                        else
                        {
                            return new ReturObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ReturObject { status = 400, message = "User ABCE Not Running (Issuance)" };
                    }
                } 

                var abcStatus = (int)abcResponse.StatusCode;
                log("BHO", "Issue", "- response from user service : " + abcResponse + " " + abcStatus);

                if (abcStatus != 200 && abcStatus != 203 && abcStatus != 204)
                {
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    return new ReturObject { status = abcStatus, message = "Local ABC engine did not return an Issuancemessage." };
                }

                // we now have step response from User ABCE...
                if (abcStatus == 203)
                {
                    // select credentials
                    abcResponse.Close();
                    var selectionUIURL = USER_UI_SERVICE + "?mode=issuance&demo=false&language=" + language + "&sessionid=" + sessionID;
                    log("BHO", "Issue", "- selectionUIURL : " + selectionUIURL);

                    var form = new WebForm { URL = selectionUIURL };

                    var dialogResponse = form.ShowDialog();
                    log("BHO", "Issue", "- dialogResponse : " + dialogResponse);

                    // get presentation token... maybe...
                    abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/issuanceProtocolStepSelect/" + sessionID +
                        "?startRequest=" + start_request_mod +
                        "&stepRequest=" + step_request_mod);
                    abcRequest.Method = "POST";
                    abcRequest.Accept = "text/xml,application/xml";
                    abcRequest.ContentType = "text/plain";

                    abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                    abcStatus = (int)abcResponse.StatusCode;
                    log("BHO", "Issue", "- abc service response : " + abcStatus);

                    if (abcStatus != 200)
                    {
                        reportIssuanceStatus(status_url, false, optional_cookie);
                        return new ReturObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                    }
                }

                // we now have issuance message ready on abcResponse
                if (abcStatus == 204)
                {
                    reportIssuanceStatus(status_url, true, optional_cookie);
                    return new ReturObject { status = abcStatus, message = "Credential obtained... SUCCESS" };
                }

                // we should continue - read from abc send to xmlstep
                var abcFromStream = abcResponse.GetResponseStream();

                var xmlStepRequest = (HttpWebRequest)WebRequest.Create(step_url);
                xmlStepRequest.Method = "POST";
                xmlStepRequest.ContentType = "application/xml; charset=utf-8";
                xmlStepRequest.Accept = "text/xml,application/xml";
                xmlStepRequest.KeepAlive = false;
                xmlStepRequest.ServicePoint.ConnectionLeaseTimeout = 5000;
                xmlStepRequest.ServicePoint.MaxIdleTime = 5000;
                xmlStepRequest.ServicePoint.ConnectionLimit = 400;
                log("BHO", "Issue", "- xmlStepRequest.ServicePoint : " + xmlStepRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlStepRequest.ServicePoint.ConnectionLimit);

                if (! String.IsNullOrEmpty(optional_cookie))
                {
                    xmlStepRequest.Headers.Add("Cookie", optional_cookie);
                }

                HttpWebResponse xmlStepResponse;
                try
                {
                    log("BHO", "Issue", "- Send Step IssuanceMessage to Issuer....");
                    MemoryStream abce2IssuerStream = new MemoryStream();
                    using (BinaryWriter writer = new BinaryWriter(abce2IssuerStream))
                    {
                        while (abcFromStream.Read(b, 0, 1) != 0)
                        {
                            writer.Write(b);
                        }
                        writer.Close();
                    }
                    abcFromStream.Close();

                    abce2IssuerStream.Close();
                    byte[] abce2IssuerBytes = abce2IssuerStream.ToArray();
//                    var abce2IssuerString = new System.Text.UTF8Encoding().GetString(abce2IssuerBytes);
//                    log("BHO", "Issuer", "- xml from ABCE - length : " + abce2IssuerBytes.Length + " : " + abce2IssuerString);
                    log("BHO", "Issuer", "- xml from ABCE - length : " + abce2IssuerBytes.Length);

                    var xmlToStream = xmlStepRequest.GetRequestStream();
                    xmlToStream.Write(abce2IssuerBytes, 0, abce2IssuerBytes.Length);
                    xmlToStream.Close();
                    xmlToStream.Dispose();

                    log("BHO", "Issue", "- get Response from Issuer step...");

                    xmlStepResponse = (HttpWebResponse)xmlStepRequest.GetResponse();
                    log("BHO", "Issue", "- Contacted Issuer...");
                }
                catch (WebException e)
                {
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    log("BHO", "Issue", "- WebException : " + e);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            log("BHO", "Issue", "- Error : " + (int)response.StatusCode + " : " + response.StatusDescription);
                            return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                        }
                        else
                        {
                            log("BHO", "Issue", "- Unhandled error : " + e.Status);
                            return new ReturObject { status = 500, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        log("BHO", "Issue", "- Could not contact issuanceserver.");
                        return new ReturObject { status = 500, message = "Could not contact issuanceserver." };
                    }
                }
                catch (Exception e)
                {
                    log("BHO", "Issue", "- Internal Error ? : " + e);
                    return new ReturObject { status = 500, message = "Internal Error ? : " + e };
                }

                // skip verify response from issuer! - seems we never stop on issuer side...
                int xmlStepStatus = (int)xmlStepResponse.StatusCode;
                log("BHO", "Issue", "- xmlStepStatus sending Step to Issuer : " + xmlStepStatus + " - type : " + xmlStepRequest.ContentType + " - length : " + xmlStepRequest.ContentLength);

                // we now have IssuanceMessage from Issuer - continuer loop and send to local User ABC
                xmlFromStream = xmlStepResponse.GetResponseStream();
            }
        }

        private ReturObject reportIssuanceStatus(string status_url, bool success, string optional_cookie)
        {
            if (status_url != null)
            {
                var statusRequest = (HttpWebRequest)WebRequest.Create(status_url);
                statusRequest.Method = "POST";
                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    statusRequest.Headers.Add("Cookie", optional_cookie);
                }

                try
                {
                    var statusRequestStream = statusRequest.GetRequestStream();

                    System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
                    Byte[] statusBytes = encoding.GetBytes(success ? "1" : "0");

                    statusRequestStream.Write(statusBytes, 0, statusBytes.Length);
                    statusRequestStream.Close();

                }
                catch (WebException e)
                {
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            int statusCodeX = (int)response.StatusCode;
                            return new ReturObject { status = statusCodeX, message = "Failuer contacting Issuer Status Service" };
                        }
                        else
                        {
                            return new ReturObject { status = 500, message = "Uknown Response calling Issuer Status : " + e };
                        }
                    }
                    else
                    {
                        return new ReturObject { status = 500, message = "Could not contact Issuer Status Service" };
                    }

                }

            }
            return new ReturObject { status = 200, message = "Ok" };

        }

        [ComVisible(true)]
        public ReturObject PresentNoCookie(string language, string policy_url, string verify_url)
        {
            return Present(language, policy_url, verify_url, null);
        }

        [ComVisible(true)]
        public ReturObject Present(string language, string policy_url, string verify_url, string optional_cookie)
        {
            try
            {

                long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
                var sessionID = "PresentIE" + milliseconds;

                log("BHO", "Present", "Start : " + language + " " + policy_url + " " + verify_url + " " + optional_cookie);

                // todo : check smartcard
                ReturObject error = CheckSmartCard(sessionID);
                if (error != null) return error;

                //
                var xmlPolicyRequest = (HttpWebRequest)WebRequest.Create(policy_url);
                xmlPolicyRequest.Method = "GET";
                xmlPolicyRequest.Accept = "text/xml,application/xml";
                xmlPolicyRequest.KeepAlive = false;
                xmlPolicyRequest.ContentLength = 0;
                xmlPolicyRequest.ReadWriteTimeout = 5000;
                xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout = 5000;
                xmlPolicyRequest.ServicePoint.MaxIdleTime = 5000;
                xmlPolicyRequest.ServicePoint.ConnectionLimit = 400;
                log("BHO", "Present", "- xmlVerifyRequest.ServicePoint : " + xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlPolicyRequest.ServicePoint.ConnectionLimit);
 
                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    log("BHO", "Present", "- Set Cookie for Policy : " + optional_cookie);
                    xmlPolicyRequest.Headers.Add("Cookie", optional_cookie);
                }
                else
                {
                    log("BHO", "Present", "- No Cookie");
                }
                
                HttpWebResponse xmlPolicyResponse;
                try
                {
                    log("BHO", "Present", "- Try to get policy.... " + policy_url + " - timeout : " + xmlPolicyRequest.ReadWriteTimeout);
                    xmlPolicyResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                    log("BHO", "Present", "- Contacted Verifier... " + xmlPolicyResponse.StatusCode + " : " + xmlPolicyResponse.ContentType + " - content length : " + xmlPolicyResponse.ContentLength);
                }
                catch (Exception e)
                {
                  if(e.GetType() == typeof(System.Net.WebException)) 
                  {
                      System.Net.WebException we = (System.Net.WebException)e;
                    log("BHO", "Present", "- WebException - on policy : " + e);
                    if (we.Status == WebExceptionStatus.ProtocolError)
                    {
                        log("BHO", "Present", "- e.Status : " + we.Status + " - e.Response : " + we.Response);
                        var response = we.Response as HttpWebResponse;
                        log("BHO", "Present", "- got response ? : " + response);
                        if (response != null)
                        {
                            log("BHO", "Present", "- response : " + response.StatusCode + " : " + response.StatusDescription);
                            return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                        }
                        else
                        {
                            return new ReturObject { status = 500, message = "Unhandled error : " + we.Status };
                        }

                    }
                    else
                    {
                        return new ReturObject { status = 500, message = "Could not contact policyserver." };
                    }
                  } 
                  else 
                  {
                      log("BHO", "Present", "- Unknown exception  : " + e);
                      return new ReturObject { status = 500, message = "Unknown error  : " + e.Message };
                  }
                }

                if (xmlPolicyResponse.StatusCode != HttpStatusCode.OK)
                {
                    log("BHO", "Present", "- No Policy from Server " + xmlPolicyResponse.StatusCode + " " + xmlPolicyResponse);

                    return new ReturObject { status = (int)xmlPolicyResponse.StatusCode, message = "Failed to obtain presentation policy" };
                }
                log("BHO", "Present", "- OK From Policy from Server " + xmlPolicyResponse.StatusCode + " " + xmlPolicyResponse);
                log("BHO", "Present", "- Policy Response Headers : " + xmlPolicyResponse.Headers);

                var xmlPolicyStream = xmlPolicyResponse.GetResponseStream();
                log("BHO", "Present", "- got Response Stream! " + xmlPolicyStream);

                //
                var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/createPresentationToken/" + sessionID);
                abcRequest.Method = "POST";
                abcRequest.Accept = "text/xml,application/xml";
                abcRequest.ContentType = "application/xml";
                Stream abcToStream = null; // = abcRequest.GetRequestStream();
                try
                {
                    abcToStream = abcRequest.GetRequestStream();
                }
                catch (WebException e)
                {
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                        }
                        else
                        {
                            return new ReturObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ReturObject { status = 400, message = "User ABCE Not Running (Verification)" };
                    }
                }

                log("BHO", "Present", "- got Request Stream! " + abcToStream);
                MemoryStream policyByteStream = new MemoryStream();
                byte[] b = new byte[1];
                using (BinaryWriter writer = new BinaryWriter(policyByteStream))
                {
                    while (xmlPolicyStream.Read(b, 0, 1) != 0)
                    {
                        writer.Write(b);
                    }
                    writer.Close();
                }
                xmlPolicyStream.Close();
                log("BHO", "Present", "- dispose xmlPolicyStream...");
                xmlPolicyStream.Dispose();
                log("BHO", "Present", "- close xmlPolicyResponse...");
                xmlPolicyResponse.Close();

                policyByteStream.Close();
                byte[] policyBytes = policyByteStream.ToArray();
//                var xmlPolicyString = new System.Text.UTF8Encoding().GetString(policyBytes);
//                log("BHO", "Present", "- xml from Verifier - length : " + policyBytes.Length + " : " + xmlPolicyString);
                log("BHO", "Present", "- xml from Verifier - length : " + policyBytes.Length + " - abc stream : " + abcToStream);

                abcToStream.Write(policyBytes, 0, policyBytes.Length);
                abcToStream.Close();
                abcToStream.Dispose();

                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                var abcStatus = (int)abcResponse.StatusCode;
                log("BHO", "Present", "- response from user service : " + abcResponse + " " + abcStatus);

                if (abcStatus == 422)
                {
                    abcResponse.Close();
                    return new ReturObject { status = abcStatus, message = "Can not satisfy policy" };
                }
                if (abcStatus != 200 && abcStatus != 203)
                {
                    abcResponse.Close();
                    return new ReturObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                }
                if (abcStatus == 203)
                {   // 203 - means id selection...

                    abcResponse.Close();
                    var selectionUIURL = USER_UI_SERVICE + "?mode=presentation&demo=false&language=" + language + "&sessionid=" + sessionID;
                    log("BHO", "Present", "- selectionUIURL : " + selectionUIURL);

                    var form = new WebForm { URL = selectionUIURL };
                    var dialogResponse = form.ShowDialog();
                    // web form is closed
                    log("BHO", "Present", "- ID Select web form was closed : " + dialogResponse);

                    // get presentation token... maybe...
                    abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/createPresentationTokenIdentitySelection/" + sessionID);
                    abcRequest.Method = "POST";
                    abcRequest.Accept = "text/xml,application/xml";
                    abcRequest.ContentType = "text/plain";

                    try
                    {
                        abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                        abcStatus = (int)abcResponse.StatusCode;
                    }
                    catch (WebException e)
                    {
                        log("BHO", "Present", "- abc service error obtaining token : " + e);
                        if (e.Status == WebExceptionStatus.ProtocolError)
                        {
                            log("BHO", "Present", "- abc service error obtaining token - status : " + e.Status + " : " + e.Response);
                            var response = e.Response as HttpWebResponse;
                            if (response != null)
                            {
                                return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                            }
                            else
                            {
                                return new ReturObject { status = 400, message = "Unhandled error : " + e.Status };
                            }
                        }
                        else
                        {
                            return new ReturObject { status = 400, message = "User ABCE Not Running (Verification)" };
                        }
                    }


                    log("BHO", "Present", "- abc service response : " + abcStatus);

                    if (abcStatus != 200)
                    {
                        return new ReturObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                    }
                }
                // abcResponse should now have ABCE PresentationToken..
                log("BHO", "Present", "- setup verify post");
                var xmlVerifyRequest = (HttpWebRequest)WebRequest.Create(verify_url);
                xmlVerifyRequest.Method = "POST";
                xmlVerifyRequest.Accept = "text/xml,application/xml";
                xmlVerifyRequest.ContentType = "application/xml; charset=utf-8"; // charset=utf-8
                xmlVerifyRequest.KeepAlive = false;
                xmlVerifyRequest.ReadWriteTimeout = 5000;
                xmlVerifyRequest.ServicePoint.ConnectionLeaseTimeout = 5000;
                xmlVerifyRequest.ServicePoint.MaxIdleTime = 5000;
                xmlVerifyRequest.ServicePoint.ConnectionLimit = 400;
                log("BHO", "Present", "- xmlVerifyRequest.ServicePoint - keepalive false : " + xmlVerifyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlVerifyRequest.ServicePoint.ConnectionLimit);

                // just hand over cookie 
                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    log("BHO", "Present", "- Set Cookie for Verify : " + optional_cookie);
                    xmlVerifyRequest.Headers.Add("Cookie", optional_cookie);
                }

                log("BHO", "Present", "- get response stream from ABC...");
                var abcFromStream = abcResponse.GetResponseStream();

                log("BHO", "Present", "- get request stream to verifier...");
                var xmlToStream = xmlVerifyRequest.GetRequestStream();
                log("BHO", "Present", "- send xml to verifier...");

                MemoryStream tokenByteStream = new MemoryStream();
                using (BinaryWriter writer = new BinaryWriter(tokenByteStream))
                {
                    while (abcFromStream.Read(b, 0, 1) != 0)
                    {
                        writer.Write(b);
                    }
                    writer.Close();
                }
                abcFromStream.Close();
                abcFromStream.Dispose();
                abcResponse.Close();

                tokenByteStream.Close();
                byte[] tokenBytes = tokenByteStream.ToArray();
//                var xmlTokenString = new System.Text.UTF8Encoding().GetString(tokenBytes);
//                log("BHO", "Present", "- xml to Verifier - length : " + tokenBytes.Length + " : " + xmlTokenString);
                log("BHO", "Present", "- xml to Verifier - length : " + tokenBytes.Length);

                xmlToStream.Write(tokenBytes, 0, tokenBytes.Length);
                xmlToStream.Close();
                xmlToStream.Dispose();

                HttpWebResponse xmlVerifyResponse;
                try {
                    log("BHO", "Present", "- check response...");
                    xmlVerifyResponse = (HttpWebResponse)xmlVerifyRequest.GetResponse();
                    xmlVerifyResponse.Close();
                    log("BHO", "Present", "- check response - not exception... - response closed");
                }
                catch (Exception e)
                {
                    if(e.GetType() == typeof(System.Net.WebException)) 
                    {
                        System.Net.WebException we = (System.Net.WebException)e;
                        log("BHO", "Present", "- WebException - on verify : " + e);
                        if (we.Status == WebExceptionStatus.ProtocolError)
                        {
                            log("BHO", "Present", "- e.Status : " + we.Status + " - e.Response : " + we.Response);
                            var response = we.Response as HttpWebResponse;
                            log("BHO", "Present", "- got response ? : " + response);
                            if (response != null)
                            {
                                log("BHO", "Present", "- response : " + response.StatusCode + " : " + response.StatusDescription);
                                return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                            }
                            else
                            {
                                return new ReturObject { status = 500, message = "Unhandled error : " + we.Status };
                            }
                        }
                        else
                        {
                            return new ReturObject { status = 500, message = "Could not contact policyserver." };
                        }
                      } 
                      else 
                      {
                          log("BHO", "Present", "- Unknown exception - on verify : " + e);
                          return new ReturObject { status = 500, message = "Unknown error  : " + e.Message };
                      } 
                }

                log("BHO", "Present", "- Token Response Headers : " + xmlVerifyResponse.Headers);

                int xmlVerifyStatus = (int)xmlVerifyResponse.StatusCode;

                log("BHO", "Present", "- status from verify request : " + xmlVerifyStatus + " " + xmlVerifyResponse.StatusDescription);

                if (xmlVerifyStatus != 200 && xmlVerifyStatus != 202 && xmlVerifyStatus != 204)
                {
                    return new ReturObject { status = xmlVerifyStatus, message = "Failed to contact verifier" };
                }
                return new ReturObject { status = xmlVerifyStatus, message = "Verfication was succesful" };
            }
            catch (Exception e)
            {
                log("BHO", "Present", "- Present Exception : " + e);
            }

            return new ReturObject { status = 504, message = "Bail out after exception." };

        }

        [ComVisible(true)]
        public ReturObject StoreData(string data)
        {
            log("BHO", "StoreData", "Start! " + data);

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "StoreDataIE" + milliseconds;

            // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error != null) return error;

            //
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/storeData/" + sessionID);
            abcRequest.Method = "POST";
            abcRequest.ContentType = "text/plain";
            abcRequest.Accept = "text/plain";
            Stream abcToStream; // = abcRequest.GetRequestStream();
            try
            {
                abcToStream = abcRequest.GetRequestStream();

                System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
                Byte[] dataBytes = encoding.GetBytes(data);

                abcToStream.Write(dataBytes, 0, dataBytes.Length);

                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                abcToStream.Close();
                abcToStream.Dispose();
                abcResponse.Close();

                log("BHO", "StoreData", "- store ok!");
                return new ReturObject { status = 200, message = "Ok" };
            }
            catch (WebException e)
            {
                log("BHO", "StoreData", "- exception=! " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        log("BHO", "StoreData", "Error loading data : " + response.StatusCode + " : " + response.StatusDescription);
                        return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        log("BHO", "StoreData", "Uknown Response calling StoreData : " + e);
                        return new ReturObject { status = 400, message = "Uknown Response calling StoreData : " + e };
                    }
                }
                else
                {
                    log("BHO", "StoreData", "could not contact User ABC Service : " + e);
                    return new ReturObject { status = 400, message = "User ABCE Service Not Running" };
                }
            }
        }

        [ComVisible(true)]
        public ReturObject LoadData()
        {
            log("BHO", "LoadData", "Start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "LoadDataIE" + milliseconds;

            // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error != null) return error;

            //
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/loadData/" + sessionID);
            abcRequest.Method = "GET";
            abcRequest.ContentType = "text/plain";
            abcRequest.Accept = "text/plain";
            try
            {
                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                Stream abcFromStream = abcResponse.GetResponseStream();

                System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
                string data = "";
                byte[] b = new byte[1];
                while (abcFromStream.Read(b, 0, 1) != 0)
                {
                    data += encoding.GetString(b);
                }
                abcFromStream.Close();
                abcFromStream.Dispose();
                abcResponse.Close();

                log("BHO", "LoadData", "Load data ok : " + data);
                return new ReturObject { status = 200, message = "Ok", data = data };
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        log("BHO", "LoadData", "Error loading data : " + response.StatusCode + " : " + response.StatusDescription);
                        return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        log("BHO", "LoadData", "Uknown Response calling LoadData : " + e);
                        return new ReturObject { status = 400, message = "Uknown Response calling LoadData : " + e };
                    }
                }
                else
                {
                    log("BHO", "LoadData", "could not contact User ABC Service : " + e);
                    return new ReturObject { status = 400, message = "User ABCE Service Not Running" };
                }
            }
        }

        private void ManageCredentials() {
            log("BHO", "ManageCredentials", "ManageCredentials - start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "ManageCredentialsIE" + milliseconds;
            ReturObject check = CheckSmartCard(sessionID);
            if(check != null) {
                MessageBox.Show("Manager credentials - Unlock Failed : " + check.message,
                    "Manager credentials",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            // var manageCredentialListURL = USER_ABCE_SERVICE + "/user/getUiManageCredentialData/" + sessionID;

            // TODO : Language ??
            var language = "en";
            var manageCredentialListURL = USER_UI_SERVICE + "?mode=management&demo=false&language=" + language + "&sessionid=" + sessionID;

            var form = new WebForm { URL = manageCredentialListURL };
            var dialogResponse = form.ShowDialog();
            log("BHO", "ManageCredentials", "- dialogResponse : " + dialogResponse);
        }

        private void ShowDebugInfo() {
            log("BHO", "showDebugInfo", "Get Debug Info from UserABCE startup");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "DebugInfoIE" + milliseconds;

            var debugInfoURL = USER_ABCE_SERVICE + "/user/getDebugInfo/"+sessionID;

            var form = new WebForm { URL = debugInfoURL };
            var dialogResponse = form.ShowDialog();
            log("BHO", "showDebugInfo", "- dialogResponse : " + dialogResponse);

        }

        private void CheckRevocationStatus()
        {
            log("BHO", "CheckRevocationStatus", "Check RevocationStatus.");
            MessageBox.Show("Checking for revocation status. This may take up to a minute. Please do not remove the card from the reader.");
            /*
                        var strbundle = document.getElementById("overlayStrings");
                        alert(strbundle.getString("checking_revocation_status"));
                        var abcquery = new XMLHttpRequest();
                        abcquery.open("POST", USER_ABCE_SERVICE + "/user/checkRevocationStatus", false);
                        try
                        {
                            abcquery.send();
                        }
                        catch (failed)
                        {
                            alert(strbundle.getString("smartcard_server_unavailable"));
                        }
                        if (abcquery.status == 200)
                        {
                            alert(strbundle.getString("checking_revocation_status_ok"));
                        }
                        else
                        {
                            alert(strbundle.getString("checking_revocation_status_fail"));
                        }
            */
            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/checkRevocationStatus");
            abcRequest.ContentType = "application/xml";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                log("BHO", "CheckRevocationStatus", " - setup call! ");
                var abcRequestStream = abcRequest.GetRequestStream();

                log("BHO", "CheckRevocationStatus", " - rest call 'CheckRevocationStatus' ");

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                statusCode = (int)abcResponse.StatusCode;

                log("BHO", "CheckRevocationStatus", " - check OK!");
                MessageBox.Show("Credentials have been check.\nRevoked credentials have been deleted.");
                return;
            }
            catch (WebException e)
            {
                log("BHO", "CheckRevocationStatus", " - WebException! " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        statusCode = (int)response.StatusCode;
                        log("BHO", "CheckRevocationStatus", " - RESPONSE : : " + response + " : " + statusCode);
                    }
                    else
                    {
                        log("BHO", "CheckRevocationStatus", " - NO RESPONSE : : " + response);
                    }
                    MessageBox.Show("Checking Revocation Status failed.",
                        "Error!",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    return;
                }
                else
                {
                    log("BHO", "CheckRevocationStatus", " - UserABCE Service not available!");
                    MessageBox.Show("UserABCE Service is not Running!",
                        "Error!",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    return;
                }
            }
        }

        private void ChangePin()
        {
            log("BHO", "ChangePin", "ChangePin - start");

            if (!SmartcardStatus()) return;

            //
            ChangePinForm changePinForm = new ChangePinForm();
            var res = changePinForm.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK) return;

            if(changePinForm.getCurrentPin().Length != 4 || changePinForm.getNewPin().Length != 4)
            {
                MessageBox.Show("Pin must be 4 digits.",
                    "Wrong Pin",
                     MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            string changePin = changePinForm.getCurrentPin() + " " + changePinForm.getNewPin();

            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] changePinBytes = encoding.GetBytes(changePin);

            //
            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/changePin");
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                log("BHO", "ChangePin", " - rest call 'ChangePin' ");
                abcRequestStream.Write(changePinBytes, 0, changePinBytes.Length);
                abcRequestStream.Close();

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                statusCode = (int)abcResponse.StatusCode;
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        statusCode = (int)response.StatusCode;
                    }
                    else
                    {
                        MessageBox.Show("Uknown Response calling Change Pin : : " + e,
                            "Error!",
                            MessageBoxButtons.OK,
                            MessageBoxIcon.Exclamation,
                            MessageBoxDefaultButton.Button1);
                        return;
                    }
                }
                else
                {
                    MessageBox.Show("Uknown Response calling Change Pin, not ProtocolError: " + e,
                        "Error!",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    return;
                }
            }

            if (statusCode >= 200 && statusCode <= 204)
            {
                // PIN OK!
                log("BHO", "ChangePin", " - PIN OK!");
                MessageBox.Show("Successfully changed the PIN");
                return;
            }
            if (statusCode == 401)
            {
                // PIN WRONG - not Locked yet!
                MessageBox.Show("Incorrect PIN entered. Try again",
                    "Incorrect Pin!",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            if (statusCode == 402)
            {
                // PIN WRONG - not Locked yet!
                MessageBox.Show("Incorrect PIN entered - card has been locked! Use the PUK-code to unlock",
                    "Incorrect Pin!",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            MessageBox.Show("Unknown return code. Could not change PIN. Maybe you forgot to connect your smartcard?",
                "Unknown return code : " + statusCode,
                MessageBoxButtons.OK,
                MessageBoxIcon.Exclamation,
                MessageBoxDefaultButton.Button1);
            return;

        }


        private void UnlockCard()
        {
            log("BHO", "UnlockCard", "UnlockCard - start");

            if (!SmartcardStatus()) return;

            //
            UnlockCardForm unlockCardForm = new UnlockCardForm();
            var res = unlockCardForm.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK) return;

            if (unlockCardForm.getPUK().Length != 8)
            {
                MessageBox.Show("PUK must be 8 digits.",
                    "Wrong PUK",
                     MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            if (unlockCardForm.getNewPin().Length != 4)
            {
                MessageBox.Show("Pin must be 4 digits.",
                    "Wrong Pin",
                     MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            string unlockCard = unlockCardForm.getPUK() + " " + unlockCardForm.getNewPin();

            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] unlockCardBytes = encoding.GetBytes(unlockCard);

            //
            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/unlockCard");
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                log("BHO", "UnlockCard", " - rest call 'unlockCard' ");
                abcRequestStream.Write(unlockCardBytes, 0, unlockCardBytes.Length);
                abcRequestStream.Close();

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                statusCode = (int)abcResponse.StatusCode;
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        statusCode = (int)response.StatusCode;
                    }
                    else
                    {
                        MessageBox.Show("Uknown Response calling Unlock : : " + e,
                            "Error!",
                            MessageBoxButtons.OK,
                            MessageBoxIcon.Exclamation,
                            MessageBoxDefaultButton.Button1);
                        return;
                    }
                }
                else
                {
                    MessageBox.Show("Uknown Response calling Unlock, not ProtocolError: " + e,
                        "Error!",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    return;
                }
            }

            if (statusCode >= 200 && statusCode <= 204)
            {
                // PIN OK!
                log("BHO", "UnlockCard", " - Unlock OK!");
                MessageBox.Show("Unlocked the card. PIN is now set to your chosen PIN");
                return;
            }
            if (statusCode == 401)
            {
                // PIN WRONG - not Locked yet!
                MessageBox.Show("Wrong PUK - try again.",
                    "Unlock Failed!",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            if (statusCode == 402)
            {
                // PIN WRONG - not Locked yet!
                MessageBox.Show("Wrong PUK 10 times in a row - card is now dead. Take it to an admin in order to restore the card.",
                    "Unlock Failed!",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            MessageBox.Show("Unknown return code. Could not unlock the card",
                "Unknown return code : " + statusCode,
                MessageBoxButtons.OK,
                MessageBoxIcon.Exclamation,
                MessageBoxDefaultButton.Button1);
            return;

        }

        private Boolean SmartcardStatus()
        {
            log("BHO", "SmartcardStatus", "SmartcardStatus - start");

            // smartcard status
            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/smartcardStatus");
            abcRequest.Method = "GET";
            try
            {
                log("BHO", "SmartcardStatus", " - call rest smartcardStatus");
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                log("BHO", "SmartcardStatus", " - status ok : " + abcResponse.StatusCode);
            }
            catch (Exception e)
            {
                // could not contact User ABC Service
                log("BHO", "SmartcardStatus", "Internal User ABCE Service Error : " + e);
                MessageBox.Show("Smartcard/Server not available.",
                    "Error!",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1); 
                
                return false;
            }

            // default
            return true;
        }


    }

}
