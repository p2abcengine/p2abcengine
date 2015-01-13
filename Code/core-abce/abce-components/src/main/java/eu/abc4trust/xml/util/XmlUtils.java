//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.xml.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import eu.abc4trust.xml.CryptoParams;

public class XmlUtils {

    private static Schema abc4trustSchema = null;
    private static JAXBContext context = null;

    private static JAXBContext getContext() throws JAXBException {
        if(context==null) {
            context = JAXBContext.newInstance(eu.abc4trust.xml.ObjectFactory.class, eu.abc4trust.returnTypes.ObjectFactoryReturnTypes.class);
            //System.out.println(context.toString());
        }
        return context;
    }


    private static Schema getSchema() {
        if(abc4trustSchema == null) {
            // this XSD includes original + ui json + pilot specifid + test
            InputStream xmlSchema_all = XmlUtils.class.getResourceAsStream("/xsd/schema-include-all.xsd");
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // only include xsd once
            final HashSet<String> seen = new HashSet<String>();
            // resolver will create 'one' scheame with all definitions...
            schemaFactory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(final String type, final String namespaceURI,
                        final String publicId, final String systemId,
                        final String baseURI) {
                    if(! seen.add(systemId)) {
                        return null;
                    }
                    final InputStream xmlSchema_included = XmlUtils.class.getResourceAsStream("/xsd/" + systemId );
                    return new LSInput() {
                        @Override
                        public void setSystemId(String systemId) {
                        }
                        @Override
                        public void setStringData(String stringData) {
                        }
                        @Override
                        public void setPublicId(String publicId) {
                        }
                        @Override
                        public void setEncoding(String encoding) {
                        }
                        @Override
                        public void setCharacterStream(Reader characterStream) {
                        }
                        @Override
                        public void setCertifiedText(boolean certifiedText) {
                        }
                        @Override
                        public void setByteStream(InputStream byteStream) {
                        }
                        @Override
                        public void setBaseURI(String baseURI) {
                        }
                        @Override
                        public String getSystemId() {
                            return systemId;
                        }
                        @Override
                        public String getStringData() {
                            return null;
                        }
                        @Override
                        public String getPublicId() {
                            return publicId;
                        }
                        @Override
                        public String getEncoding() {
                            return null;
                        }
                        @Override
                        public Reader getCharacterStream() {
                            return null;
                        }
                        @Override
                        public boolean getCertifiedText() {
                            return false;
                        }
                        @Override
                        public InputStream getByteStream() {
                            return xmlSchema_included;
                        }
                        @Override
                        public String getBaseURI() {
                            return null;
                        }
                    };
                }
            });
            try {
                abc4trustSchema = schemaFactory.newSchema(new StreamSource(xmlSchema_all));
            } catch (SAXException e) {
                throw new RuntimeException("Cannot load abc4trust schema: " + e.getMessage());
            }
        }
        return abc4trustSchema;
    }

    @Deprecated
    public static Object getObjectFromXML(String string) throws JAXBException,
    UnsupportedEncodingException, SAXException {
        return getObjectFromXML(string, true);
    }

    @Deprecated
    public static Object getObjectFromXML(String string, boolean validate) throws JAXBException,
    UnsupportedEncodingException, SAXException {
        return getJaxbElementFromXml(string, validate).getValue();
    }

    public static Object getObjectFromXML(InputStream inputStream, boolean validate)
            throws JAXBException, UnsupportedEncodingException, SAXException {
        return getJaxbElementFromXml(inputStream, validate).getValue();
    }

    @Deprecated
    public static JAXBElement<?> getJaxbElementFromXml(String string, boolean validate)
            throws JAXBException, UnsupportedEncodingException, SAXException {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
        return getJaxbElementFromXml(inputStream, validate);
    }

    public static JAXBElement<?> getJaxbElementFromXml(InputStream inputStream, boolean validate)
            throws JAXBException, UnsupportedEncodingException, SAXException {
        JAXBContext jc = getContext();
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        if (validate) {
            unmarshaller.setSchema(getSchema());
        }

        Object object = unmarshaller.unmarshal(inputStream);
        JAXBElement<?> e = (JAXBElement<?>) object;
        return e;
    }

    public static String toXml(JAXBElement<?> element) throws JAXBException, SAXException {
        return toXml(element, true);
    }

    public static String toXml(JAXBElement<?> element, boolean validate) throws JAXBException, SAXException {
        ByteArrayOutputStream byteArrayOutputStream = toXmlAsBaos(element, validate);
        try {
            return byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toXmlFromObject(Object element) throws JAXBException, SAXException {
        JAXBContext jaxbcontext = getContext();
        Marshaller marshaller = jaxbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new AbcNamespace());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(element, byteArrayOutputStream);
        try {
            return byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteArrayOutputStream toXmlAsBaos(JAXBElement<?> element, boolean validate) throws JAXBException, SAXException {
        JAXBContext jaxbcontext = getContext();
        Marshaller marshaller = jaxbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new AbcNamespace());
        if (validate) {
            marshaller.setSchema(getSchema());
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(element, byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public static String toNormalizedXML(JAXBElement<?> el) throws JAXBException, ParserConfigurationException, SAXException, IOException  {
        String s = toXml(el, true);
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes("UTF-8"));
        return toNormalizedXML(bais);
    }

    /**
     * Remove leading a trailing whitespace characters from each line of input
     * @param input
     * @return
     */
    public static String trim(String input) {
        final String newlineDelimiters = "\n\r\f";

        StringBuilder ret = new StringBuilder();
        StringTokenizer st = new StringTokenizer(input, newlineDelimiters);
        while (st.hasMoreTokens()) {
            ret.append(st.nextToken().replaceAll("^\\s+", "").replaceAll("\\s+$", ""));
            ret.append('\n');
        }
        return ret.toString();
    }

    public static String toNormalizedXML(InputStream is) throws ParserConfigurationException, SAXException, IOException  {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(is);
        document.normalizeDocument();
        document.getDocumentElement()
        .setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://abc4trust.eu/wp2/abcschemav1.0 ../../../../../../../../../abc4trust-xml/src/main/resources/xsd/schema.xsd");
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String xml = serializer.writeToString(document);

        return trim(xml);
    }

    /**
     * Given an object that is presumably a JAXBElement<clazz>, return object.getValue(). This method
     * is useful for parsing XML elements of type xs:any (but where you know the type is clazz).
     * 
     * If object is not a JAXBElement<clazz>, return null else return
     * ((JAXBElement<?>)object).getValue()
     * 
     * @param jaxbElement
     * @param clazz
     * @return
     */
    public static <T> T unwrap(Object jaxbElement, Class<T> clazz) {
        if (jaxbElement instanceof JAXBElement<?>) {
            Object ret = ((JAXBElement<?>) jaxbElement).getValue();
            if (clazz.isInstance(ret)) {
                return (T)ret;
            } else {
                System.err.println("Cannot cast " + ret + " to class " + clazz + " (actual class is "
                        + ret.getClass() + ").");
                return null; // TODO(enr): Throw an exception here
            }
        } else if (clazz.isInstance(jaxbElement)) {
            return (T)jaxbElement;
        } else {
            System.err.println("Cannot cast " + jaxbElement + " to class JAXBElement<?> or " + clazz
                    + " (actual class is " + jaxbElement.getClass() + ").");
            return null; // TODO(enr): Throw an exception here
        }
    }

    /**
     * Given a list of objects that presumably contains one and only one JAXBElement<clazz>, return
     * list.get(0).getObject(). This method is useful for parsing a sequence of XML elements of type
     * xs:any (but where you know that there is only one element of type clazz).
     * 
     * If the list contains 0, or more than 2 elements, return null. If the first element of the list
     * is not a JAXBElement<clazz>, return null else return ((JAXBElement<?>)list.get(0)).getValue()
     * 
     * @param jaxbElementList
     * @param clazz
     * @return
     */
    public static <T> T unwrap(List<Object> jaxbElementList, Class<T> clazz) {
      List<Object> cleanedUpList = cleanupXmlContents(jaxbElementList);
        if (cleanedUpList.size() == 1) {
            return unwrap(cleanedUpList.get(0), clazz);
        } else {
            System.err.println("Cannot unwrap " + cleanedUpList + ". Size is " + cleanedUpList.size()
                    + " (expected 1). Before cleanup: " + jaxbElementList + ".");
            return null; // TODO(enr): Throw an exception here
        }
    }
    
    public static List<Object> cleanupXmlContents(List<Object> jaxbElementList) {
      List<Object> cleanedUpList = new ArrayList<>();
      for(Object o: jaxbElementList) {
        if(o instanceof String) {
          // All whitespace in XML denotes formatting
          if(((String) o).trim().length() == 0) {
            continue;
          }
        }
        cleanedUpList.add(o);
      }
      return cleanedUpList;
    }
    
    public static void fixNestedContent(final CryptoParams cp) {
    	final List<Object> content = cp.getContent();
    	final List<Object> filtered = new ArrayList<Object>();
    	for (final Object o : content) {
    		if (!"".equals(o.toString().trim())) {
    			if (o instanceof JAXBElement) {
					JAXBElement<?> unwrapped = (JAXBElement<?>) o;
					filtered.add(unwrapped.getValue());
				} else {
					filtered.add(o);
				}
    		}
    	}
    	content.clear();
    	content.addAll(filtered);
    }
}
