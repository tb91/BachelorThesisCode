#!/usr/bin/env python
import shlex, subprocess, os
from multiprocessing import Pool
import sys

# Simulation parameters
PROCESSES  = 8
DENSITIES_START  = int(sys.argv[1])
DENSITIES_END    = int(sys.argv[2])
FROM_ID          = 0
TO_ID            = 20
PATH = os.path.dirname(os.path.abspath(__file__)) + '\\'


def main():
    args = []
    for density in range(DENSITIES_START, DENSITIES_END+1):
        for procs in range(0, PROCESSES):
            passes=TO_ID - FROM_ID
            end = round((procs+1)*(passes)/PROCESSES)-1
            if procs==PROCESSES-1:
                end+=1
            args.append([density, round(procs*passes/PROCESSES), end, procs])
            # collect arguments for the processes to be started
        
    
    # execute the simulations via a process pool
    pool = Pool(PROCESSES)
    pool.map(simulate, args)
    pool.close()
    pool.join()

def simulate(settings):
    density = settings[0]
    startid = settings[1]
    endid = settings[2]
    processid=settings[3]
    # simulate one algorithm for one density
    logfile=PATH+ "results\\" + str(density)+"-"+str(startid)+"-"+str(endid)+"-"+str(settings)
    
    for i in range(startid, endid):
        posFile=PATH + "graphs\\Dens" + str(density) + "\\" + str(i) + ".graph"
        command = (
            "java -cp 'binaries/bin;binaries/jdom.jar ' sinalgo.Run "
            "-project rmys " +
            "-batch " +
            "-overwrite " +
            "AutoStart=true " +
            "exitOnTerminationInGUI=true " +
            "algorithm/name=MEASUREMENT " +
            "positionFile/src='" + posFile + "' " + 
            "resultsLog=" + logfile + 
            " useFixedSeed=false " +
            "exitAfterRounds=2006"
        )
        print(posFile)
    args = shlex.split(command)
    subprocess.call(args)

if __name__ == "__main__":
    main()
