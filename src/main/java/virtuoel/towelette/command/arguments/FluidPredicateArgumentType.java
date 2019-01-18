package virtuoel.towelette.command.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockArgumentParser;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.property.Property;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import virtuoel.towelette.api.CachedFluidPosition;

public class FluidPredicateArgumentType implements ArgumentType<FluidPredicateArgumentType.FluidPredicateFactory>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("water", "minecraft:water", "water[foo=bar]", "#water", "#water[foo=bar]");
	private static final DynamicCommandExceptionType UNKNOWN_TAG_EXCEPTION = new DynamicCommandExceptionType((object_1) ->
	{
		return new TranslatableText("arguments.fluid.tag.unknown", object_1);
	});
	
	public static FluidPredicateArgumentType create()
	{
		return new FluidPredicateArgumentType();
	}
	
	@Override
	public FluidPredicateFactory parse(StringReader stringReader_1) throws CommandSyntaxException
	{
		FluidArgumentParser blockArgumentParser_1 = new FluidArgumentParser(stringReader_1, true).parse(true);
		if(blockArgumentParser_1.getFluidState() != null)
		{
			FluidPredicateArgumentType.FluidStatePredicate FluidPredicateArgumentType$BlockStatePredicate_1 = new FluidPredicateArgumentType.FluidStatePredicate(blockArgumentParser_1.getFluidState(), blockArgumentParser_1.getFluidProperties().keySet());
			return (tagManager_1) ->
			{
				return FluidPredicateArgumentType$BlockStatePredicate_1;
			};
		}
		else
		{
			Identifier identifier_1 = blockArgumentParser_1.getTagId();
			return (tagManager_1) ->
			{
				Tag<Fluid> tag_1 = tagManager_1.fluids().get(identifier_1);
				if(tag_1 == null)
				{
					throw UNKNOWN_TAG_EXCEPTION.create(identifier_1.toString());
				}
				else
				{
					return new FluidPredicateArgumentType.TagPredicate(tag_1, blockArgumentParser_1.getProperties());
				}
			};
		}
	}
	
	public static Predicate<CachedBlockPosition> getPredicateArgument(CommandContext<ServerCommandSource> commandContext_1, String string_1) throws CommandSyntaxException
	{
		return ((FluidPredicateArgumentType.FluidPredicateFactory) commandContext_1.getArgument(string_1, FluidPredicateArgumentType.FluidPredicateFactory.class)).create(((ServerCommandSource) commandContext_1.getSource()).getMinecraftServer().getTagManager());
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext_1, SuggestionsBuilder suggestionsBuilder_1)
	{
		StringReader stringReader_1 = new StringReader(suggestionsBuilder_1.getInput());
		stringReader_1.setCursor(suggestionsBuilder_1.getStart());
		BlockArgumentParser blockArgumentParser_1 = new BlockArgumentParser(stringReader_1, true);
		
		try
		{
			blockArgumentParser_1.parse(true);
		}
		catch(CommandSyntaxException var6)
		{}
		
		return blockArgumentParser_1.getSuggestions(suggestionsBuilder_1);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
	
	static class TagPredicate implements Predicate<CachedBlockPosition>
	{
		private final Tag<Fluid> tag;
		private final Map<String, String> properties;
		
		private TagPredicate(Tag<Fluid> tag_1, Map<String, String> map_1)
		{
			this.tag = tag_1;
			this.properties = map_1;
		}
		
		@Override
		public boolean test(CachedBlockPosition cachedBlockPosition_1)
		{
			FluidState blockState_1 = ((CachedFluidPosition) cachedBlockPosition_1).getFluidState();
			if(!blockState_1.matches(this.tag))
			{
				return false;
			}
			else
			{
				Iterator<Entry<String, String>> var3 = this.properties.entrySet().iterator();
				
				while(var3.hasNext())
				{
					Entry<String, String> map$Entry_1 = var3.next();
					Property<?> property_1 = blockState_1.getFluid().getStateFactory().getProperty(map$Entry_1.getKey());
					if(property_1 == null)
					{
						return false;
					}
					
					Comparable<?> comparable_1 = property_1.getValue(map$Entry_1.getValue()).orElse(null);
					if(comparable_1 == null)
					{
						return false;
					}
					
					if(blockState_1.get(property_1) != comparable_1)
					{
						return false;
					}
				}
				
				return true;
			}
		}
	}
	
	static class FluidStatePredicate implements Predicate<CachedBlockPosition>
	{
		private final FluidState state;
		private final Set<Property<?>> properties;
		
		public FluidStatePredicate(FluidState blockState_1, Set<Property<?>> set_1)
		{
			this.state = blockState_1;
			this.properties = set_1;
		}
		
		@Override
		public boolean test(CachedBlockPosition cachedBlockPosition_1)
		{
			FluidState blockState_1 = ((CachedFluidPosition) cachedBlockPosition_1).getFluidState();
			if(blockState_1.getFluid() != this.state.getFluid())
			{
				return false;
			}
			else
			{
				Iterator<Property<?>> var3 = this.properties.iterator();
				
				while(var3.hasNext())
				{
					Property<?> property_1 = var3.next();
					if(blockState_1.get(property_1) != this.state.get(property_1))
					{
						return false;
					}
				}
				
				return true;
			}
		}
	}
	
	public interface FluidPredicateFactory
	{
		Predicate<CachedBlockPosition> create(RegistryTagManager var1) throws CommandSyntaxException;
	}
}
