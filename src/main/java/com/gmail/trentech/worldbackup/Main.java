package com.gmail.trentech.worldbackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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
import com.google.inject.Inject;

import me.flibio.updatifier.Updatifier;
import ninja.leaping.configurate.ConfigurationNode;

@Updatifier(repoName = Resource.NAME, repoOwner = Resource.AUTHOR, version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, description = Resource.DESCRIPTION, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class Main {

	@Inject @ConfigDir(sharedRoot = false)
    private Path path;

	@Inject
	private Logger log;

	private static PluginContainer plugin;
	private static Main instance;
	
	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		plugin = Sponge.getPluginManager().getPlugin(Resource.ID).get();
		instance = this;

		try {			
			Files.createDirectories(path);		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Listener
	public void onInitialization(GameInitializationEvent event) {
		Sponge.getCommandManager().register(this, new CommandManager().cmdBackup, "backup");
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
					for (WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
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

	public Logger getLog() {
		return log;
	}

	public Path getPath() {
		return path;
	}
	
	public static PluginContainer getPlugin() {
		return plugin;
	}
	
	public static Main instance() {
		return instance;
	}
	
	public void createTask(String name, String worldName, long interval) {
		ConfigManager configManager = ConfigManager.get();
		ConfigurationNode config = configManager.getConfig();

		long newInterval = config.getNode("schedulers", name, "interval").getLong();

		Sponge.getScheduler().createTaskBuilder().delay(interval, TimeUnit.SECONDS).name(name).execute(t -> {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, (int) interval);

			Date date = calendar.getTime();

			createTask(name, worldName, newInterval);

			String next = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

			config.getNode("schedulers", name, "next").setValue(next);

			configManager.save();

			if (worldName.equalsIgnoreCase("all")) {
				for (WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
					new Zip(properties.getWorldName()).save();
				}
			} else {
				new Zip(worldName).save();
			}
		}).submit(getPlugin());
	}
	
	public String getReadableTime(long interval) {
		long weeks = interval / 604800;
		long wRemainder = interval % 604800;
		long days = wRemainder / 86400;
		long dRemainder = wRemainder % 86400;
		long hours = dRemainder / 3600;
		long hRemainder = dRemainder % 3600;
		long minutes = hRemainder / 60;
		long seconds = hRemainder % 60;

		String time = null;

		if (weeks > 0) {
			String wks = " Weeks";
			if (weeks == 1) {
				wks = " Week";
			}
			time = weeks + wks;
		}
		if (days > 0) {
			String dys = " Days";
			if (days == 1) {
				dys = " Day";
			}
			if (time != null) {
				time = time + ", " + days + dys;
			} else {
				time = days + dys;
			}
		}
		if (hours > 0) {
			String hrs = " Hours";
			if (hours == 1) {
				hrs = " Hour";
			}
			if (time != null) {
				time = time + ", " + hours + hrs;
			} else {
				time = hours + hrs;
			}
		}
		if (minutes > 0) {
			String min = " Minutes";
			if (minutes == 1) {
				min = " Minute";
			}
			if (time != null) {
				time = time + ", " + minutes + min;
			} else {
				time = minutes + min;
			}
		}
		if (seconds > 0) {
			String sec = " Seconds";
			if (seconds == 1) {
				sec = " Second";
			}
			if (time != null) {
				time = time + ", " + seconds + sec;
			} else {
				time = seconds + sec;
			}
		}
		return time;
	}
}