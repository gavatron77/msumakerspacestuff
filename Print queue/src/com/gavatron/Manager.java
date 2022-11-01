package com.gavatron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;
import javax.swing.filechooser.FileSystemView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Manager {
	static Path dir;
	static String pQueue = dir + "\\Gqueue\\queue.txt";
	static String pRunning = dir + "\\Done\\running.txt";
	static JsonArray drives;
	static List<String> keys = Arrays.asList("Num", "Material", "Time", "Printer", "Name");
	static int fileNum = -1;
	static QueueV2 q = new QueueV2("./prints.csv");
	static File card;
	static Exception err;
	static String badThing = "";
	static Webhook w;

	public static void main(String[] args) throws IOException {
		JsonObject settings = readJsonFile("./path.json");
		dir = Paths.get(settings.get("queue").getAsString());
		drives = settings.get("sd-card").getAsJsonArray();
		pQueue = dir + "\\Gqueue\\queue.txt";
		pRunning = dir + "\\Done\\running.txt";
		w = new Webhook(settings.get("webhook-url").getAsString());

		createDependencies();
		if (args.length != 0) {
			if (args[0].matches("get")) {
				getFile(args[1].contains("true"));
			} else {
				String f = settings.get("grabcad").getAsString() + "\\Gcode" + "\\" + LocalDate.now().getMonth().toString();
				File folder = new File(f);
				if (!folder.exists()) {
					folder.mkdirs();
				}

				addFile(args[0], f + "\\");
			}
		}

		if (!badThing.equals("")) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			err.printStackTrace(pw);
			w.addEmbed(new Embed().setDesc(badThing).setTitle(sw.toString())).setContent("Print Queue Manager error:").send();
		}
	}

	static void getFile(boolean copy) throws IOException {
		moveDone();

		Scanner f = null;

		try {
			f = new Scanner(new File(pQueue));
		} catch (FileNotFoundException e) {
			err = e;
			badThing += "\nQueue file is missing";
		}

		ArrayList<String> files = new ArrayList<String>();

		while (f.hasNext())
			files.add(f.nextLine());

		f.close();

		List<Map<String, String>> user = new ArrayList<>();

		if (files.size() == 0) {
			System.out.println("Queue is empty. Closing this window...");
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			}
		} else {
			System.out.println("\n\n\nWhich file?\n");
			System.out.println("List is sorted oldest to newest.\n");
			System.out.println("(Enter 1 - " + files.size() + ")");

			int c = 0;

			for (String s : files) {
				Map<String, String> temp = new HashMap<>();

				temp.put("Material", "?");
				temp.put("Time", "?");
				temp.put("Printer", "?");
				temp.put("Name", "?");
				temp.put("Num", String.valueOf(c + 1));

				try {
					temp.put("Name", s.substring(s.lastIndexOf('\\') + 1, s.lastIndexOf('.')));
					temp.put("Num", String.valueOf(c + 1));

					try {
						temp.put("ext", s.substring(s.lastIndexOf("."), s.length()));
					} catch (Exception e) {
						temp.put("ext", "null");
						err = e;
						badThing += "\nUnable to get extension";
					}

					temp.put("Material", "?");
					temp.put("Time", "?");
					temp.put("Printer", "?");

					if (temp.get("ext").contains(".gcode")) {
						try (BufferedReader br = new BufferedReader(new FileReader(s))) {
							for (String line; (line = br.readLine()) != null;) {
								if (line.contains("; filament_type"))
									temp.put("Material", line.substring(line.lastIndexOf('=') + 2, line.length()));
								else if (line.contains("time (normal mode)"))
									temp.put("Time", line.substring(line.lastIndexOf('=') + 2, line.length()));
								else if (line.contains("; printer_model"))
									temp.put("Printer", line.substring(line.lastIndexOf('=') + 2, line.length()));
							}
						} catch (Exception e) {
							System.out.println("File " + temp.get("Name") + " doesn't exist, or is unreadable.");
							err = e;
							badThing += "\nFile gone";
						}
					} else {
						temp.put("Material", "Resin");
						temp.put("Time", "?");
						temp.put("Printer", "Phrozen");
					}
				} catch (Exception e) {
					err = e;
					badThing += "\nSo many things could have gone wrong";
				}

				c += 1;
				user.add(temp);
			}

			// Get lengths of the data
			int lengths[] = new int[keys.size()];
			for (int i = 0; i < keys.size(); i++) {
				ArrayList<String> stuff = new ArrayList<String>();
				for (int j = 0; j < files.size(); j++) {
					stuff.add(user.get(j).get(keys.get(i)));
				}

				lengths[i] = length(stuff);
				if (lengths[i] < keys.get(i).length())
					lengths[i] = keys.get(i).length();
			}

			// Print the stuff

			int length = 0;
			System.out.print("\n\n\n");
			for (int i = 0; i < keys.size(); i++) {
				String thing = "| " + keys.get(i);
				thing += ": ";
				System.out.print(thing);
				length += thing.length();
				for (int j = 0; j < (lengths[i] - thing.length() + 4); j++) {
					System.out.print(" ");
					length++;
				}
			}

			System.out.println("|");
			System.out.print("|");
			for (int i = 0; i < length - 1; i++) {
				System.out.print("-");
			}
			System.out.println("|");

			for (int i = 0; i < files.size(); i++) {
				System.out.print("|");
				for (int j = 0; j < keys.size(); j++) {
					System.out.print(" " + user.get(i).get(keys.get(j)));
					try {
						for (int k = 0; k < (lengths[j] - user.get(i).get(keys.get(j)).length() + 2); k++)
							System.out.print(" ");
					} catch (Exception e) {
						for (int k = 0; k < (lengths[j] - 2); k++)
							System.out.print(" ");
					}
					System.out.print("|");
				}

				System.out.println();
			}

			System.out.println();

			// Done printing, get input
			Scanner s = new Scanner(System.in);

			while (fileNum < 0 || fileNum > files.size() - 1) {
				try {
					fileNum = Integer.parseInt(s.nextLine()) - 1;
				} catch (Exception e) {
					System.out.println("Enter a number between 1 and " + files.size());
				}
			}

			s.close();
			f.close();

			int printNum = -1;

			List<List<String>> prints = q.getAll("", false);

			{
				for (int i = 0; i < prints.size(); i++) {
					if (files.get(fileNum).contains(prints.get(i).get(1))) {
						if (files.get(fileNum).contains(prints.get(i).get(2))) {
							printNum = i;
						}
					}
				}
			}

			if (printNum == -1)
				System.out.println("\"Name was not found in queue. Status has not been updated.\"");

			String data = files.get(fileNum);

			if (copy) {
				System.out.println("\n\nWaiting for SD card to be inserted.\n");
				card = new File("");

				while (!card.exists()) {
					for (int i = 0; i < drives.size(); i++) {
						if (new File(drives.get(i).getAsString()).exists())
							card = new File(drives.get(i).getAsString());
					}
				}

				String name = FileSystemView.getFileSystemView().getSystemDisplayName(new File(card + "\\"));
				System.out.println("Card " + card + " (" + name.substring(0, name.lastIndexOf("(") - 1) + ") detected. Copying file. Please wait for confirmation.\n\n\n");

				try (Stream<Path> paths = Files.walk(card.toPath())) {
					paths.filter(Files::isRegularFile).forEach(Manager::donify);
				}

				try {
					File dest = new File(card + getName(data));
					Files.copy(Paths.get(data), Paths.get(dest.toString()));
					updateTime(dest.toString());

					new File(data).renameTo(new File(dir + "\\" + getName(data)));

					FileWriter w = new FileWriter(pRunning);
					w.write(dir + "\\" + getName(data));
					w.close();

					ArrayList<String> queue = new ArrayList<String>();

					try {
						queue = (ArrayList<String>) Files.readAllLines(new File(pQueue).toPath(), Charset.defaultCharset());
					} catch (Exception e) {
						err = e;
						badThing += "\nQueue file is gone";
					}

					queue.remove(fileNum);

					fileArrayList("\\Gqueue\\queue.txt", queue);

					System.out.println("File copied. Remember to print in " + user.get(fileNum).get("Material") + "!");
					q.update(printNum, "Printing");
					q.writeFile();
					System.out.println("\nStatus for " + q.getPrint(printNum, 2) + "'s " + q.getPrint(printNum, 3) + " print changed to: " + q.getPrint(printNum, 0));
				} catch (Exception e) {
					err = e;
					badThing += "\nFile wasn't found in the print log";
				}
			}
		}
	}

	static void donify(Path path) {
		if (path.toString().toLowerCase().contains(".gcode") && !path.toString().contains("\\")) {
			File f = new File(card + "\\Done");
			if (!f.exists())
				f.mkdirs();
			new File(path.toString()).renameTo(new File(f.toString() + "\\" + path.getFileName()));
		}
	}

	static int length(ArrayList<String> stuff) {
		int longest = 0;
		for (int i = 0; i < stuff.size(); i++) {
			if (longest < stuff.get(i).length())
				longest = stuff.get(i).length();
		}
		return longest;
	}

	static void moveDone() {
		String p = "";
		try {
			p = Files.readString(new File(pRunning).toPath());
		} catch (Exception e) {
			err = e;
			badThing += "\nCurrently running file is gone";
		}

		new File(p).renameTo(new File(dir + "\\Done\\" + getName(p)));
	}

	static void addFile(String p, String gc) {
		ArrayList<String> queue = new ArrayList<String>();
		try {
			queue = (ArrayList<String>) Files.readAllLines(new File(pQueue).toPath(), Charset.defaultCharset());
			queue.add(dir + "\\Gqueue\\" + Paths.get(p).getFileName());
			fileArrayList("\\Gqueue\\queue.txt", queue);

			try {
				Files.copy(Paths.get(p), Paths.get(gc + Paths.get(p).getFileName()));
			} catch (Exception e) {
				err = e;
				badThing += "\nCopying new file failed";
			}

			new File(p).renameTo(new File(dir + "\\Gqueue\\" + Paths.get(p).getFileName()));
		} catch (Exception e) {
			err = e;
			badThing += "\nBacking up new file failed";
		}
	}

	static void createDependencies() {
		String[] folders = { "\\Gqueue", "\\Done" };
		File folder;
		for (String f : folders) {
			folder = new File(dir + f);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
	}

	static String getName(String file) {
		int bs = 0;
		for (int i = 0; i < file.length(); i++) {
			if (file.charAt(i) == '\\')
				bs = i + 1;
		}

		return file.substring(bs, file.length());
	}

	static String getExtension(String file) {
		int dot = 0;
		for (int i = 0; i < file.length(); i++) {
			if (file.charAt(i) == '.')
				dot = i + 1;
		}

		return file.substring(dot, file.length());
	}

	static void fileArrayList(String file, ArrayList<String> queue) throws IOException {
		FileWriter w = new FileWriter(dir + file);
		for (String l : queue) {
			w.write(l + System.lineSeparator());
		}
		w.close();
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
			err = e;
			badThing += "\nReading settings file failed";
		}

		return file;
	}

	static void updateTime(String path) {
		try {
			Path file = Paths.get(path);

			long currentTimeMillis = System.currentTimeMillis();
			FileTime fileTime = FileTime.fromMillis(currentTimeMillis);
			Files.setLastModifiedTime(file, fileTime);
		} catch (Exception e) {
			err = e;
			badThing += "\nUpdating file modified time failed";
		}
	}
}
