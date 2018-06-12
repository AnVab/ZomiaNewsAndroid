package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import news.zomia.zomianews.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    OnSettingsChangedListener onSettingsChangedListenerCallback;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_prefs_fragment);
        toolbar.setBackground(getContext().getResources().getDrawable(R.drawable.action_bar_color));
        toolbar.setTitle(getString(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getActivity().onBackPressed(); }}
            );

        return rootView;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference connectionPref = findPreference(key);
        if(connectionPref != null)
            connectionPref.setSummary(sharedPreferences.getString(key, ""));

        onSettingsChangedListenerCallback.onSettingsUpdated(key);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onSettingsChangedListenerCallback = (OnSettingsChangedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSettingsChangedListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnSettingsChangedListener {
        public void onSettingsUpdated(String key);
    }
}
