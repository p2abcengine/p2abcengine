using System;
using System.Collections.Generic;
using System.Text;
using System.ServiceModel;
using System.ServiceModel.Description;

namespace Thinktecture.ServiceModel.Extensions.Description
{
    public class FlatWsdlServiceHost : ServiceHost
    {
        public FlatWsdlServiceHost()
        {
        }

        public FlatWsdlServiceHost(Type serviceType, params Uri[] baseAddresses)
            :
            base(serviceType, baseAddresses)
        {
        }

        public FlatWsdlServiceHost(object singeltonInstance, params Uri[] baseAddresses)
            :
            base(singeltonInstance, baseAddresses)
        {
        }

        protected override void ApplyConfiguration()
        {
            base.ApplyConfiguration();
            InjectFlatWsdlExtension();
        }

        private void InjectFlatWsdlExtension()
        {
            foreach (ServiceEndpoint endpoint in this.Description.Endpoints)
            {
                endpoint.Behaviors.Add(new FlatWsdl());
            }
        }
    }
}


