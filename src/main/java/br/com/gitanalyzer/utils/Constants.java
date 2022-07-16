package br.com.gitanalyzer.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

	public static String absPath = "/home/otavio/Desktop/GitAnalyzer/projetos/ihealth/";

	public static List<String> invalidPaths = Arrays.asList(new String[]{"/dev/null"});

	/**
	 * DOE coefficients
	 */
	public static final double interceptDoe = 5.28223;
	public static final double addsCoefDoe = 0.23173;
	public static final double faCoefDoe = 0.36151;
	public static final double numDaysCoefDoe = -0.19421;
	public static final double sizeCoefDoe = -0.28761;

	/**
	 * DOA coefficients
	 */
	public static final double interceptDoa = 3.293;
	public static final double faCoefDoa = 1.098;
	public static final double dlCoefDoa = 0.164;
	public static final double acCoefDoa = -0.321;

	public static final String ADD = new String("ADD");
	public static final String MODIFY = new String("MODIFY");
	public static final String DELETE = new String("DELETE");
	public static final String RENAME = new String ("RENAME");

	public static final String DOT = new String(".");
	public static final String LESS_THEN = new String("<");
	public static final String BIGGER_THEN = new String(">");
	public static final String OPEN_BRACKET = new String("[");
	public static final String FILE_SEPARATOR = new String("/");
	public static final String NUMBER_SIGN = new String("#");
	public static final String COMMA = new String(",");
	public static final String WHITESPACE = new String(" ");
	public static final String CLOSE_BRACKET = new String("]");
	public static final String NEW = new String("new");
	public static final String OPEN_PARENTHESE = new String("(");
	public static final String EMPTY = new String("");
	public static final String COLON = new String(":");

	public static final String JAVA_EXTENSION = new String(".java");
	public static final String TAG_EXTENSION = new String(".tag");

	public static final String TAB = new String("\t");
	public static final String BREAK_LINE = new String("\n");

	public static final String OPEN_TAG_LIB = new String("<%@");
	public static final String INCLUDE = new String("include");
	public static final String TAGLIB = new String("taglib");
	public static final String TAGDIR = new String("tagdir");
	public static final String PREFIX = new String("prefix");
	public static final String ATTRIBUTE = new String("attribute");
	public static final String TYPE = new String("type");

	public static final String linguistFileName = new String("linguistfiles.log");
	public static final String clocFileName = new String("cloc_info.log");
	public static final String allFilesFileName = new String("filelist.log");

	public static final String truckFactorResultFile = new String("truck_factor.log");
	public static final String truckFactorHistoryFile = new String("truckFactorHistoryFile.log");
	public static final String developersProjectFileName = new String("truck_factor_devs.log");

	public static final double normalizedThresholdMantainerDOA = 0.75;
	public static final double normalizedThresholdMantainerDOE = 0.7;
	public static final double thresholdMantainerDOA = 3.293;

	public static final int quantKnowledgedDevsByFile = 3;

	public static final Map<String, String> tagsPathIHealth = new HashMap<String, String>(){{
		put("infoway:", "template/WEB-INF/tags/");
	}};

	public static Date thresholdDateDisable(Date currentCommitDate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(currentCommitDate); 
		c.add(Calendar.MONTH, -3);
		return c.getTime();
	}

	public static Date thresholdDateSquadTouchedFiles(Date currentCommitDate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(currentCommitDate); 
		c.add(Calendar.MONTH, -3);
		return c.getTime();
	}
}
