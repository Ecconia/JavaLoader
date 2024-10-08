package io.github.pieter12345.javaloader.velocity.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.velocitypowered.api.command.SimpleCommand;

import io.github.pieter12345.javaloader.core.CommandExecutor;
import io.github.pieter12345.javaloader.core.CommandExecutor.CommandSender;
import io.github.pieter12345.javaloader.core.CommandExecutor.MessageType;
import io.github.pieter12345.javaloader.core.ProjectManager;
import io.github.pieter12345.javaloader.core.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * This class represents the "/javaloaderproxy" command.
 * @author P.J.S. Kools
 */
public class JavaLoaderProxyCommand implements SimpleCommand {
	
	private final String infoPrefix;
	private final String errorPrefix;
	private final CommandExecutor commandExecutor;
	private final ProjectManager projectManager;
	
	public JavaLoaderProxyCommand(String infoPrefix, String errorPrefix,
			CommandExecutor commandExecutor, ProjectManager projectManager) {
		this.infoPrefix = infoPrefix;
		this.errorPrefix = errorPrefix;
		this.commandExecutor = commandExecutor;
		this.projectManager = projectManager;
	}
	
	@Override
	public void execute(Invocation invocation) {
		this.commandExecutor.executeCommand(new CommandSender() {
			
			@Override
			public void sendMessage(MessageType messageType, String message) {
				var wholeMessage = this.getPrefix(messageType) + message;
				invocation.source().sendMessage(LegacyComponentSerializer.legacySection().deserialize(wholeMessage));
			}
			
			@Override
			public void sendMessage(MessageType messageType, String... messages) {
				if(messages.length > 0) {
					var wholeMessage = this.getPrefix(messageType) + Utils.glueIterable(Arrays.asList(messages), (str) -> str, "\n");
					invocation.source().sendMessage(LegacyComponentSerializer.legacySection().deserialize(wholeMessage));
				}
			}
			
			public String getPrefix(MessageType messageType) {
				switch(messageType) {
					case ERROR:
						return JavaLoaderProxyCommand.this.errorPrefix;
					case INFO:
						return JavaLoaderProxyCommand.this.infoPrefix;
					default:
						throw new Error("Unimplemented "
								+ MessageType.class.getSimpleName() + ": " + messageType.name());
				}
			}
		}, invocation.arguments());
	}
	
	@Override
	public boolean hasPermission(Invocation invocation) {
		return invocation.source().hasPermission("eccsjavaloader.use");
	}
	
	@Override
	public List<String> suggest(Invocation invocation) {
		String[] args = invocation.arguments();
		String search = args.length == 0 ? "" : args[args.length - 1].toLowerCase();
		
		// TAB-complete "/javaloaderproxy <arg>".
		if(args.length <= 1) {
			List<String> ret = new ArrayList<String>();
			for(String comp : new String[] {"help", "list", "load", "unload", "recompile", "scan"}) {
				if(comp.startsWith(search)) {
					ret.add(comp);
				}
			}
			return ret;
		}
		
		// TAB-complete "/javaloaderproxy <load, unload, recompile> <arg>".
		if(args.length == 2) {
			// TAB-complete "/javaloaderproxy load <arg>".
			if(args[0].equalsIgnoreCase("load")) {
				return this.projectManager.getUnloadedProjectNames().stream()
					.filter(e -> e.toLowerCase().startsWith(search))
					.collect(Collectors.toList());
			}
			
			// TAB-complete "/javaloaderproxy unload <arg>".
			if(args[0].equalsIgnoreCase("unload")) {
				return this.projectManager.getLoadedProjectNames().stream()
					.filter(e -> e.toLowerCase().startsWith(search))
					.collect(Collectors.toList());
			}
			
			// TAB-complete "/javaloaderproxy recompile <arg>".
			if(args[0].equalsIgnoreCase("recompile")) {
				return this.projectManager.getProjectNames().stream()
					.filter(e -> e.toLowerCase().startsWith(search))
					.collect(Collectors.toList());
			}
			
			// TAB-complete "/javaloader help <arg>".
			if(args[0].equalsIgnoreCase("help")) {
				List<String> ret = new ArrayList<String>();
				for(String comp : new String[]{"help", "list", "recompile", "load", "unload", "scan"})
				{
					if(comp.toLowerCase().startsWith(search)) {
						ret.add(comp);
					}
				}
				return ret;
			}
		}
		
		// Subcommand without tabcompleter.
		return Collections.emptyList();
	}
}
