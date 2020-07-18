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
        if(packetDispatcher == null) super.a(packetPlayInSteerVehicle);
        else packetDispatcher.dispatchPacket(packetPlayInSteerVehicle, this,
            (PacketPlayInSteerVehicle packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInVehicleMove packetPlayInVehicleMove) {
        if(packetDispatcher == null) super.a(packetPlayInVehicleMove);
        else packetDispatcher.dispatchPacket(packetPlayInVehicleMove, this,
            (PacketPlayInVehicleMove packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTeleportAccept packetPlayInTeleportAccept) {
        if(packetDispatcher == null) super.a(packetPlayInTeleportAccept);
        else packetDispatcher.dispatchPacket(packetPlayInTeleportAccept, this,
            (PacketPlayInTeleportAccept packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInRecipeDisplayed packetPlayInRecipeDisplayed) {
        if(packetDispatcher == null) super.a(packetPlayInRecipeDisplayed);
        else packetDispatcher.dispatchPacket(packetPlayInRecipeDisplayed, this,
            (PacketPlayInRecipeDisplayed packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAdvancements packetPlayInAdvancements) {
        if(packetDispatcher == null) super.a(packetPlayInAdvancements);
        else packetDispatcher.dispatchPacket(packetPlayInAdvancements, this,
            (PacketPlayInAdvancements packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTabComplete packetPlayInTabComplete) {
        if(packetDispatcher == null) super.a(packetPlayInTabComplete);
        else packetDispatcher.dispatchPacket(packetPlayInTabComplete, this,
            (PacketPlayInTabComplete packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCommandBlock packetPlayInSetCommandBlock) {
        if(packetDispatcher == null) super.a(packetPlayInSetCommandBlock);
        else packetDispatcher.dispatchPacket(packetPlayInSetCommandBlock, this,
            (PacketPlayInSetCommandBlock packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCommandMinecart packetPlayInSetCommandMinecart) {
        if(packetDispatcher == null) super.a(packetPlayInSetCommandMinecart);
        else packetDispatcher.dispatchPacket(packetPlayInSetCommandMinecart, this,
            (PacketPlayInSetCommandMinecart packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInPickItem packetPlayInPickItem) {
        if(packetDispatcher == null) super.a(packetPlayInPickItem);
        else packetDispatcher.dispatchPacket(packetPlayInPickItem, this,
            (PacketPlayInPickItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInItemName packetPlayInItemName) {
        if(packetDispatcher == null) super.a(packetPlayInItemName);
        else packetDispatcher.dispatchPacket(packetPlayInItemName, this,
            (PacketPlayInItemName packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBeacon packetPlayInBeacon) {
        if(packetDispatcher == null) super.a(packetPlayInBeacon);
        else packetDispatcher.dispatchPacket(packetPlayInBeacon, this,
            (PacketPlayInBeacon packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInStruct packetPlayInStruct) {
        if(packetDispatcher == null) super.a(packetPlayInStruct);
        else packetDispatcher.dispatchPacket(packetPlayInStruct, this,
            (PacketPlayInStruct packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetJigsaw packetPlayInSetJigsaw) {
        if(packetDispatcher == null) super.a(packetPlayInSetJigsaw);
        else packetDispatcher.dispatchPacket(packetPlayInSetJigsaw, this,
            (PacketPlayInSetJigsaw packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTrSel packetPlayInTrSel) {
        if(packetDispatcher == null) super.a(packetPlayInTrSel);
        else packetDispatcher.dispatchPacket(packetPlayInTrSel, this,
            (PacketPlayInTrSel packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBEdit packetPlayInBEdit) {
        if(packetDispatcher == null) super.a(packetPlayInBEdit);
        else packetDispatcher.dispatchPacket(packetPlayInBEdit, this,
            (PacketPlayInBEdit packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEntityNBTQuery packetPlayInEntityNBTQuery) {
        if(packetDispatcher == null) super.a(packetPlayInEntityNBTQuery);
        else packetDispatcher.dispatchPacket(packetPlayInEntityNBTQuery, this,
            (PacketPlayInEntityNBTQuery packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTileNBTQuery packetPlayInTileNBTQuery) {
        if(packetDispatcher == null) super.a(packetPlayInTileNBTQuery);
        else packetDispatcher.dispatchPacket(packetPlayInTileNBTQuery, this,
            (PacketPlayInTileNBTQuery packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInFlying packetPlayInFlying) {
        if(packetDispatcher == null) super.a(packetPlayInFlying);
        else packetDispatcher.dispatchPacket(packetPlayInFlying, this,
            (PacketPlayInFlying packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBlockDig packetPlayInBlockDig) {
        if(packetDispatcher == null) super.a(packetPlayInBlockDig);
        else packetDispatcher.dispatchPacket(packetPlayInBlockDig, this,
            (PacketPlayInBlockDig packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUseItem packetPlayInUseItem) {
        if(packetDispatcher == null) super.a(packetPlayInUseItem);
        else packetDispatcher.dispatchPacket(packetPlayInUseItem, this,
            (PacketPlayInUseItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {
        if(packetDispatcher == null) super.a(packetPlayInBlockPlace);
        else packetDispatcher.dispatchPacket(packetPlayInBlockPlace, this,
            (PacketPlayInBlockPlace packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSpectate packetPlayInSpectate) {
        if(packetDispatcher == null) super.a(packetPlayInSpectate);
        else packetDispatcher.dispatchPacket(packetPlayInSpectate, this,
            (PacketPlayInSpectate packet) -> super.a(packet));
	}

	// CraftBukkit start
	public void a(PacketPlayInResourcePackStatus packetPlayInResourcePackStatus) {
        if(packetDispatcher == null) super.a(packetPlayInResourcePackStatus);
        else packetDispatcher.dispatchPacket(packetPlayInResourcePackStatus, this,
            (PacketPlayInResourcePackStatus packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInBoatMove packetPlayInBoatMove) {
        if(packetDispatcher == null) super.a(packetPlayInBoatMove);
        else packetDispatcher.dispatchPacket(packetPlayInBoatMove, this,
            (PacketPlayInBoatMove packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {
        if(packetDispatcher == null) super.a(packetPlayInHeldItemSlot);
        else packetDispatcher.dispatchPacket(packetPlayInHeldItemSlot, this,
            (PacketPlayInHeldItemSlot packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInChat packetPlayInChat) {
        if(packetDispatcher == null) super.a(packetPlayInChat);
        else packetDispatcher.dispatchPacket(packetPlayInChat, this,
            (PacketPlayInChat packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInArmAnimation packetPlayInArmAnimation) {
        if(packetDispatcher == null) super.a(packetPlayInArmAnimation);
        else packetDispatcher.dispatchPacket(packetPlayInArmAnimation, this,
            (PacketPlayInArmAnimation packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEntityAction packetPlayInEntityAction) {
        if(packetDispatcher == null) super.a(packetPlayInEntityAction);
        else packetDispatcher.dispatchPacket(packetPlayInEntityAction, this,
            (PacketPlayInEntityAction packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUseEntity packetPlayInUseEntity) {
        if(packetDispatcher == null) super.a(packetPlayInUseEntity);
        else packetDispatcher.dispatchPacket(packetPlayInUseEntity, this,
            (PacketPlayInUseEntity packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInClientCommand packetPlayInClientCommand) {
        if(packetDispatcher == null) super.a(packetPlayInClientCommand);
        else packetDispatcher.dispatchPacket(packetPlayInClientCommand, this,
            (PacketPlayInClientCommand packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInCloseWindow packetPlayInCloseWindow) {
        if(packetDispatcher == null) super.a(packetPlayInCloseWindow);
        else packetDispatcher.dispatchPacket(packetPlayInCloseWindow, this,
            (PacketPlayInCloseWindow packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInWindowClick packetPlayInWindowClick) {
        if(packetDispatcher == null) super.a(packetPlayInWindowClick);
        else packetDispatcher.dispatchPacket(packetPlayInWindowClick, this,
            (PacketPlayInWindowClick packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAutoRecipe packetPlayInAutoRecipe) {
        if(packetDispatcher == null) super.a(packetPlayInAutoRecipe);
        else packetDispatcher.dispatchPacket(packetPlayInAutoRecipe, this,
            (PacketPlayInAutoRecipe packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInEnchantItem packetPlayInEnchantItem) {
        if(packetDispatcher == null) super.a(packetPlayInEnchantItem);
        else packetDispatcher.dispatchPacket(packetPlayInEnchantItem, this,
            (PacketPlayInEnchantItem packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot) {
        if(packetDispatcher == null) super.a(packetPlayInSetCreativeSlot);
        else packetDispatcher.dispatchPacket(packetPlayInSetCreativeSlot, this,
            (PacketPlayInSetCreativeSlot packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInTransaction packetPlayInTransaction) {
        if(packetDispatcher == null) super.a(packetPlayInTransaction);
        else packetDispatcher.dispatchPacket(packetPlayInTransaction, this,
            (PacketPlayInTransaction packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {
        if(packetDispatcher == null) super.a(packetPlayInUpdateSign);
        else packetDispatcher.dispatchPacket(packetPlayInUpdateSign, this,
            (PacketPlayInUpdateSign packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInKeepAlive packetPlayInKeepAlive) {
        if(packetDispatcher == null) super.a(packetPlayInKeepAlive);
        else packetDispatcher.dispatchPacket(packetPlayInKeepAlive, this,
            (PacketPlayInKeepAlive packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInAbilities packetPlayInAbilities) {
        if(packetDispatcher == null) super.a(packetPlayInAbilities);
        else packetDispatcher.dispatchPacket(packetPlayInAbilities, this,
            (PacketPlayInAbilities packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInSettings packetPlayInSettings) {
        if(packetDispatcher == null) super.a(packetPlayInSettings);
        else packetDispatcher.dispatchPacket(packetPlayInSettings, this,
            (PacketPlayInSettings packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInCustomPayload packetPlayInCustomPayload) {
        if(packetDispatcher == null) super.a(packetPlayInCustomPayload);
        else packetDispatcher.dispatchPacket(packetPlayInCustomPayload, this,
            (PacketPlayInCustomPayload packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInDifficultyChange packetPlayInDifficultyChange) {
        if(packetDispatcher == null) super.a(packetPlayInDifficultyChange);
        else packetDispatcher.dispatchPacket(packetPlayInDifficultyChange, this,
            (PacketPlayInDifficultyChange packet) -> super.a(packet));
	}

	@Override
	public void a(PacketPlayInDifficultyLock packetPlayInDifficultyLock) {
        if(packetDispatcher == null) super.a(packetPlayInDifficultyLock);
        else packetDispatcher.dispatchPacket(packetPlayInDifficultyLock, this,
            (PacketPlayInDifficultyLock packet) -> super.a(packet));
	}

}
