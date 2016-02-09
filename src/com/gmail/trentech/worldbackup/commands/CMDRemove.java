package com.gmail.trentech.worldbackup.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.worldbackup.data.BackupData;
import com.gmail.trentech.worldbackup.utils.Help;

public class CMDRemove implements CommandExecutor {

	public CMDRemove(){
		Help help = new Help("remove", "remove", " Remove an existing scheduled world backup");
		help.setSyntax(" /backup remove <name>\n /b r <name>");
		help.setExample(" /backup remove MyTask");
		help.save();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(!args.hasAny("source")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/backup <name> <world> <time>"));
			return CommandResult.empty();
		}
		String source = args.<String>getOne("source").get();

		Optional<BackupData> optionalBackupData = BackupData.get(source);
		
		if(!optionalBackupData.isPresent()){
			src.sendMessage(Text.of(TextColors.DARK_RED, source, " does not exist"));
			return CommandResult.empty();
		}
		
		optionalBackupData.get().delete();
		
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Scheduled backup removed"));
		
		return CommandResult.success();
	}
}