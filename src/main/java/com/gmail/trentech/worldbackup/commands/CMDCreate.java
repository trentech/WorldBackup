package com.gmail.trentech.worldbackup.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.Main;
import com.gmail.trentech.worldbackup.data.BackupData;
import com.gmail.trentech.worldbackup.utils.Help;
import com.gmail.trentech.worldbackup.utils.Utils;

public class CMDCreate implements CommandExecutor {

	public CMDCreate() {
		Help help = new Help("create", "create", " Create a scheduled world backup");
		help.setSyntax(" /backup create <source> <interval> [delay]\n /b c <source> <interval> [delay]");
		help.setExample(" /backup create world 30m\n  /backup create all 30m 5m");
		help.save();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!args.hasAny("source")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <source> <interval> [delay]"));
			return CommandResult.empty();
		}
		String source = args.<String> getOne("source").get();

		Optional<BackupData> optionalBackupData = BackupData.get(source);

		if (optionalBackupData.isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " already exists"));
			return CommandResult.empty();
		}

		if (!source.equalsIgnoreCase("all") && !Main.getGame().getServer().getWorldProperties(source).isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " does not exist"));
			return CommandResult.empty();
		}

		if (!args.hasAny("interval")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <source> <interval> [delay]"));
			return CommandResult.empty();
		}
		String interval = args.<String> getOne("interval").get();

		Optional<Integer> optionalSeconds = Utils.getTimeInSeconds(interval);

		if (!optionalSeconds.isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid time"));
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <source> <interval> [delay]"));
			return CommandResult.empty();
		}
		int seconds = optionalSeconds.get();

		if (args.hasAny("delay")) {
			Optional<Integer> optionalDelay = Utils.getTimeInSeconds(args.<String> getOne("delay").get());

			if (!optionalDelay.isPresent()) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid delay"));
				src.sendMessage(Text.of(TextColors.YELLOW, "/backup create <source> <interval> [delay]"));
				return CommandResult.empty();
			}
			seconds = optionalDelay.get();
		}

		BackupData backupData = new BackupData(source, seconds);
		backupData.start(seconds);

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup created"));

		return CommandResult.success();
	}
}