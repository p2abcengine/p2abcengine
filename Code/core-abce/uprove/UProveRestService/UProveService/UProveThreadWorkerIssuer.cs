using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;
using System.Text;
using System.Threading.Tasks;
using ABC4TrustSmartCard;
using UProveWCFServiceLib;

namespace UProveService
{
  public class UProveThreadWorkerIssuer : IDisposable
  {
    private WebServiceHost _host;
    private ServiceEndpoint _serviceEndPoint;

    public UProveThreadWorkerIssuer()
    {
      WebHttpBinding binding = new WebHttpBinding();
      UProveRestServiceIssuer instance = UProveRestServiceIssuer.Instance;
      _host = new WebServiceHost(instance, new Uri(ParseConfigManager.GetAddress(), ParseConfigManager.GetIssuerApiPath()));
      _serviceEndPoint = _host.AddServiceEndpoint(typeof(IUProveRestServiceIssuer), binding, "");
      
      WebHttpBehavior enableHelp = new WebHttpBehavior();
      enableHelp.HelpEnabled = true;
      enableHelp.DefaultOutgoingResponseFormat = WebMessageFormat.Json;
      enableHelp.DefaultOutgoingRequestFormat = WebMessageFormat.Json;
      _serviceEndPoint.EndpointBehaviors.Add(enableHelp);

      if (ParseConfigManager.SetupTimeProfiles())
      {
        _serviceEndPoint.Behaviors.Add(new WcfTimeProfileEndPointLogger());
      }

    }


    public void Startup()
    {
      _host.Open();
    }


    #region IDisposable Members

    public void Dispose()
    { 
      _host.Close();
    }

    #endregion
  }
}
