package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.GeneAnnotationDAO;
import hci.biominer.model.GeneAnnotation;


@Service("GeneAnnotationService")
@Transactional
public class GeneAnnotationService {
	@Autowired
	private GeneAnnotationDAO geneAnnotationDAO;
	
	public GeneAnnotation getGeneAnnotation(Long idGeneAnnotation) {
		return geneAnnotationDAO.getGeneAnnotationById(idGeneAnnotation);
	}
	
	public List<GeneAnnotation> getAllGeneAnnotations() {
		return geneAnnotationDAO.getGeneAnnotations();
	}
	
	
	public void addGeneAnnotation(GeneAnnotation geneAnnotation) {
		geneAnnotationDAO.addGeneAnnotation(geneAnnotation);		
		
	}

	public void deleteGeneAnnotationById(Long idGeneAnnotation) {
        geneAnnotationDAO.deleteGeneAnnotation(idGeneAnnotation);
		
	}

	public void updateGeneAnnotation(Long idGeneAnnotation, GeneAnnotation geneAnnotation) {
        geneAnnotationDAO.updateGeneAnnotation(idGeneAnnotation, geneAnnotation);
	}
	
	
	
}
