# SurvivalGames
#### [Link to project on Spigot](https://www.spigotmc.org/)

## Features
* Lightweight
* Messages fully configurable
* Chest stocking fully configurable
* Helpful admin commands
* Intuitive lobby system
* Single server optimized


## Plugin Guide
### Setting up a new game
Firstly you will need to create the details of the map, execute
```
/sgadmin setup [name] [min-players] [max-players]
```

```name``` is the name of the map

```min-players``` is the minimum amount of players needed to start the game

```max-players``` is the maximum amount of players the map can hold

<br>

Next you set the map bounderies by selecting two corners of the map, like so

![demo](https://i.stack.imgur.com/aHMgv.png "Map dimentions examples")
For example 1 & 2, or 5 & 7

Once in postion of corner 1, execute
```
/sgadmin setup
```

<br>

Next the second corner of the map

- if you picked position 2, then go to position 1
- if you picked position 5, then go to position 7

Once in position of corner 2, execute
```
/sgadmin setup
```

<br>

Almost there! Now you will need to set the waiting lobby location

Go to where you want players to spawn in the waiting lobby, execute
```
/sgadmin setup
```

<br>

Finally the map needs to be scanned for chests to be filled when the
game starts, once you've added all the chests in you map, execute
```
/sgadmin setup
```

#### Congradulations, you have set up a SurvivalGames map!

<br>
<br>

### Survival Games Lobby
#### Setting spawn

To set the spawn of the lobby, execute
```
/sgadmin lobby setspawn
```

Its a good idea that you do this as when a games finishes it will try to send the players back to the game lobby

#### Map signs

Lobby signs are an important and player-friendly way of joining games, you can join games by either
1. Using the join command
2. Using the sign system

So to set up a sign, use the following format:
```
  [SG]
 [name]
 ```
 
 ```[SG]``` is always required at the top of the sign
 
 ```[name]``` is the name of the map
 
 These signs will update every half second, if the sign is removed the updating simply stops for that sign. Alternatively you can use the join command to join a map, to do this you can execute: ```/sg join [name]``` where ```name``` is the name of the map to join
 
 ### The Chest System
 
 The chest system is set up using the inital setup command and the congifuration file. The config specifies each of the items that *could* be added to a chest. Each item specifies the following:
 1. Material - Is it a sword or a stone block?
 2. Amount - How many apples should be in the chest?
 3. Probability - What is the probability of this item being placed in the chest?
 4. Enchantments - Should the axe have unbreaking?
 
 When specifing the [Materials](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) and [Enchantments](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html), please use the links provided for reference.
 
 #### The Config
 To specify each item the config should be layed out like so:
 ```YAML
 chests:
 # Minutes until each chest restock
  restock-time: 10
  
  # Maximum items that can spawn in the chest
  max-items-in-chest: 6
  
  # Minumum items that can spawn in the chest
  min-items-in-chest: 3
  items:
    '1':
      material: DIRT
      amount: 12
      probability: 80
    '2':
      material: DIAMOND_SWORD
      amount: 1
      probability: 20
      enchantments:
        - DAMAGE_ALL;3
        - DURABILITY;1
```

In the example above, two items have been added, 12 dirt blocks and 1 diamond sword with 2 enchantments. Enchantments are added in a list format where you represent both the enchantment name and level in one line, like so ```[ENCHANTMENT_NAME];[ENCHANTMENT_LEVEL]```. And yes the semi-collon is very important!

<strong>NOTE!</strong> The probability of all the items added up should equal 100

Adding a new item is as simple as copy and pasting one of the examples and incrementing the numbers. So the next item wouve have the parent ```'3'``` and so on...

### The Chat System

<strong>NOTE!</strong> You have the option to enable or disable this

When enabled, the players can only talk amongst themselves in games, and if they are in the lobby, then the players in games can only see chat messages from other players in their game.

### A Config Overview

This is an overview of the example config that has helpful comments to understand the config options more clearly

```YAML
# Should the plugin be enabled
enabled: true

# Lobby options
lobby:
  # Should players be teleported to the spawn on joinning the server
  teleport-players-to-lobby-on-join: true
  
  # The gamemode to set players on joining the server
  set-gamemode-on-join: ADVENTURE

# Chat options
chat:
  # Independent chats for each games
  per-game-chat: true

# Configurable messages for the commands of the plugin
commands:
  no-permission: "&4You do not have permission to do this!"

  sg:
    zero-arguments: "&ePlease use &b'/sg help' &eto see all command options!"
    specify-game: "&4Please specify the game name!"
    game-does-not-exist: "&4The game [%game%] does not exist!"
    already-in-game: "&4You are already in a game!"
    not-in-game: "&4You are currently not in a game!"
  sgadmin:
    zero-arguments: "&ePlease use &b'/sgadmin help' &eto see all command options!"
    set-lobby-spawn: "&2The set spawn lobby sub-command is not supported by the console!"
    chests-restocked: "&2You have restocked the chests in [%game%]!"
    specify-game-to-remove: "&4Please specify the game you would like to remove!"
    game-removed: "&2You have removed the game [%game%]!"

# The configurable messages for the event of the plugin
events:
  game:
    # The countdown time in seconds when waiting in lobby
    waiting-countdown: 15
    
    # The countdown time in seconds when starting the game
    starting-countdown: 5
    
    attempting-start: "&2&lAttempting to start the game..."
    insufficient-players: "&eNot enough players to start game! Waiting again..."
    waiting-game-countdown: "&eStarting game in &2%count%"
    starting-message: " \n&bWelcome to Survival Games! Have fun.\n "
    starting-game-countdown: "&ePrepare to run in &4%count%&e!"
    start-message: "&2&lGO GO GO"

    chest-restock: "&2All Chests have been restocked!"

    player-returning-error: "&4 You cannot return to this game once left!"
    not-joinable-error: "&4This game is currently not joinable!"
    full-game-error: "&4The game you are trying to join is full!"

    player-join: "&b[%player_count%/%max_players%] &2%player% &ehas joined the game!"
    player-leave: "&2%player% &ehas left the game!"

    player-killed: "&a%player% &7was killed by &4%killer%"
    player-died: "&4%player% &ehas died!"

# The section on the chests
chests:
  # The time in minutes for the chest to restock after the game starts
  restock-time: 10
  
  # The maximum amount of items that can be added to the chest
  max-items-in-chest: 6
  
  # The minimum amount of items that are added to the chest
  min-items-in-chest: 3
  
  # Items that can be added to the chests
  items:
    1:
      # Material of item
      material: DIRT
      
      # The item amount
      amount: 12
      
      # The probability of the item being added to the chest
      probability: 80
    2:
      material: DIAMOND_SWORD
      amount: 1
      probability: 20
      enchantments:
        - DAMAGE_ALL;3
        - DURABILITY;1
```
