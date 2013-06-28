using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{
    public partial class WebForm : Form
    {
        public WebForm()
        {
            InitializeComponent();
            webBrowser1.Closing += new ClosingEventHandler(browserControl_Closing);

            // start close detector
            webBrowser1.detectClose();
            webBrowser1.HandleDestroyed += browserControl_HandleDestroy;

            // try to place...
            CenterToParent();
        }


        public string URL
        {
            set { webBrowser1.Url = new Uri(value); }
        }

        //private ExtendedWebBrowser browserControl;
        private void browserControl_Closing(object sender, EventArgs e)
        {
            BrowserHelperObject.log("WebForm", "browserControl_Closing", "my form / trying to close");
            // webBrowser1.closing = true;
            this.Close();
        }
        private void browserControl_HandleDestroy(object sender, EventArgs e)
        {
            BrowserHelperObject.log("WebForm", "browserControl_HandleDestroy", "my form / trying to close");
            // webBrowser1.closing = true;
            this.Close();
        }

    }
}
