var stormcore = StormCore()
var context = stormcore.getContext()
var factory = context.getAutowireCapableBeanFactory()
var allBeanNames = context.getBeanDefinitionNames();
var stormStatsInstance = wireByName("stormStats")
var statRepo = wireByName("statRepo")
var InventoryOpen = Java.type("org.bukkit.event.inventory.InventoryOpenEvent").class
var PlayerLogin = Java.type("org.bukkit.event.player.PlayerLoginEvent").class
var bukkit = Java.type("org.bukkit.Bukkit")
var miscCfg = wireByName("stat-config")

//
registerCommand("characters", (args, sender) => {
	var charMenu = wireByName("characterMenu")
	var rP = statRepo.getBlocking(sender.getUniqueId())
	charMenu.open(sender, rP)
})


registerCommand("createcharacter", (args, sender) => {
	var charMenu = wireByName("createCharacterMenu")
	var rP = statRepo.getBlocking(sender.getUniqueId())
	charMenu.open(sender, rP)
})

/*
registerEvent(PlayerLogin, (e) => {
	statRepo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rp) => {
		log("Primary: "+bukkit.isPrimaryThread())
		if (rp.getChosenCharacter() != null) {
			return
		}
		else if (rp.getCharacters().size() == 1) {
			rp.setChosenCharacter(rp.getCharacters().iterator().next())
		}
		else if (rp.getCharacters().size() > 1) {
			var menu = factory.getBean("characterMenu");
			menu.open(e.getPlayer(), rp);
		}
		else {
			e.getPlayer().teleport(miscCfg.getConfig().getCharRoomLocation());
			var menu = wireByName("createCharacterMenu")
			menu.open(e.getPlayer(), rp);
		}
	}, 1)
})
*/