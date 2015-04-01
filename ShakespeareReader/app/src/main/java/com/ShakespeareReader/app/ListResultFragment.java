package com.ShakespeareReader.app;

import android.app.Activity;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * Created by cmcooney on 6/6/14.
 */
public class ListResultFragment extends Fragment {
    String TAG = "ListResultFragment";
    SearchAsyncResponse asyncResponse;
    Boolean report_search;

    public interface SearchAsyncResponse {
        public void asyncFinished(ArrayList all_results, int total_hits, int start_hit, Boolean bibliography_report);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.i(TAG, " onAttach works...");
        try {
            asyncResponse = (SearchAsyncResponse) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AsyncResponse2");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to ListResultFragment!");
        View view = inflater.inflate(R.layout.list_result_linear, container, false);
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
        String test_activity = this.getActivity().toString();
        Log.i(TAG + " What's the activity?: ", test_activity);
        report_search = true;

        GetResults gr = new GetResults(getActivity());
        gr.execute(query_uri);

    }

    /*
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, " ON PAUSEEEEE!!!!!!");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, " ON STOPPPPPPPP!!!!!!!");
    }*/
}
