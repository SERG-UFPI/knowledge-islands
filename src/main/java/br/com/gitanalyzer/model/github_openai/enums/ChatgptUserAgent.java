package br.com.gitanalyzer.model.github_openai.enums;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatgptUserAgent {
	USER("user"), ASSISTANT("assistant");
	private String agent;

	public static List<String> getValuesArray(){
		List<String> array = new ArrayList<>();
		for (ChatgptUserAgent element : ChatgptUserAgent.values()) {
			array.add(element.getAgent());
		}
		return array;
	}

	public static ChatgptUserAgent getByAgent(String agent) {
		for (ChatgptUserAgent element : ChatgptUserAgent.values()) {
			if(element.getAgent().equals(agent)) {
				return element;
			}
		}
		return null;
	}
}
