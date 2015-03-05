package bekkopen.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.ProtectionDomain;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

//import eu.abc4trust.ri.servicehelper.FileSystem;



public class Main {

  // used for shutdown
    private final int port = 9300;
//    private final String contextPath;
////    private final String workPath;
    private final String secret = "eb27fb2e61ed603363461b3b4e37e0a0";

    public static void main(String[] args) throws Exception {
        Main sc = new Main();

        if (args.length != 1)               sc.start();
//        else if ("status".equals(args[0]))  sc.status();
//        else if ("offline".equals(args[0])) sc.offline();
//        else if ("online".equals(args[0]))  sc.online();
        else if ("stop".equals(args[0]))    sc.stop();
        else if ("start".equals(args[0]))   sc.start();
        else                                sc.usage();
    }

    public Main() {
//        try {
//            String configFile = System.getProperty("config", "jetty.properties");
//            System.getProperties().load(new FileInputStream(configFile));
//        } catch (Exception ignored) {}

//        port = 9300; // Integer.parseInt(System.getProperty("jetty.port", "9300"));
//        contextPath = System.getProperty("jetty.contextPath", "/");
////        workPath = System.getProperty("jetty.workDir", null);
//        secret = System.getProperty("jetty.secret", "eb27fb2e61ed603363461b3b4e37e0a0");
    }

    private void start() {
        System.out.println("Start ABC4Trust user service");
        // Start a Jetty server with some sensible(?) defaults
        try {
            Server srv = new Server();
            srv.setStopAtShutdown(true);

            // Allow 5 seconds to complete.
            // Adjust this to fit with your own webapp needs.
            // Remove this if you wish to shut down immediately (i.e. kill <pid> or Ctrl+C).
            srv.setGracefulShutdown(5000);

            // Increase thread pool
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMaxThreads(100);
            srv.setThreadPool(threadPool);

            // Ensure using the non-blocking connector (NIO)
            Connector connectorABCE = new SelectChannelConnector();
            connectorABCE.setPort(9300);
            connectorABCE.setMaxIdleTime(600000);
            Connector connectorUI = new SelectChannelConnector();
            connectorUI.setPort(9093);
            connectorUI.setMaxIdleTime(600000);
            srv.setConnectors(new Connector[]{connectorABCE, connectorUI});

            // Get the war-file
            ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
            String warFile = protectionDomain.getCodeSource().getLocation().toExternalForm();
            File currentDir;
            if(warFile.contains("%20")) {
                // windows 'program files'
                warFile = warFile.replaceAll("%20", " ");
                currentDir = new File(protectionDomain.getCodeSource().getLocation().getPath().replaceAll("%20", " ")).getParentFile();
            } else {
                currentDir = new File(protectionDomain.getCodeSource().getLocation().getPath()).getParentFile();
            }

            warFile = currentDir + "/webapp";
            
            if(! new File(warFile).exists()) {
              System.err.println("WAR FILE Folder does not exist ??? : " + warFile );
              System.exit(1);
            }
            System.out.println("currentDir : " + currentDir + " - web folder in /webapp");


            String user_home = System.getProperty("user.home");
            String LOCALAPPDATA = user_home + File.separator + "AppData" + File.separator + "Local";
            String abc4Trust_LOCALAPPDATA = LOCALAPPDATA + File.separator + "ABC4Trust";
            File abc4Trust_LOCALAPPDATA_FILE = new File(abc4Trust_LOCALAPPDATA);
            if(! abc4Trust_LOCALAPPDATA_FILE.exists()) {
              System.out.println("Created ABC4Trust folder in AppData \\ Local : " + abc4Trust_LOCALAPPDATA);
              abc4Trust_LOCALAPPDATA_FILE.mkdirs();
            }
            System.setProperty("ABC4TRUST_LOCALAPPDATA", abc4Trust_LOCALAPPDATA);

            
            // Test software smartcard!
            // testSoftwareSmartcard(currentDir, user_home, LOCALAPPDATA);
            
            // user storage folder...
            File userStorageFolder = new File(LOCALAPPDATA, "user_storage");
            if(!userStorageFolder.exists()) {
                userStorageFolder.mkdirs();
            }
            
            // select pilot issuer resoruces...
            System.setProperty("testcase", "generic-installer");
            System.setProperty("UserServiceRunDir", currentDir.getAbsolutePath());

            // Add the warFile (this jar)
            WebAppContext context = new WebAppContext(warFile, "/");
            context.setServer(srv);
//            resetTempDirectory(context, currentDir.getAbsolutePath());

            // Add the handlers
            HandlerList handlers = new HandlerList();
            handlers.addHandler(new ShutdownHandler(srv, context, secret));
            handlers.addHandler(context);
            // handlers.addHandler(new BigIPNodeHandler(secret));
            srv.setHandler(handlers);

            System.out.println(" - call start");

            srv.start();
            srv.join();
            System.out.println(" - start DONE");
        } catch (Exception e) {
            System.err.println(" - start Failed " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

//    UProve is includede in exploded 'bundle.zip'
    
//    private static final String[] uproveFiles = { ///"ABC4Trust-UProve.exe", "ABC4Trust-UProve.exe.config", "UProveCrypto.dll", "bc-trimmed.dll" 
//                                                  "ABC4Trust-UProve.exe", "ABC4TrustSmartCard.dll", "bc-trimmed.dll", //"pcsc-sharp.dll.config",
//                                                  "ABC4Trust-UProve.exe.config", "UProveCrypto.dll", "pcsc-sharp.dll", "pcsc-sharp.dll.mdb"
//    };
//    private void setupUProveBinaries(File currentDir) {
//        System.out.println("setupUProveBinaries " );
//        File uproveDir = new File(currentDir, "uprove");
//        if(! uproveDir.exists()) {
//            uproveDir.mkdirs();
//        }
//        System.out.println("UPROVE FOLDER : " + uproveDir.getAbsolutePath());
//        for(String uproveFileName : uproveFiles) {
//          extractResource(uproveDir, "/uprove/",  uproveFileName);
//        }
//        System.setProperty("PathToUProveExe", uproveDir.getAbsolutePath());
//    }

    private void copyResourceFile(File extractDir, File inputFile) throws FileNotFoundException {
      System.out.println("copyResourceFile - " + inputFile.getAbsolutePath() + " - to folder " + extractDir);
      InputStream is = new FileInputStream(inputFile);
      extractResource(extractDir, is, inputFile.getName());
    }
    private void extractJarResource(File extractDir, String resourceFolder, String resourceName) {
      System.out.println("extractJarResource - " + resourceFolder + "/" + resourceName + " - to folder " + extractDir);
      InputStream is = Main.class.getResourceAsStream(resourceFolder + resourceName);
      extractResource(extractDir, is, resourceName);
    }    
    private void extractResource(File extractDir, InputStream is, String resourceName) {
//      System.out.println("extractResource - " + resourceFolder + "/" + resourceName + " - to folder " + extractDir);
      FileOutputStream fos = null;
        try {
            byte[] b = new byte[1];
            fos = new FileOutputStream(new File(extractDir, resourceName));
            while(is.read(b)!=-1) {
                fos.write(b);
            }
        } catch(Exception e) {
            System.err.println("Faild to extract : " + resourceName);
            e.printStackTrace();
        } finally {
            if(fos!=null) {
                try {
                  fos.close();
                } catch (IOException e) {
                }
            }
            if(is !=null) {
                try {
                  is.close();
                } catch (IOException e) {
                }
            }
        }
    }

//    private void testSoftwareSmartcard(File currentDir, String user_home, String user_app_data) {
//      // softwaresmartcard.properties
//      String smartcardResource = currentDir.getAbsolutePath() + "/resources/softwaresmartcard.properties";
//      InputStream is = null;
//      try {
//        is = FileSystem.getInputStream(smartcardResource);
//      } catch(Exception e) {
//        throw new IllegalStateException("smartcardResource not found : " + smartcardResource);
//      }
//      Properties softwaresmartcard = new Properties();
//      try {
//        softwaresmartcard.load(is);
//        String path = softwaresmartcard.getProperty("path", null);
//        if(false && path!=null) {
//            
//            if(path.startsWith("USER_HOME")) {
//                path = user_home + path.substring(9);
//            } else if(path.startsWith("LOCALAPPDATA")) {
//                path = user_app_data + path.substring(12);
//            }
//            System.out.println("Setting Softwaresmartcard to be read from path : " + path);
//            System.setProperty("UserSoftwareSmartcard", path);
//            // test if we must generate!
//            
//            String generate = softwaresmartcard.getProperty("generate", null);
//            if(generate ==  null && ! Boolean.valueOf(generate)) {
//              System.out.println("Do NOT try generate!");
//              return;
//            }            
//
//            File smartcardFile = new File(path);
//            if(smartcardFile.exists()) {
//              System.out.println("Smartcard File already exist!");
//              return;
//            }
//            System.out.println("Try to generate - smartcard!");
//
//            File resourcesDir = new File(currentDir, "resources");
//            
//            InitSmartcard.initSmartcard(resourcesDir, smartcardFile);
//            
//        }
//      } catch(Exception e) {
//        System.err.println("Failed to init Software Smartcard.");
//        e.printStackTrace();
//          throw new IllegalStateException("Failed to init Software Smartcard.", e);
//      } finally {
//        if(is !=null) {
//            try {
//              is.close();
//            } catch (IOException e) {
//            }
//        }
//      }
//    }

    private void setupTrustedSSLCA(File currentDir) {
      System.out.println("setupTrustedSSLCA" );
      
      try {
          InputStream is = Main.class.getResourceAsStream("/cacerts");
          if(is == null) {
              // no cacerts - skip
              System.out.println("No cacerts keystore - skip");
              return;
          }
          System.out.println("- load cacerts form a new java");
          KeyStore cacerts = KeyStore.getInstance("JKS");
          cacerts.load(is, "changeit".toCharArray());

          CertificateFactory cf = CertificateFactory.getInstance("X509");
          X509Certificate idm_ca = (X509Certificate) cf.generateCertificate(Main.class.getResourceAsStream("/idm_ca.pem"));

          System.out.println("- add NSN IDM CA : " + idm_ca.getSubjectX500Principal() + " : " + idm_ca.getSerialNumber());
          cacerts.setCertificateEntry("nsn_idm_ca", idm_ca);
          
          TrustManagerFactory tmf  = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
          tmf.init(cacerts);

          X509TrustManager trustManager = null;
          TrustManager tms[] = tmf.getTrustManagers();
          for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
              trustManager = (X509TrustManager)tms[i];
              break;
            }
          }
          
          System.out.println("- created new trust manager " + trustManager.getAcceptedIssuers().length);
          
          SSLContext sslContext = SSLContext.getInstance("TLS");
          sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());

          System.out.println("- set DefaultSSLSocketFactor to use our own TrustManager..");
          HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
          
      } catch(Exception e) {
          System.err.println("Failed to add NSN IDM CA Certificate : " + e);
          e.printStackTrace();
      }

  }

    
    private void stop() {
        System.out.println("Stop ABC4Trust user service");
        System.out.println(" - result : " + ShutdownHandler.shutdown(port, secret));
    }

    private void usage() {
        System.out.println("Usage: java -jar <file.jar> [start|stop|status|enable|disable]\n\t" +
                "start    Start the server (default)\n\t" +
                "stop     Stop the server gracefully\n\t" +
                  ""
        );
        System.exit(-1);
    }

}
