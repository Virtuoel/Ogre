package virtuoel.towelette.command.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.arguments.serialize.ArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.PacketByteBuf;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class StateArgumentType<O, S extends PropertyContainer<S>> implements ArgumentType<StateArgument<O, S>>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");
	
	public static <O, S extends PropertyContainer<S>> StateArgumentType<O, S> create(LayerData<O, S> layer)
	{
		return new StateArgumentType<O, S>(layer);
	}
	
	final LayerData<O, S> layer;
	
	public StateArgumentType(LayerData<O, S> layer)
	{
		this.layer = layer;
	}
	
	public LayerData<O, S> getLayer()
	{
		return layer;
	}
	
	@Override
	public StateArgument<O, S> parse(StringReader reader) throws CommandSyntaxException
	{
		final StateArgumentParser<O, S> parser = new StateArgumentParser<O, S>(reader, layer).parse();
		return new StateArgument<O, S>(layer, parser.getState(), parser.getStateProperties().keySet());
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends PropertyContainer<S>> StateArgument<O, S> getArgument(CommandContext<ServerCommandSource> context, String name)
	{
		return context.getArgument(name, StateArgument.class);
	}
	
	@Override
	public <U> CompletableFuture<Suggestions> listSuggestions(CommandContext<U> context, SuggestionsBuilder builder)
	{
		final StringReader reader = new StringReader(builder.getInput());
		reader.setCursor(builder.getStart());
		final StateArgumentParser<O, S> parser = new StateArgumentParser<O, S>(reader, layer);
		
		try
		{
			parser.parse();
		}
		catch (CommandSyntaxException e)
		{
			
		}
		
		return parser.getSuggestions(builder);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
	
	public static class Serializer<O, S extends PropertyContainer<S>> implements ArgumentSerializer<StateArgumentType<O, S>>
	{
		@Override
		public void toPacket(StateArgumentType<O, S> argType, PacketByteBuf buffer)
		{
			buffer.writeIdentifier(LayerRegistrar.LAYERS.getId(argType.getLayer()));
		}
		
		@Override
		public StateArgumentType<O, S> fromPacket(PacketByteBuf buffer)
		{
			return new StateArgumentType<O, S>(LayerRegistrar.getLayerData(buffer.readIdentifier()));
		}
		
		@Override
		public void toJson(StateArgumentType<O, S> argType, JsonObject json)
		{
			json.addProperty("id", LayerRegistrar.LAYERS.getId(argType.getLayer()).toString());
		}
	}
}
