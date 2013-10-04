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

package eu.abc4trust.cryptoEngine.bridging;

import java.math.BigInteger;
import java.net.URI;

import com.ibm.zurich.idmx.utils.GroupParameters;

import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;

public class StaticGroupParameters {
	
	//U-Prove Recommended Parameters (L2048N256)
  	//OID = 1.3.6.1.4.1.311.75.1.1.1
  	public static final BigInteger p = new BigInteger("ef0990061db67a9eaeba265f1b8fa12b553390a8175bcb3d0c2e5ee5dfb826e229ad37431148ce31f8b0e531777f19c1e381c623e600bff7c55a23a8e649ccbcf833f2dba99e6ad66e52378e92f7492b24ff8c1e6fb189fa8434f5402fe415249ae02bf92b3ed8eaaaa2202ec3417b2079da4f35e985bb42a421cfaba8160b66949983384e56365a4486c046229fc8c818f930b80a60d6c2c2e20c5df880534d4240d0d81e9a370eef676a1c3b0ed1d8ff30340a96b21b89f69c54ceb8f3df17e31bc20c5b601e994445a1d347a45d95f41ae07176c7380c60db2aceddeeda5c5980964362e3a8dd3f973d6d4b241bcf910c7f7a02ed3b60383a0102d8060c27", 16);
  	public static final BigInteger q = new BigInteger("c8f750941d91791904c7186d62368ec19e56b330b669d08708f882e4edb82885", 16);
  	public static final BigInteger Gd = new BigInteger("487813c6d3efc50b646745573142de47649cc77789aa545d2fca97e9e5e94639810fda34e77cff614b3a86715c7ae093a1070987b183c3c7efa892e3dca1f98fcfaa39e1d649aaae00f89473db7c8cf92037ad771fc464cb6b76f18325a1b02ea41d29276a1cf9b9bd7b25bb5f9a219ab022c7ab8d25378bcc7b9ffcdb70971c03d320fbff71797338ff24007bd785cfbdaaf4bb219b079b96382dff211e23f554092c3aa8af79e8a60d21355e7d026b3c8207fe4feeaca8a9a8dc5fc8333817c67bf805bbe0c032b10839a9026ba9c9bb120bd4ceebacd3152b66b256e41e4a06224ba3f2d3ab99a26c364fe822c0d2c5e972545c2572561c795fbb68a34018", 16);
  	public static final BigInteger h = new BigInteger("bca29a2d4b226f594591ecedbd1859ccb0ba3d20186b30e0ffbf05ba25788a6720005194c1f005b2ced980ca160254bb48a0e2d756ddcc919afe9017a47905154177fb2c37fb6cc0f4423e8f4a8b8376e0043dddf06255050523d4ee1f68748d0d415732686f01d88d98c75bd1e25fa48cd5bf4cc69b6d67bf0dd5c9cf18ee91ae17ebf128151286de3ab17ac4025a91168d42532144b7357e423f1b8d9dbcee68df89b44150e496ff6d416e4376e2daf9e422807d276572cec335d0587a5d798022415e3737326251d304fd7129183357ef9c8d194447705360b5bb270a2ce6194e5894c1fafad3ca78af080f500227564d43cb63462b1084e9ccd55d002e19", 16);
  	public static final URI sysParamsUri = URI.create(IdemixConstants.systemParameterId);
	
	public static GroupParameters getGroupParameters(){
		GroupParameters gp = new GroupParameters(p, q, Gd, h, sysParamsUri);
		return gp;
	}
}
