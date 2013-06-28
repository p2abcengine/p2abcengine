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
using System.Windows.Navigation;
using System.Windows.Shapes;
using ABC4TrustSmartCard;
using System.Globalization;
using System.Numerics;
using PCSC;
using ABC4TrustSmartCardUI.Menu;



namespace ABC4TrustSmartCardUI
{
  /// <summary>
  /// Interaction logic for MainWindow.xaml
  /// </summary>
  public partial class MainWindow : Window
  {
    private List<CardInfo> cardInfoLst;
    private ABC4TrustSmartCard.ABC4TrustSmartCard smartCard;
    private SmartCardIO sIO;
    private SmartCardIO cardIO;
    private SCardMonitor monitor;
    private MenuResourceDictionary menuRes;


    static void setupLoggers()
    {
      // normal console writer.
      LoggerSpec outConsole = new LoggerSpec();
      outConsole.name = LoggerDefine.OUT_CONSOLE;
      outConsole.level = Logger.Level.Info;
      outConsole.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
      outConsole.logType = Logger.LogType.Console;
      Logger.Instance.AppendLoggerSpec(outConsole);

      // debug console writer.
      LoggerSpec debugConsole = new LoggerSpec();
      debugConsole.name = LoggerDefine.DEBUG_CONSOLE;
      debugConsole.level = Logger.Level.Info;
      debugConsole.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
      debugConsole.logType = Logger.LogType.Console;
      Logger.Instance.AppendLoggerSpec(debugConsole);
    }

    public MainWindow()
    {
      InitializeComponent();
      setupLoggers();
      menuRes = new MenuResourceDictionary();
      System.Windows.Controls.Menu m = menuRes.getMenu();
     
      cardIO = new SmartCardIO();
      monitor = cardIO.getMonitor();
      monitor.CardInserted += new CardInsertedEvent(CardEvent);
      monitor.CardRemoved += new CardRemovedEvent(CardEvent);
      monitor.Initialized += new CardInitializedEvent(CardEvent);
      monitor.StatusChanged += new StatusChangeEvent(StatusChanged);
      monitor.MonitorException += new MonitorExceptionEvent(MonitorException);
      monitor.Start(cardIO.getReaders());
    }

    private void CardEvent(object sender, CardStatusEventArgs args)
    {
      if (this.Dispatcher.CheckAccess())
      {
        UpdateDataGrid();
      }
      else
      {
        this.Dispatcher.Invoke((Action)(() =>
        {
          UpdateDataGrid();
        }));
      }
    }

    private void MonitorException(object sender, PCSCException ex)
    {
      Console.WriteLine("Monitor exited due an error:");
    }

    private void StatusChanged(object sender, StatusChangeEventArgs args)
    {
      if (this.Dispatcher.CheckAccess())
      {
        UpdateDataGrid();
      }
      else
      {
        this.Dispatcher.Invoke((Action)(() =>
        {
          UpdateDataGrid();
        }));
      }
    }

    private void UpdateDataGrid()
    {
      SmartCardIO cardIO = new SmartCardIO();
      List<String> connectedCards = cardIO.GetConnected();
      cardInfoLst = new List<CardInfo>();
      
      foreach (String s in connectedCards)
      {
        CardInfo info = new CardInfo();
        ABC4TrustSmartCard.ABC4TrustSmartCard smartCard = new ABC4TrustSmartCard.ABC4TrustSmartCard(s);
        info.ReaderName = s;
        string version;
        ErrorCode err = smartCard.GetVersion(out version);
        if (err.IsOK)
        {
          info.CardVersion = version;
        }
        CardMode cardMode;
        err = smartCard.GetMode(out cardMode);
        if (err.IsOK)
        {
          info.CardMode = (int)cardMode;
        }
        cardInfoLst.Add(info);
      }

      this.dataGrid1.ItemsSource = cardInfoLst;
      this.modeButton.Visibility = Visibility.Hidden;
      this.initCard.Visibility = Visibility.Hidden;
    }

    private void dataGrid1_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
      DataGrid dGrid = sender as DataGrid;
      if (dGrid.SelectedCells.Count == 0)
      {
        return;
      }
      CardInfo cInfo;
      try
      {
        cInfo = (CardInfo)dGrid.SelectedCells[0].Item;
      }
      catch (Exception)
      {
        return;
      }
      this.smartCard = new ABC4TrustSmartCard.ABC4TrustSmartCard(cInfo.ReaderName);
      this.sIO = this.smartCard.sIO;
      if ((CardMode)cInfo.CardMode == CardMode.VIRGIN)
      {
        this.modeButton.Click += modeButton_Root_Click;
        String content = "SetRootMode";
        this.modeButton.Content = content;
        this.modeButton.Visibility = Visibility.Visible;        
      }
      if ((CardMode)cInfo.CardMode == CardMode.ROOT)
      {
        this.initCard.Visibility = Visibility.Visible;
      }

    }
   
    private void modeButton_Root_Click(object sender, RoutedEventArgs e)
    {
      byte[] accMac = new byte[] { 0xDD, 0xE8, 0x90, 0x96, 0x3E, 0xF8, 0x09, 0x0E };
      ErrorCode err = this.smartCard.SetRootMode(accMac);
      if (!err.IsOK)
      {
        String msg = String.Format("Error while setting card in root mode: ErrorCode : {0:X2} {1:X2} \n see doc for info", err.SW1, err.SW2);
        System.Windows.MessageBox.Show(msg);
      }
      UpdateDataGrid();
    }

    private void virginMode_Click(object sender, RoutedEventArgs e)
    {
      if (this.smartCard == null)
      {
        String msg = String.Format("Please select a card to set in Virgin mode");
        System.Windows.MessageBox.Show(msg);
        return;
      }
      SetVirginMode virgin = new SetVirginMode(this.smartCard.sIO.GetReader().ReaderName);
      virgin.ShowDialog();
      UpdateDataGrid();
    }

    private void InitCard_Click(object sender, RoutedEventArgs e)
    {
      if (this.smartCard == null)
      {
        String msg = String.Format("Please select a card to init");
        System.Windows.MessageBox.Show(msg);
        return;
      }
      InitCardWindow initCard = new InitCardWindow(this.smartCard.sIO.GetReader().ReaderName);
      initCard.ShowDialog();
      UpdateDataGrid();
      /*
      KeyPair pq = new KeyPair(p, q);
      PinPuk pp;
      this.smartCard.InitDevice(pq, new byte[] { 0x01, 0x02, 0x03, 0x04 }, out pp);
      */
    }

   
  }
}
