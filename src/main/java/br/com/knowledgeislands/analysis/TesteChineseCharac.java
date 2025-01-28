package br.com.knowledgeislands.analysis;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import br.com.knowledgeislands.utils.KnowledgeIslandsUtils;

public class TesteChineseCharac {
	public static String decodeOctalString(String octalEncoded) {
		StringBuilder decodedString = new StringBuilder();
		int length = octalEncoded.length();

		for (int i = 0; i < length; i++) {
			char currentChar = octalEncoded.charAt(i);

			// Check for an octal sequence (e.g., "\101")
			if (currentChar == '\\' && i + 3 < length && isOctalDigit(octalEncoded.charAt(i + 1))
					&& isOctalDigit(octalEncoded.charAt(i + 2)) && isOctalDigit(octalEncoded.charAt(i + 3))) {

				// Extract the octal sequence (next 3 characters)
				String octalSequence = octalEncoded.substring(i + 1, i + 4);

				// Convert the octal sequence to an integer, then to a character
				int charCode = Integer.parseInt(octalSequence, 8);
				decodedString.append((char) charCode);

				// Skip the octal sequence
				i += 3;
			} else {
				// Append regular characters as is
				decodedString.append(currentChar);
			}
		}

		return new String(decodedString.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	}

	private static boolean isOctalDigit(char c) {
		return c >= '0' && c <= '7';
	}

	public static void main(String[] args) {
		String octalEncoded = "Exerc\\303\\255cios F\\303\\241ceis/ex02/index.html"; // represents "ABC" in octal
		String decoded = decodeOctalString(octalEncoded);
		System.out.println("Decoded string: " + decoded);
	}
}
