package rgpiograph;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import static org.rrd4j.ConsolFun.AVERAGE;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.rrd4j.graph.TimeLabelFormat;

public class RGPIOGraph {

    static ArrayList<String> SENSORS = new ArrayList<>();
    static HashMap<String, String> COLORNAMES = new HashMap<>();

    static void println(String msg) {
        System.out.println(msg);
    }

    static void print(String msg) {
        System.out.print(msg);
    }

    static String createGraph(String rrdPath, String imgPath, long seconds, String ylabel) {


        /*      example code how to fetch data from the RRD
        
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
            gDef.setStartTime(lastUpdateTime - seconds);
            gDef.setEndTime(lastUpdateTime);
            gDef.setTitle("server room environmentals");
            gDef.setVerticalLabel(ylabel);
            gDef.setUnitsExponent(0);

            // add the data sources to the graph, each with their color
//            for (String sensor : rrdDb.getDsNames()) {
            for (String sensor : SENSORS) {
                System.out.println("adding " + sensor + " to graph (" + COLORNAMES.get(sensor) + ")");
                gDef.datasource(sensor, rrdPath, sensor, AVERAGE);
                Color color = colorOf(COLORNAMES.get(sensor));
                gDef.line(sensor, color, sensor);
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

    public static Color colorOf(String color) {
        try {
            return (Color) Color.class.getDeclaredField(color).get(null);
        } catch (Exception notAvailable) {
            return null; // ??
        }
    }

    static int colorIndex = 0;

    public static String colorNameGenerator() {
        // next color is taken round-robin from the list 'allColors)
        String nextColor;
        ArrayList<String> allColors = new ArrayList<>();
        allColors.add("blue");
        allColors.add("black");
        allColors.add("yellow");
        allColors.add("red");
        allColors.add("pink");
        allColors.add("orange");
        allColors.add("magenta");
        allColors.add("green");
        allColors.add("gray");
        allColors.add("cyan");

        nextColor = allColors.get(colorIndex);
        colorIndex = (colorIndex + 1) % allColors.size();
        return nextColor;
    }

    public static void main(String[] args) throws IOException {
        
     TimeStamp now = new TimeStamp();
        
        String rrdPath = "";
        String imgPath = "";
        String imgFileName=now.asDashSeparatedString()+".png";
        
        // the image file name is expected as the first line of output 
        // RGPIOGraph will be called by the client handler of RGPIO on request of the web client
        // The image file name is read by RGPIO from the process output and passed on to the web client
       
        System.out.println(imgFileName);
        
        if (System.getProperty("file.separator").equals("/")) {
            rrdPath = "/home/pi/RGPIO/dataStore/datastore.rrd";
            imgPath = "/home/pi/html/graphs/"+imgFileName;
        } else {
            rrdPath = "C:\\Users\\erikv\\Documents\\RRD\\datastore.rrd";
            imgPath = "C:\\Users\\erikv\\Documents\\RRD\\"+imgFileName;
        }

        String arg_range = "1d";
        String arg_ylabel = "";

        for (int arg = 0; arg <= args.length - 1; arg++) {
            String[] s = args[arg].split("=");
            if (s[0].equals("range")) {
                arg_range = s[1];
            } else if (s[0].equals("ylabel")) {
                arg_ylabel = s[1];
            } else {
                //               System.out.println("size="+s.length);
                String dataSource = s[0];
                Color color = null;
                String colorName = "none";
                if (s.length == 2) {
                    colorName = s[1];
                    color = colorOf(colorName);
                    if (color == null) {
                        System.out.println("unknown color " + colorName);
                    }
                }
                System.out.println("data source=" + dataSource + " color=" + colorName);

                SENSORS.add(dataSource);
                COLORNAMES.put(dataSource, colorName); // null if not specified on the command line
            };
        }

        System.out.println("range: " + arg_range);

        Integer multiplier = null;

        if (arg_range.matches("[0123456789]+[dD]")) {
            multiplier = 24 * 60 * 60;
        } else if (arg_range.matches("[0123456789]+[hH]")) {
            multiplier = 60 * 60;
        } else if (arg_range.matches("[0123456789]+[mM]")) {
            multiplier = 60;
        } else if (arg_range.matches("[0123456789]+[sS]")) {
            multiplier = 1;
        } else {
            System.out.println("RGPIOGraph range=[0123456789]+[dDhHmMsS] ");
            System.out.println(" examples: ");
            System.out.println("    RGPIOGraph range=2d   (2 days) ");
            System.out.println("    RGPIOGraph range=30M  (30 minutes) ");
            System.out.println("    RGPIOGraph range=12H  (12 hours) ");
            System.exit(0);
        }

//        System.out.println("range:ok ");
        String rangeNumber = arg_range.replaceFirst("[dDhHmMsS]", "");

//        System.out.println(" string <" + rangeNumber + ">");
        Long range = Long.parseLong(rangeNumber) * multiplier;

        System.out.println("range = " + range + " seconds");

        for (String sensor : SENSORS) {
            String colorName = COLORNAMES.get(sensor);
            if (colorName.equals("none")) {
                COLORNAMES.put(sensor, colorNameGenerator());
            }
            //           System.out.println("adding " + sensor + " to graph (" + colorName + ")");
        }

        createGraph(rrdPath, imgPath, range, arg_ylabel);

    }
}
