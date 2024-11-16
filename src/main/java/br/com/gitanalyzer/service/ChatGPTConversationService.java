package br.com.gitanalyzer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gitanalyzer.exceptions.PageJsonProcessingException;
import br.com.gitanalyzer.model.entity.ChatGptConversation;
import br.com.gitanalyzer.model.entity.ConversationTurn;
import br.com.gitanalyzer.model.entity.PromptCode;
import br.com.gitanalyzer.model.enums.ChatgptUserAgent;
import br.com.gitanalyzer.utils.KnowledgeIslandsUtils;

@Service
public class ChatGPTConversationService {

	public List<String> getLinesCopiedAndRemoveFromAddedLines(List<String> addedLines, List<String> code) {
		List<String> linesCopied = new ArrayList<>();
		Iterator<String> iteratorAddedLines = addedLines.iterator();
		while(iteratorAddedLines.hasNext()) {
			String addedLine = iteratorAddedLines.next();
			if(!addedLine.isBlank()) {
				for(String lineCode : code) {
					if(!lineCode.isBlank() && 
							lineCode.toLowerCase().trim().equals(addedLine.toLowerCase().trim())) {
						linesCopied.add(addedLine);
						iteratorAddedLines.remove();
						break;
					}
				}
			}
		}
		return linesCopied;
	}

	public List<String> getCodesFromConversation(List<ConversationTurn> conversation){
		List<String> codes = new ArrayList<>();
		for (ConversationTurn turn : conversation) {
			if(turn.getUserAgent().equals(ChatgptUserAgent.ASSISTANT) && turn.getCodes() != null
					&& !turn.getCodes().isEmpty()) {
				for(PromptCode code: turn.getCodes()) {
					List<String> lines = Arrays.asList(code.getCode().split("\n"));
					for (String line : lines) {
						if(!line.isEmpty() && !line.isBlank()) {
							line = line.trim();
							codes.add(line);
						}
					}
				}
			}
		}
		return codes;
	}

	public ChatGptConversation getConversationOfOpenAiJson(String json) throws PageJsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		String createTimeLabel = "create_time";
		String updateTimeLabel = "update_time";
		try {
			JsonNode rootNode = objectMapper.readTree(json);
			JsonNode dataNode = rootNode.path("state").path("loaderData").path("routes/share.$shareId.($action)").path("serverResponse").path("data");
			if(!dataNode.isMissingNode()) {
				ChatGptConversation conversation = new ChatGptConversation();
				Date createTime = new java.util.Date(dataNode.get(createTimeLabel)!=null?dataNode.get(createTimeLabel).asLong()*1000:null);
				conversation.setCreateTime(createTime);
				Date updateTime = new java.util.Date(dataNode.get(updateTimeLabel)!=null?dataNode.get(updateTimeLabel).asLong()*1000:null);
				conversation.setUpdateTime(updateTime);
				conversation.setTitle(dataNode.get("title")!=null?dataNode.get("title").asText():"");
				if(dataNode != null) {
					JsonNode conversationsNode = dataNode.get("linear_conversation");
					for (JsonNode node : conversationsNode) {
						JsonNode messageNode = node.get("message");
						if(messageNode != null) {
							JsonNode authorNode = messageNode.get("author");
							if(authorNode != null && ChatgptUserAgent.getValuesArray().contains(authorNode.get("role").asText())) {
								ConversationTurn conversationTurn = new ConversationTurn();
								String agent = authorNode.get("role").asText();
								ChatgptUserAgent userAgent = ChatgptUserAgent.getByAgent(agent);
								conversationTurn.setUserAgent(userAgent);
								Long promptCreateTime = messageNode.get(createTimeLabel).asLong();
								conversationTurn.setCreateTime(promptCreateTime);
								JsonNode contentParts = messageNode.get("content").get("parts");
								if(contentParts != null) {
									for (JsonNode content : contentParts) {
										String fullContent = content.asText();
										List<PromptCode> codes = null;
										if(agent.equals(ChatgptUserAgent.ASSISTANT.getAgent()) && fullContent.contains(KnowledgeIslandsUtils.openAiCodeJsonDelimiter)) {
											int delimiterCount = fullContent.split(KnowledgeIslandsUtils.openAiCodeJsonDelimiter, -1).length-1;
											if(delimiterCount % 2 == 0) {
												codes = getCodesFromOpenAiJson(fullContent);
												for (PromptCode promptCode : codes) {
													fullContent = fullContent.replace(promptCode.getCodeFullText(), promptCode.getCodeId());
													checkLanguageEmpty(promptCode);
												}
												conversationTurn.setCodes(codes);
											}
										}
										conversationTurn.setFullText(fullContent);
									}
									conversation.addConversationTurn(conversationTurn);
								}
							}
						}
					}
				}
				return conversation;
			}else {
				throw new PageJsonProcessingException("Error on server response");
			}
		}catch(JsonProcessingException e) {
			e.printStackTrace();
			throw new PageJsonProcessingException(e.getMessage());
		}
	}

	private void checkLanguageEmpty(PromptCode promptCode) {
		if(promptCode.getLanguage() == null || promptCode.getLanguage().isEmpty()) {
			if(promptCode.getCode().contains("javascript")) {
				promptCode.setLanguage("javascript");
			}else if(promptCode.getCode().matches(".*\\bgo\\b.*")) {
				promptCode.setLanguage("go");
			}
		}
	}

	private List<PromptCode> getCodesFromOpenAiJson(String fullContent) {
		List<PromptCode> promptCode = new ArrayList<>();
		Pattern r = Pattern.compile(KnowledgeIslandsUtils.openAiCodeJsonDelimiter+".*?"+KnowledgeIslandsUtils.openAiCodeJsonDelimiter, 
				Pattern.DOTALL);
		Matcher m = r.matcher(fullContent);
		int codeId = 0;
		while (m.find()) {
			String codeText = m.group(0).trim();
			String codeBlock = codeText.replace(KnowledgeIslandsUtils.openAiCodeJsonDelimiter, "");
			String[] words = codeBlock.split("\\s+");
			String language = words.length > 0 ? words[0].trim():"";
			int firstNewlineIndex = codeBlock.indexOf("\n");
			int lastNewlineIndex = codeBlock.lastIndexOf("\n");
			if(firstNewlineIndex != -1 && lastNewlineIndex != -1) {
				codeBlock = codeBlock.substring(firstNewlineIndex+1, lastNewlineIndex);
				codeBlock = codeBlock.trim();
				promptCode.add(new PromptCode(language, codeText, codeBlock, PromptCode.startOfCodeBlockId()+codeId));
				codeId++;
			}
		}
		return promptCode;
	}
}
