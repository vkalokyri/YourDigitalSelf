package com.rutgers.neemi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.LatLng;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlaceAutocompleteType;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.TransactionsGetResponse;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.TransactionHasPlaces;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    String frequency;

    PlaidClient plaidClient = PlaidClient.newBuilder()
            .clientIdAndSecret(client_id, secret)
            .publicKey("0ea8ed7c85e1c6d8aa4695cb156c97") // optional. only needed to call endpoints that require a public key
            .developmentBaseUrl() // or equivalent, depending on which environment you're calling into
            //.sandboxBaseUrl()
            .build();

    GeoApiContext context = new GeoApiContext.Builder()
            .apiKey("AIzaSyCwsSXnT_jN107mMLhz55vo7JKwREaflJQ")
            .build();

    FusedLocationProviderClient fusedLocationClient;
    Location myLocation;


    Integer[] imgid = {
            R.drawable.bank,
            R.drawable.bank,
            R.drawable.bank,
            R.drawable.bank,
            R.drawable.bank
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_bank, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        frequency = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("sync_frequency", "");

        helper=DatabaseHelper.getHelper(getActivity()); 
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

        Button addBank = (Button) myView.findViewById(R.id.addAnotherBankBtn);

        addBank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                PlaidFragment plaidFragment = new PlaidFragment();
                android.support.v4.app.FragmentTransaction setfragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                setfragmentTransaction.replace(R.id.frame_container,plaidFragment);
                setfragmentTransaction.addToBackStack(null);
                setfragmentTransaction.commit();
//                Intent myIntent = new Intent(getActivity(), BankFragment.class);
//                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(myIntent);
            }
        });

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

                Toast.makeText(getActivity(), SelectedItem, Toast.LENGTH_SHORT).show();

            }
        });

    }

    public Calendar getCalendarDate(String period){
        Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());

        if (period.equals("7")){
            cal.add(Calendar.DATE, -7);
        }else if(period.equals("30")){
            cal.add(Calendar.MONTH, -1);
        }else if(period.equals("180")){
            cal.add(Calendar.MONTH, -6);
        }else if(period.equals("365")){
            cal.add(Calendar.MONTH, -12);
        }else if(period.equals("1")){
            cal.add(Calendar.DATE, -1);
        }

        return  cal;

    }





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

    }


        private class AsyncGetTransactionsTask extends AsyncTask<String, Void, Integer> {

            final RuntimeExceptionDao<Category, String> categoryDao = helper.getCategoryDao();
            final RuntimeExceptionDao<Transaction, String> transactionDao = helper.getTransactionDao();
            final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
            final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
            final RuntimeExceptionDao<TransactionHasCategory, String> transactionHasCategoriesDao = helper.getTransactionHasCategoryRuntimeDao();

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

                        //Calendar cal = Calendar.getInstance();
                        Calendar cal = getCalendarDate(frequency);
                        //cal.add(Calendar.MONTH, -7);
                        Date startDate = null;
                        Date endDate = null;



                        try {

                            startDate = simpleDateFormat.parse(simpleDateFormat.format(cal.getTime()));
                            endDate = simpleDateFormat.parse(simpleDateFormat.format(Calendar.getInstance().getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        String timestamp = null;

                        GenericRawResults<String[]> rawResults = transactionDao.queryRaw("select max(timestamp) from `Transaction` where account_id='"+selectedAccountID+"';");
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
                                    Transaction transaction = new Transaction();

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
                                        transaction.setDate(simpleDateFormat.parse(txn.getDate()).getTime());
                                    }
                                    if (txn.getName() != null) {
                                        transaction.setMerchant_name(txn.getName());
                                    }
                                    if (txn.getOriginalDescription() != null) {
                                        transaction.setDescription(txn.getOriginalDescription());
                                    }
                                    if (txn.getPending() != null) {
                                        transaction.setPending(txn.getPending());
                                    }

                                    if (txn.getTransactionId() != null) {
                                        transaction.setId(txn.getTransactionId());
                                    }
                                    if (txn.getTransactionType() != null) {
                                        transaction.setTransaction_type(txn.getTransactionType());
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
                                    ArrayList<Place> listOfPlaces = new ArrayList();
                                    if (txn.getLocation() != null) {
                                        TransactionsGetResponse.Transaction.Location location = txn.getLocation();
                                        if (location.getLat() != null && location.getLon() != null) {
                                            Place placeExists = helper.placeExistsByLatLong(location.getLat(), location.getLon());
                                            if (placeExists == null) {
                                                LatLng gmapslatLong = new LatLng(location.getLat(),location.getLon());
                                                try {
                                                    if (transaction.getMerchant_name()!=null) {
                                                        PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(context, gmapslatLong)
                                                                .radius(100)
                                                                .keyword(transaction.getMerchant_name())
                                                                .name(transaction.getMerchant_name())
                                                                .await();
                                                        if (gmapsResponse.results != null) {
                                                            for  (PlacesSearchResult place: gmapsResponse.results) {
                                                                placeExists= new Place();
                                                                placeExists.setName(place.name);
                                                                placeExists.setStreet(place.formattedAddress);
                                                                placeExists.setId(place.placeId);
                                                                placeExists.setLatitude(place.geometry.location.lat);
                                                                placeExists.setLongitude(place.geometry.location.lng);
                                                                if (place.photos != null) {
                                                                    PhotoResult photoResult = PlacesApi.photo(context, place.photos[0].photoReference).maxWidth(400)
                                                                            .await();
                                                                    byte[] image = photoResult.imageData;
                                                                    placeExists.setImage(image);
                                                                }
                                                                placeDao.create(placeExists);
                                                                for (String placeCategory : place.types) {
                                                                    Category categoryExists = helper.placeCategoryExists(placeCategory);
                                                                    if (categoryExists == null) {
                                                                        Category newCategory = new Category();
                                                                        newCategory.setCategoryName(placeCategory);
                                                                        helper.getCategoryDao().create(newCategory);
                                                                        PlaceHasCategory placeHasCategories = new PlaceHasCategory(placeExists, newCategory);
                                                                        helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                                                    } else {
                                                                        PlaceHasCategory trans_categories = new PlaceHasCategory(placeExists, categoryExists);
                                                                        helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                                                    }
                                                                }
                                                                listOfPlaces.add(placeExists);

                                                            }
                                                        }
                                                    }
                                                } catch (ApiException e) {
                                                    placeDao.create(placeExists);
                                                    e.printStackTrace();
                                                } catch (InterruptedException e) {
                                                    placeDao.create(placeExists);
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    placeDao.create(placeExists);
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                listOfPlaces.add(placeExists);
                                            }

                                        }else {
                                            StringBuilder locationString = new StringBuilder();


                                            if (location.getAddress() != null) {
                                                locationString.append(location.getAddress());
                                            }
                                            if (location.getCity() != null) {
                                                locationString.append(" ");
                                                locationString.append(location.getCity());
                                            }
                                            if (location.getState() != null) {
                                                locationString.append(" ");
                                                locationString.append(location.getState());
                                            }
                                            if (location.getZip() != null) {
                                                locationString.append(" ");
                                                locationString.append(location.getZip());
                                            }

                                            PlacesSearchResponse gmapsResponse = PlacesApi.textSearchQuery(context, transaction.getMerchant_name()+" "+locationString.toString()).await();
                                            listOfPlaces.addAll(findTransactionPlaces(gmapsResponse));


                                        }
                                    }

                                    transaction.setTimestamp(System.currentTimeMillis() / 1000);
                                    transactionDao.create(transaction);

                                    for (Place p:listOfPlaces){
                                        TransactionHasPlaces transactionHasPlaces = new TransactionHasPlaces(transaction,p);
                                        helper.getTransactionHasPlacesDao().create(transactionHasPlaces);

                                    }

                                    if (txn.getCategory() != null) {
                                        List<Category> categoryList = new ArrayList<>();
                                        for (String category : txn.getCategory()) {
                                            Category categoryExists = categoryExists(category);
                                            if (categoryExists == null) {
                                                Category newCategory = new Category();
                                                newCategory.setCategoryName(category);
                                                categoryDao.create(newCategory);
                                                categoryList.add(newCategory);
                                            } else {
                                                TransactionHasCategory trans_categories = new TransactionHasCategory(transaction, categoryExists);
                                                transactionHasCategoriesDao.create(trans_categories);
                                            }
                                        }
                                        for (Category eachCategory : categoryList) {
                                            TransactionHasCategory trans_categories = new TransactionHasCategory(transaction, eachCategory);
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
                mProgress.dismiss();
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                myIntent.putExtra("key", "bank");
                myIntent.putExtra("items", output);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);

            }


            private ArrayList<Place> findTransactionPlaces(PlacesSearchResponse gmapsResponse) throws InterruptedException, ApiException, IOException {
                ArrayList<Place> listOfPlaces = new ArrayList();
                if (gmapsResponse.results != null) {
                    for  (PlacesSearchResult place: gmapsResponse.results) {
                        Place placeExists = helper.placeExistsById(place.placeId);
                        if (placeExists == null) {
                            placeExists = new Place();
                            placeExists.setName(place.name);
                            placeExists.setStreet(place.formattedAddress);
                            placeExists.setId(place.placeId);
                            placeExists.setLatitude(place.geometry.location.lat);
                            placeExists.setLongitude(place.geometry.location.lng);
                            if (place.photos != null) {
                                PhotoResult photoResult = PlacesApi.photo(context, place.photos[0].photoReference).maxWidth(400)
                                        .await();
                                byte[] image = photoResult.imageData;
                                placeExists.setImage(image);
                            }
                            placeDao.create(placeExists);
                            for (String placeCategory : place.types) {
                                Category categoryExists = helper.placeCategoryExists(placeCategory);
                                if (categoryExists == null) {
                                    Category newCategory = new Category();
                                    newCategory.setCategoryName(placeCategory);
                                    helper.getCategoryDao().create(newCategory);
                                    PlaceHasCategory placeHasCategories = new PlaceHasCategory(placeExists, newCategory);
                                    helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                } else {
                                    PlaceHasCategory trans_categories = new PlaceHasCategory(placeExists, categoryExists);
                                    helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                }
                            }
                            listOfPlaces.add(placeExists);
                        }else{
                            listOfPlaces.add(placeExists);
                        }
                    }
                }
                return listOfPlaces;
            }

            public Category categoryExists(String name) {

                RuntimeExceptionDao<Category, String> categoryDao = helper.getCategoryDao();

                QueryBuilder<Category, String> queryBuilder =
                        categoryDao.queryBuilder();
                Where<Category, String> where = queryBuilder.where();
                try {
                    where.eq(Category.CATEGORY, name);
                    List<Category> results = queryBuilder.query();
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

