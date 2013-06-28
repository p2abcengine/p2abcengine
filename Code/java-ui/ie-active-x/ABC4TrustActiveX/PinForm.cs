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
    public partial class PinForm : Form
    {
        public PinForm()
        {
            InitializeComponent();

            // try to place...
            CenterToParent();
        }

        private void btnOk_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }

        public string getPin() 
        {
            return txtPin.Text;
        }

    }
}
