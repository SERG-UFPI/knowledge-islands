package br.com.gitanalyzer.analysis;

public class TesteChineseCharac {
	public static String removeEnclosingQuotes(String text) {
        // Check if the string starts and ends with double quotes
        if (text.startsWith("\"") && text.endsWith("\"")) {
            // Remove the enclosing double quotes
            return text.substring(1, text.length() - 1);
        }
        return text; // Return as-is if not enclosed by double quotes
    }

    public static void main(String[] args) {
        String pathWithQuotes = "\"Programers/Day5/42626_\\354\\212\\244\\354\\275\\224\\353\\271\\214.java\"";
        String pathWithoutQuotes = removeEnclosingQuotes(pathWithQuotes);

        System.out.println("Original: " + pathWithQuotes);
        System.out.println("Without quotes: " + pathWithoutQuotes);
    }
}
