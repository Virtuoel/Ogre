package virtuoel.towelette.command.arguments;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
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
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;

public class StateArgumentParser<O, S extends PropertyContainer<S>>
{
	public static final DynamicCommandExceptionType INVALID_FLUID_ID_EXCEPTION = new DynamicCommandExceptionType((object_1) ->
	{
		return new TranslatableText("argument.fluid.id.invalid", new Object[] { object_1 });
	});
	public static final Dynamic2CommandExceptionType UNKNOWN_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object_1, object_2) ->
	{
		return new TranslatableText("argument.fluid.property.unknown", new Object[] { object_1, object_2 });
	});
	public static final Dynamic2CommandExceptionType DUPLICATE_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object_1, object_2) ->
	{
		return new TranslatableText("argument.fluid.property.duplicate", new Object[] { object_2, object_1 });
	});
	public static final Dynamic3CommandExceptionType INVALID_PROPERTY_EXCEPTION = new Dynamic3CommandExceptionType((object_1, object_2, object_3) ->
	{
		return new TranslatableText("argument.fluid.property.invalid", new Object[] { object_1, object_3, object_2 });
	});
	public static final Dynamic2CommandExceptionType EMPTY_PROPERTY_EXCEPTION = new Dynamic2CommandExceptionType((object_1, object_2) ->
	{
		return new TranslatableText("argument.fluid.property.novalue", new Object[] { object_1, object_2 });
	});
	public static final SimpleCommandExceptionType UNCLOSED_PROPERTIES_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.fluid.property.unclosed"));
	private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
	private final StringReader reader;
	private final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
	private Identifier id = new Identifier("");
	private StateFactory<O, S> stateFactory;
	private S state;
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;
	private PaletteData<O, S> paletteData;
	
	public StateArgumentParser(StringReader reader, Identifier layer)
	{
		this.suggestions = SUGGEST_DEFAULT;
		this.reader = reader;
		this.paletteData = PaletteRegistrar.<O, S>getPaletteData(layer);
	}
	
	public Map<Property<?>, Comparable<?>> getProperties()
	{
		return this.properties;
	}
	
	@Nullable
	public S getState()
	{
		return this.state;
	}
	
	public Identifier getFluidId()
	{
		return this.id;
	}
	
	public StateArgumentParser<O, S> parse(boolean boolean_1) throws CommandSyntaxException
	{
		this.suggestions = this::suggestId;
		this.parseId();
		this.suggestions = this::suggestMojangsonOrFluidProperties;
		if(this.reader.canRead() && this.reader.peek() == '[')
		{
			this.parseFluidProperties();
			this.suggestions = SuggestionsBuilder::buildFuture;
		}
		
		if(boolean_1 && this.reader.canRead() && this.reader.peek() == '{')
		{
			this.suggestions = SUGGEST_DEFAULT;
		}
		
		return this;
	}
	
	private CompletableFuture<Suggestions> suggestFluidPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			suggestionsBuilder_1.suggest(String.valueOf(']'));
		}
		
		return this.suggestFluidProperties(suggestionsBuilder_1);
	}
	private CompletableFuture<Suggestions> suggestFluidProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		String string_1 = suggestionsBuilder_1.getRemaining().toLowerCase(Locale.ROOT);
		
		Iterator<Property<?>> var3 = state.getEntries().keySet().iterator();
		
		while(var3.hasNext())
		{
			Property<?> property_1 = var3.next();
			if(!this.properties.containsKey(property_1) && property_1.getName().startsWith(string_1))
			{
				suggestionsBuilder_1.suggest(property_1.getName() + '=');
			}
		}
		
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestEqualsCharacter(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			suggestionsBuilder_1.suggest(String.valueOf('='));
		}
		
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestCommaOrEnd(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			suggestionsBuilder_1.suggest(String.valueOf(']'));
		}
		
		if(suggestionsBuilder_1.getRemaining().isEmpty() && this.properties.size() < this.state.getEntries().size())
		{
			suggestionsBuilder_1.suggest(String.valueOf(','));
		}
		
		return suggestionsBuilder_1.buildFuture();
	}
	
	private static <T extends Comparable<T>> SuggestionsBuilder suggestPropertyValues(SuggestionsBuilder suggestionsBuilder_1, Property<T> property_1)
	{
		Iterator<T> var2 = property_1.getValues().iterator();
		
		while(var2.hasNext())
		{
			T comparable_1 = var2.next();
			if(comparable_1 instanceof Integer)
			{
				suggestionsBuilder_1.suggest((Integer) comparable_1);
			}
			else
			{
				suggestionsBuilder_1.suggest(property_1.getName(comparable_1));
			}
		}
		
		return suggestionsBuilder_1;
	}
	
	private CompletableFuture<Suggestions> suggestMojangsonOrFluidProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			if(!paletteData.getManager(this.state).getProperties().isEmpty())
			{
				suggestionsBuilder_1.suggest(String.valueOf('['));
			}
		}
		
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestId(SuggestionsBuilder suggestionsBuilder_1)
	{
		CommandSource.suggestIdentifiers(paletteData.getRegistry().getIds(), suggestionsBuilder_1);
		return suggestionsBuilder_1.buildFuture();
	}
	
	public void parseId() throws CommandSyntaxException
	{
		int int_1 = this.reader.getCursor();
		this.id = Identifier.fromCommandInput(this.reader);
		if(paletteData.getRegistry().containsId(this.id))
		{
			O entry = paletteData.getRegistry().get(this.id);
			this.stateFactory = paletteData.getManager(entry);
			this.state = paletteData.getDefaultState(entry);
		}
		else
		{
			this.reader.setCursor(int_1);
			throw INVALID_FLUID_ID_EXCEPTION.createWithContext(this.reader, this.id.toString());
		}
	}
	
	public void parseFluidProperties() throws CommandSyntaxException
	{
		this.reader.skip();
		this.suggestions = this::suggestFluidPropertiesOrEnd;
		this.reader.skipWhitespace();
		
		while(this.reader.canRead() && this.reader.peek() != ']')
		{
			this.reader.skipWhitespace();
			int int_1 = this.reader.getCursor();
			String string_1 = this.reader.readString();
			Property<?> property_1 = this.stateFactory.getProperty(string_1);
			if(property_1 == null)
			{
				this.reader.setCursor(int_1);
				throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), string_1);
			}
			
			if(this.properties.containsKey(property_1))
			{
				this.reader.setCursor(int_1);
				throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), string_1);
			}
			
			this.reader.skipWhitespace();
			this.suggestions = this::suggestEqualsCharacter;
			if(this.reader.canRead() && this.reader.peek() == '=')
			{
				this.reader.skip();
				this.reader.skipWhitespace();
				this.suggestions = (suggestionsBuilder_1) ->
				{
					return suggestPropertyValues(suggestionsBuilder_1, property_1).buildFuture();
				};
				int int_2 = this.reader.getCursor();
				this.parsePropertyValue(property_1, this.reader.readString(), int_2);
				this.suggestions = this::suggestCommaOrEnd;
				this.reader.skipWhitespace();
				if(!this.reader.canRead())
				{
					continue;
				}
				
				if(this.reader.peek() == ',')
				{
					this.reader.skip();
					this.suggestions = this::suggestFluidProperties;
					continue;
				}
				
				if(this.reader.peek() != ']')
				{
					throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
				}
				break;
			}
			
			throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), string_1);
		}
		
		if(this.reader.canRead())
		{
			this.reader.skip();
		}
		else
		{
			throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
		}
	}
	
	private <T extends Comparable<T>> void parsePropertyValue(Property<T> property_1, String string_1, int int_1) throws CommandSyntaxException
	{
		Optional<T> optional_1 = property_1.getValue(string_1);
		if(optional_1.isPresent())
		{
			this.state = this.state.with(property_1, optional_1.get());
			this.properties.put(property_1, optional_1.get());
		}
		else
		{
			this.reader.setCursor(int_1);
			throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.id.toString(), property_1.getName(), string_1);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <S extends PropertyContainer<S>> String stringifyFluidState(Identifier id, S fluidState_1)
	{
		StringBuilder stringBuilder_1 = new StringBuilder(id.toString());
		if(!fluidState_1.getEntries().isEmpty())
		{
			stringBuilder_1.append('[');
			boolean boolean_1 = false;
			
			for(UnmodifiableIterator<Entry<Property<?>, Comparable<?>>> var3 = fluidState_1.getEntries().entrySet().iterator(); var3.hasNext(); boolean_1 = true)
			{
				Entry<Property<?>, Comparable<?>> map$Entry_1 = var3.next();
				if(boolean_1)
				{
					stringBuilder_1.append(',');
				}
				
				stringifyProperty(stringBuilder_1, (Property) map$Entry_1.getKey(), (Comparable) map$Entry_1.getValue());
			}
			
			stringBuilder_1.append(']');
		}
		
		
		return stringBuilder_1.toString();
	}
	
	private static <T extends Comparable<T>> void stringifyProperty(StringBuilder stringBuilder_1, Property<T> property_1, T comparable_1)
	{
		stringBuilder_1.append(property_1.getName());
		stringBuilder_1.append('=');
		stringBuilder_1.append(property_1.getName(comparable_1));
	}
	
	public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder suggestionsBuilder_1)
	{
		return this.suggestions.apply(suggestionsBuilder_1.createOffset(this.reader.getCursor()));
	}

	public Identifier getLayerId()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
