REM - rename ie-active-x-.....dll to ABC4TrustActiveX.dll
"C:\Windows\Microsoft.NET\Framework\v4.0.30319\RegAsm.exe" /unregister ABC4TrustActiveX.dll
"C:\Windows\Microsoft.NET\Framework\v4.0.30319\RegAsm.exe" /codebase /tlb ABC4TrustActiveX.dll
"C:\Windows\Microsoft.NET\Framework64\v4.0.30319\RegAsm.exe" /unregister ABC4TrustActiveX.dll
"C:\Windows\Microsoft.NET\Framework64\v4.0.30319\RegAsm.exe" /codebase /tlb ABC4TrustActiveX.dll
