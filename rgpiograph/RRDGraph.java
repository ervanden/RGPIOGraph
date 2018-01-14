package rgpiograph;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import static org.rrd4j.ConsolFun.AVERAGE;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdSafeFileBackend;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.rrd4j.graph.TimeLabelFormat;

public class RRDGraph {

    static ArrayList<String> SENSORS = new ArrayList<>();
    static HashMap<String, Color> COLORS = new HashMap<>();

    static void println(String msg) {
        System.out.println(msg);
    }

    static void print(String msg) {
        System.out.print(msg);
    }

    static String createGraph(String rrdPath, long start, long end) {

        // test read-only access!
 /*           
         try{
         RrdDb rrdDb = new RrdDb(rrdPath, true);
         println("File reopen in read-only mode");
         println("== Last update time was: " + rrdDb.getLastUpdateTime());
         println("== Last info was: " + rrdDb.getInfo());

         // fetch data
         println("== Fetching data for the whole month");
         FetchRequest request = rrdDb.createFetchRequest(AVERAGE,
         rrdDb.getLastUpdateTime()-100 , 
         rrdDb.getLastUpdateTime());
         println(request.dump());

         FetchData fetchData = request.fetchData();
         println("== Data fetched. " + fetchData.getRowCount() + " points obtained");
         println(fetchData.toString());
         } catch (IOException ioe){};
         */
        String imgPath = rrdPath.replaceAll(".rrd", ".png");

        try {

            RrdDb rrdDb = new RrdDb(rrdPath, true);
            Long lastUpdateTime = rrdDb.getLastUpdateTime();
            for (String dsName : rrdDb.getDsNames()) {
                System.out.println("RRD contains data source : " + dsName);
            }

            int IMG_WIDTH = 900;
            int IMG_HEIGHT = 500;

            RrdGraphDef gDef = new RrdGraphDef();
            gDef.setTimeLabelFormat(new CustomTimeLabelFormat());
            gDef.setLocale(Locale.US);
            gDef.setWidth(IMG_WIDTH);
            gDef.setHeight(IMG_HEIGHT);

            gDef.setFilename(imgPath);
            gDef.setStartTime(lastUpdateTime - 3600*2);
            gDef.setEndTime(lastUpdateTime);
            gDef.setTitle("server room environmentals");
            gDef.setVerticalLabel("temp (centigrade), humidity (%)");

            // create a map 'colors' with a color for each data source
            // the colors are taken round-robin from the list 'allColors)
            ArrayList<Color> allColors = new ArrayList<>();
            HashMap<String, Color> colors = new HashMap<>();

            allColors.add(Color.blue);
            allColors.add(Color.black);
            allColors.add(Color.cyan);
            allColors.add(Color.red);

            int colorIndex = 0;
            for (String sensor : rrdDb.getDsNames()) {
                colors.put(sensor, allColors.get(colorIndex));
                colorIndex = (colorIndex + 1) % allColors.size();
            }

            // now define the data sources for the graph, each with their color
            
            for (String sensor : rrdDb.getDsNames()) {
                System.out.println("addding " + sensor + " to graph");
                gDef.datasource(sensor, rrdPath, sensor, AVERAGE);
                gDef.line(sensor, colors.get(sensor), sensor);
            }

            gDef.comment("\\r");

            gDef.setImageInfo("<img src='%s' width='%d' height = '%d'>");
            gDef.setPoolUsed(false);
            gDef.setImageFormat("png");
            gDef.setDownsampler(new eu.bengreen.data.utility.LargestTriangleThreeBuckets((int) (IMG_WIDTH * 1)));
            println("Rendering graph " + Util.getLapTime());
            // create graph finally
            try {
                RrdGraph graph = new RrdGraph(gDef);
                println(graph.getRrdGraphInfo().dump());
            } catch (IOException ioe) {
                imgPath = null;
            };
        } catch (IOException ioe) {
        };

        return imgPath;
    }

    static class CustomTimeLabelFormat implements TimeLabelFormat {

        public String format(Calendar c, Locale locale) {
            if (c.get(Calendar.MILLISECOND) != 0) {
                return String.format(locale, "%1$tH:%1$tM:%1$tS.%1$tL", c);
            } else if (c.get(Calendar.SECOND) != 0) {
                return String.format(locale, "%1$tH:%1$tM:%1$tS", c);
            } else if (c.get(Calendar.MINUTE) != 0) {
                return String.format(locale, "%1$tH:%1$tM", c);
            } else if (c.get(Calendar.HOUR_OF_DAY) != 0) {
                return String.format(locale, "%1$tH:%1$tM", c);
            } else if (c.get(Calendar.DAY_OF_MONTH) != 1) {
                return String.format(locale, "%1$td %1$tb", c);
            } else if (c.get(Calendar.DAY_OF_YEAR) != 1) {
                return String.format(locale, "%1$td %1$tb", c);
            } else {
                return String.format(locale, "%1$tY", c);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        String RRDDIRECTORY = "C:\\Users\\erikv\\Documents\\RRD\\";
        String RRDNAME = "datastore";
        String rrdPath = RRDDIRECTORY + RRDNAME + ".rrd";

        // create graph
        String imgPath = createGraph(rrdPath,
                Util.getTimestamp(2018, 1, 13, 16, 0),
                Util.getTimestamp(2018, 1, 13, 24, 0)
        );

        // locks info
        println("== Locks info ==");
        println(RrdSafeFileBackend.getLockInfo());

    }

    static class GaugeSource {

        double value;
        double slope = 0;
        int countdown = 0;
        Random RANDOM;

        GaugeSource(long seed, double value) {
            RANDOM = new Random(seed);
            this.value = value;
        }

        long getValue() {
            if (countdown == 0) {
                // new slope and countdown     
                slope = (RANDOM.nextDouble() - 0.5);
                countdown = RANDOM.nextInt(5) + 1;
            }
            value = value + slope * 0.01;
//           System.out.println(slope + "\t" + value);
            countdown--;
            return Math.round(value * 10);
        }

    }

}
