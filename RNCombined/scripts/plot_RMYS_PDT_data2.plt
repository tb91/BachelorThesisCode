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
set output "./plots/RMYS_PDT_Neighbors.png"
set xrange [5:20]
unset log                              # remove any log-scaling
unset label                            # remove any previous labels
set xtic 1    
set term png size 1100,600 font 'Verdana,14' 

set title "... 1000 Simulations per density."
set xlabel "Node density"
set ylabel "Number of Messages"
set key right top
set ytic 5
set yrange[0.0:50]

plot "./data_RMYS_rPDT.dat" using 1:3 notitle lc rgb "red" with lines,\
    ""using 1:3:4:5 title 'Beaconing Messages' lc rgb "red" with yerrorbars,\
    ""using 1:7 notitle lc rgb "blue" with lines,\
    ""using 1:7:8:9 title 'RMYSMessages' lc rgb "blue" with yerrorbars,\
    ""using 1:11 notitle lc rgb "green" with lines,\
    ""using 1:11:12:13 title 'PDTMessages' lc rgb "green" with yerrorbars,\


