# Gossiping with PeerSim

Every command in run in the root folder of the project if nothing else is specified.

## Building and running ##

You can build the project by simply running `make` or `ant` in the terminal.  Similarly to clean the project of class files, run `make clean` or `ant clean`.

**If the project is built with** `make` **:**

`java -cp "src:lib/*" peersim.Simulator <simulation script>`

**If the project is build with** `ant` **:**

`java -cp "classes:lib/*" peersim.Simulator <simulation script>`

## Collecting data from output ##

We only need data for one axis, i.e the x-axis, which means that there will only be a single column in the data files.

The simulation will output the *Average Path Length* if the `nl` property in `GraphStats` is set in the simulation script. If you wish to output the *Average Clustering Coefficient* instead, comment out the `nl` property, and set the `nc` property. It is important that only *one* of these properties are set at a time.

The following command will extract the data from either the *Average Path Length* or the *Average Clustering Coefficient*:

`java -cp "classes:lib/*" peersim.Simulator example/<plot script> | grep -Eo "[0-]+\.[0-9]+" > plot/<output file>`

This command will extract *In-degree Distribution* from the output of the simulation:

`java -cp "classes:lib/*" peersim.Simulator example/<plot script> | grep -Eo "[0-9]+\.[0-9]+|[0-9]{1,}" | grep -Ev "[0-9]+\.[0-9]+" > plot/<output file>`

## Generating graphs ##

We are already given two gnuplot script that works well. These are run in the `plot` directory.

Generate a graph from a plot script with the following command:

`gnuplot <gnuplot script> -p`

The `-p` flag lets the plot window survive after main gnuplot program exits.
