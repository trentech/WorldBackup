package com.gmail.trentech.worldbackup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.commands.CommandManager;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Resource;
import com.gmail.trentech.worldbackup.utils.Zip;

import me.flibio.updatifier.Updatifier;
import ninja.leaping.configurate.ConfigurationNode;

@Updatifier(repoName = Resource.ID, repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, dependencies = "after: Updatifier")
public class Main {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;

	@Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getGame().getPluginManager().getLogger(plugin);
    }

    @Listener
    public void onInitialization(GameInitializationEvent event) {
    	getGame().getCommandManager().register(this, new CommandManager().cmdBackup, "backup");
    }

    @Listener
    public void onStartedServer(GameStartedServerEvent event) {
    	ConfigManager configManager = new ConfigManager();
    	ConfigurationNode config = configManager.getConfig();
    	ConfigurationNode schedulers = config.getNode("schedulers");
    	
		for(Entry<Object, ? extends ConfigurationNode> entry : schedulers.getChildrenMap().entrySet()){
			String name = entry.getKey().toString();
			
			ConfigurationNode node = schedulers.getNode(name);
			
			String source = node.getNode("source").getString();
			String next = node.getNode("next").getString();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Date date = null;
			try {
				date = format.parse(next);		
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			int interval = (int) ((date.getTime() - new Date().getTime()) / 1000);
			
			if(interval <= 0){
				Calendar calendar = Calendar.getInstance();
				
				while(interval <= 0){
					interval = node.getNode("interval").getInt();

					calendar.setTime(date);
					calendar.add(Calendar.SECOND, interval);
					date = calendar.getTime();

					next = format.format(date);

					config.getNode("schedulers", name, "next").setValue(next);

					interval = (int) ((date.getTime() - new Date().getTime()) / 1000);
				}
				
				createTask(name, source, interval);
				
				configManager.save();
	
				if(source.equalsIgnoreCase("all")){
					for(WorldProperties properties : Main.getGame().getServer().getAllWorldProperties()){
						new Zip(properties.getWorldName()).save();
					}
				}else{
					new Zip(source).save();
				}
				continue;
			}
			
			createTask(name, source, interval);
		}
    }

    public static Logger getLog() {
        return log;
    }
    
	public static Game getGame() {
		return game;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}
	
	public static void createTask(String name, String worldName, int interval){
		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();
		
		int newInterval = config.getNode("schedulers", name, "interval").getInt();
		
        Main.getGame().getScheduler().createTaskBuilder().delay(interval, TimeUnit.SECONDS).name(name).execute(new Runnable() {

			@Override
            public void run() {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.SECOND, interval);
				
				Date date = calendar.getTime();
				
				createTask(name, worldName, newInterval);
				
				String next = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

				config.getNode("schedulers", name, "next").setValue(next);

				configManager.save();
				
				if(worldName.equalsIgnoreCase("all")){
					for(WorldProperties properties : Main.getGame().getServer().getAllWorldProperties()){
						new Zip(properties.getWorldName()).save();
					}
				}else{
					new Zip(worldName).save();
				}
			}
        }).submit(getPlugin());
	}
}