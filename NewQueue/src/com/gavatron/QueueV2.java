package com.gavatron;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class QueueV2 {
    static List<List<String>> file = new ArrayList<>();
    static String path;
    Map<String, String> user = new HashMap<>();

    public QueueV2(String p) {
        path = p;
        readFile(path);
//        Collections.reverse(file);
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

    public QueueV2 readFile(String path) {
        Scanner f = null;

        try {
            f = new Scanner(Paths.get(path));
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }

        int i = 0;
        while (f.hasNext()) {
            file.add(Arrays.asList(f.nextLine().split(",")));
        }
        return this;
    }

    public QueueV2 addLine(String fName, String lName, String email, String use, String mat, String part) {
        Calendar c = Calendar.getInstance();
        file.add(List.of("In queue", lName, fName, c.get(Calendar.MONTH) + 1 + "/" + c.get(Calendar.DAY_OF_MONTH), email, use, mat, part, "0"));

        return this;
    }

    public String getPrint(int index, int info) {
        return getPrint(index).get(info);
    }

    public List<String> getPrint(int index) {
        return file.get(index);
    }

    public List<List<String>> getAll(String key) {
        return getAll(key, true);
    }

    public List<List<String>> getAll(String key, boolean print) {
        List<List<String>> r = new ArrayList<>();

        int i = 0;
        int j = 0;
        for (List<String> line : file) {
            if (line.get(0).equals(key) || key.equals("")) {
                List<String> linee = new ArrayList<>();
                for (String o : line)
                    linee.add(o);
                linee.add(String.valueOf(j));
                r.add(linee);
                if (print) System.out.println((j + 1) + ": " + linee);
                i++;
            }
            j++;
        }

        if (print) System.out.println("Showing " + i + " item(s), out of " + j + " total.");

        return r;
    }

    public QueueV2 update(int index, String status) {
        file.get(index).set(0, status);

        return this;
    }

    public QueueV2 update(int index) {
        switch (file.get(index).get(0)) {
            case "In queue":
                update(index, "Printing");
                break;
            case "Printing":
                update(index, "Emailed");
                break;
            case "Emailed":
                update(index, "Charge");
                break;
            case "Charge":
                update(index, "Paid");
                break;
            default:
                System.out.println("This should never be reached. Please let Gavin know.");
                break;
        }

        return this;
    }

    public void charge(int index, String p) {
        file.get(index).set(9, p);
    }

    public void writeFile() {
        FileWriter f = null;

        try {
            f = new FileWriter(path);

            for (List s : file) {
                String l = "";
                for (Object o : s)
                    l += o + ",";
                f.append(l + System.lineSeparator());
            }
            f.close();
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }

    public String getErrorText(Exception err) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        err.printStackTrace(pw);
        return sw.toString();
    }
}
