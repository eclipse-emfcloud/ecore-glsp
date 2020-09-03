package org.eclipse.emfcloud.ecore.codegen;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class GenmodelGenerator {
	
	private static List<String> validArgs;
	private static final String ARG_JAR = "-launcher";
	private static final String ARG_ECORE = "-ecore";
	private static final String ARG_WORKSPACE = "-workspace";
	
	static {
		validArgs = new ArrayList<String>();
		validArgs.add(ARG_JAR);
		validArgs.add(ARG_ECORE);
		validArgs.add(ARG_WORKSPACE);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Map<String, String> data = new HashMap<String, String>();	
		
		if (args.length < 2) {
			printArgs();
			return;
		}
		
		String lastArg = null;
		for (String arg : args) {
			if (validArgs.contains(arg.toLowerCase())) {
				if (lastArg != null) {
					// System.exit(-10);
					throw new RuntimeException("Missing value for argument " + lastArg);
				}
				lastArg = arg.toLowerCase();
				continue;
			}
			
			if (lastArg != null) {
				data.put(lastArg, arg);
				lastArg = null;
			} else {
				printArgs();
				throw new RuntimeException("Argument " + arg + " not recognized");
			}
		}
		
		createGenModel(data);
	}
	
	private static void printArgs() {
		System.out.println("Required arguments are");
		for (String string : validArgs) {
			System.out.println("  " + string);
		}
	}
	
	private static void createGenModel(Map<String, String> data) throws IOException, InterruptedException {
		if (data.get(ARG_JAR) == null) {
			throw new RuntimeException("Eclipse launcher jar path not specified, use " + ARG_JAR + " to specify the path to the .jar file");
		}
		if (data.get(ARG_ECORE) == null) {
			throw new RuntimeException("Ecore file path not specified, use argument " + ARG_ECORE + " to specify the path to the .ecore file");
		}
		if (data.get(ARG_WORKSPACE) == null) {
			throw new RuntimeException("Workspace path not specified, use argument " + ARG_ECORE + " to specify the path to the .ecore file");
		}
		File ecoreFile = new File(data.get(ARG_ECORE));
		if (!ecoreFile.exists()) {
			throw new RuntimeException("Ecore file not found: " + ecoreFile.getAbsolutePath());
		}
	    
		System.out.println("Opening ecore file: " + ecoreFile.getAbsolutePath());
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		URI uri =  URI.createFileURI(ecoreFile.getAbsolutePath());
		Resource ecorePackageResource = resourceSet.getResource(uri, true);
		EPackage rootPackage = (EPackage) ecorePackageResource.getContents().get(0);
		
		// Instead of calling eclipse to generate the genmodel, it can be create manually if more customization is required
		// For reference the following class is called to generate the genmodel:
		// \org.eclipse.emf\plugins\org.eclipse.emf.codegen.ecore\src\org\eclipse\emf\codegen\ecore\Generator.java
				
		String[] args = new String[] {"java", "-cp", data.get(ARG_JAR),
			"org.eclipse.equinox.launcher.Main",
			"-data", data.get(ARG_WORKSPACE),
			"-application", "org.eclipse.emf.codegen.ecore.Generator",
			"-ecore2GenModel", ecoreFile.getAbsolutePath(),
			rootPackage.getName(), rootPackage.getNsPrefix()};
		
		Process process = new ProcessBuilder(args).start();
		
		process.waitFor();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ( (line = reader.readLine()) != null) {
		   builder.append(line);
		   builder.append(System.getProperty("line.separator"));
		}
		
		reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ( (line = reader.readLine()) != null) {
		   builder.append(line);
		   builder.append(System.getProperty("line.separator"));
		}
		
		if (process.exitValue() != 0) {
			throw new RuntimeException(builder.toString());
		} else {
			System.out.println(builder.toString());
		}
		
		System.exit(0);
	}
}
