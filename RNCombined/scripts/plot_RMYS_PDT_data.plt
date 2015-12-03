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

set datafile separator " "
#set output "./plots/RMYS_PDT_SpanningRatio.png"
plot "./tmp/data_RMYS_rPDT.dat" using 1 4