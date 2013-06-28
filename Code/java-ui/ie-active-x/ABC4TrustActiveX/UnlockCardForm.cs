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
    public partial class UnlockCardForm : Form
    {
        public UnlockCardForm()
        {
            InitializeComponent();

            // try to place...
            CenterToParent();
        }

        private void btnOk_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
        }

        public string getPUK() 
        {
            return txtPUK.Text;
        }
        public string getNewPin()
        {
            return txtNewPin.Text;
        }

    }
}
