
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class PopulationQuery {
	// next four constants are relevant to parsing
	public static final int TOKENS_PER_LINE  = 7;
	public static final int POPULATION_INDEX = 4; // zero-based indices
	public static final int LATITUDE_INDEX   = 5;
	public static final int LONGITUDE_INDEX  = 6;
	
	// TODO: get rid of later and rearrange code
	public static float minLat;
	public static float maxLat;
	public static float minLong;
	public static float maxLong;
	
	public static int totalPop = 0;
	public static int queryPop = 0;
	
	// parse the input file into a large array held in a CensusData object
	public static CensusData parse(String filename) {
		CensusData result = new CensusData();
		
        try {
            BufferedReader fileIn = new BufferedReader(new FileReader(filename));
            
            // Skip the first line of the file
            // After that each line has 7 comma-separated numbers (see constants above)
            // We want to skip the first 4, the 5th is the population (an int)
            // and the 6th and 7th are latitude and longitude (floats)
            // If the population is 0, then the line has latitude and longitude of +.,-.
            // which cannot be parsed as floats, so that's a special case
            //   (we could fix this, but noisy data is a fact of life, more fun
            //    to process the real data as provided by the government)
            
            String oneLine = fileIn.readLine(); // skip the first line

            // read each subsequent line and add relevant data to a big array
            while ((oneLine = fileIn.readLine()) != null) {
                String[] tokens = oneLine.split(",");
                if(tokens.length != TOKENS_PER_LINE)
                	throw new NumberFormatException();
                int population = Integer.parseInt(tokens[POPULATION_INDEX]);
                if(population != 0)
                	result.add(population,
                			   Float.parseFloat(tokens[LATITUDE_INDEX]),
                		       Float.parseFloat(tokens[LONGITUDE_INDEX]));
            }

            fileIn.close();
        } catch(IOException ioe) {
            System.err.println("Error opening/reading/writing input or output file.");
            System.exit(1);
        } catch(NumberFormatException nfe) {
            System.err.println(nfe.toString());
            System.err.println("Error in file format");
            System.exit(1);
        }
        return result;
	}


	// argument 1: file name for input data: pass this to parse
	// argument 2: number of x-dimension buckets
	// argument 3: number of y-dimension buckets
	// argument 4: -v1, -v2, -v3, -v4, or -v5
	public static void main(String[] args) throws IllegalArgumentException {

        if (args.length < 4) {
            throw new IllegalArgumentException();
        }

        String filename = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        String version = args[3];

        CensusData censusData = parse(filename);

        Scanner console = new Scanner(System.in);
        boolean cont = true;
        while (cont) {
            System.out.println("Please give west, south, east, north coordinates of your query");
            System.out.println("  rectangle:");
            String rect = console.next();

            String[] borders = rect.trim().split("-"); //todo: change

            System.out.println("hey borders has a length of " + borders.length); //todo: remove

            if (borders.length == 4) {
                int west = Integer.parseInt(borders[0]);
                int south = Integer.parseInt(borders[1]);
                int east = Integer.parseInt(borders[2]);
                int north = Integer.parseInt(borders[3]);

                if ((west < 1) || (west > x) || (south < 1) || (south > y)
                        || (east < west) || (east > x) || (north < south) || (north > y)) {
                    throw new IllegalArgumentException();
                }

                if (version.equals("-v1")) {
                    System.out.println("Test starting..."); //TODO: remove

                    versionOne(censusData, x, y, west, south, east, north);

                    double percent = (double) queryPop / totalPop;
                    percent = Math.round(percent * 100 * 100);
                    percent = percent / 100;

                    System.out.println("population of rectangle: " + queryPop);
                    System.out.println("total pop: " + totalPop); //TODO: remove
                    System.out.println("percent of total population: " + percent);
                }
            } else {
                cont = false;
            }
        }
	}
	
	public static void versionOne(CensusData censusData, int x, int y,
                                  int west, int south, int east, int north) {
		minLat = censusData.data[0].latitude;
		maxLat = censusData.data[0].latitude;
		minLong = censusData.data[0].longitude;
		maxLong = censusData.data[0].longitude;
		
		// compute US rectangle size
		for (int i = 1; i < censusData.data_size; i++) {
			float lat = censusData.data[i].latitude;
			float lon = censusData.data[i].longitude;
			minLat = Math.min(minLat, lat);
			maxLat = Math.max(maxLat, lat);
			minLong = Math.min(minLong, lon);
			maxLong = Math.max(maxLong, lon);
		}

        float colSize = (maxLong - minLong) / x;
        float rowSize = (maxLat - minLat) / y;
		
		// compute population of small rectangle: queryPop
		// compute population of entire US: totalPop
		for (int i = 0; i < censusData.data_size; i++) {
			int pop = censusData.data[i].population;

            float curX = (censusData.data[i].longitude - minLong) / colSize;
            float curY = (censusData.data[i].latitude - minLat) / rowSize;

            if (curX >= (float)west && curX < (float)east
                    && curY >= (float)south && curY < (float)north) {
                queryPop += pop;
            }

            totalPop += pop;
		}
				
	}
	
}
