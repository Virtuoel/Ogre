package virtuoel.towelette.command.arguments;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.State;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class LayerArgumentType implements ArgumentType<LayerData<?, ?>>
{
	private static final Collection<String> EXAMPLES =
		Stream.of(LayerRegistrar.BLOCK, LayerRegistrar.FLUID)
		.map(LayerRegistrar.LAYERS::getId)
		.map(Identifier::toString)
		.collect(Collectors.toList());
	
	public static final DynamicCommandExceptionType INVALID_LAYER_EXCEPTION = new DynamicCommandExceptionType(arg ->
	{
		return new TranslatableText("argument.layer.invalid", arg);
	});
	
	@Override
	public LayerData<?, ?> parse(StringReader reader) throws CommandSyntaxException
	{
		final Identifier id = Identifier.fromCommandInput(reader);
		return LayerRegistrar.LAYERS.getOrEmpty(id).orElseThrow(() ->
		{
			return INVALID_LAYER_EXCEPTION.createWithContext(reader, id);
		});
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return CommandSource.suggestIdentifiers(LayerRegistrar.LAYERS.getIds(), builder);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
	
	public static LayerArgumentType layer()
	{
		return new LayerArgumentType();
	}
	
	@SuppressWarnings("unchecked")
	public static <O, S extends State<S>> LayerData<O, S> getLayerArgument(CommandContext<ServerCommandSource> context, String name)
	{
		return context.getArgument(name, LayerData.class);
	}
}
