package io.github.jadegg2568.listener;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameService;
import io.github.jadegg2568.SpawnType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {
    private final GameService gameService;

    public PlayerListener(GameService gameService) {
        this.gameService = gameService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Game game = getGame();
        if (game != null) {
            game.join(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Game game = getGame();
        if (game != null && game.hasPlayer(event.getPlayer())) {
            game.leave(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Game game = getGameInWorld(victim.getWorld());
        if (game == null) {
            return;
        }

        if (!game.isPlaying(victim)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = getPlayerAttacker(byEntity.getDamager());
            if (attacker != null && !game.isPlaying(attacker)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Game game = getGame();
        Player player = event.getEntity();
        if (game != null && game.isPlaying(player)) {
            game.onDie(player, player.getKiller(), false);
            event.setCancelled(true);
        }
    }

    private Player getPlayerAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    private Game getGameInWorld(World world) {
        Game game = getGame();
        return game != null && game.isLoaded() && game.getW().equals(world) ? game : null;
    }

    private Game getGame() {
        return gameService.getGame();
    }
}
