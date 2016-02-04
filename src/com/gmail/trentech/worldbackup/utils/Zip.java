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

import com.gmail.trentech.worldbackup.Main;

public class Zip {

	public static void save(String worldName){
		Main.getLog().info("Backing up " + worldName);
		
		File savesDir = Main.getGame().getSavesDirectory().toFile();
		File worldDir;
		
		String defaultWorld = Main.getGame().getServer().getDefaultWorldName();
		
		if(worldName.equalsIgnoreCase(defaultWorld)){
			worldDir = new File(savesDir, worldName);
		}else{
			worldDir = new File(savesDir, defaultWorld + File.separator + worldName);
		}

		File backupDir = new File("config" + File.separator + Resource.ID.toLowerCase() + File.separator + "backups" + File.separator + worldName);
		
    	if (!backupDir.isDirectory()) {
    		backupDir.mkdirs();
    	}
    	
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

		String zipFile = backupDir.getAbsolutePath() + File.separator + worldName + "_" + formatter.format(Instant.now()) + ".zip";

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
			addDir(worldName, worldDir, zipOutputStream);
			zipOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<File> backups = Arrays.asList(backupDir.listFiles());

		Collections.sort(backups, new FileComparator());

		int keep = new ConfigManager().getConfig().getNode("settings", "keep").getInt();

		if(backups.size() > keep){
			int run = backups.size() - keep;

			for(int i = 0; i < run - 1; i++){
				backups.get(i).delete();
			}
		}
	}

	private static void addDir(String worldName, File directory, ZipOutputStream zipOutputStream) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[1024];

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				String name = files[i].getName();
				if(!Main.getGame().getServer().getWorldProperties(name).isPresent()){
					addDir(worldName, files[i], zipOutputStream);
				}
				continue;
			}
			
			FileInputStream fileInputStream = new FileInputStream(files[i]);
			
			String relativePath = files[i].getAbsolutePath().replace(Main.getGame().getSavesDirectory().toFile().getAbsolutePath(), "").replace(" ", "")
					.replace(File.separator + Main.getGame().getServer().getDefaultWorldName() + File.separator, "")
					.replace(worldName + File.separator, "");

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
