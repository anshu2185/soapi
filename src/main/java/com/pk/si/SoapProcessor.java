package com.pk.si;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import com.sun.tools.xjc.api.XJC;

import groovy.xml.MarkupBuilder;
import me.tongfei.progressbar.ProgressBar;

public class SoapProcessor {

	public static HandlebarTemplateEngine templateEngine = new HandlebarTemplateEngine();
	private static String sourceFolder = "src" + File.separator + "main" + File.separator + "java";
	private static String resourceFolder = "src" + File.separator + "main" + File.separator + "resources";
	private static String testFolder = "src" + File.separator + "test" + File.separator + "java";
	private static String testResourceFolder = "src" + File.separator + "test" + File.separator + "resources";
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
	private static String qname = null;

	public static String getBasePackage() {
		return basePackage;
	}

	public static int process(List<Path> resourceFileList, String serviceName, String output, ProgressBar pb,
			String wsdlLocation) throws IOException {
		File outputFolder = null;

		if (output != null) {
			outputFolder = new File(output);

		} else {
			outputFolder = new File("./output");

		}
		outputFolder.mkdir();
		pb.step();
		List<String> folders = new ArrayList<String>();
		folders.add(sourceFolder);
		folders.add(resourceFolder);
		folders.add(testFolder);
		folders.add(testResourceFolder);
		folders.add(testResourceFolder + "/" + "requests");
		folders.add(wsdlFolder);
		folders.add(sourceFolder + "/" + getBasePackage().replace(".", File.separator) + "/" + "endpoint");
		folders.add(sourceFolder + "/" + configPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + loggingPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + transformationPackage.replace(".", File.separator));
		folders.add(sourceFolder + "/" + controllerPackage.replace(".", File.separator));
		createFolder(folders, outputFolder);
		pb.stepTo(600);
		// copyWsdl(resourceFileList, outputFolder.getPath() + "/" + wsdlFolder + "/");
		copyWsdlAsitis(wsdlLocation, outputFolder.getPath() + "/" + wsdlFolder + "/");
		createConfigurationFiles(resourceFileList, outputFolder, pb);
		List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
		supportingFiles.add(new SupportingFile("starterapplication.mustache",
				(outputFolder.getPath() + "/" + sourceFolder + "/" + getBasePackage().replace(".", File.separator)),
				serviceName + "Application" + ".java"));
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
		supportingFiles.add(new SupportingFile("bindingxml.mustache", (outputFolder.getPath() + "/" + resourceFolder),
				"binding" + ".xml"));
		Map<String, Object> params = new HashMap<>();
		params.put("serviceName", serviceName);
		params.put("basePackage", getBasePackage());
		params.put("apiVersion", apiVersion);
		params.put("groupId", groupId);
		params.put("artifactId", artifactId);
		params.put("artifactVersion", artifactVersion);
		params.put("loggingPackage", loggingPackage);
		generateFiles(supportingFiles, params, pb);
		pb.stepTo(2500);
		return 0;
	}

	private static void copyWsdlAsitis(String wsdlLocation, String wsdlFolder) {
		try {
			System.out.println("wsdlFolder::" + wsdlFolder);
			System.out.println("wsdlLocation::" + wsdlLocation);
			FileUtils.copyDirectory(new File(wsdlLocation), new File(wsdlFolder));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static List<WsdlProperties> generateMethodandReturnTypes(List<Path> resourceFileList) {
		List<WsdlProperties> w = new ArrayList<WsdlProperties>();
		for (Path resource : resourceFileList) {
			if (resource.getFileName().toString().contains("wsdl")) {
				WSDLParser parser = new WSDLParser();
				Definitions defs = parser.parse(resource.toString());
				List<String> l = null;
				List<String> l2 = null;
				for (PortType pt : defs.getPortTypes()) {
					l = new ArrayList<String>();
					l2 = new ArrayList<String>();
					for (Operation op : pt.getOperations()) {

						l.add(op.getName());
						l2.add(op.getInput().getMessage().getQname().getLocalPart());
						l2.add(op.getOutput().getMessage().getQname().getLocalPart());
					}
				}

				List<String> l1 = new ArrayList<String>();
				boolean flag = false;
				for (Message msg : defs.getMessages()) {
					for (Part part : msg.getParts()) {
						if (part.getName() != null && !part.getName().equals("")) {
							if (l2.contains(part.getName())) {
								l1.add(part.getName());
								flag = true;
							}
						}
						if (!flag) {
							if (part.getElement() != null) {
								if (l2.contains(part.getElement().getQname().getLocalPart())) {
									l1.add(part.getElement().getQname().getLocalPart());

								}
							}

						}
					}

				}
				boolean check = false;
				if (l1.isEmpty()) {
					for (Message msg : defs.getMessages()) {
						for (Part part : msg.getParts()) {
							if (part.getElement() != null) {
								if (!check) {
									qname = part.getElement().getQname().getNamespaceURI();
									check = true;
								}
								l1.add(part.getElement().getQname().getLocalPart());
							}
						}
					}
				}

				if (!(l1.size() % 2 == 0)) {
					l1.remove(l1.size() - 1);
				}
				int count = 0;
				for (String lop : l) {
					WsdlProperties w1 = new WsdlProperties();
					w1.setOperationName(lop);

					callL1(w1, l1, count);
					w.add(w1);
					count = count + 2;
				}
				System.out.println(w);

			}
		}
		return w;
	}

	private static void callL1(WsdlProperties w1, List<String> l1, int count) {
		if (count < l1.size()) {
			w1.setInputParam(l1.get(count));
			w1.setReturnType(l1.get(count + 1));
		}

	}

	private static void copyWsdl(List<Path> resourceFileList, String folder) throws IOException {
		for (Path resource : resourceFileList) {
			Files.copy(resource, new File(folder + resource.getFileName()).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		}

	}

	private static void generateFiles(List<com.pk.si.SupportingFile> supportingFiles, Map<String, Object> params,
			ProgressBar pb) throws IOException {

		for (SupportingFile supportingFile : supportingFiles) {
			writeToFile(supportingFile.getFolder(), supportingFile.getTemplateFile(),
					supportingFile.getDestinationFilename(), params);
		}
		pb.stepTo(1337);
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

	private static void createConfigurationFiles(List<Path> resourceFileList, File outputFolder, ProgressBar pb)
			throws IOException {
		for (Path resource : resourceFileList) {
			if (resource.getFileName().toString().contains("wsdl")) {
				WSDLParser parser = new WSDLParser();
				Definitions defs = parser.parse(resource.toString());
				String targetNamespace = defs.getTargetNamespace();
				String packageNameFromNamespace = XJC.getDefaultPackageName(targetNamespace);
				String wsdlServiceName = null;
				String location = null;
				String wsdlSvcNamefirstLetterSmall = null;
				for (Service service : defs.getServices()) {
					wsdlServiceName = service.getName();
					char svc[] = wsdlServiceName.toCharArray();
					svc[0] = Character.toLowerCase(svc[0]);
					wsdlSvcNamefirstLetterSmall = new String(svc);
					for (Port port : service.getPorts()) {
						location = port.getAddress().getLocation()
								.substring(port.getAddress().getLocation().lastIndexOf("/") + 1);
						break;
					}
					break;

				}
				String portTypeName = null;
				String portType = null;
				for (PortType pt : defs.getPortTypes()) {
					List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
					Map<String, Object> params = new HashMap<>();
					portType = pt.getName();
					portTypeName = pt.getName();
					char c[] = portTypeName.toCharArray();
					c[0] = Character.toLowerCase(c[0]);
					portTypeName = new String(c);
					supportingFiles.add(new SupportingFile("appconfig.mustache",
							(outputFolder.getPath() + "/" + sourceFolder + "/"
									+ configPackage.replace(".", File.separator)),
							"ApplicationConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("webserviceconfig.mustache",
							(outputFolder.getPath() + "/" + sourceFolder + "/"
									+ configPackage.replace(".", File.separator)),
							"WebServiceConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("apptestconfiguration.mustache",
							(outputFolder.getPath() + "/" + testFolder + "/"
									+ basePackage.replace(".", File.separator)),
							"ApplicationTestConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("cxfteststarter.mustache",
							(outputFolder.getPath() + "/" + testFolder + "/"
									+ basePackage.replace(".", File.separator)),
							"SimpleBootCxfSystemTestApplication" + ".java"));
					supportingFiles.add(new SupportingFile("webserviceinttestconfig.mustache",
							(outputFolder.getPath() + "/" + testFolder + "/"
									+ basePackage.replace(".", File.separator)),
							"WebServiceIntegrationTestConfiguration" + ".java"));
					supportingFiles.add(new SupportingFile("webservicesystemtest.mustache",
							(outputFolder.getPath() + "/" + testFolder + "/"
									+ basePackage.replace(".", File.separator)),
							"WebServiceSystemTestConfiguration" + ".java"));
					supportingFiles
							.add(new SupportingFile("xmlutils.mustache",
									(outputFolder.getPath() + "/" + testFolder + "/"
											+ basePackage.replace(".", File.separator) + "/" + "utils"),
									"XmlUtils" + ".java"));
					supportingFiles.add(new SupportingFile(
							"xmlutilsexception.mustache", (outputFolder.getPath() + "/" + testFolder + "/"
									+ basePackage.replace(".", File.separator) + "/" + "utils"),
							"XmlUtilsException" + ".java"));

					supportingFiles.add(new SupportingFile("logbackspring.mustache",
							(outputFolder.getPath() + "/" + resourceFolder), "logback-spring" + ".xml"));
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
					params.put("basePackage", basePackage);
					params.put("wsdlSvcNamefirstLetterSmall", wsdlSvcNamefirstLetterSmall);
					generateFiles(supportingFiles, params, pb);
					break;

				}
				String faultName = null;
				for (Binding bnd : defs.getBindings()) {
					List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
					Map<String, Object> params = new HashMap<>();
					String bindingName = bnd.getName();
					String bindingType = bnd.getPortType().getName();
					char pt[] = bindingType.toCharArray();
					pt[0] = Character.toLowerCase(pt[0]);
					List<String> opName = new ArrayList<String>();
					List<String> opNameOriginal = new ArrayList<String>();
					for (BindingOperation bop : bnd.getOperations()) {
						if (bop.getFaults().size() > 0) {
							faultName = bop.getFaults().get(0).getName();
						}

						StringWriter writer = new StringWriter();
						SOARequestCreator creator = new SOARequestCreator(defs, new RequestTemplateCreator(),
								new MarkupBuilder(writer));
						creator.createRequest(bindingType, bop.getName(), bindingName);
						FileWriter fileWriter = new FileWriter(new File(outputFolder.getPath() + "/"
								+ testResourceFolder + "/" + "requests" + "/" + bop.getName() + "Test.xml"));
						fileWriter.write(writer.toString());
						fileWriter.close();
						char c[] = bop.getName().toCharArray();
						c[0] = Character.toLowerCase(c[0]);
						opName.add(new String(c));
						opNameOriginal.add(bop.getName());
					}
					/*
					 * supportingFiles.add(new SupportingFile( "serviceinttest.mustache",
					 * (outputFolder.getPath() + "/" + testFolder + "/" + basePackage.replace(".",
					 * File.separator) + "/" + "test"), bindingType + "IntegrationTest" + ".java"));
					 * supportingFiles.add(new SupportingFile( "servicesystemtest.mustache",
					 * (outputFolder.getPath() + "/" + testFolder + "/" + basePackage.replace(".",
					 * File.separator) + "/" + "test"), bindingType + "SystemTest" + ".java"));
					 * supportingFiles.add(new SupportingFile( "servicetest.mustache",
					 * (outputFolder.getPath() + "/" + testFolder + "/" + basePackage.replace(".",
					 * File.separator) + "/" + "test"), bindingType + "Test" + ".java"));
					 * supportingFiles.add(new SupportingFile( "servicexmltest.mustache",
					 * (outputFolder.getPath() + "/" + testFolder + "/" + basePackage.replace(".",
					 * File.separator) + "/" + "test"), bindingType + "XmlFileSystemTest" +
					 * ".java"));
					 */

					params.put("faultName", faultName);
					params.put("basePackage", basePackage);
					params.put("opName", opName);
					params.put("packageNameFromNamespace", packageNameFromNamespace);
					params.put("portTypeName", new String(pt));
					params.put("portType", bindingType);
					params.put("opNameOriginal", opNameOriginal);
					// generateFiles(supportingFiles, params, pb);
					break;
				}

				List<WsdlProperties> wsdlProperties = generateMethodandReturnTypes(resourceFileList);
				List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
				Map<String, Object> params = new HashMap<>();
				supportingFiles.add(new SupportingFile(
						"sei.mustache", (outputFolder.getPath() + "/" + sourceFolder + "/"
								+ basePackage.replace(".", File.separator) + "/" + "endpoint"),
						wsdlServiceName + "ServiceEndpoint" + ".java"));
				supportingFiles.add(new SupportingFile("controller.mustache",
						(outputFolder.getPath() + "/" + sourceFolder + "/"
								+ controllerPackage.replace(".", File.separator)),
						wsdlServiceName + "ServiceController" + ".java"));
				supportingFiles.add(new SupportingFile(
						"serviceinttest.mustache", (outputFolder.getPath() + "/" + testFolder + "/"
								+ basePackage.replace(".", File.separator) + "/" + "test"),
						wsdlServiceName + "ServiceIntegrationTest" + ".java"));
				supportingFiles.add(new SupportingFile(
						"servicesystemtest.mustache", (outputFolder.getPath() + "/" + testFolder + "/"
								+ basePackage.replace(".", File.separator) + "/" + "test"),
						wsdlServiceName + "ServiceSystemTest" + ".java"));
				supportingFiles.add(new SupportingFile(
						"servicetest.mustache", (outputFolder.getPath() + "/" + testFolder + "/"
								+ basePackage.replace(".", File.separator) + "/" + "test"),
						wsdlServiceName + "ServiceTest" + ".java"));
				supportingFiles.add(new SupportingFile(
						"servicexmltest.mustache", (outputFolder.getPath() + "/" + testFolder + "/"
								+ basePackage.replace(".", File.separator) + "/" + "test"),
						wsdlServiceName + "ServiceXmlFileSystemTest" + ".java"));
				supportingFiles
						.add(new SupportingFile("testhelper.mustache",
								(outputFolder.getPath() + "/" + testFolder + "/"
										+ basePackage.replace(".", File.separator) + "/" + "utils"),
								"TestHelper" + ".java"));
				if (qname != null) {
					params.put("namespace", XJC.getDefaultPackageName(qname));
				}
				params.put("basePackage", basePackage);
				params.put("wsdlServiceName", wsdlServiceName);
				params.put("packageNameFromNamespace", packageNameFromNamespace);
				params.put("portTypeName", portTypeName);
				params.put("portType", portType);
				params.put("wsdlproperties", wsdlProperties);
				params.put("faultName", faultName);
				generateFiles(supportingFiles, params, pb);

			}
		}

	}

}
