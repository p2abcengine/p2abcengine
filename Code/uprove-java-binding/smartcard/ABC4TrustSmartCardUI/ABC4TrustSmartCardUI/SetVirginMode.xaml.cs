using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using System.Net;
using System.IO;
using ABC4TrustSmartCard;
using PCSC;
using PCSC.Iso7816;


namespace ABC4TrustSmartCardUI
{
  /// <summary>
  /// Interaction logic for SetVirginMode.xaml
  /// </summary>
  public partial class SetVirginMode : Window
  {
    private SmartCard device;

    public SetVirginMode()
    {
      InitializeComponent();
    }

    ~SetVirginMode()
    {
      this.device.Dispose();
    }

    public SetVirginMode(String readerName)
    {
      InitializeComponent();
      this.device = new SmartCard(readerName, "1234");     
    }

    private void virginMode_Click(object sender, RoutedEventArgs e)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device.device))
      {
        try
        {
          device.SetVirginMode();
        }
        catch (ErrorCode ex)
        {
          String msg = String.Format("Could not set card in virgin mode: {0}{X2} {1}{X2} in commando {3}", ex.SW1, ex.SW2, ex.Command);
          System.Windows.MessageBox.Show(msg);
        }
        catch (Exception ex)
        {
          String msg = String.Format("Could not set card in virgin mode: {0}", ex.Message);
          System.Windows.MessageBox.Show(msg);
        }

        this.Close();
      }
    }
  }
}
