# (C) Matthias von Steimker - 2014
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
#
# Gnuplot script file for plotting data of BFP and reactive PDT file
# Data in file is hold as following:
# Node density, number of nodes,"UDG", mean UDG neighbors, lower bar UDG, upper bar UDG, "GG", mean GG neighbors, lower bar GG neighbors, upper bar GG neighbors, "PDT", mean PDT neighbors, lower bar PDT neighbors, upper bar PDT neighbors, "GG/UDG", mean GG/UDG, lower bar GG/UDG, upper bar GG/UDG, "PDT/UDG", mean PDT/UDG, lower bar PDT/UDG, upper bar PDT/UDG, "BFP", mean BFP, lower bar BFP, upper bar BFP, "rPDT", mean rPDT, lower bar rPDT, upper bar rPDT, "BFP/GG", mean BFP/GG, lower bar BFP/GG, upper bar BFP/GG, "rPDT/PDT", mean rPDT/PDT, lower bar rPDT/PDT, upper bar rPDT/PDT, 
# This file is called   evalBFP_rPDT.p
set autoscale                          # scale axes automatically
set xrange [2.5:23.5]
set grid
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1                            
set datafile separator " "
set term png size 1400,1000 font 'Verdana,14' 

set title "Number of neighbors in the Gabriel Graph and the Partial Delaunay Triangulation \n in context to the node density"
set xlabel "Node density"
set ylabel "Subgraph neighbors"
set key left top
set ytic 1
#set term wxt 0 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/GG+PDT_-_Density.png"
plot "../../Evaluations/data_bfp_rpdt.dat" using 1:8 notitle lc rgb "red" with lines,\
 	""using 1:8:9:10 smooth unique title 'Gabriel Graph' lc rgb "red" with yerrorbars,\
	"" using 1:12 notitle lc rgb "blue" with lines,\
	""using 1:12:13:14 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\

set title "Number of neighbors in the Gabriel Graph and in the Partial Delaunay Triangulation in ratio to the Unit disk Model \n in context to the node density"
set xlabel "Node density"
set ylabel "Neighbors in the subgraph / Neighbors in the Unit Disk Model"
set key right top
set ytic 0.1
#set term wxt 1 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/GG+PDT2UDG_-_Density.png"
plot "" using 1:16 notitle lc rgb "red" with lines , \
	""using 1:16:17:18 smooth unique title 'Gabriel Graph / Unit Disk Model' lc rgb "red" with yerrorbars, \
	"" using 1:20 notitle lc rgb "blue" with lines, \
	""using 1:20:21:22 smooth unique title 'Partial Delaunay Triangulation / Unit Disk Model' lc rgb "blue" with yerrorbars, \

set title "Number of sent control messages for the Beaconless Forwarder Planarization and the reactive Partial Delaunay Triangulation \n in context to the node density"
set xlabel "Node density"
set ylabel "Sent control messages"
set key left top
set ytic 0.5
#set term wxt 2 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/BFP+rPDT_msgs_-_Density.png"
plot "" using 1:24 notitle lc rgb "red" with lines , \
	""using 1:24:25:26 smooth unique title 'Beaconless Forwarder Planarization' lc rgb "red" with yerrorbars, \
	"" using 1:28 notitle lc rgb "blue" with lines, \
	""using 1:28:29:30 smooth unique title 'reactive Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars, \


#set term png size 1800,1000 font 'Verdana,14' 
set title "Number of sent control messages for the Beaconless Forwarder Planarization in ratio to the Gabriel Graph and the reactive Partial Delaunay\n Triangulation in ratio to the Partial Delaunay Triangulation and both in context to the node density"
set xlabel "Node density"
set ylabel "Sent control messages / Neighbors in the subgraph"
set key right top
set ytic 0.1 
#set term wxt 3 size 1800,1000 font 'Verdana,14' 
set output "../../Evaluations/BFP+rPDT_msgs2SubG_-_Density.png"
plot "" using 1:32 notitle lc rgb "red" with lines , \
	""using 1:32:33:34 smooth unique title 'Beaconless Forwarder Planarization / Gabriel Graph' lc rgb "red" with yerrorbars, \
	"" using 1:36 notitle lc rgb "blue" with lines, \
	""using 1:36:37:38 smooth unique title 'reactive Partial Delaunay Triangulation / Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars, \