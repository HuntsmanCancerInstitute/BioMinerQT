package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.DataTrackDAO;
import hci.biominer.model.DataTrack;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;

@Service("DataTrackService")
@Transactional
public class DataTrackService {
	@Autowired
	private DataTrackDAO dataTrackDAO;
	
	public DataTrack getDataTrackById(Long idDataTrack) {
		return dataTrackDAO.getDataTrackById(idDataTrack);
	}
	
	public List<DataTrack> getAllDataTracks() {
		return dataTrackDAO.getDataTracks();
	}
	
	public List<DataTrack> getDataTrackByProject(Project project) {
		return dataTrackDAO.getDataTrackByProject(project);
	}
	
	public List<DataTrack> getDataTrackByAnalysis(Analysis analysis) {
		return dataTrackDAO.getDataTrackByAnalysis(analysis);
	}
 	
	public void addDataTrack(DataTrack dataTrack) {
		dataTrackDAO.addDataTrack(dataTrack);		
	}

	public void deleteDataTrackById(Long idDataTrack) {
        dataTrackDAO.deleteDataTrack(idDataTrack);
	}

	public void updateDataTrack(Long idDataTrack, DataTrack dataTrack) {
        dataTrackDAO.updateDataTrack(idDataTrack, dataTrack);
	}

}
