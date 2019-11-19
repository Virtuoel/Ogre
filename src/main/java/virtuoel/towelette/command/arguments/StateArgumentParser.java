package virtuoel.towelette.command.arguments;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.CommandSource;
import net.minecraft.state.PropertyContainer;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Property;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import virtuoel.towelette.api.LayerData;

public class StateArgumentParser
{
	public static final DynamicCommandExceptionType INVALID_BLOCK_ID_EXCEPTION = new DynamicCommandExceptionType(arg ->
	{
		return new TranslatableText("argument.state.id.invalid", arg);
	});
	public static final Dynamic2CommandExceptionType UNKNOWN_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((arg1, arg2) ->
	{
		return new TranslatableText("argument.state.property.unknown", arg1, arg2);
	});
	public static final Dynamic2CommandExceptionType DUPLICATE_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((arg1, arg2) ->
	{
		return new TranslatableText("argument.state.property.duplicate", arg2, arg1);
	});
	public static final Dynamic3CommandExceptionType INVALID_PROPERTY_EXCEPTION = new Dynamic3CommandExceptionType((arg1, arg2, arg3) ->
	{
		return new TranslatableText("argument.state.property.invalid", arg1, arg3, arg2);
	});
	public static final Dynamic2CommandExceptionType EMPTY_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((arg1, arg2) ->
	{
		return new TranslatableText("argument.state.property.novalue", arg1, arg2);
	});
	public static final SimpleCommandExceptionType UNCLOSED_PROPERTIES_EXCEPTION = new SimpleCommandExceptionType(
		new TranslatableText("argument.state.property.unclosed")
	);
	private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
	private final StringReader reader;
	private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
	private Identifier id = new Identifier("");
	private StateFactory<?, ?> stateFactory;
	private PropertyContainer<?> state;
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;
	
	public StateArgumentParser(StringReader reader)
	{
		this.suggestions = SUGGEST_DEFAULT;
		this.reader = reader;
	}
	
	public Map<Property<?>, Comparable<?>> getStateProperties()
	{
		return this.properties;
	}
	
	@Nullable
	public PropertyContainer<?> getState()
	{
		return this.state;
	}
	
	@SuppressWarnings("rawtypes")
	private LayerData layer;
	
	public <O, S extends PropertyContainer<S>> StateArgumentParser parse(LayerData<O, S> layer) throws CommandSyntaxException
	{
		this.layer = layer;
		
		this.suggestions = this::suggestId;
		this.parseId();
		this.suggestions = this::suggestSnbtOrBlockProperties;
		if (this.reader.canRead() && this.reader.peek() == '[')
		{
			this.parseBlockProperties();
			this.suggestions = SUGGEST_DEFAULT;
		}
		
		return this;
	}
	
	private CompletableFuture<Suggestions> suggestBlockPropertiesOrEnd(SuggestionsBuilder builder)
	{
		if (builder.getRemaining().isEmpty())
		{
			builder.suggest(String.valueOf(']'));
		}
		
		return this.suggestBlockProperties(builder);
	}
	
	private CompletableFuture<Suggestions> suggestBlockProperties(SuggestionsBuilder builder)
	{
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		Iterator<Property<?>> iter = this.state.getEntries().keySet().iterator();
		
		while (iter.hasNext())
		{
			Property<?> property = iter.next();
			if (!this.properties.containsKey(property) && property.getName().startsWith(remaining))
			{
				builder.suggest(property.getName() + '=');
			}
		}
		
		return builder.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestEqualsCharacter(SuggestionsBuilder builder)
	{
		if (builder.getRemaining().isEmpty())
		{
			builder.suggest(String.valueOf('='));
		}
		
		return builder.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestCommaOrEnd(SuggestionsBuilder builder)
	{
		if (builder.getRemaining().isEmpty())
		{
			builder.suggest(String.valueOf(']'));
		}
		
		if (builder.getRemaining().isEmpty() && this.properties.size() < this.state.getEntries().size())
		{
			builder.suggest(String.valueOf(','));
		}
		
		return builder.buildFuture();
	}
	
	private static <T extends Comparable<T>> SuggestionsBuilder suggestPropertyValues(SuggestionsBuilder builder, Property<T> property)
	{
		Iterator<T> iter = property.getValues().iterator();
		
		while (iter.hasNext())
		{
			T value = iter.next();
			if (value instanceof Integer)
			{
				builder.suggest((Integer) value);
			}
			else
			{
				builder.suggest(property.getName(value));
			}
		}
		
		return builder;
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Suggestions> suggestSnbtOrBlockProperties(SuggestionsBuilder builder)
	{
		if (builder.getRemaining().isEmpty())
		{
			if (!layer.getManager(this.state).getProperties().isEmpty())
			{
				builder.suggest(String.valueOf('['));
			}
			/* // TODO
			if (this.state.getBlock().hasBlockEntity())
			{
				builder.suggest(String.valueOf('{'));
			}*/
		}
		
		return builder.buildFuture();
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Suggestions> suggestId(SuggestionsBuilder builder)
	{
		return CommandSource.suggestIdentifiers(layer.getRegistry().getIds(), builder);
	}
	
	@SuppressWarnings("unchecked")
	public void parseId() throws CommandSyntaxException
	{
		int pos = this.reader.getCursor();
		this.id = Identifier.fromCommandInput(this.reader);
		Optional<?> data = layer.getRegistry().getOrEmpty(this.id);
		
		Object entry = data.orElseThrow(() ->
		{
			this.reader.setCursor(pos);
			return INVALID_BLOCK_ID_EXCEPTION.createWithContext(this.reader, this.id.toString());
		});
		
		this.stateFactory = layer.getManager(entry);
		this.state = layer.getDefaultState(entry);
	}
	
	public void parseBlockProperties() throws CommandSyntaxException
	{
		this.reader.skip();
		this.suggestions = this::suggestBlockPropertiesOrEnd;
		this.reader.skipWhitespace();
		
		while (this.reader.canRead() && this.reader.peek() != ']')
		{
			this.reader.skipWhitespace();
			int pos = this.reader.getCursor();
			String name = this.reader.readString();
			Property<?> property = this.stateFactory.getProperty(name);
			if (property == null)
			{
				this.reader.setCursor(pos);
				throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), name);
			}
			
			if (this.properties.containsKey(property))
			{
				this.reader.setCursor(pos);
				throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), name);
			}
			
			this.reader.skipWhitespace();
			this.suggestions = this::suggestEqualsCharacter;
			if (this.reader.canRead() && this.reader.peek() == '=')
			{
				this.reader.skip();
				this.reader.skipWhitespace();
				this.suggestions = builder ->
				{
					return suggestPropertyValues(builder, property).buildFuture();
				};
				int pos2 = this.reader.getCursor();
				this.parsePropertyValue(property, this.reader.readString(), pos2);
				this.suggestions = this::suggestCommaOrEnd;
				this.reader.skipWhitespace();
				if (!this.reader.canRead())
				{
					continue;
				}
				
				if (this.reader.peek() == ',')
				{
					this.reader.skip();
					this.suggestions = this::suggestBlockProperties;
					continue;
				}
				
				if (this.reader.peek() != ']')
				{
					throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
				}
				break;
			}
			
			throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), name);
		}
		
		if (this.reader.canRead())
		{
			this.reader.skip();
		}
		else
		{
			throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
		}
	}
	
	private <T extends Comparable<T>> void parsePropertyValue(Property<T> property, String valueName, int pos) throws CommandSyntaxException
	{
		Optional<T> value = property.getValue(valueName);
		if (value.isPresent())
		{
			this.state = (PropertyContainer<?>) this.state.with(property, value.get());
			this.properties.put(property, value.get());
		}
		else
		{
			this.reader.setCursor(pos);
			throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), property.getName(), valueName);
		}
	}
	
	public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder)
	{
		return this.suggestions.apply(builder.createOffset(this.reader.getCursor()));
	}
}
