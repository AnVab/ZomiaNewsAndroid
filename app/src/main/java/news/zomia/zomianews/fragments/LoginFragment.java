package news.zomia.zomianews.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Token;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private static final String TAG = "ZomiaLoginFrame";
    private APIService mAPIService;
    public Token mToken;
    public User mUser;
    private View rootView;
    private ProgressBar loadingProgressBar;
    private Button signInBtn;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.layout_login, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mAPIService = ApiUtils.getAPIService();
        mUser = new User();
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loadingProgressBar);

        signInBtn = (Button) view.findViewById(R.id.enterButton);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onSignInButtonClicked(view);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onSignInButtonClicked(View view)
    {
        EditText emailText = (EditText)rootView.findViewById(R.id.emailText);
        EditText passwordText = (EditText)rootView.findViewById(R.id.passwordText);

        String layout_login = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(!TextUtils.isEmpty(layout_login) && !TextUtils.isEmpty(password)) {
            authorizePostRequest(layout_login, password);
        }
    }

    public void authorizePostRequest(String login, String password) {
        mUser.setEmail(login);
        mUser.setPassword(password);

        loadingProgressBar.setVisibility(View.VISIBLE);

        mAPIService.authenticateUser(mUser).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            mToken = response.body();
                            showResponse(mToken.toString());
                            break;
                        default:
                            loadingProgressBar.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
                else
                {
                    //Connection problem
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void showResponse(String response) {

        Toast.makeText(getActivity(), getResources().getString(R.string.success), Toast.LENGTH_LONG).show();
        loadingProgressBar.setVisibility(View.INVISIBLE);
    }
}
