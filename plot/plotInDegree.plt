#set terminal png enhanced
#set output 'plot.png'

set title "In-degree distribution"
set xlabel "in-degree"
set ylabel "number of nodes"
set xrange [0:90]
set yrange [0:800]
set key right top
set nologscale y
plot "output-indegree-Shuffle-c30.txt" title 'Basic Shuffle c = 30' with histeps, \
	"output-indegree-Shuffle-c50.txt" title 'Basic Shuffle c = 50' with histeps, \
	"output-indegree-Random-c30.txt" title 'Random c = 30' with histeps, \
	"output-indegree-Random-c50.txt" title 'Random c = 50' with histeps