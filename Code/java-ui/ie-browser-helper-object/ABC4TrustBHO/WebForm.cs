using System;
using System.IO;
using System.Windows.Forms;

namespace ABC4TrustBHO
{
    public partial class WebForm : Form
    {
        public WebForm()
        {
            InitializeComponent();
            webBrowser1.Closing += new ClosingEventHandler(browserControl_Closing);
        }


        public string URL
        {
            set { webBrowser1.Url = new Uri(value); }
        }

        //private ExtendedWebBrowser browserControl;
        private void browserControl_Closing(object sender, EventArgs e)
        {
            BrowserHelperObject.log("WebForm", "browserControl_Closing", "my form / trying to close");
            this.Close();
        }
    }
}
