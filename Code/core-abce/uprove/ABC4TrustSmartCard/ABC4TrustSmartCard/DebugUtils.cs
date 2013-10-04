using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;

namespace ABC4TrustSmartCard
{
  // All methods is guarded by the debug marco so hopefully the compiler will optimize this away in Release builds.
  public static class DebugUtils
  {
    [MethodImpl(MethodImplOptions.NoInlining)]
    public static void DebugPrintBegin(byte[] inputArgs)
    {
#if DEBUG
      StackTrace st = new StackTrace(new StackFrame(1));
      string methodName = st.GetFrame(0).GetMethod().Name;
      string s = null;
      if (inputArgs != null)
      {
        s = Utils.ByteArrayToString(inputArgs);
      }
      Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);
      dOut.write("Calling " + methodName + " with args " + s);
#endif
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    public static void DebugPrintEnd(byte[] outputArgs)
    {
#if DEBUG
      StackTrace st = new StackTrace(new StackFrame(1));
      string methodName = st.GetFrame(0).GetMethod().Name;
      string s = null;
      if (outputArgs != null)
      {
        s = Utils.ByteArrayToString(outputArgs); ;
      }
      Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);
      dOut.write("Exit " + methodName + " with args " + s);
#endif
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    public static void DebugPrintData(byte[] data)
    {
#if DEBUG
      StackTrace st = new StackTrace(new StackFrame(1));
      string methodName = st.GetFrame(0).GetMethod().Name;
      string s = null;
      if (data != null)
      {
        s = Utils.ByteArrayToString(data); ;
      }
      Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);
      dOut.write("Data in " + methodName + " " + s);
#endif
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    public static void DebugPrintErrorCodes(ErrorCode err)
    {
#if DEBUG
      StackTrace st = new StackTrace(new StackFrame(1));
      string methodName = st.GetFrame(0).GetMethod().Name;
      Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);
      dOut.write("Error in : " + methodName + "with error code " + Utils.PrintAsHexValue(err.SW1) + " : " + Utils.PrintAsHexValue(err.SW2));
#endif
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    public static void DebugPrint(string strackTrace)
    {
#if DEBUG
      StackTrace st = new StackTrace(new StackFrame(1));
      string methodName = st.GetFrame(0).GetMethod().Name;
      Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);
      dOut.write("Thrown from : " + methodName);
      dOut.write(strackTrace);
#endif
    }  
  }



}
