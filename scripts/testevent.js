var EquipEvent = Java.type("org.stormrealms.stormspigot.event.ChangeArmorEvent")
var ItemStack = Java.type("org.bukkit.inventory.ItemStack")
var Material = Java.type("org.bukkit.Material")
var Bukkit = Java.type("org.bukkit.Bukkit")

var event = new EquipEvent(new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND), Bukkit.getOnlinePlayers()[0], 0)
Bukkit.getPluginManager().callEvent(event)
//