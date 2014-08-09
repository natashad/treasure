package endee.fried.treasure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by natasha on 2014-08-08.
 */
public class GameInvitationFragment extends DialogFragment {

    public static final String GAME_SEED = "GameSeed";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final long seed = getArguments().getLong(GAME_SEED);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.game_invitation)
                .setPositiveButton(R.string.accept_invitation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getActivity(), GameActivity.class);
                        intent.putExtra(GameInvitationFragment.GAME_SEED, seed);
                        getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.decline_invitation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }
}
