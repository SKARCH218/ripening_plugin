package lightstudio.ripening_plugin.manager

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack
import org.bukkit.Material

class RecipeManager(private val configManager: ConfigManager) {

    fun getRecipeId(items: List<ItemStack?>): String? {
        val recipeSection = configManager.recipesConfig.getConfigurationSection("recipes") ?: return null
        val itemIds = items.mapNotNull { itemStack ->
            if (itemStack == null || itemStack.type == Material.AIR) return@mapNotNull null
            val customStack = CustomStack.byItemStack(itemStack)
            // ItemsAdder 아이템인 경우 namespacedID를, 아닌 경우 바닐라 아이템의 네임스페이스 키를 사용
            customStack?.namespacedID ?: itemStack.type.key.toString()
        }.map { it.trim() }.sorted()

        if (itemIds.isEmpty()) return null

        for (recipeId in recipeSection.getKeys(false)) {
            val requiredItems = recipeSection.getStringList("$recipeId.items").map { it.trim() }.sorted()
            if (itemIds.size == requiredItems.size && itemIds == requiredItems) {
                return recipeId
            }
        }
        return null
    }

    fun getFermentationTime(recipeId: String): Long {
        return configManager.recipesConfig.getLong("recipes.$recipeId.time", 3600)
    }

    fun getResultItem(recipeId: String): String? {
        val resultsSection = configManager.recipesConfig.getConfigurationSection("recipes.$recipeId.results") ?: return null
        val random = Math.random() * 100
        var cumulativeChance = 0.0

        for (resultKey in resultsSection.getKeys(false)) {
            val chance = resultsSection.getDouble("$resultKey.chance")
            cumulativeChance += chance
            if (random < cumulativeChance) {
                return resultsSection.getString("$resultKey.item")
            }
        }
        return null
    }
}
