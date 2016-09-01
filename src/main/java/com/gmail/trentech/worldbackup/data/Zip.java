package com.gmail.trentech.worldbackup.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;

public class Zip {

	private String worldName;
	private File backupDir;
	private File worldDir;

	public Zip(String worldName) {
		this.worldName = worldName;

		this.backupDir = new File(ConfigManager.get().getConfig().getNode("settings", "backup_directory").getString());

		if (!this.backupDir.isDirectory()) {
			this.backupDir.mkdirs();
		}

		File savesDir = Sponge.getGame().getSavesDirectory().toFile();

		String defaultWorld = Sponge.getServer().getDefaultWorldName();

		if (worldName.equalsIgnoreCase(defaultWorld)) {
			this.worldDir = new File(savesDir, this.worldName);
		} else {
			this.worldDir = new File(savesDir, defaultWorld + File.separator + this.worldName);
		}
	}

	public void save() {
		Main.instance().getLog().info("Backing up " + this.worldName);

		Collection<Player> players = Sponge.getServer().getOnlinePlayers();

		for (Player player : players) {
			if (!player.hasPermission("worldbackup.notify")) {
				continue;
			}

			player.sendMessage(Text.of(TextColors.GREEN, "[World Backup] ", TextColors.YELLOW, "Backing up ", this.worldName, ". There may be lag."));
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

		String zipName = this.worldName + "_" + formatter.format(Instant.now()) + ".zip";
		String zipFile = this.backupDir.getAbsolutePath() + File.separator + zipName;

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
			addDir(this.worldDir, zipOutputStream, zipName);
			zipOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Player player : players) {
			if (!player.hasPermission("worldbackup.notify")) {
				continue;
			}

			player.sendMessage(Text.of(TextColors.GREEN, "[World Backup] ", TextColors.YELLOW, "Backup complete"));
		}

		Main.instance().getLog().info("Backup complete");
	}

	public void clean(int keep) {
		List<File> backups = Arrays.asList(this.backupDir.listFiles());

		Collections.sort(backups, new FileComparator());

		if (backups.size() > keep) {
			int run = backups.size() - keep;

			for (int i = 0; i < run; i++) {
				backups.get(i).delete();
			}
		}
	}

	private void addDir(File directory, ZipOutputStream zipOutputStream, String zipName) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[1024];

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				String worldName = files[i].getName();

				if (!Sponge.getServer().getWorldProperties(worldName).isPresent()) {
					addDir(files[i], zipOutputStream, zipName);
				}

				continue;
			}

			FileInputStream fileInputStream = new FileInputStream(files[i]);

			Path absolutePath = Paths.get(files[i].getAbsolutePath());
			Path backupPath = Paths.get(this.backupDir.getAbsolutePath());
			String relativePath = backupPath.relativize(absolutePath).toString().replaceAll("\\.\\.\\" + File.separator, "").replaceFirst("\\.\\" + File.separator, "").replace(this.worldName + File.separator, "");

			zipOutputStream.putNextEntry(new ZipEntry(relativePath));

			int length;

			try {
				while ((length = fileInputStream.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, length);
				}
				Main.instance().getLog().info(relativePath + " -> " + zipName);
			} catch (Exception e) {
				Main.instance().getLog().warn("Skipped: " + relativePath);
			}

			zipOutputStream.closeEntry();
			fileInputStream.close();
		}
	}
	
	public class FileComparator implements Comparator<File> {

		public int compare(File f0, File f1) {
			long date1 = f0.lastModified();
			long date2 = f1.lastModified();

			if (date1 > date2)
				return 1;
			else if (date2 > date1)
				return -1;

			return 0;
		}
	}
}
