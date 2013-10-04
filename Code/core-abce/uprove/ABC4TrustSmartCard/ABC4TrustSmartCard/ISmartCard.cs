using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Numerics;

namespace ABC4TrustSmartCard
{
  interface ISmartCard
  {
    /// <summary>
    /// Initialize the device with the selected key N and set the pin code. It returns the puk for the card.
    /// </summary>
    /// <param name="keyPair">RSA keypair</param>
    /// <param name="pin">new pin code for the card</param>
    /// <returns>A string that holds the puk for the card</returns>
    String InitDevice(KeyPair keyPair, String pin);

    /// <summary>
    /// Will make a power cycle of the card to reset it.
    /// </summary>
    void ResetDevice();

    /// <summary>
    /// Reset the card. Will make a challenge and upload it to cryptoexperts webpage and pass the results to the card
    /// This method will remove all from the card except the ABC4Trust lite program.
    /// </summary>
    void SetVirginMode();

    /// <summary>
    /// Set the card into working mode.
    /// </summary>
    void SetCardInWorkingMode();

    /// <summary>
    /// Will set the Group component of unknown order on the card. For unknown orders it is
    /// only needed to set the modulus.
    /// </summary>
    /// <param name="modulus">The modulus</param>
    void SetGroupOfUnknownOrder(BigInteger modulus);

    /// <summary>
    /// Based on the groupID it makes a call to the smartcard to readout the group. It then checks if
    /// more than 1 generator is set to use the groupID. This is used to verify that the card have a
    /// group and generator set.
    /// </summary>
    /// <param name="groupID">The group ID</param>
    /// <returns>True if there is a least one generator set otherwise False</returns>
    bool IsGeneratorSet(int groupID);

    /// <summary>
    /// Returns the public key for the credential defined with the ID "credID".
    /// </summary>
    /// <param name="credID">Credential to lookup</param>
    /// <returns>The public key as a byte array</returns>
    byte[] GetCredentialPublicKey(int credID, bool raw = false);

    /// <summary>
    /// Return the Commitment used for zero-knowledge proofs.
    /// </summary>
    /// <param name="proverID">ID for the prover</param>
    /// <param name="credID">ID for the credential</param>
    /// <returns>Returns the presentation commitment as a byte array</returns>
    byte[] GetPresCommitment(int proverID, int credID, bool raw = false);

    /// <summary>
    /// Return the Response used for zero-knowledge proofs.
    /// </summary>
    /// <returns>The device reponse as a byte array</returns>
    byte[] GetDeviceResponse(bool raw = false);


    /// <summary>
    /// Return the Response used for zero-knowledge proofs.
    /// </summary>
    /// <param name="proverID">ID for the prover</param>
    /// <returns>The device reponse as a BigInteger</returns>
    byte[] GetPresResponse(int credID);


    /// <summary>
    /// Return the device commitment for zero-knowledge proofs.
    /// BeginCommitments must be called before this.
    /// </summary>
    /// <returns>Returns the commitment as a byte array</returns>
    byte[] GetDeviceCommitment(bool raw = true);

    /// <summary>
    /// Begin the Commitment proofsession
    /// </summary>
    /// <param name="proverID">ID of the prover.</param>
    byte[] BeginCommitment(int proverID);

    /// <summary>
    /// End the proofsession for commitment.
    /// </summary>
    void EndCommitment();

    /// <summary>
    /// Returns the scope ecxlusive commitment (aPrime)
    /// </summary>
    /// <param name="scopeURI">The scope as a string</param>
    /// <returns>aPrime</returns>
    byte[] GetScopeExclusiveCommitment(byte[] scopeURI, bool raw = false);


    /// <summary>
    /// Returns the scope exclusive pseudonym (Ps)
    /// </summary>
    /// <param name="scopeURI">The scope as a string</param>
    /// <returns>Ps</returns>
    byte[] GetScopeExclusivePseudonym(byte[] scopeURI, bool raw = false);


    byte[] ReadGroupComponent(int groupID, int compType, bool raw = false);


    void GetIssuanceCommitment(byte credID);
    void GetIssuanceResponse(byte credID);

    int GetCreadentialStatus(byte credID);

    void BeginResponse(int proverID, byte[] input);

  }
}
