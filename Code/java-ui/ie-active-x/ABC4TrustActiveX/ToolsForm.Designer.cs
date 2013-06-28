namespace ABC4TrustActiveX
{
    partial class ToolsForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ToolsForm));
            this.btnManageCredentials = new System.Windows.Forms.Button();
            this.btnRevocationCheck = new System.Windows.Forms.Button();
            this.btnChangePin = new System.Windows.Forms.Button();
            this.btnUnlockSmartcard = new System.Windows.Forms.Button();
            this.btnDebugInfo = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // btnManageCredentials
            // 
            resources.ApplyResources(this.btnManageCredentials, "btnManageCredentials");
            this.btnManageCredentials.Name = "btnManageCredentials";
            this.btnManageCredentials.Tag = "1";
            this.btnManageCredentials.UseVisualStyleBackColor = true;
            this.btnManageCredentials.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // btnRevocationCheck
            // 
            resources.ApplyResources(this.btnRevocationCheck, "btnRevocationCheck");
            this.btnRevocationCheck.Name = "btnRevocationCheck";
            this.btnRevocationCheck.Tag = "7";
            this.btnRevocationCheck.UseVisualStyleBackColor = true;
            this.btnRevocationCheck.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // btnChangePin
            // 
            resources.ApplyResources(this.btnChangePin, "btnChangePin");
            this.btnChangePin.Name = "btnChangePin";
            this.btnChangePin.Tag = "4";
            this.btnChangePin.UseVisualStyleBackColor = true;
            this.btnChangePin.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // btnUnlockSmartcard
            // 
            resources.ApplyResources(this.btnUnlockSmartcard, "btnUnlockSmartcard");
            this.btnUnlockSmartcard.Name = "btnUnlockSmartcard";
            this.btnUnlockSmartcard.Tag = "5";
            this.btnUnlockSmartcard.UseVisualStyleBackColor = true;
            this.btnUnlockSmartcard.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // btnDebugInfo
            // 
            resources.ApplyResources(this.btnDebugInfo, "btnDebugInfo");
            this.btnDebugInfo.Name = "btnDebugInfo";
            this.btnDebugInfo.Tag = "6";
            this.btnDebugInfo.UseVisualStyleBackColor = true;
            this.btnDebugInfo.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // btnCancel
            // 
            resources.ApplyResources(this.btnCancel, "btnCancel");
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // ToolsForm
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.Controls.Add(this.btnManageCredentials);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnDebugInfo);
            this.Controls.Add(this.btnUnlockSmartcard);
            this.Controls.Add(this.btnChangePin);
            this.Controls.Add(this.btnRevocationCheck);
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ToolsForm";
            this.TopMost = true;
            this.Load += new System.EventHandler(this.ToolsForm_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button btnManageCredentials;
        private System.Windows.Forms.Button btnRevocationCheck;
        private System.Windows.Forms.Button btnChangePin;
        private System.Windows.Forms.Button btnUnlockSmartcard;
        private System.Windows.Forms.Button btnDebugInfo;
        private System.Windows.Forms.Button btnCancel;
    }
}