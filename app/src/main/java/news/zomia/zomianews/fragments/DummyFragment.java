package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import news.zomia.zomianews.R;

public class DummyFragment  extends Fragment {

    OnDummyFragmentListener onMenuListListenerCallback;

    public DummyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_dummy_landscape, container, false);

        Bundle arguments = getArguments();
        boolean showArrow = arguments.getBoolean("showArrow", false);
        boolean showBurger = arguments.getBoolean("showBurger", false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_dummy_fragment_container);
        toolbar.setBackground(getContext().getResources().getDrawable(R.drawable.action_bar_color));
        if(showArrow) {
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        //Add menu for the toolbar
        toolbar.inflateMenu(R.menu.stories_list_action_menu);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        if(!showBurger){
            toolbar.getMenu().findItem(R.id.action_settings).setVisible(false);
            toolbar.getMenu().findItem(R.id.logout).setVisible(false);
        }

        ImageView dummyLogoImageView = (ImageView) view.findViewById(R.id.dummyLogoImageView);

        //Set logo to grayscale colors
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);  //0 - grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(colorMatrix);
        dummyLogoImageView.setColorFilter(cf);
        dummyLogoImageView.setImageAlpha(128);
        return view;
    }

    Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()){

                case android.R.id.home:
                    getActivity().onBackPressed();
                    return true;

                case R.id.action_settings:
                    onMenuListListenerCallback.onSettings();
                    return true;

                case R.id.logout:
                    onMenuListListenerCallback.onLogOut();
                    return true;
            }
            return true;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onMenuListListenerCallback = (OnDummyFragmentListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnDummyFragmentListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnDummyFragmentListener {
        public void onSettings();
        public void onLogOut();
    }
}
