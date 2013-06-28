using System.Runtime.InteropServices;

namespace ABC4TrustActiveX
{
    [ComVisible(true)]
    public class ReturObject
    {
        public string message { get; set; }
        public int status { get; set; }
        public string data { get; set; }
    }

}
