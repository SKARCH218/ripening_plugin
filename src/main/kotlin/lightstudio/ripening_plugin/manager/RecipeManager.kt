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
        }.map { it.trim() }

        if (itemIds.isEmpty()) return null

        for (recipeId in recipeSection.getKeys(false)) {
            val requiredItemsConfig = recipeSection.getStringList("$recipeId.items")
            val requiredItemsParsed = requiredItemsConfig.map { it.trim().split(';').map { s -> s.trim() } }

            if (configManager.isShapedCraftingRequired()) {
                // Shaped crafting logic (order matters)
                if (itemIds.size != requiredItemsParsed.size) {
                    continue
                }

                var matches = true
                for (i in itemIds.indices) {
                    val currentCraftedItem = itemIds[i]
                    val possibleRequiredItems = requiredItemsParsed[i]

                    if (currentCraftedItem !in possibleRequiredItems) {
                        matches = false
                        break
                    }
                }

                if (matches) {
                    return recipeId
                }
            } else {
                // Shapeless crafting logic (order does not matter)
                // Generate all possible combinations of required items
                val possibleRequiredItemCombinations = generateCombinations(requiredItemsParsed)

                var foundMatch = false
                for (possibleCombination in possibleRequiredItemCombinations) {
                    if (itemIds.size != possibleCombination.size) {
                        continue
                    }

                    val itemCounts = itemIds.groupingBy { it }.eachCount()
                    val requiredItemCounts = possibleCombination.groupingBy { it }.eachCount()

                    if (itemCounts == requiredItemCounts) {
                        foundMatch = true
                        break
                    }
                }

                if (foundMatch) {
                    return recipeId
                }
            }
        }
        return null
    }

    fun getFermentationTime(recipeId: String): Long {
        return configManager.recipesConfig.getLong("recipes.$recipeId.time", 3600)
    }

    fun getResultItems(recipeId: String): List<String>? { // Changed function name and return type
        val resultsSection = configManager.recipesConfig.getConfigurationSection("recipes.$recipeId.results") ?: return null
        val random = Math.random() * 100
        var cumulativeChance = 0.0

        for (resultKey in resultsSection.getKeys(false)) {
            val chance = resultsSection.getDouble("$resultKey.chance")
            cumulativeChance += chance
            if (random < cumulativeChance) {
                val itemValue = resultsSection.get("$resultKey.item") // Get as Any?
                return when (itemValue) {
                    is String -> listOf(itemValue) // If it's a single string, wrap it in a list
                    is List<*> -> itemValue.filterIsInstance<String>() // If it's a list, filter for strings
                    else -> null
                }
            }
        }
        return null
    }

    private fun generateCombinations(input: List<List<String>>): List<List<String>> {
        if (input.isEmpty()) {
            return listOf(emptyList())
        }

        val firstList = input.first()
        val restOfLists = input.drop(1)

        val combinationsOfRest = generateCombinations(restOfLists)

        val result = mutableListOf<List<String>>()
        for (itemInFirst in firstList) {
            for (combinationInRest in combinationsOfRest) {
                result.add(listOf(itemInFirst) + combinationInRest)
            }
        }
        return result
    }
}
