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

public class FluidArgumentType implements ArgumentType<FluidArgument>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");
	
	public static FluidArgumentType create()
	{
		return new FluidArgumentType();
	}
	
	@Override
	public FluidArgument parse(StringReader stringReader_1) throws CommandSyntaxException
	{
		FluidArgumentParser FluidArgumentParser_1 = new FluidArgumentParser(stringReader_1, false).parse(true);
		return new FluidArgument(FluidArgumentParser_1.getFluidState(), FluidArgumentParser_1.getFluidProperties().keySet());
	}
	
	public static FluidArgument getArgument(CommandContext<ServerCommandSource> commandContext_1, String string_1)
	{
		return commandContext_1.getArgument(string_1, FluidArgument.class);
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext_1, SuggestionsBuilder suggestionsBuilder_1)
	{
		StringReader stringReader_1 = new StringReader(suggestionsBuilder_1.getInput());
		stringReader_1.setCursor(suggestionsBuilder_1.getStart());
		FluidArgumentParser FluidArgumentParser_1 = new FluidArgumentParser(stringReader_1, false);
		
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
