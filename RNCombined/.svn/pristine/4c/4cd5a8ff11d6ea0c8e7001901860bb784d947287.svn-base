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
# Gnuplot script file for plotting data of Face-Routing and Greedy-Face-Routing
# Data, marked with a <<"Data name">>(position_first_value-position_last_value), in file is hold in 4 values per data set as following: "Data set name", mean Data set, lower bar Data set, upper bar Data set
#
# The values are the following('Node density' and 'number of nodes' have only one value each):
# Node density, number of nodes, <<minEuclDis>>(3-6), <<minEuclDis_UDG>>(7-10), <<minNumHops_UDG>>(11-14), <<minEuclDis_GG>>(15-18), <<minNumHops_GG>>(19-22), <<minEuclDis_PDT>>(23-26), <<minNumHops_PDT>>(27-30), <<euclDisFaceR_GG>>(31-34), <<hopsFaceR_GG>>(35-38), <<euclDisFaceR_PDT>>(39-42), <<hopsFaceR_PDT>>(43-46),<<euclDisGFG_GG>>(47-50), <<hopsGFG_GG>>(51-54), <<euclDisGFG_PDT>>(55-58), <<hopsGFG_PDT>>(59-62), <<euclDisFaceRGG/mineuclDisGG>>(63-66), <<hopDisFaceRGG/minHopDisGG>>(67-70), <<euclDisFaceRPDT/minEuclDisPDT>>(71-74), <<hopDisFaceRPDT/minHopDisPDT>>(75-78), <<euclDisGFGGG/minHopDisGG>>(79-82), <<hopDisGFGGG/minHopDisGG>>(83-86), <<euclDisGFGPDT/minEuclDisPDT>>(87-90), <<hopDisGFGPDT/minHopDisPDT>>(91-94), <<euclDisFaceRGG/minEuclDisUDG>>(95-98), <<hopDisFaceRGG/minHopDisUDG>>(99-102), <<euclDisFaceRPDT/minEuclDisUDG>>(103-106), <<hopDisFaceRPDT/minHopDisUDG>>(107-110), <<euclDisGFGGG/minEuclDisUDG>>(111-114), <<hopDisGFGGG/minHopDisUDG>>(115-118), <<euclDisGFGPDT/minEuclDisUDG>>(119-122), <<hopDisGFGPDT/minHopDisUDG>>(123-126), <<minEuclDisGG/minEuclDisUDG>>(127-130), <<minHopDisGG/minHopDisUDG>>(131-134), <<minEuclDisPDT/minEuclDisUDG>>(135-138), <<minHopDisPDT/minHopDisUDG>>(139-142)
# This file is called   evaluateData_FaceR_GF.p
set autoscale                          # scale axes automatically
set xrange [2.8:14.2]
set yrange [0.0:*]
set grid
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1                            
set datafile separator " "
set term png size 1400,1000 font 'Verdana,14' 

#set title "Shortest Euclidean distance in the Unit Disk Model, the Gabriel Graph and the Partial Delaunay Triangulation \n in context to the node density"
set title "Shortest Euclidean distance on different subgraphs and traveled by routing algorithms in context to the node density \n(5000 samples per density and each for Euclidean distances between start and destination of 100, 200 and 300; UDG radius = 30)"
set xlabel "Node density"
set ylabel "Euclidean distance traveled"
set key right top
set ytic 25
#set term wxt 0 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/ShortestEuclDistance.png"
plot "../../Evaluations/data_faceR_GFG.dat" using 1:8 notitle lc rgb "black" with lines,\
	""using 1:8:9:10 smooth unique title 'Unit Disk Graph' lc rgb "black" with yerrorbars,\
	""using 1:16 notitle lc rgb "red" with lines,\
	""using 1:16:17:18 smooth unique title 'Gabriel Graph' lc rgb "red" with yerrorbars,\
	""using 1:24 notitle lc rgb "blue" with lines,\
	""using 1:24:25:26 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\
	""using 1:32 notitle lc rgb "magenta" with lines,\
	""using 1:32:33:34 smooth unique title 'Face-Routing on the Gabriel Graph' lc rgb "magenta" with yerrorbars,\
	""using 1:48 notitle lc rgb "orange" with lines,\
	""using 1:48:49:50 smooth unique title 'Greedy-Face-Routing on the Gabriel Graph' lc rgb "orange" with yerrorbars,\
	""using 1:40 notitle lc rgb "green" with lines,\
	""using 1:40:41:42 smooth unique title 'Face-Routing on the Partial Delaunay Triangulation' lc rgb "green" with yerrorbars,\
	""using 1:56 notitle lc rgb "olive" with lines,\
	""using 1:56:57:58 smooth unique title 'Greedy-Face-Routing on the Partial Delaunay Triangulation' lc rgb "olive" with yerrorbars

#set title "Shortest Hop distance in the Unit Disk Model, the Gabriel Graph and the Partial Delaunay Triangulation \n in context to the node density"
set title "Shortest Hop distance on different subgraphs and traveled by routing algorithms in context to the node density \n(5000 samples per density and each for Euclidean distances between start and destination of 100, 200 and 300; UDG radius = 30)"
set xlabel "Node density"
set ylabel "Hop distance traveled"
set key right top
set ytic 2
#set term wxt 1 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/ShortestHopDistance.png"
plot "../../Evaluations/data_faceR_GFG.dat" using 1:12 notitle lc rgb "black" with lines ,\
	""using 1:12:13:14 smooth unique title 'Unit Disk Graph' lc rgb "black" with yerrorbars,\
	""using 1:20 notitle lc rgb "red" with lines,\
	""using 1:20:21:22 smooth unique title 'Gabriel Graph' lc rgb "red" with yerrorbars,\
	""using 1:28 notitle lc rgb "blue" with lines,\
	""using 1:28:29:30 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\
	""using 1:36 notitle lc rgb "magenta" with lines,\
	""using 1:36:37:38 smooth unique title 'Face-Routing on the Gabriel Graph' lc rgb "magenta" with yerrorbars,\
	""using 1:52 notitle lc rgb "orange" with lines,\
	""using 1:52:53:54 smooth unique title 'Greedy-Face-Routing on the Gabriel Graph' lc rgb "orange" with yerrorbars,\
	""using 1:44 notitle lc rgb "green" with lines,\
	""using 1:44:45:46 smooth unique title 'Face-Routing on the Partial Delaunay Triangulation' lc rgb "green" with yerrorbars,\
	""using 1:60 notitle lc rgb "olive" with lines,\
	""using 1:60:61:62 smooth unique title 'Greedy-Face-Routing on the Partial Delaunay Triangulation' lc rgb "olive" with yerrorbars

set title "Shortest Euclidean distance in the Gabriel Graph and the Partial Delaunay Triangulation \nand Euclidean distance traveled by Face-Routing and Greedy-Face-Routing \n in ratio to the Unit Disk Graph and in context to the node density \n(5000 samples per density and each for Euclidean distances between start and destination of 100, 200 and 300; UDG radius = 30)"
set xlabel "Node density"
set ylabel "Euclidean distance traveled relative to the shortest Euclidean path in UDG"
set key right top
set ytic 0.2
#set term wxt 0 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/ShortestEuclDistance_relative_to_UDG.png"
plot "../../Evaluations/data_faceR_GFG.dat" using 1:128 notitle lc rgb "red" with lines,\
	""using 1:128:129:130 smooth unique title 'Gabriel Graph' lc rgb "red" with yerrorbars,\
	""using 1:136 notitle lc rgb "blue" with lines,\
	""using 1:136:137:137 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\
	""using 1:96 notitle lc rgb "magenta" with lines ,\
	""using 1:96:97:98 smooth unique title 'Face-Routing on the Gabriel Graph' lc rgb "magenta" with yerrorbars,\
	""using 1:112 notitle lc rgb "orange" with lines,\
	""using 1:112:113:114 smooth unique title 'Greedy-Face-Routing on the Gabriel Graph' lc rgb "orange" with yerrorbars,\
	""using 1:104 notitle lc rgb "green" with lines,\
	""using 1:104:105:106 smooth unique title 'Face-Routing on the Partial Delaunay Triangulation' lc rgb "green" with yerrorbars,\
	""using 1:120 notitle lc rgb "olive" with lines,\
	""using 1:120:121:122 smooth unique title 'Greedy-Face-Routing on the Partial Delaunay Triangulation' lc rgb "olive" with yerrorbars

set title "Shortest Hop distance in the Gabriel Graph and the Partial Delaunay Triangulation \nand traveled by Face-Routing and Greedy-Face-Routing \nin ratio to the Unit Disk Graph and in context to the node density \n(5000 samples per density and each for Euclidean distances between start and destination of 100, 200 and 300; UDG radius = 30)"
set xlabel "Node density"
set ylabel "Hop distance traveled relative to the shortest hop distance in UDG"
set key right top
set ytic 0.2
#set term wxt 0 size 1400,1000 font 'Verdana,14' 
set output "../../Evaluations/ShortestHopDistance_relative_to_UDG.png"
plot "../../Evaluations/data_faceR_GFG.dat" using 1:132 notitle lc rgb "red" with lines,\
	""using 1:132:133:134 smooth unique title 'Gabriel Graph' lc rgb "red" with yerrorbars,\
	""using 1:140 notitle lc rgb "blue" with lines,\
	""using 1:140:141:142 smooth unique title 'Partial Delaunay Triangulation' lc rgb "blue" with yerrorbars,\
	""using 1:100 notitle lc rgb "magenta" with lines ,\
	""using 1:100:101:102 smooth unique title 'Face-Routing on the Gabriel Graph' lc rgb "magenta" with yerrorbars,\
	""using 1:116 notitle lc rgb "orange" with lines,\
	""using 1:116:117:118 smooth unique title 'Greedy-Face-Routing on the Gabriel Graph' lc rgb "orange" with yerrorbars,\
	""using 1:108 notitle lc rgb "green" with lines,\
	""using 1:108:109:110 smooth unique title 'Face-Routing on the Partial Delaunay Triangulation' lc rgb "green" with yerrorbars,\
	""using 1:124 notitle lc rgb "olive" with lines,\
	""using 1:124:125:126 smooth unique title 'Greedy-Face-Routing on the Partial Delaunay Triangulation' lc rgb "olive" with yerrorbars