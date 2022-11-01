package com.gavatron;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import static com.gavatron.DiscordBot.tag;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String m = event.getMessage().getContentDisplay();

        System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(), event.getMember().getEffectiveName(), m);

        String msg = "Gavin forgot to update this lol";

        if (m.startsWith(tag)) {
            switch (m.substring(1).split(" ")[0].trim()) {
                case "update":
                    if (m.split(" ").length == 1) {
                        if (saveFile("", "QueueV2.jar")) msg = "Files successfully updated!";
                        else msg = "File update failed.";
                    } else if (m.split(" ").length == 3) {
                        if (saveFile(m.split(" ")[1], m.split(" ")[2]))
                            msg = m.split(" ")[2] + " successfully updated!";
                        else msg = m.split(" ")[2] + " update failed.";
                    } else {
                        msg = "Command formatted incorrectly.";
                    }

                    break;
                case "selfupdate":
                    updateSelf(event);
                case "defaultupdate":
                    if (saveFile("https://github.com/gavatron77/randomfiles/raw/main/newbot.jar", "newbot.jar") && saveFile("https://github.com/gavatron77/randomfiles/raw/main/Bot%20updater.jar", "Bot updater.jar"))
                        msg = "newbot.jar and Bot Updater.jar updated!";
                    else
                        msg = "One or more files failed to copy. See error log.";
                    if (m.split(" ").length == 3)
                        if (m.split(" ")[2].equals(" y"))
                            updateSelf(event);
                    break;
                default:
                    msg = "Unknown or badly formatted command.";
            }
        }

        event.getGuild().getTextChannelsByName(event.getTextChannel().getName(), true).get(0).sendMessage(msg).queue();
    }

    public static boolean saveFile(String page, String name) {
        String file = "";

        for (int i = 0; i < name.length(); i++)
            if (name.substring(i, i).equals("|")) file += " ";
            else file += name.charAt(i);

        try {
            URL url = new URL(page);
            InputStream in = url.openStream();
            FileOutputStream fos = new FileOutputStream(file);
            int length = -1;
            byte[] buffer = new byte[1024];

            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);
            }

            fos.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void updateSelf(MessageReceivedEvent event) {
        event.getGuild().getTextChannelsByName(event.getTextChannel().getName(), true).get(0).sendMessage("Updating... please wait.").queue();
        System.exit(0);
    }
}
