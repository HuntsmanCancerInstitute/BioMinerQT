package hci.biominer.util;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.genome.Genome;
import hci.biominer.parser.GenomeParser;

import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class GenomeBuilds {
	private static HashMap<String,Genome> loadedGenomes = new HashMap<String,Genome>();
	
	public static void loadGenome(OrganismBuild ob) throws Exception {
    	if (ob.getGenomeFile() != null) {
    		Resource cpr = new ClassPathResource(ob.getGenomeFile());
			try {
				System.out.println("Loading file " + ob.getGenomeFile() + " for build " + ob.getName());
				GenomeParser gp = new GenomeParser(cpr.getFile());
				Genome genome = gp.getGenome();
				loadedGenomes.put(ob.getGenomeFile(), genome);
				

			} catch (Exception ex) {
				throw ex;
			}
    	} else {
    		throw new Exception("There is no genome file to load! " + ob.getName());
    	}
    }
	
	public static Genome getGenome(OrganismBuild ob) throws Exception {
		if (loadedGenomes.containsKey(ob.getGenomeFile())) {
			return loadedGenomes.get(ob.getGenomeFile());
		} else {
			throw new Exception("There is no genome file for the specified build! " + ob.getGenomeFile());
		}
	}
	
	public static boolean doesGenomeExist(OrganismBuild ob) throws Exception {
		if (loadedGenomes.containsKey(ob.getGenomeFile())) {
			return true;
		} else {
			return false;
		}
	}
 
    
    
    
    
    
    
	

}
