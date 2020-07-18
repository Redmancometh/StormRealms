package org.stormrealms.stormspigot;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.PlayerConnection;

import net.minecraft.server.PacketPlayInSteerVehicle;
import net.minecraft.server.PacketPlayInVehicleMove;
import net.minecraft.server.PacketPlayInTeleportAccept;
import net.minecraft.server.PacketPlayInRecipeDisplayed;
import net.minecraft.server.PacketPlayInAdvancements;
import net.minecraft.server.PacketPlayInTabComplete;
import net.minecraft.server.PacketPlayInSetCommandBlock;
import net.minecraft.server.PacketPlayInSetCommandMinecart;
import net.minecraft.server.PacketPlayInPickItem;
import net.minecraft.server.PacketPlayInItemName;
import net.minecraft.server.PacketPlayInBeacon;
import net.minecraft.server.PacketPlayInStruct;
import net.minecraft.server.PacketPlayInSetJigsaw;
import net.minecraft.server.PacketPlayInTrSel;
import net.minecraft.server.PacketPlayInBEdit;
import net.minecraft.server.PacketPlayInEntityNBTQuery;
import net.minecraft.server.PacketPlayInTileNBTQuery;
import net.minecraft.server.PacketPlayInFlying;
import net.minecraft.server.PacketPlayInBlockDig;
import net.minecraft.server.PacketPlayInUseItem;
import net.minecraft.server.PacketPlayInBlockPlace;
import net.minecraft.server.PacketPlayInSpectate;
import net.minecraft.server.PacketPlayInResourcePackStatus;
import net.minecraft.server.PacketPlayInBoatMove;
import net.minecraft.server.PacketPlayInHeldItemSlot;
import net.minecraft.server.PacketPlayInChat;
import net.minecraft.server.PacketPlayInArmAnimation;
import net.minecraft.server.PacketPlayInEntityAction;
import net.minecraft.server.PacketPlayInUseEntity;
import net.minecraft.server.PacketPlayInClientCommand;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayInWindowClick;
import net.minecraft.server.PacketPlayInAutoRecipe;
import net.minecraft.server.PacketPlayInEnchantItem;
import net.minecraft.server.PacketPlayInSetCreativeSlot;
import net.minecraft.server.PacketPlayInTransaction;
import net.minecraft.server.PacketPlayInUpdateSign;
import net.minecraft.server.PacketPlayInKeepAlive;
import net.minecraft.server.PacketPlayInAbilities;
import net.minecraft.server.PacketPlayInSettings;
import net.minecraft.server.PacketPlayInCustomPayload;
import net.minecraft.server.PacketPlayInDifficultyChange;
import net.minecraft.server.PacketPlayInDifficultyLock;

public class PlayerConnectionOverride extends PlayerConnection {
    private static PacketDispatcher packetDispatcher;

    public static void setPacketDispatcher(PacketDispatcher _packetDispatcher) {
        packetDispatcher = _packetDispatcher;
    }

	public PlayerConnectionOverride(MinecraftServer minecraftserver, NetworkManager networkmanager,
			EntityPlayer entityplayer) {
		super(minecraftserver, networkmanager, entityplayer);
	}

	@Override
	public void a(PacketPlayInSteerVehicle packetPlayInSteerVehicle) {
		packetDispatcher.dispatchPacket(packetPlayInSteerVehicle, this,
				(PacketPlayInSteerVehicle packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInVehicleMove packetPlayInVehicleMove) {
		packetDispatcher.dispatchPacket(packetPlayInVehicleMove, this,
				(PacketPlayInVehicleMove packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTeleportAccept packetPlayInTeleportAccept) {
		packetDispatcher.dispatchPacket(packetPlayInTeleportAccept, this,
				(PacketPlayInTeleportAccept packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInRecipeDisplayed packetPlayInRecipeDisplayed) {
		packetDispatcher.dispatchPacket(packetPlayInRecipeDisplayed, this,
				(PacketPlayInRecipeDisplayed packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAdvancements packetPlayInAdvancements) {
		packetDispatcher.dispatchPacket(packetPlayInAdvancements, this,
				(PacketPlayInAdvancements packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTabComplete packetPlayInTabComplete) {
		packetDispatcher.dispatchPacket(packetPlayInTabComplete, this,
				(PacketPlayInTabComplete packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCommandBlock packetPlayInSetCommandBlock) {
		packetDispatcher.dispatchPacket(packetPlayInSetCommandBlock, this,
				(PacketPlayInSetCommandBlock packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCommandMinecart packetPlayInSetCommandMinecart) {
		packetDispatcher.dispatchPacket(packetPlayInSetCommandMinecart, this,
				(PacketPlayInSetCommandMinecart packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInPickItem packetPlayInPickItem) {
		packetDispatcher.dispatchPacket(packetPlayInPickItem, this,
				(PacketPlayInPickItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInItemName packetPlayInItemName) {
		packetDispatcher.dispatchPacket(packetPlayInItemName, this,
				(PacketPlayInItemName packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBeacon packetPlayInBeacon) {
		packetDispatcher.dispatchPacket(packetPlayInBeacon, this,
				(PacketPlayInBeacon packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInStruct packetPlayInStruct) {
		packetDispatcher.dispatchPacket(packetPlayInStruct, this,
				(PacketPlayInStruct packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetJigsaw packetPlayInSetJigsaw) {
		packetDispatcher.dispatchPacket(packetPlayInSetJigsaw, this,
				(PacketPlayInSetJigsaw packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTrSel packetPlayInTrSel) {
		packetDispatcher.dispatchPacket(packetPlayInTrSel, this,
				(PacketPlayInTrSel packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBEdit packetPlayInBEdit) {
		packetDispatcher.dispatchPacket(packetPlayInBEdit, this,
				(PacketPlayInBEdit packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEntityNBTQuery packetPlayInEntityNBTQuery) {
		packetDispatcher.dispatchPacket(packetPlayInEntityNBTQuery, this,
				(PacketPlayInEntityNBTQuery packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTileNBTQuery packetPlayInTileNBTQuery) {
		packetDispatcher.dispatchPacket(packetPlayInTileNBTQuery, this,
				(PacketPlayInTileNBTQuery packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInFlying packetPlayInFlying) {
		packetDispatcher.dispatchPacket(packetPlayInFlying, this,
				(PacketPlayInFlying packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBlockDig packetPlayInBlockDig) {
		packetDispatcher.dispatchPacket(packetPlayInBlockDig, this,
				(PacketPlayInBlockDig packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUseItem packetPlayInUseItem) {
		packetDispatcher.dispatchPacket(packetPlayInUseItem, this,
				(PacketPlayInUseItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {
		packetDispatcher.dispatchPacket(packetPlayInBlockPlace, this,
				(PacketPlayInBlockPlace packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSpectate packetPlayInSpectate) {
		packetDispatcher.dispatchPacket(packetPlayInSpectate, this,
				(PacketPlayInSpectate packet) -> super.a(packet));
	}

	// CraftBukkit start
	public void a(PacketPlayInResourcePackStatus packetPlayInResourcePackStatus) {
		packetDispatcher.dispatchPacket(packetPlayInResourcePackStatus, this,
				(PacketPlayInResourcePackStatus packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBoatMove packetPlayInBoatMove) {
		packetDispatcher.dispatchPacket(packetPlayInBoatMove, this,
				(PacketPlayInBoatMove packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {
		packetDispatcher.dispatchPacket(packetPlayInHeldItemSlot, this,
				(PacketPlayInHeldItemSlot packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInChat packetPlayInChat) {
		packetDispatcher.dispatchPacket(packetPlayInChat, this,
				(PacketPlayInChat packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInArmAnimation packetPlayInArmAnimation) {
		packetDispatcher.dispatchPacket(packetPlayInArmAnimation, this,
				(PacketPlayInArmAnimation packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEntityAction packetPlayInEntityAction) {
		packetDispatcher.dispatchPacket(packetPlayInEntityAction, this,
				(PacketPlayInEntityAction packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUseEntity packetPlayInUseEntity) {
		packetDispatcher.dispatchPacket(packetPlayInUseEntity, this,
				(PacketPlayInUseEntity packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInClientCommand packetPlayInClientCommand) {
		packetDispatcher.dispatchPacket(packetPlayInClientCommand, this,
				(PacketPlayInClientCommand packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInCloseWindow packetPlayInCloseWindow) {
		packetDispatcher.dispatchPacket(packetPlayInCloseWindow, this,
				(PacketPlayInCloseWindow packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInWindowClick packetPlayInWindowClick) {
		packetDispatcher.dispatchPacket(packetPlayInWindowClick, this,
				(PacketPlayInWindowClick packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAutoRecipe packetPlayInAutoRecipe) {
		packetDispatcher.dispatchPacket(packetPlayInAutoRecipe, this,
				(PacketPlayInAutoRecipe packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEnchantItem packetPlayInEnchantItem) {
		packetDispatcher.dispatchPacket(packetPlayInEnchantItem, this,
				(PacketPlayInEnchantItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot) {
		packetDispatcher.dispatchPacket(packetPlayInSetCreativeSlot, this,
				(PacketPlayInSetCreativeSlot packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTransaction packetPlayInTransaction) {
		packetDispatcher.dispatchPacket(packetPlayInTransaction, this,
				(PacketPlayInTransaction packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {
		packetDispatcher.dispatchPacket(packetPlayInUpdateSign, this,
				(PacketPlayInUpdateSign packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInKeepAlive packetPlayInKeepAlive) {
		packetDispatcher.dispatchPacket(packetPlayInKeepAlive, this,
				(PacketPlayInKeepAlive packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAbilities packetPlayInAbilities) {
		packetDispatcher.dispatchPacket(packetPlayInAbilities, this,
				(PacketPlayInAbilities packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSettings packetPlayInSettings) {
		packetDispatcher.dispatchPacket(packetPlayInSettings, this,
				(PacketPlayInSettings packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInCustomPayload packetPlayInCustomPayload) {
		packetDispatcher.dispatchPacket(packetPlayInCustomPayload, this,
				(PacketPlayInCustomPayload packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInDifficultyChange packetPlayInDifficultyChange) {
		packetDispatcher.dispatchPacket(packetPlayInDifficultyChange, this,
				(PacketPlayInDifficultyChange packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInDifficultyLock packetPlayInDifficultyLock) {
		packetDispatcher.dispatchPacket(packetPlayInDifficultyLock, this,
				(PacketPlayInDifficultyLock packet) -> super.a(packet));
	}

}
