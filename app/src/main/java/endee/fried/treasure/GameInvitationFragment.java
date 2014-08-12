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

    public static final String GAME_SEED = "GameSeed";
    public static final String DECLINE_INVITATION = "Declined";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final long seed = getArguments().getLong(GAME_SEED);
        final int playerNumber = getArguments().getInt(InviteeLounge.PLAYER_NUMBER_PRE);
        final ArrayList<String> invitedList = getArguments().getStringArrayList(InviteeLounge.INITIAL_INVITED_LIST_PRE);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.game_invitation)
                .setPositiveButton(R.string.accept_invitation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(getActivity(), InviteeLounge.class);
                        intent.putExtra(GameInvitationFragment.GAME_SEED, seed);
                        intent.putExtra(InviteeLounge.INITIAL_INVITED_LIST_PRE,invitedList );
                        intent.putExtra(InviteeLounge.PLAYER_NUMBER_PRE, playerNumber);
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
