import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class GetWeatherApi {

    public static  void  main (String args[]) throws IOException, ParseException, java.text.ParseException {

        JSONObject json = new JSONObject(); //JSON object for storing all the data from API
        JSONArray timeArr = new JSONArray(); //JSON ARRAY to store timestamp and values.
        JSONArray valueArr = new JSONArray();

        String dt = "2020-12-29";  // Start date - 1, As we will add =1 day when we start with first API Call

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar c = Calendar.getInstance();

        c.setTime(sdf.parse(dt));

        //Loop to increment date with + 1 and get json for particular date from API

        for( int k = 0 ; k<12 ; k++) // 11 days till 2021-01-10
        {
            c.add(Calendar.DATE, 1);  // number of days to add

            dt = sdf.format(c.getTime());  // dt is now the new date

            String endPoint = "https://api.data.gov.sg/v1/environment/air-temperature?date=" + dt ;

            URL url = new URL(endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //GET request for requesting data
            conn.connect();

            int responescode = conn.getResponseCode();
            String inline = "";


            if (responescode != 200)
                throw new RuntimeException(("HttpResponse :" + responescode));
            else {
                Scanner sc = new Scanner(url.openStream());

                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }
                sc.close();
            }

            JSONParser parse = new JSONParser();
            JSONObject jobj = (JSONObject) parse.parse(inline);
            JSONObject obj2 = (JSONObject) jobj.get("metadata");  //Object of metadata to create array for stations and items
            JSONArray arr1 = (JSONArray) obj2.get("stations");
            JSONObject obj3 = (JSONObject) obj2.get("items");
            JSONArray arr3 = (JSONArray) jobj.get("items");




//Loop to iterate over array of stations and get ID of station name: CLEMENTI ROAD
            String id = "";

            for (int i = 0; i < arr1.size(); i++)
            {
                JSONObject jsobj = (JSONObject) arr1.get(i); //Defining object for each station
                String name = (String) jsobj.get("name");//Getting value(name-Of-Station) from key (name) in station

                if (name.equals("Clementi Road"))
                {
                    System.out.println("Stations : " + jsobj.get("name"));
                    id = (String) jsobj.get("id"); //if the name is Clementi Road then get the id from key:value - id:S50
                    System.out.println("Success : " + id);

                }
            }
 //After Getting the ID for specific location : station
            // We iterate over each timestamp and search for key equals to ID of station (Clementri Road)
            //We add each time stamp to array timestamp
            // When found, we retrieve the value and store in the JSON Array which after exiting the loop will be added to json file

            for (int i = 0; i < arr3.size(); i++) {
                JSONObject jsobj2 = (JSONObject) arr3.get(i);
                String timestamp = (String) jsobj2.get("timestamp");
                System.out.println(" TIMESTAMP : " + timestamp);

                JSONArray r1 = (JSONArray) jsobj2.get("readings");

                timeArr.add(timestamp);

                for (int j = 0; j < r1.size(); j++) {
                    JSONObject jsobj3 = (JSONObject) r1.get(j);
                    String sid = (String) jsobj3.get("station_id");

                    if (sid.equals(id)) {
                        System.out.println("Station ID IN TiMESTAMP : " + sid);

                        //Due to the reason of having double and long type value in json,
                        // the exceptions are catch and relevant casting is done

                        try {
                            double value = (Double) jsobj3.get("value");
                            System.out.println(" Value IN TIMESTAMP : " + value);
                            valueArr.add(value);
                        } catch (Exception e) {
                            e.printStackTrace();

                            long value = (Long) jsobj3.get("value");
                            System.out.println(" Value IN TIMESTAMP : " + value);
                            valueArr.add(value);
                        }

                    }

                }

            }

            json.put("timestamps", timeArr);
            json.put("values", valueArr);

        } //for loop for incrementing of dates ends

        FileWriter file = new FileWriter("weatherData.json"); // Writing the file name
        file.write(json.toJSONString()); //Writing JSON TO FILE
        file.flush();

    }
}
