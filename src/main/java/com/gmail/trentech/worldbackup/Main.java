package com.gmail.trentech.worldbackup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.commands.CommandManager;
import com.gmail.trentech.worldbackup.data.BackupData;
import com.gmail.trentech.worldbackup.data.Zip;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Resource;

import me.flibio.updatifier.Updatifier;
import ninja.leaping.configurate.ConfigurationNode;

@Updatifier(repoName = Resource.ID, repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class Main {

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;

	@Listener
	public void onPreInitialization(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getPlugin().getLogger();
	}

	@Listener
	public void onInitialization(GameInitializationEvent event) {
		getGame().getCommandManager().register(this, new CommandManager().cmdBackup, "backup");
	}

	@Listener
	public void onStartedServer(GameStartedServerEvent event) {
		for (BackupData backupData : BackupData.all()) {
			long interval = ((backupData.getNext().getTime() - new Date().getTime()) / 1000);

			if (interval <= 0) {
				Calendar calendar = Calendar.getInstance();

				while (interval <= 0) {
					interval = backupData.getInterval();

					calendar.setTime(backupData.getNext());
					calendar.add(Calendar.SECOND, (int) interval);
					Date date = calendar.getTime();

					backupData.setNext(date);

					interval = ((date.getTime() - new Date().getTime()) / 1000);
				}

				backupData.start(interval);

				String source = backupData.getSource();

				if (source.equalsIgnoreCase("all")) {
					for (WorldProperties properties : Main.getGame().getServer().getAllWorldProperties()) {
						new Zip(properties.getWorldName()).save();
					}
				} else {
					new Zip(source).save();
				}
				continue;
			}

			backupData.start(interval);
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

	public static void createTask(String name, String worldName, long interval) {
		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		long newInterval = config.getNode("schedulers", name, "interval").getLong();

		Main.getGame().getScheduler().createTaskBuilder().delay(interval, TimeUnit.SECONDS).name(name).execute(t -> {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, (int) interval);

			Date date = calendar.getTime();

			createTask(name, worldName, newInterval);

			String next = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

			config.getNode("schedulers", name, "next").setValue(next);

			configManager.save();

			if (worldName.equalsIgnoreCase("all")) {
				for (WorldProperties properties : Main.getGame().getServer().getAllWorldProperties()) {
					new Zip(properties.getWorldName()).save();
				}
			} else {
				new Zip(worldName).save();
			}
		}).submit(getPlugin());
	}
}