package com.gavatron;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CatcardLogger {
	static String fileDir = "./cards.json";
	static JsonObject file = readJsonFile(fileDir);
	static JsonObject members;
	static JsonArray mArray;
	static JsonObject status;
	static Scanner s = new Scanner(System.in);

	public static void main(String[] args) {
		members = file.get("members").getAsJsonObject();
		mArray = file.get("m-array").getAsJsonArray();
		String in = "";
		String number = "";

		while (!in.contains("exit")) {
			for(int i = 0; i < 50; i++)
				System.out.println();
			
			try {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			System.out.print("\n\n\nWaiting for CatCard scan: \n\n\n\n\n\n\n\n\n");

			while (!s.hasNext()) {
			}

			in = s.nextLine();
			number = trim(in);

			if (number.length() == 8) {
				JsonObject member;
				try { // Tries to get current values from json file
					member = file.get("members").getAsJsonObject().get(number).getAsJsonObject();
				} catch (Exception e) { // If the card doesn't exist, create it and then get it
					String generic = "{\"signed\":false,\"present\":false,\"visits\":0,\"warnings\":0,\"310\":false,\"trainings\":{\"woodshop\":false}}";
					members.add(number, JsonParser.parseString(generic));
					mArray.add(number);

					member = file.get("members").getAsJsonObject().get(number).getAsJsonObject();
					if (!member.get("signed").getAsBoolean() && in.contains("signed"))
						member.addProperty("signed", true);

					saveListToFile(file, fileDir);
				}
				Calendar c = Calendar.getInstance();
				String code = "" + c.get(Calendar.DAY_OF_MONTH);
				if (code.length() == 2)
					code = code.charAt(0) + " " + code.charAt(1);
				else
					code = code + " ";
				if (c.get(Calendar.HOUR_OF_DAY) % 12 == 0)
					code += 12;
				else
					code += c.get(Calendar.HOUR_OF_DAY) % 12;
				System.out.println(code);
				if (!in.contains(code))
					status(member, number);
				else
					edit(member, number);
			}

			System.out.println("Press any key to continue...");
			try {
				System.in.read();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		s.close();
	}

	private static void edit(JsonObject member, String number) {
		System.out.println("What needs to be edited?");
		System.out.println(
				"1 = Policies signed\n2 = 310 student\n3 = Woodshop training\n4 = Add warning\n5 = Set warnings\n6 = Form submission");
		int val = Integer.parseInt(eliProofing(new String[] { "1", "2", "3", "4", "5", "6" }));
		JsonObject edit = new JsonObject();
		switch (val) {
		case 1:
			member.addProperty("signed", !member.get("signed").getAsBoolean());
			System.out.println("\nSigning status set to: " + member.get("signed").getAsBoolean());
			edit.add(number, member);
			break;
		case 2:
			member.addProperty("310", !member.get("310").getAsBoolean());
			System.out.println("\n310 status set to: " + member.get("310").getAsBoolean());
			edit.add(number, member);
			break;
		case 3:
			JsonObject temp = new JsonObject();
			temp.addProperty("woodshop", !member.get("trainings").getAsJsonObject().get("woodshop").getAsBoolean());
			member.add("trainings", temp);
			System.out.println("\nWoodshop training set to: "
					+ member.get("trainings").getAsJsonObject().get("woodshop").getAsBoolean());
			edit.add(number, member);
			break;
		case 4:
			member.addProperty("warnings", member.get("warnings").getAsInt() + 1);
			System.out.println("\nWarnings set to: " + member.get("warnings").getAsInt());
			edit.add(number, member);
			break;
		case 5:
			System.out.print("\nEnter number of warnings: ");
			member.addProperty("warnings", Integer.parseInt(eliProofing(new String[] { "0", "1", "2", "3" })));
			System.out.println("\nWarnings set to: " + member.get("warnings").getAsInt());
			edit.add(number, member);
			break;
		case 6:
			edit.add(number, form(number, member));
		default:
			System.out.print("");
		}

		file.add("members", edit);
		saveListToFile(file, fileDir);
		System.out.println("New values saved.");
	}

	static JsonObject form(String number, JsonObject member) {
		System.out.println("\nFilling out forms for " + number);

		boolean correct = false;
		while (!correct) {
			System.out.print("\nFirst name: ");
			member.addProperty("first", format(s.next()));
			System.out.print("\nLast name: ");
			member.addProperty("last", format(s.next()));
			member.add("school-info", getMajor());
			System.out.print("\n\n\n");

			System.out.println("Is all of this correct?");

			System.out.println("First: " + member.get("first").getAsString());
			System.out.println("Last: " + member.get("last").getAsString());
			System.out.println("Major: " + member.get("school-info").getAsJsonObject().get("major").getAsString());
			System.out.print("Year: " + member.get("school-info").getAsJsonObject().get("year").getAsInt());

			System.out.println("\ny/n");
			if (s.next().charAt(0) == 'y')
				correct = true;
			else {
				System.out.println("Restarting\n\n");
			}
		}

		member.addProperty("signed", true);
		System.out
				.println("\nCongratulations, you can now use the Makerspace! There is additional training needed for\n"
						+ "using the woodshop. Talk to the Lab Director or a Lab Assistance for assistance.\n"
						+ "Please remember to sign in and out every time you visit.\n");
		return member;
	}

	static String format(String f) {
		f = StringUtils.lowerCase(f);
		f = StringUtils.capitalize(f);

		return f;
	}

	static void status(JsonObject values, String n) {
		if (values.get("signed").getAsBoolean()) {
			if (values.get("present").getAsBoolean()) {
				System.out.println("Exiting? (y/n)");
			} else {
				System.out.println("Entering? (y/n)");
			}

			if (s.next().charAt(0) == 'y') {
				values.addProperty("present", !values.get("present").getAsBoolean());
				if (values.get("present").getAsBoolean()) {
					values.addProperty("visits", values.get("visits").getAsInt() + 1);
					try {
						file.addProperty("total-visits", file.get("total-visits").getAsInt() + 1);
					} catch (Exception e) {
						file.addProperty("total-visits", 1);
					}
					try {
						file.addProperty("current-visitors", file.get("current-visitors").getAsInt() + 1);
					} catch (Exception e) {
						file.addProperty("current-visitors", 1);
					}
				} else {
					file.addProperty("current-visitors", file.get("current-visitors").getAsInt() - 1);
				}
			}

			members.add(n, values);
			saveListToFile(file, fileDir);
		} else {
			System.out.println(
					"You must fill out the Lab Usage and Lab Safety policies before using the Makerspace.\nPlease talk to the Lab Director or a Lab Assistant for assistance.");
		}
	}

	static String eliProofing(String[] valid) {
		String r = "";
		do {
			if (!r.isBlank())
				System.out.println("Invalid input. Please try again.");
			while (!s.hasNext()) {
			}
			r = s.next().trim();
		} while (!(Arrays.asList(valid).contains(r) & StringUtils.isNumeric(r)));
		return r;
	}

	static void saveListToFile(JsonObject json, String loc) {
		file.add("members", members);
		file.add("m-array", mArray);
		try {
			FileWriter w = new FileWriter(loc);
			w.write(json.toString());
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to write to file!");
		}
	}

	static String trim(String in) {
		String r;

		try {
			r = in.substring(in.indexOf(";") + 2, in.indexOf("?") - 2);
		} catch (Exception e) {
			if (in.length() >= 8)
				r = in.substring(in.length() - 8, in.length());
			else {
				System.out.println("Invalid CatCard number");
				return "";
			}
		}

		System.out.println("CatCard number: " + r + "\n");
		return r;
	}

	static String parseFile(String s) {
		String e = "";
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '\\')
				e += "\\\\";
			else
				e += s.charAt(i);
		}

		return e;
	}

	static JsonObject readJsonFile(String p) {
		JsonObject file = new JsonObject();

		try {
			String raw = "";
			Scanner s = new Scanner(new File(p));
			while (s.hasNext())
				raw += s.nextLine();
			s.close();
			file = JsonParser.parseString(parseFile(raw)).getAsJsonObject();
		} catch (Exception e) {
			file = JsonParser.parseString("{\"members\":{},\"m-array\":[]}").getAsJsonObject();
		}

		return file;
	}

	static JsonObject getMajor() {
		JsonObject j = new JsonObject();
		System.out.println("1 = College of Agriculture");
		System.out.println("2 = College of Arts and Architecture");
		System.out.println("3 = College of Business & Entrepreneurship");
		System.out.println("4 = College of Education, Health and Human Development");
		System.out.println("5 = College of Engineering");
		System.out.println("6 = College of Letters and Science");
		System.out.println("7 = College of Nursing");
		System.out.println("8 = Gallatin College\n");
		System.out.print("Select your college: ");

		Path file = Paths.get("./majors.json");

		String m = "";
		switch (Integer.parseInt(eliProofing(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" }))) {
		case 1:
			m = "agriculture";
			break;
		case 2:
			m = "arts-architecture";
			break;
		case 3:
			m = "business-entrepreneurship";
			break;
		case 4:
			m = "education-development";
			break;
		case 5:
			m = "engineering";
			break;
		case 6:
			m = "letters-science";
			break;
		case 7:
			m = "nursing";
			break;
		case 8:
			m = "gallatin";
			break;
		default:
			System.out.println("Bad input");
		}
		j.addProperty("college", m);

		System.out.println("\n\n");
		JsonArray majors = readJsonFile(file.toString()).get(m).getAsJsonArray();

		String[] mCount = new String[majors.size()];

		for (int i = 0; i < majors.size(); i++) {
			System.out.println(i + " = " + majors.get(i).getAsString());
			mCount[i] = "" + (1 + i);
		}

		System.out.print("\nSelect your major: ");
		j.addProperty("major", majors.get(Integer.parseInt(eliProofing(mCount))).getAsString());

		System.out.print("\nEnter your year as a number (ex. freshman = 1, senior = 4): ");
		j.addProperty("year", Integer.parseInt(eliProofing(new String[] { "1", "2", "3", "4" })));

		return j;
	}
}
