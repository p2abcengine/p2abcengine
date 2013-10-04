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

namespace UProveCrypto
{
    /// <summary>
    /// Exception thrown by a device implementation
    /// </summary>
    public class DeviceException : Exception
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="DeviceException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        public DeviceException(string message)
            : base(message)
        {
        }
    }
}
