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
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{
    public class UserServiceCalls
    {
        public static string USER_ABCE_SERVICE = "http://localhost:9300/idselect-user-service";
        public static string USER_UI_SERVICE = "http://localhost:9093/user-ui";

        private static ResultObject CheckSmartCardInserted(string sessionID)
        {
            BrowserHelperObject.log("BHO", "CheckSmartCardInserted", "SessionID : " + sessionID);

            HttpWebResponse abcResponse = null;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
            abcRequest.Method = "GET";
            try
            {
                BrowserHelperObject.log("BHO", "CheckSmartCardInserted", " - call rest : " + USER_ABCE_SERVICE + "/user/checkSmartcard/" + sessionID);
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                if (abcResponse.StatusCode == HttpStatusCode.NoContent)
                {
                    BrowserHelperObject.log("BHO", "CheckSmartCardInserted", " - ' no content ' we have same card! " + abcResponse.StatusCode);
                    // we have card...
                }
                return new ResultObject { status = (int)abcResponse.StatusCode, message = "Not An error" };

            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        int statusCodeX = (int)response.StatusCode;
                        if(statusCodeX == 410) 
                        {
                            return new ResultObject { status = statusCodeX, message = "Please ensure that your smartcard is connected before continuing and do not remove it during the process!" };
                        }
                        else if(statusCodeX == 406) 
                        {
                            // card present / but not unlocked
                            return new ResultObject { status = statusCodeX, message = "Card is present but is 'locked'." };
                        }
                        else
                        {
                            return new ResultObject { status = statusCodeX, message = "Uknown Response calling checkSmartcard : " + e };
                        }
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "CheckSmartCard", "Uknown Response calling checkSmartcard : " + e);
                        return new ResultObject { status = 400, message = "Uknown Response calling checkSmartcard : " + e };
                    }
                }
                else
                {
                    BrowserHelperObject.log("BHO", "CheckSmartCard", "could not contact User ABC Service : " + e);
                    return new ResultObject { status = 400, message = "User ABCE Service Not Running" };
                }

                // could not contact User ABC Service
            }
        }

        // return null if ok...
        private static ResultObject CheckSmartCard(string sessionID)
        {

            BrowserHelperObject.log("BHO", "CheckSmartCard", "SessionID : " + sessionID);
            ResultObject cardInserted = CheckSmartCardInserted(sessionID);

            if (cardInserted.status == 204)
            {
                return null;
            }
            if (cardInserted.status!=406 && cardInserted.status >= 400)
            { 
                return cardInserted;
            }

            BrowserHelperObject.log("BHO", "CheckSmartCard", " - show Pin dialog!!");
            PinForm pf = new PinForm();

            string pin = null;
            var res = pf.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK)
            {
                // pin not entered
                BrowserHelperObject.log("BHO", "CheckSmartCard", " - user canceled..." + res);
                return new ResultObject { status = 400, message = "Pin not entered" };
            }
            else
            {
                pin = pf.getPin();
                if (pin.Length != 4)
                {
                    return new ResultObject { status = 400, message = "Pin must be 4 digits." };
                }
                BrowserHelperObject.log("BHO", "CheckSmartCard", " - user OK! " + res);
            }

            BrowserHelperObject.log("BHO", "CheckSmartCard", " - show dialog DONE!! " + res);

            // unlock
            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] pinBytes = encoding.GetBytes(pin);

            HttpWebResponse abcResponse = null;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/unlockSmartcards/" + sessionID);
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                BrowserHelperObject.log("BHO", "CheckSmartCard", " - rest call 'unlockSmartard'");
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
                        return new ResultObject { status = 400, message = "Uknown Response calling unlock : : " + e };
                    }
                }
                else
                {
                    return new ResultObject { status = 400, message = "Uknown Response calling unlock, not ProtocolError: " + e };
                }
            }
            finally
            {
                if (abcResponse != null) abcResponse.Close();
            }

            if (statusCode >= 200 && statusCode <= 204)
            {
                // PIN OK!
                BrowserHelperObject.log("BHO", "CheckSmartCard", " - PIN OK!");
                return null;
            }
            if (statusCode == 401)
            {
                BrowserHelperObject.log("BHO", "CheckSmartCard", "Smartcard Not Accepted - wrong PIN entered. Try again");
                return new ResultObject { status = statusCode, message = "Smartcard Not Accepted - wrong PIN entered. Try again" };
            }
            if (statusCode == 403)
            {
                BrowserHelperObject.log("BHO", "CheckSmartCard", "Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code");
                return new ResultObject { status = statusCode, message = "Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code" };
            }
            BrowserHelperObject.log("BHO", "CheckSmartCard", "Smartcard Not Accepted - Unknown cause " + statusCode);
            return new ResultObject { status = statusCode, message = "Smartcard Not Accepted - Unknown cause" };
        }

        public static ResultObject IsSameSmartcard()
        {
            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "IsSameSmartcardIE" + milliseconds;

            BrowserHelperObject.log("BHO", "IsSameSmartcard", "SessionID : " + sessionID);
            ResultObject cardInserted = CheckSmartCardInserted(sessionID);
            if (cardInserted.status >= 400)
            {
                return new ResultObject { status = 400, message = "Missing UserService/No Card Inserted/PIN Needed" };
            }

            HttpWebResponse abcResponse = null;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/isSameSmartcard/" + sessionID);
            abcRequest.Method = "GET";
            try
            {
                BrowserHelperObject.log("BHO", "IsSameSmartcardIE", " - call rest : " + USER_ABCE_SERVICE + "/user/isSameSmartcard/" + sessionID);
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                return new ResultObject { status = 200, message = "Same Smartcard" };
            }
            catch (WebException)
            {
        		// ignore - always 410
            }
            return new ResultObject { status = 410, message = "Not Same Smartcard" };
        }


        public static ResultObject Issue(string language, string start_url, string step_url, string status_url, string optional_cookie)
        {

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "IssuanceIE" + milliseconds;

            BrowserHelperObject.log("BHO", "Issue", "Start : " + language + " " + start_url + " " + step_url + " " + status_url);

            // todo : check smartcard
            ResultObject error = CheckSmartCard(sessionID);
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
            BrowserHelperObject.log("BHO", "Issue", "- xmlPolicyRequest.ServicePoint : " + xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlPolicyRequest.ServicePoint.ConnectionLimit);

            // set cookie
            if (!String.IsNullOrEmpty(optional_cookie))
            {
                xmlPolicyRequest.Headers.Add("Cookie", optional_cookie);
                BrowserHelperObject.log("BHO", "Issue", "- Set Cookie : " + optional_cookie);
            }
            else
            {
                BrowserHelperObject.log("BHO", "Issue", "- No Cookie");
            }

            HttpWebResponse xmlResponse;
            try
            {
                BrowserHelperObject.log("BHO", "Issue", "- Try to get initial issuance message - Timeout : " + xmlPolicyRequest.ReadWriteTimeout);
                xmlResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                BrowserHelperObject.log("BHO", "Issue", "- Contacted Issuer...");
            }
            catch (WebException e)
            {
                BrowserHelperObject.log("BHO", "Issuer", "- WebException - getting Issuance Policy : " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        return new ResultObject { status = 500, message = "Unhandled error : " + e.Status };
                    }

                }
                else
                {
                    return new ResultObject { status = 500, message = "Could not contact issuanceserver." };
                }
            }
            if (xmlResponse.StatusCode != HttpStatusCode.OK)
            {
                BrowserHelperObject.log("BHO", "Issuer", "- Failed to obtain issuance policy from issuance server " + xmlResponse.StatusCode + " " + xmlResponse);

                return new ResultObject { status = (int)xmlResponse.StatusCode, message = "Failed to obtain issuance policy from issuance server" };
            }
            BrowserHelperObject.log("BHO", "Issuer", "- OK From Issuance Policy from Server " + xmlResponse.StatusCode + " " + xmlResponse);

            var xmlFromStream = xmlResponse.GetResponseStream();
            BrowserHelperObject.log("BHO", "Issuer", "- got Response Stream! " + xmlFromStream);

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
                    BrowserHelperObject.log("BHO", "Issuer", "- WebException - on step - contacting ABCE : " + e);
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            int statusCode = (int)response.StatusCode;
                            if (statusCode == 501)
                            {
                                // specific check for insufficient storage. 
                                MessageBox.Show("Insufficient storage on card. Please try to use check revocation status from the ABC4Trust menu.",
                                    "Smart Card Error!",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Exclamation,
                                    MessageBoxDefaultButton.Button1);

                                return new ResultObject { status = 501, message = "Insufficient storage on card." };
                            }
                            else
                            {
                                return new ResultObject { status = statusCode, message = response.StatusDescription };
                            }
                        }
                        else
                        {
                            return new ResultObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ResultObject { status = 400, message = "User ABCE Not Running (Issuance)" };
                    }
                }

                BrowserHelperObject.log("BHO", "Issuer", "- got Request Stream! " + abcToStream);
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
                //                BrowserHelperObject.log("BHO", "Issuer", "- xml from Issuer - length : " + issuer2ABCEBytes.Length + " : " + issuer2ABCEString);
                BrowserHelperObject.log("BHO", "Issuer", "- xml from Issuer - length : " + issuer2ABCEBytes.Length);

                abcToStream.Write(issuer2ABCEBytes, 0, issuer2ABCEBytes.Length);
                abcToStream.Close();

                BrowserHelperObject.log("BHO", "Issuer", "- get status from ABCE UserService...");
                HttpWebResponse abcResponse;
                try
                {
                    abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                }
                catch (WebException e)
                {
                    BrowserHelperObject.log("BHO", "Issuer", "- WebException - on step - sending to ABCE : " + e);
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            int statusCode = (int)response.StatusCode;
                            if (statusCode == 501)
                            {
                                // specific check for insufficient storage. 
                                MessageBox.Show("Insufficient storage on card. Please try to use check revocation status from the ABC4Trust menu.",
                                    "Smart Card Error!",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Exclamation,
                                    MessageBoxDefaultButton.Button1);

                                return new ResultObject { status = 501, message = "Insufficient storage on card." };
                            }
                            else
                            {
                                return new ResultObject { status = statusCode, message = response.StatusDescription };
                            }
                        }
                        else
                        {
                            return new ResultObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ResultObject { status = 400, message = "User ABCE Not Running (Issuance)" };
                    }
                }

                var abcStatus = (int)abcResponse.StatusCode;
                BrowserHelperObject.log("BHO", "Issue", "- response from user service : " + abcResponse + " " + abcStatus);

                if (abcStatus != 200 && abcStatus != 203 && abcStatus != 204)
                {
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    return new ResultObject { status = abcStatus, message = "Local ABC engine did not return an Issuancemessage." };
                }

                // we now have step response from User ABCE...
                if (abcStatus == 203)
                {
                    // select credentials
                    abcResponse.Close();
                    var selectionUIURL = USER_UI_SERVICE + "?mode=issuance&demo=false&language=" + language + "&sessionid=" + sessionID;
                    BrowserHelperObject.log("BHO", "Issue", "- selectionUIURL : " + selectionUIURL);

                    var form = new WebForm { URL = selectionUIURL };

                    var dialogResponse = form.ShowDialog();
                    BrowserHelperObject.log("BHO", "Issue", "- dialogResponse : " + dialogResponse);

                    // get presentation token... maybe...
                    abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/issuanceProtocolStepSelect/" + sessionID +
                        "?startRequest=" + start_request_mod +
                        "&stepRequest=" + step_request_mod);
                    abcRequest.Method = "POST";
                    abcRequest.Accept = "text/xml,application/xml";
                    abcRequest.ContentType = "text/plain";

                    abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                    abcStatus = (int)abcResponse.StatusCode;
                    BrowserHelperObject.log("BHO", "Issue", "- abc service response : " + abcStatus);

                    if (abcStatus != 200)
                    {
                        reportIssuanceStatus(status_url, false, optional_cookie);
                        return new ResultObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                    }
                }

                // we now have issuance message ready on abcResponse
                if (abcStatus == 204)
                {
                    reportIssuanceStatus(status_url, true, optional_cookie);
                    return new ResultObject { status = abcStatus, message = "Credential obtained... SUCCESS" };
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
                BrowserHelperObject.log("BHO", "Issue", "- xmlStepRequest.ServicePoint : " + xmlStepRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlStepRequest.ServicePoint.ConnectionLimit);

                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    xmlStepRequest.Headers.Add("Cookie", optional_cookie);
                }

                HttpWebResponse xmlStepResponse;
                try
                {
                    BrowserHelperObject.log("BHO", "Issue", "- Send Step IssuanceMessage to Issuer....");
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
                    //                    BrowserHelperObject.log("BHO", "Issuer", "- xml from ABCE - length : " + abce2IssuerBytes.Length + " : " + abce2IssuerString);
                    BrowserHelperObject.log("BHO", "Issuer", "- xml from ABCE - length : " + abce2IssuerBytes.Length);

                    var xmlToStream = xmlStepRequest.GetRequestStream();
                    xmlToStream.Write(abce2IssuerBytes, 0, abce2IssuerBytes.Length);
                    xmlToStream.Close();
                    xmlToStream.Dispose();

                    BrowserHelperObject.log("BHO", "Issue", "- get Response from Issuer step...");

                    xmlStepResponse = (HttpWebResponse)xmlStepRequest.GetResponse();
                    BrowserHelperObject.log("BHO", "Issue", "- Contacted Issuer...");
                }
                catch (WebException e)
                {
                    reportIssuanceStatus(status_url, false, optional_cookie);
                    BrowserHelperObject.log("BHO", "Issue", "- WebException : " + e);
                    if (e.Status == WebExceptionStatus.ProtocolError)
                    {
                        var response = e.Response as HttpWebResponse;
                        if (response != null)
                        {
                            BrowserHelperObject.log("BHO", "Issue", "- Error : " + (int)response.StatusCode + " : " + response.StatusDescription);
                            return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                        }
                        else
                        {
                            BrowserHelperObject.log("BHO", "Issue", "- Unhandled error : " + e.Status);
                            return new ResultObject { status = 500, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "Issue", "- Could not contact issuanceserver.");
                        return new ResultObject { status = 500, message = "Could not contact issuanceserver." };
                    }
                }
                catch (Exception e)
                {
                    BrowserHelperObject.log("BHO", "Issue", "- Internal Error ? : " + e);
                    return new ResultObject { status = 500, message = "Internal Error ? : " + e };
                }

                // skip verify response from issuer! - seems we never stop on issuer side...
                int xmlStepStatus = (int)xmlStepResponse.StatusCode;
                BrowserHelperObject.log("BHO", "Issue", "- xmlStepStatus sending Step to Issuer : " + xmlStepStatus + " - type : " + xmlStepRequest.ContentType + " - length : " + xmlStepRequest.ContentLength);

                // we now have IssuanceMessage from Issuer - continuer loop and send to local User ABC
                xmlFromStream = xmlStepResponse.GetResponseStream();
            }
        }

        private static ResultObject reportIssuanceStatus(string status_url, bool success, string optional_cookie)
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
                            return new ResultObject { status = statusCodeX, message = "Failuer contacting Issuer Status Service" };
                        }
                        else
                        {
                            return new ResultObject { status = 500, message = "Uknown Response calling Issuer Status : " + e };
                        }
                    }
                    else
                    {
                        return new ResultObject { status = 500, message = "Could not contact Issuer Status Service" };
                    }

                }

            }
            return new ResultObject { status = 200, message = "Ok" };

        }

        public static ResultObject Present(string language, string policy_url, string verify_url, string optional_cookie)
        {
            try
            {

                long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
                var sessionID = "PresentIE" + milliseconds;

                BrowserHelperObject.log("BHO", "Present", "Start : " + language + " " + policy_url + " " + verify_url + " " + optional_cookie);

                // todo : check smartcard
                ResultObject error = CheckSmartCard(sessionID);
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
                BrowserHelperObject.log("BHO", "Present", "- xmlVerifyRequest.ServicePoint : " + xmlPolicyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlPolicyRequest.ServicePoint.ConnectionLimit);

                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    BrowserHelperObject.log("BHO", "Present", "- Set Cookie for Policy : " + optional_cookie);
                    xmlPolicyRequest.Headers.Add("Cookie", optional_cookie);
                }
                else
                {
                    BrowserHelperObject.log("BHO", "Present", "- No Cookie");
                }

                HttpWebResponse xmlPolicyResponse;
                try
                {
                    BrowserHelperObject.log("BHO", "Present", "- Try to get policy.... " + policy_url + " - timeout : " + xmlPolicyRequest.ReadWriteTimeout);
                    xmlPolicyResponse = (HttpWebResponse)xmlPolicyRequest.GetResponse();
                    BrowserHelperObject.log("BHO", "Present", "- Contacted Verifier... " + xmlPolicyResponse.StatusCode + " : " + xmlPolicyResponse.ContentType + " - content length : " + xmlPolicyResponse.ContentLength);
                }
                catch (Exception e)
                {
                    if (e.GetType() == typeof(System.Net.WebException))
                    {
                        System.Net.WebException we = (System.Net.WebException)e;
                        BrowserHelperObject.log("BHO", "Present", "- WebException - on policy : " + e);
                        if (we.Status == WebExceptionStatus.ProtocolError)
                        {
                            BrowserHelperObject.log("BHO", "Present", "- e.Status : " + we.Status + " - e.Response : " + we.Response);
                            var response = we.Response as HttpWebResponse;
                            BrowserHelperObject.log("BHO", "Present", "- got response ? : " + response);
                            if (response != null)
                            {
                                BrowserHelperObject.log("BHO", "Present", "- response : " + response.StatusCode + " : " + response.StatusDescription);
                                return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                            }
                            else
                            {
                                return new ResultObject { status = 500, message = "Unhandled error : " + we.Status };
                            }

                        }
                        else
                        {
                            return new ResultObject { status = 500, message = "Could not contact policyserver." };
                        }
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "Present", "- Unknown exception  : " + e);
                        return new ResultObject { status = 500, message = "Unknown error  : " + e.Message };
                    }
                }

                if (xmlPolicyResponse.StatusCode != HttpStatusCode.OK)
                {
                    BrowserHelperObject.log("BHO", "Present", "- No Policy from Server " + xmlPolicyResponse.StatusCode + " " + xmlPolicyResponse);

                    return new ResultObject { status = (int)xmlPolicyResponse.StatusCode, message = "Failed to obtain presentation policy" };
                }
                BrowserHelperObject.log("BHO", "Present", "- OK From Policy from Server " + xmlPolicyResponse.StatusCode + " " + xmlPolicyResponse);
                BrowserHelperObject.log("BHO", "Present", "- Policy Response Headers : " + xmlPolicyResponse.Headers);

                var xmlPolicyStream = xmlPolicyResponse.GetResponseStream();
                BrowserHelperObject.log("BHO", "Present", "- got Response Stream! " + xmlPolicyStream);

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
                            return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                        }
                        else
                        {
                            return new ResultObject { status = 400, message = "Unhandled error : " + e.Status };
                        }
                    }
                    else
                    {
                        return new ResultObject { status = 400, message = "User ABCE Not Running (Verification)" };
                    }
                }

                BrowserHelperObject.log("BHO", "Present", "- got Request Stream! " + abcToStream);
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
                BrowserHelperObject.log("BHO", "Present", "- dispose xmlPolicyStream...");
                xmlPolicyStream.Dispose();
                BrowserHelperObject.log("BHO", "Present", "- close xmlPolicyResponse...");
                xmlPolicyResponse.Close();

                policyByteStream.Close();
                byte[] policyBytes = policyByteStream.ToArray();
                //                var xmlPolicyString = new System.Text.UTF8Encoding().GetString(policyBytes);
                //                BrowserHelperObject.log("BHO", "Present", "- xml from Verifier - length : " + policyBytes.Length + " : " + xmlPolicyString);
                BrowserHelperObject.log("BHO", "Present", "- xml from Verifier - length : " + policyBytes.Length + " - abc stream : " + abcToStream);

                abcToStream.Write(policyBytes, 0, policyBytes.Length);
                abcToStream.Close();
                abcToStream.Dispose();

                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                var abcStatus = (int)abcResponse.StatusCode;
                BrowserHelperObject.log("BHO", "Present", "- response from user service : " + abcResponse + " " + abcStatus);

                if (abcStatus == 422)
                {
                    abcResponse.Close();
                    return new ResultObject { status = abcStatus, message = "Can not satisfy policy" };
                }
                if (abcStatus != 200 && abcStatus != 203)
                {
                    abcResponse.Close();
                    return new ResultObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                }
                if (abcStatus == 203)
                {   // 203 - means id selection...

                    abcResponse.Close();
                    var selectionUIURL = USER_UI_SERVICE + "?mode=presentation&demo=false&language=" + language + "&sessionid=" + sessionID;
                    BrowserHelperObject.log("BHO", "Present", "- selectionUIURL : " + selectionUIURL);

                    var form = new WebForm { URL = selectionUIURL };
                    var dialogResponse = form.ShowDialog();
                    // web form is closed
                    BrowserHelperObject.log("BHO", "Present", "- ID Select web form was closed : " + dialogResponse);

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
                        BrowserHelperObject.log("BHO", "Present", "- abc service error obtaining token : " + e);
                        if (e.Status == WebExceptionStatus.ProtocolError)
                        {
                            BrowserHelperObject.log("BHO", "Present", "- abc service error obtaining token - status : " + e.Status + " : " + e.Response);
                            var response = e.Response as HttpWebResponse;
                            if (response != null)
                            {
                                return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                            }
                            else
                            {
                                return new ResultObject { status = 400, message = "Unhandled error : " + e.Status };
                            }
                        }
                        else
                        {
                            return new ResultObject { status = 400, message = "User ABCE Not Running (Verification)" };
                        }
                    }


                    BrowserHelperObject.log("BHO", "Present", "- abc service response : " + abcStatus);

                    if (abcStatus != 200)
                    {
                        return new ResultObject { status = abcStatus, message = "Failed to contact local ABC engine" };
                    }
                }
                // abcResponse should now have ABCE PresentationToken..
                BrowserHelperObject.log("BHO", "Present", "- setup verify post");
                var xmlVerifyRequest = (HttpWebRequest)WebRequest.Create(verify_url);
                xmlVerifyRequest.Method = "POST";
                xmlVerifyRequest.Accept = "text/xml,application/xml";
                xmlVerifyRequest.ContentType = "application/xml; charset=utf-8"; // charset=utf-8
                xmlVerifyRequest.KeepAlive = false;
                xmlVerifyRequest.ReadWriteTimeout = 5000;
                xmlVerifyRequest.ServicePoint.ConnectionLeaseTimeout = 5000;
                xmlVerifyRequest.ServicePoint.MaxIdleTime = 5000;
                xmlVerifyRequest.ServicePoint.ConnectionLimit = 400;
                BrowserHelperObject.log("BHO", "Present", "- xmlVerifyRequest.ServicePoint - keepalive false : " + xmlVerifyRequest.ServicePoint.ConnectionLeaseTimeout + " : " + xmlVerifyRequest.ServicePoint.ConnectionLimit);

                // just hand over cookie 
                if (!String.IsNullOrEmpty(optional_cookie))
                {
                    BrowserHelperObject.log("BHO", "Present", "- Set Cookie for Verify : " + optional_cookie);
                    xmlVerifyRequest.Headers.Add("Cookie", optional_cookie);
                }

                BrowserHelperObject.log("BHO", "Present", "- get response stream from ABC...");
                var abcFromStream = abcResponse.GetResponseStream();

                BrowserHelperObject.log("BHO", "Present", "- get request stream to verifier...");
                var xmlToStream = xmlVerifyRequest.GetRequestStream();
                BrowserHelperObject.log("BHO", "Present", "- send xml to verifier...");

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
                //                BrowserHelperObject.log("BHO", "Present", "- xml to Verifier - length : " + tokenBytes.Length + " : " + xmlTokenString);
                BrowserHelperObject.log("BHO", "Present", "- xml to Verifier - length : " + tokenBytes.Length);

                xmlToStream.Write(tokenBytes, 0, tokenBytes.Length);
                xmlToStream.Close();
                xmlToStream.Dispose();

                HttpWebResponse xmlVerifyResponse;
                try
                {
                    BrowserHelperObject.log("BHO", "Present", "- check response...");
                    xmlVerifyResponse = (HttpWebResponse)xmlVerifyRequest.GetResponse();
                    xmlVerifyResponse.Close();
                    BrowserHelperObject.log("BHO", "Present", "- check response - not exception... - response closed");
                }
                catch (Exception e)
                {
                    if (e.GetType() == typeof(System.Net.WebException))
                    {
                        System.Net.WebException we = (System.Net.WebException)e;
                        BrowserHelperObject.log("BHO", "Present", "- WebException - on verify : " + e);
                        if (we.Status == WebExceptionStatus.ProtocolError)
                        {
                            BrowserHelperObject.log("BHO", "Present", "- e.Status : " + we.Status + " - e.Response : " + we.Response);
                            var response = we.Response as HttpWebResponse;
                            BrowserHelperObject.log("BHO", "Present", "- got response ? : " + response);
                            if (response != null)
                            {
                                BrowserHelperObject.log("BHO", "Present", "- response : " + response.StatusCode + " : " + response.StatusDescription);
                                return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                            }
                            else
                            {
                                return new ResultObject { status = 500, message = "Unhandled error : " + we.Status };
                            }
                        }
                        else
                        {
                            return new ResultObject { status = 500, message = "Could not contact policyserver." };
                        }
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "Present", "- Unknown exception - on verify : " + e);
                        return new ResultObject { status = 500, message = "Unknown error  : " + e.Message };
                    }
                }

                BrowserHelperObject.log("BHO", "Present", "- Token Response Headers : " + xmlVerifyResponse.Headers);

                int xmlVerifyStatus = (int)xmlVerifyResponse.StatusCode;

                BrowserHelperObject.log("BHO", "Present", "- status from verify request : " + xmlVerifyStatus + " " + xmlVerifyResponse.StatusDescription);

                if (xmlVerifyStatus != 200 && xmlVerifyStatus != 202 && xmlVerifyStatus != 204)
                {
                    return new ResultObject { status = xmlVerifyStatus, message = "Failed to contact verifier" };
                }
                return new ResultObject { status = xmlVerifyStatus, message = "Verfication was succesful" };
            }
            catch (Exception e)
            {
                BrowserHelperObject.log("BHO", "Present", "- Present Exception : " + e);
                MessageBox.Show("BHO-Present ", "Exception ! " + e);

            }

            return new ResultObject { status = 504, message = "Bail out after exception." };

        }



        public static ResultObject StoreData(string data)
        {
            BrowserHelperObject.log("BHO", "StoreData", "Start! " + data);

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "StoreDataIE" + milliseconds;

            // todo : check smartcard
            ResultObject error = CheckSmartCard(sessionID);
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

                BrowserHelperObject.log("BHO", "StoreData", "- store ok!");
                return new ResultObject { status = 200, message = "Ok" };
            }
            catch (WebException e)
            {
                BrowserHelperObject.log("BHO", "StoreData", "- exception=! " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        BrowserHelperObject.log("BHO", "StoreData", "Error loading data : " + response.StatusCode + " : " + response.StatusDescription);
                        return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "StoreData", "Uknown Response calling StoreData : " + e);
                        return new ResultObject { status = 400, message = "Uknown Response calling StoreData : " + e };
                    }
                }
                else
                {
                    BrowserHelperObject.log("BHO", "StoreData", "could not contact User ABC Service : " + e);
                    return new ResultObject { status = 400, message = "User ABCE Service Not Running" };
                }
            }
        }

        public static ResultObject LoadData()
        {
            BrowserHelperObject.log("BHO", "LoadData", "Start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "LoadDataIE" + milliseconds;

            // todo : check smartcard
            ResultObject error = CheckSmartCard(sessionID);
            if (error != null) return error;

            //
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/loadData/" + sessionID);
            abcRequest.Headers.Add(HttpRequestHeader.AcceptCharset, "UTF-8");

            abcRequest.Method = "GET";
            abcRequest.ContentType = "text/plain";
            abcRequest.Accept = "text/plain";
            try
            {
                string data = null;
                using(var abcResponse = (HttpWebResponse)abcRequest.GetResponse())
                using(var abcFromStream = abcResponse.GetResponseStream())
                using(var sr = new StreamReader(abcFromStream, Encoding.UTF8))
                {
                    data = sr.ReadToEnd();
                    abcResponse.Close();
                    if(abcFromStream!=null) 
                    {
                        abcFromStream.Close();
                    }
                    sr.Close();
                }

                /*

                                HttpWebResponse abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                                Stream abcFromStream = abcResponse.GetResponseStream();

                                var sr = new StreamReader(abcFromStream, Encoding.UTF8);

                                string data = sr.ReadToEnd();
                abcFromStream.Read
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

                                MessageBox.Show("LOAD DATA : ! ", "DATA : " + data);
                */
                BrowserHelperObject.log("BHO", "LoadData", "Load data ok : " + data);
                return new ResultObject { status = 200, message = "Ok", data = data };
            }
            catch (WebException e)
            {
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        BrowserHelperObject.log("BHO", "LoadData", "Error loading data : " + response.StatusCode + " : " + response.StatusDescription);
                        return new ResultObject { status = (int)response.StatusCode, message = response.StatusDescription };
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "LoadData", "Uknown Response calling LoadData : " + e);
                        return new ResultObject { status = 400, message = "Uknown Response calling LoadData : " + e };
                    }
                }
                else
                {
                    BrowserHelperObject.log("BHO", "LoadData", "could not contact User ABC Service : " + e);
                    return new ResultObject { status = 400, message = "User ABCE Service Not Running" };
                }
            }
        }

        public static void ManageCredentials()
        {
            BrowserHelperObject.log("BHO", "ManageCredentials", "ManageCredentials - start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "ManageCredentialsIE" + milliseconds;
            ResultObject check = CheckSmartCard(sessionID);
            if (check != null)
            {
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
            BrowserHelperObject.log("BHO", "ManageCredentials", "- dialogResponse : " + dialogResponse);
        }

        public static void ShowDebugInfo()
        {
            BrowserHelperObject.log("BHO", "showDebugInfo", "Get Debug Info from UserABCE startup");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "DebugInfoIE" + milliseconds;

            var debugInfoURL = USER_ABCE_SERVICE + "/user/getDebugInfo/" + sessionID;

            var form = new WebForm { URL = debugInfoURL };
            var dialogResponse = form.ShowDialog();
            BrowserHelperObject.log("BHO", "showDebugInfo", "- dialogResponse : " + dialogResponse);

        }

        public static void CheckRevocationStatus()
        {
            BrowserHelperObject.log("BHO", "CheckRevocationStatus", "Check RevocationStatus.");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "RevocationStatusIE" + milliseconds;

            ResultObject check = CheckSmartCard(sessionID);
            if (check != null)
            {
                MessageBox.Show("Check Revocation Status - Unlock Failed : " + check.message,
                    "Check Revocation Status",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            MessageBox.Show("Checking for revocation status. This may take up to a minute. Please do not remove the card from the reader.");

            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/checkRevocationStatus");
            abcRequest.ContentType = "application/xml";
            abcRequest.Method = "POST";
            int statusCode = -1;
            try
            {
                BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - setup call! ");
                var abcRequestStream = abcRequest.GetRequestStream();

                BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - rest call 'CheckRevocationStatus' ");

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                statusCode = (int)abcResponse.StatusCode;

                BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - check OK!");
                MessageBox.Show("Credentials have been check.\nRevoked credentials have been deleted.");
                return;
            }
            catch (WebException e)
            {
                BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - WebException! " + e);
                if (e.Status == WebExceptionStatus.ProtocolError)
                {
                    var response = e.Response as HttpWebResponse;
                    if (response != null)
                    {
                        statusCode = (int)response.StatusCode;
                        BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - RESPONSE : : " + response + " : " + statusCode);
                    }
                    else
                    {
                        BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - NO RESPONSE : : " + response);
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
                    BrowserHelperObject.log("BHO", "CheckRevocationStatus", " - UserABCE Service not available!");
                    MessageBox.Show("UserABCE Service is not Running!",
                        "Error!",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    return;
                }
            }
        }

        public static void ChangePin()
        {
            BrowserHelperObject.log("BHO", "ChangePin", "ChangePin - start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "ChangePinIE" + milliseconds;

            ResultObject cardInserted = CheckSmartCardInserted(sessionID);
            if (cardInserted.status != 406 && cardInserted.status >= 400)
            {
                MessageBox.Show("Change Pin - Unlock Failed : " + cardInserted.message,
                    "Change Pin",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            //
            ChangePinForm changePinForm = new ChangePinForm();
            var res = changePinForm.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK) return;

            if (changePinForm.getCurrentPin().Length != 4 || changePinForm.getNewPin().Length != 4)
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

                BrowserHelperObject.log("BHO", "ChangePin", " - rest call 'ChangePin' ");
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
                BrowserHelperObject.log("BHO", "ChangePin", " - PIN OK!");
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


        public static void UnlockCard()
        {
            BrowserHelperObject.log("BHO", "UnlockCard", "UnlockCard - start");

            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "UnlockCardIE" + milliseconds;
            ResultObject cardInserted = CheckSmartCardInserted(sessionID);
            if (cardInserted.status != 406 && cardInserted.status >= 400)
            {
                MessageBox.Show("Unlock Card Failed : " + cardInserted.message,
                    "Unlock Card",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

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

                BrowserHelperObject.log("BHO", "UnlockCard", " - rest call 'unlockCard' ");
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
                BrowserHelperObject.log("BHO", "UnlockCard", " - Unlock OK!");
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


        public static void BackupSmartcard()
        {
            BrowserHelperObject.log("BHO", "BackupSmartcard", "BackupSmartcard - start");
            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "BackupSmartcardIE" + milliseconds;

            ResultObject check = CheckSmartCard(sessionID);
            if (check != null)
            {
                MessageBox.Show("Backup Smartcard - Failed : " + check.message,
                    "Backup Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            HttpWebResponse abcResponse;
            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/backupExists/" + sessionID);
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "GET";
            try
            {
                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
                if (abcResponse.StatusCode == HttpStatusCode.NoContent)
                {
                    BrowserHelperObject.log("BHO", "BackupSmartcard", " - ' no content ' backup file exist!");

                    var result = MessageBox.Show("Backup file already exists. Pressing ok will overwrite it. Are you sure you want to continue?",
                        "Backup Smartcard",
                        MessageBoxButtons.YesNo,
                        MessageBoxIcon.Exclamation,
                        MessageBoxDefaultButton.Button1);
                    if (result != System.Windows.Forms.DialogResult.Yes) 
                    {
                        return;
                    }
                }
            }
            catch (WebException e)
            {
                MessageBox.Show("Check if backup file existe failed : " + e,
                    "Backup Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            PasswordForm pf = new PasswordForm();

            var resPf = pf.ShowDialog();
            if(resPf == System.Windows.Forms.DialogResult.Cancel)
            {
                return;
            }
            string password = pf.getPassword();
            if(password.Length != 8)
            {
                MessageBox.Show("Password must be 8 chars",
                    "Backup Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            // unlock
            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] passwordBytes = encoding.GetBytes(password);

            abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/backupSmartcard/" + sessionID);
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                BrowserHelperObject.log("BHO", "BackupSmartcard", " - rest call 'backupSmartcard'");
                abcRequestStream.Write(passwordBytes, 0, passwordBytes.Length);
                abcRequestStream.Close();
                abcRequestStream.Dispose();

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
            }
            catch (WebException e)
            {
                BrowserHelperObject.log("BHO", "BackupSmartcard", " - Error calling 'backupSmartcard' " + e);
                MessageBox.Show("Something went wrong during backup! " + e,
                    "Backup Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            BrowserHelperObject.log("BHO", "BackupSmartcard", " - Success calling 'backupSmartcard' ");
            MessageBox.Show("Card backed up successfully",
                "Backup Smartcard",
                MessageBoxButtons.OK,
                MessageBoxIcon.Exclamation,
                MessageBoxDefaultButton.Button1);
            return;

        }

        internal static void RestoreSmartcard()
        {
            BrowserHelperObject.log("BHO", "RestoreSmartcard", "RestoreSmartcard- start");
            long milliseconds = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond;
            var sessionID = "RestoreSmartcardIE" + milliseconds;

            ResultObject check = CheckSmartCard(sessionID);
            if (check != null)
            {
                MessageBox.Show("Restore Smartcard - Failed : " + check.message,
                    "Restore Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            HttpWebResponse abcResponse;
            PasswordForm pf = new PasswordForm();

            var resPf = pf.ShowDialog();
            if (resPf == System.Windows.Forms.DialogResult.Cancel)
            {
                return;
            }
            string password = pf.getPassword();
            if (password.Length != 8)
            {
                MessageBox.Show("Password must be 8 chars",
                    "Backup Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }

            // unlock
            System.Text.ASCIIEncoding encoding = new System.Text.ASCIIEncoding();
            Byte[] passwordBytes = encoding.GetBytes(password);

            var abcRequest = (HttpWebRequest)WebRequest.Create(USER_ABCE_SERVICE + "/user/restoreSmartcard/" + sessionID);
            abcRequest.ContentType = "text/plain";
            abcRequest.Method = "POST";
            try
            {
                var abcRequestStream = abcRequest.GetRequestStream();

                BrowserHelperObject.log("BHO", "RestoreSmartcard", " - rest call 'restoreSmartcard'");
                abcRequestStream.Write(passwordBytes, 0, passwordBytes.Length);
                abcRequestStream.Close();
                abcRequestStream.Dispose();

                abcResponse = (HttpWebResponse)abcRequest.GetResponse();
            }
            catch (WebException e)
            {
                BrowserHelperObject.log("BHO", "RestoreSmartcard", " - Error calling 'restoreSmartcard' " + e);
                MessageBox.Show("Something went wrong during restoration! " + e,
                    "Restore Smartcard",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Exclamation,
                    MessageBoxDefaultButton.Button1);
                return;
            }
            BrowserHelperObject.log("BHO", "RestoreSmartcard", " - Success calling 'restoreSmartcard' ");
            MessageBox.Show("Card restored successfully!",
                "Restore Smartcard",
                MessageBoxButtons.OK,
                MessageBoxIcon.Exclamation,
                MessageBoxDefaultButton.Button1);
            return;
        }
    }
}
