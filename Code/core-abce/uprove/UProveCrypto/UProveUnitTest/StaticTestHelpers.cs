//*********************************************************
//
//    Copyright (c) Microsoft. All rights reserved.
//    This code is licensed under the Apache License
//    Version 2.0.
//
//    THIS CODE IS PROVIDED *AS IS* WITHOUT WARRANTY OF
//    ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING ANY
//    IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR
//    PURPOSE, MERCHANTABILITY, OR NON-INFRINGEMENT.
//
//*********************************************************

using System;
using UProveCrypto;

namespace UProveUnitTest
{
    static class StaticTestHelpers
    {
        private static System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

        public static void GenerateTestIssuanceParameters(string uidp, string spec, int numberOfAttributes, bool useRecommendedParameters, int numberOfTokens, out IssuerKeyAndParameters ikap, out IssuerProtocolParameters ipp, out ProverProtocolParameters ppp)
        {
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = (uidp == null ? null : encoding.GetBytes(uidp));
            isp.E = IssuerSetupParameters.GetDefaultEValues(numberOfAttributes);
            isp.UseRecommendedParameterSet = useRecommendedParameters;
            isp.S = (spec == null ? null : encoding.GetBytes(spec));
            ikap = isp.Generate();
            IssuerParameters ip = ikap.IssuerParameters;

            // Issuance
            byte[][] attributes = new byte[numberOfAttributes][];
            for (int i = 0; i < numberOfAttributes; i++)
            {
                attributes[i] = encoding.GetBytes("attribute value " + (i + 1));
            }
            byte[] tokenInformation = encoding.GetBytes("token information field");
            byte[] proverInformation = encoding.GetBytes("prover information field");

            ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numberOfTokens;
            ipp.TokenInformation = tokenInformation;

            ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numberOfTokens;
            ppp.TokenInformation = tokenInformation;
            ppp.ProverInformation = proverInformation;
        }

        public static byte[] IntToBigEndianBytes(int integer)
        {
            byte[] bytes = BitConverter.GetBytes(integer);
            Array.Reverse(bytes, 0, bytes.Length);
            return bytes;
        }
    }
}
