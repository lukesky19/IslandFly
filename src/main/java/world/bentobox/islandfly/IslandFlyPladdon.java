//
// Created by BONNe
// Copyright - 2022
//
package world.bentobox.islandfly;

import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 * This acts a way for other plugins to get the {@link IslandFlyAddon}.
 */
public class IslandFlyPladdon extends Pladdon {
    private @Nullable Addon addon;

    /**
     * Get the {@link IslandFlyAddon}.
     * @return The {@link Addon} for the {@link IslandFlyAddon}.
     */
    @Override
    public Addon getAddon() {
        if(addon == null) {
            addon = new IslandFlyAddon();
        }

        return addon;
    }
}
