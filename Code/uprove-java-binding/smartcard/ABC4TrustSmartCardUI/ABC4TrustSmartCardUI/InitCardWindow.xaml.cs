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
using System.Numerics;
using ABC4TrustSmartCard;
using PCSC;

namespace ABC4TrustSmartCardUI
{
  /// <summary>
  /// Interaction logic for InitCardWindow.xaml
  /// </summary>
  public partial class InitCardWindow : Window
  {
    String pString = "98123248929977781234033599438430872512413464343146397351387389379354368678144441573871246352773205104826862682926853362525445766087875638152522866999171082979077110521492402490396873693935392516032428981301612806156847276776118279635414146466050412914757988580508268698346492883186329475125347579894931495911";
    String qString = "153675233447601346431048868855343067422415662106390990153196543740996536617679216226026465835429401901713210990800796200382364371445046479284855834231353845317728582606162246819027954438959188793527020710865829941771890543954962333617286117444708446256701507879424546361300317931079057381850093323907216844089";

    private string pin = "1234";

    private BigInteger p;
    private BigInteger q;
    private SmartCard smartCard;

    private void CleanAndClose()
    {
      smartCard.Dispose();
      this.Close();
    }

    public InitCardWindow(String readerName)
    {
      InitializeComponent();
      this.smartCard = new SmartCard(readerName, "1234");

      BigInteger.TryParse(this.pString, out p);
      BigInteger.TryParse(this.qString, out q);

      this.pValue.Text = this.p.ToString();
      this.qValue.Text = this.q.ToString();
      this.pinValue.Content = pin;
    }

    private void initCardButton_Click(object sender, RoutedEventArgs e)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.smartCard.device))
      {
        string puk;
        try
        {
          String pin = "1234";
          KeyPair pq = new KeyPair(p, q);
          puk = this.smartCard.InitDevice(pq, pin);
        }
        catch (ErrorCode ex)
        {
          String msg = String.Format("Could not INITIALIZE DEVICE. error code: {0} {1}", ex.SW1, ex.SW2);
          System.Windows.MessageBox.Show(msg);
        }
      }
      this.CleanAndClose();
    }
  }
}
