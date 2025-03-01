# Uncomment to create file:
set terminal png enhanced
set output 'ddPlot.png'

set title "In-degree distribution (star)"
set xlabel "in-degree"
set ylabel "number of nodes"
set key right top
set xrange [0:120]
plot "dd30.txt" title 'Basic Shuffle c = 30' with histeps, \
	"ddRandom30.txt" title 'Random Graph c = 30' with histeps, \
	"dd50.txt" title 'Basic Shuffle c = 50' with histeps, \
	"ddRandom50.txt" title 'Random Graph c = 50' with histeps