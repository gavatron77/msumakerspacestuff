package com.gavatron;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.gavatron.MainQueue.q;
import static com.gavatron.MainQueue.s;

public class Charge {
    public static void charge() {
        List<List<String>> toCharge = q.getAll("Charge", false);
        JsonObject prints = new JsonObject();


        for (List<String> line : toCharge) {
            JsonObject user = new JsonObject();
            JsonArray indexes = new JsonArray();

            try {
                user = prints.get(line.get(2) + " " + line.get(1)).getAsJsonObject();
            } catch (Exception e) {
                user.addProperty("unpaid", 0.0);
            }

            try {
                indexes = user.get("indexes").getAsJsonArray();
            } catch (Exception e) {
            }
            indexes.add(line.get(9));

            user.addProperty("unpaid", (Float.parseFloat(line.get(8)) + user.get("unpaid").getAsFloat()));
            user.add("indexes", indexes);
            if (Float.parseFloat(line.get(8)) != 0.0) prints.add(line.get(2) + " " + line.get(1), user);
        }
        System.out.println("\n\n");
        {
            int i = 1;
            for (Map.Entry<String, JsonElement> entry : prints.entrySet()) {
                if (entry.getValue().getAsJsonObject().get("unpaid").getAsInt() != 0) {
                    String price = new DecimalFormat("#.##").format(entry.getValue().getAsJsonObject().get("unpaid").getAsFloat());
                    System.out.println(i++ + ": " + entry.getKey() + " has $" + price + " unpaid.");
                }
            }
        }

        System.out.println("\nEnter a number from the list above to mark them as charged.");
        int key = Integer.parseInt(s.nextLine().trim());
        System.out.println("\n\n");

        {
            int i = 1;
            for (Map.Entry<String, JsonElement> entry : prints.entrySet()) {
                if (i++ == key) {
                    for (JsonElement s : entry.getValue().getAsJsonObject().get("indexes").getAsJsonArray()) {
                        String price = new DecimalFormat("#.##").format(Float.parseFloat(q.getPrint(s.getAsInt(), 8)));
                        System.out.println("Updated print from " + q.getPrint(s.getAsInt(), 3) + " valuing $" + price);
                        q.update(s.getAsInt()).writeFile();
                    }
                }
            }
        }
    }
}
