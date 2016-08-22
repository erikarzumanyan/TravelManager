package com.uniquemiban.travelmanager.weather;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;

import github.vatsal.easyweather.Helper.ForecastCallback;
import github.vatsal.easyweather.Helper.TempUnitConverter;
import github.vatsal.easyweather.Helper.WeatherCallback;
import github.vatsal.easyweather.WeatherMap;
import github.vatsal.easyweather.retrofit.models.ForecastResponseModel;
import github.vatsal.easyweather.retrofit.models.Weather;
import github.vatsal.easyweather.retrofit.models.WeatherResponseModel;


public class WeatherFragment extends DialogFragment {

    public static final String ARG_PHOTO_URL = "arg_photo_url";
    private static final String ARG_LONGITUDE = "arg_long";
    private static final String ARG_LATITUDE = "arg_lat";
    private static final String OWM_API_KEY = "9a73fe1cc6cd3cc10dddc1cf44a8c7e0";


    TextView temperatureText;
    ImageView descriptionImage;
    ImageView sigthImage;
    TextView mTempMonday;
    TextView mTempTuesday;
    TextView mTempWednesday;
    TextView mTempThursday;
    TextView mTempFriday;
    TextView mTempSaturday;
    TextView mTempSunday;
    Double weather;
    WeatherMap weatherMap;
    Weather weatherList[];
    private ImageView mIconMonday;
    private ImageView mIconTuesday;
    private ImageView mIconWednesday;
    private ImageView mIconThursday;
    private ImageView mIconFriday;
    private ImageView mIconSaturday;
    private ImageView mIconSunday;
    private double latitude;
    private double longitude;
    private String backgroundImageUrl;

    public WeatherFragment() {
    }

    public static WeatherFragment newInstance(double pLatitude, double pLongitude, String pPhotoUrl) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, pLatitude);
        args.putDouble(ARG_LONGITUDE, pLongitude);
        args.putString(ARG_PHOTO_URL, pPhotoUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LATITUDE);
            longitude = getArguments().getDouble(ARG_LONGITUDE);
            backgroundImageUrl = getArguments().getString(ARG_PHOTO_URL);

            weatherMap = new WeatherMap(getActivity(), OWM_API_KEY);


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        temperatureText = (TextView) view.findViewById(R.id.temperatureText);
        descriptionImage = (ImageView) view.findViewById(R.id.descriptionLogo);
        sigthImage = (ImageView) view.findViewById(R.id.sigthImage);
        mTempMonday = (TextView) view.findViewById(R.id.tempMonday);
        mTempTuesday = (TextView) view.findViewById(R.id.tempTuesday);
        mTempWednesday = (TextView) view.findViewById(R.id.tempWednsday);
        mTempThursday = (TextView) view.findViewById(R.id.tempThursday);
        mTempFriday = (TextView) view.findViewById(R.id.tempFriday);
        mTempSaturday = (TextView) view.findViewById(R.id.tempSaturday);
        mTempSunday = (TextView) view.findViewById(R.id.tempSunday);
        mIconMonday = (ImageView) view.findViewById(R.id.icon_monday);
        mIconTuesday = (ImageView) view.findViewById(R.id.icon_thuesday);
        mIconWednesday = (ImageView) view.findViewById(R.id.icon_wednsday);
        mIconThursday = (ImageView) view.findViewById(R.id.icon_thursday);
        mIconFriday = (ImageView) view.findViewById(R.id.icon_friday);
        mIconSaturday = (ImageView) view.findViewById(R.id.icon_saturday);
        mIconSunday = (ImageView) view.findViewById(R.id.icon_sunday);
        Picasso.with(getActivity())
                .load(backgroundImageUrl)
                .into(sigthImage);


        weatherMap.getLocationForecast(String.valueOf(latitude), String.valueOf(longitude), new ForecastCallback() {
            @Override
            public void success(ForecastResponseModel response) {
                weather = TempUnitConverter.convertToCelsius(response.getList()[0].getMain().getTemp());
                mTempMonday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[1].getMain().getTemp());
                mTempTuesday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[2].getMain().getTemp());
                mTempWednesday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[3].getMain().getTemp());
                mTempThursday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[4].getMain().getTemp());
                mTempFriday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[5].getMain().getTemp());
                mTempSaturday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[6].getMain().getTemp());
                mTempSunday.setText(weather.intValue() + "°C");
                Weather w[];
                String iconLink;
                w = response.getList()[0].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconMonday);

                w = response.getList()[1].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconTuesday);

                w = response.getList()[2].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconWednesday);

                w = response.getList()[3].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconThursday);

                w = response.getList()[4].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconFriday);

                w = response.getList()[5].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconSaturday);

                w = response.getList()[6].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconSunday);

            }

            @Override
            public void failure(String message) {
                Log.d("eee", message);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        weatherMap.getLocationWeather(String.valueOf(latitude), String.valueOf(longitude), new WeatherCallback() {
            @Override
            public void success(WeatherResponseModel response) {
                weather = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
                temperatureText.setText(weather.intValue() + "°C");
                weatherList = response.getWeather();

                String iconLink = weatherList[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(descriptionImage);
            }

            @Override
            public void failure(String message) {
                Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
