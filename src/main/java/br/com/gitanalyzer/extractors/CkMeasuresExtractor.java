//package br.com.gitanalyzer.extractors;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import com.github.mauricioaniche.ck.CK;
//import com.github.mauricioaniche.ck.CKClassResult;
//import com.github.mauricioaniche.ck.CKNotifier;
//
//import br.com.gitanalyzer.model.entity.QualityMeasures;
//
//public class CkMeasuresExtractor {
//
//	public QualityMeasures extract(String path) throws IOException {
//		boolean useJars = false;
//		int maxAtOnce = 0;
//		boolean variablesAndFields = false;
//
//		Map<String, CKClassResult> results = new HashMap<>();
//
//		new CK(useJars, maxAtOnce, variablesAndFields).calculate(path, new CKNotifier() {
//			@Override
//			public void notify(CKClassResult result) {
//				results.put(result.getClassName(), result);
//			}
//			@Override
//			public void notifyError(String sourceFilePath, Exception e) {
//				System.err.println("Error in " + sourceFilePath);
//				e.printStackTrace(System.err);
//			}
//		});
//
//		double cbo = 0, dit = 0, noc = 0, lcom = 0, rfc = 0, wmc = 0;
//		if(results.entrySet() != null && results.entrySet().size() > 0) {
//			for(Map.Entry<String, CKClassResult> entry : results.entrySet()){
//				cbo = cbo + entry.getValue().getCbo();
//				dit = dit + entry.getValue().getDit();
//				noc = noc + entry.getValue().getNoc();
//				lcom = lcom + entry.getValue().getLcom();
//				rfc = rfc + entry.getValue().getRfc();
//				wmc = wmc + entry.getValue().getWmc();
//			}
//		}
//		cbo = cbo/results.entrySet().size();
//		dit = dit/results.entrySet().size();
//		noc = noc/results.entrySet().size();
//		lcom = lcom/results.entrySet().size();
//		rfc = rfc/results.entrySet().size();
//		wmc = wmc/results.entrySet().size();
//		QualityMeasures qualityMeasures = new QualityMeasures(cbo, dit, noc, lcom, rfc, wmc);
//		return qualityMeasures;
//	}
//}
