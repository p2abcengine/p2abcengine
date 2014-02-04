using System;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{

/*
    [Guid("4B3AE7D8-FB6A-4558-8A96-BF82B54F329C")]
    [ComVisible(true)]
    public interface IComObject
    {
        [DispId(0x10000001)]
        bool SayHelloCallback(int handle, string arg);

        [DispId(0x10000002)]
        string SayHelloBlocking(string arg);

        [DispId(0x10000003)]
        bool IssueCallback(int handle, string language, string start_url, string step_url, string status_url, string optional_cookie);

        [DispId(0x10000004)]
        bool PresentCallback(int handle, string language, string policy_url, string verify_url, string optional_cookie);

        [DispId(0x10000005)]
        bool StoreDataCallback(int handle, string data);

        [DispId(0x10000006)]
        bool LoadDataCallback(int handle);
    }

    [Guid("ECA5DD1D-096E-440c-BA6A-0118D351650B")]
    [ComVisible(true)]
    [InterfaceType(ComInterfaceType.InterfaceIsIDispatch)]
    public interface IComEvents
    {
        [DispId(0x00000001)]
        void ABC4TrustEvent(ResultObject args);
    }


    [Guid("b6f4c62a-118d-4ab5-887c-4c035a4df443")]
    [ComVisible(true)]
    [ClassInterface(ClassInterfaceType.None)]
    [ComSourceInterfaces(typeof(IComEvents))]
    [ProgId("ABC4TrustActiveX.CallbackHandler")]
    public class ABC4TrustCallback : IComObject
    {
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
        public string SayHelloBlocking(string arg)
        {
            return "Hello Blocking World : " + arg;
        }


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
                    //result.setHandle(handle);
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

*/
}
