package virtuoel.towelette;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.api.PaletteData;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.util.PacketUtils;

public class ToweletteClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.DELTA, (packetContext, packetByteBuf) ->
		{
			final World world = packetContext.getPlayer().world;
			
			final PaletteData<?, ?> data = PaletteRegistrar.getPaletteData(packetByteBuf.readVarInt());
			
			final ChunkPos chunkPos = new ChunkPos(packetByteBuf.readInt(), packetByteBuf.readInt());
			final int length = packetByteBuf.readVarInt();
			final BlockPos[] positions = new BlockPos[length];
			final Object[] states = new Object[length];
			
			for (int i = 0; i < length; i++)
			{
				short pos = packetByteBuf.readShort();
				positions[i] = chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15);
				states[i] = data.getIds().get(packetByteBuf.readVarInt());
			}
			
			packetContext.getTaskQueue().execute(() ->
			{
				for (int i = 0; i < length; i++)
				{
					@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
					final Object noop = ((ModifiableWorldStateLayer) world).setState(PaletteRegistrar.PALETTES.getId(data), positions[i], (PropertyContainer) states[i], 19);
				}
			});
		});
		
		ClientSidePacketRegistry.INSTANCE.register(PacketUtils.UPDATE, (packetContext, packetByteBuf) ->
		{
			final World world = packetContext.getPlayer().world;
			
			final BlockPos pos = packetByteBuf.readBlockPos();
			
			final PaletteData<?, ?> data = PaletteRegistrar.getPaletteData(packetByteBuf.readVarInt());
			
			final Object state = data.getIds().get(packetByteBuf.readVarInt());
			
			packetContext.getTaskQueue().execute(() ->
			{
				@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
				final Object noop = ((ModifiableWorldStateLayer) world).setState(PaletteRegistrar.PALETTES.getId(data), pos, (PropertyContainer) state, 19);
			});
		});
	}
}
