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
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class StateArgumentType<O, S extends PropertyContainer<S>> implements ArgumentType<StateArgument<O, S>>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");
	
	public static <O, S extends PropertyContainer<S>> StateArgumentType<O, S> create(String id)
	{
		return create(new Identifier(id));
	}
	
	public static <O, S extends PropertyContainer<S>> StateArgumentType<O, S> create(Identifier id)
	{
		return create(LayerRegistrar.<O, S>getLayerData(id));
	}
	
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StateArgument<O, S> parse(StringReader reader) throws CommandSyntaxException
	{
		final StateArgumentParser parser = new StateArgumentParser(reader).parse(layer);
		return new StateArgument(layer, parser.getState(), parser.getStateProperties().keySet());
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
		final StateArgumentParser parser = new StateArgumentParser(reader);
		
		try
		{
			parser.parse(layer);
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
	
	@SuppressWarnings("rawtypes")
	public static class Serializer implements ArgumentSerializer<StateArgumentType>
	{
		@Override
		public void toPacket(StateArgumentType argType, PacketByteBuf buffer)
		{
			buffer.writeIdentifier(LayerRegistrar.LAYERS.getId(argType.getLayer()));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public StateArgumentType fromPacket(PacketByteBuf buffer)
		{
			return new StateArgumentType(LayerRegistrar.LAYERS.get(buffer.readIdentifier()));
		}
		
		@Override
		public void toJson(StateArgumentType argType, JsonObject json)
		{
			json.addProperty("id", LayerRegistrar.LAYERS.getId(argType.getLayer()).toString());
		}
	}
}
