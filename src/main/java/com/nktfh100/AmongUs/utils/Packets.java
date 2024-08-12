package com.nktfh100.AmongUs.utils;

import java.util.*;
import java.util.logging.Level;

import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.nktfh100.AmongUs.info.ColorInfo;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityPose;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;

public class Packets {
	public static byte toPackedByte(float f) {
		return (byte) ((int) (f * 256.0F / 360.0F));
	}

	public static int toPacketRotation(float f) {
		return (int) (f * 256.0F / 360.0F);
	}

	public static void sendPacket(Player p, PacketContainer packet) {
		if (p.isOnline() && packet != null) {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
		}
	}

	public static PacketContainer UPDATE_DISPLAY_NAME(UUID uuid, String orgName, String newName) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
		WrappedGameProfile wgp = new WrappedGameProfile(uuid, orgName);

        if (Main.getVersion()[0] < 19 || (Main.getVersion()[0] == 19 && Main.getVersion()[1] < 3)) {
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME);
			packet.getPlayerInfoDataLists().write(0,
					Collections.singletonList(
							new PlayerInfoData(wgp, 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromLegacyText(newName))
					)
			);

        } else {
            packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));
			packet.getPlayerInfoDataLists().write(1,
					Collections.singletonList(new PlayerInfoData(wgp, 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromLegacyText(newName)
					)));

        }
        return packet;
    }

	public static PacketContainer ADD_PLAYER(Player player, UUID playerToAdd, String name, String displayName, String textureValue, String textureSignature, boolean... isFakePlayer) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
		WrappedGameProfile wgp = new WrappedGameProfile(playerToAdd, name);
		PlayerInfoData playerInfoData = new PlayerInfoData(wgp, 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(displayName));

		if (Main.getVersion()[0] < 19 || (Main.getVersion()[0] == 19 && Main.getVersion()[1] < 3)) {
			packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
			packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
			// new ArrayList<>(List.of(playerInfoData))

        } else {
			packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME, EnumWrappers.PlayerInfoAction.UPDATE_LISTED));
			packet.getPlayerInfoDataLists().write(1, Collections.singletonList(playerInfoData));
        }

        wgp.getProperties().get("textures").clear();
        wgp.getProperties().get("textures").add(new WrappedSignedProperty("textures", textureValue, textureSignature));

        boolean fakePlayer = isFakePlayer.length >= 1 && isFakePlayer[0];
		if (Main.getIsTab() && !fakePlayer) {
			Main.getTabApi().getNameTagManager().showNameTag(Main.getTabApi().getPlayer(playerToAdd), Main.getTabApi().getPlayer(player.getUniqueId()));
		}

		return packet;
	}

	public static PacketContainer REMOVE_PLAYER(Player player, UUID playerToHide, boolean... isFakePlayer) {
		PacketContainer packet;
		if (Main.getVersion()[0] < 19 || (Main.getVersion()[0] == 19 && Main.getVersion()[1] < 3)) {
			packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
			packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
			packet.getPlayerInfoDataLists().write(0, Collections.singletonList(
					new PlayerInfoData(new WrappedGameProfile(playerToHide, player.getName()), 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(player.getDisplayName()))
			));

		} else {
			packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE);
			packet.getUUIDLists().write(0, Collections.singletonList(playerToHide));
		}

		boolean fakePlayer = isFakePlayer.length >= 1 && isFakePlayer[0];
		if (Main.getIsTab() && !fakePlayer) {
			Main.getTabApi().getNameTagManager().showNameTag(Main.getTabApi().getPlayer(playerToHide), Main.getTabApi().getPlayer(player.getUniqueId()));
		}

		return packet;
	}

	public static PacketContainer NAMED_SOUND(Location loc, Sound sound) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
		packet.getSoundEffects().write(0, sound);
		packet.getSoundCategories().write(0, EnumWrappers.SoundCategory.BLOCKS);
		packet.getIntegers().write(0, (int) (loc.getX() * 8.0));
		packet.getIntegers().write(1, (int) (loc.getY() * 8.0));
		packet.getIntegers().write(2, (int) (loc.getZ() * 8.0));
		packet.getFloat().write(0, 50F); // volume
		packet.getFloat().write(1, 1F); // pitch

		return packet;
	}

	public static PacketContainer NAMED_SOUND(Location loc, Sound sound, Float volume, Float pitch) {
		PacketContainer packet = NAMED_SOUND(loc, sound);
		packet.getFloat().write(0, volume);
		packet.getFloat().write(1, pitch);
		return packet;
	}

	public static PacketContainer BLOCK_CHANGE(Location loc, WrappedBlockData wrappedData) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
		packet.getBlockPositionModifier().write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (wrappedData != null) {
			packet.getBlockData().write(0, wrappedData);
		}
		return packet;
	}

	public static PacketContainer SPAWN_PLAYER(Player player, int entityId, UUID uuid, Location location) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

		try {
			packet.getIntegers().write(0, entityId);
		} catch (Exception e) {
			// Ignore if the field doesn't exist
		}

		try {
			packet.getUUIDs().write(0, uuid);
		} catch (Exception e) {
			// Ignore if the field doesn't exist
		}

		try {
			packet.getDoubles()
					.write(0, location.getX())
					.write(1, location.getY())
					.write(2, location.getZ());
		} catch (Exception e) {
			// Ignore if the fields don't exist
		}

		try {
			packet.getBytes()
					.write(0, (byte) (location.getYaw() * 256.0F / 360.0F))
					.write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
		} catch (Exception e) {
			// Ignore if the fields don't exist
		}

		try {
			packet.getDataWatcherModifier().write(0, new WrappedDataWatcher());
		} catch (Exception e) {
			// Ignore if the field doesn't exist
		}

		return packet;
	}


	public static PacketContainer ENTITY_HEAD_ROTATION(int entityId, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
		packet.getIntegers().write(0, entityId);
		packet.getBytes().write(0, toPackedByte(loc.getYaw()));
//		packet.getBytes().write(0, (byte) loc.getYaw());
		return packet;
	}

	public static PacketContainer ENTITY_LOOK(int entityId, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
		packet.getIntegers().write(0, entityId);
		packet.getBytes().write(0, toPackedByte(loc.getYaw())).write(1, toPackedByte((loc.getPitch())));
		packet.getBooleans().write(0, true);
		return packet;
	}

	public static PacketContainer PLAYER_ARMOR(ColorInfo color, int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entityId);
		List<Pair<ItemSlot, ItemStack>> newSlotStack = new ArrayList<Pair<ItemSlot, ItemStack>>();
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.HEAD, new ItemStack(color.getGlass())));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.CHEST, Utils.getArmorColor(color, Material.LEATHER_CHESTPLATE)));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.LEGS, Utils.getArmorColor(color, Material.LEATHER_LEGGINGS)));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.FEET, Utils.getArmorColor(color, Material.LEATHER_BOOTS)));
		packet.getSlotStackPairLists().write(0, newSlotStack);
		return packet;
	}

	public static PacketContainer ENTITY_EQUIPMENT_HEAD(int entityId, Material mat) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entityId);
		List<Pair<ItemSlot, ItemStack>> newSlotStack = new ArrayList<Pair<ItemSlot, ItemStack>>();
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.HEAD, new ItemStack(mat)));
		packet.getSlotStackPairLists().write(0, newSlotStack);
		return packet;
	}

	public static PacketContainer DESTROY_ENTITY(int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

		// Verificar o tamanho do StructureModifier
		int size = packet.getIntegerArrays().size();

		// Garantir que o índice 0 está dentro dos limites
		if (size > 0) {
			packet.getIntegerArrays().write(0, new int[]{entityId});
		}
		return packet;
	}

	public static PacketContainer PLAYER_SLEEPING(int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entityId);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		Serializer serializer = Registry.get(EnumWrappers.getEntityPoseClass());
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(6, serializer);
		watcher.setObject(object, EntityPose.SLEEPING.toNms());

		if (Main.getVersion()[0] < 19 || (Main.getVersion()[0] == 19 && Main.getVersion()[1] < 3)) {
			packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

		} else {
			final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
			watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
				final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
				wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
			});
			packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
		}

		return packet;
	}

	public static PacketContainer PARTICLES(Location loc, Particle particle, Object data, Integer count, float offSetX, float offSetY, float offSetZ) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
		packet.getNewParticles().write(0, WrappedParticle.create(particle, data));
		packet.getIntegers().write(0, count); // count
		packet.getFloat().write(0, offSetX).write(1, offSetY).write(2, offSetZ); // offset
		packet.getBooleans().write(0, false); // long distance
		packet.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
		return packet;
	}

	public static PacketContainer ARMOR_STAND(Location loc, Integer entityId, UUID uuid) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

		packet.getIntegers().write(0, entityId); // entity id
		packet.getUUIDs().write(0, uuid); // uuid
		packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY()); // location
		packet.getDoubles().write(2, loc.getZ());

		// Verificar o tamanho do StructureModifier
		int size = packet.getIntegers().size();

		// Garantir que o índice 5 está dentro dos limites
		if (size > 5) {
			packet.getIntegers().write(4, 0); // yaw
			packet.getIntegers().write(5, 0); // pitch
		}

		return packet;
	}

	public static PacketContainer ENTITY_TELEPORT(int entityId, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
		packet.getIntegers().write(0, entityId); // entity id
		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY());
		packet.getDoubles().write(2, loc.getZ());
		return packet;
	}

	public static PacketContainer METADATA_SKIN(int entityId, Player player, boolean isGhost) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entityId);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		Serializer serializer = Registry.get(Byte.class);
		if (player != null) {
			watcher.setEntity(player);
		}

		// To show all the skin overlay parts
		watcher.setObject(17, serializer, (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));

		if (isGhost) {
			watcher.setObject(0, serializer, (byte) (0x20));
		}

		if (Main.getVersion()[0] < 19 || (Main.getVersion()[0] == 19 && Main.getVersion()[1] < 3)) {
			packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

		} else {
			final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
			watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
				final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
				wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
			});
			packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
		}

		return packet;
	}
	
//	public static PacketContainer METADATA_INVIS(int entityId, Player player) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		Serializer serializer = Registry.get(Byte.class);
//		if (player != null) {
//			watcher.setEntity(player);
//		}
//		watcher.setObject(0, serializer, (byte) (0x20));
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer ENTITY_INVIS(int entityId) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		Serializer serializer = Registry.get(Byte.class);
//		watcher.setObject(0, serializer, (byte) (0x20));
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer ARMOR_STAND_ROTATION(int entityId, Vector3F vector) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		watcher.setObject(15, Registry.getVectorSerializer(), vector);
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer GLOW(Player player) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, player.getEntityId()); // Set packet's entity id
//		WrappedDataWatcher watcher = new WrappedDataWatcher(); // Create data watcher, the Entity Metadata packet requires this
//		Serializer serializer = Registry.get(Byte.class); // Found this through google, needed for some stupid reason
//		watcher.setEntity(player); // Set the new data watcher's target
//		watcher.setObject(0, serializer, (byte) (0x40)); // Set status to glowing, found on protocol page
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); // Make the packet's datawatcher the one we created
//		return packet;
//	}
}
