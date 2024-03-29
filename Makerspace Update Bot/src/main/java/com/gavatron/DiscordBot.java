package com.gavatron;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class DiscordBot {
    static String tag = "";

    public static void main(String[] args) {
        /* Start the JDA bot builder, letting you provide the token externally rather
         * than writing it in your program's code. args[0] is the token. */
        JDABuilder jdaBotBuilder = JDABuilder.createDefault(args[0]);
        tag = args[1];

        // Disable parts of the cache
        jdaBotBuilder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);

        // Enable the bulk delete event - this means you'll have to handle it yourself!
        jdaBotBuilder.setBulkDeleteSplittingEnabled(false);

        // Set activity (like "playing Something")
        jdaBotBuilder.setActivity(Activity.playing(tag));

        // Set event listeners
        jdaBotBuilder.addEventListeners(new MessageListener(), new ReadyListener());

        try {
            // create the instance of JDA
            JDA discordBot = jdaBotBuilder.build();

            // optionally block until JDA is ready
            discordBot.awaitReady();
        } catch (LoginException | InterruptedException e) {
            System.err.println("Couldn't login.");
            e.printStackTrace();
        }
    }
}
