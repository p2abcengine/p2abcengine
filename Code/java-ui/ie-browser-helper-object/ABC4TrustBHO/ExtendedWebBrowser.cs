using System;
using System.IO;
using System.Windows.Forms;

namespace ABC4TrustBHO
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
