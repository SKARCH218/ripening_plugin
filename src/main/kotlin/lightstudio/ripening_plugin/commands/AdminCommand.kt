package lightstudio.ripening_plugin.commands

import lightstudio.ripening_plugin.Ripening_plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AdminCommand(private val plugin: Ripening_plugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by a player.")
            return true
        }

        if (!sender.hasPermission("ripening.admin")) {
            sender.sendMessage(plugin.configManager.getLangString("no_permission"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Usage: /ripening <reload>")
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> {
                plugin.configManager.reloadConfigs()
                sender.sendMessage(plugin.configManager.getLangString("config_reloaded"))
            }
            else -> {
                sender.sendMessage("Unknown subcommand. Usage: /ripening <reload>")
            }
        }
        return true
    }
}