var stormcore = StormCore()
var context = stormcore.getContext()
var factory = context.getAutowireCapableBeanFactory()
var allBeanNames = context.getBeanDefinitionNames();
var statRepo = wireByName("statRepo")
var InventoryOpen = Java.type("org.bukkit.event.inventory.InventoryOpenEvent").class
var PlayerLogin = Java.type("org.bukkit.event.player.PlayerLoginEvent").class
var bukkit = Java.type("org.bukkit.Bukkit")
var miscCfg = wireByName("stat-config")
var RPGStat = Java.type("org.stormrealms.stormcore.outfacing.RPGStat")
var EntityType = Java.type("org.bukkit.entity.EntityType")
var RPGEntityData = Java.type("org.stormrealms.stormspigot.entities.RPGEntityData")
var Integer = Java.type("java.lang.Integer")

registerCommand("spawncustom", (args,p)=> {
	var uuid = p.getUniqueId()
	var data = new RPGEntityData()
	var id = args[0]
	data.setId(Integer.parseInt(id))
	data.setLevel(5)
	p.getWorld().spawnCustom(p.getLocation(), data)
	var entities = p.getNearbyEntities(100,100,100)
	for(index in entities) {
		var entity = entities[index]
		log(entity.getClass().getName())
		log(entity.getHandle().getClass().getName())
		entity.setCustomName("Test")
		entity.setCustomNameVisible(true)
		//log("Spawned entity ID: "+entity.getHandle().getEntityType())
		//log(entity.getClass().getName())
	}
})

registerCommand("getentities", (args,p)=> {
	var entities = p.getNearbyEntities(100,100,100)
	for(index in entities) {
		log("ENT")
		log(entities[index].getHandle().getId())
	}
})