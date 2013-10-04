using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Numerics;

namespace ABC4TrustSmartCard
{
  /// <summary>
  /// This is a place holder for a readl crypto KeyPair repo.
  /// Remove when not needed.
  /// </summary>
  public class KeyPair
  {
    public KeyPair(BigInteger p, BigInteger q)
    {
      this.p = p;
      this.q = q;
      this.N = p * q;
    }
    public BigInteger q { get; set; }
    public BigInteger p { get; set; }
    public BigInteger N { get; set; }
    public BigInteger Phi
    {
      get
      {
        BigInteger phi = (p - 1) * (q - 1);
        return phi;
      }
    }
    
  }
}
