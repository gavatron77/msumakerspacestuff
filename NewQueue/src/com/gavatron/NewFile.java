package com.gavatron;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.gavatron.MainQueue.*;

public class NewFile {
    static void newFile() throws IOException {
        boolean folder = f.isDirectory();

        List<String> in = new ArrayList<>();

        System.out.println("\nFirst name:"); //0
        in.add(format(s.nextLine().trim()));

        System.out.println("\nLast name:"); //1
        in.add(format(s.nextLine().trim()));

        System.out.println("\nEmail:"); //2
        in.add(s.nextLine().trim());

        System.out.println("\nUse (enter 1 - " + purposes.size() + "): "); //3
        for (int i = 0; i < purposes.size(); i++) {
            System.out.println((i + 1) + ": " + purposes.get(i));
        }

        {
            int selection = Integer.parseInt(s.nextLine().trim()) - 1;

            if ((selection + 1) != purposes.size())
                in.add(purposes.get(selection));
            else {
                System.out.println("\nEnter custom use: ");
                in.add(format(s.nextLine().trim()));
            }
        }

        System.out.println("\nMaterial (enter 1 - " + mats.size() + "): "); //4
        for (int i = 0; i < mats.size(); i++) {
            System.out.println((i + 1) + ": " + mats.get(i));
        }

        {
            int material = Integer.parseInt(s.nextLine().trim()) - 1;

            if ((material + 1) != mats.size())
                in.add(mats.get(material));
            else {
                System.out.println("\nEnter custom material: ");
                in.add(format(s.nextLine().trim()));
            }
        }

        if (folder) { //5
            in.add(format(f.getName().trim()));
        } else {
            in.add("");
        }

        if (folder) { //6
            in.add(f.getAbsolutePath());
        }

        if (f.exists()) {
            System.out.println(f.getName());
            String arg;
            if (!folder)
                arg = "java -jar NewPrintProcesser.jar \"" + f.getAbsolutePath() + "\" \"" + in.get(0) + "\" \"" + in.get(1) + "\"";
            else
                arg = "java -jar NewPrintProcesser.jar \"" + f.getAbsolutePath() + "\" \"" + in.get(0) + "\" \"" + in.get(1) + "\" \"" + f.getName() + "\"";

            System.out.println("\n\n");

            List<String> run = getCMD(arg);
            for (String l : run) {
                if (l.contains("Please"))
                    System.out.println(l);
                else if (l.contains("fileLocation"))
                    in.set(5, Path.of(l.substring(15)).toString());
            }

            q.addLine(in.get(0), in.get(1), in.get(2), in.get(3), in.get(4), in.get(5).substring(in.get(5).lastIndexOf("\\"),in.get(5).length()));

        }


    }

    static String format(String f) {
        f = StringUtils.lowerCase(f);
        f = StringUtils.capitalize(f);

        return f;
    }

    public static ArrayList<String> getCMD(String command) {
        System.out.println(command);
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
}
