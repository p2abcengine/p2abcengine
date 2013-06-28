using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;

namespace ABC4TrustBHO
{
    [ComVisible(true)]
    [Guid("B4E8BC2D-9F15-4372-A72F-6B6776881A76")]
    [InterfaceType(ComInterfaceType.InterfaceIsDual)]
    public interface IExtension
    {

        [DispId(1)]
        ReturObject Present(string language, string policy_url, string verify_url);

        [DispId(2)]
        ReturObject Issue(string language, string start_url, string step_url, string status_url);

        [DispId(3)]
        ReturObject StoreData(string data);

        [DispId(4)]
        ReturObject LoadData();

        [DispId(5)]
        ReturObject ShowSite(string url);

        [DispId(6)]
        ReturObject Test(string s);
/*
        [DispId(1)]
        ReturObject Test(string s);

        [DispId(2)]
        ReturObject ShowSite(string url);
 */ 
    }
}
