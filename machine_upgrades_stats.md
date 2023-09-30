# Machine upgrade parts
A list of all the currently updated machine parts
## Fossil Processor
### Tanks
Affects the capacity of the water tanks.
 - Default -> 2 buckets
 - 1 -> 3 buckets
 - 2 -> 4 buckets
### Computer Chip
Affects the time taken to convert fossil to genetic material  .
 - default -> 4 minutes
 - 1 -> 3 minutes
 - 2 -> 2 minutes
 - 3 -> 1 minute
### Filter
An item that goes in the filter slot. Effects base efficiency of the fossil processor. Note that the overall efficiency 
of the base processor is the following equation: `base_efficiency * (1 - 0.75*durability)`. Essentially, the efficiency 
at full durability is the base efficiency, and at 0 durability is 25% of the base efficiency. 
 - Iron -> 25% base efficiency, 150 durability
 - Gold -> 50% base efficiency, 250 durability
 - Diamond -> 100% base efficiency, 500 durability
## Drill Extractor
### Drill Bit
Affects the time taken to convert amber into genetic material, and the rough amount of genetic material it produces.
 - Default -> 6 minutes, ~1 genetic material
 - 1 -> 5.5 minutes, ~2 genetic material
 - 2 -> 5 minutes, ~3 genetic material
 - 3 -> 4.5 minutes, ~4 genetic material
 - 4 -> 4 minutes, ~5 genetic material
 - 5 -> 3.5 minutes, ~6 genetic material
## Sequencer
### Tanks
Affects the capacity of the water, bone, sugar and plant tanks. Water is measured in buckets while bone/sugar/plant 
is measured in matter.
 - Default -> 1 bucket, 16 matter
 - 1 -> 1.5 bucket, 24 matter
 - 2 -> 2 bucket, 32 matter
 - 3 -> 2.5 bucket, 40 matter
 - 4 -> 3 bucket, 48 matter
### Computer Chip
Affects the time taken to convert the water, bone, sugar, plant matter and selected dna values into a dna test tube.  
 - Default -> 10 minutes
 - 1 -> 7 minutes
 - 2 -> 4 minutes
### Storage Type
The item used to store the data on. Gets put into the storage slot. Effects the dinosaurAge taken to convert a syringe or 
genetic material into data and put the data into the storage item. 
 - hdd -> 10 seconds
 - ssd -> 5 seconds
## 3D Printer
### Computer Chip
Affects the time taken to use embryo and bone meal and 3d print it into an artificial egg. 
 - Default -> 10 minutes
 - 1 -> 8 minutes
 - 2 -> 6 minutes
 - 3 -> 4 minutes
### Levelling Sensor
Adding this upgrade will remove the change of a broken egg occurring.
 - Default -> 10% chance
 - 1 -> 0% chance
## Incubator  
### Tanks
Affects the capacity of the plant matter tank.
 - Default -> 100 plant matter
 - 1 -> 150 plant matter 
 - 2 -> 200 plant matter
### Bulb
Affects the time taken to incubate an egg.
 - Default -> 30 minutes (18s per %)
 - 1 -> 25 minutes (15s per %)
 - 2 -> 20 minutes (12s per %)
 - 3 -> 15 minutes (9s per %)
### Container
(needs renaming)  
Affects the number of eggs that can be incubated at once on a single incubator.
 - Default -> 3 eggs
 - 1 -> 6 eggs
 - 2 -> 9 eggs
## Generator
### Turbines
Affects the amount of energy (FE) a tick from burning items.
 - Default -> 2000
 - 1 -> 3000
 - 2 -> 4000