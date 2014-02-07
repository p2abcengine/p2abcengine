//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.junit.Test;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.xml.util.XmlUtils;

public class UiJaxbExperimentsTest {
  
  /**
   * Outputs a schema for the return types.
   * @throws Exception
   */
  @Test
  public void testSchema() throws Exception
  {
      JAXBContext context = JAXBContext.newInstance( eu.abc4trust.returnTypes.ObjectFactoryReturnTypes.class );

      final List<DOMResult> results = new ArrayList<DOMResult>();

      // generate the schema
      context.generateSchema(
              // need to define a SchemaOutputResolver to store to
              new SchemaOutputResolver()
              {
                  @Override
                  public Result createOutput( String ns, String file )
                          throws IOException
                  {
                      // save the schema to the list
                      DOMResult result = new DOMResult();
                      result.setSystemId( file );
                      results.add( result );
                      return result;
                  }
              } );

      for(DOMResult domResult: results) {  
        System.out.println("\n\n\n###############\n\n\n");
        Document doc = (Document) domResult.getNode();
        OutputFormat format = new OutputFormat( doc );
        format.setIndenting( true );
        XMLSerializer serializer = new XMLSerializer( System.out, format );
        serializer.serialize( doc );
      }
  }
  
  @Test
  public void unmarshallXml() throws Exception {
    UiPresentationArguments args =
    ((UiPresentationArguments) XmlUtils.getObjectFromXML(
        getClass().getResourceAsStream(
            "/eu/abc4trust/sampleXml/ui/ui-pres-1.xml"), false));
    
    System.out.println(args);
  }


}
