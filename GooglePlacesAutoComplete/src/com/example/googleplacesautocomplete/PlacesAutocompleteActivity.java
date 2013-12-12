package com.example.googleplacesautocomplete;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

public class PlacesAutocompleteActivity extends Activity implements OnItemClickListener {
	
	private static final String LOG_TAG = "ExampleApp";
    
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	private String LAT_LONG = "22.5667,88.3667";
	private double miles_to_meter = 1609.34;
	
	//https://maps.googleapis.com/maps/api/place/autocomplete/json

	private static final String API_KEY = "AIzaSyDhWkGyusUyW4v-aqsgTrqUFqEDRQL_j28";
	ArrayList<String> referencePlaces ;

	String placesDetailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
		autoCompView.setOnItemClickListener(this);
	    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        /*String str = (String) adapterView.getItemAtPosition(position);
        String str1 = (String) referencePlaces.get(position);
        Toast.makeText(this, str1, Toast.LENGTH_SHORT).show();
        Log.e("test","reference url is "+str1);*/
    }
	
	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	    private ArrayList<String> resultList;
	    
	    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
	        super(context, textViewResourceId);
	    }
	    
	    @Override
	    public int getCount() {
	        return resultList.size();
	    }

	    @Override
	    public String getItem(int index) {
	        return resultList.get(index);
	    }

	    @Override
	    public Filter getFilter() {
	        Filter filter = new Filter() {
	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {
	                FilterResults filterResults = new FilterResults();
	                if (constraint != null) {
	                    // Retrieve the autocomplete results.
	                    resultList = autocomplete(constraint.toString());
	                    
	                    // Assign the data to the FilterResults
	                    filterResults.values = resultList;
	                    filterResults.count = resultList.size();
	                }
	                return filterResults;
	            }

	            @Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                if (results != null && results.count > 0) {
	                    notifyDataSetChanged();
	                }
	                else {
	                    notifyDataSetInvalidated();
	                }
	            }};
	        return filter;
	    }
	    
	    private ArrayList<String> autocomplete(String input) {
	        ArrayList<String> resultList = null;
	        referencePlaces = null;
	        HttpURLConnection conn = null;
	        StringBuilder jsonResults = new StringBuilder();
	        try {
	            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
	            sb.append("?sensor=false&key=" + API_KEY);
	            sb.append("&components=country:us");
	            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
	            sb.append("&location=" + LAT_LONG);
	            sb.append("&radius=" + miles_to_meter * 10);
	            
	            URL url = new URL(sb.toString());
	            Log.e("test","url is "+url);
	            conn = (HttpURLConnection) url.openConnection();
	            InputStreamReader in = new InputStreamReader(conn.getInputStream());
	            
	            // Load the results into a StringBuilder
	            int read;
	            char[] buff = new char[1024];
	            while ((read = in.read(buff)) != -1) {
	                jsonResults.append(buff, 0, read);
	            }
	        } catch (MalformedURLException e) {
	            Log.e(LOG_TAG, "Error processing Places API URL", e);
	            return resultList;
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "Error connecting to Places API", e);
	            return resultList;
	        } finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }

	        try {
	            // Create a JSON object hierarchy from the results
	            JSONObject jsonObj = new JSONObject(jsonResults.toString());
	            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
	            
	            // Extract the Place descriptions from the results
	            resultList = new ArrayList<String>(predsJsonArray.length());
	            referencePlaces = new ArrayList<String>(predsJsonArray.length());
	            for (int i = 0; i < predsJsonArray.length(); i++) {
	                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
	                referencePlaces.add(predsJsonArray.getJSONObject(i).getString("reference"));
	            }
	        } catch (JSONException e) {
	            Log.e(LOG_TAG, "Cannot process JSON results", e);
	        }
	        
	        return resultList;
	    }
	}

	

}
