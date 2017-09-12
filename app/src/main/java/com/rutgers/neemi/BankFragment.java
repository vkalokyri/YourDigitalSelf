package com.rutgers.neemi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.PaymentCategory;
import com.rutgers.neemi.model.PaymentHasCategory;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Place;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Response;


public class BankFragment extends Fragment {

    View myView;

    ArrayList<String> accountNames = new ArrayList();
    ArrayList<String> accountlastNumbers = new ArrayList();
    ArrayList<String> accountIds = new ArrayList();
    DatabaseHelper helper;
    String selectedAccountID;
    ProgressDialog mProgress;
    String client_id = "596e6db04e95b810ac887f56";
    String secret = "d1ba7f8c06c7d70da0cef6e28b6b2f";

    PlaidClient plaidClient = PlaidClient.newBuilder()
            .clientIdAndSecret(client_id, secret)
            .publicKey("0ea8ed7c85e1c6d8aa4695cb156c97") // optional. only needed to call endpoints that require a public key
            .developmentBaseUrl() // or equivalent, depending on which environment you're calling into
            .build();


    Integer[] imgid={
            R.drawable.fb_logo,
            R.drawable.google_calendar,
            R.drawable.gmail_icon,
            R.drawable.bank,
            R.drawable.location
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_bank, container, false);

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        helper=new DatabaseHelper(getActivity());
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Getting your financial transactions. Please wait ...");

        ArrayList<String> itemname = getArguments().getStringArrayList("Accounts");
        for (String item :itemname) {
            String[] fullAccount = item.split("_@#_");
            accountNames.add(fullAccount[0]);
            accountlastNumbers.add(fullAccount[1]);
            accountIds.add(fullAccount[2]);
        }

        CustomListAdapter adapter=new CustomListAdapter(getActivity(), accountNames, accountlastNumbers, imgid);
        ListView list=(ListView) myView.findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                String SelectedItem= accountNames.get(+position).toString();
                selectedAccountID = accountIds.get(+position).toString();
                String filename = accountNames.get(+position)+"_@#_"+accountlastNumbers.get(+position)+"_@#_"+accountIds.get(+position);

                FileInputStream fis = null;
                String accessToken=null;
                try {

                    fis = getActivity().openFileInput(filename);
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr);
                    accessToken = br.readLine();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(accessToken!=null){
                    new AsyncGetTransactionsTask().execute(accessToken);
                }

//                 if (Selecteditem.equalsIgnoreCase("Facebook")) {
//                     Intent myIntent = new Intent(getActivity(), FacebookActivity.class);
//                     startActivity(myIntent);
//                 }
//                if (Selecteditem.equalsIgnoreCase("Gmail")){
//                    Intent myIntent = new Intent(getActivity(), GmailActivity.class);
//                    startActivity(myIntent);
//                }
//                if (Selecteditem.equalsIgnoreCase("Bank data")){
//
//                }

                Toast.makeText(getActivity(), SelectedItem, Toast.LENGTH_SHORT).show();

            }
        });


//        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Services, android.R.layout.simple_list_item_1);
//        setListAdapter(adapter);
//        getListView().setOnItemClickListener(this);
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {

//
//
////
//
//    }


    private class CustomListAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final ArrayList accountNames;
        private final Integer[] imgid;
        private final ArrayList accountDigits;

        public CustomListAdapter(Activity context, ArrayList accountNames, ArrayList<String> accountDigits, Integer[] imgid) {


            super(context, R.layout.bankview, accountNames);
            // TODO Auto-generated constructor stub

            this.context = context;
            this.accountNames = accountNames;
            this.imgid = imgid;
            this.accountDigits = accountDigits;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.bankview, null, true);

            TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

            txtTitle.setText(accountNames.get(position).toString());
            imageView.setImageResource(imgid[position]);
            extratxt.setText(accountDigits.get(position).toString());
            return rowView;

        }

        ;
    }


        private class AsyncGetTransactionsTask extends AsyncTask<String, Void, Integer> {

            final RuntimeExceptionDao<PaymentCategory, String> categoryDao = helper.getCategoryDao();
            final RuntimeExceptionDao<Payment, String> paymentDao = helper.getPaymentDao();
            final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
            final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
            final RuntimeExceptionDao<PaymentHasCategory, String> transactionHasCategoriesDao = helper.getTransactionCategoriesDao();

            int transactionsRetrieved = 0;

            @Override
            protected void onPreExecute() {
                mProgress.show();
            }


            @Override
            protected Integer doInBackground(String... params) {
                String accessToken = params[0];

                try {
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MONTH, -6);
                        Date startDate = null;
                        Date endDate = null;



                        try {

                            startDate = simpleDateFormat.parse(simpleDateFormat.format(cal.getTime()));
                            endDate = simpleDateFormat.parse(simpleDateFormat.format(Calendar.getInstance().getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        String timestamp = null;

                        GenericRawResults<String[]> rawResults = paymentDao.queryRaw("select max(timestamp) from Payment where account_id='"+selectedAccountID+"';");
                        List<String[]> results = null;
                        try {
                            results = rawResults.getResults();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (results!=null){
                            String[] resultArray = results.get(0);
                            System.out.println("timestamp= " + resultArray[0]);
                            timestamp=resultArray[0];
                        }

                        if (timestamp!=null) {
                            Calendar caltimestamp = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                            caltimestamp.setTimeInMillis(Long.parseLong(timestamp)*1000);
                            startDate = simpleDateFormat.parse(simpleDateFormat.format(caltimestamp.getTime()));
                        }



                        //startDate = simpleDateFormat.parse("2017-01-01");
                        //endDate = simpleDateFormat.parse("2017-02-01");
                        int totalTransactions=0;
                        do{
                            Response<TransactionsGetResponse> transactionsResponse = plaidClient.service().transactionsGet(
                                    new TransactionsGetRequest(
                                            accessToken,
                                            startDate,
                                            endDate)
                                            .withAccountIds(Arrays.asList(selectedAccountID))
                                            .withOffset(transactionsRetrieved)).execute();


                            if (transactionsResponse!=null) {
                                totalTransactions = transactionsResponse.body().getTotalTransactions();

                                for (TransactionsGetResponse.Transaction txn : transactionsResponse.body().getTransactions()) {
                                    Payment transaction = new Payment();

                                    if (txn.getAccountId() != null) {
                                        transaction.setAccount_id(txn.getAccountId());
                                    }
                                    if (txn.getAccountOwner() != null) {
                                        Person personExists = helper.personExistsByName(txn.getAccountOwner());
                                        if (personExists == null) {
                                            Person newPerson = new Person();
                                            newPerson.setName(txn.getAccountOwner());
                                            personDao.create(newPerson);
                                            transaction.setAccount_owner(newPerson);
                                        } else {
                                            transaction.setAccount_owner(personExists);
                                        }
                                    }
                                    if (txn.getAmount() != null) {
                                        transaction.setAmount(txn.getAmount());
                                    }
                                    if (txn.getDate() != null) {
                                        String date = txn.getDate();
                                        transaction.setDate(simpleDateFormat.parse(txn.getDate()).getTime());
                                    }
                                    if (txn.getName() != null) {
                                        transaction.setName(txn.getName());
                                    }
                                    if (txn.getOriginalDescription() != null) {
                                        transaction.setDescription(txn.getOriginalDescription());
                                    }
                                    if (txn.getPending() != null) {
                                        transaction.setPending(txn.getPending());
                                        System.out.println();
                                    }

                                    if (txn.getTransactionId() != null) {
                                        transaction.setId(txn.getTransactionId());
                                    }
                                    if (txn.getTransactionType() != null) {
                                        transaction.setTransaction_type(txn.getTransactionType());
                                    }
                                    if (txn.getAccountId() != null) {
                                        transaction.setAccount_id(txn.getAccountId());
                                    }
                                    if (txn.getPaymentMeta() != null) {
                                        TransactionsGetResponse.Transaction.PaymentMeta meta = txn.getPaymentMeta();
                                        if (meta.getPayee() != null) {
                                            Person personExists = helper.personExistsByName(meta.getPayee());
                                            if (personExists == null) {
                                                Person newPerson = new Person();
                                                newPerson.setName(meta.getPayee());
                                                personDao.create(newPerson);
                                                transaction.setPayee(newPerson);
                                            } else {
                                                transaction.setPayee(personExists);
                                            }
                                        }
                                        if (meta.getPayer() != null) {
                                            Person personExists = helper.personExistsByName(meta.getPayer());
                                            if (personExists == null) {
                                                Person newPerson = new Person();
                                                newPerson.setName(meta.getPayer());
                                                personDao.create(newPerson);
                                                transaction.setPayer(newPerson);
                                            } else {
                                                transaction.setPayer(personExists);
                                            }

                                        }
                                        if (meta.getPaymentMethod() != null) {
                                            transaction.setPayment_method(meta.getPaymentMethod());
                                        }
                                    }
                                    if (txn.getLocation() != null) {
                                        TransactionsGetResponse.Transaction.Location location = txn.getLocation();
                                        if (location.getLat() != null && location.getLon() != null) {
                                            Place placeExists = helper.placeExistsByLatLong(location.getLat(), location.getLon());
                                            if (placeExists == null) {
                                                Place newPlace = new Place();
                                                if (location.getAddress() != null) {
                                                    newPlace.setStreet(location.getAddress());
                                                }
                                                if (location.getCity() != null) {
                                                    newPlace.setCity(location.getCity());
                                                }
                                                if (location.getLat() != null) {
                                                    newPlace.setLatitude(location.getLat());
                                                }
                                                if (location.getLon() != null) {
                                                    newPlace.setLongitude(location.getLon());
                                                }
                                                if (location.getState() != null) {
                                                    newPlace.setState(location.getState());
                                                }
                                                if (location.getZip() != null) {
                                                    newPlace.setZip(location.getZip());
                                                }
                                                placeDao.create(newPlace);
                                                transaction.setPlace(newPlace);

                                            } else {
                                                transaction.setPlace(placeExists);
                                            }
                                        }
                                    }

                                    transaction.setTimestamp(System.currentTimeMillis() / 1000);
                                    paymentDao.create(transaction);

                                    if (txn.getCategory() != null) {
                                        List<PaymentCategory> categoryList = new ArrayList<>();
                                        for (String category : txn.getCategory()) {
                                            PaymentCategory categoryExists = categoryExists(category);
                                            if (categoryExists == null) {
                                                PaymentCategory newCategory = new PaymentCategory();
                                                newCategory.setCategoryName(category);
                                                categoryDao.create(newCategory);
                                                categoryList.add(newCategory);
                                            } else {
                                                PaymentHasCategory trans_categories = new PaymentHasCategory(transaction, categoryExists);
                                                transactionHasCategoriesDao.create(trans_categories);
                                            }
                                        }
                                        for (PaymentCategory eachCategory : categoryList) {
                                            PaymentHasCategory trans_categories = new PaymentHasCategory(transaction, eachCategory);
                                            transactionHasCategoriesDao.create(trans_categories);
                                        }


                                    }
                                    transactionsRetrieved++;
                                }
                            }
                        }while(transactionsRetrieved<totalTransactions);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return transactionsRetrieved;
            }

            protected void onPostExecute(Integer output) {
                mProgress.hide();
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                myIntent.putExtra("key", "bank");
                myIntent.putExtra("items", output);
                startActivity(myIntent);

            }

            public PaymentCategory categoryExists(String name) {

                RuntimeExceptionDao<PaymentCategory, String> categoryDao = helper.getCategoryDao();

                QueryBuilder<PaymentCategory, String> queryBuilder =
                        categoryDao.queryBuilder();
                Where<PaymentCategory, String> where = queryBuilder.where();
                try {
                    where.eq(PaymentCategory.CATEGORY, name);
                    List<PaymentCategory> results = queryBuilder.query();
                    if (results.size() != 0) {
                        return results.get(0);
                    } else
                        return null;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }






}

