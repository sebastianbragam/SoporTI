package com.saltapor.soporti;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DatePickerCategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatePickerCategoriesFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Variables to define which button created the fragment.
    static final int START_DATE = 1;
    static final int END_DATE = 2;
    private int mChosenDate;
    int cur = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DatePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatePickerCategoriesFragment newInstance(String param1, String param2) {
        DatePickerCategoriesFragment fragment = new DatePickerCategoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Code to differentiate which button created the fragment.
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            mChosenDate = bundle.getInt("DATE", 1);
        }

        switch (mChosenDate) {
            case START_DATE:
                cur = START_DATE;
                return new DatePickerDialog(getActivity(), this, year, month, day);
            case END_DATE:
                cur = END_DATE;
                return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        return null;

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ReportCategoryActivity activity = (ReportCategoryActivity) getActivity();
        if (cur == START_DATE)
        {
            activity.processDateFromPickerResult(year, month, day);
        } else {
            activity.processDateToPickerResult(year, month, day);
        }
    }

}