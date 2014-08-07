package hci.biominer.service;


import hci.biominer.model.QueryResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;



@Service("QueryService")
public class QueryService {
  
  public List<QueryResult> runQuery(String codeResultType, 
      Integer idOrganismBuild, 
      List<Integer> idAnalysisTypes,
      List<Integer> idLabs,
      List<Integer> idProjects,
      List<Integer> idAnalyses,
      List<Integer> idSampleSources,
      boolean        isIntersect,
      String         regions,
      Integer        regionMargins,
      String         genes,
      Integer        geneMargins,
      List<Integer>  idGeneAnnotations,
      boolean        isThresholdBasedQuery,
      String         FDR,
      String         codeFDRComparison,
      String         log2Ratio,
      String         codeLog2RatioComparison
      ) {
    ArrayList<QueryResult> queryResults = new ArrayList<QueryResult>();
    
    QueryResult result = new QueryResult();
    result.setProjectName("Cell 2012 Wamstad Alexander");
    result.setAnalysisType("ChIP Seq");
    result.setAnalysisName("Histone Mod RNA polymerase II");
    result.setAnalysisSummary("Global occupancy for histone modifications and RNA polymerase II");
    result.setSampleConditions("Mouse Embryonic - Stage ESC");
    result.setCoordinates("ch4:23454-23898");
    result.setFDR(new BigDecimal(.03));
    result.setLog2Ratio(new BigDecimal(1.5));
    queryResults.add(result);
    
    result = new QueryResult();
    result.setProjectName("Cell 2012 Wamstad Alexander");
    result.setAnalysisName("Histone Mod RNA polymerase II");
    result.setAnalysisType("ChIP Seq");
    result.setAnalysisSummary("Global occupancy for histone modifications and RNA polymerase II");
    result.setSampleConditions("Mouse Embryonic - Stage ESC");
    result.setCoordinates("ch7:45666-66888");
    result.setFDR(new BigDecimal(.03));
    result.setLog2Ratio(new BigDecimal(1.6));
    queryResults.add(result);

    
    result = new QueryResult();
    result.setProjectName("Cell 2012 Wamstad Alexander");
    result.setAnalysisName("Histone Mod RNA polymerase II");
    result.setAnalysisType("ChIP Seq");
    result.setAnalysisSummary("Global occupancy for histone modifications and RNA polymerase II");
    result.setSampleConditions("Mouse Embryonic - Stage MES");
    result.setCoordinates("ch5:6700-10788");
    result.setFDR(new BigDecimal(.05));
    result.setLog2Ratio(new BigDecimal(1.8));
    queryResults.add(result);
    
    return queryResults;
    
  }
 
}
