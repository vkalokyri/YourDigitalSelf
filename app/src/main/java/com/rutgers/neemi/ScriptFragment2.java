package com.rutgers.neemi;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rutgers.neemi.interfaces.NLevelView;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.model.Transaction;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.zip.Inflater;

import static com.facebook.FacebookSdk.getApplicationContext;


public class ScriptFragment2 extends Fragment {

    List<NLevelItem> list;
    ListView listView;
    View view;
    Integer[] imgid={
            R.drawable.restaurant
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scriptview, container, false);


        ArrayList listOfProcesses = (ArrayList) getArguments().getSerializable("processes");
        int position = (int) getArguments().getSerializable("position");
        long id  = (long) getArguments().getSerializable("id");



        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);
        // TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);


        Script script = (Script)listOfProcesses.get(position);//.getScriptDefinition();
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

        Place place = null;

        for (Task task: script.getTasks()) {
            if (task.getPid() instanceof Transaction) {
                place = ((Transaction) task.getPid()).getPlace();
                break;
            } else if (task.getPid() instanceof Photo) {
                place = ((Photo) task.getPid()).getPlace();
                break;
            } else if (task.getPid() instanceof Feed) {
                place = ((Feed) task.getPid()).getPlace();
                break;
            }
        }

        if (place != null) {
            byte[] image = place.getImage();
            if (image != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 40, 40, false));
            }
        }else{
            imageView.setImageResource(imgid[0]);

        }

        //imageView.setImageResource(imgid[0]);
        ArrayList<ScriptLocalValues> localValues = script.getLocalValues();
        if (localValues != null) {
            for (ScriptLocalValues localValue : localValues) {
                if (localValue != null) {
                    LocalProperties lp = localValue.getLocalProperties();
                    if (lp != null) {
                        String w5h_value = lp.getW5h_value();
                        if (map.containsKey(w5h_value)) {
                            ArrayList<String> values = map.get(w5h_value);
                            values.add(localValue.getLocal_value());
                            map.put(w5h_value, values);
                        } else {
                            if (w5h_value != null) {
                                ArrayList<String> values = new ArrayList<String>();
                                values.add(localValue.getLocal_value());
                                map.put(w5h_value, values);
                            }
                        }
                    }
                }
            }
        }

        for (String localLabel:map.keySet()) {
            StringBuilder sb = new StringBuilder();
            for (String localValue : map.get(localLabel)) {
                sb.append(localValue);
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length() - 1);

            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(10, 5, 5, 0);
            textLayout.setLayoutParams(llp);

            TextView localTextView = new TextView(this.getContext());
            localTextView.setTextColor(Color.parseColor("#996666"));
            localTextView.setText(getString(R.string.local, localLabel + " : "));

            TextView localValueTextView = new TextView(this.getContext());
            localValueTextView.setTextColor(Color.parseColor("#FFFFFF"));
            localValueTextView.setText(getString(R.string.local, sb.toString()));

            textLayout.addView(localTextView);
            textLayout.addView(localValueTextView);
            linearLayout.addView(textLayout);

        }


        listView = (ListView) view.findViewById(R.id.lvExp);
        list = new ArrayList<NLevelItem>();

        for (final Script subscript: script.getSubscripts()) {

            final NLevelItem grandParent = new NLevelItem(subscript,null, new NLevelView() {

                @Override
                public View getView(NLevelItem item) {

                    return getScriptView(inflater, subscript); //makeAttendEatingOut
                }
            });
            list.add(grandParent);

            int numChildrenTask = subscript.getTasks().size();
            for (int k = 0; k < numChildrenTask; k++) {
                final Task task = subscript.getTasks().get(k);
                NLevelItem child = new NLevelItem(task,grandParent, new NLevelView() {

                    @Override
                    public View getView(NLevelItem item) {
                        return getTaskView(inflater,task); //PostOnFacebook
                    }
                });

                list.add(child);

            }


            int numChildren = subscript.getSubscripts().size();
            for (int j = 0; j < numChildren; j++) {
                final Script subsubscript = subscript.getSubscripts().get(j);
                NLevelItem parent = new NLevelItem(subsubscript,grandParent, new NLevelView() {

                    @Override
                    public View getView(NLevelItem item) {
                        return getScriptView(inflater, subsubscript); //makeAPayment
                    }
                });

                list.add(parent);

                numChildrenTask = subsubscript.getTasks().size();
                for (int k = 0; k < numChildrenTask; k++) {
                    final Task task = subsubscript.getTasks().get(k);
                    NLevelItem child = new NLevelItem(task,parent, new NLevelView() {

                        @Override
                        public View getView(NLevelItem item) {
                            return getTaskView(inflater,task); //payByCredit
                        }
                    });

                    list.add(child);

                }


            }


        }

        NLevelAdapter adapter = new NLevelAdapter(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ((NLevelAdapter)listView.getAdapter()).toggle(arg2);
                ((NLevelAdapter)listView.getAdapter()).getFilter().filter();

            }
        });

        return view;
    }


    public View getScriptView(LayoutInflater inflater, Script subscript) {

        StringBuilder localsSb = new StringBuilder();
        HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

        for (ScriptLocalValues lv : subscript.getLocalValues()){
            String key=lv.getLocalProperties().getW5h_value();
            if(map.containsKey(key)){
                map.get(key).add(lv.getLocal_value());
            }else{
                HashSet<String> valueSet = new HashSet<String>();
                valueSet.add(lv.getLocal_value());
                map.put(key,valueSet);
            }
        }

        for(String w5hValue: map.keySet()){
            localsSb.append(w5hValue);
            localsSb.append(": ");
            for (String value: map.get(w5hValue)) {
                localsSb.append(value);
                localsSb.append(",");
            }
            localsSb.deleteCharAt(localsSb.length()-1);
            localsSb.append("\n");
        }



        View view = inflater.inflate(R.layout.list_group, null);
        view.setBackgroundColor(Color.parseColor("#999999"));
        // TextView tv = (TextView) view.findViewById(R.id.item);

        TextView lblListHeader = (TextView) view.findViewById(R.id.lblListHeader);
        lblListHeader.setTextColor(Color.parseColor("#333333"));
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(subscript.getScriptDefinition().getName());

        TextView lblListHeaderLocals = (TextView) view.findViewById(R.id.lblListHeaderLocals);
        lblListHeaderLocals.setTextColor(Color.parseColor("#333333"));
        lblListHeaderLocals.setTypeface(null, Typeface.NORMAL);
        lblListHeaderLocals.setText(localsSb.toString());

        return view;

    }


    public View getTaskView(LayoutInflater inflater, Task childTask){

        View view = inflater.inflate(R.layout.list_item, null);
        view.setBackgroundColor(Color.parseColor("#999999"));

        TextView txtListChild = (TextView) view.findViewById(R.id.lblListItem);
        txtListChild.setText(childTask.getName());
        TextView txtListHeader = (TextView) view.findViewById(R.id.relItemHeader);
        TextView txtListHeaderBody = (TextView) view.findViewById(R.id.relItem);
        final ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        if(childTask.getPid() instanceof Email){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.gmail_icon));
            txtListHeader.setText(((Email) childTask.getPid()).getSubject());
            String text = "whoSent: "+((Email) childTask.getPid()).getFrom()+" \n whoReceived: "+ ((Email) childTask.getPid()).getTo()+" \n whenSent: "+ ((Email) childTask.getPid()).getDate();
            txtListHeaderBody.setText(text);
        }else if(childTask.getPid() instanceof Transaction){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.bank));
            txtListHeader.setText(((Transaction) childTask.getPid()).getMerchant_name());
            Date extractedDate = new Date(((Transaction) childTask.getPid()).getDate());
            Format format = new SimpleDateFormat("yyyy-MM-dd");
            String parsedDate = format.format(extractedDate);
            String text = "whenPaid: "+parsedDate+" \n howMuchWasPaid: $"+((Transaction) childTask.getPid()).getAmount() ;
            txtListHeaderBody.setText(text);
        }else if(childTask.getPid() instanceof Calendar){
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.google_calendar));
            txtListHeader.setText(((Event) childTask.getPid()).getTitle());
            txtListHeaderBody.setText(((Event) childTask.getPid()).getCreator().getName());
        }else if(childTask.getPid() instanceof Feed) {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fb_logo));
            txtListHeader.setText(((Feed) childTask.getPid()).getMessage());
            txtListHeaderBody.setText(((Feed) childTask.getPid()).getCreator().getName());
            Date extractedDate = new Date(((Feed) childTask.getPid()).getCreated_time());
            Format format = new SimpleDateFormat("yyyy-MM-dd");
            String parsedDate = format.format(extractedDate);
            StringBuilder sb = new StringBuilder();
            sb.append("whenWasPosted: " + parsedDate + "\n");
            sb.append("whereWasPosted: " + ((Feed) childTask.getPid()).getPlace().getName() + "\n" + "whatWasPosted: ");
            final SpannableString myString = new SpannableString(((Feed) childTask.getPid()).getLink());
            sb.append(myString);
            String allTheString = sb.toString();
            final int i1 = allTheString.indexOf(myString.toString());
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
            txtListHeaderBody.setText(allTheString, TextView.BufferType.SPANNABLE);

            Spannable mySpannable = (Spannable) txtListHeaderBody.getText();

            ClickableSpan myClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    WebView mWebview = new WebView(getApplicationContext());
                    mWebview.loadUrl(((TextView) view).getText().toString().substring(i1));
                    alert.setView(mWebview);
                    alert.create();
                    alert.show();


                }
            };
            mySpannable.setSpan(myClickableSpan, i1, i1 + myString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtListHeaderBody.setText(mySpannable);
            txtListHeaderBody.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return view;
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }

    class SomeObject {
        public String name;

        public SomeObject(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

}