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
var RPGStat = Java.type("org.stormrealms.stormcore.outfacing.RPGStat")


registerCommand("setstat", (args,p)=> {
	var uuid = p.getUniqueId()
	log(uuid)
	statRepo.getRecord(p.getUniqueId()).thenAccept((rpgPlayer)=> {
		log(rpgPlayer)
		if(rpgPlayer.getChosenCharacter()==null) {
			p.sendMessage("No chosen character, cannot set stat!")
			return
		}
		if(rpgPlayer.getChosenCharacter()!=null) {
			var char = rpgPlayer.getChosenCharacter()
			char.getStats().put(RPGStat.valueOf(args[0].toUpperCase()), parseInt(args[1]))
			statRepo.save(rpgPlayer)
		}
	})
})