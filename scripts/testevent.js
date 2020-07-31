var EquipEvent = Java.type("org.stormrealms.stormspigot.event.ChangeGearEvent")
var ItemStack = Java.type("org.bukkit.inventory.ItemStack")
var Material = Java.type("org.bukkit.Material")
var Bukkit = Java.type("org.bukkit.Bukkit")
var ItemClickEvent = Java.type("org.bukkit.event.inventory.InventoryClickEvent")
var CharacterStatMenu = Java.type("org.stormrealms.stormcombat.combatsystem.menus.CharacterStatMenu")

registerCommand("statmenu", (args,p)=> {
	var menu = wireByClass(CharacterStatMenu.class)
	var statRepo = wireByName("statRepo")
	log("Getting record")
	statRepo.getRecord(p.getUniqueId()).thenAccept((record)=> {
		log("Then accept")
		log(record)
		menu.open(p,record.getChosenCharacter())
	})
})