package com.gmail.trentech.worldbackup.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;

public class Zip {

	String worldName;
	File backupDir;
	File worldDir;
	
	public Zip(String worldName){
		this.worldName = worldName;
		this.backupDir = new File("config" + File.separator + Resource.ID.toLowerCase() + File.separator + "backups" + File.separator + this.worldName);
		
    	if (!this.backupDir.isDirectory()) {
    		this.backupDir.mkdirs();
    	}
    	
		File savesDir = Main.getGame().getSavesDirectory().toFile();

		String defaultWorld = Main.getGame().getServer().getDefaultWorldName();
		
		if(worldName.equalsIgnoreCase(defaultWorld)){
			this.worldDir = new File(savesDir, this.worldName);
		}else{
			this.worldDir = new File(savesDir, defaultWorld + File.separator + this.worldName);
		}
	}
	
	public void save(){
		Main.getLog().info("Backing up " + this.worldName);

		for(Player player : Main.getGame().getServer().getOnlinePlayers()){
			if(!player.hasPermission("worldbackup.notify")){
				continue;
			}
			
			player.sendMessage(Text.of(TextColors.GREEN, "[World Backup] ", TextColors.YELLOW, "backing up ", this.worldName, ". There may be lag."));
		}
		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

		String zipFile = this.backupDir.getAbsolutePath() + File.separator + this.worldName + "_" + formatter.format(Instant.now()) + ".zip";

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
			addDir(this.worldDir, zipOutputStream);
			zipOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		deleteOld();
	}

	private void deleteOld(){
		List<File> backups = Arrays.asList(this.backupDir.listFiles());

		Collections.sort(backups, new FileComparator());

		int keep = new ConfigManager().getConfig().getNode("settings", "keep").getInt();

		if(backups.size() > keep){
			int run = backups.size() - keep;

			for(int i = 0; i < run - 1; i++){
				backups.get(i).delete();
			}
		}
	}
	
	private void addDir(File directory, ZipOutputStream zipOutputStream) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[1024];

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				String name = files[i].getName();
				if(!Main.getGame().getServer().getWorldProperties(name).isPresent()){
					addDir(files[i], zipOutputStream);
				}
				continue;
			}
			
			FileInputStream fileInputStream = new FileInputStream(files[i]);
			
			String relativePath = files[i].getAbsolutePath().replace(Main.getGame().getSavesDirectory().toFile().getAbsolutePath(), "").replace(" ", "")
					.replace(File.separator + Main.getGame().getServer().getDefaultWorldName() + File.separator, "")
					.replace(this.worldName + File.separator, "");

			zipOutputStream.putNextEntry(new ZipEntry(relativePath));
			
			Main.getLog().info(relativePath);
			
			int length;

			while ((length = fileInputStream.read(buffer)) > 0) {
				zipOutputStream.write(buffer, 0, length);
			}

			zipOutputStream.closeEntry();
			fileInputStream.close();
		}
	}
}
