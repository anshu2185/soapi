package com.pk.si;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "pksi", mixinStandardHelpOptions = true, version = "pksi 1.0", header = { "  ______          ",
		" |  _ \\| |/ / ___| / _ \\  / \\  |  _ \\_ _|", " | |_) | ' /\\___ \\| | | |/ _ \\ | |_) | | ",
		" |  __/| . \\ ___) | |_| / ___ \\|  __/| |", " |_|   |_|\\_\\____/ \\___/_/   \\_\\_|  |___|",

}, description = "Scaffold a service project")
public class PKSI implements Runnable {

	@Parameters(index = "0", description = "Define service type - soap, rest")
	private String serviceType;

	@Option(names = { "-w", "--wsdllocation" }, description = "Location for wsdl and XSDs")
	private String wsdlLocation;

	@Option(names = { "-o", "--output" }, description = "Output location where project will be generated")
	private String outputLocation;

	@Option(names = { "-n", "--name" }, description = "Name of the service")
	private String nameOfService;

	private List<Path> wsdlFiles = new ArrayList<>();

	public void run() {
		System.out.println("==>" + serviceType);
		switch (serviceType) {
		case "soap":
			validateSoapServiceType();
			try {
				if(nameOfService == null) {
					nameOfService = "CXFSpring";
				}
				SoapProcessor.process(wsdlFiles, nameOfService, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.err.print("Only soap and rest service type suppported");
			System.exit(-1);
		}

	}

	private void validateSoapServiceType() {
		if (null == wsdlLocation) {
			System.err.println("-w or --wsdlLocation is required for soap service type");
		}
		System.out.println("==>" + wsdlLocation);
		File f = new File(wsdlLocation);
		if (!f.exists()) {
			System.err.println("WSDL is not available at given location");
		}
		if (f.isDirectory()) {

			try (Stream<Path> paths = Files.walk(Paths.get(wsdlLocation))) {
				paths.filter(Files::isRegularFile)
						.filter(p -> p.toString().endsWith(".wsdl") || p.toString().endsWith(".xsd"))
						.forEach(wsdlFiles::add);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (wsdlFiles.isEmpty()) {
			System.err.println("WSDL is not available at given location");
		} else {
			wsdlFiles.forEach(System.out::println);
		}
	}

	public static void main(String ar[]) {
		int exitCode = new CommandLine(new PKSI()).execute(ar);
		System.exit(exitCode);

	}

}