package com.gmail.trentech.worldbackup.commands;

import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandManager {

	public CommandSpec cmdCreate = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.create")
		    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))),
		    		GenericArguments.optional(GenericArguments.world(Text.of("world"))),
		    		GenericArguments.optional(GenericArguments.string(Text.of("time"))))
		    .executor(new CMDCreate())
		    .build();
	
	public CommandSpec cmdRemove = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.remove")
		    .arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
		    .executor(new CMDRemove())
		    .build();
	
	public CommandSpec cmdList = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup.list")
		    .executor(new CMDList())
		    .build();
	
	public CommandSpec cmdBackup = CommandSpec.builder()
		    .permission("worldbackup.cmd.backup")
		    .arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
		    .child(cmdCreate, "create", "c")
		    .child(cmdRemove, "remove", "r")
		    .child(cmdList, "list", "l")
		    .executor(new CMDBackup())
		    .build();
}
