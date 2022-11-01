package com.gavatron;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NewPrintProcesser {
	static ArrayList<String> values = new ArrayList<String>();
	static ArrayList<String> key = new ArrayList<>(Arrays.asList("First name: ", "Last name: "));
	static String filename;
	static String date;
	static boolean folder;
	static String fileLocation;

	public static void main(String[] args) throws IOException {
		String file = "";

		System.out.println(args[0]);
		for (int i = 0; i < args[0].length(); i++) {
			if (args[0].charAt(i) == '\\')
				file += "\\\\";
			else
				file += args[0].charAt(i);
		}

		JsonObject settings = readJsonFile("./path.json");
		String gc = settings.get("grabcad").getAsString();
		folder = new File(file).isDirectory();

		File gcFolder = new File(gc + "\\STLs" + "\\" + LocalDate.now().getMonth().toString());
		if (!gcFolder.exists()) {
			gcFolder.mkdirs();
		}

		date = new SimpleDateFormat().format(new Date()).toString();
		date = date.split("/")[0] + "/" + date.split("/")[1];
		for (int i = 0; i < date.length(); i++)
			if (date.charAt(i) == '/')
				date = date.split("/")[0] + "." + date.split("/")[1];

		if (folder)
			key.add("Project name: ");

		if (args.length == 1) {
			System.out.println("\nIf at any time you enter something incorrectly, just close this window.");
			getInputs();
		} else if (args.length == 3) {
			values.add(0, args[1]);
			values.add(1, args[2]);
		} else if (args.length == 4) {
			values.add(0, args[1]);
			values.add(1, args[2]);
			values.add(2, args[3]);
		}

		String open = "";

		if (!folder) {
			open = nameFile(file, gcFolder.toString(), true).toString();
		} else {
			Path dir = Paths.get(file);

			ArrayList<String> files = new ArrayList<String>();

			Files.walk(dir).forEach(path -> {
				if (path.toFile().isFile()) {
					try {
						files.add(nameFile(path.toString(), gcFolder.toString(), false));
					} catch (IOException e) {
						e.printStackTrace();
						e.printStackTrace();
					}
				}
			});

			for (String s : files)
				open += (s + " ");

//			Runtime.getRuntime().exec("explorer.exe \"" + fileLocation + "\"");
		}
		
		Runtime.getRuntime().exec(settings.get("ps-exe") + " " + open);
		
		System.out.println(settings.get("ps-exe") + " " + open);

		System.out.println("\n\n\nFiles have been copied to \"" + gcFolder + "\", and hopefully PrusaSlicer opened.");
//		Runtime.getRuntime().exec(settings.get("gc-exe").getAsString());
	}

	static String nameFile(String dir, String path, boolean show) throws IOException {
		path += "\\" + values.get(1) + ", ";
		path += values.get(0) + "\\";
		if (folder)
			path += values.get(2) + "\\";
		path += date;

		File projFolder = new File(path);
		if (!projFolder.exists())
			projFolder.mkdirs();

		File stl = new File(dir);
		String newName = path + "\\" + values.get(0) + " " + values.get(1) + " - " + stl.getName();

		fileLocation = path;

		if (show)
			Runtime.getRuntime().exec("explorer.exe /select,\"" + newName + "\"");

//		new File(dir).renameTo(new File(newName));
		System.out.println("Original: " + Paths.get(dir));
		System.out.println("New:" + Paths.get(newName));

		if (Paths.get(newName).toFile().exists())
			new File(newName).delete();

		Files.copy(Paths.get(dir), Paths.get(newName));

		System.out.println("fileLocation:::" + newName);
		System.out.println(Paths.get(newName));
		return "\"" + newName + "\"";
	}

	static void getInputs() {
		boolean correct = false;
		Scanner u = new Scanner(System.in);
		while (!correct) {
			for (int i = 0; i < key.size(); i++) {
				System.out.println(key.get(i));
				values.add(i, format(u.nextLine().trim()));
			}

			values.add(filename);
			System.out.print("\n\n\n");

			System.out.println("Is all of this correct?");

			for (int i = 0; i < key.size(); i++) {
				System.out.println(key.get(i) + values.get(i));
			}

			System.out.println("y/n");
			if (u.next().charAt(0) == 'y')
				correct = true;
			else {
				System.out.println("Restarting\n\n");
				values = new ArrayList<String>();
			}
		}
		u.close();
	}

	static String format(String f) {
		f = StringUtils.lowerCase(f);
		f = StringUtils.capitalize(f);

		return f;
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
			e.printStackTrace();
		}

		return file;
	}
}
