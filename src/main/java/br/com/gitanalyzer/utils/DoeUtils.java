package br.com.gitanalyzer.utils;

public class DoeUtils {

	public double getDOE(int adds, int fa, int numDays, int size) {
		double addsModel = KnowledgeIslandsUtils.addsCoefDoe*Math.log(adds + 1);
		double faModel = KnowledgeIslandsUtils.faCoefDoe*fa;
		double numDaysModel = KnowledgeIslandsUtils.numDaysCoefDoe*Math.log(numDays + 1);
		double sizeModel = KnowledgeIslandsUtils.sizeCoefDoe*Math.log(size);
		return KnowledgeIslandsUtils.interceptDoe + addsModel + faModel
				+ numDaysModel + sizeModel;
	}

}
