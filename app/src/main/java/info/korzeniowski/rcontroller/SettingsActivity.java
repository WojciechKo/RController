package info.korzeniowski.rcontroller;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.NumberPicker;

import info.korzeniowski.rcontroller.communicator.CarCommunicator;
import info.korzeniowski.rcontroller.communicator.CarCommunicatorFactory;
import info.korzeniowski.rcontroller.view.NumberPickerPreference;
import info.korzeniowski.rcontroller.view.NumberPickerPreferenceDialogFragment;

public class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .add(CarCommunicatorFactory.getCommunicator(), CarCommunicator.TAG)
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

        private CarCommunicator carCommunicator;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            carCommunicator = (CarCommunicator) getActivity().getSupportFragmentManager().findFragmentByTag(CarCommunicator.TAG);
            carCommunicator.connect();
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof NumberPickerPreference) {
                NumberPickerPreferenceDialogFragment fragment = NumberPickerPreferenceDialogFragment.newInstance(preference);

                fragment.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        ControlData controlData = new ControlData(
                                ControlData.Direction.FORWARD,
                                0,
                                ControlData.Side.RIGHT,
                                0,
                                newVal);
                        carCommunicator.write(controlData);
                    }
                });

                fragment.setTargetFragment(SettingsFragment.this, 0);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
