package com.gavatron;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.gavatron.Charge.charge;
import static com.gavatron.Done.smartDone;
import static com.gavatron.NewFile.newFile;
import static com.gavatron.PickedUp.pickUp;

public class MainQueue {
    static File f;
    static QueueV2 q = new QueueV2("./prints.csv");
    static List<String> mats = Arrays.asList("PLA", "PETG", "TPU", "Resin", "Laser", "Machining", "Other...");
    static List<String> purposes = Arrays.asList("Personal", "103", "310r", "494", "Other...");
    static Scanner s = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("1: Email\n2: Pickup\n3: Charge");

            int g = Integer.parseInt(s.nextLine().trim());
            switch (g) {
                case 1:
                    smartDone();
                    break;
                case 2:
                    pickUp();
                    break;
                case 3:
                    charge();
                    break;
            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "email":
                    smartDone();
                    break;
                case "pickup":
                    pickUp();
                    break;
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "new":
                    f = new File(args[1]);
                    newFile();
                    break;
            }
        } else System.out.println("asdf");

        q.writeFile();

        s.close();
    }
}
