package br.com.gitanalyzer.utils;

public class DoaUtils {

	public double getDOA(int fa, int dl, int ac) {
		double faModel = KnowledgeIslandsUtils.faCoefDoa*fa;
		double dlModel = KnowledgeIslandsUtils.dlCoefDoa*dl;
		double acModel = KnowledgeIslandsUtils.acCoefDoa*Math.log(ac + 1);
		return KnowledgeIslandsUtils.interceptDoa + faModel + dlModel + acModel;
	}

}
