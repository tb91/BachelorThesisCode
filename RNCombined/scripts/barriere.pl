#!/usr/bin/perl

# used to get the current directorys
use Cwd;

#switching directory to the root of the working project
my $dir = getcwd;		# should be reactive-spanner/scripts
$dir = $dir . "/..";	# switching to subdirectory
chdir $dir;

$numRounds = 100;	# number of rounds, sinalgo have at least to run (for BFP it should be at least 2000 because of numeric errors)

$PI = 3.14159265359;
$dimX = 500; # x dimension of plane
$dimY = 500; # y dimension of plane
$qudrMin = 36;
$qudrMax = 50;
$conProbability = 0.6;

#args
$density = $ARGV[0];
$maxDensity = $ARGV[1];
$maxPasses = $ARGV[2];
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
	#due to the 1/sqrt(2) relation of rmin and rmax this formula can be used
	$numNodes = int(($density*$dimX*$dimY) / ((1+$conProbability)*$PI*$qudrMin**2)+0.99);
	for($pass = 0; $pass < $maxPasses; $pass += 1) {
		print "Simulation $round with density $density \n";
		#$numNodes = int(($density * $dimX * $dimY) / ($PI * $rUDG * $rUDG));
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
			"QUDG/rMax=$qudrMax " . 
			"QUDG/rMin=$qudrMin ");
		$round++;
		print $pass + 1 . ". pass of calculated Barriere and Barriere Extended subgraphs on StatucQUDG graph with " . $numNodes . " nodes and node density of " . $density . "\n";
		$roundTime = time;
	}
	print "Completed calculation with density " . $density . ". Current simulation time: " . (time - $roundTime) . " seconds. \n\n";
}
print "Total time elapsed: " . (time - $startTime) . " seconds. \n\n";
