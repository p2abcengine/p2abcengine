using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace ABC4TrustActiveX
{
    public partial class ToolsForm : Form
    {
        public object sender;
        public EventArgs e;
        public object tag;

        public ToolsForm()
        {
            InitializeComponent();

            // try to place...
            CenterToParent();
        }

        private void btnSelect_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
            this.e = e;
            this.sender = sender;
            Button b = (Button)sender;
            this.tag = b.Tag;
        }


        private void ToolsForm_Load(object sender, EventArgs e)
        {

        }

        private void btnCancel_Click(object sender, EventArgs e)
        {

        }

    }
}
