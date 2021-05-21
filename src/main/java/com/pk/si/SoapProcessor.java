package com.pk.si;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.pk.template.HandlebarTemplateEngine;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.sun.tools.xjc.api.XJC;

public class SoapProcessor {

	public static HandlebarTemplateEngine templateEngine = new HandlebarTemplateEngine();
	private static String sourceFolder = "src" + File.separator + "main" + File.separator + "java";
	private static String resourceFolder = "src" + File.separator + "main" + File.separator + "resources";
	private static String testFolder = "src" + File.separator + "test" + File.separator + "java";
	private static String wsdlFolder = resourceFolder + File.separator + "wsdl";
	private static String apiVersion = "1.0.0";
	private static String groupId = "com.prokarma";
	private static String artifactId = "pk-soapi";
	private static String artifactVersion = "1.0.0";
	private static String basePackage = "com.digitalengineering.pk";
	private static String configPackage = "com.digitalengineering.pk.configuration";
	private static String loggingPackage = "com.digitalengineering.pk.soapmsglogging";
	private static String transformationPackage = "com.digitalengineering.pk.transformation";
	private static String controllerPackage = "com.digitalengineering.pk.controller";
	private static String endpointPackage = "com.digitalengineering.pk.endpoint";

	public static String getBasePackage() {
		return basePackage;
	}

	public static int process(List<Path> resourceFileList, String serviceName, String output) throws IOException {
		File outputFolder = null;

		if (output != null) {
			outputFolder = new File(output);

		} else {
			outputFolder = new File("./output");

		}
		outputFolder.mkdir();
		System.out.println("outputfolder===>" + outputFolder);
		List<String> folders = new ArrayList<String>();
		folders.add(sourceFolder);
		folders.add(resourceFolder);
		folders.add(testFolder);
		folders.add(wsdlFolder);
		folders.add(sourceFolder + "/" + getBasePackage().replace(".", File.separator) + "/" + "endpoint");
		folders.add(sourceFolder + "/" + configPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + loggingPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + transformationPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + controllerPackage.replace(".", File.separator));
		createFolder(folders, outputFolder);
		copyWsdl(resourceFileList, outputFolder.getPath() + "/" + wsdlFolder + "/");
		createConfigurationFiles(resourceFileList, configPackage, controllerPackage, loggingPackage, endpointPackage,
				outputFolder);
		generateServiceEndpoints(resourceFileList);
		List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
		supportingFiles.add(new SupportingFile("starterapplication.mustache",
				(outputFolder.getPath() + "/" + sourceFolder + "/" + getBasePackage().replace(".", File.separator)),
				serviceName + "Application" + ".java"));
		supportingFiles.add(new SupportingFile("logbackspring.mustache",
				(outputFolder.getPath() + "/" + resourceFolder), "logback-spring" + ".xml"));
		supportingFiles.add(new SupportingFile("application.mustache", (outputFolder.getPath() + "/" + resourceFolder),
				"application" + ".properties"));
		supportingFiles.add(new SupportingFile("loggingininterceptor.mustache",
				(outputFolder.getPath() + "/" + sourceFolder + "/" + loggingPackage.replace(".", File.separator)),
				"LoggingInInterceptorXmlOnly" + ".java"));
		supportingFiles.add(new SupportingFile("loggingoutinterceptor.mustache",
				(outputFolder.getPath() + "/" + sourceFolder + "/" + loggingPackage.replace(".", File.separator)),
				"LoggingOutInterceptorXmlOnly" + ".java"));
		supportingFiles.add(new SupportingFile("gitlabci.mustache", outputFolder.getPath(), ".gitlab-ci.yml"));
		supportingFiles.add(new SupportingFile("pom.mustache", outputFolder.getPath(), "pom.xml"));
		Map<String, Object> params = new HashMap<>();
		params.put("serviceName", serviceName);
		params.put("basePackage", getBasePackage());
		params.put("apiVersion", apiVersion);
		params.put("groupId", groupId);
		params.put("artifactId", artifactId);
		params.put("artifactVersion", artifactVersion);
		params.put("loggingPackage", loggingPackage);
		generateFiles(supportingFiles, params);
		System.out.println("Scaffolding successfull !!!!");
		return 0;
	}

	private static void generateServiceEndpoints(List<Path> resourceFileList) throws IOException {

		for (Path resource : resourceFileList) {
			if (resource.getFileName().toString().contains("wsdl")) {
				extractPortType(resource);
			}
		}
	}

	private static void extractPortType(Path resource) throws IOException {

		WSDLParser parser = new WSDLParser();
		Definitions defs = parser.parse(resource.toString());
		for (PortType pt : defs.getPortTypes()) {
			System.out.println(pt.getName());
			List<String> operationName = new ArrayList<String>();
			Map<String, Object> params = new HashMap<>();
			for (Operation op : pt.getOperations()) {
				System.out.println("==>" + op.getName());
				operationName.add(op.getName());

			}
			params.put("endpointName", pt.getName());
			params.put("opName", operationName);
			params.put("endpointPackage", endpointPackage);
			params.put("controllerPackage", controllerPackage);

			generateEndpointFiles(pt.getName(), operationName, params);
		}
	}

	private static void generateEndpointFiles(String endpointName, List<String> operationName,
			Map<String, Object> params) throws IOException {

		File outputFile = new File(
				sourceFolder + "/" + getBasePackage().replace(".", File.separator) + "/" + "endpoint",
				endpointName + "Endpoint.java");
		System.out.println(outputFile.getAbsolutePath());
		String formatted = templateEngine.getRendered("sei.mustache", params);
		// System.out.println(formatted);
		FileUtils.writeStringToFile(outputFile, formatted, Charset.defaultCharset());

	}

	private static void copyWsdl(List<Path> resourceFileList, String folder) throws IOException {
		for (Path resource : resourceFileList) {
			Files.copy(resource, new File(folder + resource.getFileName()).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		}

	}

	private static void generateFiles(List<com.pk.si.SupportingFile> supportingFiles, Map<String, Object> params)
			throws IOException {

		for (SupportingFile supportingFile : supportingFiles) {
			writeToFile(supportingFile.getFolder(), supportingFile.getTemplateFile(),
					supportingFile.getDestinationFilename(), params);
		}
	}

	private static void createFolder(List<String> folders, File outputDir) {
		for (String folder : folders) {
			new File(outputDir.getPath() + "/" + folder).mkdirs();
		}

	}

	private static void writeToFile(String folder, String templateFile, String destinationFilename,
			Map<String, Object> params) throws IOException {

		File outputFile = new File(folder, destinationFilename);
		String formatted = templateEngine.getRendered(templateFile, params);
		FileUtils.writeStringToFile(outputFile, formatted, Charset.defaultCharset());

	}

	private static void createConfigurationFiles(List<Path> resourceFileList, String configPackage,
			String controllerPackage, String loggingPackage, String endpointPackage, File outputFolder)
			throws IOException {
		for (Path resource : resourceFileList) {
			if (resource.getFileName().toString().contains("wsdl")) {
				WSDLParser parser = new WSDLParser();
				Definitions defs = parser.parse(resource.toString());
				String targetNamespace = defs.getTargetNamespace();
				String packageNameFromNamespace = XJC.getDefaultPackageName(targetNamespace);
				String wsdlServiceName = null;
				String location = null;
				for (Service service : defs.getServices()) {
					wsdlServiceName = service.getName();
					for (Port port : service.getPorts()) {
						location = port.getAddress().getLocation()
								.substring(port.getAddress().getLocation().lastIndexOf("/") + 1);
						break;
					}
					break;

				}
				for (PortType pt : defs.getPortTypes()) {
					List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
					Map<String, Object> params = new HashMap<>();
					String portTypeName = pt.getName();
					char c[] = portTypeName.toCharArray();
					c[0] = Character.toLowerCase(c[0]);
					portTypeName = new String(c);
					supportingFiles.add(new SupportingFile("appconfig.mustache",
							(outputFolder.getPath() + "/" + sourceFolder+"/"+configPackage.replace(".", File.separator)),
							"ApplicationConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("webserviceconfig.mustache",
							(outputFolder.getPath() + "/" + sourceFolder+"/"+configPackage.replace(".", File.separator)),
							"WebServiceConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("controller.mustache",
							(outputFolder.getPath() + "/" + sourceFolder+"/"+controllerPackage.replace(".", File.separator)),
							pt.getName() + "Controller" + ".java"));
					params.put("configPackage", configPackage);
					params.put("controllerPackage", controllerPackage);
					params.put("loggingPackage", loggingPackage);
					params.put("porttype", pt.getName());
					params.put("portTypeName", portTypeName);
					params.put("wsdlServiceName", wsdlServiceName);
					params.put("svclowercase", wsdlServiceName.toLowerCase());
					params.put("packageNameFromNamespace", packageNameFromNamespace);
					params.put("location", location);
					params.put("endpointPackage", endpointPackage);
					generateFiles(supportingFiles, params);
					break;

				}

			}
		}

	}

}
