package news.zomia.zomianews.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Token;
import news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedsListFragment extends Fragment {

    private APIService apiService;
    private View rootView;

    public FeedsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.layout_feeds_list, container, false);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiUtils.getAPIService();

    }

    public void LoadFeeds()
    {
        apiService.getFeedsList().enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call,Response<List<Feed>> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            Toast.makeText(getActivity(), getResources().getString(R.string.success), Toast.LENGTH_LONG).show();
                            // Send the event to the host activity

                            break;
                        default:

                            break;
                    }
                }
                else
                {
                    //Connection problem
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
            }
        });
    }
}
