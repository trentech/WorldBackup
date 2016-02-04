package com.gmail.trentech.worldbackup.commands;

import java.util.Set;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Help;

import ninja.leaping.configurate.ConfigurationNode;

public class CMDRemove implements CommandExecutor {

	public CMDRemove(){
		Help help = new Help("remove", "remove", " Remove an existing scheduled world backup");
		help.setSyntax(" /backup remove <name>\n /b r <name>");
		help.setExample(" /backup remove MyTask");
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
		
		if(config.getNode("schedulers", name).isVirtual()){
			src.sendMessage(Text.of(TextColors.DARK_RED, name, " does not exist"));
			return CommandResult.empty();
		}
		
		config.getNode("schedulers").removeChild(name);
		
		configManager.save();
		
		Set<Task> tasks = Main.getGame().getScheduler().getScheduledTasks();
		
		for(Task task : tasks){
			if(task.getName().equalsIgnoreCase(name)){
				task.cancel();
				break;
			}		
		}
		
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup removed"));
		
		return CommandResult.success();
	}
}