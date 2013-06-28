using System;
using System.Diagnostics;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.Expando;
using Microsoft.Win32;
using SHDocVw;
using mshtml;
using System.IO;
using System.Net;

namespace ABC4TrustBHO
{
    [ComVisible(true)]
    [Guid("B19B5289-420F-4BC4-B4B5-3FD5D7EA8D02")]
//    [ClassInterface(ClassInterfaceType.None)]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    [ProgId("ABC4Trust")]
    [ComDefaultInterface(typeof(IExtension))]
    public class BrowserHelperObject : IObjectWithSite, IExtension, IOleCommandTarget
    {
        WebBrowser _webBrowser;
        private object site;

        public void OnDocumentComplete(object pDisp, ref object url)
        {
            log("BHO","OnDocumentComplete", "Start");

            if (pDisp != this.site)
                return;

            dynamic window =  _webBrowser.Document.parentWindow;
            var windowEx = (IExpando) window;
            var prop = windowEx.GetProperty("abc4trust",BindingFlags.Default);
            if(prop!=null)
                windowEx.RemoveMember(prop);

            windowEx.AddProperty("abc4trust");
            window.abc4trust = this;

            log("BHO", "OnDocumentComplete", "DONE");
        }

        #region BHO Internal Functions

        public int SetSite(object site)
        {
            this.site = site;
            if (site != null)
            {
                _webBrowser = (WebBrowser) site;
                _webBrowser.DocumentComplete += OnDocumentComplete;
                
            }
            else
            {
                _webBrowser.DocumentComplete -= OnDocumentComplete;
                _webBrowser = null;
            }

            return 0;

        }

        public int GetSite(ref Guid guid, out IntPtr ppvSite)
        {
            IntPtr punk = Marshal.GetIUnknownForObject(_webBrowser);
            int hr = Marshal.QueryInterface(punk, ref guid, out ppvSite);
            Marshal.Release(punk);

            return hr;
        }
        #endregion

        #region Implementation of IOleCommandTarget
        public int QueryStatus(IntPtr pguidCmdGroup, uint cCmds, ref OLECMD prgCmds, IntPtr pCmdText)
        {
            using (StreamWriter sw = File.AppendText("C:\\Users\\default.Win7x64\\abc4trust_bho_ole.txt"))
            {
                sw.AutoFlush = true;
                log("BHO.IOleCommandTarget", "QueryStatus", "...");
            }

            return 0;
        }
        public int Exec(IntPtr pguidCmdGroup, uint nCmdID, uint nCmdexecopt, IntPtr pvaIn, IntPtr pvaOut)
        {
            using (StreamWriter sw = File.AppendText("C:\\Users\\default.Win7x64\\abc4trust_bho_ole.txt"))
            {
                sw.AutoFlush = true;
                log("BHO.IOleCommandTarget", "Exec", "Params : " + pguidCmdGroup + " " + nCmdID + " " + nCmdexecopt + " " + pvaIn + " " + pvaOut);

                PinForm pf = new PinForm();

                string pin = null;
                var res = pf.ShowDialog();
                if (res != System.Windows.Forms.DialogResult.OK)
                {
                    // pin not entered
                    log("BHO.IOleCommandTarget", "Exec", " - user canceled..." + res);
                }
                else
                {
                    pin = pf.getPin();
                    log("BHO.IOleCommandTarget", "Exec", " - user OK! " + res + " : " + pin);
                }

                log("BHO.IOleCommandTarget", "Exec", " - exec DONE!! " + res);

            }
            /*
                        try
                        {
                            // Accessing the document from the command-bar.
                            var document = _webBrowser.Document as IHTMLDocument2;
                            var window = document.parentWindow;
                            var result = window.execScript(@"alert('You will now be allowed to configure the text to highlight...');");

                            var form = new HighlighterOptionsForm();
                            form.InputText = TextToHighlight;
                            if (form.ShowDialog() != DialogResult.Cancel)
                            {
                                TextToHighlight = form.InputText;
                                SaveOptions();
                            }
                        }
                        catch (Exception ex)
                        {
                            MessageBox.Show(ex.Message);
                        }
            */
            return 0;
        }
        #endregion

        #region Registering with regasm
        public static string RegBHO64 = "Software\\Wow6432Node\\Windows\\CurrentVersion\\Explorer\\Browser Helper Objects";
        public static string RegBHO32 = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Browser Helper Objects";
        public static string RegCmd = "Software\\Microsoft\\Internet Explorer\\Extensions";
        public static string TestRegCmd = "Software\\Microsoft\\Internet Explorer\\MenuExt";

        [ComRegisterFunction]
        public static void RegisterBHO(Type type)
        {
            string guid = type.GUID.ToString("B");

            // BHO 64
            /*
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO64, true);
                if (registryKey == null)
                    registryKey = Registry.LocalMachine.CreateSubKey(RegBHO64);
                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                    key = registryKey.CreateSubKey(guid);
                key.SetValue("Alright", 1);
                //key.SetValue("NoExplorer", 1);
                registryKey.Close();
                key.Close();
            }
            */
            // BHO 32
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO32, true);
                if (registryKey == null)
                    registryKey = Registry.LocalMachine.CreateSubKey(RegBHO32);
                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                    key = registryKey.CreateSubKey(guid);
                key.SetValue("Alright", 1);
                //key.SetValue("NoExplorer", 1);
                registryKey.Close();
                key.Close();
            }

            // Command
            {
//                Registry
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegCmd, true);
                if (registryKey == null)
                    registryKey = Registry.LocalMachine.CreateSubKey(RegCmd);
                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                    key = registryKey.CreateSubKey(guid);

                key.SetValue("ButtonText", "ABC4Trust Menu");
                key.SetValue("CLSID", "{1FBA04EE-3024-11d2-8F1F-0000F87ABD16}");
                key.SetValue("ClsidExtension", guid);
                key.SetValue("Icon", "");
                key.SetValue("HotIcon", "");
                key.SetValue("Default Visible", "Yes");
                key.SetValue("MenuText", "&ABC4Trust Menu");
                key.SetValue("ToolTip", "ABC4Trust Menu");
                //key.SetValue("KeyPath", "no");

                //key.SetValue("MenuCustomize", "file");

                registryKey.Close();
                key.Close();
            }
        }

        [ComUnregisterFunction]
        public static void UnregisterBHO(Type type)
        {
            string guid = type.GUID.ToString("B");
            // BHO
            /*
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO64, true);
                if (registryKey != null)
                    registryKey.DeleteSubKey(guid, false);
            }
             */
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegBHO32, true);
                if (registryKey != null)
                    registryKey.DeleteSubKey(guid, false);
            }
            // Command
            {
                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(RegCmd, true);
                if (registryKey != null)
                    registryKey.DeleteSubKey(guid, false);
            }
        }
        #endregion



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

        public ReturObject ShowSite(string url)
        {
            log("BHO", "ShowSite", "Open : " + url);
            var form = new WebForm { URL = url };
            form.ShowDialog();
            return new ReturObject { message = "ShowSite OK!", status = 4224 };
        }

        public static string USER_ABCE_SERVICE = "http://localhost:9300/idselect-user-service";
        public static string USER_UI_SERVICE = "http://localhost:9093/user-ui";

        private static bool enableLogging = true;
        private static StreamWriter logWriter = null;
        public static void log(string clazz, string method, string msg)
        {
            if(enableLogging) 
            {
                if (logWriter == null) initLogger();

                logWriter.WriteLine(clazz + "::" + method + " : " + msg);
                        
            }
        }
        private static void initLogger()
        {
            string homePath = (Environment.OSVersion.Platform == PlatformID.Unix || 
                   Environment.OSVersion.Platform == PlatformID.MacOSX)
                ? Environment.GetEnvironmentVariable("HOME")
            : Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");

            logWriter = File.AppendText(homePath + "\\abc4trust_bho_card.txt");
            logWriter.AutoFlush = true;
        }



        private ReturObject CheckSmartCard(string sessionID)
        {
            log("BHO", "CheckSmartCard", "SessionID : " + sessionID);

            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
            abcRequest.Method = "GET";
            try
            {
                log("BHO", "CheckSmartCard", " - call rest : " + USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                if (abcResponse.StatusCode ==  HttpStatusCode.NoContent)
                {
                    log("BHO", "CheckSmartCard", " - ' no content ' we have same card! " + abcResponse.StatusCode);
                    // we have card...
                    return null;
                }
            }
            catch(WebException e)
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
            if(res != System.Windows.Forms.DialogResult.OK)
            {
                // pin not entered
                log("BHO", "CheckSmartCard", " - user canceled..." + res);
                return new ReturObject { status = 400, message = "Pin not entered" };
            } 
            else
            {
                pin = pf.getPin();
                log("BHO", "CheckSmartCard", " - user OK! " + res + " : " + pin);
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

                log("BHO", "CheckSmartCard", " - rest call 'unlockSmartard' " + pin);
                abcRequestStream.Write(pinBytes, 0, pinBytes.Length);
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
                        return new ReturObject { status = 400, message = "Uknown Response calling unlock : : " + e };
                    }
                }
                else
                {
                    return new ReturObject { status = 400, message = "Uknown Response calling unlock, not ProtocolError: " + e };
                }
            }

            if (statusCode >= 200 && statusCode <=204)
            {   
                // PIN OK!
                log("BHO", "CheckSmartCard", " - PIN OK!");
                return null;
            } 
            if(statusCode == 401)
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

        public ReturObject Issue(string language, string start_url, string step_url, string status_url)
        {

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "IssuanceIE" + milliseconds;

            log("BHO", "Issue", "Start : " + language + " " + start_url + " " + step_url + " " + status_url);

            // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error != null) return error;

            //
            //
            var xmlPolicyRequest = (HttpWebRequest)WebRequest.Create(start_url);
            xmlPolicyRequest.Method = "POST";
            xmlPolicyRequest.Accept = "text/xml,application/xml";
            xmlPolicyRequest.KeepAlive = false;

            HttpWebResponse xmlResponse;
            try
            {
                log("BHO", "Issue", "- Try to get initial issuance message....");
                xmlResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                log("BHO", "Issue", "- Contacted Issuer...");
            }
            catch (WebException e)
            {
                log("BHO", "Issue", "- WebException : " + e);
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
                log("BHO", "Issue", "- Failed to obtain issuance policy from issuance server " + xmlResponse.StatusCode + " " + xmlResponse);

                return new ReturObject { status = (int)xmlResponse.StatusCode, message = "Failed to obtain issuance policy from issuance server" };
            }
            log("BHO", "Issue", "- OK From Issuance Policy from Server " + xmlResponse.StatusCode + " " + xmlResponse);

            var xmlFromStream = xmlResponse.GetResponseStream();
            log("BHO", "Issue", "- got Response Stream! " + xmlFromStream);

            while(true)
            {
                // read from issuer - write to local abc
                var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/issuanceProtocolStep/" + sessionID);
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
                    reportIssuanceStatus(status_url, false);
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

                log("BHO", "Issue", "- got Request Stream! " + abcToStream);
                byte[] b = new byte[1];
                while (xmlFromStream.Read(b, 0, 1) != 0)
                {
                    abcToStream.Write(b, 0, 1);
                }
                xmlFromStream.Close();
                abcToStream.Close();

                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                var abcStatus = (int)abcResponse.StatusCode;
                log("BHO", "Issue", "- response from user service : " + abcResponse + " " + abcStatus);

                if(abcStatus != 200 && abcStatus != 203 && abcStatus != 204)
                {
                    reportIssuanceStatus(status_url, false);
                    return new ReturObject { status = abcStatus, message = "Local ABC engine did not return an Issuancemessage." };
                }

                // we now have step response from User ABCE...
                if(abcStatus == 203) 
                {
                    // select credentials
                    abcResponse.Close();
                    var selectionUIURL = USER_UI_SERVICE + "?mode=issuance&demo=false&language=" + language + "&sessionid=" + sessionID;
                    log("BHO", "Issue", "- selectionUIURL : " + selectionUIURL);

                    var form = new WebForm { URL = selectionUIURL };

                    var dialogResponse = form.ShowDialog();
                    log("BHO", "Issue", "- dialogResponse : " + dialogResponse);

                    // get presentation token... maybe...
                    abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/issuanceProtocolStepSelect/" + sessionID);
                    abcRequest.Method = "POST";
                    abcRequest.Accept = "text/xml,application/xml";
                    abcRequest.ContentType = "text/plain";

                    abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                    abcStatus = (int)abcResponse.StatusCode;
                    log("BHO", "Issue", "- abc service response : " + abcStatus);

                    if (abcStatus != 200)
                    {
                        reportIssuanceStatus(status_url, false);
                        return new ReturObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                    }
                }

                // we now have issuance message ready on abcResponse
                if(abcStatus == 204) 
                {
                    reportIssuanceStatus(status_url, true);
                    return new ReturObject { status = abcStatus, message = "Credential obtained... SUCCESS" };
                }

                // we should continue - read from abc send to xmlstep
                var abcFromStream = abcResponse.GetResponseStream();

                var xmlStepRequest = (HttpWebRequest)WebRequest.Create(step_url);
                xmlStepRequest.Method = "POST";
                xmlStepRequest.ContentType = "application/xml";
                xmlStepRequest.Accept = "text/xml,application/xml";
                xmlStepRequest.KeepAlive = false;

                HttpWebResponse xmlStepResponse;
                try
                {
                    log("BHO", "Issue", "- Send Step IssuanceMessage to Issuer....");
                    var xmlToStream = xmlStepRequest.GetRequestStream();
                    while (abcFromStream.Read(b, 0, 1) != 0)
                    {
                        xmlToStream.Write(b, 0, 1);
                    }
                    abcFromStream.Close();
                    xmlToStream.Close();

                    log("BHO", "Issue", "- get Response from Issuer step...");

                    xmlStepResponse = (HttpWebResponse)xmlStepRequest.GetResponse();
                    log("BHO", "Issue", "- Contacted Issuer...");
                }
                catch (WebException e)
                {
                    reportIssuanceStatus(status_url, false);
                    log("BHO", "Issue", "- WebException : " + e);
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
                
                // skip verify response from issuer! - seems we never stop on issuer side...
                int xmlStepStatus = (int)xmlStepResponse.StatusCode;
                log("BHO", "Issue", "- xmlStepStatus : " + xmlStepStatus);

                // we now have IssuanceMessage from Issuer - continuer loop and send to local User ABC
                xmlFromStream = xmlStepResponse.GetResponseStream();
            }
        }

        private ReturObject reportIssuanceStatus(string status_url, bool success)
        {
            if(status_url != null) 
            {
                var statusRequest = (HttpWebRequest)WebRequest.Create(status_url);
                statusRequest.Method = "POST";
                try
                {
                    var statusRequestStream = statusRequest.GetRequestStream();

                    System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
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

        public ReturObject Present(string language, string policy_url, string verify_url)
        {
            try {

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "PresentIE" + milliseconds;

            log("BHO", "Present", "Start : " + language + " " + policy_url + " " + verify_url);
 
            // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error!=null) return error;

            //
            var xmlPolicyRequest = (HttpWebRequest)WebRequest.Create(policy_url);
            xmlPolicyRequest.Method = "GET";
            xmlPolicyRequest.Accept = "text/xml,application/xml";
            xmlPolicyRequest.KeepAlive = false;

            HttpWebResponse xmlResponse;
            try
            {
                log("BHO", "Present", "- Try to get policy....");
                xmlResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                log("BHO", "Present", "- Contacted Verifier...");
            }
            catch (WebException e)
            {
                log("BHO", "Present", "- WebException : " + e);
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
                    return new ReturObject { status = 500, message = "Could not contact policyserver." };
                }
            }
            if (xmlResponse.StatusCode != HttpStatusCode.OK)
            {
                log("BHO", "Present", "- No Policy from Server " + xmlResponse.StatusCode + " " + xmlResponse);

                return new ReturObject{ status = (int)xmlResponse.StatusCode, message = "Failed to obtain presentation policy"};
            }
            log("BHO", "Present", "- OK From Policy from Server " + xmlResponse.StatusCode + " " + xmlResponse);

            var xmlFromStream = xmlResponse.GetResponseStream();
            log("BHO", "Present", "- got Response Stream! " + xmlFromStream);

            //
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/createPresentationToken/" + sessionID);
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
            byte[] b = new byte[1];
            while (xmlFromStream.Read(b,0,1)!=0)
            {
                abcToStream.Write(b, 0, 1);
            }
            xmlFromStream.Close();
            abcToStream.Close();

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
            {
                abcResponse.Close();
                var selectionUIURL = USER_UI_SERVICE + "?mode=presentation&demo=false&language=" + language + "&sessionid=" + sessionID;
                log("BHO", "Present", "- selectionUIURL : " + selectionUIURL);

                var form = new WebForm { URL = selectionUIURL };
                var dialogResponse = form.ShowDialog();
                log("BHO", "Present", "- dialogResponse : " + dialogResponse);

                // get presentation token... maybe...
                abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/createPresentationTokenIdentitySelection/" + sessionID);
                abcRequest.Method = "POST";
                abcRequest.Accept = "text/xml,application/xml";
                abcRequest.ContentType = "text/plain";

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                abcStatus = (int)abcResponse.StatusCode;
                log("BHO", "Present", "- abc service response : " + abcStatus);

                if (abcStatus != 200)
                {
                    return new ReturObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                }
            }

            log("BHO", "Present", "- setup verify post");
            var xmlVerifyRequest = (HttpWebRequest)WebRequest.Create(verify_url);
            xmlVerifyRequest.Method = "POST";
            xmlVerifyRequest.Accept = "text/xml,application/xml";
            xmlVerifyRequest.ContentType = "application/xml";

            log("BHO", "Present", "- get response stream from ABC...");
            var abcFromStream = abcResponse.GetResponseStream();

            log("BHO", "Present", "- get request stream to verifier...");
            var xmlToStream = xmlVerifyRequest.GetRequestStream();
            log("BHO", "Present", "- send xml to verifier...");

            while (abcFromStream.Read(b, 0, 1) != 0)
            {
                xmlToStream.Write(b, 0, 1);
            }
            abcFromStream.Close();
            xmlToStream.Close();

            HttpWebResponse xmlVerifyResponse = (HttpWebResponse)xmlVerifyRequest.GetResponse();
            int xmlVerifyStatus = (int)xmlVerifyResponse.StatusCode;
            if (xmlVerifyStatus != 200 && xmlVerifyStatus != 202 && xmlVerifyStatus != 204)
            {
                return new ReturObject { status = xmlVerifyStatus, message = "Failed to contact verifier" };
            }
            return new ReturObject { status = xmlVerifyStatus, message = "Verfication was succesful" };
        } catch(Exception e) {
            log("BHO", "Present", "- Present Exception : " + e);
        }

            return new ReturObject { status = 504, message = "Bail out after exception." };

        }


        public ReturObject StoreData(string data)
        {
            log("BHO", "StoreData", "Start! " + data);

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "StoreDataIE" + milliseconds;

                    // todo : check smartcard
            ReturObject error = CheckSmartCard(sessionID);
            if (error!=null) return error;

            //
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/storeData/" + sessionID);
            abcRequest.Method = "POST";
            abcRequest.ContentType = "text/plain";
            abcRequest.Accept = "text/plain";
            Stream abcToStream; // = abcRequest.GetRequestStream();
            try
            {
                abcToStream = abcRequest.GetRequestStream();
                System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
                Byte[] dataBytes = encoding.GetBytes(data);

                log("BHO", "StoreData", "- got Request Stream! " + abcToStream);
                abcToStream.Write(dataBytes, 0, dataBytes.Length);

                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                log("BHO", "StoreData", "- response ! " + abcResponse.StatusCode);
                abcToStream.Close();

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
                        return new ReturObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
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

        public ReturObject LoadData()
        {
            log("BHO", "StoreData", "Start");

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

                    System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
                    string data = "";
                    byte[] b = new byte[1];
                    while (abcFromStream.Read(b, 0, 1) != 0)
                    {
                        data += encoding.GetString(b);
                    }
                    abcFromStream.Close();
                    return new ReturObject { status = 200, message = "Ok", data = data };
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
                            log("BHO", "StoreData", "Uknown Response calling LoadData : " + e);
                            return new ReturObject { status = 400, message = "Uknown Response calling LoadData : " + e };
                        }
                    }
                    else
                    {
                        log("BHO", "StoreData", "could not contact User ABC Service : " + e);
                        return new ReturObject { status = 400, message = "User ABCE Service Not Running" };
                    }
                }
            }

    }
}
