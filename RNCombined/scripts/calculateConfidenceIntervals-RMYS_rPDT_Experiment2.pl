#!/usr/bin/perl
# (C) Matthias von Steimker - 2014 adapted by Tim Budweg - 12/2015
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



# This script is used to calculate confidence intervals of the of the data generated by generateSamples-BFP_rPDT.pl
# The result will be saved in a easy to read file defined by the $DEBUG_OUTPATH and in the GNU_OUTPATH that is easy to
# handle with Gnuplot to plot the data. Data will be summarized and the mean will be calculated. Also lower and upper bound 
# of the confidence intervall will be saved
# The Data in the GNU_OUTPATH will be saved in the format VALUE_NAME mean upper_bound lower_bound
# Therefore a dataset for a value is represented by 4 values. The VALUE_NAME is only used to show what the following three values represents

# using strictly defined variables only
use strict;
use warnings;
# warn user (from perspective of caller)
use Carp;

my $MARGIN_ERROR = 1.96;
my $DEBUG_OUTPATH = "./debug_confiIntervals-Routing.csv";
my $GNU_OUTPATH = "./data_RMYS_rPDT.dat";

my $IN_SPLIT_CHAR = " ";
my $OUT_SPLIT_CHAR = ",";

################
#STARTING POINT#
################
#initialize maps for all the value types that are stored in the file
my %mapRMYSMessages = ();
my %mapPDTMessages = ();
my %mapBeaconingMessages = ();

my %mapNumDataSamples = ();

# print OUT "$_:\n" or die "Something went wrong during writing to $DEBUG_OUTPATH. Aborted.\n";
#parsing of the files
foreach (@ARGV)
{
	#system("perl validateData-FaceR_GFG.pl $_"); #validate the data before calculating confidence intervals
	readFileAndSaveValsInHashMaps($_);
}
#saveToDebugFile();
saveToGNUPlotConformFile();
exit;
##############
#ENDING POINT# 
##############

# read a file in fixed form and save values in hashmaps with densities as keys
sub readFileAndSaveValsInHashMaps {
	my ($filepath) = @_;
	unless (defined $filepath)
	{
		die "readFileAndSaveValsInHashMaps needs an argument!";
	}
	#do not continue if file does not exist
	unless(-e $filepath)
	{
		die "No file of given filepath $filepath exists! \n";
	}
	print "Parsing file \"" . $filepath . "\"... \n";

	#open file in reading mode
	open (FILE, "<" , $filepath) or die "Cannot read the input file $filepath! \n";
	while (<FILE>) 
	{
		chomp; # kill newlines
		s/^\s+//;  # remove leading whitespace
    	s/\s+$//; # remove trailing whitespace
    	next unless length; # next rec unless anything left

		my ($density, $UDGNeighbors, $beaconingMessages, $neighbors, 
		$neighbors2, $RMYSMessages, $PDTMessages) = split($IN_SPLIT_CHAR);
		
		# print "Pushing number of neighbors UDG = $neighborsUDG to map with density $density \n";
		push(@{$mapBeaconingMessages{$density}}, $beaconingMessages);
		push(@{$mapRMYSMessages{$density}}, $RMYSMessages+$PDTMessages);
		push(@{$mapPDTMessages{$density}}, $PDTMessages);

		my $val;
		if (defined $mapNumDataSamples{$density})
		{
			$val = $mapNumDataSamples{$density} + 1;
		} else {
			$val = 1;
		}
		$mapNumDataSamples{$density} = $val;	
	}
	close (FILE);
}
# calculate and return the mean of a given array
sub calculateMean {
	my $sumedUp = 0;
	foreach my $element (@_)
	{
		$sumedUp += $element;
	}
	return $sumedUp / scalar(@_);
}
# calculate and return the variance of a given array
sub calculateVariance {
	my $mean = calculateMean(@_);
	my $sumedUp = 0;
	foreach my $element (@_)
	{
		$sumedUp += ($element - $mean) * ($element - $mean);
	}
	return $sumedUp / scalar(@_);
}
# calculate and return the upper bound of confidence interval of the three given arguments mean, standard deviation and number of elements
sub calculateUpperBound {
	my ($mean, $standardDeviation, $n) = @_;	
	return $mean + $MARGIN_ERROR * ($standardDeviation / sqrt($n));
}
# calculate and return the lower bound of confidence interval of the three given arguments mean, standard deviation and number of elements
sub calculateLowerBound {
	my ($mean, $standardDeviation, $n) = @_;	
	return $mean - $MARGIN_ERROR * ($standardDeviation / sqrt($n));
}
# printing result to file
sub printToOutputFile{
	my ($valName, $density, $mean, $lowerBound, $upperBound) = @_;
	
	unless (defined($valName) && defined($density) && defined($upperBound) && defined($lowerBound))
	{
		die "printToOutputFile: has not enough arguments!";
	}
	unless (index($valName, $OUT_SPLIT_CHAR) == -1)
	{
		die "printToOutputFile: Data separation character \'$OUT_SPLIT_CHAR\' is contained in value name \"$valName\"! Please change the name of this value.";
	}
	print OUT "$valName$OUT_SPLIT_CHAR$mean$OUT_SPLIT_CHAR$lowerBound$OUT_SPLIT_CHAR$upperBound" or die "Something went wrong during writing to file. Aborted.\n";
}
# Actual subroutine for calculation of the confidence intervals and print them to file
# @[0]: name of data for that the confidence intervals should be calculated
# @[1]: node density that is corresponded with the given values
# @[2]: array for actual data
sub estimateConfidenceIntervals {
	my ($name, $density, @values) = @_;

	my $mean = calculateMean(@values);
	my $standardDeviation = sqrt(calculateVariance(@values));

	#calculate confidence intervals
	my $lowerBound = calculateLowerBound( $mean, $standardDeviation, scalar(@values) );
	my $upperBound = calculateUpperBound( $mean, $standardDeviation, scalar(@values) );
	print "$name: Lower bound: $lowerBound \n";
	print "$name: Upper bound: $upperBound \n";
	printToOutputFile($name, $density, $mean, $lowerBound, $upperBound);
}

sub printNumDataSamples {
	
}

sub saveToDebugFile {
#open OUT in overwriting mode
open(OUT, ">", $DEBUG_OUTPATH) or die "Cannot read/write the output file $DEBUG_OUTPATH\n";
my $error_write_msg = "Something went wrong during writing to $DEBUG_OUTPATH. Aborted.\n";
#calculation of the confidence intervals of all value pairs per value type
foreach my $density ( sort {$a <=> $b} keys %mapBeaconingMessages ) #presumption: all maps have the same keys
{
	print OUT "$density$OUT_SPLIT_CHAR" or die "$error_write_msg";
	estimateConfidenceIntervals("beaconingMessages", $density, @{$mapBeaconingMessages{$density}});
	print OUT "$OUT_SPLIT_CHAR" or die "$error_write_msg";
	
	estimateConfidenceIntervals("PDTMessages", $density, @{$mapRMYSMessages{$density}});
	print OUT "$OUT_SPLIT_CHAR" or die "$error_write_msg";
	
	estimateConfidenceIntervals("RMYSMessages", $density, @{$mapPDTMessages{$density}});
		
	print OUT "\n" or die "$error_write_msg";
}
close(OUT);
print "Finished writing to Debug file \"$DEBUG_OUTPATH\" successfully!\n\n";
}

sub twoArraySum{
	my ( $aRef, $bRef ) = @_;
	my  @result = ();

	unless (scalar(@{$aRef}) == scalar(@{$bRef}))
	{
		die "twoArraySum: Number of elements in given arrays is not equal!";
	}

    my $idx = 0;
    foreach my $aItem (@{$aRef}) {
        my $bItem = $bRef->[$idx++];
        push (@result, $aItem + $bItem);
    }
    return @result;
}
#Attention: result could have less values than input arrays because if a value of the second array is zero, no result value will be calculated
sub twoArrayDiv{
	my ( $aRef, $bRef ) = @_;
	my  @result = ();

	unless (scalar(@{$aRef}) == scalar(@{$bRef}))
	{
		die "twoArraySum: Number of elements in given arrays is not equal!";
	}

    my $idx = 0;
    foreach my $aItem (@{$aRef}) {
        my $bItem = $bRef->[$idx++];
        if($bItem != 0)
        {
        	push (@result, $aItem / $bItem);
        }
         
    }
    return @result;
}

#
sub saveToGNUPlotConformFile {
#open OUT in overwriting mode
open(OUT, ">", $GNU_OUTPATH) or die "Cannot read/write the output file $GNU_OUTPATH\n";
my $error_write_msg = "Something went wrong during writing to $GNU_OUTPATH. Aborted.\n";
#calculation of the confidence intervals of all value pairs per value type

foreach my $density ( sort {$a <=> $b} keys %mapBeaconingMessages ) #presumption: all maps have the same keys
{
	
	print OUT "$density$OUT_SPLIT_CHAR" or die "$error_write_msg";
	estimateConfidenceIntervals("beaconingMessages", $density, @{$mapBeaconingMessages{$density}});
	print OUT "$OUT_SPLIT_CHAR" or die "$error_write_msg";
	
	estimateConfidenceIntervals("RMYSMessages", $density, @{$mapRMYSMessages{$density}});
	print OUT "$OUT_SPLIT_CHAR" or die "$error_write_msg";
	
	estimateConfidenceIntervals("PDTMessages", $density, @{$mapPDTMessages{$density}});
		
	print OUT "\n" or die "$error_write_msg";

}
close(OUT);
print "Finished writing to GNUPlot conform file \"$GNU_OUTPATH\" successfully!";
}