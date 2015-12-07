# (C) Tim Budweg - 2015
#
# This software is written under the Software libre para Uso Civil (SLUC) license
#
# You can use it for any other purpose, copy, modify, and always trade with the copies 
# and redistribute modifications in respect to the SLUC license.
#
# A copy of the Software libre para Uso Civil License can be found in the file license_sluc.txt 
# The license is also available at http://www.sluc.info (both in spanish language).
#
# This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# Also the use of this software is prohibited to military personnel and to personnel 
# in industries creating offensive weapons.
# See the Software libre para Uso Civil License (in spanish language) for more details.
#


#############################
#####plot spanning ratio#####
#############################
set datafile separator ","
set output "./plots/RMYS_PDT_SpanningRatio.png"
set xrange [5:20]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "Euclidean spanning ratio of Reactive Modified Yao Step (RMYS) and Partial Delaunay Triangulation (PDT) \n with respect to the Unit Disk Graph in context to the node density. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Spanning ratio"
set key right top
set ytic 0.5
set yrange[0.0:2]

plot "./data_RMYS_rPDT.dat" using 1:8 notitle lc rgb "red" with lines,\
    ""using 1:8:9:10 title 'RMYS' lc rgb "red" with yerrorbars,\
    ""using 1:16 notitle lc rgb "blue" with lines,\
    ""using 1:16:17:18 title 'PDT' lc rgb "blue" with yerrorbars,\

##################################
#####plot hop spanning ratio#####
##################################

set datafile separator ","
set output "./plots/RMYS_PDT_HopSpanningRatio.png"
set xrange [5:20]
set yrange[0:5.5]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "Hop spanning ratio of Reactive Modified Yao Step (RMYS) and Partial Delaunay Triangulation (PDT) \n with respect to the Unit Disk Graph in context to the node density. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Hop spanning ratio"
set key right top
set ytic 1

plot "./data_RMYS_rPDT.dat" using 1:12 notitle lc rgb "red" with lines,\
    ""using 1:12:13:14 title 'RMYS' lc rgb "red" with yerrorbars,\
    ""using 1:2 notitle lc rgb "blue" with lines,\
    ""using 1:20:21:22 title 'PDT' lc rgb "blue" with yerrorbars,\



########################
#####plot neighbors#####
########################
set datafile separator ","
set output "./plots/RMYS_PDT_avrNeighbors.png"
set xrange [5:20]
set yrange[0:15]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "Average and maximal Neighbors of Reactive Modified Yao Step (RMYS) and \nPartial Delaunay Triangulation (PDT) with respect to node density. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "average Neighbors"
set key right top
set ytic 1


plot "./data_RMYS_rPDT.dat" using 1:24 notitle lc rgb "red" with lines,\
    ""using 1:24:25:26 title 'RMYS' lc rgb "red" with yerrorbars,\
    ""using 1:28 notitle lc rgb "blue" with lines,\
    ""using 1:28:29:30 title 'PDT' lc rgb "blue" with yerrorbars,\
    ""using 1:32 notitle lc rgb "red" with points,\
    ""using 1:32:33:34 title 'maximal RMYS neighbors' lc rgb "red",\
    ""using 1:36 notitle lc rgb "blue" with points,\
    ""using 1:36:37:38 title 'maximal PDT neighbors' lc rgb "blue",\


##################################
#####plot neighbors UDG ratio#####
##################################

set datafile separator ","
set output "./plots/RMYS_PDT_UDGNeighborsRatio.png"
set xrange [5:20]
set yrange[0:1]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "The average neighbors for Reactive Modified Yao Step (RMYS) and Partial Delaunay Triangulation (PDT)\nare shown per neighbors in the Unit Disk Model. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Neighbors in the subgraph /\n Neighbors in the Unit Disk Model"
set key right top
set ytic 0.1

plot "./data_RMYS_rPDT.dat" using 1:40 notitle lc rgb "red" with lines,\
    ""using 1:40:41:42 title 'RMYS' lc rgb "red" with yerrorbars,\
    ""using 1:44 notitle lc rgb "blue" with lines,\
    ""using 1:44:45:46 title 'PDT' lc rgb "blue" with yerrorbars,\

