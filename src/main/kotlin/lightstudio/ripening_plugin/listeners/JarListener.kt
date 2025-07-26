package lightstudio.ripening_plugin.listeners

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
import lightstudio.ripening_plugin.data.JarData
import lightstudio.ripening_plugin.database.DatabaseManager
import lightstudio.ripening_plugin.manager.ConfigManager
import lightstudio.ripening_plugin.manager.GuiManager
import lightstudio.ripening_plugin.manager.RecipeManager
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import org.bukkit.Material
import dev.lone.itemsadder.api.CustomBlock
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

class JarListener(
    private val databaseManager: DatabaseManager,
    private val guiManager: GuiManager,
    private val recipeManager: RecipeManager,
    private val configManager: ConfigManager
) : Listener {

    private val openInventories = ConcurrentHashMap<UUID, Location>()

    @EventHandler
    fun onJarInteract(event: FurnitureInteractEvent) {
        val furniture = event.furniture ?: return
        if (furniture.getNamespacedID() != "ripening:fermentation_jar") return
        event.isCancelled = true

        val player = event.player
        val location = event.getBukkitEntity().getLocation()
        val jarData = databaseManager.getJarData(location)

        if (jarData != null && jarData.recipeId != null && jarData.startTime != null) {
            val fermentationTime = recipeManager.getFermentationTime(jarData.recipeId!!)
            val elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - jarData.startTime!!)

            if (elapsedTime >= fermentationTime) {
                // Fermentation complete - give result directly
                val resultItemIds = recipeManager.getResultItems(jarData.recipeId!!)
                if (resultItemIds != null) {
                    for (resultItemId in resultItemIds) {
                        val resultItem = if (resultItemId.startsWith("minecraft:")) {
                            val material = Material.matchMaterial(resultItemId.substringAfter("minecraft:").uppercase())
                            if (material != null) ItemStack(material) else null
                        } else {
                            CustomStack.getInstance(resultItemId)?.itemStack
                        }

                        if (resultItem != null) {
                            player.inventory.addItem(resultItem)
                        }
                    }
                }
                databaseManager.deleteJarData(location)
                player.sendMessage(configManager.getLangString("fermentation_complete"))
                player.playSound(player.location, org.bukkit.Sound.valueOf(configManager.getFermentationClaimSound()), 1.0f, 1.0f)
                return // End interaction here
            } else {
                // Fermenting - show time left
                val timeLeft = fermentationTime - elapsedTime
                val timeLeftFormatted = guiManager.formatTime(timeLeft)
                player.sendMessage(configManager.getLangString("fermentation_in_progress").replace("{time_left}", timeLeftFormatted))
                return
            }
        }
        // If not fermenting (empty jar), or fermentation is complete, open GUI
        guiManager.openJarGui(player, jarData)
        openInventories[player.uniqueId] = location
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!openInventories.containsKey(player.uniqueId)) {
            return
        }

        val location = openInventories[player.uniqueId]!!
        val clickedItem = event.currentItem ?: run {
            return
        }
        val customStack = CustomStack.byItemStack(clickedItem)

        val clickedSlot = event.rawSlot
        val guiInventory = event.inventory
        val playerInventory = event.whoClicked.inventory

        val ingredientSlots = setOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
        val actionButtonSlot = 25 // New position for the action button

        val startButtonName = configManager.getLangString("start_button")
        val claimButtonName = configManager.getLangString("claim_button")

        // Handle clicks on the action button
        if (clickedSlot == actionButtonSlot && clickedItem.hasItemMeta() && clickedItem.itemMeta.hasDisplayName()) {
            val displayName = clickedItem.itemMeta.displayName
            if (displayName == startButtonName || displayName == claimButtonName) {
                event.isCancelled = true // Always cancel button clicks
                val jarData = databaseManager.getJarData(location)

                if (jarData == null || (jarData.recipeId == null && jarData.startTime == null)) {
                    // Start fermentation
                    val items = ingredientSlots.mapNotNull { guiInventory.getItem(it) }
                    val recipeId = recipeManager.getRecipeId(items)

                    if (recipeId != null) {
                        val inputSlots = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)

                        for (slot in inputSlots) {
                            val item = guiInventory.getItem(slot)
                            if (item != null && item.amount > 1) {
                                val itemToReturn = item.clone()
                                itemToReturn.amount = item.amount - 1
                                player.inventory.addItem(itemToReturn) // Return excess to player

                                item.amount = 1 // Keep only one for the recipe
                                guiInventory.setItem(slot, item) // Update GUI slot
                                player.sendMessage("§c[알림] §f중첩된 아이템 중 초과분만 반환되었습니다.")
                            }
                        }

                        val newJarData = JarData(location.world.name, location.blockX, location.blockY, location.blockZ, player.uniqueId, recipeId, System.currentTimeMillis())
                        databaseManager.saveJarData(newJarData)
                        player.closeInventory()
                        player.sendMessage(configManager.getLangString("fermentation_started"))
                        player.playSound(player.location, org.bukkit.Sound.valueOf(configManager.getFermentationStartSound()), 1.0f, 1.0f)
                    } else {
                        val invalidRecipeMessage = configManager.getLangString("invalid_recipe")
                        player.sendMessage(invalidRecipeMessage)
                    }
                } else {
                    val fermentationTime = recipeManager.getFermentationTime(jarData.recipeId!!)
                    val elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - jarData.startTime!!)

                    if (elapsedTime >= fermentationTime) {
                        // Claim result
                        val resultItemIds = recipeManager.getResultItems(jarData.recipeId ?: return)
                        if (resultItemIds != null) {
                            for (resultItemId in resultItemIds) {
                                val resultItem = if (resultItemId.startsWith("minecraft:")) {
                                    val material = Material.matchMaterial(resultItemId.substringAfter("minecraft:").uppercase())
                                    if (material != null) ItemStack(material) else null
                                } else {
                                    CustomStack.getInstance(resultItemId)?.itemStack
                                }

                                if (resultItem != null) {
                                    player.inventory.addItem(resultItem)
                                }
                            }
                        }
                        databaseManager.deleteJarData(location)
                        player.closeInventory()
                        player.sendMessage(configManager.getLangString("fermentation_complete"))
                    } else {
                        // Fermentation is in progress, do nothing (cancel button removed)
                        player.sendMessage(configManager.getLangString("fermentation_in_progress").replace("{time_left}", guiManager.formatTime(fermentationTime - elapsedTime)))
                    }
                }
                return // Exit after handling button click
            }
        }

        // Handle clicks within the GUI (top inventory)
        if (event.clickedInventory == guiInventory) {
            // Allow moving items within ingredient slots
            if (clickedSlot in ingredientSlots) {
                // Allow taking items out of ingredient slots
                if (event.cursor == null || event.cursor?.type == Material.AIR) {
                    return // Allow taking item
                }
                // Allow placing items into ingredient slots
                if (clickedItem.type == Material.AIR) {
                    return // Allow placing item
                }
            }
            // Cancel clicks on border slots or other non-ingredient/non-button slots
            event.isCancelled = true
        }

        // Handle clicks within the player's inventory (bottom inventory)
        if (event.clickedInventory == playerInventory) {
            // Allow moving items from player inventory to ingredient slots
            if (event.isShiftClick) {
                // Find an empty ingredient slot
                val emptySlot = ingredientSlots.firstOrNull { guiInventory.getItem(it) == null || guiInventory.getItem(it)?.type == Material.AIR }
                if (emptySlot != null) {
                    guiInventory.setItem(emptySlot, clickedItem)
                    event.currentItem?.amount = 0 // Remove from player inventory
                    event.isCancelled = true
                }
            } else {
                // Allow normal drag-and-drop from player inventory
                return
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val location = openInventories.remove(player.uniqueId) ?: return

        val jarData = databaseManager.getJarData(location)

        // Only return items if fermentation is not in progress
        if (jarData == null || jarData.recipeId == null || jarData.startTime == null) {
            val guiInventory = event.inventory
            val ingredientSlots = setOf(12, 13, 14, 21, 22, 23, 30, 31, 32)

            for (slot in ingredientSlots) {
                val item = guiInventory.getItem(slot)
                if (item != null && item.type != Material.AIR) {
                    player.inventory.addItem(item)
                    guiInventory.setItem(slot, null) // Clear the slot in the GUI
                }
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val placedBlock = event.blockPlaced
        val customBlock = CustomBlock.byAlreadyPlaced(placedBlock)
        if (customBlock?.id == "ripening:fermentation_jar") {
            val location = placedBlock.location
            val jarData = JarData(
                location.world.name,
                location.blockX,
                location.blockY,
                location.blockZ,
                event.player.uniqueId,
                null,
                null
            )
            databaseManager.saveJarData(jarData)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val brokenBlock = event.block
        val customBlockAtLocation = CustomBlock.byAlreadyPlaced(brokenBlock)
        if (customBlockAtLocation?.id == "ripening:fermentation_jar") {
            val location = brokenBlock.location
            databaseManager.deleteJarData(location)
        }
    }
}

