using System;
using System.Runtime.InteropServices;

namespace ABC4TrustBHO
{
    [ComVisible(true)]
    [Guid("FC4801A3-2BA9-11CF-A229-00AA003D7352")]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    public interface IObjectWithSite
    {
        [PreserveSig]
        int SetSite([In, MarshalAs(UnmanagedType.IUnknown)]object pUnkSite);
        [PreserveSig]
        int GetSite(ref Guid riid, out IntPtr ppvSite);
    }
}
