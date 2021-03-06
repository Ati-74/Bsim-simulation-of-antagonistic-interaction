package BSimAtiDiffusibleToxin;
import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Vector3d;

import bsim.BSim;
import bsim.BSimTicker;
import bsim.capsule.BSimCapsuleBacterium;
import bsim.capsule.Mover;
import bsim.capsule.RelaxationMoverGrid;

public class AtiDiffusibleToxinTicker extends BSimTicker {

    BSim sim;
    // logs data about time taken by ticker every LOG_INTERVAL timesteps
    final int LOG_INTERVAL;
    /** Whether to enable growth in the ticker etc. or not... */
    private static boolean WITH_GROWTH = true;

    static Random bacRng;
    //growth rate standard deviation
    public final double growth_stdv;
    //growth rate mean
    public final double growth_mean;
    //elongation threshold standard deviation
    public final double length_stdv;
    //elongation threshold mean
    public final double length_mean;

    final ArrayList<AtiDiffusibleToxinBacterium> attacker_bac;
    final ArrayList<AtiDiffusibleToxinBacterium> susp_bac;
    final ArrayList<BSimCapsuleBacterium> bacteriaAll;
    final ArrayList<AtiDiffusibleToxinBacterium> bac_bornAttacker;
    final ArrayList<AtiDiffusibleToxinBacterium> bac_bornSusp;
    final ArrayList<AtiDiffusibleToxinBacterium> bac_deadAttacker;
    final ArrayList<AtiDiffusibleToxinBacterium> bac_deadSusp;

    public static ChemicalField toxin;

	// Initial Conditions
	/** Used to determine the toxin distribution for the simulation. */
	private int toxin_condition;

	/** Toxins flow in from the left boundary. */
	private static final int FLOW_IN = 1;

    // For a single screen
    final int MIXED_CONC = 1;
    final int CHECKER_BOARD = 2;
    int SINGLE_SCREEN = 2;

    /** Defines the progress of the chemical field flowing through the boundary on the x&y-axis. */
    int endpoint = 0;
    int field_box_num = 50;

    // internal machinery - don't worry about this
    final Mover mover;

    public AtiDiffusibleToxinTicker(BSim sim, ArrayList<AtiDiffusibleToxinBacterium> attacker_bac, ArrayList<AtiDiffusibleToxinBacterium> susp_bac,
    		ArrayList<BSimCapsuleBacterium> bacteriaAll, int LOG_INTERVAL, Random bacRng,
    		double growth_stdv, double growth_mean, double length_stdv, double length_mean,
    		ChemicalField toxin, int toxin_condition) {
        this.sim = sim;
        this.LOG_INTERVAL = LOG_INTERVAL;
        AtiDiffusibleToxinTicker.bacRng = bacRng; //random number generator
        this.growth_stdv = growth_stdv;
        this.growth_mean = growth_mean;
        this.length_stdv = length_stdv;
        this.length_mean = length_mean;
        this.attacker_bac = attacker_bac;
        this.susp_bac = susp_bac;
        this.bacteriaAll = bacteriaAll;
        bac_bornAttacker = new ArrayList();
        bac_bornSusp = new ArrayList();
        bac_deadAttacker = new ArrayList();
        bac_deadSusp = new ArrayList();
        mover = new RelaxationMoverGrid(bacteriaAll, sim);

        AtiDiffusibleToxinTicker.toxin = toxin;
        this.toxin_condition = toxin_condition;
    }

    /** Sets the flag for growth. **/
    public void setGrowth(boolean b) { WITH_GROWTH = b; }

    /** Function for bacteria growth activities. */
    public void growAttacker( ArrayList<AtiDiffusibleToxinBacterium> bac, ArrayList<AtiDiffusibleToxinBacterium> bacBorn ) {
    	Random bacRng = new Random(); 			// Random number generator
    	bacRng.setSeed(50); 					// Initializes random number generator

        for (AtiDiffusibleToxinBacterium b : bac) { 				// Loop over bac array
            b.grow();

            // Divide if grown past threshold
            if (b.L >= b.L_th) {
            	AtiDiffusibleToxinBacterium daughter = b.divide();

            	bacBorn.add(daughter);  		// Add daughter to newborn class, 'mother' keeps her status
            }
        }

        bac.addAll(bacBorn); 					// Adds all the newborn daughters to sub-population
        bacteriaAll.addAll(bacBorn); 			// Adds all the newborn daughters to total population

        // Allow daughter cells to grow
        for ( AtiDiffusibleToxinBacterium b : bacBorn ) {

            // Assigns a division length to each bacterium according to a normal distribution
            double lengthThreshold = length_stdv*bacRng.nextGaussian()+length_mean;
            b.setElongationThreshold(lengthThreshold);
        }

        bacBorn.clear(); 						// Cleared for next time-step


        /** Allow the flow of toxins through a boundary. */
        /**toxin flows is depends on attacker growth. */
        // State enabled where toxin flows in through a boundary
        		final double conc = 50;
            	if ( endpoint < field_box_num ) {
	                for (AtiDiffusibleToxinBacterium b : attacker_bac) {
	                	Random rnd = new Random(); 		// Random number generator
	                    rnd.setSeed(50); 				// Initializes random number generator
	    	                	for ( int x = (int)Math.min(b.x1.x,b.x2.x); x < endpoint*Math.random()+(int)Math.max(b.x1.x,b.x2.x); x ++ ) {
	    	                		for ( int y = (int)Math.min(b.x1.y,b.x2.y); y < endpoint*Math.random()+(int)Math.max(b.x1.y,b.x2.y); y ++ ) {
	    	                			Vector3d coordinate=new Vector3d(x,y,0);
	    	                			toxin.addQuantity(coordinate, conc );
	    	                		}
	    	                	}
	                    }
            		endpoint ++;
            	}
            	else {
            		endpoint = 0;
            	}
        // Update the toxin field
        toxin.update();
    }

    public void growSucp( ArrayList<AtiDiffusibleToxinBacterium> bac, ArrayList<AtiDiffusibleToxinBacterium> bacBorn ) {
    	Random bacRng = new Random(); 			// Random number generator
    	bacRng.setSeed(50); 					// Initializes random number generator

        for (AtiDiffusibleToxinBacterium b : bac) { 				// Loop over bac array
            b.grow();

            // Divide if grown past threshold
            if (b.L >= b.L_th) {
            	AtiDiffusibleToxinBacterium daughter = b.divide();

            	bacBorn.add(daughter);  		// Add daughter to newborn class, 'mother' keeps her status
            }
        }

        bac.addAll(bacBorn); 					// Adds all the newborn daughters to sub-population
        bacteriaAll.addAll(bacBorn); 			// Adds all the newborn daughters to total population

        // Allow daughter cells to grow
        for ( AtiDiffusibleToxinBacterium b : bacBorn ) {

            // Assigns a division length to each bacterium according to a normal distribution
            double lengthThreshold = length_stdv*bacRng.nextGaussian()+length_mean;
            b.setElongationThreshold(lengthThreshold);
        }

        bacBorn.clear(); 						// Cleared for next time-step
    }

    /** Function to remove bacteria due to cell death or by boundary. */
    public void removeBacteria( BSim sim, ArrayList<AtiDiffusibleToxinBacterium> bac, ArrayList<AtiDiffusibleToxinBacterium> bac_dead ) {

        for (AtiDiffusibleToxinBacterium b : bac) {

            // Kick out if past any boundary
        	// Bacteria out of bounds = dead
            if(b.position.x < 0 || b.position.x > sim.getBound().x || b.position.y < 0 || b.position.y > sim.getBound().y || b.position.z < 0 || b.position.z > sim.getBound().z){
            	bac_dead.add(b);
            }
            // Remove cell after it shrinks and becomes too small
            if ( b.L <= 1 ) {
            	bac_dead.add(b);
            }

        }

        bac.removeAll(bac_dead);

        // Remove from total population
        bacteriaAll.removeAll(bac_dead);
        bac_dead.clear();
    }

    // This one is a bit long too. Let's break it up
    // 1. Begins an "action" -> this represents one timestep
    // 2. Tells each bacterium to perform their action() function
    // 3. Updates each chemical field in the simulation
    // 4. Bacteria are then told to grow()
    // 5. bacteria which are longer than their threshold are told to divide()
    // 6. forces are applied and bacteria move around
    // 7. bacteria which are out of bounds are removed from the simulation
    @Override
    public void tick() {
        // increase lifetimes of cells
        for (AtiDiffusibleToxinBacterium b : attacker_bac) {
            b.lifetime++;
        }
        for (AtiDiffusibleToxinBacterium b : susp_bac) {
            b.lifetime++;

        }



        /********************************************** Action */

        long startTimeAction = System.nanoTime(); 	// Wall-clock time, for diagnosing timing

        // Calculates and stores the midpoint of the cell.
        // Bacteria does action at each time step
        for(BSimCapsuleBacterium b : bacteriaAll) {
            b.action();
        }

        // Outputs how long each step took, once every log interval.
        long endTimeAction = System.nanoTime();
        if((sim.getTimestep() % LOG_INTERVAL) == 0) {
            System.out.println("Action update for " + bacteriaAll.size() + " bacteria took " + (endTimeAction - startTimeAction)/1e6 + " ms.");
        }

        /********************************************** Chemical fields */

        startTimeAction = System.nanoTime();



        endTimeAction = System.nanoTime();
        if((sim.getTimestep() % LOG_INTERVAL) == 0) {
            System.out.println("Chemical field update took " + (endTimeAction - startTimeAction)/1e6 + " ms.");
        }

        /********************************************** Growth related activities if enabled. */
        if (WITH_GROWTH) {
            // ********************************************** Growth and division
            startTimeAction = System.nanoTime(); 	// Start action timer

            growAttacker( attacker_bac, bac_bornAttacker );		// For sub-population A
            growSucp( susp_bac, bac_bornSusp );		// For sub-population B

            // Prints out information about bacteria when u want it to
            endTimeAction = System.nanoTime();
            if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                System.out.println("Growth and division took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
            }

            /********************************************** Neighbor interactions */

            startTimeAction = System.nanoTime();

            mover.move();

            endTimeAction = System.nanoTime();
            if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                System.out.println("Wall and neighbour interactions took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
            }

            /********************************************** Boundaries/removal */
            startTimeAction = System.nanoTime();

            removeBacteria( sim, attacker_bac, bac_deadAttacker );		// For sub-population A
            removeBacteria( sim, susp_bac, bac_deadSusp );		// For sub-population B


            endTimeAction = System.nanoTime();
            if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
                System.out.println("Death and removal took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
            }
        }

        startTimeAction = System.nanoTime();

        endTimeAction = System.nanoTime();
        if ((sim.getTimestep() % LOG_INTERVAL) == 0) {
            System.out.println("Switch took " + (endTimeAction - startTimeAction) / 1e6 + " ms.");
        }

    }
}
