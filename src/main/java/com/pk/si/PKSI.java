package com.pk.si;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "pksi", mixinStandardHelpOptions = true, version = "pksi 1.0", header = { "  ______          ",
		" |  _ \\| |/ / ___| / _ \\  / \\  |  _ \\_ _|", " | |_) | ' /\\___ \\| | | |/ _ \\ | |_) | | ",
		" |  __/| . \\ ___) | |_| / ___ \\|  __/| |", " |_|   |_|\\_\\____/ \\___/_/   \\_\\_|  |___|",

}, description = "Scaffold a service project")
public class PKSI implements Runnable {
	
	@Option(names = { "-s", "--font-size" }, description = "Font size") // |3|
    int fontSize = 14;

    @Parameters(paramLabel = "<word>", defaultValue = "SOAPInitializer",  // |4|
               description = "Words to be translated into ASCII art.")
    private String[] words = { "SOAPInitializer" }; 
    
	@Parameters(index = "0", description = "Define service type - soap, rest")
	private String serviceType;

	@Option(names = { "-w", "--wsdllocation" }, description = "Location for wsdl and XSDs")
	private String wsdlLocation;

	@Option(names = { "-o", "--output" }, description = "Output location where project will be generated")
	private String outputLocation;

	@Option(names = { "-n", "--name" }, description = "Name of the service")
	private String nameOfService;

	private List<Path> wsdlFiles = new ArrayList<>();
	
	public static final String TEXT_GREEN = "\u001B[32m";
	
	public static final String TEXT_BLUE = "\u001B[34m";

	public void run() {
		BufferedImage image = new BufferedImage(144, 32, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.drawString(String.join(" ", words), 6, 24);

        for (int y = 0; y < 32; y++) {
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < 144; x++)
                builder.append(image.getRGB(x, y) == -16777216 ? " " : image.getRGB(x, y) == -1 ? "#" : "*");
            if (builder.toString().trim().isEmpty()) continue;
            System.out.println(builder);
        }
        System.out.println(TEXT_BLUE+"***********************************************");
		System.out.println(TEXT_GREEN+"Generating Scaffolding for SOAP services....");
		System.out.println(TEXT_BLUE+"***********************************************");
		switch (serviceType) {
		case "soap":
			validateSoapServiceType();
			try {
				if (nameOfService == null) {
					nameOfService = "CXFSpring";
				}
				try (ProgressBar pb = new ProgressBar("Scaffolding SOAP services", 2000)) {
					SoapProcessor.process(wsdlFiles, nameOfService, null,pb);
				}
				System.out.println(TEXT_GREEN+"Scaffolding Done !!! ");
				//new Tree().print(outputLocation);
				System.out.println(TEXT_BLUE+"***********************************************");
				System.out.println(TEXT_GREEN+"Generated Files Structure ");
				System.out.println(TEXT_BLUE+"***********************************************");
				Thread.sleep(3000);
				new Tree().print(outputLocation);
			} catch (IOException  | InterruptedException e) {
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
		//System.out.println("==>" + wsdlLocation);
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
			//wsdlFiles.forEach(System.out::println);
		}
	}

	public static void main(String ar[]) {
		int exitCode = new CommandLine(new PKSI()).execute(ar);
		System.exit(exitCode);

	}

}