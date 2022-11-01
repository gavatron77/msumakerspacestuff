package com.gavatron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Manager {
    static Path dir;
    static String pQueue = dir + "\\Gqueue\\queue.txt";
    static String pRunning = dir + "\\Done\\running.txt";
    static JsonArray drives;

    public static void main(String[] args) throws IOException {
        JsonObject settings = readJsonFile("./path.json");
        dir = Paths.get(settings.get("queue").getAsString());
        drives = settings.get("sd-card").getAsJsonArray();
        pQueue = dir + "\\Gqueue\\queue.txt";
        pRunning = dir + "\\Done\\running.txt";

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
    }

    static void getFile(boolean copy) throws IOException {
        moveDone();

        Scanner f = null;

        try {
            f = new Scanner(new File(pQueue));
        } catch (FileNotFoundException e) {
            System.err.println(e.getStackTrace());
        }

        ArrayList<String> files = new ArrayList<String>();

        while (f.hasNext()) files.add(f.nextLine());

        f.close();

        if (files.size() == 0) {
            System.out.println("Queue is empty. Closing this window...");
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        } else {
            System.out.println("Which file?\n");
            System.out.println("List is sorted oldest to newest.\n");
            System.out.println("(Enter 1 - " + files.size() + ")");

            int c = 1;

            for (String s : files) {
                try (BufferedReader br = new BufferedReader(new FileReader(s))) {
                    System.out.print("    " + c++ + ": ");

                    System.out.print(s.substring(s.lastIndexOf('\\') + 1, s.lastIndexOf('.')));

                    String ext;
                    ext = s.substring(s.lastIndexOf('.') + 1, s.length());

                    switch (ext) {
                        case "ctb":
                            System.out.println(" (Resin)");
                            break;
                        case "gcode":
                            System.out.print(" (FDM");
                            for (String line; (line = br.readLine()) != null; ) {
                                if (line.contains("; filament_type")) {
                                    System.out.print(", " + line.substring(line.lastIndexOf('=') + 2, line.length()));
                                }
                            }
                            System.out.println(")");
                            break;
                        default:
                            System.out.println(" (Unknown type: ." + ext + ")");
                            break;
                    }
                } catch (Exception e) {
                    System.err.println(e.getStackTrace());
                }


            }

            Scanner s = new Scanner(System.in);

            int fileNum = -1;
            while (fileNum < 0 || fileNum > files.size() - 1) {
                try {
                    fileNum = Integer.parseInt(s.nextLine()) - 1;
                } catch (Exception e) {
                    System.out.println("Enter a number between 1 and " + files.size());
                }
            }


            s.close();
            f.close();

            String data = files.get(fileNum);

            if (copy) {
                System.out.println("\n\nWaiting for SD card to be inserted...\n\n\n\n");
                File card = new File("");

                while (!card.exists()) {
                    for (int i = 0; i < drives.size(); i++)
                        if (new File(drives.get(i).getAsString()).exists())
                            card = new File(drives.get(i).getAsString());
                }

                try {
                    File dest = new File(card + getName(data));
                    Files.copy(Paths.get(data), Paths.get(dest.toString()));
                    updateTime(dest.toString());
                } catch (Exception e) {
                    System.err.println(e.getStackTrace());
                }
            }

            new File(data).renameTo(new File(dir + "\\" + getName(data)));

            FileWriter w = new FileWriter(pRunning);
            w.write(dir + "\\" + getName(data));
            w.close();

            ArrayList<String> queue = new ArrayList<String>();

            try {
                queue = (ArrayList<String>) Files.readAllLines(new File(pQueue).toPath(), Charset.defaultCharset());
            } catch (Exception e) {
                System.err.println(e.getStackTrace());
            }

            queue.remove(fileNum);

            fileArrayList("\\Gqueue\\queue.txt", queue);
        }
    }

    static void moveDone() {
        String p = "";
        try {
            p = Files.readString(new File(pRunning).toPath());
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
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
                System.err.println(e.getStackTrace());
            }

            new File(p).renameTo(new File(dir + "\\Gqueue\\" + Paths.get(p).getFileName()));
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }

    static void createDependencies() {
        String[] folders = {"\\Gqueue", "\\Done"};
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
            if (file.charAt(i) == '\\') bs = i + 1;
        }

        return file.substring(bs, file.length());
    }

    static String getExtension(String file) {
        int dot = 0;
        for (int i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.') dot = i + 1;
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
            if (s.charAt(i) == '\\') e += "\\\\";
            else e += s.charAt(i);
        }

        return e;
    }

    static JsonObject readJsonFile(String p) {
        JsonObject file = new JsonObject();

        try {
            String raw = "";
            Scanner s = new Scanner(new File(p));
            while (s.hasNext()) raw += s.nextLine();
            s.close();
            file = JsonParser.parseString(parseFile(raw)).getAsJsonObject();
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
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
            System.err.println(e.getStackTrace());
        }
    }
}
