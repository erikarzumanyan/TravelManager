package com.uniquemiban.travelmanager.weather;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.HistoricalHourWeather;
import com.survivingwithandroid.weather.lib.model.HistoricalWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.uniquemiban.travelmanager.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class WeatherFragment extends DialogFragment {

    private static final String ARG_LONGITUDE = "arg_long";
    private static final String ARG_LATITUDE = "arg_lat";
    public static final String ARG_PHOTO_URL = "arg_photo_url";

    TextView temperatureText;
    TextView descriptionText;
    ImageView descriptionImage;
    ImageView sigthImage;
    private WeatherClient client;

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


            WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
            WeatherConfig config = new WeatherConfig();
            config.ApiKey = getResources().getString(R.string.forecastio_key);

            try {
                client = builder.attach(getActivity())
                        .provider(new OpenweathermapProviderType())
                        .httpClient(com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault.class)
                        .config(config)
                        .build();
            } catch (WeatherProviderInstantiationException pE) {
                pE.printStackTrace();
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        temperatureText = (TextView) view.findViewById(R.id.temperatureText);
        descriptionText = (TextView) view.findViewById(R.id.descriptionText);
        descriptionImage = (ImageView) view.findViewById(R.id.descriptionLogo);
        sigthImage = (ImageView) view.findViewById(R.id.sigthImage);

        Picasso.with(getActivity())
                .load(backgroundImageUrl)
                .into(sigthImage);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Calendar c1 = Calendar.getInstance();
        c1.clear();
        //c1.set(2014,Calendar.JANUARY,1,0,0,0);
        c1.set(2014, Calendar.JULY, 6, 0, 0, 0);

        Date d1 = c1.getTime();

        Calendar c2 = Calendar.getInstance();
        c2.clear();
        // c2.set(2014,Calendar.JANUARY, 2,0,0,0);
        c2.set(2014, Calendar.JULY, 7, 0, 0, 0);
        Date d2 = c2.getTime();

        client.getCurrentCondition(new WeatherRequest(longitude, latitude), new WeatherClient.WeatherEventListener() {
            @Override
            public void onWeatherRetrieved(final CurrentWeather weather) {


                temperatureText.setText("" + (int)weather.weather.temperature.getTemp() + "°");
                descriptionText.setText("" + weather.weather.currentCondition.getDescr());

                if (weather.weather.currentCondition.getDescr().contains("sun")) {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                }
                if (String.valueOf(weather.weather.currentCondition.getDescr()).contains("rain")) {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                }
                if (String.valueOf(weather.weather.currentCondition.getDescr()).contains("snow")) {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                }
                if (String.valueOf(weather.weather.currentCondition.getDescr()).contains("thunder")) {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                }
                if (String.valueOf(weather.weather.currentCondition.getDescr()).contains("cloud")) {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                } else {
                    descriptionImage.setImageResource(R.mipmap.cloudy);
                }

            }


            @Override
            public void onWeatherError(WeatherLibException wle) {
                wle.printStackTrace();
            }

            @Override
            public void onConnectionError(Throwable t) {

            }
        });


        client.getHistoricalWeather(new WeatherRequest(40.1833, 44.5167), d1, d2, new WeatherClient.HistoricalWeatherEventListener() {
            @Override
            public void onWeatherRetrieved(HistoricalWeather histWeather) {
                List<HistoricalHourWeather> historicalWeatherList = histWeather.getHoistoricalData();
                Log.d("Hist", "Data [" + historicalWeatherList + "] - Size [" + historicalWeatherList.size() + "]");
//                descriptionText.setText(historicalWeatherList + "");
            }

            @Override
            public void onWeatherError(WeatherLibException wle) {

            }

            @Override
            public void onConnectionError(Throwable t) {

            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
