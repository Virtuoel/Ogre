package virtuoel.towelette.server.command;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.command.arguments.FluidArgument;
import virtuoel.towelette.command.arguments.FluidArgumentType;

public class SetFluidCommand
{
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.setfluid.failed"));
	
	public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1)
	{
		commandDispatcher_1.register(CommandManager.literal("setfluid").requires((serverCommandSource_1) ->
		{
			return serverCommandSource_1.hasPermissionLevel(2);
		}).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
		.then(CommandManager.argument("fluid", FluidArgumentType.create()).executes((commandContext_1) ->
		{
			return run(commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), FluidArgumentType.getArgument(commandContext_1, "fluid"), (Predicate<CachedBlockPosition>) null);
		}).then(CommandManager.literal("keep").executes((commandContext_1) ->
		{
			return run(commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), FluidArgumentType.getArgument(commandContext_1, "fluid"), (cachedBlockPosition_1) ->
			{
				return cachedBlockPosition_1.getWorld().getFluidState(cachedBlockPosition_1.getBlockPos()).isEmpty();
			});
		})).then(CommandManager.literal("replace").executes((commandContext_1) ->
		{
			return run(commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), FluidArgumentType.getArgument(commandContext_1, "fluid"), (Predicate<CachedBlockPosition>) null);
		})))));
	}
	
	private static int run(ServerCommandSource serverCommandSource_1, BlockPos blockPos_1, FluidArgument blockArgument_1, @Nullable Predicate<CachedBlockPosition> predicate_1) throws CommandSyntaxException
	{
		ServerWorld serverWorld_1 = serverCommandSource_1.getWorld();
		if(predicate_1 != null && !predicate_1.test(new CachedBlockPosition(serverWorld_1, blockPos_1, true)))
		{
			throw FAILED_EXCEPTION.create();
		}
		else if(!blockArgument_1.setFluidState(serverWorld_1, blockPos_1, 2))
		{
			throw FAILED_EXCEPTION.create();
		}
		else
		{
		//	serverWorld_1.updateNeighbors(blockPos_1, blockArgument_1.getFluidState().getBlockState().getBlock());
			serverCommandSource_1.sendFeedback(new TranslatableText("commands.setfluid.success", blockPos_1.getX(), blockPos_1.getY(), blockPos_1.getZ()), true);
			return 1;
		}
	}
}
