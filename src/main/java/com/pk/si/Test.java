package com.pk.si;

import java.io.File;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

public class Test {
	public static void main(String[] args) {
		WSDLParser parser = new WSDLParser();
		Definitions defs = parser.parse("/home/sanshuman/Downloads/AccountLookup.wsdl");
		for (PortType pt : defs.getPortTypes()) {
			System.out.println(pt.getName());
			for (Operation op : pt.getOperations()) {
			System.out.println("==>"+op.getName());
			}

		}
	}
}
