package com.gavatron;

import java.text.DecimalFormat;
import java.util.List;

import static com.gavatron.MainQueue.q;
import static com.gavatron.MainQueue.s;

public class PickedUp {
    public static void pickUp() {
        System.out.println("Enter first and last name, separated by a space.");
        String name = s.nextLine().trim().toLowerCase();

        List<List<String>> jobs = q.getAll("", false);

        double tab = 0;
        int count = 0;

        for (int i = 0; i < jobs.size(); i++) {
            List<String> l = jobs.get(i);
            if (name.contains(l.get(1).toLowerCase()) && name.contains(l.get(2).toLowerCase()) && !l.get(0).contains("Paid") && !(l.get(5).contains("310") || l.get(5).contains("494"))) {
                if (!l.get(0).contains("Charge")) {
                    System.out.println("Found print from " + l.get(3) + " and updated to \"Charge\"");
                    count++;
                }
                q.update(i, "Charge");
                tab += Double.parseDouble(l.get(8));
            }
        }

        System.out.println("Found and updated " + count + " prints.");
        System.out.println("User's unpaid tab is $" + new DecimalFormat("#.##").format(tab) + ".");
    }
}
