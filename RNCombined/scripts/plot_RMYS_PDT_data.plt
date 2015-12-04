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

#set autoscale                          # scale axes automatically
set datafile separator ","
set output "./plots/RMYS_PDT_SpanningRatio.png"
set xrange [5:20]
#set grid  #show grid
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "Euclidean spanning ratio of Reactive Modified Yao Step (RMYS) and Partial Delaunay Triangulation (PDT) \n with respect to the Unit Disk Graph in context to the node density. 16 Simulations per density."
set xlabel "Node density"
set ylabel "Spanning ratio"
set key right top
set ytic 0.5
set yrange[0.0:2]

plot "./data_RMYS_rPDT.dat" using 1:8 notitle lc rgb "red" with lines,\
    ""using 1:8:9:10 smooth unique title 'Reactive Modified Yao Step' lc rgb "red" with yerrorbars,\
    ""using 1:16 notitle lc rgb "blue" with lines,\
    ""using 1:16:17:18 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\
