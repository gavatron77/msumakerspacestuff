package com.gavatron;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.gavatron.MainQueue.q;
import static com.gavatron.MainQueue.s;
import static com.gavatron.QueueV2.readJsonFile;

public class Done {
    static JsonObject settings = readJsonFile("./path.json");
    static Webhook w = new Webhook(settings.get("webhook-url").getAsString());
    static String formatPrice = "";

    static void smartDone() throws IOException {
        File card = new File("");
        JsonArray drives = settings.get("sd-card").getAsJsonArray();

        while (!card.exists()) {
            for (int i = 0; i < drives.size(); i++) {
                if (new File(drives.get(i).getAsString()).exists()) card = new File(drives.get(i).getAsString());
            }
        }

        long date = 0;
        String fileName = "";

        for (String file : card.list()) {
            long d = new File(card + file).lastModified();
            if ((d > date) && (file.contains("gcode"))) {
                date = new File(card + file).lastModified();
                fileName = file;
            }
        }

        Scanner f = new Scanner(new File(card + fileName));
//        Scanner f = new Scanner(new File("C:\\Users\\Gavin Smith\\eclipse-workspace\\Print queue\\Gqueue\\Norrell - armassembly_4.13.2022.gcode"));

        String[] time = {"0"};
        double price = 0.0;

        while (f.hasNext()) {
            String line = f.nextLine();
            if (line.contains("(normal mode)")) time = line.split("= ")[1].split(" ");
            if (line.contains("filament used [g]")) price = Double.valueOf(line.split("= ")[1]) * 0.06;
        }

        time = reverse(time);

        try {
            time[0] = time[0].substring(0, time[0].length() - 1);

            for (int i = 0; i < time.length; i++) {
                if (time[i].contains("m"))
                    time[0] = String.valueOf((Integer.parseInt(time[i].substring(0, time[i].length() - 1)) * 60) + Integer.parseInt(time[0]));
                else if (time[i].contains("h"))
                    time[0] = String.valueOf((Integer.parseInt(time[i].substring(0, time[i].length() - 1)) * 60 * 60) + Integer.parseInt(time[0]));
                else if (time[i].contains("d"))
                    time[0] = String.valueOf((Integer.parseInt(time[i].substring(0, time[i].length() - 1)) * 60 * 60 * 24) + Integer.parseInt(time[0]));
            }

            price += Double.parseDouble(time[0]) * (0.5 / 60 / 60);
            if (price < 0.4) price = 0;
            if (price < 1.0) price = 1;

            formatPrice = new DecimalFormat("#.##").format(price);

            if (!doneDone(fileName.substring(0, fileName.indexOf(" -")), price)) {
                if (!doneDone(fileName.split(" ")[0] + " " + fileName.split(" ")[1], price)) {
                    String desc = "File name: " + fileName + "\nCalculated price: $" + formatPrice;
                    w.setName("Done Error Reporting").addEmbed(new Embed().setTitle("Name not found in queue. Info:").setDesc(desc).setColor(0, 255, 0));
                    w.send(true);

                    System.out.println("Something went wrong, Gavin has been notified. Price for this print was " + formatPrice);
                }
            }
        } catch (Exception e) {
            String desc = "File name: " + fileName + "\n\n" + q.getErrorText(e);
            w.setName("Done Error Reporting").addEmbed(new Embed().setTitle("Done was unable to get a price:").setDesc(desc).setColor(0, 255, 0));
            if (w.send(true)) {
                System.out.println("Something went wrong, Gavin has been notified. The price for this print was unable to be determined.");
            } else {
                System.out.println("Something went wrong, and the error was unable to be sent. Please take a screenshot of this, and let him know\n\n\n\n" + q.getErrorText(e));
            }
        }
    }


    static String[] reverse(String[] r) {
        String[] temp = new String[r.length];
        for (int i = 0; i < r.length; i++)
            temp[i] = r[r.length - i - 1];
        return temp;
    }

    static void dumbDone() {
        System.out.println("Enter name:");
        String name = s.nextLine().toLowerCase();
        System.out.println("Enter price:");

        doneDone(name, Double.parseDouble(s.nextLine().trim()));
        s.close();
    }

    private static boolean doneDone(String name, Double price) {
        int index = -1;

        try {
            name = name.toLowerCase();

            List<String> foundJob = new ArrayList<>();

            List<List<String>> jobs = q.getAll("", false);
            for (int i = 0; i < jobs.size(); i++) {
                List<String> l = jobs.get(i);
                if (name.contains(l.get(1).toLowerCase()) && name.contains(l.get(2).toLowerCase()) && (l.get(0).contains("Printing") || l.get(0).contains("Emailed"))) {
                    index = i;
                    foundJob = l;
                    break;
                }
            }

            if (index != -1) {
                q.update(index, "Emailed");
                q.charge(index, formatPrice);
                StringSelection selection = new StringSelection(foundJob.get(4));
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);

                System.out.println(foundJob.get(2) + "'s print from " + foundJob.get(3) + " updated to Emailed, and price was set to $" + formatPrice);
                System.out.println("User email has been copied to clipboard. Make sure to send an email.");
            } else {
                return false;
            }
        } catch (Exception err) {
            w.addEmbed(new Embed().setTitle("Done failed, at index " + index + ":").setDesc(q.getErrorText(err)).setColor(255, 25, 0));
            return false;
        }
        return true;
    }
}
