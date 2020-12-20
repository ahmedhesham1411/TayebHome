package com.uriallab.haat.haat;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.databinding.ObservableField;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    ObservableField<String> date;
    Activity context;
    public DatePickerFragment(Activity context, ObservableField<String> date){
        this.context = context;
        this.date = date;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(( (System.currentTimeMillis() - 1000 )  ));
        return datePickerDialog;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String monthTxt , dayTxt;
        if (month+1 < 10){
            monthTxt = "0"+ (month + 1);
            date.set(year + "-0" + (month + 1) + "-" + day);
        }else {
            monthTxt = String.valueOf(month+1);
        }
        if (day < 10){
            dayTxt = "0" + day;
        }else {
            dayTxt = String.valueOf(day);
        }

        date.set(year + "-" + monthTxt + "-" + dayTxt);
    }
}
