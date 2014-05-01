package hci.biominer.controller;

import hci.biominer.service.SubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 
 * By: Tony Di Sera
 * Date: Apr 17, 2014
 * 
 */
@Controller
@RequestMapping("/useradmin")
public class UserAdminController {

    @Autowired
    private SubmitService submitService;



    @RequestMapping("/layout")
    public String getSubmitPartialPage(ModelMap modelMap) {
        return "useradmin/layout";
    }
}
