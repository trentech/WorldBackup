package com.gmail.trentech.worldbackup.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;

import ninja.leaping.configurate.ConfigurationNode;

public class BackupData {

	private String source;
	private Date next;
	private long interval;
	private int keep;

	private BackupData(String source, Date next, long interval, int keep) {
		this.source = source;
		this.next = next;
		this.interval = interval;
		this.keep = keep;
	}

	public BackupData(String worldName, long interval) {
		this.source = worldName;
		this.interval = interval;
		this.keep = new ConfigManager().getConfig().getNode("settings", "keep").getInt();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, (int) interval);
		Date date = calendar.getTime();

		this.next = date;

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		config.getNode("schedulers", source, "interval").setValue(this.interval);
		config.getNode("schedulers", source, "next").setValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.next));
		config.getNode("schedulers", source, "keep").setValue(this.keep);

		configManager.save();
	}

	public String getSource() {
		return source;
	}

	public Date getNext() {
		return next;
	}

	public long getInterval() {
		return interval;
	}

	public int getKeep() {
		return keep;
	}

	public void setNext(Date date) {
		this.next = date;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String next = format.format(date);

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		config.getNode("schedulers", this.source, "next").setValue(next);
		configManager.save();
	}

	public void setInterval(long interval) {
		this.interval = interval;

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		config.getNode("schedulers", this.source, "interval").setValue(this.interval);
		configManager.save();
	}

	public void setKeep(int keep) {
		this.keep = keep;

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		config.getNode("schedulers", this.source, "keep").setValue(this.keep);
		configManager.save();
	}

	public void delete() {
		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();

		config.getNode("schedulers").removeChild(this.source);

		configManager.save();

		Set<Task> tasks = Sponge.getScheduler().getScheduledTasks();

		for (Task task : tasks) {
			if (task.getName().equalsIgnoreCase(this.source)) {
				task.cancel();
				break;
			}
		}
	}

	public void start(long interval) {
		long newInterval = this.interval;

		Sponge.getScheduler().createTaskBuilder().delay(interval, TimeUnit.SECONDS).name(this.source).execute(new Runnable() {

			@Override
			public void run() {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.SECOND, (int) newInterval);

				Date date = calendar.getTime();

				start(newInterval);

				setNext(date);

				if (source.equalsIgnoreCase("all")) {
					for (WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
						new Zip(properties.getWorldName()).save();
					}
				} else {
					Zip zip = new Zip(source);
					zip.save();
					zip.clean(keep);
				}
			}
		}).submit(Main.getPlugin());
	}

	public static Optional<BackupData> get(String source) {
		ConfigurationNode scheduler = new ConfigManager().getConfig().getNode("schedulers", source);

		if (scheduler.isVirtual()) {
			return Optional.empty();

		}

		String next = scheduler.getNode("next").getString();

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date date = null;
		try {
			date = format.parse(next);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long interval = scheduler.getNode("interval").getLong();
		int keep = scheduler.getNode("keep").getInt();

		return Optional.of(new BackupData(source, date, interval, keep));
	}

	public static List<BackupData> all() {
		ArrayList<BackupData> list = new ArrayList<>();

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();
		ConfigurationNode schedulers = config.getNode("schedulers");

		for (Entry<Object, ? extends ConfigurationNode> entry : schedulers.getChildrenMap().entrySet()) {
			String source = entry.getKey().toString();

			ConfigurationNode scheduler = schedulers.getNode(source);

			String next = scheduler.getNode("next").getString();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date date = null;
			try {
				date = format.parse(next);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			long interval = scheduler.getNode("interval").getLong();
			int keep = scheduler.getNode("keep").getInt();

			list.add(new BackupData(source, date, interval, keep));
		}

		return list;
	}
}
