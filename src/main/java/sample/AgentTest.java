package sample;

import java.util.Random;

public class AgentTest {

	public static void main(String[] args) {
		final String emoji = "\\uDBC0\\uDC";
		String[] emojiList = { "78", "79", "7A", "7B", "7C", "7D", "7E", "7F", "8C", "8D", "8E", "8F", "91", "92", "93",
				"94", "95", "9A", "9B", "9C", "9D" };
		int emojiLength = emojiList.length;
		System.out.println(emojiLength);
		Random rnd = new Random();
		String retEmoji = "";
		for (int i = 0; i < 5; i++) {
			int val = rnd.nextInt(emojiLength);
			System.out.println(val);
			retEmoji += emoji + emojiList[val];
			System.out.println(emojiList[val]);
		}
		System.out.println(retEmoji);

		String text = "3 emoji";
		System.out.println(text.substring(0, text.indexOf(" emoji")));
		System.out.println("type");
		System.out.println(addQuotation("type", ":"));
	}
	
	private static String addQuotation(String instr, String addstr) {
		String outstr = "";
		outstr = "\"" + instr + "\"" + addstr;
		return outstr;
	}
}
