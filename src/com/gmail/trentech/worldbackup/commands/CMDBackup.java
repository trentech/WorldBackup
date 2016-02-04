package com.gmail.trentech.worldbackup.commands;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.utils.Help;
import com.gmail.trentech.worldbackup.utils.Zip;

public class CMDBackup implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!args.hasAny("world")) {
			PaginationBuilder pages = Main.getGame().getServiceManager().provide(PaginationService.class).get().builder();

			pages.title(Text.builder().color(TextColors.DARK_GREEN).append(Text.of(TextColors.AQUA, "Commands")).build());
			
			List<Text> list = new ArrayList<>();
			
			list.add(Text.of(TextColors.YELLOW, "/backup <world>\n"));
			
			if(src.hasPermission("worldbackup.cmd.backup.create")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information ")))
						.onClick(TextActions.executeCallback(Help.getHelp("create"))).append(Text.of(" /backup create")).build());
			}
			if(src.hasPermission("worldbackup.cmd.backup.remove")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information ")))
						.onClick(TextActions.executeCallback(Help.getHelp("remove"))).append(Text.of(" /backup remove")).build());
			}
			if(src.hasPermission("worldbackup.cmd.backup.list")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information ")))
						.onClick(TextActions.executeCallback(Help.getHelp("list"))).append(Text.of(" /backup list")).build());
			}

			pages.contents(list);
			
			pages.sendTo(src);

			return CommandResult.success();
		}
		String worldName = args.<WorldProperties>getOne("world").get().getWorldName();

		Zip.save(worldName);
		
		return CommandResult.success();
	}
}