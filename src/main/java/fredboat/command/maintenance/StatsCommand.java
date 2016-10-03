package fredboat.command.maintenance;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.FredBoat;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.CommandManager;
import fredboat.commandmeta.abs.Command;
import fredboat.sharding.ShardTracker;
import fredboat.util.BotConstants;
import fredboat.util.DiscordUtil;
import fredboat.util.TextUtils;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.json.JSONException;
import org.json.JSONObject;

public class StatsCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        long totalSecs = (System.currentTimeMillis() - FredBoat.START_TIME) / 1000;
        int days = (int) (totalSecs / (60 * 60 * 24));
        int hours = (int) ((totalSecs / (60 * 60)) % 24);
        int mins = (int) ((totalSecs / 60) % 60);
        int secs = (int) (totalSecs % 60);

        String uptime30Days = "ERROR";
        String url = "https://api.uptimerobot.com/getMonitors?apiKey=m777550344-171dca574df5a9f8dc7f2484&responseTimesAverage=180&monitors=15830-32696&customUptimeRatio=30&format=json";
        try {
            String jsonstr = Unirest.get(url).asString().getBody().substring("jsonUptimeRobotApi(".length());
            jsonstr = jsonstr.substring(0, jsonstr.length() - 1);
            uptime30Days = new JSONObject(jsonstr).getJSONObject("monitors").getJSONArray("monitor").getJSONObject(0).getString("customuptimeratio") + "%";
            //uptime30Days = Unirest.get(url).asJson().getBody().getObject().getJSONObject("monitors").getJSONArray("monitor").getJSONObject(0).getString("customUptimeRatio") + "%";
        } catch (JSONException | UnirestException ex) {
            ex.printStackTrace();
        }
        
        String str = " This shard has been running for "
                + days + " days, "
                + hours + " hours, "
                + mins + " minutes and "
                + secs + " seconds.\n"
                + "This shard has executed " + (CommandManager.commandsExecuted - 1) + " commands this session.\n";
        
        if(DiscordUtil.isMusicBot()){
            str = str + "Players playing:           " + PlayerRegistry.getPlayingPlayers().size() + "\n";
        }
        
        str = str + "That's a rate of " + (float) (CommandManager.commandsExecuted - 1) / ((float) totalSecs / (float) (60 * 60)) + " commands per hour\n\n```";
        str = str + "Host uptime last 30 days:  " + uptime30Days + "\n";
        str = str + "Reserved memory:           " + Runtime.getRuntime().totalMemory() / 1000000 + "MB\n";
        str = str + "-> Of which is used:       " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + "MB\n";
        str = str + "-> Of which is free:       " + Runtime.getRuntime().freeMemory() / 1000000 + "MB\n";
        str = str + "Max reservable:            " + Runtime.getRuntime().maxMemory() / 1000000 + "MB\n";

        str = str + "\n----------\n\n";

        str = str + "Shard:                     " + FredBoat.shardId + " of a total of " + FredBoat.numShards + "\n";
        str = str + "Known servers:             " + ShardTracker.getGlobalGuildCount() + "\n";
        str = str + "-> In this shard:          " + guild.getJDA().getGuilds().size();
        str = str + "Known users in servers:    " + ShardTracker.getGlobalUserCount()+ "\n";
        str = str + "-> In this shard:          " + guild.getJDA().getUsers().size();
        str = str + "Is beta:                   " + BotConstants.IS_BETA + "\n";
        str = str + "JDA responses total:       " + guild.getJDA().getResponseTotal() + "\n";
        str = str + "JDA version:               " + JDAInfo.VERSION;
        
        str = str + "```";

        channel.sendMessage(TextUtils.prefaceWithMention(invoker, str));
        //channel.sendMessage(str2);
        //hannel.sendMessage(str3);
    }

}