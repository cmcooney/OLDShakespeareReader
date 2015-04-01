package com.ShakespeareReader.app;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements
        ListResultFragment.SearchAsyncResponse,
        FullResultFragment.FullAsyncResponse,
        TOCResultFragment.TOCAsyncResponse,
        FreqResultFragment.FreqAsyncResponse {

    private static final String TAG = "MainActivity";
    public static String layout_type;
    ConnectionDetector cd;
    GetResults gr;
    AsyncTask<String, Void, Boolean> url_con;
    private DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    //RadioButton startingRadioValue, concordance_search, frequency_search;
    //RadioGroup radioSearchGroup;
    Spinner spinner;
    public boolean get_concordance;
    public String query_search_type = "concordance"; // default
    EditText search_et;
    //EditText author_et;
    EditText title_et;
    public String uri_authority = "condorcet.uchicago.edu";
    public String philo_dir = "philologic4";
    public String build_name = "shakespeare_demo";
    private ListView mListView;
    private TextView mTextView;
    private WebView mWebView;
    public DisplayResultsAdapter outAdapter;
    public boolean canAddBookmark = false;
    public boolean thisIsABookmark = false;
    public boolean lookingAtInfo = false;
    public String bookmarkPhiloId = "";
    public String bookmarkShrtCit = "";
    public String conc_title_from_freq = "";
    public String conc_author_from_freq = "";
    public String conc_date_from_freq = "";
    public String freq_search_term = "";
    public Boolean conc_from_freq;
    public float chuck_float =  Float.parseFloat(".25");
    AddBookmark addBookmark;
    SubMenu bookmarkMenu;
    String selected_bookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG + "  onCreate", "onCreate being called!");

        // From Walt's Encyc //
        // Determine which layout size is being used so we can organize the fragments properly

        if (((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE)
                || (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            layout_type = "tablet";
        } else {
            layout_type = "phone";
        }
        Log.i(TAG + "  Which Layout?", layout_type);

        // Drawer settings //

        if (layout_type.equals("tablet")) {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        }

        setContentView(R.layout.drawer_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_mainView);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.report_options,
                R.layout.spinner_item);
        spinner.setAdapter(adapter);

        // skipping Walt's display.getWidth() code in favor of
        // getScreenOrientation at bottom

        if (layout_type.equals("tablet")) {
            if (getScreenOrientation() == 2) {
                Log.i(TAG + "  Orientation", "landscape");
                //mDrawerLayout.openDrawer(Gravity.LEFT);
                mDrawerLayout.setFocusableInTouchMode(false);
            } else if (getScreenOrientation() == 1) {
                Log.i(TAG + "  Orientation", "portrait");
                //mDrawerLayout.closeDrawer(Gravity.LEFT);
                mDrawerLayout.setFocusableInTouchMode(false);
            }
        } else {
            Log.i(TAG + "  Orientation", "Fixed portrait, you're on a phone...");
            //mDrawerLayout.openDrawer(Gravity.LEFT);
            mDrawerLayout.setFocusableInTouchMode(false);
        }

        // adapted from:
        // http://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/

        // items in
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        cd = new ConnectionDetector(getApplicationContext());

        if (!cd.isConnectingToInternet()) {
            Log.i(TAG + "  ConnectingToInternet", "Aie! Check the connection!");
            //Toast.makeText(this, "Not connected to internet.", Toast.LENGTH_SHORT).show();
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);

            dialog.setContentView(R.layout.no_connection_dialog);
            dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_warning );
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.DIM_AMOUNT_CHANGED, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialog.getWindow().getAttributes().dimAmount = 0;

            final Button b = (Button) dialog.findViewById(R.id.no_connection_button);

            // If you touch the dialog then it will exit
            b.setOnLongClickListener(new Button.OnLongClickListener() {
                public boolean onLongClick(final View v) {
                    b.setBackgroundColor(0xffde5800);
                    dialog.dismiss();
                    System.exit(0);
                    return true;
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

        }
        else {
            Log.i(TAG + "  ConnectingToInternet", "You are connected...");
            url_con = new UrlConnect();
            url_con.execute();
            //Toast.makeText(this, "Ready to begin.", Toast.LENGTH_LONG).show();
            try {
                if (url_con.get() == false) {
                    Log.i(TAG + "  URL Connect", "Yike -- server not live!");
                    //Toast.makeText(this, "Remote server is down. Try again later.", Toast.LENGTH_SHORT).show();
                    final Dialog dialog = new Dialog(this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
                    dialog.setContentView(R.layout.server_down_dialog);
                    dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_warning );
                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.DIM_AMOUNT_CHANGED, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                    dialog.getWindow().getAttributes().dimAmount = 0;

                    final Button b = (Button) dialog.findViewById(R.id.server_down_button);

                    // If you touch the dialog then it will exit
                    b.setOnLongClickListener(new Button.OnLongClickListener() {
                        public boolean onLongClick(final View v) {
                            b.setBackgroundColor(0xffde5800);
                            dialog.dismiss();
                            System.exit(0);
                            return true;
                        }
                    });

                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        addBookmark = new AddBookmark(getApplicationContext());
        addBookmark.createDataBase();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                Log.i(TAG + "  onDrawerClosed", "stock code");
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                Log.i(TAG +  "  onDrawerOpened", "stock code");
                invalidateOptionsMenu();
                conc_from_freq = null;

                spinner = (Spinner) findViewById(R.id.spinner);
                spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                        Log.i(TAG, "Your item: " + parent.getItemAtPosition(pos).toString());
                        String query_type_to_send = parent.getItemAtPosition(pos).toString();
                        newQuerySelector(query_type_to_send);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        }
                    });
                // Handle the radio buttons -- keeping code around for future use. //

                //radioSearchGroup = (RadioGroup) findViewById(R.id.search_radios);
                //int selectedRadio = radioSearchGroup.getCheckedRadioButtonId();
                //startingRadioValue = (RadioButton) findViewById(selectedRadio);

                //String button_value = startingRadioValue.toString();
                //Log.i(TAG +  "  Radio button:", button_value);

                //if (startingRadioValue.findViewById(R.id.freq_radio) != null) {
                //    setQuerySelector(false);
                //} else {
                //    setQuerySelector(true);
                //}
                /*
                concordance_search = (RadioButton) findViewById(R.id.conc_radio);
                frequency_search = (RadioButton) findViewById(R.id.freq_radio);

                concordance_search.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setQuerySelector(true);
                    }
                });

                frequency_search.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setQuerySelector(false);

                    }
                });*/

            } // end onDrawerOpened
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Deal with search form submit and reset //

        Button search = (Button) findViewById(R.id.search_button);
        search_et = (EditText)findViewById(R.id.search_edittext);
        //author_et = (EditText) findViewById(R.id.author_edittext);
        title_et = (EditText) findViewById(R.id.title_edittext);

        search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Context context = MainActivity.this; // need to do this to pass context to GetResults

                // eh, not so sure about this hardcoding, but so it goes...//
                final String my_start_hit = "1";
                final String my_end_hit = "25";

                makeMyQueryUri(my_start_hit, my_end_hit);

                mDrawerLayout.closeDrawer(Gravity.LEFT);
                InputMethodManager imm = (InputMethodManager)getSystemService(context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search_et.getWindowToken(), 0);

            }
        });

        Button reset = (Button) findViewById(R.id.search_reset);
        reset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search_et.setText("");
                //author_et.setText("");
                title_et.setText("");
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                spinner.setSelection(0);
                //RadioButton reset_conc = (RadioButton) findViewById(R.id.conc_radio);
                //reset_conc.setChecked(true);
            }
        });

    }

    // Get query type from spinner //

    public String newQuerySelector(String query_selection){
        // Hard coding to "concordance" at top //
        Log.i(TAG, " In newQuerySelector;  query selection is: " + query_selection);
        if (query_selection.contains("Concordance Report")){
            query_search_type = "concordance";
            }
        else if (query_selection.contains("Frequency by Author")){
            query_search_type = "author";
        }
        else if (query_selection.contains("Frequency by Title")){
            query_search_type = "title";
        }
        else if (query_selection.contains("Frequency by Date")){
            query_search_type = "date";
        }
        return query_search_type;
    }

    /*
    // save this for future use. radio buttons
    public boolean setQuerySelector(boolean concordance_report) {

    if (concordance_report) {
          Log.i(TAG + "  Radio button says: ", "I want concordance!!");
          get_concordance = true;
        }
    else {
          Log.i(TAG + "  Radio button says: ", "I want frequency!!");
          get_concordance = false;
          }
          return get_concordance;
    }*/



    @Override
    public void asyncFinished(ArrayList all_results, int total_hits, int start_hit, Boolean bibliography_report){

        Context context = MainActivity.this;

        Log.i(TAG + "  asyncFinished", "Getting Results from onPostExecute!");
        Log.i(TAG, "  total hits passed! " + total_hits);
        Log.i(TAG, " Starting hit for this set of results == " + start_hit);
        //Log.i(TAG, " asyncFinished results: " + all_results);
	final TextView mTextView;
        final ListView mListView;
        thisIsABookmark = false;
        canAddBookmark = false;
        final Boolean bibliography_report2pass = bibliography_report; // need this for 'inner class'

	    if (findViewById(R.id.hit_count ) == null) {
            Log.i(TAG, "GAWWWWWD -- no view. ");
            //LayoutInflater inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View emergency_view = LayoutInflater.from(context).inflate(R.layout.list_result_linear,null);
            mTextView = (TextView) emergency_view.findViewById(R.id.hit_count);
            mListView = (ListView) emergency_view.findViewById(R.id.results_list);
        } else {
            mTextView = (TextView) findViewById(R.id.hit_count);
            mListView = (ListView) findViewById(R.id.results_list);
        }
        //mTextView = (TextView) findViewById(R.id.hit_count);
        //mListView = (ListView) findViewById(R.id.results_list);

        String count_display = getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);

        mTextView.setText(count_display);
        if (all_results != null && !all_results.isEmpty()) {

            try {
               Log.i(TAG + "  Sending results to Results Adapter:", "verified");
               outAdapter = new DisplayResultsAdapter(context, R.layout.result, all_results);
               mListView.setAdapter(outAdapter);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (total_hits > 25 && bibliography_report == null) {
                int next_start_hit = start_hit + 25;
                int next_end_hit = next_start_hit + 24;

                final String my_start_hit = Integer.toString(next_start_hit);
                final String my_end_hit = Integer.toString(next_end_hit);
                final String my_prev_start_hit = Integer.toString(start_hit - 25);
                final String my_prev_end_hit = Integer.toString(start_hit - 1);

                LayoutInflater inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View buttons_view = inflater.inflate(R.layout.image_buttons, null);

                ImageButton prev_btn = (ImageButton) buttons_view.findViewById(R.id.ll_previous);
                ImageButton next_btn = (ImageButton) buttons_view.findViewById(R.id.ll_next);

                next_btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "You clicked next!");
                        makeMyQueryUri(my_start_hit, my_end_hit);
                    }
                });

                prev_btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "You clicked previous!");
                        makeMyQueryUri(my_prev_start_hit, my_prev_end_hit);
                    }
                });

                if (start_hit == 1) {
                    //prev_btn.setVisibility(View.INVISIBLE);
                    prev_btn.setAlpha(chuck_float);
                    prev_btn.setOnClickListener(null);
                }
                else if (total_hits < next_start_hit) {
                    //next_btn.setVisibility(View.INVISIBLE);
                    next_btn.setAlpha(chuck_float);
                    next_btn.setOnClickListener(null);
                }
                if (findViewById(R.id.list_res_linear) == null){
                    Log.i(TAG, " THIS VIEW SHIT....");
                    //LayoutInflater emergency_inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
                    View emergency_view = LayoutInflater.from(context).inflate(R.layout.list_result_linear,null);
                    View insertPoint = emergency_view.findViewById(R.id.list_res_linear);
                    ((ViewGroup) insertPoint).addView(buttons_view, 1, new
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                } else {
                    View insertPoint = findViewById(R.id.list_res_linear);
                    ((ViewGroup) insertPoint).addView(buttons_view, 1, new
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }

            } // end next + prev button code

            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String single_result_hit = mListView.getItemAtPosition(position).toString();
                        String pid_query_string_match = "";
                        String pid_address = "";
                        Pattern pid_regex = Pattern.compile("<pid>([^<]*)</pid>");
                        Matcher pid_match = pid_regex.matcher(single_result_hit);
                        String offsets = "";
                        Pattern offset_regex = Pattern.compile("<off>([^<]*)</off>");
                        Matcher off_match = offset_regex.matcher(single_result_hit);

                        if (bibliography_report2pass == null){
                            if (pid_match.find() && off_match.find()) {
                                pid_address = pid_match.group(1);
                                pid_address = pid_address.replaceAll("\\[", "").replaceAll("\\]", "");
                                offsets = off_match.group(1);
                                offsets = offsets.replaceAll("\\[", "").replaceAll("\\]", "");

                                Log.i(TAG, " Your philoID && offsets: " + pid_address + "|" + offsets);

                                String[] pid_address_array = pid_address.split(",");
                                String[] offsets_2pass = offsets.split(",");
                                buildFullTextFragment(pid_address_array, offsets_2pass);
                                }
                            }
                        else {
                            Log.i(TAG, " This is a bibliography report, need different stuff");
                            Log.i(TAG, " Goodies to get your TOC " + single_result_hit);
                            if (pid_match.find()){
                                pid_query_string_match = pid_match.group(1);
                                String[] pid_query_array = pid_query_string_match.split(",");
                                buildTOCFragment(pid_query_array);

                                }
                            }
                        }
                    }); // end click listener
                }
        else {

            // Generate no results message in button //
            Log.i(TAG, "NO RESULTS!");
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.no_results_dialog);
            dialog.getWindow().getAttributes().dimAmount = 0;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    @Override
    public void asyncFinished2(ArrayList all_results, String prev, String next, String full_shrtcit, String full_philoid){

        Context context = MainActivity.this;
        Log.i(TAG + "  asyncFinished2", "Getting Results from onPostExecute!");
        Log.i(TAG + " Your current & prev & next philoids + shrtcit: ", full_philoid + " " + prev + "/" + next + "; " + full_shrtcit);
        String results_array = all_results.toString();

        full_philoid = full_philoid.replaceAll("[\\[\\]]", "");
        String[] bookMarkEdit = full_philoid.split(",");
        bookmarkPhiloId = bookMarkEdit[0] + "/" + bookMarkEdit[1] + "/" + bookMarkEdit[2];
        //Log.i(TAG + " Fulltext results look like this: ", results_array);
        //String[] split_results_array = results_array.split("</shrt>");
        String[] split_results_array = results_array.split("</title>");
        String[] title_chunk = split_results_array[0].split("</shrt>");
        String title_string = title_chunk[1];
        title_string = title_string.replace("<title>", "<div class=\"title\">");
        title_string = title_string.replace("</title>", "</div>");
        title_string = title_string.replace("|", "&nbsp;");
        title_string = title_string.trim();
        Log.i(TAG, " Title string: " + title_string);
        String results_string = split_results_array[1];
        results_string = results_string.replaceFirst("]$","");
        //results_string = results_string.replace("<title>", "<div class=\"title\">");
        //results_string = results_string.replace("</title>", "</div>");
        //results_string =  results_string.replace("|", "&nbsp;");

        final String[] prev_array = prev.split(" ");
        final String[] next_array = next.split(" ");
        final String[] offsets = {};
        ImageButton prev_btn;
        ImageButton next_btn;
        TextView mTextView;
        WebView mWebView;
        if (findViewById(R.id.ll_previous ) == null) {
            Log.i(TAG, "GAWWWWWD -- no view. ");
            //LayoutInflater emergency_inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view = LayoutInflater.from(context).inflate(R.layout.full_result_linear, null);
            prev_btn = (ImageButton) view.findViewById(R.id.ll_previous);
            next_btn = (ImageButton) view.findViewById(R.id.ll_next);
            mTextView = (TextView) view.findViewById(R.id.full_text_title);
            mWebView = (WebView) view.findViewById(R.id.full_wv_text_result);
        } else {
            prev_btn = (ImageButton) findViewById(R.id.ll_previous);
            next_btn = (ImageButton) findViewById(R.id.ll_next);
            mTextView = (TextView) findViewById(R.id.full_text_title);
            mWebView = (WebView) findViewById(R.id.full_wv_text_result);
        } 

        next_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Log.i(TAG, "You clicked next! Length: " + next_array.length + " " + next_array[0] + "/" + next_array[1] + "/" + next_array[2]);
                buildFullTextFragment(next_array, offsets);
            }
        });

        prev_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Log.i(TAG, "You clicked prev!" + prev_array[0] + "/" + prev_array[1] + "/" + prev_array[2]);
                buildFullTextFragment(prev_array, offsets);
            }
        });

        if (prev.isEmpty()){
            prev_btn.setAlpha(chuck_float);
            prev_btn.setOnClickListener(null);
        }
        if (next.isEmpty()){
            next_btn.setAlpha(chuck_float);
            next_btn.setOnClickListener(null);
        }

        mTextView.setText(Html.fromHtml(title_string));
        final String getTOC = title_string;
        mTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " Get the TOC from bib citation" + getTOC);
                String pid_query_string_match = "";
                Pattern pid_regex = Pattern.compile("<a data-id='([^']*)'");
                Matcher pid_match = pid_regex.matcher(getTOC);
                if (pid_match.find()){
                    pid_query_string_match = pid_match.group(1);
                    //Log.i(TAG , " PID for TOC: " + pid_query_string_match);
                    String[] pid_query_array = pid_query_string_match.split(" ");
                    buildTOCFragment(pid_query_array);
                }
            }
        });
        String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\">";
        html_header = html_header.concat("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>");
        html_header = html_header.concat("<script src=\"file:///android_asset/scroll2hit.js\" type=\"text/javascript\"></script>");
        html_header = html_header.concat("<script src=\"file:///android_asset/popnote.js\" type=\"text/javascript\"></script></head>");
        //Log.i(TAG, "HEADER: " + html_header);
        results_string = "<body>" + html_header + results_string + "</body>";
        //Log.i(TAG, " Full html: " + results_string);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url){
                    view.loadUrl("javascript:getOffset();");
                }
        });
        //mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // for nativeOnDraw error
        mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");


        ///// DO NOT DELETE THESE /////
        bookmarkShrtCit = full_shrtcit;
        canAddBookmark = true;
    }


    @Override
    public void asyncFinished3(ArrayList all_results){

        Log.i(TAG + "  asyncFinished3", "Getting Results from onPostExecute!");
        //Log.i(TAG + " Your toc: ", all_results.toString());
	Context context = MainActivity.this;
        WebView mWebView;
        String results_array = all_results.toString();
        String results_string = results_array.replaceAll("[\\[\\]]", "");
        results_string = results_string.replace("<title>", "<div class=\"toc-title\">");
        results_string = results_string.replace("</title>", "</div>");
        results_string =  results_string.replace("|", "&nbsp;");

        //Log.i(TAG + " Grab id from here: ", results_string);

        if (findViewById(R.id.toc_wv_result ) == null) {
            Log.i(TAG, "GAWWWWWD -- no view. ");
            //LayoutInflater emergency_inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view = LayoutInflater.from(context).inflate(R.layout.toc_result_linear, null);
            mWebView = (WebView) view.findViewById(R.id.toc_wv_result);
        }
        else {
            mWebView = (WebView) findViewById(R.id.toc_wv_result);
        }

        String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
        results_string = "<body>" + html_header + results_string + "</body>";

        //mWebView = (WebView) findViewById(R.id.toc_wv_result);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, " Your URL: " + url);
                if (url != null && url.contains("dispatcher.py")) {
                    String[] chuck_uri_array = url.split("/");
                    String[] offsets = {};
                    String [] query_array = new String[9];
                    query_array[0] = chuck_uri_array[6];
                    query_array[1] = chuck_uri_array[7];
                    query_array[2] = chuck_uri_array[8];
                    query_array[3] = chuck_uri_array[9];
                    query_array[4] = "0";
                    query_array[5] = "0";
                    query_array[6] = "0";
                    query_array[7] = "0";
                    query_array[8] = "";
                    String chuck_uri = chuck_uri_array[6] + "/" + chuck_uri_array[7] + "/" + chuck_uri_array[8] + "/" + chuck_uri_array[9]
                            + "/" + chuck_uri_array[10] + "/" + chuck_uri_array[11];
                    Log.i(TAG, "F-ing with uri. Need: " + chuck_uri);
                    buildFullTextFragment(query_array, offsets);
                    return true;
                } else {
                    return false;
                }
            }
        });

        mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");
    }

    @Override
    public void asyncFinished4(ArrayList all_results, String field, String[] metadata_values){
        Context context = MainActivity.this;
        Log.i(TAG + "  asyncFinished4", "Getting Results from onPostExecute!");

        if (all_results != null && !all_results.isEmpty()) {
            //Log.i(TAG + " Your freq result: ", all_results.toString());
            Log.i(TAG, " Freq params to display: " + field + " " + metadata_values);
            String freq_search_value = metadata_values[0];
            freq_search_term = metadata_values[1];
            String extra_metadata = metadata_values[2];
            extra_metadata = extra_metadata.replaceAll("[{}\"]", "");
            //String freq_params_to_display = "<div class=\"title\">Frequency of \"" + search_term + "\" by " + field + "</div>";
            String freq_params_to_display = "<div class=\"title\">Frequency of \"" + freq_search_term + "\" by " + freq_search_value;

            if (!extra_metadata.isEmpty()) {
                freq_params_to_display = freq_params_to_display + ", " + extra_metadata + "</div>";
            } else {
                freq_params_to_display = freq_params_to_display + "</div>";
            }

            Log.i(TAG, " Display: " + freq_params_to_display);
            String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
            String results_string = all_results.toString();
            results_string = results_string.replaceAll("[\\[\\]]", "");
            //results_string = results_string.replace("[\\]]", "");
            results_string = results_string.replaceAll("</div>,", "</div><br>");
            results_string = "<body>" + html_header + results_string + "</body>";
            //Log.i(TAG, " Results string: " + results_string);
            TextView mTextView;
            WebView mWebView;

            if (findViewById(R.id.freq_params ) == null) {
                Log.i(TAG, "GAWWWWWD -- no view. ");
                //LayoutInflater emergency_inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = LayoutInflater.from(context).inflate(R.layout.freq_result_linear, null);
                mTextView = (TextView) view.findViewById(R.id.freq_params);
                mWebView = (WebView) view.findViewById(R.id.freq_results_list);
            }
            else {
                mTextView = (TextView) findViewById(R.id.freq_params);
                mWebView = (WebView) findViewById(R.id.freq_results_list);
            }
            //mTextView = (TextView) findViewById(R.id.freq_params);
            Log.i(TAG, " Textview: " + mTextView.toString());

            mTextView.setText(Html.fromHtml(freq_params_to_display));
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(TAG, " Your URL: " + url);
                    if (url != null && url.contains("dispatcher.py")) {
                        Bundle bundle = new Bundle();
                        url = url.replace("file:///android_asset/", "");
                        url = url.replace("&report=concordance", "");
                        url = url.replace("dispatcher.py/?", "dispatcher.py?report=concordance&");
                        String my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/" + url +
                                "&format=json";
                        bundle.putString("query_uri", my_query_uri);
                        Log.i(TAG, "  FREQ query_uri: " + my_query_uri);
                        Fragment fr;
                        fr = new ListResultFragment();
                        fr.setArguments(bundle);
                        Log.i(TAG + "  onClick getting fragment", fr.toString());
                        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                        //FragmentManager fm = getFragmentManager();
                        FragmentManager fm = getSupportFragmentManager();
                        Log.i(TAG, " Listview click backstack count: "+ fm.getBackStackEntryCount());
                        //FragmentTransaction fragTransaction = fm.beginTransaction();
                        fragTransaction.addToBackStack(null);
                        fragTransaction.replace(R.id.text, fr, "text");
                        fragTransaction.commit();
                        Log.i(TAG, " Listview post-commit click backstack count: "+ fm.getBackStackEntryCount());

                        // Now set values to do conc links from freq report //
                        conc_from_freq = true;
                        conc_title_from_freq = "";
                        conc_author_from_freq = "";
                        conc_date_from_freq = "";
                        Pattern freq_title_regex = Pattern.compile("&title=(.*)");
                        Matcher freq_title = freq_title_regex.matcher(url);
                        if (freq_title.find()){
                            Log.i(TAG, " Getting title from FREQ url");
                            conc_title_from_freq = freq_title.group(1);
                        }
                        Pattern freq_author_regex = Pattern.compile("&author=(.*)&title=");
                        Matcher freq_author = freq_author_regex.matcher(url);
                        if (freq_author.find()){
                            conc_author_from_freq = freq_author.group(1);
                        }
                        Pattern freq_date_regex = Pattern.compile("&date=(.*)&");
                        Matcher freq_date = freq_date_regex.matcher(url);
                        if (freq_date.find()){
                            conc_date_from_freq = freq_date.group(1);
                        }
                        Log.i(TAG, "  FREQ metdata params: " + conc_title_from_freq + "/" + conc_author_from_freq + "/" + conc_date_from_freq);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            Log.i(TAG, " WebView: " + mWebView.toString());
            mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");

        }
        else {
            // Generate no results message in button //
            Log.i(TAG, "NO RESULTS!");
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.no_results_dialog);
            dialog.getWindow().getAttributes().dimAmount = 0;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            //Log.i(TAG, " You pressed back, gr status is: " + gr.getStatus());
            if (gr != null && gr.getStatus() == AsyncTask.Status.RUNNING) {
                Log.i(TAG, " Here's some goofiness. " + gr.getStatus().toString());
                gr.cancel(true);
            }
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft;
            ft = getSupportFragmentManager().beginTransaction();

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            Log.i(TAG, " onKeyDown backstack count == " + fm.getBackStackEntryCount());
            if (fm.getBackStackEntryCount() == 1){
                Log.i(TAG, " You are probably at list view level. Don't close app.");
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return false;
                }
            else if (fm.getBackStackEntryCount() > 1) {
                Log.i(TAG, " You are probably at fulltext view level. Pop that backstack!");
                fm.popBackStack();
                Log.i(TAG, " Is it officially destroyed? " + fm.getBackStackEntryCount());
            } 
            Log.i(TAG, " post onKeyDown backstack count == " + fm.getBackStackEntryCount());
            ft.commit();
        }
        return super.onKeyDown(keyCode, event);
    }

    // Inflate the menu; this adds items to the action bar if it is present. //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Log.i(TAG + "  onCreateOptionsMenu", "returning true");

        // show bookmarks here //
        bookmarkMenu = menu.findItem(R.id.show_bookmarks).getSubMenu();

        Cursor cursor = addBookmark.showBookmarkItems();
        if (cursor == null){
            Log.i(TAG, " You ain't got no bookmarks.");
        }
        else {
            cursor.moveToFirst();
            if (cursor.moveToFirst()){
                do {
                    Log.i(TAG, " Cursor out string: " + cursor.getString(1));
                    bookmarkMenu.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        addBookmark.close();

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(TAG, " onPrepareOptionsMenu at work");
        return super.onPrepareOptionsMenu(menu);
    }

    // Sync the toggle state after onRestoreInstanceState has occurred. //
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         Log.i(TAG + "  onPostcreate", "running just fine, thanks.");
         mDrawerToggle.syncState();
    }

    // Pass any configuration change to the drawer toggle //
    // Not working 5-13-14
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         Log.i(TAG + "  onConfigurationChanged", "yup, this is firing");
         mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Toggle drawer by clicking on ActionBar icon //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.i(TAG + "  onOptionsItemSelected", "drawer toggle true!");
            return true;
        }
        Log.i(TAG + " menu item clicked: ", item.toString());
        Log.i(TAG + " menu item ID: ", item.getTitle().toString());
        switch (item.getItemId()){

            case R.id.app_info:
                // This keeps bookmark AlertDialog from popping up
                // when clicking on the info icon. See below in
                // "default". I set back to false in displayInfoDialog
                // to re-enable bookmarks.
                lookingAtInfo = true;
                return true;
            case R.id.info1:
                displayInfoDialog("about_app");
                return true;
            case R.id.info2:
                displayInfoDialog("about_artfl");
                return true;
            case R.id.info3:
                //displayInfoDialog("faq");
                return true;
            case R.id.show_bookmarks:
                lookingAtInfo = false;
                return true;
            case R.id.bookmark_this:
                Log.i(TAG, " Can I add a bookmark? " + canAddBookmark);
                invalidateOptionsMenu();
                if (canAddBookmark){
                    if (thisIsABookmark){
                        Toast.makeText(this, "You are viewing a bookmarked page.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        invalidateOptionsMenu(); // pretty sure this updates the bookmarks menu automatically
                        bookMark();
                    }
                }
                else {
                    if (thisIsABookmark) {
                        Toast.makeText(this, "You are viewing a bookmarked page.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "This element cannot be bookmarked.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;

            default:
                // need this boolean to keep Alert from popping up on info menu click //
                if (!lookingAtInfo){
                    //selected_bookmark = String.valueOf(item.getItemId());
                    selected_bookmark = item.toString();
                    Log.i(TAG, " You clicked on this bookmark: " + selected_bookmark);
                    final String[] items = {getResources().getString(R.string.view_bookmark), getResources().getString(R.string.delete_bookmark)};

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(item.getTitle());
                    Log.i(TAG, " Item title: " + builder.toString());
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            if (items[item].equals(getResources().getString(R.string.view_bookmark)) ) {
                                //String[] values = {selected_bookmark};
                                Log.i(TAG, "Selected bookmark " + selected_bookmark);
                                String bookmark_uri = addBookmark.getBookmarkedText(selected_bookmark);

                                Bundle bundle = new Bundle();
                                bundle.putString("new_query_uri", bookmark_uri);

                                Fragment fr;
                                fr = new FullResultFragment();
                                fr.setArguments(bundle);
                                Log.i(TAG + "  onClick getting fragment", fr.toString());
                                FragmentManager fm = getSupportFragmentManager();
                                Log.i(TAG, " Fulltext click backstack count: " + fm.getBackStackEntryCount());
                                FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                                fragTransaction.replace(R.id.text, fr, "text");
                                fragTransaction.addToBackStack(null);
                                fragTransaction.commit();
                                thisIsABookmark = true;
                                }

                            else if (items[item].equals(getResources().getString(R.string.delete_bookmark)) ) {
                                Log.i(TAG, " Gonna delete us a bookmark: " + selected_bookmark);
                                addBookmark.deleteBookmark(selected_bookmark);
                                invalidateOptionsMenu();
                                Toast.makeText(getApplicationContext(), "Deleting " + selected_bookmark, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    AlertDialog alert = builder.create();
                    alert.show();
                    addBookmark.close();
                }
            return true;
        } // end switch logic
    }

    public void displayInfoDialog(String info_string){
        Log.i(TAG, "in displayInfoDialog: " + info_string);

        String file_name = info_string + ".html";
        Bundle bundle = new Bundle();
        bundle.putString("file_name", file_name);

        Fragment fr;
        fr = new InfoFragment();
        fr.setArguments(bundle);
        Log.i(TAG + "  onClick getting fragment", fr.toString());
        FragmentManager fm = getSupportFragmentManager();
        Log.i(TAG, " Info click backstack count: " + fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.commit();
        lookingAtInfo = false;
    }

    // Determine screen orientation //
    public int getScreenOrientation() {
    // from http://stackoverflow.com/questions/14955728/getting-orientation-of-android-device //

        Log.i(TAG + "  getScreenOrientation", "At work!");
        // Query what the orientation currently really is.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return 1; // Portrait Mode

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 2;   // Landscape mode
        }
        return 0;
    }

    public void makeMyQueryUri(String my_start_hit, String my_end_hit) {
        Context context = MainActivity.this;
        String my_report_value = "";
        String my_query_uri = "";
        String frequency_field = "";
        // 9-11-14:  gonna build all query uris by hand, now //
        // that we're calling get_frequency.py //

        Log.i(TAG, " Checking on this: " + query_search_type);
        if (search_et.getText().toString().isEmpty()) {
            /*if (!query_search_type.contains("concordance")){ // catch bad frequency searches
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.freq_search_error);
                dialog.getWindow().getAttributes().dimAmount = 0;
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                }
            else {*/ // straight biblio search
                Log.i(TAG, "No search term here -- bibliographic search");
                my_report_value = "bibliography";
                //String query_author = author_et.getText().toString();
                //query_author = query_author.trim();
                //query_author = query_author.replaceAll(" ", "+");
                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/dispatcher.py?" +
                    "report=bibliography&q=&method=proxy&title=" + query_title +
                    //"&author=" + query_author +
                    "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            //}
        } else {
            if (conc_from_freq != null){
                Log.i(TAG, " Conc from FREQ!");
                Log.i(TAG, " Search values: " + conc_author_from_freq + " " + conc_title_from_freq
                        + " " + conc_date_from_freq + " " + freq_search_term);
                my_report_value = "concordance";
                freq_search_term = freq_search_term.trim();
                freq_search_term = freq_search_term.replaceAll(" ", "+");
                conc_author_from_freq = conc_author_from_freq.trim();
                conc_author_from_freq = conc_author_from_freq.replaceAll(" ", "+");
                conc_title_from_freq = conc_title_from_freq.trim();
                conc_title_from_freq = conc_title_from_freq.replaceAll(" ", "+");
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/dispatcher.py?" +
                        "report=concordance&q=" + freq_search_term + "&method=proxy&title=" +
                        conc_title_from_freq + "&author=" + conc_author_from_freq + "&date=" + conc_date_from_freq +
                        "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            }
            else if (query_search_type.contains("concordance")) {
                Log.i(TAG, "get concordance");
                my_report_value = "concordance";
                String query_term = search_et.getText().toString();
                query_term = query_term.trim();
                query_term = query_term.replaceAll(" ", "+");
                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                //String query_author = author_et.getText().toString();
                //query_author = query_author.trim();
                //query_author = query_author.replaceAll(" ", "+");
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/dispatcher.py?" +
                        "report=concordance&q=" + query_term + "&method=proxy&title=" +
                         query_title +
                        //"&author=" + query_author +
                        "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            } else {
                frequency_field = query_search_type;
                Log.i(TAG, "get frequency");
                my_report_value = "frequency";
                String query_term = search_et.getText().toString();
                query_term = query_term.trim();
                query_term = query_term.replaceAll(" ", "+");
                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                //String query_author = author_et.getText().toString();
                //query_author = query_author.trim();
                //query_author = query_author.replaceAll(" ", "+");
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/scripts/get_frequency.py?" +
                        "report=concordance&q=" + query_term + "&method=proxy&title=" +
                        query_title +
                        //"&author=" + query_author +
                        "&frequency_field=" + frequency_field + "&format=json";
            }
        }

        Log.i(TAG + "  Search submit", "Executing search!");
        Log.i(TAG + "  Search Text", search_et.getText().toString());
        //Log.i(TAG + "  Author Text", author_et.getText().toString());
        Log.i(TAG + "  Title Text", title_et.getText().toString());
        Log.i(TAG + "  Search Report", my_report_value);

        Log.i(TAG, " Hand built URI: " + my_query_uri);

        /*
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(uri_authority)
                .appendPath(philo_dir).appendPath(build_name).appendPath("dispatcher.py")
                .appendQueryParameter("report", my_report_value)
                .appendQueryParameter("q", search_et.getText().toString())
                .appendQueryParameter("method", "proxy")
                .appendQueryParameter("title", title_et.getText().toString())
                .appendQueryParameter("author", author_et.getText().toString())
                .appendQueryParameter("start", my_start_hit)
                .appendQueryParameter("end", my_end_hit)
                .appendQueryParameter("pagenum", "25")
                .appendQueryParameter("format", "json");

        //Log.i(TAG, "Can we still get the goodies? " + builder.toString());

        //query_uri = builder.toString();
        */
        Bundle bundle = new Bundle();
        bundle.putString("query_uri", my_query_uri);

        //Log.i(TAG + "  on result click view is: ", v.toString());
        Fragment fr;
        if (my_report_value.contains("frequency")){
            fr = new FreqResultFragment();
            }
        else {
            fr = new ListResultFragment();
        }
        fr.setArguments(bundle);
        Log.i(TAG + "  onClick getting fragment", fr.toString());
        FragmentManager fm = getSupportFragmentManager();
        Log.i(TAG, " Listview click backstack count: "+ fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.commit();
        Log.i(TAG, " Listview post-commit click backstack count: "+ fm.getBackStackEntryCount());

    }

    public void bookMark() {
        addBookmark.open();
        Log.i(TAG, "In bookMark, gotta add something... like: " + bookmarkPhiloId + " " + bookmarkShrtCit);
        addBookmark.addBookmarkItem(bookmarkPhiloId, bookmarkShrtCit);
        addBookmark.close();
    }

    public void buildTOCFragment(String[] pid_toc_query_array){

        String pid_toc_address = "";
        String toc_query_uri = "";
        pid_toc_address = pid_toc_query_array[0].replaceFirst("\\[", "");
        Log.i(TAG, " Your philoID: " + pid_toc_address);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(uri_authority)
                .appendPath(philo_dir).appendPath(build_name)
                .appendPath("scripts").appendPath("get_table_of_contents.py")
                .appendQueryParameter("philo_id", pid_toc_address)
                .appendQueryParameter("format", "json");
        toc_query_uri = builder.toString();
        Log.i(TAG, " TOC query string: " + toc_query_uri);

        Bundle bundle = new Bundle();
        bundle.putString("new_query_uri", toc_query_uri);

        Fragment fr;
        fr = new TOCResultFragment();
        fr.setArguments(bundle);
        //Log.i(TAG + "  onClick getting fragment", fr.toString());
        FragmentManager fm = getSupportFragmentManager();
        //Log.i(TAG, " Fulltext click backstack count: " + fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.addToBackStack(null);
        fragTransaction.commit();
        Log.i(TAG, " Post-commit Fulltext click backstack count: " + fm.getBackStackEntryCount());
    }

    public void buildFullTextFragment(String[] build_query_array, String[] offsets){

        //Log.i(TAG, " I need to build fulltext queries sensibly: " + build_query_array);
        //Log.i(TAG, " Check your array length: " + build_query_array.length);

        Log.i(TAG, " in buildFullText.");
        String[] query_array = new String[9];
        if (build_query_array.length < 9){
            query_array[0] = build_query_array[0];
            query_array[1] = build_query_array[1];
            query_array[2] = build_query_array[2];
            query_array[3] = "0";
            query_array[4] = "0";
            query_array[5] = "0";
            query_array[6] = "0";
            query_array[7] = "0";
            query_array[8] = "";
            }
        else {
            query_array = build_query_array;
        }

        String byte_offsets = "";
        if (offsets != null && offsets.length>0){
            Log.i(TAG, " offsets content: " + offsets.toString());
            Log.i(TAG, " offsets length: " + offsets.length);
            for (int i = 0; i <  offsets.length; i++){
                byte_offsets = byte_offsets.concat("&byte=" + offsets[i].toString());
            }
        }
        //Log.i(TAG, " Check your array length again: " + query_array.length);
        String new_query_uri = "";

        /*
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(uri_authority)
                .appendPath(philo_dir).appendPath(build_name).appendPath("dispatcher.py")
                .appendPath(query_array[0]).appendPath(query_array[1])
                .appendPath(query_array[2])
                .appendQueryParameter("byte", query_array[8])
                .appendQueryParameter("format", "json");
        Log.i(TAG + "  New Query String: ", builder.toString());

        */

        new_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/dispatcher.py/" +
                query_array[0] + "/" + query_array[1] + "/" + query_array[2] +"?" + byte_offsets + "&format=json";
        Log.i(TAG, " Hand built URI: " + new_query_uri);

        Bundle bundle = new Bundle();
        // URI.BUILDER IS FUCKING UP MISERABLY!!!!! //
        bundle.putString("new_query_uri", new_query_uri);

        Fragment fr;
        fr = new FullResultFragment();
        fr.setArguments(bundle);
        //Log.i(TAG + "  onClick getting fragment", fr.toString());
        FragmentManager fm = getSupportFragmentManager();
        //Log.i(TAG, " Fulltext click backstack count: " + fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.addToBackStack(null);
        fragTransaction.commit();
        Log.i(TAG, " Post-commit Fulltext click backstack count: " + fm.getBackStackEntryCount());
    }

}
