package hci.biominer.controller;

import hci.biominer.model.DashboardModel;
import hci.biominer.service.AnalysisService;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;

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

    @RequestMapping("/layout")
    public String getDashboardPartialPage() {
        return "resources/dashboard/layout";
    }
    
    @RequestMapping(value="/getCount", method=RequestMethod.POST)
    public @ResponseBody List<DashboardModel> getCount(@RequestParam(value="type") AnalysisTypeEnum type) {
    	return this.analysisService.getDashboard(type);
    }
    
}
