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

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Help;
import com.gmail.trentech.worldbackup.utils.Utils;

import ninja.leaping.configurate.ConfigurationNode;

public class CMDCreate implements CommandExecutor {

	public CMDCreate(){
		Help help = new Help("create", "create", " Create a scheduled world backup");
		help.setSyntax(" /backup create <name> <source> <interval> [delay]\n /b c <name> <source> <interval> [delay]");
		help.setExample(" /backup create MyTask world 30m\n  /backup create MyTask all 30m 5m\n /backup create MyTask server 1d,6h,5m,10s");
		help.save();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!args.hasAny("name")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <name> <source> <interval> [delay]"));
			return CommandResult.empty();
		}
		String name = args.<String>getOne("name").get();

		ConfigManager configManager = new ConfigManager();
		ConfigurationNode config = configManager.getConfig();
		
		if(!config.getNode("schedulers", name).isVirtual()){
			src.sendMessage(Text.of(TextColors.DARK_RED, name, " already exists"));
			return CommandResult.empty();
		}
		
		if(!args.hasAny("source")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <name> <source> <interval>"));
			return CommandResult.empty();
		}
		String source = args.<String>getOne("source").get();
		
		if(!source.equalsIgnoreCase("server") && !source.equalsIgnoreCase("all") && !Main.getGame().getServer().getWorldProperties(source).isPresent()){
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " does not exist"));
			return CommandResult.empty();
		}
		
		if(!args.hasAny("interval")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <name> <source> <interval>"));
			return CommandResult.empty();
		}
		String interval = args.<String>getOne("interval").get();
		
		Optional<Integer> optionalSeconds = Utils.getTimeInSeconds(interval);
		
		if(!optionalSeconds.isPresent()){
			src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid time"));
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <name> <source> <interval>"));
			return CommandResult.empty();
		}		
		int seconds = optionalSeconds.get();
		
		config.getNode("schedulers", name, "source").setValue(source);
		config.getNode("schedulers", name, "interval").setValue(seconds);
		
		if(args.hasAny("delay")) {
			Optional<Integer> optionalDelay = Utils.getTimeInSeconds(args.<String>getOne("delay").get());
			
			if(!optionalDelay.isPresent()){
				src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid delay"));
				src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <name> <source> <interval> [delay]"));
				return CommandResult.empty();
			}
			seconds = optionalDelay.get();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, seconds);
		Date date = calendar.getTime();

		String next = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

		config.getNode("schedulers", name, "next").setValue(next);
		
		configManager.save();
		
		Main.createTask(name, source, seconds);
		
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup created"));
		
		return CommandResult.success();
	}
}