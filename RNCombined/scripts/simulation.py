#!/usr/bin/env python
import shlex, subprocess, os
from multiprocessing import Pool
import sys

# Simulation parameters
PROCESSES  = 4
DENSITIES_START  = int(sys.argv[1])
DENSITIES_END    = int(sys.argv[2])
FROM_ID          = 0
TO_ID            = 3
PATH = os.path.dirname(os.path.abspath(__file__)) + '\\'

def main():
    args = []
    
    runs = TO_ID - FROM_ID
    delta = round(runs / PROCESSES)
    if(delta % 1 !=0):
        print ("delta % 1 must be 0! But it is: " + str(delta))
        quit()
    for density in range(DENSITIES_START, DENSITIES_END+1):
        for procs in range(0, PROCESSES):
            start = FROM_ID + procs * delta
            end=FROM_ID + (procs+1) * delta
            args.append([density, start, end, procs])
            # collect arguments for the processes to be started
        
    # execute the simulations via a process pool
    print (args)
    pool = Pool(PROCESSES)
    pool.map(simulate, args)
    pool.close()
    pool.join()

    #merge results!
    print ("Merging Results:")
    for density in range(DENSITIES_START, DENSITIES_END+1):
        output = open(PATH + "results\\" + str(density) + ".dat" , 'w')
        i=0
        for parts in range(0, PROCESSES):
            path=PATH + "results\\" + str(density) + "-" + str(args[i][1]) + "-" + str(args[i][2]) + "-" + str(parts) + ".dat"
            print ("merging file: \n" + path)
            part=open(path)
            output.write(part.read())
            part.close()
            os.remove(path)
            i=i+1

    #merge LOGS!
    print ("Merging Logfiles:")
    
    for parts in range(0, PROCESSES):
        output = open(PATH + "results\\log\\process-" + str(parts) + ".log", 'w')    
        for density in range(DENSITIES_START, DENSITIES_END+1):
            path=PATH + "results\\log\\dens" + str(density) + "-" + str(parts) + ".log"
            print ("merging file: \n" + path)
            part = open(path)
            output.write(part.read())
            part.close()
            os.remove(path)
        



def simulate(settings):
    density = settings[0]
    startid = settings[1]
    endid = settings[2]
    processid=settings[3]
    # simulate one algorithm for one density
    logfile=PATH+ "results\\" + str(density)+"-"+str(startid)+"-"+str(endid)+"-"+str(processid) + ".dat"
   
    for i in range(startid, endid):
        posFile=PATH + "graphs_1000\\density" + str(density) + "\\" + str(i) + ".pos"
        fp = open(posFile)
        for i, line in enumerate(fp):
            if i == 13:
                dimx=line[11:]
            elif i == 14:
                dimy=line[11:]
        fp.close()
        
        runLogFile = PATH + "results\\log\\dens" + str(density) + "-" + str(processid) + ".log"
        

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
            "exitAfterRounds=1 " + 
            "dimX=" +dimx + " " +
            "dimY=" +dimy + " " +
            "RMYS/runLogFile='" + runLogFile + "' " + 
            "outputToConsole=false " +
            "RMYS/batchmode=true " + 
            "javaCmd=java" #disable debugging!
        )

        args = shlex.split(command)
        subprocess.call(args)

if __name__ == "__main__":
    main()
