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
 * Created by cmcooney on 6/4/14.
 */
public class TOCResultFragment extends Fragment {
    String TAG = "TOCResultFragment";
    TOCAsyncResponse tocAsyncResponse;

    public interface TOCAsyncResponse {
        public void asyncFinished3(ArrayList all_results);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.i(TAG, " onAttach works...");
        try {
            tocAsyncResponse = (TOCAsyncResponse) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " Problem with asyncResponse");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to TOCResultFragment!");
        //View view = inflater.inflate(R.layout.full_result_frag, container, false);
        View view = inflater.inflate(R.layout.toc_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String new_query_uri = bundle.getString("new_query_uri");
        Log.i(TAG + " toc query string: ", new_query_uri);
        Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        String test_activity = this.getActivity().toString();
        Log.i(TAG + " What's the activity?: ", test_activity);
        GetResults gr = new GetResults(getActivity());
        gr.execute(new_query_uri);

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

