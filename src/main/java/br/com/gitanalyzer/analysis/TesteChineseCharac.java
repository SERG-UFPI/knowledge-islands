package br.com.gitanalyzer.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

public class TesteChineseCharac {
	public static void main(String[] args) {
//        String text = "vivekhebs@gmail.comhuggingface-cli login --token \"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\""
//                    + "wandb login 42f92cd30e98dd827d409693246504bc33a15ca4git config --global user.name \"VH-abc\""
//                    + "git config --global user.name \"VH-abc\"git config --global user.email vivekhebs@gmail.com";
//
//        // Regular expression to match email addresses
//        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,7}";
//        Pattern pattern = Pattern.compile(emailRegex);
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//            System.out.println("Found email: " + matcher.group());
//        }
        
        
        Pattern pattern = Pattern.compile(KnowledgeIslandsUtils.regexOpenAiRegexChatGPT);
        Matcher matcher = pattern.matcher("https://chatgpt.com/share/6730ac9a-1a80-8005-bbab-520e69f239f0");
        matchWhile:while(matcher.find()) {
        	System.out.println(matcher.group());
        }
    }
}
