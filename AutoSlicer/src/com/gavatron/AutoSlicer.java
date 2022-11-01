package com.gavatron;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;
import java.awt.Desktop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AutoSlicer {
	static String dir;
	static String tw;
	static String ps;
	static JsonObject settings;
	static String error = "";
	static Scanner in = new Scanner(System.in);

	public static void main(String[] args) {
		settings = readJsonFile("./path.json");
		tw = settings.get("tw-py").getAsString();
		ps = settings.get("ps-exe").getAsString();

		File n = new File("./temp/");
		n.mkdir();

		dir = args[0];
		if (new File(dir).isDirectory()) {
			ArrayList<String> files = new ArrayList<String>();

			for (File s : new File(dir).listFiles()) {
				files.add(s.toString());
			}

			files.parallelStream().forEach(AutoSlicer::slice);
		} else {
			slice(dir);
		}

		System.out.println("\n\n\nPlease check the following files.");
		System.out.println("(Gcode will open in new window. Check it, close it, then enter y/n to enter into queue or delete)");
		System.out.println("Press enter to begin...");

		try {
			System.in.read();
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		}
		
		in.nextLine();

		try {
			try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + "/temp/"))) {
				paths.filter(Files::isRegularFile).forEach(AutoSlicer::check);
			}
		} catch (Exception e) {
		}

		if (!error.equals("")) {
			System.out.println("\n\n\n\n\n\n\nError(s) occurred. See below, and/or tell Gavin.");
			System.out.println(error);
		}
		
		n.delete();

		in.close();
	}

	public static void check(Path p) {
		if (!p.toString().contains(".stl")) {
			Desktop desktop = Desktop.getDesktop();

			try {
				desktop.open(p.toFile());

				System.out.println("y/n");

				if (in.nextLine().charAt(0) == 'y') {
					runCMD("C: && cd " + settings.get("queue").getAsString() + " && java -jar PrintQueueManager.jar \"" + p + "\"");
					
					if (p.toFile().exists())
						System.out.println("Unable to move file " + p.getFileName() + ". Duplicate?");
					else
						System.out.println("File " + p.getFileName() + " added to queue.");
					
					
					
				} else {
					p.toFile().delete();
					System.out.println("File " + p.getFileName() + " deleted.");
				}
			} catch (Exception e) {
				System.err.println(e.getStackTrace());
			}
		} else {
			p.toFile().delete();
		}
	}

	public static void slice(String slice) {
		File f = new File(System.getProperty("user.dir") + "/temp/" + new File(slice).getName());
		runCMD("\"python \"" + tw + "\" -min sur -x -i \"" + slice + "\" -o \"" + f + "\"\"");

		ArrayList<String> lines = getCMD("\"\"" + ps + "\" --info \"" + f + "\"\"");
		ArrayList<String> size = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i++) {
			String dim = lines.get(i);
			if (dim.contains("size_"))
				size.add(dim.substring(dim.lastIndexOf(' ') + 1, dim.length()).trim());
		}

		int scale = 1;

		double layerHeight = 0;
		while (layerHeight == 0) {
			if ((Double.parseDouble(size.get(0)) * scale <= 11.0) && (Double.parseDouble(size.get(1)) * scale <= 11.0) && (Double.parseDouble(size.get(2)) * scale <= 11.0))
				scale = 2540;
			else if ((Double.parseDouble(size.get(0)) * scale <= 50.0) && (Double.parseDouble(size.get(1)) * scale <= 50.0) && (Double.parseDouble(size.get(2)) * scale <= 25.0))
				layerHeight = 0.15;
			else if ((Double.parseDouble(size.get(0)) * scale <= 75.0) && (Double.parseDouble(size.get(1)) * scale <= 75.0) && (Double.parseDouble(size.get(2)) * scale <= 50.0))
				layerHeight = 0.2;
			else
				layerHeight = 0.3;
		}

		int count = 1;

		if (f.getName().toLowerCase().startsWith("ct")) {
			try {
				count = Integer.parseInt(f.getName().substring(2, 4));
			} catch (Exception e) {
				try {
					count = Integer.parseInt(f.getName().substring(2, 3));
				} catch (Exception ee) {
				}
			}
		}

		if (count > 4) {
			if (layerHeight == 0.15)
				layerHeight = 0.2;
			else if (layerHeight == 0.2)
				layerHeight = 0.3;
		}

		String cfg = String.valueOf(layerHeight);

		String process = "\"\"" + ps + "\" --load \"" + settings.get("ps-cfg").getAsString() + "\\" + cfg + "mm.ini\"";
		process += " --scale " + scale + " --duplicate " + count + " -g " + "\"" + f + "\"\"";

		lines = getCMD(process);

		for (String s : lines) {
			if (s.contains("error")) {
				error += s + System.lineSeparator();
			}
		}

		f.delete();
	}

	public static ArrayList<String> getCMD(String command) {
		ArrayList<String> lines = new ArrayList<String>();

		try {
			Process p = new ProcessBuilder().command("cmd.exe", "/c", command).start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			return null;
		}

		return lines;
	}

	public static void runCMD(String command) {
		try {
			new ProcessBuilder("cmd", "/c", command).inheritIO().start().waitFor();
		} catch (Exception e) {
		}
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
			System.err.println(e.getStackTrace());
		}

		return file;
	}
}
