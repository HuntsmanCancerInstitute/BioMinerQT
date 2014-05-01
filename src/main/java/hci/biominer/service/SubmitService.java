package hci.biominer.service;

import hci.biominer.dao.SpeciesDAO;

import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

	
/**
 * 
 * By: Tony Di Sera
 * Date: Apr 17, 2014
 * 
 */
@Service("submitService")
@Transactional
public class SubmitService {
	
	@Autowired
	private SpeciesDAO speciesDAO;

    
	
}
