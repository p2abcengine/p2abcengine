package bekkopen.jetty;



public class InitSmartcard {

//  public static void initSmartcard(File resourcesDir, File smartcardFile) throws Exception {
//    
//    List<IssuerParameters> issuerParams = FileSystem.findAndLoadXmlResoucesInDir(resourcesDir, "issuer_param");
//    
//    if(issuerParams.size()==0) {
//      System.err.println("No Issuer Parametres found !");
//      return;
//    }
//
//     
//    eu.abc4trust.smartcard.RSAKeyPair rootKey = 
//        eu.abc4trust.smartcard.SmartcardInitializeTool.loadPrivateKey(resourcesDir.getAbsolutePath() + "/pki_keys_sk");
//
//    
//    eu.abc4trust.xml.SystemParameters systemParameters = FileSystem.loadXmlFromResource(resourcesDir.getAbsolutePath() + "/system_params.xml");
//
//    // NO PSEUDONYM!
//    URI scopeUri = URI.create("urn:eu:abce4trust:demo");
//    eu.abc4trust.smartcard.SmartcardInitializeTool abceTool = new eu.abc4trust.smartcard.SmartcardInitializeTool(rootKey, systemParameters, scopeUri );
//
//    // 
//    abceTool.setIssuerParameters(eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine.IDEMIX, issuerParams);
//
//    eu.abc4trust.smartcard.SoftwareSmartcard smartcard = new eu.abc4trust.smartcard.SoftwareSmartcard();
//    int pin_and_matNumber = 1234;
//    String smartcard_id = String.format("%09d", pin_and_matNumber);
//    URI deviceURI = URI.create("secret://software-smartcard-" + smartcard_id);
//    short deviceID = (short)pin_and_matNumber;
//
//    int minAttendence = 0;
//    abceTool.initializeSmartcard(smartcard, pin_and_matNumber, deviceID, deviceURI, minAttendence);
//
//    FileSystem.storeObjectInFile(smartcard, smartcardFile);
//    
//  }
//
}
