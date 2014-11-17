package hci.biominer.util;

import hci.biominer.controller.FileController;
import hci.biominer.model.FileUpload;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.genome.Genome;
import hci.biominer.parser.GenomeParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

public class GenomeBuilds {
	private static HashMap<String,Genome> loadedGenomes = new HashMap<String,Genome>();
	
	public static void loadGenome(OrganismBuild ob) throws Exception {
		//Deploy any available class resources
		deployResources();
		
		//Load genome file
    	if (ob.getGenomeFile() != null) {
    		File genomeFile = new File(FileController.getGenomeDirectory(),ob.getGenomeFile());
    		File transFile = new File(FileController.getGenomeDirectory(),ob.getTranscriptFile());
    		//Resource cpr = new ClassPathResource(ob.getGenomeFile());
			try {
				System.out.println("Loading file " + ob.getGenomeFile() + " for build " + ob.getName());
				GenomeParser gp = new GenomeParser(genomeFile, transFile);
				Genome genome = gp.getGenome();
				loadedGenomes.put(ob.getGenomeFile(), genome);
				

			} catch (Exception ex) {
				throw ex;
			}
    	} else {
    		throw new Exception("There is no genome file to load! " + ob.getName());
    	}
    }
	
	public static void deployResources() throws Exception {
		PathMatchingResourcePatternResolver rr = new PathMatchingResourcePatternResolver();
		Resource[] resources = rr.getResources("classpath:genomes/**");
		for (Resource r: resources) {
			String resourceName = r.getFilename();
			File localName = new File(FileController.getGenomeDirectory(),resourceName);
			if (!localName.exists()) {
				FileCopyUtils.copy(new FileInputStream(r.getFile()), new FileOutputStream(localName));
			}
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
	
	public static void removeGenome(OrganismBuild ob) {
		if (loadedGenomes.containsKey(ob.getGenomeFile())) {
			loadedGenomes.remove(ob.getGenomeFile());
		}
	}
	
	public static Genome fetchGenome(OrganismBuild ob) throws Exception {
    	try {
    		if (!GenomeBuilds.doesGenomeExist(ob)) {
        		GenomeBuilds.loadGenome(ob);
        	} 
        	return GenomeBuilds.getGenome(ob);
    	} catch (Exception ex) {
    		throw ex;
    	}
    }
 
    
    
    
    
    
    
	

}
