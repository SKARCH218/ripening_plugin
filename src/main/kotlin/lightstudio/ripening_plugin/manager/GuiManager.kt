package lightstudio.ripening_plugin.manager


import dev.lone.itemsadder.api.CustomStack
import lightstudio.ripening_plugin.data.JarData
import lightstudio.ripening_plugin.database.DatabaseManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.concurrent.TimeUnit

class GuiManager(
    private val configManager: ConfigManager,
    private val recipeManager: RecipeManager,
    private val databaseManager: DatabaseManager
) {

    fun openJarGui(player: Player, jarData: JarData?) {
        val title = configManager.mainConfig.getString("gui-titles.jar_gui") ?: "발효기"
        val gui = Bukkit.createInventory(null, 45, title)

        // Fill all slots with border initially
        val border = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        for (i in 0..44) {
            gui.setItem(i, border)
        }

        // Clear ingredient slots
        val ingredientSlots = setOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
        ingredientSlots.forEach { gui.setItem(it, null) }

        // 명시적으로 빈 공간으로 설정
        gui.setItem(25, null)

        val currentJarData = jarData ?: databaseManager.getJarData(player.location) // Ensure jarData is not null

        if (currentJarData == null || (currentJarData.recipeId == null && currentJarData.startTime == null)) {
            // New or empty jar GUI
            gui.setItem(25, createButton("itemsadder:action_button", configManager.getLangString("start_button")))
        } else {
            // Fermenting or finished jar GUI
            val recipeId = currentJarData.recipeId
            val fermentationTime = recipeManager.getFermentationTime(recipeId!!)
            val elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - currentJarData.startTime!!)

            if (elapsedTime >= fermentationTime) {
                // Fermentation complete
                gui.setItem(25, createButton("itemsadder:action_button", configManager.getLangString("claim_button")))
            } else {
                // Fermenting
                val timeLeft = fermentationTime - elapsedTime
                val timeLeftFormatted = formatTime(timeLeft)
                val statusMessage = configManager.getLangString("fermentation_in_progress").replace("{time_left}", timeLeftFormatted)
                gui.setItem(40, createStatusItem(statusMessage))
            }
        }

        player.openInventory(gui)
    }

    private fun setGridSlots(gui: Inventory, items: List<ItemStack?>?) {
        val gridSlots = setOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
        if (items != null) {
            gridSlots.forEachIndexed { index, slot ->
                gui.setItem(slot, items.getOrNull(index))
            }
        } else {
            gridSlots.forEach { slot -> gui.setItem(slot, null) }
        }
    }

    private fun createButton(customStackId: String, displayName: String): ItemStack {
        val customStack = CustomStack.getInstance(customStackId)
        val item = customStack?.itemStack?.clone() ?: ItemStack(Material.PAPER) // Fallback to PAPER if CustomStack not found
        val meta: ItemMeta? = item.itemMeta
        meta?.setDisplayName(displayName)
        item.itemMeta = meta
        return item
    }

    private fun createStatusItem(message: String): ItemStack {
        val item = ItemStack(Material.CLOCK)
        val meta: ItemMeta? = item.itemMeta
        meta?.setDisplayName(message)
        item.itemMeta = meta
        return item
    }

    fun formatTime(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}

