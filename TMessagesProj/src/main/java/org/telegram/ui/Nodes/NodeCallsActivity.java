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
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/**
 * Calls tab: active voice rooms + recent calls.
 * Design: Calls & Video Chats/Main.png
 */
public class NodeCallsActivity extends BaseFragment {

    private final long nodeId;
    private final ArrayList<TL_nodes.TL_voiceRoom> activeRooms = new ArrayList<>();
    private ActiveRoomsAdapter adapter;

    public NodeCallsActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesCallsTitle));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 2) showMoreMenu();
            }
        });
        actionBar.createMenu().addItem(2, R.drawable.ic_ab_other);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        // "Start New Call" row
        LinearLayout startRow = new LinearLayout(context);
        startRow.setOrientation(LinearLayout.HORIZONTAL);
        startRow.setGravity(Gravity.CENTER_VERTICAL);
        startRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        startRow.setClickable(true);
        startRow.setBackground(Theme.getSelectorDrawable(false));
        startRow.setOnClickListener(v -> createVoiceRoom());

        ImageView callIcon = new ImageView(context);
        callIcon.setImageResource(R.drawable.msg_calls);
        callIcon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
        startRow.addView(callIcon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView startText = new TextView(context);
        startText.setText(LocaleController.getString(R.string.NodesStartNewCall));
        startText.setTextSize(16);
        startText.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        startRow.addView(startText);
        root.addView(startRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Hint
        TextView hint = new TextView(context);
        hint.setText("You can add up to 200 participants to a call.");
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        hint.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(hint, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Section: Active calls
        TextView activeSection = new TextView(context);
        activeSection.setText(LocaleController.getString(R.string.NodesActiveCallsSection));
        activeSection.setTextSize(13);
        activeSection.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        activeSection.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(activeSection, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ListView lv = new ListView(context);
        lv.setDividerHeight(0);
        adapter = new ActiveRoomsAdapter(context);
        lv.setAdapter(adapter);
        root.addView(lv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        ((FrameLayout) fragmentView).addView(root, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        loadVoiceRooms();
        return fragmentView;
    }

    private void loadVoiceRooms() {
        TL_nodes.TL_nodes_getVoiceRooms req = new TL_nodes.TL_nodes_getVoiceRooms();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_voiceRooms) {
                activeRooms.clear();
                activeRooms.addAll(((TL_nodes.TL_nodes_voiceRooms) res).rooms);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        }));
    }

    private void createVoiceRoom() {
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_createVoiceRoom req = new TL_nodes.TL_nodes_createVoiceRoom();
        req.node_id = nodeId;
        req.visible_all = true;
        req.flags |= 16;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_voiceRoom) {
                // Connect to voice room
                TL_nodes.TL_nodes_voiceRoom vr = (TL_nodes.TL_nodes_voiceRoom) res;
                openVoiceRoom(vr);
            }
        }));
    }

    private void joinRoom(TL_nodes.TL_voiceRoom room) {
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_joinVoiceRoom req = new TL_nodes.TL_nodes_joinVoiceRoom();
        req.room_id = room.id;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_voiceRoom) {
                openVoiceRoom((TL_nodes.TL_nodes_voiceRoom) res);
            }
        }));
    }

    private void openVoiceRoom(TL_nodes.TL_nodes_voiceRoom voiceRoom) {
        // Navigate to voice room screen / connect to SFU
        // In a full implementation, this would:
        // 1. Connect WebSocket to voiceRoom.rtc_url with voiceRoom.rtc_token
        // 2. Get router capabilities, create transports, produce/consume
        NodeVoiceRoomActivity vra = new NodeVoiceRoomActivity(voiceRoom);
        presentFragment(vra);
    }

    private void showMoreMenu() {
        // Options like "Manage rooms", permissions, etc.
    }

    private class ActiveRoomsAdapter extends android.widget.BaseAdapter {
        private final Context context;
        ActiveRoomsAdapter(Context ctx) { this.context = ctx; }
        @Override public int getCount() { return activeRooms.size(); }
        @Override public TL_nodes.TL_voiceRoom getItem(int pos) { return activeRooms.get(pos); }
        @Override public long getItemId(int pos) { return activeRooms.get(pos).id; }

        @Override
        public View getView(int pos, View cv, android.view.ViewGroup parent) {
            if (cv == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
                row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                BackupImageView av = new BackupImageView(context);
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
                TextView sub = new TextView(context);
                sub.setTag("sub");
                sub.setTextSize(13);
                sub.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
                col.addView(sub);
                row.addView(col, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                TextView joinBtn = new TextView(context);
                joinBtn.setTag("join");
                joinBtn.setText(LocaleController.getString(R.string.NodesJoinCall));
                joinBtn.setTextSize(14);
                joinBtn.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
                joinBtn.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                joinBtn.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(6), AndroidUtilities.dp(12), AndroidUtilities.dp(6));
                joinBtn.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(16),
                        Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
                row.addView(joinBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                cv = row;
            }
            TL_nodes.TL_voiceRoom room = getItem(pos);
            LinearLayout row = (LinearLayout) cv;

            ((TextView) row.findViewWithTag("name")).setText(room.title.isEmpty() ? "Group Call" : room.title);
            ((TextView) row.findViewWithTag("sub")).setText(LocaleController.formatString(R.string.NodesCallParticipants, room.participants_count));

            TextView joinBtn = (TextView) row.findViewWithTag("join");
            joinBtn.setOnClickListener(v -> joinRoom(room));

            return cv;
        }
    }
}
