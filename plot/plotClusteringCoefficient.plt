# You can uncomment the following lines to produce a png figure
#set terminal png enhanced
#set output 'plot.png'

set title "Average Clustering Coefficient"
set xlabel "cycles"
set ylabel "clustering coefficient (log)"
set key right top
set logscale y
plot "output-clustering-Shuffle-c30.txt" title 'Basic Shuffle c = 30' with lines, \
	"output-clustering-Shuffle-c50.txt" title 'Basic Shuffle c = 50' with lines, \
	"output-clustering-Random-c30.txt" title 'Random c = 30' with lines, \
	"output-clustering-Random-c50.txt" title 'Random c = 50' with lines