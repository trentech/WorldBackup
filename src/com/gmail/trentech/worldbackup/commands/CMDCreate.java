package com.gmail.trentech.worldbackup.commands;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Help;
import com.gmail.trentech.worldbackup.utils.Utils;

import ninja.leaping.configurate.ConfigurationNode;

public class CMDCreate implements CommandExecutor {

	public CMDCreate(){
		Help help = new Help("create", "create", " Create a scheduled world backup");
		help.setSyntax(" /backup create <name> <world> <time>\n /b c <name> <world> <time>");
		help.setExample(" /backup create MyTask world 30m\n /backup create MyTask world 1d,6h,5m,10s");
		help.save();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!args.hasAny("name")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup <name> <world> <time>"));
			return CommandResult.empty();
		}
		String name = args.<String>getOne("name").get();

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();
		
		if(!config.getNode("schedulers", name).isVirtual()){
			src.sendMessage(Text.of(TextColors.DARK_RED, name, " already exists"));
			return CommandResult.empty();
		}
		
		if(!args.hasAny("world")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup <world>"));
			return CommandResult.empty();
		}
		String worldName = args.<WorldProperties>getOne("world").get().getWorldName();
		
		if(!args.hasAny("time")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup <name> <world> <time>"));
			return CommandResult.empty();
		}
		String time = args.<String>getOne("time").get();
		
		Optional<Integer> optionalTime = Utils.getTimeInSeconds(time);
		
		if(!Utils.getTimeInSeconds(time).isPresent()){
			src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid time"));
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup <name> <world> <time>"));
			return CommandResult.empty();
		}
		
		int seconds = optionalTime.get();
		
		config.getNode("schedulers", name, "world").setValue(worldName);
		config.getNode("schedulers", name, "delay").setValue(seconds);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, seconds);
		Date date = calendar.getTime();

		String next = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

		config.getNode("schedulers", name, "next").setValue(next);
		
		configManager.save();
		
		Main.createTask(name, worldName, seconds);
		
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup created"));
		
		return CommandResult.success();
	}
}