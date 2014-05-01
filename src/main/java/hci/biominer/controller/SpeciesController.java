package hci.biominer.controller;

import hci.biominer.model.Species;
import hci.biominer.service.SpeciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * By: Tony Di Sera
 * Date: Apr 17, 2014
 */
@Controller
@RequestMapping("/species")
public class SpeciesController {

    @Autowired
    private SpeciesService speciesService;

    @RequestMapping("specieslist.json")
    public @ResponseBody List<Species> getSpeciesList() {
        return speciesService.getAllSpecies();
    }

    @RequestMapping(value = "/addSpecies", method = RequestMethod.POST)
    public @ResponseBody void addSpecies(@RequestBody Species species) {
        speciesService.addSpecies(species);
    }

    @RequestMapping(value = "/updateSpecies", method = RequestMethod.PUT)
    public @ResponseBody void updateSpecies(@RequestBody Species species) {
        speciesService.updateSpecies(species);
    }

    @RequestMapping(value = "/removeSpecies/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void removeSpecies(@PathVariable("id") Long id) {
        speciesService.deleteSpeciesById(id);
    }

    @RequestMapping(value = "/removeAllSpeciess", method = RequestMethod.DELETE)
    public @ResponseBody void removeAllSpeciess() {
        speciesService.deleteAll();
    }

    @RequestMapping("/layout")
    public String getSpeciesPartialPage(ModelMap modelMap) {
        return "species/layout";
    }
}
