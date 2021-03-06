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
#####plot all messages#######
#############################
set datafile separator ","
set output "./plots/RMYS_PDT_Beaconing_Neighbors.png"
set xrange [5:20]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1250,600 font 'Verdana,14' 

#set title "This plot shows the needed messages to construct the RMYS-neighborhood on a given node with respect\nto the node density in a 2-hop beaconing approach and in a reactive way. Additionally, the messages rPDT\nuses to construct the PDT-neighborhood of a node and its neighbors are shown. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Number of Messages"
set key left top
set ytic 25
set yrange[0.0:500]

plot "./data_RMYS_rPDT2.dat" using 1:3 notitle lc rgb "red" with lines,\
    ""using 1:3:4:5 title 'Beaconing Messages' lc rgb "red" with yerrorbars,\
    ""using 1:7 notitle lc rgb "blue" with lines,\
    ""using 1:7:8:9 title 'RMYSMessages' lc rgb "blue" with yerrorbars,\
    ""using 1:11 notitle lc rgb "green" with lines,\
    ""using 1:11:12:13 title 'PDTMessages on Neighborhood' lc rgb "green" with yerrorbars,\
    ""using 1:15 notitle lc rgb "olive" with lines,\
    ""using 1:15:16:17 title 'PDTMessages' lc rgb "olive" with yerrorbars


#############################
#####plot RMYS rPDT messages#
#############################
set datafile separator ","
set output "./plots/RMYS_PDT_Neighbors.png"
set xrange [5:20]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1250,600 font 'Verdana,14' 

#set title "The messages needed to construct the RMYS-neighborhood and the \nPDT-neighborhood of a node and its neighbors are shown. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Number of Messages"
set key left top
set ytic 5
set yrange[0.0:55]

plot "./data_RMYS_rPDT2.dat" using 1:7 notitle lc rgb "red" with lines,\
    ""using 1:7:8:9 title 'RMYSMessages' lc rgb "red" with yerrorbars,\
    ""using 1:11 notitle lc rgb "blue" with lines,\
    ""using 1:11:12:13 title 'PDTMessages on Neighborhood' lc rgb "blue" with yerrorbars,\
    ""using 1:15 notitle lc rgb "olive" with lines,\
    ""using 1:15:16:17 title 'PDTMessages' lc rgb "olive" with yerrorbars


###############################################
#####plot RMYS rPDT messages per neighbors#####
###############################################
set datafile separator ","
set output "./plots/RMYS_PDT_MessagesPerNeighbors.png"
set xrange [5:20]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1250,600 font 'Verdana,14' 

#set title "The messages needed to construct the RMYS-neighborhood and the \nPDT-neighborhood of a node and its neighbors are shown. 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Number of Messages / Neighbors in the subgraph"
set key left top
set ytic 1
set yrange[0.0:9.5]

plot "./data_RMYS_rPDT2.dat" using 1:19 notitle lc rgb "red" with lines,\
    ""using 1:19:20:21 title 'RMYSMessages' lc rgb "red" with yerrorbars,\
    ""using 1:23 notitle lc rgb "blue" with lines,\
    ""using 1:23:24:25 title 'PDTMessages on Neighborhood' lc rgb "blue" with yerrorbars,\
    ""using 1:27 notitle lc rgb "green" with lines,\
    ""using 1:27:28:29 title 'PDTMessages' lc rgb "green" with yerrorbars,\