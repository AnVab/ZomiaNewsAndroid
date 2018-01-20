package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import javax.inject.Inject;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Token;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.service.ApiUtils;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import news.zomia.zomianews.di.Injectable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements
        LifecycleRegistryOwner,
        Injectable {

    private static final String TAG = "ZomiaLoginFragment";
    OnSuccessAuthorizationListener onSuccessAuthorizationCallback;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    private View rootView;
    private ProgressBar loadingProgressBar;
    @Inject
    SharedPreferences sharedPref;
    @Inject
    DataRepository dataRepo;

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
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
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

        dataRepo.getZomiaService().authenticateUser(user).enqueue(new Callback<Token>() {
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

                            //Save token
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.preferences_token), response.body().getToken());
                            editor.commit();

                            // Send the event to the host activity
                            onSuccessAuthorizationCallback.onSuccessAuthorization();
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
        public void onSuccessAuthorization();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
