package world.bentobox.islandfly.database.object;

import com.google.gson.annotations.Expose;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

@Table(name = "IslandFlyPlayerData")
public class IslandFlyPlayerData implements DataObject {
    @Expose
    String uuid;
    @Expose
    int timeSeconds;

    public IslandFlyPlayerData(String uuid, int timeSeconds) {
        this.uuid = uuid;
        this.timeSeconds = timeSeconds;
    }

    @Override
    public String getUniqueId() {
        return uuid;
    }

    @Override
    public void setUniqueId(String uuid) {
        this.uuid = uuid;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }
}
