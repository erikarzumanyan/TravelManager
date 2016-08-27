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

import java.util.Calendar;

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
    static int currentDay;
    TextView temperatureText;
    ImageView descriptionImage;
    ImageView sigthImage;
    TextView mTempTuesday;
    TextView mTempWednesday;
    TextView mTempThursday;
    TextView mTempFriday;
    Double weather;
    WeatherMap weatherMap;
    Weather weatherList[];
    private ImageView mIconMonday;
    private ImageView mIconTuesday;
    private ImageView mIconWednesday;
    private ImageView mIconThursday;
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
        final View view = inflater.inflate(R.layout.fragment_weather, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        temperatureText = (TextView) view.findViewById(R.id.temperatureText);
        descriptionImage = (ImageView) view.findViewById(R.id.descriptionLogo);
        sigthImage = (ImageView) view.findViewById(R.id.sigthImage);
        mTempTuesday = (TextView) view.findViewById(R.id.tempTuesday);
        mTempWednesday = (TextView) view.findViewById(R.id.tempWednsday);
        mTempThursday = (TextView) view.findViewById(R.id.tempThursday);
        mTempFriday = (TextView) view.findViewById(R.id.tempFriday);
        mIconMonday = (ImageView) view.findViewById(R.id.icon_monday);
        mIconTuesday = (ImageView) view.findViewById(R.id.icon_thuesday);
        mIconWednesday = (ImageView) view.findViewById(R.id.icon_wednsday);
        mIconThursday = (ImageView) view.findViewById(R.id.icon_thursday);
        Picasso.with(getActivity())
                .load(backgroundImageUrl)
                .into(sigthImage);

        //  Date date = new Date();

        //    calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        //    calendar.setTime(date);
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        //   Log.d("ttt", String.valueOf(currentDay));


        switch (currentDay) {
            case Calendar.MONDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Tuesday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Wednesday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Thursday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Friday");
                Log.d("ttt", String.valueOf(Calendar.MONDAY));
                break;
            }
            case Calendar.TUESDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Wednesday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Thursday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Friday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Saturday");
                Log.d("ttt", String.valueOf(Calendar.TUESDAY));
                break;
            }
            case Calendar.WEDNESDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Thursday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Friday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Saturday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Sunday");
                Log.d("ttt", String.valueOf(Calendar.WEDNESDAY));
                break;
            }
            case Calendar.THURSDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Friday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Saturday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Sunday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Monday");
                Log.d("ttt", String.valueOf(Calendar.THURSDAY));
                break;
            }
            case Calendar.FRIDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Saturday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Sunday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Monday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Tuesday");
                Log.d("ttt", String.valueOf(Calendar.FRIDAY));
                break;
            }
            case Calendar.SATURDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Sunday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Monday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Tuesday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Wednesday");
                Log.d("ttt", String.valueOf(Calendar.SATURDAY));
                break;
            }
            case Calendar.SUNDAY: {
                ((TextView) view.findViewById(R.id.text_view_day_1)).setText("Monday");
                ((TextView) view.findViewById(R.id.text_view_day_2)).setText("Tuesday");
                ((TextView) view.findViewById(R.id.text_view_day_3)).setText("Wednesday");
                ((TextView) view.findViewById(R.id.text_view_day_4)).setText("Thursday");
                Log.d("ttt", String.valueOf(Calendar.SUNDAY));
                break;
            }

            default:
                Log.d("ttt", "NO");

        }
        weatherMap.getLocationForecast(String.valueOf(latitude), String.valueOf(longitude), new ForecastCallback() {
            @Override
            public void success(ForecastResponseModel response) {

//                weather = TempUnitConverter.convertToCelsius(response.getList()[0].getMain().getTemp());
//                mTempMonday.setText(weather.intValue() + "°C");

                //Log.d("qqq", response.getList());

                weather = TempUnitConverter.convertToCelsius(response.getList()[8].getMain().getTemp_max());
                mTempTuesday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[16].getMain().getTemp_max());
                mTempWednesday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[24].getMain().getTemp_max());
                mTempThursday.setText(weather.intValue() + "°C");
                weather = TempUnitConverter.convertToCelsius(response.getList()[32].getMain().getTemp_max());
                mTempFriday.setText(weather.intValue() + "°C");
                Weather w[];

                w = response.getList()[8].getWeather();
                String iconLink;
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconMonday);

                w = response.getList()[16].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconTuesday);

                w = response.getList()[24].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconWednesday);

                w = response.getList()[32].getWeather();
                iconLink = w[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(mIconThursday);
//

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
                weather = TempUnitConverter.convertToCelsius(response.getMain().getTemp_max());
                temperatureText.setText(weather.intValue() + "°C");
                weatherList = response.getWeather();
                String iconLink = weatherList[0].getIconLink();
                Picasso.with(getActivity()).load(iconLink).into(descriptionImage);
            }

            @Override
            public void failure(String message) {
                Snackbar.make(getView().getRootView(), "Connection Error", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
