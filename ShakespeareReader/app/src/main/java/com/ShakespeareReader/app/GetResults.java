package com.ShakespeareReader.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

public class GetResults extends AsyncTask<String, Void, ArrayList> {
	private static final String TAG = "GetResults";
    public String field = "";
    public String count = "";
    public String search_term = "";
    public String text = "";
    public String freq_citation = "";
    public String freq_url = "";
    public String freq_results = "";
    String[] metadata_values = {};
    public String citation = "";
    public String list_shrtcit = "";
    public String full_shrtcit = "";
    public String offsets = "";
    public String philoid = "";
    public String full_philoid = "";
    public String title = "";
    public String prev = "";
    public String next = "";
    public int total_hits = 0;
    public int start_hit = 0;
    int hit_number = 0;
    public Boolean report_search;
    public Boolean toc_search;
    public Boolean conc_report;
    public Boolean freq_report;
    public Boolean bibliography_report;

	public Activity a;
	public Context context;

    Locale locale = new Locale("fr");
    Configuration config = new Configuration();
    
	public Exception exception;
    ProgressDialog dialog;

    public ListResultFragment.SearchAsyncResponse delegate = null;
    public FullResultFragment.FullAsyncResponse delegate2 = null;
    public TOCResultFragment.TOCAsyncResponse delegate3 = null;
    public FreqResultFragment.FreqAsyncResponse delegate4 = null;

    public GetResults(Context context) {
		this.context = context;
        Log.i(TAG, "context is: " + this.toString());
    	Locale.setDefault(locale);
    	config.locale = locale;
	}

	@Override
	protected void onPreExecute() {
        super.onPreExecute();
        if (dialog == null){
		    //dialog = new ProgressDialog(context);
            Activity a = (Activity) context; // this is just a shot in the dark
            dialog = new ProgressDialog(a);
            dialog.setMessage("Retrieving results.");
		    dialog.show();
       }
	}
	
	@Override
	protected ArrayList doInBackground(String... urls) {
		BufferedReader reader = null;
        ArrayList<String> all_results = new ArrayList<String>();

		try {
			String search_URI = urls[0];
			Log.i(TAG + "  Search URI: ", search_URI);

            // test for kind of search //
            if (search_URI.contains("report=")){
                report_search = true;
                Log.i(TAG + "  Search report!!", report_search.toString());
                if (search_URI.contains("dispatcher.py?report=concordance")){
                    Log.i(TAG, "We have concordance!");
                    conc_report = true;
                    }
                else if (search_URI.contains("get_frequency.py")) {
                    Log.i(TAG, "We have frequency!");
                    freq_report = true;
                    }
                else if (search_URI.contains("report=bibliography")){
                    Log.i(TAG, "We have bibliography search!");
                    bibliography_report = true;
                    }
                }
            // toc report //
            else if (search_URI.contains("get_table_of_contents.py")){
                Log.i(TAG, " TOC display!");
                report_search = false;
                toc_search = true;
                }
            // fulltext display //
            else {
                report_search = false;
                toc_search = false;
                Log.i(TAG + "  Full text search:", report_search.toString());
                }

            Log.i(TAG, "conc_report == " + conc_report +
                       "; freq_report == " + freq_report +
                       "; bib_report == " + bibliography_report);

			URI search_query = new URI(urls[0]);
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(search_query);
			
			HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();

            // read results into buffer //
            try {
            	reader = new BufferedReader(new InputStreamReader(content));
            	String line = "";
            	
            	while ((line = reader.readLine()) != null) {
                    //Log.i(TAG + "  Your string: ", line.toString());
                    if (report_search) {
                        Log.i(TAG + "  Running a report search", "Boolean is true!");

                        if (conc_report != null){
                            Log.i(TAG, "  concordance report");
                            JSONArray jsonArray = new JSONArray(line.toString());
                            for (int i = 0; i< jsonArray.length(); i++){
                                JSONObject result_line = jsonArray.getJSONObject(i);
                                text = result_line.getString("text");
                                philoid = result_line.getString("philo_id");
                                offsets = result_line.getString("offsets");
                                citation = result_line.getString("citation");
                                list_shrtcit = result_line.getString("shrtcit"); // not using shrtcit in list; only fulltext
                                total_hits = result_line.getInt("hit_count");
                                start_hit = result_line.getInt("start");
                                //Log.i(TAG, "Shrtcit and philoid == " + list_shrtcit + " " + philoid);
                                hit_number = i + start_hit;
                                String out_pair = "<off>" + offsets + "</off><pid>" + philoid + "</pid><hit>" + hit_number + "</hit>) " + citation + "<cmc>" + text;
                                all_results.add(out_pair);
                                }
                            }
                        else if (freq_report != null) {
                            Log.i(TAG, "  freq report");
                            JSONArray jsonArray = new JSONArray(line.toString());
                            for (int i = 0; i< jsonArray.length(); i++){
                                JSONObject result_line = jsonArray.getJSONObject(i);
                                hit_number = i + 1;
                                field = result_line.getString("frequency_field");
                                freq_citation = result_line.getString("bib_values");
                                count = result_line.getString("count");
                                int count_value = Integer.parseInt(count);
                                freq_url = result_line.getString("url");
                                search_term = result_line.getString("search_term");
                                freq_results = result_line.getString("results");
                                freq_results = freq_results.replaceAll("\n", "");
                                String out_instance = "";
                                //Log.i(TAG, " Metadata values:  " +  field + " " + search_term + " " + freq_citation);
                                metadata_values = new String[] {field, search_term, freq_citation};
                                if (count_value > 1) {
                                    out_instance = "instances";
                                }
                                else {
                                    out_instance = "instance";
                                }
                                //total_hits = result_line.getInt("hit_count");
                                //start_hit = result_line.getInt("start");
                                //Log.i(TAG, "First hit is number == " + start_hit);
                                //hit_number = i + start_hit;
                                String out_pair = "<div class=\"freq_result\">" + hit_number + ") " + freq_results +
                                        "<div class=\"freq_count\"><a href=\"" + freq_url + "\">" +
                                        count + " " + out_instance + "</a></div></div>";
                                //Log.i(TAG, " Out Pair: " + out_pair);
                                all_results.add(out_pair);
                                }
                            }
                        else if (bibliography_report != null){
                            Log.i(TAG, " bibliography report");
                            JSONArray jsonArray = new JSONArray(line.toString());
                            for (int i = 0; i< jsonArray.length(); i++){
                                JSONObject result_line = jsonArray.getJSONObject(i);
                                philoid = result_line.getString("philo_id");
                                citation = result_line.getString("citation");
                                text = result_line.getString("text");
                                total_hits = result_line.getInt("hit_count");
                                hit_number = i + 1;
                                String out_pair = "<pid>" + philoid + "</pid><hit>" + hit_number + "</hit>) " + citation + "<cmc>" + text;
                                all_results.add(out_pair);
                                }
                            }
            		    }
                    else if (toc_search) {
                        Log.i(TAG, " Building TOC array");
                        JSONObject jsonObject = new JSONObject(line.toString());
                        String toc_string = jsonObject.getString("toc");
                        String toc_title = jsonObject.getString("citation");
                        //String tmp_title = "Chuckler's Honor";
                        String out_pair = "<title>" + toc_title + "</title>" + toc_string;
                        all_results.add(out_pair);
                        }
                    else {
                        Log.i(TAG + "  Doing a fulltext search", "Boolean is false");
                        JSONObject jsonObject = new JSONObject(line.toString());
                        //String dummy_tag = "<cmc>" + "Read text!" + "</cmc>";
                        String json_string = jsonObject.getString("text");
                        full_shrtcit = jsonObject.getString("shrtcit");
                        full_philoid = jsonObject.getString("current");
                        prev = jsonObject.getString("prev");
                        next = jsonObject.getString("next");
                        citation = jsonObject.getString("citation");
                        String info_tag = "<shrt>" + full_shrtcit + "</shrt><title>" + citation + "</title>";
                        String out_pair = info_tag + json_string.toString();
                        all_results.add(out_pair);
                        Log.i(TAG, "  Creating FullText array! With shrtcit: " + full_shrtcit + " And fullcit: " + citation);
                        }
                    }
            	}
            catch (IOException exception) {
            	Log.e(TAG, "Here? IOException --> " + exception.toString());
            	}
            // pro-forma cleanup //
            finally {
            	if (reader != null) {
            		try {
            			reader.close();
            		}
            		catch (IOException exception) {
            			Log.e(TAG, "IOException --> " + exception.toString());
            		}
            	}
            }
		}
		// Exception for problems with HTTP connection //
		catch (Exception exception) {
            Log.e(TAG, "Trouble connecting -->" + exception.toString());
            return null;
		}
        //Log.i(TAG + "  Results string: ", all_results.toString());
		return all_results;
	}

    @Override
	protected void onPostExecute(ArrayList all_results) {

        // this might in fact keep app from //
        // crashing on back button //
        if (!isCancelled()){

           if (dialog != null && dialog.isShowing()){
                dialog.dismiss();
           }

            Log.i(TAG, "Total hits: " + total_hits);
            if (report_search) {
                if (freq_report != null){
                    Activity a = (Activity) context;
                    Log.i(TAG, "Report search onPostExecute: " + a.toString());
                    Log.i(TAG + " onPostExecute: ", "passing to FreqResultFragment");
                    delegate4 = (FreqResultFragment.FreqAsyncResponse) a;
                    delegate4.asyncFinished4(all_results, field, metadata_values);
                    }
                else {
                    Activity a = (Activity) context;
                    Log.i(TAG, "Report search onPostExecute: " + a.toString());
                    Log.i(TAG + " onPostExecute: ", "passing to ListResultFragment");
                    delegate = (ListResultFragment.SearchAsyncResponse) a;
                    delegate.asyncFinished(all_results, total_hits, start_hit, bibliography_report);
                }
            }

            else if (toc_search) {
                Activity a = (Activity) context;
                Log.i(TAG, "TOC search onPostExecute: " + a.toString());
                Log.i(TAG, " Displaying a TOC.");
                delegate3 = (TOCResultFragment.TOCAsyncResponse) a;
                delegate3.asyncFinished3(all_results);
            }

            else {
                Activity a = (Activity) context;
                Log.i(TAG, "FullText onPostExecute: " + a.toString());
                Log.i(TAG + " onPostExecute: ", "passing to FullResultFragment");
                delegate2 = (FullResultFragment.FullAsyncResponse) a;
                delegate2.asyncFinished2(all_results, prev, next, full_shrtcit, full_philoid);
            }
        }
    }

}
