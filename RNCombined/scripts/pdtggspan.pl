#!/usr/bin/perl

# used to get the current directorys
use Cwd;

#switching directory to the root of the working project
my $dir = getcwd;		# should be reactive-spanner/scripts
$dir = $dir . "/..";	# switching to subdirectory
chdir $dir;

$numRounds = 4004;	# number of rounds, sinalgo have at least to run (for BFP it should be at least 2000 because of numeric errors)

$PI = 3.14159265359;
$dimX = 200; # x dimension of plane
$dimY = 200; # y dimension of plane
$rUDG = 30; #radius of the unit disk; should not be larger than GeometricNodeCollection/rMax

print "Please type in the number of graphs per node density that should be generated:";
$maxPasses = <>;
$density = 4;
$maxDensity = 16;
#run from density $density until $maxDensity and for each $maxPasses times
print "Generating $maxPasses graphs for node densities from $density to $maxDensity...";

#counting down from 3 for start
#for ($countdown = 3;$countdown > 0; $countdown -=1) {
#   printf("Starting in $countdown seconds...");
#   sleep(1);
#}


$startTime = time;
$roundTime = $startTime;

for($density; $density <= $maxDensity; $density += 1) {
	for($pass = 0; $pass < $maxPasses; $pass += 1) {
		print "Simulation $round with density $density \n";
		$numNodes = int(($density * $dimX * $dimY) / ($PI * $rUDG * $rUDG));
		print "Generating graph with $numNodes nodes... \n";
		system("java -Xmx1000m -cp \"binaries/bin;binaries/jdom.jar\" sinalgo.Run " .
			"-batch " .						#run the simulation in batch mode
			#"-overwrite " .					# Overwrite configuration file parameters
			#"exitAfter=true exitAfter/Rounds=$numRounds " .
			"-project reactiveSpanner " .
			"-gen $numNodes reactiveSpanner:PhysicalGraphNode Random " .
			"-overwrite " .
			"dimX=$dimX " .						#set the dimension
			"dimY=$dimY " .
			"-rounds $numRounds " .		# number of rounds. Attention: Look at BFP.MaximumTimeout
			#"AutoStart=true " .               # Automatically start communication protocol
			"UDG/rMax=$rUDG ");					#radius of the unit disk; should not be larger than GeometricNodeCollection/rMax	
		$round++;
		print $pass + 1 . ". pass of calculated GGSpan and PDTSpan subgraphs on UDG graph with " . $numNodes . " nodes and node density of " . $density . "\n";
		$roundTime = time;
	}
	print "Completed calculation with density " . $density . ". Current simulation time: " . (time - $roundTime) . " seconds. \n\n";
}
print "Total time elapsed: " . (time - $startTime) . " seconds. \n\n";
