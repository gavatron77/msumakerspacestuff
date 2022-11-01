package com.gavatron;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gavatron.MainQueue.q;

public class Charge {
    public static void charge() {
        List<List<String>> toCharge = q.getAll("Charge", false);
        System.out.println(toCharge);
        JsonObject names = new JsonObject();
        JsonObject prints = new JsonObject();

        for (List<String> line : toCharge) {
            JsonObject user;
            if (personal) {
                try {
                    prints.addProperty(prints.get(line.get(2) + " " + line.get(1)).getAsString(), line.get(9));
                } catch (Exception e) {
                    prints.addProperty("price", "ar");
                }
            }
        }
    }
}
