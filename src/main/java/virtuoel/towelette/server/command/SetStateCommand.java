package virtuoel.towelette.server.command;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.command.arguments.StateArgument;
import virtuoel.towelette.command.arguments.StateArgumentType;

public class SetStateCommand
{
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.setstate.failed"));
	
	public static void register(LayerData<?, ?> layer, CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(
			CommandManager.literal("setstate")
			.requires(source -> source.hasPermissionLevel(2))
			.then(
				CommandManager.argument("pos", BlockPosArgumentType.blockPos())
				.then(
					CommandManager.literal(LayerRegistrar.LAYERS.getId(layer).toString())
					.then(
						CommandManager.argument("state", StateArgumentType.create(layer))
						.executes(SetStateCommand::run)
						.then(
							CommandManager.literal("keep")
							.executes(SetStateCommand::runKeep)
						)
						.then(
							CommandManager.literal("replace")
							.executes(SetStateCommand::run)
						)
					)
				)
			)
		);
	}
	
	private static <O, S extends State<S>> int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		return run(context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), StateArgumentType.<O, S>getArgument(context, "state"), null);
	}
	
	private static <O, S extends State<S>> int runKeep(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		final StateArgument<O, S> state = StateArgumentType.getArgument(context, "state");
		final LayerData<O, S> layer = state.getLayer();
		final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, "pos");
		return run(context.getSource(), pos, state, cachedPos ->
		{
			return layer.isEmpty(((ModifiableWorldStateLayer) cachedPos.getWorld()).getState(layer, cachedPos.getBlockPos()));
		});
	}
	
	private static <O, S extends State<S>> int run(ServerCommandSource source, BlockPos pos, StateArgument<O, S> stateArg, @Nullable Predicate<CachedBlockPosition> predicate) throws CommandSyntaxException
	{
		final ServerWorld world = source.getWorld();
		
		if (predicate != null && !predicate.test(new CachedBlockPosition(world, pos, true)))
		{
			throw FAILED_EXCEPTION.create();
		}
		else if (!stateArg.setState(world, pos, 2))
		{
			throw FAILED_EXCEPTION.create();
		}
		else
		{
			source.sendFeedback(new TranslatableText("commands.setstate.success", pos.getX(), pos.getY(), pos.getZ()), true);
			return 1;
		}
	}
}
