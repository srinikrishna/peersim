# Gossiping with PeerSim

Every command is run in the root folder of the project if nothing else is specified.

## Building and runing with `make` ##

You can build the project by simply running `make` in the terminal.  Similarly to clean the project of class files, run `make clean`.

**Run the program with the following command:**

`java -cp "src:lib/*" peersim.Simulator <simulation script>`

## Building and running with `ant` ##

You can build the project by simply running `ant` in the terminal.  Similarly to clean the project of class files, run `ant clean`.

**Run tthe program with the following command:**

`java -cp "classes:lib/*" peersim.Simulator <simulation script>`

## Collecting data from output ##
We only need data for one axis, i.e the y-axis, which means that there will only be a single column in the data files.

The simulation will output the *Average Path Length* if the `nl` property in `GraphStats` is set, and *Average Clustering Coefficient* if the `nc` property is set.  The properties are set in the simulation script.  If both properties are set, then value of `nc` printed out before `nl` value.

Remember to set the cache size you want in the simulation script, `example/Shuffle<topology>Example.txt`, before running the program.

**We can filter the output of the values of the *Average Path Length*, *Average Clustering Coefficient* and *In-Degree distribution* into a temporary file with the following command:**

`java -cp "classes:lib/*" peersim.Simulator example/<plot script> | grep -Eo "[0-9]+\.[0-9]+|[0-9]{1,}" > plot/<topology>/temp.txt`

This example assumes you have built the project with `ant`. 

**We further filter the temporary file into three separate data files:**

```bash
grep -Eo "[0-9]+\.[0-9]+" plot/<topology>/temp.txt | sed 'n; d' > plot/<topology>/cc<cache size>.txt
grep -Eo "[0-9]+\.[0-9]+" plot/<topology>/temp.txt | sed '1d; n; d' > plot/<topology>/apl<cache size>.txt
grep -Ev "[0-9]+\.[0-9]+" plot/<topology>/temp.txt > plot/<topology>/dd<cache size>.txt
```

To make it even easier to collect all the data (both cache sizes) for a specific topology, we have the following scripts: `collect_star` and `collect_ring`.

**If you are unable to run the script, make sure the file is executable in your system:**

```bash
chmod +x <script>
```

## Generating graphs ##

We are already given two gnuplot script that works well. These are run in the `plot` directory.

**Generate a graph from a plot script with the following command:**

`gnuplot <gnuplot script> -p`

The `-p` flag lets the plot window survive after main gnuplot program exits.
