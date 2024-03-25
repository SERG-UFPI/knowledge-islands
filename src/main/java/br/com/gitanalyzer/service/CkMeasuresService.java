//package br.com.gitanalyzer.service;
//
//import java.io.IOException;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import br.com.gitanalyzer.extractors.CkMeasuresExtractor;
//import br.com.gitanalyzer.model.entity.Project;
//import br.com.gitanalyzer.model.entity.ProjectVersion;
//import br.com.gitanalyzer.model.entity.QualityMeasures;
//import br.com.gitanalyzer.repository.ProjectVersionRepository;
//
//@Service
//public class CkMeasuresService {
//	
//	@Autowired
//	private ProjectService projectService;
//	@Autowired
//	private ProjectVersionRepository projectVersionRepository;
//	private CkMeasuresExtractor extractor = new CkMeasuresExtractor();
//	
//	public void extractMeasures(String path) throws IOException {
//		extractor.extract(path);
//	}
//
//	public void setQualityLastVersion(String folderPath) throws IOException {
//		java.io.File dir = new java.io.File(folderPath);
//		for (java.io.File fileDir: dir.listFiles()) {
//			if (fileDir.isDirectory()) {
//				String projectPath = fileDir.getAbsolutePath()+"/";
//				Project project = projectService.returnProjectByPath(projectPath);
//				ProjectVersion projectVersion = projectVersionRepository.
//						findFirstByProjectIdOrderByDateVersionDesc(project.getId());
//				QualityMeasures qualityMeasures = extractor.extract(projectPath);
//				projectVersion.setMeanClassQualityMeasures(qualityMeasures);
//				projectVersionRepository.save(projectVersion);
//			}
//		}
//	}
//}
