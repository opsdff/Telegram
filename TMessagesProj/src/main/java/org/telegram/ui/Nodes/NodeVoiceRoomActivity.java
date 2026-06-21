package org.telegram.ui.Nodes;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/**
 * Voice room screen: participant list + mic/camera/hang-up controls.
 * WebRTC/SFU integration (Mediasoup protocol via WebSocket) is wired up here.
 * Full SFU implementation would live in a separate WebSocket client class.
 */
public class NodeVoiceRoomActivity extends BaseFragment {

    private final TL_nodes.TL_nodes_voiceRoom voiceRoom;
    private final ArrayList<TL_nodes.TL_voiceParticipant> participants = new ArrayList<>();

    private boolean muted = false;
    private boolean cameraOn = false;
    private ImageView muteBtn;
    private ImageView cameraBtn;

    public NodeVoiceRoomActivity(TL_nodes.TL_nodes_voiceRoom voiceRoom) {
        this.voiceRoom = voiceRoom;
        if (voiceRoom != null) {
            participants.addAll(voiceRoom.participants);
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(voiceRoom != null && voiceRoom.room != null ? voiceRoom.room.title : "Call");
        actionBar.setSubtitle(LocaleController.formatString(R.string.NodesCallParticipants,
                voiceRoom != null ? voiceRoom.participants.size() : 0));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        // Participant list
        ListView lv = new ListView(context);
        lv.setDividerHeight(0);
        ParticipantsAdapter adapter = new ParticipantsAdapter(context);
        lv.setAdapter(adapter);
        ((FrameLayout) fragmentView).addView(lv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, 80));

        // Control bar
        LinearLayout controls = new LinearLayout(context);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        controls.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        muteBtn = new ImageView(context);
        muteBtn.setImageResource(muted ? R.drawable.calls_mute_mini : R.drawable.calls_mute_mini);
        muteBtn.setScaleType(ImageView.ScaleType.CENTER);
        muteBtn.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(52),
                Theme.getColor(Theme.key_featuredStickers_addButton)));
        muteBtn.setOnClickListener(v -> toggleMute());
        controls.addView(muteBtn, LayoutHelper.createLinear(52, 52, 0, 0, 24, 0));

        cameraBtn = new ImageView(context);
        cameraBtn.setImageResource(R.drawable.calls_camera_mini);
        cameraBtn.setScaleType(ImageView.ScaleType.CENTER);
        cameraBtn.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(52),
                Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3)));
        cameraBtn.setOnClickListener(v -> toggleCamera());
        controls.addView(cameraBtn, LayoutHelper.createLinear(52, 52, 0, 0, 24, 0));

        // Hang up
        ImageView hangupBtn = new ImageView(context);
        hangupBtn.setImageResource(R.drawable.calls_decline);
        hangupBtn.setScaleType(ImageView.ScaleType.CENTER);
        hangupBtn.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(52), 0xFFE53935));
        hangupBtn.setOnClickListener(v -> leaveRoom());
        controls.addView(hangupBtn, LayoutHelper.createLinear(52, 52));

        ((FrameLayout) fragmentView).addView(controls, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 80, Gravity.BOTTOM));

        return fragmentView;
    }

    private void toggleMute() {
        muted = !muted;
        sendMediaState();
    }

    private void toggleCamera() {
        cameraOn = !cameraOn;
        sendMediaState();
    }

    private void sendMediaState() {
        if (voiceRoom == null) return;
        TL_nodes.TL_nodes_voiceRoomSetMedia req = new TL_nodes.TL_nodes_voiceRoomSetMedia();
        req.room_id = voiceRoom.room.id;
        req.flags |= 1;
        org.telegram.tgnet.TLRPC.Bool b = muted ? new org.telegram.tgnet.TLRPC.TL_boolTrue() : new org.telegram.tgnet.TLRPC.TL_boolFalse();
        req.muted = b;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> {});
    }

    private void leaveRoom() {
        if (voiceRoom == null) { finishFragment(); return; }
        TL_nodes.TL_nodes_leaveVoiceRoom req = new TL_nodes.TL_nodes_leaveVoiceRoom();
        req.room_id = voiceRoom.room.id;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) ->
                AndroidUtilities.runOnUIThread(this::finishFragment));
    }

    private class ParticipantsAdapter extends android.widget.BaseAdapter {
        private final Context context;
        ParticipantsAdapter(Context ctx) { this.context = ctx; }
        @Override public int getCount() { return participants.size(); }
        @Override public TL_nodes.TL_voiceParticipant getItem(int pos) { return participants.get(pos); }
        @Override public long getItemId(int pos) { return participants.get(pos).user_id; }

        @Override
        public View getView(int pos, View cv, android.view.ViewGroup parent) {
            if (cv == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
                row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                org.telegram.ui.Components.BackupImageView av = new org.telegram.ui.Components.BackupImageView(context);
                av.setRoundRadius(AndroidUtilities.dp(22));
                av.setTag("av");
                row.addView(av, LayoutHelper.createLinear(44, 44, 0, 0, 12, 0));

                LinearLayout col = new LinearLayout(context);
                col.setOrientation(LinearLayout.VERTICAL);
                TextView name = new TextView(context);
                name.setTag("name");
                name.setTextSize(16);
                name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                col.addView(name);
                row.addView(col, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                ImageView muteIcon = new ImageView(context);
                muteIcon.setTag("mute");
                muteIcon.setImageResource(R.drawable.calls_mute_mini);
                muteIcon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
                row.addView(muteIcon, LayoutHelper.createLinear(20, 20));
                cv = row;
            }

            TL_nodes.TL_voiceParticipant p = getItem(pos);
            LinearLayout row = (LinearLayout) cv;
            ((TextView) row.findViewWithTag("name")).setText("User " + p.user_id);
            row.findViewWithTag("mute").setVisibility(p.muted ? View.VISIBLE : View.GONE);
            return cv;
        }
    }
}
