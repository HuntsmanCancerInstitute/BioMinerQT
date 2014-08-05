package hci.biominer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import hci.biominer.model.QueryResult;
import hci.biominer.service.QueryService;


@Controller
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private QueryService queryService;

    @RequestMapping("/layout")
    public String getQueryPartialPage(ModelMap modelMap) {
        return "query/layout";
    }
    
    @RequestMapping(value = "run", method=RequestMethod.GET)
    @ResponseBody
    public List<QueryResult> run(
        @RequestParam(value="codeResultType") String codeResultType,
        @RequestParam(value="idOrganismBuild") Integer idOrganismBuild,
        @RequestParam(value="idAnalysisTypes") List<Integer> idAnalysisTypes,
        @RequestParam(value="idLabs") List<Integer> idLabs,
        @RequestParam(value="idProjects") List<Integer> idProjects,
        @RequestParam(value="idSampleSources") List<Integer> idSampleSources,
        @RequestParam(value="isIntersect") boolean isIntersect,
        @RequestParam(value="regions") String regions,
        @RequestParam(value="regionMargins") Integer regionMargins,
        @RequestParam(value="genes") String genes,
        @RequestParam(value="geneMargins") Integer geneMargins,
        @RequestParam(value="idGeneAnnotations") List<Integer> idGeneAnnotations,
        @RequestParam(value="isThresholdBasedQuery") boolean isThresholdBasedQuery,
        @RequestParam(value="FDR") String FDR,
        @RequestParam(value="codeFDRComparison") String codeFDRComparison,
        @RequestParam(value="log2Ratio") String log2Ratio,
        @RequestParam(value="codeLog2RatioComparison") String codeLog2RatioComparison
        ) {
      
       return queryService.runQuery(codeResultType,
           idOrganismBuild,
           idAnalysisTypes,
           idLabs,
           idProjects,
           idSampleSources,
           isIntersect,
           regions,
           regionMargins,
           genes,
           geneMargins,
           idGeneAnnotations,
           isThresholdBasedQuery,
           FDR,
           codeFDRComparison,
           log2Ratio,
           codeLog2RatioComparison);
    }
}
