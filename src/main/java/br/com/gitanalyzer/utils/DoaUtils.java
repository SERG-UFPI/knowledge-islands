package br.com.gitanalyzer.utils;

public class DoaUtils {

	public double getDOA(int fa, int dl, int ac) {
		double faModel = Constants.faCoefDoa*fa;
		double dlModel = Constants.dlCoefDoa*dl;
		double acModel = Constants.acCoefDoa*Math.log(ac + 1);
		return Constants.interceptDoa + faModel + dlModel + acModel;
	}

}
