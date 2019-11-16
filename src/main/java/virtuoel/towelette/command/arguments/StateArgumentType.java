package virtuoel.towelette.command.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import virtuoel.towelette.api.PaletteRegistrar;

public class StateArgumentType <O, S extends PropertyContainer<S>> implements ArgumentType<StateArgument<O, S>>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");
	
	public static <O, S extends PropertyContainer<S>> StateArgumentType<O, S> create()
	{
		return new StateArgumentType<O, S>(PaletteRegistrar.BLOCK_STATE);
	}
	
	public StateArgumentType(Identifier layer)
	{
		this.layer = layer;
	}
	
	Identifier layer;
	
	@Override
	public StateArgument<O, S> parse(StringReader stringReader_1) throws CommandSyntaxException
	{
		StateArgumentParser<O, S> FluidArgumentParser_1 = new StateArgumentParser<O, S>(stringReader_1, layer).parse(true);
		return new StateArgument<O, S>(layer, FluidArgumentParser_1.getState(), FluidArgumentParser_1.getProperties().keySet());
	}
	
	public static <O, S extends PropertyContainer<S>> StateArgument<O, S> getArgument(CommandContext<ServerCommandSource> commandContext_1, String string_1)
	{
		return commandContext_1.getArgument(string_1, StateArgument.class);
	}
	
	@Override
	public <U> CompletableFuture<Suggestions> listSuggestions(CommandContext<U> commandContext_1, SuggestionsBuilder suggestionsBuilder_1)
	{
		StringReader stringReader_1 = new StringReader(suggestionsBuilder_1.getInput());
		stringReader_1.setCursor(suggestionsBuilder_1.getStart());
		StateArgumentParser<O, S> FluidArgumentParser_1 = new StateArgumentParser<O, S>(stringReader_1, layer);
		
		try
		{
			FluidArgumentParser_1.parse(true);
		}
		catch(CommandSyntaxException var6)
		{
			
		}
		
		return FluidArgumentParser_1.getSuggestions(suggestionsBuilder_1);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
}
