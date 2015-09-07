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

# edited by cyron

# This script is used to generate data samples within the reactive-spanner project in the SINALGO framework. The difference to the
# generateSamples.pl script is that for every node density a own process will be created
# The script needs three parameter in the following order: density maxDensity passes
# The values represents the following:
# * density: The start density to calculate. This will be used a corresponding number of nodes in the graph. Do not use less then 4
# * maxDensity: The maximum density to calculate
# * passes: number of passes per density
#
# Attention: Be sure SINALGO calculates the right algorithm. See the CustomGlobalBatch for this.

# using strictly defined variables only
use strict;
use warnings;
# warn user (from perspective of caller)
use Carp;

# used to get the current directorys
use Cwd;

#switching directory to the root of the working project
my $dir = getcwd;		# should be reactive-spanner/scripts
$dir = $dir . "/..";	# switching to subdirectory
chdir $dir;

################
#STARTING POINT#
################

# reading starting arguments

my $density = $ARGV[0];	# in general 4 is a good value
my $maxDensity = $ARGV[1];
my $passes = $ARGV[2];

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
	die "Given parameter have to be positive!"
}


#run from density $density until $maxDensity and for each $passes times
print "Generating $passes graphs each for node densities from $density to $maxDensity...\n";

my $startTime = time;
my $roundTime = $startTime;


unless(defined $ARGV[3])
{
for(;$density <= $maxDensity; $density += 1) {
	my $pid = fork();
    if ($pid==0) { # child
        exec("perl scripts/barriere.pl $density $density $passes"); #never returns
        die "Exec $density failed: $!\n";
    } elsif (!defined $pid) {
        warn "Fork $density failed: $!\n";
    }
}
} else {
    handleSpecial();
}
print "Total time elapsed: " . (time - $startTime) . " seconds. \n\n";
exit;
##############
#ENDING POINT# 
##############


sub handleSpecial { 
    if($density != $maxDensity)
    {
        die "impossible\n";
    }
    for(my $passBunch = 0;$passBunch < $passes; $passBunch += $ARGV[3])
    {
        my $pid = fork();
        if ($pid==0) { # child
            exec("perl scripts/barriere.pl $density $density $ARGV[3]"); #never returns
            die "Exec $density failed: $!\n";
        } elsif (!defined $pid) {
            warn "Fork $density failed: $!\n";
        }
    }
}