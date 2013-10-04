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
using UProveCrypto.Math;

namespace UProveSample
{
    /// <summary>
    /// Illustrates how to use the U-Prove SDK.
    /// </summary>
    public class SDKSample
    {
        static System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

        static byte[] HexToBytes(string hexString)
        {
            int length = hexString.Length;
            if ((length % 2) != 0)
            {
                // prepend 0
                hexString = "0" + hexString;
            }

            byte[] bytes = new byte[hexString.Length / 2];
            for (int i = 0; i < length; i += 2)
            {
                try
                {
                    bytes[i / 2] = Byte.Parse(hexString.Substring(i, 2), System.Globalization.NumberStyles.HexNumber);
                }
                catch (Exception)
                {
                    throw new ArgumentException("hexString is invalid");
                }
            }
            return bytes;
        }

        private static IssuerKeyAndParameters SetupUProveIssuer(string UIDP, int numberOfAttributes, GroupType groupType = GroupType.Subgroup, bool supportDevice = false)
        {
            WriteLine("Setting up Issuer parameters");
            IssuerSetupParameters isp = new IssuerSetupParameters();
            // pick a unique identifier for the issuer params
            isp.UidP = encoding.GetBytes(UIDP);
            // set the number of attributes in the U-Prove tokens
            isp.NumberOfAttributes = numberOfAttributes;
            // an application profile would define the format of the specification field,
            // we use a dummy value in this sample
            isp.S = encoding.GetBytes("application-specific specification");
            // specify the group type: subgroup (default) or ECC
            isp.GroupConstruction = groupType;

            return isp.Generate(supportDevice);
        }

        private static UProveKeyAndToken[] IssueUProveTokens(IssuerKeyAndParameters ikap, IssuerParameters ip, byte[][] attributes, int numOfTokens, byte[] ti = null, byte[] pi = null)
        {
            WriteLine("Issuing " + numOfTokens + " tokens");
            // setup the issuer and generate the first issuance message
            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numOfTokens;
            ipp.TokenInformation = ti;
            Issuer issuer = ipp.CreateIssuer();
            string firstMessage = ip.Serialize<FirstIssuanceMessage>(issuer.GenerateFirstMessage());

            // setup the prover and generate the second issuance message
            ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numOfTokens;
            ppp.TokenInformation = ti;
            ppp.ProverInformation = pi;
            Prover prover = ppp.CreateProver();
            string secondMessage = ip.Serialize<SecondIssuanceMessage>(prover.GenerateSecondMessage(ip.Deserialize<FirstIssuanceMessage>(firstMessage)));

            // generate the third issuance message
            string thirdMessage = ip.Serialize<ThirdIssuanceMessage>(issuer.GenerateThirdMessage(ip.Deserialize<SecondIssuanceMessage>(secondMessage)));

            // generate the tokens
            return prover.GenerateTokens(ip.Deserialize<ThirdIssuanceMessage>(thirdMessage));
        }

        private static CommitmentPrivateValues PresentUProveToken(IssuerParameters ip, UProveKeyAndToken upkt, byte[][] attributes, int[] disclosed, int[] committed, byte[] message, byte[] scope, IDevice device, byte[] deviceMessage)
        {
            WriteLine("Presenting one token");
            // the returned commitment randomizer (to be used by an external proof module)
            CommitmentPrivateValues cpv;

            // generate the presentation proof
            string token = ip.Serialize<UProveToken>(upkt.Token);
            ProverPresentationProtocolParameters pppp = new ProverPresentationProtocolParameters(ip, disclosed, message, upkt, attributes);
            pppp.Committed = committed;
            // if a scope is defined, we use the first attribute to derive a scope exclusive pseudonym            
            pppp.PseudonymAttributeIndex = (scope == null ? 0 : 1);
            pppp.PseudonymScope = scope;
            if (device != null)
            {
                pppp.SetDeviceData(deviceMessage, device.GetPresentationContext());
            }
            pppp.KeyAndToken = upkt;
            pppp.Attributes = attributes;
            string proof = ip.Serialize<PresentationProof>(PresentationProof.Generate(pppp, out cpv));

            // verify the presentation proof
            VerifierPresentationProtocolParameters vppp = new VerifierPresentationProtocolParameters(ip, disclosed, message, ip.Deserialize<UProveToken>(token));
            vppp.Committed = committed;
            // if a scope is defined, we use the first attribute to derive a scope exclusive pseudonym            
            vppp.PseudonymAttributeIndex = (scope == null ? 0 : 1);
            vppp.PseudonymScope = scope;
            vppp.DeviceMessage = deviceMessage;
            ip.Deserialize<PresentationProof>(proof).Verify(vppp);

            return cpv;
        }

        /// <summary>
        /// This sample illustrates how to issue and present software-only U-Prove tokens.
        /// </summary>
        public static void SoftwareOnlySample()
        {
            WriteLine("U-Prove SDK Sample");
            
            /*
             *  issuer setup
             */
            IssuerKeyAndParameters ikap = SetupUProveIssuer("sample software-only issuer", 3);
            string privateKeyBase64 = ikap.PrivateKey.ToBase64String(); // this needs to be stored securely
            string ipJSON = ikap.IssuerParameters.Serialize();

            // the IssuerParameters instance needs to be distributed to the Prover and Verifier.
            // Each needs to verify the parameters before using them
            IssuerParameters ip = new IssuerParameters(ipJSON);
            ip.Verify();

            /*
             *  token issuance
             */
            // specify the attribute values agreed to by the Issuer and Prover
            byte[][] attributes = new byte[][] {
                    encoding.GetBytes("first attribute value"),
                    encoding.GetBytes("second attribute value"),
                    encoding.GetBytes("third attribute value")
            };
            // specify the special field values
            byte[] tokenInformation = encoding.GetBytes("token information value");
            byte[] proverInformation = encoding.GetBytes("prover information value");
            // specify the number of tokens to issue
            int numberOfTokens = 5;

            UProveKeyAndToken[] upkt = IssueUProveTokens(new IssuerKeyAndParameters(privateKeyBase64, ipJSON), ip, attributes, numberOfTokens, tokenInformation, proverInformation);           

            /*
             *  token presentation
             */
            // the indices of disclosed attributes
            int[] disclosed = new int[] { 2 };
            // the indices of the committed attributes (used by protocol extensions)
            int[] committed = null;
            // the application-specific message that the prover will sign. Typically this is a nonce combined
            // with any application-specific transaction data to be signed.
            byte[] message = encoding.GetBytes("message");
            // the application-specific verifier scope from which a scope-exclusive pseudonym will be created
            // (if null, then a pseudonym will not be presented)
            byte[] scope = encoding.GetBytes("verifier scope");

            PresentUProveToken(ip, upkt[0], attributes, disclosed, committed, message, scope, null, null);

            WriteLine("Sample completed.\n*************************************************************\n");
        }

        /// <summary>
        /// This sample illustrates how to issue and present device-protected U-Prove tokens.
        /// </summary>
        public static void DeviceSample()
        {

            WriteLine("U-Prove SDK Device Sample");

            /*
             *  issuer setup
             */

            IssuerKeyAndParameters ikap = SetupUProveIssuer("sample device-protected issuer", 3, GroupType.ECC, true);
            string ipJSON = ikap.IssuerParameters.Serialize();

            // the IssuerParameters instance needs to be distributed to the Prover, Device, and Verifier.
            // Each needs to verify the parameters before using them
            IssuerParameters ip = new IssuerParameters(ipJSON);
            ip.Verify();


            /*
             * device provisioning
             */

            // generate a new device
            IDevice device = new VirtualDevice(ip);
            // get the device public key
            GroupElement hd = device.GetDevicePublicKey();


            /*
             *  token issuance
             */

            // specify the attribute values
            byte[][] attributes = new byte[][] {
                    encoding.GetBytes("first attribute value"),
                    encoding.GetBytes("second attribute value"),
                    encoding.GetBytes("third attribute value"),
            };
            // specify the special field values
            byte[] tokenInformation = encoding.GetBytes("token information value");
            byte[] proverInformation = encoding.GetBytes("prover information value");
            // specify the number of tokens to issue
            int numberOfTokens = 5;

            UProveKeyAndToken[] upkt = IssueUProveTokens(ikap, ip, attributes, numberOfTokens, tokenInformation, proverInformation);


            /*
             *  token presentation
             */

            // the indices of disclosed attributes
            int[] disclosed = new int[] { 2 };
            // the application-specific messages that the prover and device will sign, respectively. Typically this 
            // is a nonce combined with any application-specific transaction data to be signed.
            byte[] message = encoding.GetBytes("message");
            byte[] deviceMessage = encoding.GetBytes("message for device");

            PresentUProveToken(ip, upkt[0], attributes, disclosed, null, message, null, device, deviceMessage);

            WriteLine("Sample completed.\n*************************************************************\n");
        }


        public static void Main()
        {
            SoftwareOnlySample();
            DeviceSample();
            WriteLine("Press enter to exit...");
            Console.ReadLine();
        }


        public delegate void OutputLine(string text);

        public static OutputLine outputFunction;

        public static void WriteLine(string text)
        {
            // if a consumer of this class has supplied a function to get the outup 
            // send it 
            if (outputFunction != null) outputFunction(text);

            Console.WriteLine(text);
        }

    }
}
