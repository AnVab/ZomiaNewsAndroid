package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.content.Context;
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

    private static final String TAG = "ZomiaLoginFragment";
    OnSuccessAuthorizationListener onSuccessAuthorizationCallback;

    private APIService apiService;
    private View rootView;
    private ProgressBar loadingProgressBar;

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

        apiService = ApiUtils.getAPIService();

        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loadingProgressBar);

        Button signInBtn = (Button) view.findViewById(R.id.enterButton);
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
        User user = new User();
        user.setEmail(login);
        user.setPassword(password);

        loadingProgressBar.setVisibility(View.VISIBLE);

        apiService.authenticateUser(user).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_LONG).show();
                            loadingProgressBar.setVisibility(View.INVISIBLE);
                            // Send the event to the host activity
                            onSuccessAuthorizationCallback.onSuccessAuthorization(response.body());
                            break;
                        default:
                            loadingProgressBar.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
                else
                {
                    //Connection problem
                    Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(getActivity(), getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Container Activity must implement this interface
    public interface OnSuccessAuthorizationListener {
        public void onSuccessAuthorization(Token token);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Activity activity = context instanceof Activity ? (Activity) context : null;
        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onSuccessAuthorizationCallback = (OnSuccessAuthorizationListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSuccessAuthorizationListener");
            }
        }
    }

}
