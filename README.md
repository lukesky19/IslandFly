# IslandFly
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/IslandFly)](https://ci.codemc.org/job/BentoBoxWorld/job/IslandFly/)

Add-on for BentoBox to allow players of Gamemode Addons to fly on their island.

## How to use

1. Place the .jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml
4. Edit the config.yml if required
5. Restart the server if you make a change

## Config.yml

There are only two options in the config:

**fly-timeout**
How many seconds the addon will wait before disabling fly mode when a player exit his island.

**logout-disable-fly**
If the fly mode should be disabled when a player disconnect.

## Commands
**/is fly** - This command toggles flight **On** and **Off**

**/is tempfly** - This command toggles temporary flight **On** and **Off**

**/is flighttime** - This command can be used to retrieve your current temporary flight time.

**/[gamemode] flighttime <set | add | remove> \<player name> <time>** - This command can be used to **set**, **add**, or **remove** flight time for a player.

**/[gamemode] flighttime <get | delete> \<player name>** - This command can be used to **get** or **delete** flight time for a player.

## Permissions
**[gamemode].island.fly** - For usage of flight command.

**[gamemode].island.tempfly** - For usage of the temporary flight command.

**[gamemode].island.flighttime** - For usage of the player flight time command.

**[gamemode].admin.flighttime** - For usage of the admin flight time command.

**[gamemode].island.flybypass** - Enables user to use fly command on other islands too.

Examples:<br/>
    **bskyblock.island.fly**<br/>
    **caveblock.island.flybypass**
  



