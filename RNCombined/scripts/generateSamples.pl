#!/usr/bin/perl
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



# This script is used to generate data samples within the reactive-spanner project in the SINALGO framework.
# The script needs three parameter in the following order: density maxDensity passes
# The values represents the following:
# * density: The start density to calculate. This will be used a corresponding number of nodes in the graph. Do not use less then 4
# * maxDensity: The maximum density to calculate
# * passes: number of passes per density
#
# Attention: Be sure SINALGO calculates the right algorithm. See the CustomGlobalBatch for this.

# Attention: This script has to be run in the subdirectory scripts of the reactive-spanner workspace

# using strictly defined variables only
use strict;
use warnings;
# warn user (from perspective of caller)
use Carp;
# used to get the current directorys
use Cwd;

my $PI = 3.14159265359;
my $SQRT2 = 1.41421356237309504880;

# SINALGO arguments
my $dimX = 300; # x dimension of plane
my $dimY = 300; # y dimension of plane
my $rUDG = 30; #radius of the unit disk; should not be larger than GeometricNodeCollection/rMax
my $numRounds = 4004;	# number of rounds, sinalgo have at least to run (for BFP it should be at least 2000 because of numeric errors)
################
#STARTING POINT#
################

#switching directory to the root of the working project
my $dir = getcwd;		# should be reactive-spanner/scripts
$dir = $dir . "/..";	# switching to subdirectory
chdir $dir;


# reading starting arguments

my $density = $ARGV[0];	# in general 4 is a good value
my $maxDensity = $ARGV[1];
my $passes = $ARGV[2];
my $euclDistance = $ARGV[3]; # distance between start position and end position (used for routing)

unless (defined $density && defined $maxDensity && defined $passes)
{
	die "Missing arguments for running script. Start script with first = minimum node density, second = maximum node density and third = max passes per density! ";
}

unless ($density <= $maxDensity)
{
	die "Given first parameter for density(=$density) have to be smaller than given second parameter for the maximum density(=$maxDensity)";
}
unless ($passes > 0 && $density > 0 && $maxDensity > 0)
{
	die "Given parameter have to be positive!";
}
unless (defined $euclDistance)
{
	if($euclDistance <= 0)
	{
		die "Euclidean distance is given, but has to be positive!";
	}
	$euclDistance = 100;	
}

my $toX = $SQRT2 * $euclDistance / 2;
my $toY = $SQRT2 * $euclDistance / 2;

unless ($toX <= $dimX && $toY <= $dimY)
{
	die "Given Euclidean distance $euclDistance exceeds dimensions of the simulation area!";
}
#run from density $density until $maxDensity and for each $passes times
print "Generating $passes graphs each for node densities from $density to $maxDensity...\n";

my $startTime = time;
my $roundTime = $startTime;

for(;$density <= $maxDensity; $density += 1) {
	$roundTime = time;
	my $numNodes = int(($density * $dimX * $dimY) / ($PI * $rUDG * $rUDG));
	my $randomNodes = $numNodes - 2; 
	for(my $pass = 0; $pass < $passes; $pass += 1) {
		print "Simulation [" . ($pass + 1) ."/" . $passes . "] with density $density \n";
		print "Generating graph with $numNodes nodes... \n";	
		system("java -cp binaries/bin;binaries/jdom.jar " .
			"sinalgo.Run " .
			"-batch " .						#run the simulation in batch mode
			#"exitAfter=true exitAfter/Rounds=$numRounds " .
			"-project reactiveSpanner " .
			"-gen $randomNodes reactiveSpanner:PhysicalGraphNode Random UDG " .
			"-gen 2 reactiveSpanner:PhysicalGraphNode Line2D UDG " . # we put the routing start and destination node at fixed positions
			"-overwrite " .
			"outputToConsole=false " .					# no logging output in the terminal
			"logConfiguration=false " .					# no logging output to file
			"dimX=$dimX " .								# set the dimension
			"dimY=$dimY " .
			#"-rounds $numRounds " .					# number of rounds
			#"AutoStart=true " .               			# Automatically start communication protocol
			"UDG/rMax=$rUDG " .							# radius of the unit disk; should not be larger than GeometricNodeCollection/rMax
			"DistributionModel/Line/ToX=$toX " .		# x-coordinate of the routing destination (start position is (0,0))
			"DistributionModel/Line/ToY=$toY "		# y-coordinate of the routing destination (start position is (0,0)) 
			);		
	}
	print "Completed calculation with density " . $density . ". Current simulation time: " . (time - $roundTime) . " seconds. \n\n";
}
print "Total time elapsed: " . (time - $startTime) . " seconds. \n\n";
exit;
##############
#ENDING POINT# 
##############