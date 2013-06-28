using System;
using System.IO;
using System.Threading;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{
    public delegate void ClosingEventHandler(object sender, EventArgs e);

    public class ExtendedWebBrowser : WebBrowser
    {

        // Define constants from winuser.h
        private const int WM_PARENTNOTIFY = 0x210;
        private const int WM_DESTROY = 2;

        //AxHost.ConnectionPointCookie cookie;
        //    WebBrowserExtendedEvents events;

        public event ClosingEventHandler Closing;

        public Boolean closing = false;

        System.Windows.Forms.HtmlDocument uiDocument;
   //     public class CloseDetector
   //     {
            private void Detector() //ExtendedWebBrowser ewb) 
            {
                while (true && !closing) 
                {
                    try 
                    {
                        //MessageBox.Show("get document!" + this + " : " + uiDocument);

                        //var document = wb.Document;
                        //MessageBox.Show("got document! " + document);
                        var idselectDone = uiDocument.GetElementById("idselectDone");

                        //MessageBox.Show("IN LOOP! " + " : " + idselectDone);
                        if (idselectDone != null) 
                        {
                            //closing = true;
                            //MessageBox.Show("Closing!");
                            closing = true;
                        } 
                        else
                        {
                            Thread.Sleep(200);
                        } 
                    } catch(Exception e)
                    {
                        MessageBox.Show("Detector Exception : " + e);
                        closing = true;
                    }
                    // 
                }
                Closing(this, EventArgs.Empty);
           }
     //   }

        public void forceClose() 
        {
            closing = true;
        }

        public void detectClose() 
        {
            this.DocumentCompleted += new WebBrowserDocumentCompletedEventHandler(wb_DocumentCompleted);
           
        }

        static void wb_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
            ExtendedWebBrowser wb = (ExtendedWebBrowser)sender;
            wb.uiDocument = wb.Document;

            Thread detectThread = new Thread(new ThreadStart(wb.Detector));
            detectThread.Start();
        }
  
        protected override void WndProc(ref Message m)
        {
            switch (m.Msg)
            {
                case WM_PARENTNOTIFY:
                    if (!DesignMode)
                    {
                        if (m.WParam.ToInt32() == WM_DESTROY)
                        {
                            BrowserHelperObject.log("ExtendedWebBrowser", "WndProc", "handle destroy - closing");
                            Closing(this, EventArgs.Empty);
/*
                            Message newMsg = new Message();
                            newMsg.Msg = WM_DESTROY;
                            // Tell whoever cares we are closing
                            Form parent = this.Parent as Form;
                            if (parent != null)
                                parent.Close();
 */ 
                        }
                    }
                    DefWndProc(ref m);
                    break;
                default:
                    base.WndProc(ref m);
                    break;
            }
        }
    }

}
