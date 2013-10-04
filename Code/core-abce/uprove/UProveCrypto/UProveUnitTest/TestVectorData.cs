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

namespace UProveCryptoTest
{
    static class TestVectorData
    {
        public class HashVectors
        {
            public static String UIDh = "SHA-256";
            public static String hash_byte = "4bf5122f344554c53bde2ebb8cd2b7e3d1600ad631c385a5d7cce23c7785459a";
            public static String hash_octetstring = "16df7d2d0c3882334fe0457d298a7b2413e1e5b7a880f0b5ec79eeeae7f58dd8";
            public static String hash_null = "df3f619804a92fdb4057192dc43dd748ea778adc52bc498ce80524c014b81119";
            public static String hash_list = "dfd6a31f867566ffeb6c657af1dafb564c3de74485058426633d4b6c8bad6732";
            public static String hash_subgroup = "7b36c8a3cf1552077e1cacb365888d25c9dc54f3faed7aff9b11859aa8e4ba06";
            public static String hash_ecgroup = "02bb879cb2f89c19579105be662247db15ab45875cfc63a58745361d193ba248";
        }
    }
}