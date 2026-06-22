package org.telegram.ui.Nodes;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
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
 * Roles list: Owner (pinned top), Member (default), then custom roles sorted by ord desc.
 * Design: Roles/Default.png, Roles/Custom.png
 */
public class NodeRolesActivity extends BaseFragment {

    private final long nodeId;
    private final ArrayList<TL_nodes.TL_nodeRole> roles = new ArrayList<>();
    private RolesAdapter adapter;
    private TL_nodes.TL_nodes_fullNode fullNode;

    public NodeRolesActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesRolesTitle));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        // Header row: "Add New Role" — owner only
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        final boolean isOwner = NodeController.getInstance(currentAccount).isNodeOwner();
        if (isOwner) {
            LinearLayout addRow = new LinearLayout(context);
            addRow.setOrientation(LinearLayout.HORIZONTAL);
            addRow.setGravity(Gravity.CENTER_VERTICAL);
            addRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
            addRow.setClickable(true);
            addRow.setFocusable(true);
            addRow.setBackground(Theme.getSelectorDrawable(false));
            addRow.setOnClickListener(v -> presentFragment(new AddNewRoleActivity(nodeId)));

            ImageView addIcon = new ImageView(context);
            addIcon.setImageResource(R.drawable.msg_addcontact);
            addIcon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
            addRow.addView(addIcon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

            TextView addText = new TextView(context);
            addText.setText(LocaleController.getString(R.string.NodesAddNewRole));
            addText.setTextSize(16);
            addText.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            addRow.addView(addText, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
            root.addView(addRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            root.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));
        }

        // Roles list
        ListView listView = new ListView(context);
        listView.setDividerHeight(0);
        listView.setDivider(null);
        adapter = new RolesAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TL_nodes.TL_nodeRole role = adapter.getItem(position);
            if (role != null) {
                presentFragment(new EditRoleActivity(nodeId, role));
            }
        });
        root.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1.0f));

        // Hint
        TextView hint = new TextView(context);
        hint.setText(LocaleController.getString(R.string.NodesRolesHint));
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        hint.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        root.addView(hint, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ((FrameLayout) fragmentView).addView(root, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Load data
        fullNode = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fullNode != null) {
            updateRoles(fullNode.roles);
        }
        loadRoles();

        return fragmentView;
    }

    private void loadRoles() {
        TL_nodes.TL_nodes_getRoles req = new TL_nodes.TL_nodes_getRoles();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_roles) {
                updateRoles(((TL_nodes.TL_nodes_roles) res).roles);
            }
        }));
    }

    private void updateRoles(ArrayList<TL_nodes.TL_nodeRole> newRoles) {
        roles.clear();
        // Sort: Owner first, then by ord desc
        TL_nodes.TL_nodeRole owner = null;
        TL_nodes.TL_nodeRole member = null;
        ArrayList<TL_nodes.TL_nodeRole> custom = new ArrayList<>();
        for (TL_nodes.TL_nodeRole r : newRoles) {
            if (r.is_owner) owner = r;
            else if (r.is_default) member = r;
            else custom.add(r);
        }
        if (owner != null) roles.add(owner);
        custom.sort((a, b) -> Integer.compare(b.ord, a.ord));
        roles.addAll(custom);
        if (member != null) roles.add(member);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private class RolesAdapter extends android.widget.BaseAdapter {
        private final Context context;

        RolesAdapter(Context ctx) { this.context = ctx; }

        @Override public int getCount() { return roles.size(); }
        @Override public TL_nodes.TL_nodeRole getItem(int pos) { return roles.get(pos); }
        @Override public long getItemId(int pos) { return roles.get(pos).id; }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
                row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                row.setClickable(true);
                row.setFocusable(true);
                row.setBackground(Theme.getSelectorDrawable(false));

                // Role icon circle / emoji
                TextView iconView = new TextView(context);
                iconView.setTag("icon");
                iconView.setTextSize(20);
                iconView.setGravity(Gravity.CENTER);
                iconView.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(44), Color.TRANSPARENT));
                row.addView(iconView, LayoutHelper.createLinear(44, 44, 0, 0, 12, 0));

                TextView titleView = new TextView(context);
                titleView.setTag("title");
                titleView.setTextSize(16);
                titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                row.addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                TextView countView = new TextView(context);
                countView.setTag("count");
                countView.setTextSize(16);
                countView.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                row.addView(countView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                convertView = row;
            }

            TL_nodes.TL_nodeRole role = getItem(position);
            LinearLayout row = (LinearLayout) convertView;

            TextView iconView = (TextView) row.findViewWithTag("icon");
            TextView titleView = (TextView) row.findViewWithTag("title");
            TextView countView = (TextView) row.findViewWithTag("count");

            // Icon: emoji if set, else default icon based on type
            if (!TextUtils.isEmpty(role.icon)) {
                iconView.setText(role.icon);
            } else if (role.is_owner) {
                iconView.setText("👑");
            } else if (role.is_default) {
                iconView.setText("👤");
            } else {
                iconView.setText("🎭");
            }
            // Color the icon background
            iconView.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(44), role.color != 0 ? role.color : 0x3300aaff));

            titleView.setText(role.is_owner
                    ? LocaleController.getString(R.string.NodesOwnerRole)
                    : role.is_default
                    ? LocaleController.getString(R.string.NodesMemberRole)
                    : role.title);

            countView.setText(String.valueOf(role.members_count));

            return convertView;
        }
    }
}
