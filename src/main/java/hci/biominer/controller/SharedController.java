package hci.biominer.controller;

import hci.biominer.model.AnalysisType;
import hci.biominer.model.GeneAnnotation;
import hci.biominer.model.Genotype;
import hci.biominer.model.Organism;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.SampleCondition;
import hci.biominer.model.SamplePrep;
import hci.biominer.model.SampleSource;
import hci.biominer.model.SampleType;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.User;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.GeneAnnotationService;
import hci.biominer.service.GenotypeService;
import hci.biominer.service.InstituteService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.OrganismService;
import hci.biominer.service.SampleConditionService;
import hci.biominer.service.SamplePrepService;
import hci.biominer.service.SampleSourceService;
import hci.biominer.service.SampleTypeService;
import hci.biominer.util.MailUtil;

import java.util.List;
import java.util.Properties;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("shared")
public class SharedController {
	
	@Autowired
	private OrganismBuildService organismBuildService;
	
	@Autowired
	private OrganismService organismService;
	
	@Autowired
	private AnalysisTypeService analysisTypeService;
	
	@Autowired
	private SampleTypeService sampleTypeService;
	
	@Autowired
	private SamplePrepService samplePrepService;
	
	@Autowired
	private SampleConditionService sampleConditionService;
	
	@Autowired
	private SampleSourceService sampleSourceService;
	
	@Autowired
	private InstituteService instituteService;
	 
	@Autowired
	private GenotypeService genotypeService;
  
    @Autowired
    private GeneAnnotationService geneAnnotationService;


	@RequestMapping(value="getAllInstitutes",method=RequestMethod.GET)
	@ResponseBody
	public List<Institute> getInstituteList() {
		return instituteService.getAllInstitutes();
	}
	
	@RequestMapping(value="getAllOrganisms",method=RequestMethod.GET)
	@ResponseBody
	public List<Organism> getAllOrganisms() {
		return organismService.getAllOrganisms();
	}
	
	@RequestMapping(value="getAllBuilds",method=RequestMethod.GET)
	@ResponseBody
	public List<OrganismBuild> getAllBuilds() {
		return organismBuildService.getAllOrganismBuilds();
	}
	
	@RequestMapping(value="getBuildByOrganism",method=RequestMethod.POST)
	@ResponseBody
	public List<OrganismBuild> getBuildByOrganism(@RequestParam(value="idOrganism") Long idOrganism) {
		Organism organism = this.organismService.getOrganism(idOrganism);
		return organismBuildService.getOrganismBuildByOrganism(organism);
	}
	
	@RequestMapping(value="getAllSampleTypes",method=RequestMethod.GET)
	@ResponseBody
	public List<SampleType> getAllSampleTypesTypes() {
		return sampleTypeService.getAllSampleTypes();
	}
	
	@RequestMapping(value="getAllAnalysisTypes",method=RequestMethod.GET)
	@ResponseBody
	public List<AnalysisType> getAllAnalysisTypes() {
		return analysisTypeService.getAllAnalysisTypes();
	}
	
	@RequestMapping(value="getAllSamplePreps",method=RequestMethod.GET)
	@ResponseBody
	public List<SamplePrep> getAllSamplePreps() {
		return samplePrepService.getAllSamplePreps();
	}
	
	@RequestMapping(value="getSamplePrepsBySampleType",method=RequestMethod.GET)
	@ResponseBody
	public List<SamplePrep> getSamplePrepsBySampleType(@RequestParam(value="idSampleType") Long idSampleType) {
		return samplePrepService.getSamplePrepBySampleType(idSampleType);
	}
	
	@RequestMapping(value="getAllSampleSources",method=RequestMethod.GET)
	@ResponseBody
	public List<SampleSource> getAllSampleSources() {
		return sampleSourceService.getAllSampleSources();
	}
	
	@RequestMapping(value="getAllSampleConditions",method=RequestMethod.GET)
	@ResponseBody
	public List<SampleCondition> getAllSampleConditions() {
		return sampleConditionService.getAllSampleConditions();
	}
	
	 
	@RequestMapping(value="getAllGenotypes",method=RequestMethod.GET)
	@ResponseBody
	public List<Genotype> getAllGenotypes() {
	  return genotypeService.getAllGenotypes();
	}
	
	  
	  
	@RequestMapping(value="getAllGeneAnnotations",method=RequestMethod.GET)
	@ResponseBody
	public List<GeneAnnotation> getAllGeneAnnotations() {
	  return geneAnnotationService.getAllGeneAnnotations();
	}

	@RequestMapping(value="/reportissue",method=RequestMethod.POST)
	public @ResponseBody String reportissue(@RequestParam(value="email") String useremail, @RequestParam(value="problem") String problem)  {
		String result = null;
	
		//System.out.println ("[/reportissue] email is " + useremail);

		if (useremail == null || useremail.equals("")) {
			result = "Invalid email address.  Enter a vaild email address.";
			return result;
		}
		
		if (problem == null || problem.equals("")) {
			result = "Request ignored, no issue reported.";
			return result;
		}
		
		String biominersupport = "Tim.Maness@hci.utah.edu";
		
		// send the email
		result = "Thank you for your feedback.";
			            		
		String body = problem; 
		Date today = new Date();
		String subject = "BioMiner issue reported " + today;
			
		String [] emails = new String[1];
		emails[0] = biominersupport;
		String status = MailUtil.sendMail(useremail,emails,body,subject);
		if (status != null) {
				result = "Unable to send problem report, error: " + status;
			}
		
		//System.out.println ("[/reportissue] returning result: " + result);
		return result;
	}

	
	@RequestMapping(value="sendMail",method=RequestMethod.POST) 
	@ResponseBody
	public void sendMail(@RequestParam(value="body") String body, @RequestParam(value="subject") String subject) throws MessagingException {
		String recipient = "tim.mosbruger@hci.utah.edu";
		String from = "biominer@hci.utah.edu";
		
		Properties properties = System.getProperties();
	
		properties.put("mail.smtp.host", "hci-mail.hci.utah.edu");
		Session session = Session.getDefaultInstance(properties);
		
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipient));
			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
		} catch (MessagingException mex) {
			throw mex;
		}
	}
	
	
	
}
