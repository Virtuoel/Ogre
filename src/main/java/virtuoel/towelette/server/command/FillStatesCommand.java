package virtuoel.towelette.server.command;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.command.arguments.StateArgument;
import virtuoel.towelette.command.arguments.StateArgumentType;

public class FillStatesCommand
{
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.fillstates.failed"));
	
	public static <O, S extends State<S>> void register(LayerData<O, S> layer, CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(
			CommandManager.literal("fillstates").requires(source ->
			{
				return source.hasPermissionLevel(2);
			})
			.then(
				CommandManager.argument("from", BlockPosArgumentType.blockPos())
				.then(
					CommandManager.argument("to", BlockPosArgumentType.blockPos())
					.then(
						CommandManager.literal(LayerRegistrar.LAYERS.getId(layer).toString())
						.then(
							CommandManager.argument("state", StateArgumentType.create(layer))
							.executes(context ->
							{
								return execute(
									context.getSource(),
									layer,
									new BlockBox(
										BlockPosArgumentType.getLoadedBlockPos(context, "from"),
										BlockPosArgumentType.getLoadedBlockPos(context, "to")
									),
									StateArgumentType.getArgument(context, "state"),
									Mode.REPLACE,
									null
								);
							})
						/*	.then( // TODO
								CommandManager.literal("replace")
								.executes(context ->
								{
									return execute(
										context.getSource(),
										layer,
										new BlockBox(
											BlockPosArgumentType.getLoadedBlockPos(context, "from"),
											BlockPosArgumentType.getLoadedBlockPos(context, "to")
										),
										StateArgumentType.getArgument(context, "state"),
										Mode.REPLACE,
										null
									);
								})
								.then(
									CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate())
									.executes(context ->
									{
										return execute(
											context.getSource(),
											layer,
											new BlockBox(
												BlockPosArgumentType.getLoadedBlockPos(context, "from"),
												BlockPosArgumentType.getLoadedBlockPos(context, "to")
											),
											StateArgumentType.getArgument(context, "state"),
											Mode.REPLACE,
											BlockPredicateArgumentType.getBlockPredicate(context, "filter")
										);
									})
								)
							)*/
							.then(
								CommandManager.literal("keep")
								.executes(context ->
								{
									return execute(
										context.getSource(),
										layer,
										new BlockBox(
											BlockPosArgumentType.getLoadedBlockPos(context, "from"),
											BlockPosArgumentType.getLoadedBlockPos(context, "to")
										),
										StateArgumentType.getArgument(context, "state"),
										Mode.REPLACE,
										pos ->
										{
											return layer.isEmpty(((BlockViewStateLayer) pos.getWorld()).getState(layer, pos.getBlockPos()));
										}
									);
								})
							)
							.then(
								CommandManager.literal("outline")
								.executes(context ->
								{
									return execute(
										context.getSource(),
										layer,
										new BlockBox(
											BlockPosArgumentType.getLoadedBlockPos(context, "from"),
											BlockPosArgumentType.getLoadedBlockPos(context, "to")
										),
										StateArgumentType.getArgument(context, "state"),
										Mode.OUTLINE,
										null
									);
								})
							)
							.then(
								CommandManager.literal("hollow")
								.executes(context ->
								{
									return execute(
										context.getSource(),
										layer,
										new BlockBox(
											BlockPosArgumentType.getLoadedBlockPos(context, "from"),
											BlockPosArgumentType.getLoadedBlockPos(context, "to")
										),
										StateArgumentType.getArgument(context, "state"),
										Mode.HOLLOW,
										null
									);
								})
							)
							.then(
								CommandManager.literal("destroy").executes(context ->
								{
									return execute(
										context.getSource(),
										layer,
										new BlockBox(
											BlockPosArgumentType.getLoadedBlockPos(context, "from"),
											BlockPosArgumentType.getLoadedBlockPos(context, "to")
										),
										StateArgumentType.getArgument(context, "state"),
										Mode.DESTROY,
										null
									);
								})
							)
						)
					)
				)
			)
		);
	}
	
	private static <O, S extends State<S>> int execute(ServerCommandSource source, LayerData<O, S> layer, BlockBox box, StateArgument<?, ?> state, Mode mode, @Nullable Predicate<CachedBlockPosition> predicate) throws CommandSyntaxException
	{
		final List<BlockPos> positions = Lists.newArrayList();
		final ServerWorld world = source.getWorld();
		int count = 0;
		Iterator<BlockPos> iter = BlockPos.iterate(
			box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ
		).iterator();
		
		while (true)
		{
			BlockPos pos;
			do
			{
				if (!iter.hasNext())
				{
					iter = positions.iterator();
					
					while (iter.hasNext())
					{
						pos = iter.next();
						Block block_1 = world.getBlockState(pos).getBlock();
						
						world.updateNeighbors(pos, block_1);
					}
					
					if (count == 0)
					{
						throw FAILED_EXCEPTION.create();
					}
					
					source.sendFeedback(new TranslatableText("commands.fillstates.success", count), true);
					return count;
				}
				
				pos = iter.next();
			}
			while (predicate != null && !predicate.test(new CachedBlockPosition(world, pos, true)));
			
			final StateArgument<?, ?> arg = mode.filter.filter(box, pos, state, world);
			if (arg != null)
			{
				// TODO handle BE somehow
			//	BlockEntity blockEntity_1 = world.getBlockEntity(pos);
			//	Clearable.clear(blockEntity_1);
				if (arg.setState(world, pos, 2))
				{
					positions.add(pos.toImmutable());
					count++;
				}
			}
		}
	}
	
	public interface Filter
	{
		@Nullable
		StateArgument<?, ?> filter(BlockBox var1, BlockPos var2, StateArgument<?, ?> var3, ServerWorld var4);
	}
	
	static enum Mode
	{
		REPLACE((box, pos, state, world) ->
		{
			return state;
		}),
		OUTLINE((box, pos, state, world) ->
		{
			return pos.getX() != box.minX && pos.getX() != box.maxX && pos.getY() != box.minY && pos.getY() != box.maxY && pos.getZ() != box.minZ && pos.getZ() != box.maxZ ? null : state;
		}),
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HOLLOW((box, pos, state, world) ->
		{
			return pos.getX() != box.minX && pos.getX() != box.maxX && pos.getY() != box.minY && pos.getY() != box.maxY && pos.getZ() != box.minZ && pos.getZ() != box.maxZ ? new StateArgument(state.getLayer(), state.getLayer().getEmptyState(), Collections.emptySet()) : state;
		}),
		DESTROY((box, pos, state, world) ->
		{
			world.breakBlock(pos, true);
			return state;
		});
		
		public final Filter filter;
		
		private Mode(Filter filter)
		{
			this.filter = filter;
		}
	}
}
