package org.stormrealms.stormspigot;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;
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
    public PlayerConnectionOverride(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);

        System.out.println("HAHA! Now your player connection is mine!!!");
    }

    @Override
    public void a(PacketPlayInSteerVehicle packetPlayInSteerVehicle) {
        System.out.println("Got packet PacketPlayInSteerVehicle");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSteerVehicle, this, (PacketPlayInSteerVehicle packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInVehicleMove packetPlayInVehicleMove) {
        System.out.println("Got packet PacketPlayInVehicleMove");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInVehicleMove, this, (PacketPlayInVehicleMove packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTeleportAccept packetPlayInTeleportAccept) {
        System.out.println("Got packet PacketPlayInTeleportAccept");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTeleportAccept, this, (PacketPlayInTeleportAccept packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInRecipeDisplayed packetPlayInRecipeDisplayed) {
        System.out.println("Got packet PacketPlayInRecipeDisplayed");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInRecipeDisplayed, this, (PacketPlayInRecipeDisplayed packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAdvancements packetPlayInAdvancements) {
        System.out.println("Got packet PacketPlayInAdvancements");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAdvancements, this, (PacketPlayInAdvancements packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTabComplete packetPlayInTabComplete) {
        System.out.println("Got packet PacketPlayInTabComplete");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTabComplete, this, (PacketPlayInTabComplete packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCommandBlock packetPlayInSetCommandBlock) {
        System.out.println("Got packet PacketPlayInSetCommandBlock");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCommandBlock, this, (PacketPlayInSetCommandBlock packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCommandMinecart packetPlayInSetCommandMinecart) {
        System.out.println("Got packet PacketPlayInSetCommandMinecart");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCommandMinecart, this, (PacketPlayInSetCommandMinecart packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInPickItem packetPlayInPickItem) {
        System.out.println("Got packet PacketPlayInPickItem");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInPickItem, this, (PacketPlayInPickItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInItemName packetPlayInItemName) {
        System.out.println("Got packet PacketPlayInItemName");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInItemName, this, (PacketPlayInItemName packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBeacon packetPlayInBeacon) {
        System.out.println("Got packet PacketPlayInBeacon");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBeacon, this, (PacketPlayInBeacon packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInStruct packetPlayInStruct) {
        System.out.println("Got packet PacketPlayInStruct");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInStruct, this, (PacketPlayInStruct packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetJigsaw packetPlayInSetJigsaw) {
        System.out.println("Got packet PacketPlayInSetJigsaw");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetJigsaw, this, (PacketPlayInSetJigsaw packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTrSel packetPlayInTrSel) {
        System.out.println("Got packet PacketPlayInTrSel");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTrSel, this, (PacketPlayInTrSel packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBEdit packetPlayInBEdit) {
        System.out.println("Got packet PacketPlayInBEdit");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBEdit, this, (PacketPlayInBEdit packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEntityNBTQuery packetPlayInEntityNBTQuery) {
        System.out.println("Got packet PacketPlayInEntityNBTQuery");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEntityNBTQuery, this, (PacketPlayInEntityNBTQuery packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTileNBTQuery packetPlayInTileNBTQuery) {
        System.out.println("Got packet PacketPlayInTileNBTQuery");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTileNBTQuery, this, (PacketPlayInTileNBTQuery packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInFlying packetPlayInFlying) {
        System.out.println("Got packet PacketPlayInFlying");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInFlying, this, (PacketPlayInFlying packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBlockDig packetPlayInBlockDig) {
        System.out.println("Got packet PacketPlayInBlockDig");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBlockDig, this, (PacketPlayInBlockDig packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUseItem packetPlayInUseItem) {
        System.out.println("Got packet PacketPlayInUseItem");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUseItem, this, (PacketPlayInUseItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {
        System.out.println("Got packet PacketPlayInBlockPlace");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBlockPlace, this, (PacketPlayInBlockPlace packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSpectate packetPlayInSpectate) {
        System.out.println("Got packet PacketPlayInSpectate");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSpectate, this, (PacketPlayInSpectate packet) -> super.a(packet));
    }

    // CraftBukkit start
    public void a(PacketPlayInResourcePackStatus packetPlayInResourcePackStatus) {
        System.out.println("Got packet PacketPlayInResourcePackStatus");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInResourcePackStatus, this, (PacketPlayInResourcePackStatus packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInBoatMove packetPlayInBoatMove) {
        System.out.println("Got packet PacketPlayInBoatMove");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInBoatMove, this, (PacketPlayInBoatMove packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {
        System.out.println("Got packet PacketPlayInHeldItemSlot");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInHeldItemSlot, this, (PacketPlayInHeldItemSlot packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInChat packetPlayInChat) {
        System.out.println("Got packet PacketPlayInChat");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInChat, this, (PacketPlayInChat packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInArmAnimation packetPlayInArmAnimation) {
        System.out.println("Got packet PacketPlayInArmAnimation");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInArmAnimation, this, (PacketPlayInArmAnimation packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEntityAction packetPlayInEntityAction) {
        System.out.println("Got packet PacketPlayInEntityAction");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEntityAction, this, (PacketPlayInEntityAction packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUseEntity packetPlayInUseEntity) {
        System.out.println("Got packet PacketPlayInUseEntity");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUseEntity, this, (PacketPlayInUseEntity packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInClientCommand packetPlayInClientCommand) {
        System.out.println("Got packet PacketPlayInClientCommand");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInClientCommand, this, (PacketPlayInClientCommand packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInCloseWindow packetPlayInCloseWindow) {
        System.out.println("Got packet PacketPlayInCloseWindow");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInCloseWindow, this, (PacketPlayInCloseWindow packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInWindowClick packetPlayInWindowClick) {
        System.out.println("Got packet PacketPlayInWindowClick");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInWindowClick, this, (PacketPlayInWindowClick packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAutoRecipe packetPlayInAutoRecipe) {
        System.out.println("Got packet PacketPlayInAutoRecipe");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAutoRecipe, this, (PacketPlayInAutoRecipe packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInEnchantItem packetPlayInEnchantItem) {
        System.out.println("Got packet PacketPlayInEnchantItem");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInEnchantItem, this, (PacketPlayInEnchantItem packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot) {
        System.out.println("Got packet PacketPlayInSetCreativeSlot");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSetCreativeSlot, this, (PacketPlayInSetCreativeSlot packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInTransaction packetPlayInTransaction) {
        System.out.println("Got packet PacketPlayInTransaction");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInTransaction, this, (PacketPlayInTransaction packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {
        System.out.println("Got packet PacketPlayInUpdateSign");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInUpdateSign, this, (PacketPlayInUpdateSign packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInKeepAlive packetPlayInKeepAlive) {
        System.out.println("Got packet PacketPlayInKeepAlive");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInKeepAlive, this, (PacketPlayInKeepAlive packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInAbilities packetPlayInAbilities) {
        System.out.println("Got packet PacketPlayInAbilities");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInAbilities, this, (PacketPlayInAbilities packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInSettings packetPlayInSettings) {
        System.out.println("Got packet PacketPlayInSettings");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInSettings, this, (PacketPlayInSettings packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInCustomPayload packetPlayInCustomPayload) {
        System.out.println("Got packet PacketPlayInCustomPayload");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInCustomPayload, this, (PacketPlayInCustomPayload packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInDifficultyChange packetPlayInDifficultyChange) {
        System.out.println("Got packet PacketPlayInDifficultyChange");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInDifficultyChange, this, (PacketPlayInDifficultyChange packet) -> super.a(packet));
    }

    @Override
    public void a(PacketPlayInDifficultyLock packetPlayInDifficultyLock) {
        System.out.println("Got packet PacketPlayInDifficultyLock");
        PacketSubscriptionManager
            .getInstance()
            .dispatchPacket(packetPlayInDifficultyLock, this, (PacketPlayInDifficultyLock packet) -> super.a(packet));
    }

}
