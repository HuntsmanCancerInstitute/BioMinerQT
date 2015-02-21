package hci.biominer.controller;

import hci.biominer.model.DashboardModel;
import hci.biominer.model.FileUpload;
import hci.biominer.service.AnalysisService;
import hci.biominer.service.DashboardService;
import hci.biominer.service.FileUploadService;
import hci.biominer.service.LabService;
import hci.biominer.service.UserService;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;
import hci.biominer.util.Enumerated.FileTypeEnum;
import hci.biominer.util.FloatModel;
import hci.biominer.util.LongModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 
 * By: Tony Di Sera
 * Date: Apr 17, 2014
 * 
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private LabService labService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private UserService userService;

    @RequestMapping("/layout")
    public String getDashboardPartialPage() {
        return "resources/dashboard/layout";
    }
    
    @RequestMapping(value="/getCount", method=RequestMethod.POST)
    public @ResponseBody List<DashboardModel> getCount(@RequestParam(value="type") AnalysisTypeEnum type) {
    	return this.analysisService.getDashboard(type);
    }

    @RequestMapping(value="/getUploadedSize", method=RequestMethod.GET)
    public @ResponseBody FloatModel getUploadSize() {
    	List<FileUpload> fl = fileUploadService.getAllFileUploads();
    	Float totalSize = (float)0.0;
    	for (FileUpload fu: fl) {
    		if (fu.getType().equals(FileTypeEnum.UPLOADED)) {
    			Long size = fu.getSize();
    			Float gbSize = (float)size/(float)1000000000;
    			totalSize += gbSize;
    		}
    	}
    	
    	FloatModel fm = new FloatModel();
    	fm.setFloat(totalSize);
    	return fm;
    }
    
    @RequestMapping(value="/getParsedSize", method=RequestMethod.GET)
    public @ResponseBody FloatModel getParsedSize() {
    	List<FileUpload> fl = fileUploadService.getAllFileUploads();
    	Float totalSize = (float)0.0;
    	for (FileUpload fu: fl) {
    		if (fu.getType().equals(FileTypeEnum.IMPORTED)) {
    			Long size = fu.getSize();
    			Float gbSize = (float)size/(float)1000000000;
    			totalSize += gbSize;
    		}
    	}
    	
    	FloatModel fm = new FloatModel();
    	fm.setFloat(totalSize);
    	return fm;
    }
    
    @RequestMapping(value="/getTotalUsers",method=RequestMethod.GET)
    public @ResponseBody LongModel getTotalUsers() {
    	LongModel lm = new LongModel();
    	lm.setLong((long)this.userService.getAllUsers().size());
    	return lm;
    }
    
    @RequestMapping(value="/getTotalLabs",method=RequestMethod.GET)
    public @ResponseBody LongModel getTotalLabs() {
    	LongModel lm = new LongModel();
    	lm.setLong((long)this.labService.getAllLabs().size());
    	return lm;
    }
    
    @RequestMapping(value="/getTotalAnalyses",method=RequestMethod.GET)
    public @ResponseBody LongModel getTotalAnalyses() {
    	LongModel lm = new LongModel();
    	lm.setLong((long)this.analysisService.getAllAnalyses().size());
    	return lm;
    }
    
    @RequestMapping(value="/getLastQueryDate",method=RequestMethod.GET)
    public @ResponseBody LongModel getLastQueryDate() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getQueryDate());
    	return lm;
    }
    
    @RequestMapping(value="/getLastSubmissionDate",method=RequestMethod.GET)
    public @ResponseBody LongModel getLastSubmissionDate() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getSubmissionDate());
    	return lm;
    }
    
    @RequestMapping(value="/getLastReportDate",method=RequestMethod.GET)
    public @ResponseBody LongModel getLastReportDate() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getLastReportDate());
    	return lm;
    }
    
    @RequestMapping(value="/getLastCrashDate",method=RequestMethod.GET)
    public @ResponseBody LongModel getLastCrashDate() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getLastCrashDate());
    	return lm;
    }
    
    @RequestMapping(value="/getQueryCount",method=RequestMethod.GET)
    public @ResponseBody LongModel getQueryCount() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getQueryCount());
    	return lm;
    }
    
    @RequestMapping(value="/getIgvCount",method=RequestMethod.GET)
    public @ResponseBody LongModel getIgvCount() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getIgvCount());
    	return lm;
    }
    
    @RequestMapping(value="/getLoginCount",method=RequestMethod.GET)
    public @ResponseBody LongModel getLoginCount() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getLoginCount());
    	return lm;
    }
    
    @RequestMapping(value="/getCrashCount",method=RequestMethod.GET)
    public @ResponseBody LongModel getCrashCount() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getCrashCount());
    	return lm;
    }
    
    @RequestMapping(value="/getReportCount",method=RequestMethod.GET)
    public @ResponseBody LongModel getReportCount() {
    	LongModel lm = new LongModel();
    	lm.setLong(this.dashboardService.getReportCount());
    	return lm;
    }
    
    
    
}
