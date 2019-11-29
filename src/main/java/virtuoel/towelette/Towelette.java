package virtuoel.towelette;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.Reflection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ToweletteApi;
import virtuoel.towelette.command.arguments.LayerArgumentType;
import virtuoel.towelette.command.arguments.StateArgumentType;
import virtuoel.towelette.server.command.FillStatesCommand;
import virtuoel.towelette.server.command.GetStateCommand;
import virtuoel.towelette.server.command.GetStatesCommand;
import virtuoel.towelette.server.command.SetStateCommand;

public class Towelette implements ModInitializer
{
	public static final Logger LOGGER = LogManager.getLogger(ToweletteApi.MOD_ID);
	
	public static final Tag<Block> DISPLACEABLE = TagRegistry.block(id("displaceable"));
	public static final Tag<Block> UNDISPLACEABLE = TagRegistry.block(id("undisplaceable"));
	
	static
	{
		Reflection.initialize(LayerRegistrar.class);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerArgumentTypes()
	{
		ArgumentTypes.register("state", StateArgumentType.class, new StateArgumentType.Serializer());
		ArgumentTypes.register("layer", LayerArgumentType.class, new ConstantArgumentSerializer<LayerArgumentType>(LayerArgumentType::layer));
	}
	
	@Override
	public void onInitialize()
	{
		registerArgumentTypes();
		
		CommandRegistry.INSTANCE.register(false, GetStatesCommand::register);
		
		LayerRegistrar.LAYERS.forEach(layer ->
		{
			CommandRegistry.INSTANCE.register(false, commandDispatcher ->
			{
				GetStateCommand.register(layer, commandDispatcher);
				SetStateCommand.register(layer, commandDispatcher);
				FillStatesCommand.register(layer, commandDispatcher);
			});
		});
		
		RegistryEntryAddedCallback.event(LayerRegistrar.LAYERS).register((rawId, id, object) ->
		{
			CommandRegistry.INSTANCE.register(false, commandDispatcher ->
			{
				GetStateCommand.register(object, commandDispatcher);
				SetStateCommand.register(object, commandDispatcher);
				FillStatesCommand.register(object, commandDispatcher);
			});
		});
	}
	
	public static Identifier id(final String name)
	{
		return new Identifier(ToweletteApi.MOD_ID, name);
	}
}
