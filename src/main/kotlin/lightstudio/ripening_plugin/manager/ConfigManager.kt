package lightstudio.ripening_plugin.manager

import lightstudio.ripening_plugin.Ripening_plugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(private val plugin: Ripening_plugin) {

    lateinit var recipesConfig: FileConfiguration
    lateinit var langConfig: FileConfiguration
    lateinit var mainConfig: FileConfiguration

    private lateinit var recipesFile: File
    private lateinit var langFile: File
    private lateinit var mainConfigFile: File

    init {
        loadConfigs()
    }

    fun loadConfigs() {
        recipesFile = File(plugin.dataFolder, "recipes.yml")
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false)
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile)

        langFile = File(plugin.dataFolder, "lang.yml")
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false)
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile)

        mainConfigFile = File(plugin.dataFolder, "config.yml")
        if (!mainConfigFile.exists()) {
            plugin.saveResource("config.yml", false)
        }
        mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile)
    }

    fun reloadConfigs() {
        recipesFile = File(plugin.dataFolder, "recipes.yml")
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false)
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile)

        langFile = File(plugin.dataFolder, "lang.yml")
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false)
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile)

        mainConfigFile = File(plugin.dataFolder, "config.yml")
        if (!mainConfigFile.exists()) {
            plugin.saveResource("config.yml", false)
        }
        mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile)
    }

    fun getLangString(path: String): String {
        return langConfig.getString(path) ?: "&cMessage not found: $path"
    }

    fun getFermentationStartSound(): String {
        return mainConfig.getString("sounds.fermentation_start") ?: "BLOCK_BREWING_STAND_BREW"
    }

    fun getFermentationClaimSound(): String {
        return mainConfig.getString("sounds.fermentation_claim") ?: "ENTITY_ITEM_PICKUP"
    }

    fun isShapedCraftingRequired(): Boolean {
        return mainConfig.getBoolean("recipe.shaped_crafting_required", true)
    }
}
