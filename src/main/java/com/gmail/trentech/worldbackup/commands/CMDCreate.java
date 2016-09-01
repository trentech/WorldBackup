package com.gmail.trentech.worldbackup.commands;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.data.BackupData;
import com.gmail.trentech.worldbackup.utils.Help;

public class CMDCreate implements CommandExecutor {

	public CMDCreate() {
		Help help = new Help("create", "create", " Create a scheduled world backup");
		help.setSyntax(" /backup create <source> <interval> [delay]\n /b c <source> <interval> [delay]");
		help.setExample(" /backup create world 30m\n  /backup create all 30m 5m");
		help.save();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String source = args.<String> getOne("source").get();

		Optional<BackupData> optionalBackupData = BackupData.get(source);

		if (optionalBackupData.isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " already exists"));
			return CommandResult.empty();
		}

		if (!source.equalsIgnoreCase("all") && !Sponge.getServer().getWorldProperties(source).isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " does not exist"));
			return CommandResult.empty();
		}

		long interval = args.<Long> getOne("interval").get() * 60;

		BackupData backupData = new BackupData(source, interval);
		
		if (args.hasAny("delay")) {
			backupData.start(args.<Long> getOne("delay").get());
		} else {
			backupData.start(0);
		}

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup created"));

		return CommandResult.success();
	}
}