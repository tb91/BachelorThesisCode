#!/usr/bin/env python
import shlex, subprocess, os
from multiprocessing import Pool
import sys

# Simulation parameters
PROCESSES  = 1
DENSITIES_START  = int(sys.argv[1])
DENSITIES_END    = int(sys.argv[2])
FROM_ID          = 1
TO_ID            = 1
PATH = os.path.dirname(os.path.abspath(__file__)) + '\\'


def main():
    args = []
    for density in range(DENSITIES_START, DENSITIES_END+1):
        for procs in range(0, PROCESSES):
            passes=TO_ID - FROM_ID
            end = round((procs+1)*(passes)/PROCESSES)-1
            if procs==PROCESSES-1:
                end+=1
            start = round(procs*passes/PROCESSES) + FROM_ID
            args.append([density, start, end + start, procs])
            # collect arguments for the processes to be started
        
    print (args)
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
    logfile=PATH+ "results\\" + str(density)+"-"+str(startid)+"-"+str(endid)+"-"+str(processid) + ".log"
    
    for i in range(startid, endid+1):
        posFile=PATH + "graphs_1000\\density" + str(density) + "\\" + str(i) + ".pos"
        command = (
            "java -cp 'binaries/bin;binaries/jdom.jar ' sinalgo.Run "
            "-project rmys " +
            "-batch " +
            "-overwrite " +
            "AutoStart=true " +
            "algorithm/name=EXPERIMENT1 " +
            "positionFile/src='" + posFile + "' " + 
            "resultsLog='" + logfile + 
            "' useFixedSeed=false " +
            "exitAfterRounds=1"
        )
        print(command)
    args = shlex.split(command)
    subprocess.call(args)

if __name__ == "__main__":
    main()
