package endee.fried.treasure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by natasha on 2014-08-08.
 */
public class GameInvitationFragment extends DialogFragment {

    public static final String DECLINE_INVITATION = "Declined";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final String JSON = getArguments().getString(NewBluetoothLoungeActivity.JSON_KEY);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.game_invitation)
                .setPositiveButton(R.string.accept_invitation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(getActivity(), NewBluetoothLoungeActivity.class);
                        intent.putExtra(NewBluetoothLoungeActivity.JSON_KEY, JSON);
                        getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.decline_invitation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainMenuActivity)getActivity()).doAction(DECLINE_INVITATION);
                    }
                });
        return builder.create();
    }
}
