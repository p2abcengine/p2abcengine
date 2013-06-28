using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;

namespace ABC4TrustSmartCardUI.Menu
{
  partial class MenuResourceDictionary : ResourceDictionary
  {
    public MenuResourceDictionary()
    {
      InitializeComponent();
    }

    public System.Windows.Controls.Menu getMenu()
    {
      return this.mainMenu1;
    }


    private void click_select_card(object sender, RoutedEventArgs e)
    {

      MessageBox.Show("You clicked 'New...'");
    }

  }
}
