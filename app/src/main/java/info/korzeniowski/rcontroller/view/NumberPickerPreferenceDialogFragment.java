package info.korzeniowski.rcontroller.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.NumberPicker;

import info.korzeniowski.rcontroller.R;

public class NumberPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    public static final String TAG = NumberPickerPreferenceDialogFragment.class.getSimpleName();

    // allowed range
    public static final int MIN_VALUE = -50;
    public static final int MAX_VALUE = 50;

    private NumberPicker numberPicker;
    private NumberPicker.OnValueChangeListener onValueChangedListener;

    public static final NumberPicker.Formatter FORMATTER = new NumberPicker.Formatter() {
        @Override
        public String format(int index) {
            if (index + MIN_VALUE > 0) {
                return "Right " + Integer.toString(index + MIN_VALUE);
            } else if (MAX_VALUE - index > 0) {
                return "Left " + Integer.toString(MAX_VALUE - index);
            } else {
                return Integer.toString(index + MIN_VALUE);
            }
        }
    };
    private int clickedButton;

    public static NumberPickerPreferenceDialogFragment newInstance(Preference preference) {
        NumberPickerPreferenceDialogFragment fragment = new NumberPickerPreferenceDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickedButton = which;
            }
        });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        numberPicker = getNumberPicker(view.getContext());
        addNumberPickerToView(numberPicker, view);
    }

    private NumberPicker getNumberPicker(Context context) {
        NumberPicker numberPicker = new NumberPicker(context);

        // Give it an ID so it can be saved/restored
        numberPicker.setId(android.R.id.edit);

        numberPicker.setFormatter(FORMATTER);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                onValueChangedListener.onValueChange(picker, oldVal + MIN_VALUE, newVal + MIN_VALUE);
            }
        });
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(MAX_VALUE - MIN_VALUE);
        numberPicker.setValue(getNumberPickerPreference().getValue() - MIN_VALUE);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setWrapSelectorWheel(false);

        return numberPicker;
    }

    private void addNumberPickerToView(NumberPicker numberPicker, View view) {
        ViewParent oldParent = numberPicker.getParent();
        if (oldParent == view)
            return;

        if (oldParent != null) {
            ((ViewGroup) oldParent).removeView(numberPicker);
        }

        ViewGroup container = (ViewGroup) view.findViewById(R.id.number_picker_container);
        if (container != null) {
            container.addView(numberPicker,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private NumberPickerPreference getNumberPickerPreference() {
        return (NumberPickerPreference) getPreference();
    }

    /**
     * @hide
     */
    @Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (clickedButton == DialogInterface.BUTTON_POSITIVE) {
            int value = numberPicker.getValue() + MIN_VALUE;
            if (getNumberPickerPreference().callChangeListener(value)) {
                getNumberPickerPreference().setValue(value);
            }
            getNumberPickerPreference().setSummary(FORMATTER.format(value - MIN_VALUE));
        }
    }

    public void setOnValueChangedListener(NumberPicker.OnValueChangeListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }
}
