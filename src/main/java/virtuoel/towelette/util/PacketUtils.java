package virtuoel.towelette.util;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.Towelette;
import virtuoel.towelette.api.BlockViewStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

public class PacketUtils
{
	public static final Identifier UPDATE = Towelette.id("state_update");
	public static final Identifier DELTA = Towelette.id("delta_update");
	
	public static <O, S extends PropertyContainer<S>> CustomPayloadS2CPacket stateUpdate(LayerData<O, S> data, BlockView world, BlockPos pos)
	{
		final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos);
		buffer.writeVarInt(LayerRegistrar.LAYERS.getRawId(data));
		final BlockViewStateLayer w = ((BlockViewStateLayer) world);
		buffer.writeVarInt(data.getIds().getId(w.getState(data, pos)));
		return new CustomPayloadS2CPacket(UPDATE, buffer);
	}
	
	public static CustomPayloadS2CPacket deltaUpdate(LayerData<?, ?> data, int records, short[] positions, WorldChunk chunk)
	{
		return new CustomPayloadS2CPacket(DELTA, writeDelta(data, new PacketByteBuf(Unpooled.buffer()), records, positions, chunk));
	}
	
	private static <O, S extends PropertyContainer<S>> PacketByteBuf writeDelta(LayerData<O, S> layer, PacketByteBuf buffer, int records, short[] positions, WorldChunk chunk)
	{
		final ChunkPos chunkPos = chunk.getPos();
		
		buffer.writeVarInt(LayerRegistrar.LAYERS.getRawId(layer));
		
		buffer.writeInt(chunkPos.x);
		buffer.writeInt(chunkPos.z);
		buffer.writeVarInt(records);
		
		final ChunkStateLayer c = ((ChunkStateLayer) chunk);
		
		for (int i = 0; i < records; i++)
		{
			buffer.writeShort(positions[i]);
			buffer.writeVarInt(layer.getIds().getId(c.getState(layer, toBlockPos(chunkPos, positions[i]))));
		}
		
		return buffer;
	}
	
	private static BlockPos toBlockPos(ChunkPos chunkPos, short pos)
	{
		return new BlockPos(chunkPos.toBlockPos(pos >> 12 & 15, pos & 255, pos >> 8 & 15));
	}
}
