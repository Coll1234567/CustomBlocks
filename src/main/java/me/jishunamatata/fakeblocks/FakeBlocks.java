package me.jishunamatata.fakeblocks;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockBase.Info;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertyInstrument;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IBlockAccess;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MaterialMapColor;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEffect;

public class FakeBlocks extends JavaPlugin {

	private Field BLOCK_MATERIAL_MAP;
	private BlockBreakAnimationTask task;

	@Override
	public void onLoad() {
		try {
			BLOCK_MATERIAL_MAP = CraftMagicNumbers.class.getDeclaredField("BLOCK_MATERIAL");
			BLOCK_MATERIAL_MAP.setAccessible(true);
		} catch (ReflectiveOperationException ex) {
			Bukkit.getPluginManager().disablePlugin(this);
		}

		registerBlock(new SolidCustomBlock(
				Info.a(net.minecraft.server.v1_16_R3.Material.STONE, MaterialMapColor.m).h().a(1.5F, 6.0F),
				BlockPropertyInstrument.DIDGERIDOO, 17, true), "test");

		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {

			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer container = event.getPacket();
				PlayerDigType type = container.getPlayerDigTypes().read(0);
				com.comphenix.protocol.wrappers.BlockPosition packetPos = container.getBlockPositionModifier().read(0);
				BlockPosition pos = new BlockPosition(packetPos.getX(), packetPos.getY(), packetPos.getZ());
				EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();

				if (type == PlayerDigType.START_DESTROY_BLOCK
						&& ((IBlockAccess) player.getWorld()).getType(pos).getBlock() instanceof ICustomBlock) {
					task.addEntry(player, pos);
					player.playerConnection.sendPacket(new PacketPlayOutEntityEffect(player.getId(),
							new MobEffect(MobEffects.SLOWER_DIG, Integer.MAX_VALUE, -1, true, false)));
				} else if (type == PlayerDigType.ABORT_DESTROY_BLOCK || type == PlayerDigType.STOP_DESTROY_BLOCK) {
					task.removeEntry(player);
				}
			}

		});
	}

	@Override
	public void onEnable() {
		task = new BlockBreakAnimationTask();
		task.runTaskTimer(this, 1, 1);
	}

	@SuppressWarnings("unchecked")
	public void registerBlock(Block block, String name) {
		IRegistry.a(IRegistry.BLOCK, "fakeblocks:" + name, block);
		try {
			((Map<Block, Material>) BLOCK_MATERIAL_MAP.get(null)).put(block, Material.STONE);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
