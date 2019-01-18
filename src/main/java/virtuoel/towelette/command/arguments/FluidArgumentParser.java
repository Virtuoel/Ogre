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

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.FluidStateImpl;
import net.minecraft.server.command.CommandSource;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidArgumentParser
{
	public static final SimpleCommandExceptionType DISALLOWED_TAG_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("argument.fluid.tag.disallowed", new Object[0]));
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
	private final boolean allowTag;
	private final Map<Property<?>, Comparable<?>> fluidProperties = Maps.newHashMap();
	private final Map<String, String> tagProperties = Maps.newHashMap();
	private Identifier fluidId = new Identifier("");
	private StateFactory<Fluid, FluidState> stateFactory;
	private FluidState fluidState;
	private Identifier tagId = new Identifier("");
	private int cursorPos;
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;
	
	public FluidArgumentParser(StringReader stringReader_1, boolean boolean_1)
	{
		this.suggestions = SUGGEST_DEFAULT;
		this.reader = stringReader_1;
		this.allowTag = boolean_1;
	}
	
	public static TagContainer<Fluid> fluidTagContainer; // Set by FluidTagsMixin
	
	public Map<Property<?>, Comparable<?>> getFluidProperties()
	{
		return this.fluidProperties;
	}
	
	@Nullable
	public FluidState getFluidState()
	{
		return this.fluidState;
	}
	
	@Nullable
	public Identifier getTagId()
	{
		return this.tagId;
	}
	
	public FluidArgumentParser parse(boolean boolean_1) throws CommandSyntaxException
	{
		this.suggestions = this::suggestFluidOrTagId;
		if(this.reader.canRead() && this.reader.peek() == '#')
		{
			this.parseTagId();
			this.suggestions = this::suggestMojangsonOrTagProperties;
			if(this.reader.canRead() && this.reader.peek() == '[')
			{
				this.parseTagProperties();
				this.suggestions = SuggestionsBuilder::buildFuture;
			}
		}
		else
		{
			this.parseFluidId();
			this.suggestions = this::suggestMojangsonOrFluidProperties;
			if(this.reader.canRead() && this.reader.peek() == '[')
			{
				this.parseFluidProperties();
				this.suggestions = SuggestionsBuilder::buildFuture;
			}
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
	
	private CompletableFuture<Suggestions> suggestTagPropertiesOrEnd(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			suggestionsBuilder_1.suggest(String.valueOf(']'));
		}
		
		return this.suggestTagProperties(suggestionsBuilder_1);
	}
	
	private CompletableFuture<Suggestions> suggestFluidProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		String string_1 = suggestionsBuilder_1.getRemaining().toLowerCase(Locale.ROOT);
		
		if(fluidState instanceof FluidStateImpl)
		{
			Iterator<Property<?>> var3 = ((FluidStateImpl) this.fluidState).getProperties().iterator();
			
			while(var3.hasNext())
			{
				Property<?> property_1 = var3.next();
				if(!this.fluidProperties.containsKey(property_1) && property_1.getName().startsWith(string_1))
				{
					suggestionsBuilder_1.suggest(property_1.getName() + '=');
				}
			}
		}
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestTagProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		String string_1 = suggestionsBuilder_1.getRemaining().toLowerCase(Locale.ROOT);
		if(this.tagId != null && !this.tagId.getPath().isEmpty())
		{
			Tag<Fluid> tag_1 = fluidTagContainer.get(this.tagId);
			if(tag_1 != null)
			{
				Iterator<Fluid> var4 = tag_1.values().iterator();
				
				while(var4.hasNext())
				{
					Fluid fluid_1 = var4.next();
					Iterator<Property<?>> var6 = fluid_1.getStateFactory().getProperties().iterator();
					
					while(var6.hasNext())
					{
						Property<?> property_1 = var6.next();
						if(!this.tagProperties.containsKey(property_1.getName()) && property_1.getName().startsWith(string_1))
						{
							suggestionsBuilder_1.suggest(property_1.getName() + '=');
						}
					}
				}
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
		

		if(fluidState instanceof FluidStateImpl)
		{
			if(suggestionsBuilder_1.getRemaining().isEmpty() && this.fluidProperties.size() < ((FluidStateImpl) this.fluidState).getProperties().size())
			{
				suggestionsBuilder_1.suggest(String.valueOf(','));
			}
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
	
	private CompletableFuture<Suggestions> suggestTagPropertyValues(SuggestionsBuilder suggestionsBuilder_1, String string_1)
	{
		boolean boolean_1 = false;
		if(this.tagId != null && !this.tagId.getPath().isEmpty())
		{
			Tag<Fluid> tag_1 = fluidTagContainer.get(this.tagId);
			if(tag_1 != null)
			{
				Iterator<Fluid> var5 = tag_1.values().iterator();
				
				label40: while(true)
				{
					while(true)
					{
						Fluid fluid_1;
						do
						{
							if(!var5.hasNext())
							{
								break label40;
							}
							
							fluid_1 = var5.next();
							Property<?> property_1 = fluid_1.getStateFactory().getProperty(string_1);
							if(property_1 != null)
							{
								suggestPropertyValues(suggestionsBuilder_1, property_1);
							}
						}
						while(boolean_1);
						
						Iterator<Property<?>> var8 = fluid_1.getStateFactory().getProperties().iterator();
						
						while(var8.hasNext())
						{
							Property<?> property_2 = var8.next();
							if(!this.tagProperties.containsKey(property_2.getName()))
							{
								boolean_1 = true;
								break;
							}
						}
					}
				}
			}
		}
		
		if(boolean_1)
		{
			suggestionsBuilder_1.suggest(String.valueOf(','));
		}
		
		suggestionsBuilder_1.suggest(String.valueOf(']'));
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestMojangsonOrTagProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			Tag<Fluid> tag_1 = fluidTagContainer.get(this.tagId);
			if(tag_1 != null)
			{
				boolean boolean_1 = false;
				Iterator<Fluid> var5 = tag_1.values().iterator();
				
				while(var5.hasNext())
				{
					Fluid fluid_1 = var5.next();
					boolean_1 |= !fluid_1.getStateFactory().getProperties().isEmpty();
					if(boolean_1)
					{
						break;
					}
				}
				
				if(boolean_1)
				{
					suggestionsBuilder_1.suggest(String.valueOf('['));
				}
			}
		}
		
		return this.suggestIdentifiers(suggestionsBuilder_1);
	}
	
	private CompletableFuture<Suggestions> suggestMojangsonOrFluidProperties(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(suggestionsBuilder_1.getRemaining().isEmpty())
		{
			if(!this.fluidState.getFluid().getStateFactory().getProperties().isEmpty())
			{
				suggestionsBuilder_1.suggest(String.valueOf('['));
			}
		}
		
		return suggestionsBuilder_1.buildFuture();
	}
	
	private CompletableFuture<Suggestions> suggestIdentifiers(SuggestionsBuilder suggestionsBuilder_1)
	{
		return CommandSource.suggestIdentifiers(fluidTagContainer.getKeys(), suggestionsBuilder_1.createOffset(this.cursorPos).add(suggestionsBuilder_1));
	}
	
	private CompletableFuture<Suggestions> suggestFluidOrTagId(SuggestionsBuilder suggestionsBuilder_1)
	{
		if(this.allowTag)
		{
			CommandSource.suggestIdentifiers(fluidTagContainer.getKeys(), suggestionsBuilder_1, String.valueOf('#'));
		}
		
		CommandSource.suggestIdentifiers(Registry.FLUID.getIds(), suggestionsBuilder_1);
		return suggestionsBuilder_1.buildFuture();
	}
	
	public void parseFluidId() throws CommandSyntaxException
	{
		int int_1 = this.reader.getCursor();
		this.fluidId = Identifier.fromCommandInput(this.reader);
		if(Registry.FLUID.containsId(this.fluidId))
		{
			Fluid fluid_1 = Registry.FLUID.get(this.fluidId);
			this.stateFactory = fluid_1.getStateFactory();
			this.fluidState = fluid_1.getDefaultState();
		}
		else
		{
			this.reader.setCursor(int_1);
			throw INVALID_FLUID_ID_EXCEPTION.createWithContext(this.reader, this.fluidId.toString());
		}
	}
	
	public void parseTagId() throws CommandSyntaxException
	{
		if(!this.allowTag)
		{
			throw DISALLOWED_TAG_EXCEPTION.create();
		}
		else
		{
			this.suggestions = this::suggestIdentifiers;
			this.reader.expect('#');
			this.cursorPos = this.reader.getCursor();
			this.tagId = Identifier.fromCommandInput(this.reader);
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
				throw UNKNOWN_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), string_1);
			}
			
			if(this.fluidProperties.containsKey(property_1))
			{
				this.reader.setCursor(int_1);
				throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), string_1);
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
			
			throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), string_1);
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
	
	public void parseTagProperties() throws CommandSyntaxException
	{
		this.reader.skip();
		this.suggestions = this::suggestTagPropertiesOrEnd;
		int int_1 = -1;
		this.reader.skipWhitespace();
		
		while(true)
		{
			if(this.reader.canRead() && this.reader.peek() != ']')
			{
				this.reader.skipWhitespace();
				int int_2 = this.reader.getCursor();
				String string_1 = this.reader.readString();
				if(this.tagProperties.containsKey(string_1))
				{
					this.reader.setCursor(int_2);
					throw DUPLICATE_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), string_1);
				}
				
				this.reader.skipWhitespace();
				if(!this.reader.canRead() || this.reader.peek() != '=')
				{
					this.reader.setCursor(int_2);
					throw EMPTY_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), string_1);
				}
				
				this.reader.skip();
				this.reader.skipWhitespace();
				this.suggestions = (suggestionsBuilder_1) ->
				{
					return this.suggestTagPropertyValues(suggestionsBuilder_1, string_1);
				};
				int_1 = this.reader.getCursor();
				String string_2 = this.reader.readString();
				this.tagProperties.put(string_1, string_2);
				this.reader.skipWhitespace();
				if(!this.reader.canRead())
				{
					continue;
				}
				
				int_1 = -1;
				if(this.reader.peek() == ',')
				{
					this.reader.skip();
					this.suggestions = this::suggestTagProperties;
					continue;
				}
				
				if(this.reader.peek() != ']')
				{
					throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
				}
			}
			
			if(this.reader.canRead())
			{
				this.reader.skip();
				return;
			}
			
			if(int_1 >= 0)
			{
				this.reader.setCursor(int_1);
			}
			
			throw UNCLOSED_PROPERTIES_EXCEPTION.createWithContext(this.reader);
		}
	}
	
	private <T extends Comparable<T>> void parsePropertyValue(Property<T> property_1, String string_1, int int_1) throws CommandSyntaxException
	{
		Optional<T> optional_1 = property_1.getValue(string_1);
		if(optional_1.isPresent())
		{
			this.fluidState = this.fluidState.with(property_1, optional_1.get());
			this.fluidProperties.put(property_1, optional_1.get());
		}
		else
		{
			this.reader.setCursor(int_1);
			throw INVALID_PROPERTY_EXCEPTION.createWithContext(this.reader, this.fluidId.toString(), property_1.getName(), string_1);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String stringifyFluidState(FluidState fluidState_1)
	{
		StringBuilder stringBuilder_1 = new StringBuilder(Registry.FLUID.getId(fluidState_1.getFluid()).toString());
		if(fluidState_1 instanceof FluidStateImpl && !((FluidStateImpl) fluidState_1).getProperties().isEmpty())
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
	
	public Map<String, String> getProperties()
	{
		return this.tagProperties;
	}
}
