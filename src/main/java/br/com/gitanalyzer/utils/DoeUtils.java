package br.com.gitanalyzer.utils;

public class DoeUtils {

	public double getDOE(int adds, int fa, int numDays, int size) {
		double addsModel = Constants.addsCoefDoe*Math.log(adds + 1);
		double faModel = Constants.faCoefDoe*fa;
		double numDaysModel = Constants.numDaysCoefDoe*Math.log(numDays + 1);
		double sizeModel = Constants.sizeCoefDoe*Math.log(size);
		return Constants.interceptDoe + addsModel + faModel
				+ numDaysModel + sizeModel;
	}

}
