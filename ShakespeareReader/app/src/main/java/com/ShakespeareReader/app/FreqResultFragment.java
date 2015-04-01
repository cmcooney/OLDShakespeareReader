package com.ShakespeareReader.app;

import android.app.Activity;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * Created by cmcooney on 6/6/14.
 */
public class FreqResultFragment extends Fragment {
    String TAG = "FreqResultFragment";
    FreqAsyncResponse freqasyncResponse;
    Boolean report_search;

    public interface FreqAsyncResponse {
        public void asyncFinished4(ArrayList all_results, String field, String[] metadata_values);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.i(TAG, " onAttach works...");
        try {
            freqasyncResponse = (FreqAsyncResponse) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AsyncResponse4");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to FreqResultFragment!");
        View view = inflater.inflate(R.layout.freq_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String query_uri = bundle.getString("query_uri");
        Log.i(TAG + " fulltext query string: ", query_uri);
        Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        report_search = true;

        GetResults gr = new GetResults(getActivity());
        gr.execute(query_uri);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}