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
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class NodeRoleUsersActivity extends BaseFragment {

    private final long nodeId;
    private final TL_nodes.TL_nodeRole role;
    private final ArrayList<TL_nodes.TL_nodeMember> members = new ArrayList<>();
    private final ArrayList<TLRPC.User> users = new ArrayList<>();
    private UsersAdapter adapter;

    public NodeRoleUsersActivity(long nodeId, TL_nodes.TL_nodeRole role) {
        this.nodeId = nodeId;
        this.role = role;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(role.is_owner ? LocaleController.getString(R.string.NodesOwnerRole)
                : role.is_default ? LocaleController.getString(R.string.NodesMemberRole) : role.title);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        // Add User button
        if (!role.is_owner) {
            LinearLayout addRow = buildAddRow(context);
            root.addView(addRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        ListView listView = new ListView(context);
        listView.setDividerHeight(0);
        adapter = new UsersAdapter(context);
        listView.setAdapter(adapter);
        root.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        ((FrameLayout) fragmentView).addView(root, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        loadUsers();
        return fragmentView;
    }

    private LinearLayout buildAddRow(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(Theme.getSelectorDrawable(false));
        row.setOnClickListener(v -> { /* Open contact picker */ });

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.msg_addcontact);
        icon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
        row.addView(icon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView tv = new TextView(context);
        tv.setText(LocaleController.getString(R.string.NodesAddUser));
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        row.addView(tv);
        return row;
    }

    private void loadUsers() {
        TL_nodes.TL_nodes_getRoleUsers req = new TL_nodes.TL_nodes_getRoleUsers();
        req.role_id = role.id;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_members) {
                TL_nodes.TL_nodes_members result = (TL_nodes.TL_nodes_members) res;
                members.clear();
                members.addAll(result.members);
                users.clear();
                users.addAll(result.users);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        }));
    }

    private TLRPC.User findUser(long userId) {
        for (TLRPC.User u : users) {
            if (u.id == userId) return u;
        }
        return null;
    }

    private class UsersAdapter extends android.widget.BaseAdapter {
        private final Context context;
        UsersAdapter(Context ctx) { this.context = ctx; }
        @Override public int getCount() { return members.size(); }
        @Override public TL_nodes.TL_nodeMember getItem(int pos) { return members.get(pos); }
        @Override public long getItemId(int pos) { return members.get(pos).user_id; }

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

                TextView name = new TextView(context);
                name.setTag("name");
                name.setTextSize(16);
                name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                row.addView(name, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                cv = row;
            }

            TL_nodes.TL_nodeMember m = getItem(pos);
            LinearLayout row = (LinearLayout) cv;
            BackupImageView av = (BackupImageView) row.findViewWithTag("av");
            TextView name = (TextView) row.findViewWithTag("name");

            TLRPC.User user = findUser(m.user_id);
            if (user != null) {
                AvatarDrawable ad = new AvatarDrawable();
                ad.setInfo(user);
                av.setForUserOrChat(user, ad);
                name.setText(user.first_name + (user.last_name != null ? " " + user.last_name : ""));
            }

            return cv;
        }
    }
}
