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
