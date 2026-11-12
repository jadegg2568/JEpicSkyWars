
package io.github.jadegg2568.listener;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameService;
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

public final class PlayerListener implements Listener {
    private final GameService gameService;

    public PlayerListener(GameService gameService) {
        this.gameService = gameService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Game game = getGame();
        if (game != null && game.isLoaded()) {
            game.join(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Game game = getGame();
        if (game != null && game.hasPlayer(e.getPlayer())) {
            game.leave(e.getPlayer());
        }
    }

    // DAMAGE
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        Game game = getGameInWorld(victim.getWorld());
        if (game == null) return;

        if (!game.isInGame() || !game.isPlaying(victim)) {
            e.setCancelled(true);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = getPlayerAttacker(byEntity.getDamager());

            if (attacker != null && !game.isPlaying(attacker)) {
                e.setCancelled(true);
            }
        }
    }

    // DEATH
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Game game = getGameInWorld(e.getEntity().getWorld());
        if (game == null || !game.isPlaying(e.getEntity())) return;

        game.onDie(e.getEntity(), e.getEntity().getKiller(), false);
        e.setCancelled(true);
    }

    // UTILS
    private Player getPlayerAttacker(Entity damager) {
        if (damager instanceof Player player) return player;

        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
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