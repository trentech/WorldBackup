package com.gmail.trentech.worldbackup.commands;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.data.Zip;
import com.gmail.trentech.worldbackup.utils.Help;

public class CMDBackup implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!args.hasAny("source")) {
			Builder pages = Main.getGame().getServiceManager().provide(PaginationService.class).get().builder();

			pages.title(Text.builder().color(TextColors.DARK_GREEN).append(Text.of(TextColors.GREEN, "Commands")).build());

			List<Text> list = new ArrayList<>();

			list.add(Text.of(TextColors.YELLOW, " /backup <world>"));
			list.add(Text.of(TextColors.YELLOW, " /backup all"));

			if (src.hasPermission("worldbackup.cmd.backup.create")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information "))).onClick(TextActions.executeCallback(Help.getHelp("create"))).append(Text.of(" /backup create")).build());
			}
			if (src.hasPermission("worldbackup.cmd.backup.remove")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information "))).onClick(TextActions.executeCallback(Help.getHelp("remove"))).append(Text.of(" /backup remove")).build());
			}
			if (src.hasPermission("worldbackup.cmd.backup.list")) {
				list.add(Text.builder().color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Click command for more information "))).onClick(TextActions.executeCallback(Help.getHelp("list"))).append(Text.of(" /backup list")).build());
			}

			pages.contents(list);

			pages.sendTo(src);

			return CommandResult.success();
		}
		String source = args.<String> getOne("source").get();

		if (!source.equalsIgnoreCase("all") && !Main.getGame().getServer().getWorldProperties(source).isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " does not exist"));
			return CommandResult.empty();
		}

		if (source.equalsIgnoreCase("all")) {
			for (WorldProperties properties : Main.getGame().getServer().getAllWorldProperties()) {
				new Zip(properties.getWorldName()).save();
			}
		} else {
			new Zip(source).save();
		}

		return CommandResult.success();
	}
}