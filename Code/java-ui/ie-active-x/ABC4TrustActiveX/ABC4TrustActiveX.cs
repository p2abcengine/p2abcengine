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
using System.Threading;

namespace ABC4TrustActiveX
{

/*
    [Guid("ECA5DD1D-096E-440c-BA6A-0118D351650B")]
    [ComVisible(true)]
    [InterfaceType(ComInterfaceType.InterfaceIsIDispatch)]
    public interface IComEvents
    {
        [DispId(0x00000001)]
        void ABC4TrustEvent(ResultObject args);
    }
*/

    /// ABC4TrustActiveX

    /// </summary>

    [ProgId("ABC4TrustActiveX.BrowserHelperObject")]
    [ClassInterface(ClassInterfaceType.AutoDual)]
    [Guid("BC9BB501-2EF6-4293-84DF-58884DB385DC")]
    [ComVisible(true)]
//    [ComSourceInterfaces(typeof(IComEvents))]
    public class BrowserHelperObject : IOleCommandTarget, IObjectSafety
    {
        #region Registering with regasm
        public static string RegBHO = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Browser Helper Objects";
        public static string RegCmd = "Software\\Microsoft\\Internet Explorer\\Extensions";
        public static string TestRegCmd = "Software\\Microsoft\\Internet Explorer\\MenuExt";

        // Pre Approved! HKEY_LOCAL_MACHINE
        public static string PreApproved = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Ext\\PreApproved";
//        public static string PreApproved = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Ext\\PreApproved\\{BC9BB501-2EF6-4293-84DF-58884DB385DC}";

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
            // Meny Command
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
            {
                log("BHO", "RegisterActiveX", "PreApprove Active X : " + PreApproved);

                RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(PreApproved, true);
                if (registryKey == null)
                {
                    log("BHO", "RegisterActiveX", " - create key PreApproved");
                    registryKey = Registry.LocalMachine.CreateSubKey(PreApproved);
                }
                RegistryKey key = registryKey.OpenSubKey(guid);
                if (key == null)
                {
                    log("BHO", "RegisterActiveX", " - create key PreApproved " + guid);
                    key = registryKey.CreateSubKey(guid);
                }

                registryKey.Close();
            }

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
                        registryKey.Close();
                    }
                }
                log("BHO", "UnregisterActiveX", "Menu Unregistered OK...");
                {

                    RegistryKey registryKey = Registry.LocalMachine.OpenSubKey(PreApproved, true);
                    if (registryKey != null)
                    {
                        RegistryKey key = registryKey.OpenSubKey(guid);
                        if (key != null)
                        {
                            registryKey.DeleteSubKey(guid, false);
                            log("BHO", "UnregisterActiveX", "Remove PreApproved Active X : " + PreApproved);
                        }
                        registryKey.Close();
                    }
                }
            }
        }
        #endregion


        #region Implementation of IOleCommandTarget
        [ComVisible(false)]
        public int QueryStatus(IntPtr pguidCmdGroup, uint cCmds, ref OLECMD prgCmds, IntPtr pCmdText)
        {
            log("BHO.IOleCommandTarget", "QueryStatus", "...");

            return 0;
        }
        [ComVisible(false)]
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

            var res = toolsForm.ShowDialog();
            if (res != System.Windows.Forms.DialogResult.OK)
            {
                // pin not entered
                log("BHO.IOleCommandTarget", "Exec", " - user canceled..." + res);
            }
            else
            {
                int tag = int.Parse("" + toolsForm.tag);
/*
                MessageBox.Show("Tool selected : " + tag,
    "Tool selected",
    MessageBoxButtons.OK,
    MessageBoxIcon.Exclamation,
    MessageBoxDefaultButton.Button1);
*/

                log("BHO.IOleCommandTarget", "Exec", " - user selected : " + toolsForm.tag + " : " + toolsForm.sender + " - select button/tag : " + tag);
                switch (tag)
                {
                    case 1 :
                        // Manager credentials
                        UserServiceCalls.ManageCredentials();
                        break;

                    case 2:
                        // Backup Smartcard
                        UserServiceCalls.BackupSmartcard();
                        break;
                    case 3:
                        // Restore Smartcard
                        UserServiceCalls.RestoreSmartcard();
                        break;
                    case 4:
                        // Change Pin
                        UserServiceCalls.ChangePin();
                        break;
                    case 5:
                        // Unlock Smartcard
                        UserServiceCalls.UnlockCard();
                        break;
                    case 6:
                        // Debug Info
                        UserServiceCalls.ShowDebugInfo();
                        break;
                    case 7:
                        // Debug Info
                        UserServiceCalls.CheckRevocationStatus();
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

        [ComVisible(false)]
        public int GetInterfaceSafetyOptions(ref Guid riid, out int pdwSupportedOptions, out int pdwEnabledOptions)
        {
            pdwSupportedOptions = INTERFACESAFE_FOR_UNTRUSTED_CALLER | INTERFACESAFE_FOR_UNTRUSTED_DATA;
            pdwEnabledOptions = INTERFACESAFE_FOR_UNTRUSTED_CALLER | INTERFACESAFE_FOR_UNTRUSTED_DATA;
//            log("BHO.IObjectSafety", "GetInterfaceSafetyOptions", "Params : " + riid + " " + pdwSupportedOptions + " " + pdwEnabledOptions);
            return S_OK;
        }

        [ComVisible(false)]
        public int SetInterfaceSafetyOptions(ref Guid riid, int dwOptionSetMask, int dwEnabledOptions)
        {
//            log("BHO.IObjectSafety", "SetInterfaceSafetyOptions", "Params : " + riid + " " + dwOptionSetMask + " " + dwEnabledOptions);
            return S_OK;
        }
        #endregion



        private static bool logFailureShowed = false;
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

                    // MessageBox.Show(clazz + "::" + method + " : " + msg, "Log failed!");
                }
            }
            catch(Exception)
            {
                if(! logFailureShowed )
                {
                    // MessageBox.Show("LOG FAILED : " + clazz + "::" + method + " : " + msg, "Log failed!");
                    logFailureShowed = true;
                }
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

        [ComVisible(true)]
        public String SayHello()
        {
            return "Hello World!";
        }

        [ComVisible(true)]
        public ResultObject IssueNoCookie(string language, string start_url, string step_url, string status_url)
        {
            return Issue(language, start_url, step_url, status_url, null);
        }


        [ComVisible(true)]
        public ResultObject Issue(string language, string start_url, string step_url, string status_url, string optional_cookie)
        {
            return UserServiceCalls.Issue(language, start_url, step_url, status_url, optional_cookie);
        }

        [ComVisible(true)]
        public ResultObject PresentNoCookie(string language, string policy_url, string verify_url)
        {
            return Present(language, policy_url, verify_url, null);
        }

        [ComVisible(true)]
        public ResultObject Present(string language, string policy_url, string verify_url, string optional_cookie)
        {
            return UserServiceCalls.Present(language, policy_url, verify_url, optional_cookie);
        }

        [ComVisible(true)]
        public ResultObject StoreData(string data)
        {
            return UserServiceCalls.StoreData(data);
        }

        [ComVisible(true)]
        public ResultObject LoadData()
        {
            return UserServiceCalls.LoadData();
        }

        [ComVisible(true)]
        public ResultObject IsSameSmartcard()
        {
            return UserServiceCalls.IsSameSmartcard();
        }

        [ComVisible(false)]
        public delegate void ABC4TrustEventHandler(ResultObject args);

        public event ABC4TrustEventHandler ABC4TrustEvent;

        public void Dispose()
        {
            System.Windows.Forms.MessageBox.Show("MyComComponent is now disposed");
        }

        private class SayHelloWorker
        {
            ABC4TrustEventHandler callback;
            int handle;
            string msg;
            public SayHelloWorker(ABC4TrustEventHandler callback, int handle, string msg)
            {
                this.callback = callback;
                this.handle = handle;
                this.msg = msg;
            }

            public void doit()
            {
                Thread.Sleep(5000);
                ResultObject result = new ResultObject { message = "Everything is OK!", status = 200, data = "Hello World : " + msg };
                result.handle = handle;
                callback(result);
            }

        }

        [ComVisible(true)]
        public string SayHelloBlocking(string arg)
        {
            return "Hello Blocking World : " + arg;
        }

        [ComVisible(true)]
        public bool SayHelloCallback(int handle, string arg)
        {
            if (ABC4TrustEvent != null)
            {
                SayHelloWorker fic = new SayHelloWorker(ABC4TrustEvent, handle, arg);

                Thread detectThread = new Thread(new ThreadStart(fic.doit));
                detectThread.Start();
                return true;
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("No EventHandler / Event for callback !!");
                return false;
            }
        }

        private class IssueWorker
        {
            ABC4TrustEventHandler callback;
            int handle;
            string language, start_url, step_url, status_url, optional_cookie;

            public IssueWorker(ABC4TrustEventHandler callback, int handle, string language, string start_url, string step_url, string status_url, string optional_cookie)
            {
                this.callback = callback;
                this.handle = handle;
                this.language = language;
                this.start_url = start_url;
                this.step_url = step_url;
                this.status_url = status_url;
                this.optional_cookie = optional_cookie;
            }

            public void doit()
            {
                ResultObject result = UserServiceCalls.Issue(language, start_url, step_url, status_url, optional_cookie);
                result.handle = handle;
                callback(result);
            }

        }

        [ComVisible(true)]
        public bool IssueCallback(int handle, string language, string start_url, string step_url, string status_url, string optional_cookie)
        {
            if (ABC4TrustEvent != null)
            {
                IssueWorker iw = new IssueWorker(ABC4TrustEvent, handle, language, start_url, step_url, status_url, optional_cookie);

                Thread detectThread = new Thread(new ThreadStart(iw.doit));
                detectThread.Start();
                return true;
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("No EventHandler / Event for callback !!");
                return false;
            }
        }

        private class PresentWorker
        {
            ABC4TrustEventHandler callback;
            int handle;
            string language, policy_url, verify_url, optional_cookie;

            public PresentWorker(ABC4TrustEventHandler callback, int handle, string language, string policy_url, string verify_url, string optional_cookie)
            {
                this.callback = callback;
                this.handle = handle;
                this.language = language;
                this.policy_url = policy_url;
                this.verify_url = verify_url;
                this.optional_cookie = optional_cookie;
            }

            public void doit()
            {
                ResultObject result = UserServiceCalls.Present(language, policy_url, verify_url, optional_cookie);
                result.handle = handle;
                callback(result);
            }

        }

        [ComVisible(true)]
        public bool PresentCallback(int handle, string language, string policy_url, string verify_url, string optional_cookie)
        {

            if (ABC4TrustEvent != null)
            {
                PresentWorker pw = new PresentWorker(ABC4TrustEvent, handle, language, policy_url, verify_url, optional_cookie);

                Thread detectThread = new Thread(new ThreadStart(pw.doit));
                detectThread.Start();
                return true;
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("No EventHandler / Event for callback !!");
                return false;
            }
        }

        [ComVisible(true)]
        public bool StoreDataCallback(int handle, string data)
        {
            if (ABC4TrustEvent != null)
            {
                DataWorker dw = new DataWorker(ABC4TrustEvent, handle, data);

                Thread detectThread = new Thread(new ThreadStart(dw.doit));
                detectThread.Start();
                return true;
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("No EventHandler / Event for callback !!");
                return false;
            }
        }

        [ComVisible(true)]
        public bool LoadDataCallback(int handle)
        {
            if (ABC4TrustEvent != null)
            {
                DataWorker dw = new DataWorker(ABC4TrustEvent, handle);

                Thread detectThread = new Thread(new ThreadStart(dw.doit));
                detectThread.Start();
                return true;
            }
            else
            {
                System.Windows.Forms.MessageBox.Show("No EventHandler / Event for callback !!");
                return false;
            }
        }

        private class DataWorker
        {
            ABC4TrustEventHandler callback;
            int handle;
            Boolean storeData;
            string data;

            public DataWorker(ABC4TrustEventHandler callback, int handle, String data)
            {
                storeData = true;
                this.callback = callback;
                this.handle = handle;
                this.data = data;
            }
            public DataWorker(ABC4TrustEventHandler callback, int handle)
            {
                storeData = false;
                this.callback = callback;
                this.handle = handle;
                this.data = null;
            }

            public void doit()
            {
                if (storeData)
                {
                    ResultObject result = UserServiceCalls.StoreData(data);
                    result.handle = handle;
                    callback(result);
                }
                else
                {
                    ResultObject result = UserServiceCalls.LoadData();
                    result.handle = handle;
                    callback(result);
                }
            }

        }

    }


}
