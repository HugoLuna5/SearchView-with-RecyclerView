package org.lunainc.recyclerview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.thunder413.datetimeutils.DateTimeStyle;
import com.github.thunder413.datetimeutils.DateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.lunainc.recyclerview.data.DataSearch;
import org.lunainc.recyclerview.data.JSONResponse;
import org.lunainc.recyclerview.data.RequestInterface;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://www.bioeco.lunainc.org";
    private RecyclerView mRecyclerView;
    private ArrayList<DataSearch> mArrayList;
    private DataAdapter mAdapter;
    private LinearLayout cont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecyclerView = (RecyclerView) findViewById(R.id.card_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //setHasOptionsMenu(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<JSONResponse> call = request.getJSON();
        call.enqueue(new Callback<JSONResponse>() {
            @Override
            public void onResponse(Call<JSONResponse> call, Response<JSONResponse> response) {

                JSONResponse jsonResponse = response.body();
                mArrayList = new ArrayList<>(Arrays.asList(jsonResponse.getProyecto()));
                mAdapter = new DataAdapter(mArrayList);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<JSONResponse> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!compruebaConexion(this)) {
        }else {

            getMenuInflater().inflate(R.menu.menu_search, menu);

            MenuItem searchItem = menu.findItem(R.id.search);

            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            search(searchView);


        }
        return    super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {
        if(!compruebaConexion(this)) {
        }else {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (mAdapter == null){

                        return false;
                    }else {
                        mAdapter.getFilter().filter(newText);
                        return true;

                    }
                }
            });
        }
    }





    public static boolean compruebaConexion(Context context) {

        boolean connected = false;

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();

        for (int i = 0; i < redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }





    private class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> implements Filterable {
        private ArrayList<DataSearch> mArrayList;
        private ArrayList<DataSearch> mFilteredList;


        public DataAdapter(ArrayList<DataSearch> arrayList) {
            mArrayList = arrayList;
            mFilteredList = arrayList;

        }

        @Override
        public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, viewGroup, false);
            return new DataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {
            Time time = new Time();


            viewHolder.titleView.setText(mFilteredList.get(i).getTitle());

            long now = System.currentTimeMillis();


            /*DateUtils.getRelativeDateTimeString(_mActivity,
                    Long.parseLong(mFilteredList.get(i).getPublished_date()),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_YEAR)*/

            viewHolder.fecha.setText(DateTimeUtils.formatWithStyle(mFilteredList.get(i).getPublished_date(), DateTimeStyle.LONG));
            viewHolder.autor.setText(mFilteredList.get(i).getAuthor());
            viewHolder.description.setText(mFilteredList.get(i).getDescription());
            Glide.with(MainActivity.this).load(mFilteredList.get(i).getThumb()).asBitmap().into(viewHolder.thumbnailView);


        }


        @Override
        public int getItemCount() {
            return mFilteredList.size();
        }

        @Override
        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString();

                    if (charString.isEmpty()) {

                        mFilteredList = mArrayList;
                    } else {

                        ArrayList<DataSearch> filteredList = new ArrayList<>();

                        for (DataSearch dataSearch : mArrayList) {

                            if (dataSearch.getTitle().toLowerCase().contains(charString) || dataSearch.getAuthor().toLowerCase().contains(charString) || dataSearch.getDescription().toLowerCase().contains(charString)) {

                                filteredList.add(dataSearch);
                            }
                        }

                        mFilteredList = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mFilteredList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFilteredList = (ArrayList<DataSearch>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            public CardView cardView;
            public ImageView thumbnailView;
            public TextView titleView;
            public TextView fecha;
            public Button button;
            public TextView autor;
            public TextView description;
            public ViewHolder(final View view) {
                super(view);



                cardView = (CardView) view.findViewById(R.id.card_view);
                thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
                titleView = (TextView) view.findViewById(R.id.article_title);
                fecha = (TextView) view.findViewById(R.id.article_fecha);
                autor = (TextView) view.findViewById(R.id.autor);
                button = (Button) view.findViewById(R.id.button);
                description = (TextView) view.findViewById(R.id.descripcion_corta);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(itemView.getContext(), " " +mFilteredList.get(getAdapterPosition()).getTitle() , Toast.LENGTH_SHORT).show();
                       /* Intent intent = new Intent(itemView.getContext(),ActivityMostrarHome.class);
                        intent.putExtra("titulo",mFilteredList.get(getAdapterPosition()).getTitle());
                        intent.putExtra("url",mFilteredList.get(getAdapterPosition()).getUrl());
                        intent.putExtra("photo",mFilteredList.get(getAdapterPosition()).getPhoto());
                        intent.putExtra("autor",mFilteredList.get(getAdapterPosition()).getAuthor());
                        intent.putExtra("fecha",mFilteredList.get(getAdapterPosition()).getPublished_date());
                        intent.putExtra("body",mFilteredList.get(getAdapterPosition()).getBody());

                        Pair<View, String> elem1 = Pair.create((View) thumbnailView, getString(R.string.list_detail_image_transition));

                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(getActivity(), elem1);


                        itemView.getContext().startActivity(intent, options.toBundle());*/

                    }
                });

            }
        }

    }


}
