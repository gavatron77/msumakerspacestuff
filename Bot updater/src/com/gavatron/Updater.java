package com.gavatron;

import java.io.File;

public class Updater {
    public static void main(String[] args) {
        Webhook w = new Webhook("https://ptb.discord.com/api/webhooks/970548551681728522/JqdmLZkVQhhH_mxB1F7L9K_-24CMZVu5SHeFN8iP4xIE_hv9feBXLWTwTcA06CmAmydF").setName("Makerspace Bot Self-Updater");

        try {
            new File("bot.jar").delete();
            new File("newbot.jar").renameTo(new File("bot.jar"));
            w.setContent("Bot successfully updated!").send();
        } catch (Exception err) {
            w.setContent("Bot failed to update. Error details:");
            Embed e = new Embed().setDesc(err.getLocalizedMessage());
            err.printStackTrace();
            w.addEmbed(e).send();
        }
    }
}
