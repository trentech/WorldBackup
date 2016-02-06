package com.gmail.trentech.worldbackup.utils;

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
		}else if(worldName.equalsIgnoreCase("server")){
			this.worldDir = new File(".");
		}else{
			this.worldDir = new File(savesDir, defaultWorld + File.separator + this.worldName);
		}
	}
	
	public void save(){
		Main.getLog().info("Backing up " + this.worldName);

		Collection<Player> players = Main.getGame().getServer().getOnlinePlayers();
		
		for(Player player : players){
			if(!player.hasPermission("worldbackup.notify")){
				continue;
			}
			
			player.sendMessage(Text.of(TextColors.GREEN, "[World Backup] ", TextColors.YELLOW, "Backing up ", this.worldName, ". There may be lag."));
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
		
		for(Player player : players){
			if(!player.hasPermission("worldbackup.notify")){
				continue;
			}
			
			player.sendMessage(Text.of(TextColors.GREEN, "[World Backup] ", TextColors.YELLOW, "Backup complete"));
		}
	}

	private void deleteOld(){
		List<File> backups = Arrays.asList(this.backupDir.listFiles());

		Collections.sort(backups, new FileComparator());

		int keep = new ConfigManager().getConfig().getNode("settings", "keep").getInt();

		if(backups.size() > keep){
			int run = backups.size() - keep;

			for(int i = 0; i < run; i++){
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
				
				if(this.worldName.equalsIgnoreCase("server")){
					if(!files[i].getAbsolutePath().contains("config" + File.separator + Resource.ID.toLowerCase() + File.separator + "backups")){
						addDir(files[i], zipOutputStream);
					}
				}else if(!Main.getGame().getServer().getWorldProperties(name).isPresent()){
					addDir(files[i], zipOutputStream);
				}
				continue;
			}
			
			FileInputStream fileInputStream = new FileInputStream(files[i]);
			
			Path absolutePath = Paths.get(files[i].getAbsolutePath());
	        Path backupPath = Paths.get(this.backupDir.getAbsolutePath());
	        String relativePath = backupPath.relativize(absolutePath).toString().replaceAll("\\.\\.\\" + File.separator, "").replaceFirst("\\.\\" + File.separator, "").replace(this.worldName + File.separator, "");

			zipOutputStream.putNextEntry(new ZipEntry(relativePath));

			int length;

			try{
				while ((length = fileInputStream.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, length);
				}
				Main.getLog().info(relativePath);
			}catch(Exception e){
				Main.getLog().warn("Skipped: " + relativePath);
			}

			zipOutputStream.closeEntry();
			fileInputStream.close();
		}
	}
}
