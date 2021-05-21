package com.pk.si;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.ow2.easywsdl.schema.api.XmlException;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;


public class Test {
	public static void main(String[] args) throws XmlException {
		/*WSDLParser parser = new WSDLParser();
		Definitions defs = parser.parse("/home/sanshuman/Downloads/AccountLookup.wsdl");
		String wsdlServiceName = null;
		String location = null;
		for (Service service : defs.getServices()) {
			wsdlServiceName = service.getName();
			System.out.println(wsdlServiceName);
			for (Port port : service.getPorts()) {
				System.out.println(port.getAddress().getLocation());
				String last = port.getAddress().getLocation().substring(port.getAddress().getLocation().lastIndexOf("/") + 1);
				System.out.println(last);
				break;
			}
			break;

		}
		String string = "WeatherService";
		char c[] = string.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		string = new String(c);
		System.out.println(string);*/
		
		try {
	        // Read a WSDL 1.1 or 2.0
	        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
	        Description desc = reader.read(Paths.get("/home/sanshuman/Anshuman/virtualization-tools/testwsdl/Weather1.1.wsdl").toUri().toURL());
	        // Endpoints take place in services. 
	        // Select a service
	        Service service = desc.getServices().get(0);
	        List<Endpoint> endpoints = service.getEndpoints();
	        System.out.println(endpoints);
	        //Gets address of first endpoint
	        System.out.println(endpoints.get(0).getAddress());
	        //Gets http method
	        System.out.println(endpoints.get(0).getBinding().getBindingOperations().get(0).getHttpMethod());
	        //Gets input type
	        System.out.println(endpoints.get(0).getBinding().getInterface().getOperations().get(0).getInput().getName());
	        System.out.println(endpoints.get(0).getBinding().getInterface().getOperations().get(0).getInput().getElement().getType().getQName().getLocalPart());

	    } catch (WSDLException | IOException | URISyntaxException e1) {
	        e1.printStackTrace();
	    }
	}
}
