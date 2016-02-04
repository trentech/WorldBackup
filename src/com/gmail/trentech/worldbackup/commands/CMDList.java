package com.gmail.trentech.worldbackup.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.ConfigManager;
import com.gmail.trentech.worldbackup.utils.Help;
import com.gmail.trentech.worldbackup.utils.Utils;

import ninja.leaping.configurate.ConfigurationNode;

public class CMDList implements CommandExecutor {

	public CMDList(){
		Help help = new Help("list", "list", " List all scheduled world backups");
		help.setSyntax(" /backup list\n /b l");
		help.setExample(" /backup list");
		help.save();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		PaginationBuilder pages = Main.getGame().getServiceManager().provide(PaginationService.class).get().builder();
		
		pages.title(Text.builder().color(TextColors.DARK_GREEN).append(Text.of(TextColors.GREEN, "Backups")).build());

		List<Text> list = new ArrayList<>();

		ConfigurationNode config = new ConfigManager().getConfig();
		
    	ConfigurationNode schedulers = config.getNode("schedulers");
    	
		for(Entry<Object, ? extends ConfigurationNode> entry : schedulers.getChildrenMap().entrySet()){
			String name = entry.getKey().toString();
			
			ConfigurationNode node = schedulers.getNode(name);
			
			String worldName = node.getNode("world").getString();
			String next = node.getNode("next").getString();
			int delay = node.getNode("interval").getInt();
			
			list.add(Text.of(TextColors.GREEN, "Name: ", TextColors.WHITE, name));
			list.add(Text.of(TextColors.GREEN, "  - World: ", TextColors.WHITE, worldName));
			list.add(Text.of(TextColors.GREEN, "  - Interval: ", TextColors.WHITE, Utils.getReadableTime(delay)));
			list.add(Text.of(TextColors.GREEN, "  - Next Run: ", TextColors.WHITE, next));
		}

		if(list.isEmpty()){
			list.add(Text.of(TextColors.YELLOW, " No scheduled backups"));
		}
		
		pages.contents(list);
		
		pages.sendTo(src);
		
		return CommandResult.success();
	}
}