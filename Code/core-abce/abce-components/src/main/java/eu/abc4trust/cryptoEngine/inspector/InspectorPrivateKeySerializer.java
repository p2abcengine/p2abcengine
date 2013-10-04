//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.0 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// */**/****************************************************************

package eu.abc4trust.cryptoEngine.inspector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;

import com.ibm.zurich.idmx.key.VEPrivateKey;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.XMLSerializer;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.SecretKey;

/**
 * This class serializes Inspector Private Key - using raw bytes (method 2)
 * 
 * @author hgk
 */
public class InspectorPrivateKeySerializer {

    public byte[] serializeInspectorPrivateKey(SecretKey secretKey) {
        VEPrivateKey ibmVEPrivateKey =
                (VEPrivateKey) Parser.getInstance().parse(
                        (String) secretKey.getCryptoParams().getAny().get(0));

        try {
            // new optimized version - using raw bytes. size is now 1380 vs 1746
            ByteArrayOutputStream ser = new ByteArrayOutputStream();
            // method 2
            ser.write(2);

            ser.write(ibmVEPrivateKey.getPublicKeyLocation().toString().getBytes());
            ser.write(0);

            ser.write(this.serializeBigInteger(ibmVEPrivateKey.getOrderN()));
            ser.write(this.serializeBigInteger(ibmVEPrivateKey.getX1()));
            ser.write(this.serializeBigInteger(ibmVEPrivateKey.getX2()));
            ser.write(this.serializeBigInteger(ibmVEPrivateKey.getX3()));
            ser.close();

            return ser.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // try {
        // ByteArrayOutputStream ser = new ByteArrayOutputStream();
        // OLD method 1 - using objects...
        // ser.write(1);
        //
        // ObjectOutputStream objectOutput = new ObjectOutputStream(ser);
        //
        // objectOutput.writeObject(ibmVEPrivateKey.getPublicKeyLocation());
        // objectOutput.writeObject(ibmVEPrivateKey.getOrderN());
        // objectOutput.writeObject(ibmVEPrivateKey.getX1());
        // objectOutput.writeObject(ibmVEPrivateKey.getX2());
        // objectOutput.writeObject(ibmVEPrivateKey.getX3());
        // objectOutput.close();
        //
        // return ser.toByteArray();
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }
    }

    private byte[] serializeBigInteger(BigInteger i) {
        byte[] b = i.toByteArray();
        ByteBuffer buf = ByteBuffer.allocate(b.length + 2);
        buf.putShort((short) b.length);
        buf.put(b);
        return buf.array();
    }

    private BigInteger unSerializeBigInteger(ByteBuffer buf) {
        int length = buf.getShort();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        BigInteger i = new BigInteger(bytes);
        return i;
    }

    public SecretKey unserializeInspectorPrivateKey(byte[] data) {
        try {
            VEPrivateKey ibmVEPrivateKey;
            switch (data[0]) {
            case 2: {
                ByteBuffer buf = ByteBuffer.wrap(data);
                // We need to remove the first byte from the array.
                // It is the constant 2. If not, then the URI.create will not
                // work.
                byte headerEquals2 = buf.get();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte b;
                while ((b = buf.get()) != 0) {
                    baos.write(new byte[] {b});
                }

                byte[] byteArray = baos.toByteArray();
                String publicKeyLocationStr = new String(byteArray);
                URI thePublicKeyLocation = URI.create(publicKeyLocationStr);
                BigInteger _orderN = this.unSerializeBigInteger(buf);
                BigInteger _x1 = this.unSerializeBigInteger(buf);
                BigInteger _x2 = this.unSerializeBigInteger(buf);
                BigInteger _x3 = this.unSerializeBigInteger(buf);

                ibmVEPrivateKey = new VEPrivateKey(thePublicKeyLocation, _orderN, _x1, _x2, _x3);
            }
            break;
            case 1: {
                // deprecated
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                //int headerEquals1 = bais.read();

                ObjectInputStream objectInput = new ObjectInputStream(bais);

                URI thePublicKeyLocation = (URI) objectInput.readObject();
                BigInteger _orderN = (BigInteger) objectInput.readObject();
                BigInteger _x1 = (BigInteger) objectInput.readObject();
                BigInteger _x2 = (BigInteger) objectInput.readObject();
                BigInteger _x3 = (BigInteger) objectInput.readObject();

                ibmVEPrivateKey = new VEPrivateKey(thePublicKeyLocation, _orderN, _x1, _x2, _x3);
            }
            break;
            default:
                throw new RuntimeException("Cannot unserialize this private key: header was " + data[0]
                        + " expected header 1 or 2");
            }


            SecretKey insSecretKey = new SecretKey();
            String inspectorPrivateKeyAsString = XMLSerializer.getInstance().serialize(ibmVEPrivateKey);
            CryptoParams cryptoParams = new CryptoParams();
            insSecretKey.setCryptoParams(cryptoParams);
            cryptoParams.getAny().add(inspectorPrivateKeyAsString);

            return insSecretKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
