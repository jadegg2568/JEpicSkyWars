package io.github.jadegg2568;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class EpicSkyWarsCommand implements CommandExecutor, TabCompleter {
    private final EpicSkyWars main;
    private final GameService gameService;

    public EpicSkyWarsCommand(EpicSkyWars main, GameService gameService) {
        this.main = main;
        this.gameService = gameService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эту команду может использовать только игрок."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String subcommand = args.length == 0 ? "join" : args[0].toLowerCase(Locale.ROOT);
        Game game = gameService.getGame();
        switch (subcommand) {
            case "join" -> {
                if (game.hasPlayer(player)) {
                    sender.sendMessage(Component.text("Вы уже находитесь в игре.", TextColor.color(0xFF0F00)));
                } else if (game.isFull()) {
                    sender.sendMessage(Component.text("Игра заполнена.", TextColor.color(0xFF0F00)));
                } else {
                    sender.sendMessage(Component.text("Подключение...", TextColor.color(0x42ffe6)));
                    game.join(player);
                }
            }
            case "leave" -> {
                if (!game.hasPlayer(player)) {
                    sender.sendMessage(Component.text("Вы не находитесь в игре.", TextColor.color(0xFF0F00)));
                } else {
                    game.leave(player);
                    sender.sendMessage(Component.text("Вы вышли из игры.", TextColor.color(0xFF0F00)));
                }
            }
            case "setspawn" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Недостаточно аргов!", TextColor.color(0xFF0F00)));
                    break;
                }
                String name = args[1].toUpperCase(Locale.ROOT);

                // parse type
                SpawnType type;
                try {
                    type = SpawnType.valueOf(name);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Неверная локация: " + name, TextColor.color(0xFF0F00)));
                    break;
                }
                Location spawn = player.getLocation();

                // set spawn
                String path = "game.locations." + type.name().toLowerCase(Locale.ROOT);
                main.getConfig().set(path, spawn);
                main.saveConfig();
                sender.sendMessage(Component.text(
                        "Spawn " + type.name().toLowerCase(Locale.ROOT) + " saved. Restart the server to apply it.",
                        TextColor.color(0x42ffe6)
                ));
            }
            case "fakeban", "fakehypixelban" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Недостаточно аргов!", TextColor.color(0xFF0F00)));
                    break;
                }
                String name = args[1];
                Player p = Bukkit.getPlayer(name);

                p.kick(Component.text(
                        ChatColor.RED + "You are temporarily banned for " + ChatColor.WHITE + "179d 23h 59m 59s" + ChatColor.RED + " from this server!\n"
                                + ChatColor.GRAY + "Reason: " + ChatColor.WHITE + "You're a fucking nigga, really man, change your fucking skin.\n"
                                + ChatColor.GRAY + "Find out more: " + ChatColor.AQUA + "https://hypixel.net/appeal\n\n"
                                + ChatColor.GRAY + "Sharing your Ban ID may affect the processing of your appeal!"
                ));
            }
            default -> sendUsage(sender, label);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return matching(args[0], Stream.of("join", "leave", "setspawn", "fakeban", "fakehypixelban"));
        }
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase(Locale.ROOT);
            return switch (subcommand) {
                case "setspawn" -> matching(args[1], Stream.of(SpawnType.values())
                        .map(type -> type.name().toLowerCase(Locale.ROOT)));
                case "fakeban", "fakehypixelban" -> matching(args[1], Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName));
                default -> List.of();
            };
        }
        return List.of();
    }

    private List<String> matching(String input, Stream<String> options) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return options
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("Использование: /" + label + " [join|leave|fakehypixelban]", TextColor.color(0x42ffe6)));
    }
}
