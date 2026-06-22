package org.telegram.ui.Nodes;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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

public class NodeMembersActivity extends BaseFragment {

    private final long nodeId;
    private final ArrayList<TL_nodes.TL_nodeMember> members = new ArrayList<>();
    private final ArrayList<TLRPC.User> users = new ArrayList<>();
    private TL_nodes.TL_nodes_fullNode fullNode;
    private MembersAdapter adapter;

    public NodeMembersActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesMembersCount));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        ListView lv = new ListView(context);
        lv.setDividerHeight(0);
        adapter = new MembersAdapter(context);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener((parent, view, pos, id) -> {
            showMemberMenu(pos);
            return true;
        });
        ((FrameLayout) fragmentView).addView(lv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fullNode = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fullNode != null) {
            members.addAll(fullNode.members);
            users.addAll(fullNode.users);
            adapter.notifyDataSetChanged();
        }
        loadMembers();
        return fragmentView;
    }

    private void loadMembers() {
        TL_nodes.TL_nodes_getMembers req = new TL_nodes.TL_nodes_getMembers();
        req.node_id = nodeId;
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

    private TLRPC.User findUser(long id) {
        for (TLRPC.User u : users) if (u.id == id) return u;
        return null;
    }

    private void showMemberMenu(int pos) {
        TL_nodes.TL_nodeMember m = members.get(pos);
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setItems(new String[]{LocaleController.getString(R.string.NodesRemoveFromRole)}, (d, w) -> removeMember(m.user_id))
                .show();
    }

    private void removeMember(long userId) {
        TL_nodes.TL_nodes_removeMember req = new TL_nodes.TL_nodes_removeMember();
        req.node_id = nodeId;
        req.user_id = userId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            members.removeIf(m -> m.user_id == userId);
            if (adapter != null) adapter.notifyDataSetChanged();
        }));
    }

    private class MembersAdapter extends android.widget.BaseAdapter {
        private final Context context;
        MembersAdapter(Context ctx) { this.context = ctx; }
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
                row.setBackground(Theme.getSelectorDrawable(false));
                row.setClickable(true);
                row.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);

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

                cv = row;
            }
            TL_nodes.TL_nodeMember m = getItem(pos);
            LinearLayout row = (LinearLayout) cv;
            // Tap → member's NODE multiprofile
            row.setOnClickListener(v -> presentFragment(new NodeMemberProfileActivity(nodeId, m.user_id)));
            BackupImageView av = (BackupImageView) row.findViewWithTag("av");
            TextView name = (TextView) row.findViewWithTag("name");
            TextView sub = (TextView) row.findViewWithTag("sub");

            TLRPC.User user = findUser(m.user_id);
            if (user != null) {
                AvatarDrawable ad = new AvatarDrawable();
                ad.setInfo(user);
                av.setForUserOrChat(user, ad);
                name.setText(user.first_name + (!TextUtils.isEmpty(user.last_name) ? " " + user.last_name : ""));
            }
            // Show top role name if non-default
            if (fullNode != null && !m.role_ids.isEmpty()) {
                TL_nodes.TL_nodeRole top = null;
                for (TL_nodes.TL_nodeRole r : fullNode.roles) {
                    if (m.role_ids.contains(r.id) && !r.is_default) {
                        if (top == null || r.ord > top.ord) top = r;
                    }
                }
                sub.setText(top != null ? top.title : "");
            }
            return cv;
        }
    }
}
