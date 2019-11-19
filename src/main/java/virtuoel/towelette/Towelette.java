package virtuoel.towelette;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.Reflection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.api.ToweletteApi;
import virtuoel.towelette.command.arguments.LayerArgumentType;
import virtuoel.towelette.command.arguments.StateArgumentType;
import virtuoel.towelette.server.command.SetStateCommand;

public class Towelette implements ModInitializer
{
	public static final Logger LOGGER = LogManager.getLogger(ToweletteApi.MOD_ID);
	
	public static final Tag<Block> DISPLACEABLE = TagRegistry.block(id("displaceable"));
	public static final Tag<Block> UNDISPLACEABLE = TagRegistry.block(id("undisplaceable"));
	
	static
	{
		Reflection.initialize(PaletteRegistrar.class);
	}
	
	public static boolean ignoreBlockStateFluids(BlockState state)
	{
		return false;
	}
	
	@Override
	public void onInitialize()
	{
		ArgumentTypes.register("state", StateArgumentType.class, new ConstantArgumentSerializer<StateArgumentType>(StateArgumentType::create));
		ArgumentTypes.register("layer", LayerArgumentType.class, new ConstantArgumentSerializer<LayerArgumentType>(LayerArgumentType::layer));
		
		CommandRegistry.INSTANCE.register(false, commandDispatcher ->
		{
			commandDispatcher.register(
				CommandManager.literal("getfluid")
				.requires(commandSource ->
				{
					return commandSource.hasPermissionLevel(2);
				})
				.then(
					CommandManager.argument("pos", BlockPosArgumentType.blockPos())
					.executes(context ->
					{
						final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
						final FluidState state = context.getSource().getWorld().getFluidState(pos);
						context.getSource().sendFeedback(new LiteralText(PaletteRegistrar.FLUIDS.serializeState(state).toString()), true);
						return 1;
					})
				)
			);
			
			commandDispatcher.register(
				CommandManager.literal("getblock")
				.requires(commandSource ->
				{
					return commandSource.hasPermissionLevel(2);
				})
				.then(
					CommandManager.argument("pos", BlockPosArgumentType.blockPos())
					.executes(context ->
					{
						final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
						final BlockState state = context.getSource().getWorld().getBlockState(pos);
						context.getSource().sendFeedback(new LiteralText(PaletteRegistrar.BLOCKS.serializeState(state).toString()), true);
						return 1;
					})
				)
			);
			
			SetStateCommand.register(commandDispatcher);
		});
	}
	
	public static Identifier id(final String name)
	{
		return new Identifier(ToweletteApi.MOD_ID, name);
	}
}
