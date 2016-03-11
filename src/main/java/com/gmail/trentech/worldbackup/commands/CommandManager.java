package com.gmail.trentech.worldbackup.commands;

import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandManager {

	public CommandSpec cmdCreate = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.create")
		    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("source"))),
		    		GenericArguments.optional(GenericArguments.string(Text.of("interval"))),
		    		GenericArguments.optional(GenericArguments.string(Text.of("delay"))))
		    .executor(new CMDCreate())
		    .build();
	
	public CommandSpec cmdRemove = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.remove")
		    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("source"))))
		    .executor(new CMDRemove())
		    .build();
	
	public CommandSpec cmdList = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.list")
		    .executor(new CMDList())
		    .build();
	
	public CommandSpec cmdBackup = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup")
		    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("source"))))
		    .child(cmdCreate, "create", "c")
		    .child(cmdRemove, "remove", "r")
		    .child(cmdList, "list", "l")
		    .executor(new CMDBackup())
		    .build();
}
