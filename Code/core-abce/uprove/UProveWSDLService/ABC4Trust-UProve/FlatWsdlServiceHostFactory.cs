using System;
using System.Collections.Generic;
using System.Text;
using System.ServiceModel.Activation;
using System.ServiceModel;

namespace Thinktecture.ServiceModel.Extensions.Description
{
	
	// Add the System.ServiceModel.Activation.dll assembly to the project when building on Windows / .NET - otherwise ServiceHostFactory cannot be resolved.
	// The Mono Framework has System.ServiceModel.Activation.ServiceHostFactory included in System.ServiceModel.dll
    public sealed class FlatWsdlServiceHostFactory : ServiceHostFactory
    {
        public override ServiceHostBase CreateServiceHost(string constructorString, Uri[] baseAddresses)
        {
            return base.CreateServiceHost(constructorString, baseAddresses);
        }

        protected override ServiceHost CreateServiceHost(Type serviceType, Uri[] baseAddresses)
        {
            return new FlatWsdlServiceHost(serviceType, baseAddresses);
        }
    }
}
