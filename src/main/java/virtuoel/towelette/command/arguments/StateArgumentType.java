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
import virtuoel.towelette.api.LayerData;

public class StateArgumentType implements ArgumentType<StateArgument<?, ?>>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");
	
	public static StateArgumentType create()
	{
		return create("layer");
	}
	
	public static StateArgumentType create(String layerArgumentName)
	{
		return create(StateArgumentType::parseLayerData, layerArgumentName);
	}
	
	private static LayerData<?, ?> parseLayerData(StringReader reader) throws CommandSyntaxException
	{
		return virtuoel.towelette.api.LayerRegistrar.FLUID;
		/* // TODO very broken
		final int pos = reader.getCursor();
		
		int offset = 0;
		while(reader.peek(--offset) != ' ');
		reader.setCursor(pos + offset);
		
		final LayerData<?, ?> layer = new LayerArgumentType().parse(reader);
		
		reader.setCursor(pos);
		return layer;
		*/
	}
	
	public static StateArgumentType create(ArgumentType<LayerData<?, ?>> layerIdParser, String layerArgumentName)
	{
		return new StateArgumentType(layerIdParser, layerArgumentName);
	}
	
	final ArgumentType<LayerData<?, ?>> readerLayerFunction;
	final String layerArgumentName;
	
	public <U> StateArgumentType(ArgumentType<LayerData<?, ?>> readerLayerFunction, String layerArgumentName)
	{
		this.readerLayerFunction = readerLayerFunction;
		this.layerArgumentName = layerArgumentName;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StateArgument<?, ?> parse(StringReader reader) throws CommandSyntaxException
	{
		final LayerData<?, ?> layer = readerLayerFunction.parse(reader);
		final StateArgumentParser parser = new StateArgumentParser(reader).parse(layer);
		return new StateArgument(layer, parser.getState(), parser.getStateProperties().keySet());
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> StateArgument<O, S> getArgument(CommandContext<ServerCommandSource> context, String name)
	{
		return context.getArgument(name, StateArgument.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> CompletableFuture<Suggestions> listSuggestions(CommandContext<U> context, SuggestionsBuilder builder)
	{
		StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		StateArgumentParser parser = new StateArgumentParser(reader);
		
		try
		{ // TODO FIXME layer-sensitive parsing
			parser.parse(context.getArgument(layerArgumentName, LayerData.class));
		}
		catch(CommandSyntaxException e)
		{
			
		}
		
		return parser.getSuggestions(builder);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
}
