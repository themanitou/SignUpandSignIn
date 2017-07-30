package ca.skillsup.androidapp.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.NumberPicker;

import java.util.Arrays;

import ca.skillsup.androidapp.R;

/**
 * Created by manitou on 12/8/16.
 */

public class NumberPickerFragment extends DialogFragment
        implements NumberPicker.OnValueChangeListener {

    public callBackListener mCallback;

    public interface callBackListener {
        void onNumberPicked(int number);
    }

    public static NumberPickerFragment newInstance(String title, int[] allNumbers, int selectPosition) {
        NumberPickerFragment numberPickerFragment = new NumberPickerFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putIntArray("allNumbers", allNumbers);
        args.putInt("selectPosition", selectPosition);
        numberPickerFragment.setArguments(args);
        return numberPickerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (callBackListener) getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog((Activity) mCallback);
        String title = getArguments().getString("title");
        int[] allNumbers = getArguments().getIntArray("allNumbers");
        String[] strAllNumbers = Arrays.toString(allNumbers).split("[\\[\\]]")[1].split(", ");
        int selectPosition = getArguments().getInt("selectPosition");

        dialog.setTitle(title);
        dialog.setContentView(R.layout.dialog_number_picker);
        NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker);
        numberPicker.setDisplayedValues(strAllNumbers);
        numberPicker.setWrapSelectorWheel(true);
        if (allNumbers != null) {
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(allNumbers.length);
        }
        numberPicker.setValue(selectPosition);
        numberPicker.setOnValueChangedListener(this);

        return dialog;
    }

    public void onValueChange(NumberPicker view, int oldValue, int newValue) {
        mCallback.onNumberPicked(newValue);
    }

}
