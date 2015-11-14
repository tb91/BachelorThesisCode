#!/usr/bin/env python
import shlex, subprocess, os
from multiprocessing import Pool
import sys
import os, os.path
import math
import time


# Simulation parameters
PROCESSES  = 1
DENSITIES_START  = int(sys.argv[1])
DENSITIES_END    = int(sys.argv[2])
PASSES           = int(sys.argv[3])
DIMX             = 300
DIMY             = 300
rUDG             = 30
PATH = os.path.dirname(os.path.abspath(__file__)) + '\\graphs'


def main():
    if(PASSES % PROCESSES !=0):
        quit("Error: Passes should be dividable by PROCESSES. But PASSES \% PROCESSES is: " + str(PASSES % PROCESSES))

    args = []
    for i in range(DENSITIES_START, DENSITIES_END+1):
        fullpath=PATH + '\\Dens' + str(i)
        for procs in range(0, PROCESSES):
            end = round((procs+1)*PASSES/PROCESSES)-1
            if procs==PROCESSES-1:
                end+=1
            args.append([i, round(procs*PASSES/PROCESSES), end])
        if os.path.isdir(fullpath) == False:
            os.makedirs(fullpath)

    print (args)
   
    #execute the simulations via a process pool
    print("Starting Simulation..")
    starttime=time.time()
    pool = Pool(PROCESSES)
    pool.map(simulate, args)
    pool.close()
    pool.join()
    print("Finished Simulation in " + str(time.time()-starttime) + " seconds")


def simulate(settings):
    density=settings[0]
    runsfrom=settings[1]
    runsto=settings[2]

    numNodes = int((density * DIMY * DIMX) / (math.pi * rUDG * rUDG))
    fullpath=PATH + '\\Dens' + str(density)
    # simulate one algorithm for one density
    rerun=False
    for i in range(runsfrom,runsto):
        print("Generating " + str(numNodes) + " Nodes (Density: " + str(density) + ")")
        simulate_one(settings, i)
        
        

def simulate_one(settings, i):
    density=settings[0]
    numNodes = int((density * DIMY * DIMX) / (math.pi * rUDG * rUDG))
    fullpath=PATH + '\\Dens' + str(density)
    command = (
            "java -cp 'binaries/bin;binaries/jdom.jar ' sinalgo.Run " +
            "-batch " +
            "-project rmys " +
            "-gen " + str(numNodes) + " rmys:NewPhysicalGraphNode Random UDG " +
            "-overwrite " +
            "outputToConsole=true " +            # no logging output in the terminal
            "logConfiguration=false " +          # no logging output to file
            "dimX=" + str(DIMX) +                        # set the dimension
            " dimY=" + str(DIMY) +
            #"-rounds $numRounds " +             # number of rounds
            #"AutoStart=true " +                 # Automatically start communication protocol
            " UDG/rMax=" + str(rUDG)                    # radius of the unit disk; should not be larger than GeometricNodeCollection/rMax
        )
    #if logfile is None:
    #    print("hallo")
    #else:
    #   command += "resultsLog=" + logfile
    args = shlex.split(command)
    subprocess.call(args)
    if not os.path.isfile(fullpath + "\\" + str(i)+ ".graph"):
          simulate_one(settings,i)

if __name__ == "__main__":
    main()