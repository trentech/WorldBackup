package com.gmail.trentech.worldbackup.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.data.BackupData;
import com.gmail.trentech.worldbackup.utils.Help;

public class CMDList implements CommandExecutor {

	public CMDList() {
		Help help = new Help("list", "list", " List all scheduled world backups");
		help.setSyntax(" /backup list\n /b l");
		help.setExample(" /backup list");
		help.save();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		List<Text> list = new ArrayList<>();

		for (BackupData backupData : BackupData.all()) {
			list.add(Text.of(TextColors.GREEN, "Source: ", TextColors.WHITE, backupData.getSource()));
			list.add(Text.of(TextColors.GREEN, "  - Interval: ", TextColors.WHITE, Main.instance().getReadableTime(backupData.getInterval())));
			list.add(Text.of(TextColors.GREEN, "  - Next Run: ", TextColors.WHITE, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(backupData.getNext())));
		}

		if (list.isEmpty()) {
			list.add(Text.of(TextColors.YELLOW, " No scheduled backups"));
		}

		if (src instanceof Player) {
			PaginationList.Builder pages = PaginationList.builder();

			pages.title(Text.builder().color(TextColors.DARK_GREEN).append(Text.of(TextColors.GREEN, "Backups")).build());

			pages.contents(list);

			pages.sendTo(src);
		} else {
			for (Text text : list) {
				src.sendMessage(text);
			}
		}

		return CommandResult.success();
	}
}