package hci.biominer.util;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.genome.Genome;
import hci.biominer.parser.GenomeParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BiominerProperties {
	private static HashMap<String,String> biominerProperties = new HashMap<String,String>(); 
	private static boolean loaded = false;
	
	public static boolean isLoaded() {
		return loaded;
	}
	
	public static void loadProperties() throws Exception {
		Resource cpr = new ClassPathResource("biominerProperties.ini");
		try {
			BufferedReader br = new BufferedReader(new FileReader(cpr.getFile()));
			String temp = null;
			while((temp=br.readLine()) != null) {
				String[] tokens = temp.split("=");
				biominerProperties.put(tokens[0], tokens[1]);
			}
			loaded = true;
		} catch (FileNotFoundException e) {
			throw new Exception("Can't find the biominer properties file!!");
		} catch (IOException e) {
			throw new Exception("Can't read the biominer properties file!!");
		}
	}
	
	public static String getProperty(String propertyName) throws Exception{
		if (biominerProperties.containsKey(propertyName)) {
			return biominerProperties.get(propertyName);
		} else {
			throw new Exception(String.format("Property %s is missing from properties file!",propertyName));
		}
	}
	
}
