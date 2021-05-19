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
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

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
	private static String basePackage = "com.prokarma.soapi";
	private static String configPackage = "com.prokarma.soapi.config";

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
		folders.add(sourceFolder+"/"+getBasePackage().replace(".", File.separator)+"/"+"endpoint");
		createFolder(folders, outputFolder);
		copyWsdl(resourceFileList, outputFolder.getPath() + "/" + wsdlFolder + "/");
		generateServiceEndpoints(resourceFileList);
		List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
		supportingFiles.add(new SupportingFile("starterapplication.mustache",
				(outputFolder.getPath() + "/" + sourceFolder + "/" + getBasePackage().replace(".",File.separator)),
				serviceName + "Application" + ".java"));
		supportingFiles.add(new SupportingFile("logbackspring.mustache",
				(outputFolder.getPath() + "/" + resourceFolder), "logback-spring" + ".xml"));
		supportingFiles.add(new SupportingFile("application.mustache",
				(outputFolder.getPath() + "/" + resourceFolder), "application" + ".properties"));
		supportingFiles.add(new SupportingFile("gitlabci.mustache", outputFolder.getPath(), ".gitlab-ci.yml"));
		supportingFiles.add(new SupportingFile("pom.mustache", outputFolder.getPath(), "pom.xml"));
		Map<String, Object> params = new HashMap<>();
		params.put("serviceName", serviceName);
		params.put("basePackage", getBasePackage());
		params.put("apiVersion", apiVersion);
		params.put("groupId", groupId);
		params.put("artifactId", artifactId);
		params.put("artifactVersion", artifactVersion);
		generateFiles(supportingFiles, params);
		return 0;
	}

	private static void generateServiceEndpoints(List<Path> resourceFileList) throws IOException {

		for(Path resource:resourceFileList) {
			if(resource.getFileName().toString().contains("wsdl")) {
				extractPortType(resource) ;
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
			System.out.println("==>"+op.getName());
			operationName.add(op.getName());
			
			}
			params.put("endpointName", pt.getName());
			params.put("opName", operationName);
			
			generateEndpointFiles(pt.getName(),operationName,params);
		}
	}

	private static void generateEndpointFiles(String endpointName, List<String> operationName, Map<String, Object> params) throws IOException {
		
		File outputFile = new File(sourceFolder+"/"+getBasePackage().replace(".", File.separator)+"/"+"endpoint", endpointName+"Endpoint.java");
		System.out.println(outputFile.getAbsolutePath());
		String formatted = templateEngine.getRendered("sei.mustache", params);
		System.out.println(formatted);
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

}
