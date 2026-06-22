package org.telegram.ui.Nodes;

import android.content.Context;
import android.os.Bundle;
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
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_nodes;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

/**
 * Node profile screen — shows avatar, name, member count, description,
 * invite link, Node Settings button, Invite Members button, member list.
 * Design: Node Profile/Main.png, Menu.png
 */
public class NodeProfileActivity extends BaseFragment {

    private final long nodeId;
    private TL_nodes.TL_nodes_fullNode fullNode;

    private BackupImageView avatarView;
    private TextView titleView;
    private TextView subtitleView;
    private TextView descView;
    private TextView linkView;
    private ListView memberList;
    private MembersAdapter adapter;

    public NodeProfileActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    // edit
                    if (fullNode != null && fullNode.node.is_owner) {
                        NodeSettingsActivity s = new NodeSettingsActivity(nodeId);
                        presentFragment(s);
                    }
                } else if (id == 2) {
                    // more menu
                    showMoreMenu();
                }
            }
        });
        // Edit + more (delete) — owner only
        final boolean ownerMenu = NodeController.getInstance(currentAccount).isNodeOwner();
        if (ownerMenu) {
            actionBar.createMenu().addItem(1, R.drawable.msg_edit);
            actionBar.createMenu().addItem(2, R.drawable.ic_ab_other);
        }

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);

        // Header card
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        header.setPadding(0, AndroidUtilities.dp(24), 0, AndroidUtilities.dp(16));

        avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(32));
        header.addView(avatarView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        titleView = new TextView(context);
        titleView.setTextSize(22);
        titleView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleView.setGravity(Gravity.CENTER);
        header.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

        subtitleView = new TextView(context);
        subtitleView.setTextSize(14);
        subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        subtitleView.setGravity(Gravity.CENTER);
        header.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

        scrollContent.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Description card
        LinearLayout infoCard = new LinearLayout(context);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        infoCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        descView = new TextView(context);
        descView.setTextSize(14);
        descView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        infoCard.addView(descView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        // Invite link row
        LinearLayout linkRow = new LinearLayout(context);
        linkRow.setOrientation(LinearLayout.HORIZONTAL);
        linkRow.setGravity(Gravity.CENTER_VERTICAL);

        linkView = new TextView(context);
        linkView.setTextSize(14);
        linkView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        linkView.setClickable(true);
        linkView.setFocusable(true);
        linkView.setBackground(Theme.getSelectorDrawable(false));
        linkView.setOnClickListener(v -> openCollectibleInfo());
        linkRow.addView(linkView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        ImageView qrIcon = new ImageView(context);
        qrIcon.setImageResource(R.drawable.msg_qrcode);
        qrIcon.setColorFilter(Theme.getColor(Theme.key_featuredStickers_addButton));
        qrIcon.setOnClickListener(v -> showQR());
        linkRow.addView(qrIcon, LayoutHelper.createLinear(24, 24));

        infoCard.addView(linkRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        scrollContent.addView(infoCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 1, 0, 1));

        // Action rows
        addActionRow(context, scrollContent, R.drawable.msg_settings, LocaleController.getString(R.string.NodesNodeSettingsMenu), () -> {
            NodeSettingsActivity s = new NodeSettingsActivity(nodeId);
            presentFragment(s);
        });

        addActionRow(context, scrollContent, R.drawable.msg_addcontact, LocaleController.getString(R.string.NodesInviteMembers), () -> {
            // Open contact picker for invite
        });

        // Member list
        adapter = new MembersAdapter(context);
        memberList = new ListView(context);
        memberList.setAdapter(adapter);
        memberList.setDividerHeight(0);
        memberList.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        scrollContent.addView(memberList, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        android.widget.ScrollView sv = new android.widget.ScrollView(context);
        sv.addView(scrollContent, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        ((FrameLayout) fragmentView).addView(sv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Load data
        fullNode = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fullNode != null && fullNode.node.id == nodeId) {
            bindData();
        } else {
            loadData();
        }

        return fragmentView;
    }

    private void addActionRow(Context context, LinearLayout parent, int iconRes, String label, Runnable action) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(Theme.getSelectorDrawable(false));

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        row.addView(icon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(tv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        row.setOnClickListener(v -> action.run());
        parent.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 1));
    }

    private void loadData() {
        NodeController.getInstance(currentAccount).loadFullNode(nodeId, (fn, err) -> {
            if (fn != null) {
                fullNode = fn;
                bindData();
            }
        });
    }

    private void bindData() {
        if (fullNode == null) return;
        TL_nodes.TL_node node = fullNode.node;

        AvatarDrawable ad = new AvatarDrawable();
        ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        ad.setInfo(node.id, node.title, null);
        avatarView.setImageDrawable(ad);

        titleView.setText(node.title);
        subtitleView.setText(LocaleController.formatString(R.string.NodesMembersOnline,
                node.members_count, node.online_count));

        if (!TextUtils.isEmpty(node.description)) {
            descView.setText(node.description);
            descView.setVisibility(View.VISIBLE);
        } else {
            descView.setVisibility(View.GONE);
        }

        if (node.is_public && !TextUtils.isEmpty(node.link)) {
            linkView.setText("t.me/n/" + node.link);
        } else if (!TextUtils.isEmpty(node.invite_hash)) {
            linkView.setText("t.me/+" + node.invite_hash);
        }

        adapter.notifyDataSetChanged();
    }

    private void showMoreMenu() {
        if (getParentActivity() == null) return;
        boolean isOwner = fullNode != null && fullNode.node.is_owner;
        if (!isOwner) return; // only owner has the more menu (Leave Node temporarily removed)
        String[] items = new String[]{LocaleController.getString(R.string.NodesDeleteNode)};
        new AlertDialog.Builder(getParentActivity())
                .setItems(items, (d, which) -> {
                    if (which == 0) confirmDelete();
                })
                .show();
    }

    private void confirmLeave() {
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setTitle(LocaleController.getString(R.string.NodesLeaveNode))
                .setMessage(LocaleController.getString(R.string.NodesLeaveNodeConfirm))
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> doLeave())
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void doLeave() {
        TL_nodes.TL_nodes_leaveNode req = new TL_nodes.TL_nodes_leaveNode();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            NodeController.getInstance(currentAccount).clearCurrentNode();
            NodeController.getInstance(currentAccount).invalidateMyNodes();
            finishFragment();
        }));
    }

    private void confirmDelete() {
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setTitle(LocaleController.getString(R.string.NodesDeleteNode))
                .setMessage(LocaleController.getString(R.string.NodesDeleteNodeConfirm))
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> doDelete())
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }

    private void doDelete() {
        TL_nodes.TL_nodes_deleteNode req = new TL_nodes.TL_nodes_deleteNode();
        req.node_id = nodeId;
        req.for_all = true;
        req.flags |= 1;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            NodeController.getInstance(currentAccount).clearCurrentNode();
            NodeController.getInstance(currentAccount).invalidateMyNodes();
            finishFragment();
        }));
    }

    private void openCollectibleInfo() {
        if (fullNode == null) return;
        org.telegram.tgnet.tl.TL_nodes.TL_node node = fullNode.node;
        if (!node.is_public || TextUtils.isEmpty(node.link)) return;

        final String slug = node.link;
        final org.telegram.ui.ActionBar.AlertDialog progress =
                new org.telegram.ui.ActionBar.AlertDialog(getParentActivity(),
                        org.telegram.ui.ActionBar.AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_getCollectibleInfo req = new TL_nodes.TL_nodes_getCollectibleInfo();
        req.link = slug;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (!(res instanceof org.telegram.tgnet.tl.TL_fragment.TL_collectibleInfo)) return;
            org.telegram.tgnet.tl.TL_fragment.TL_collectibleInfo info =
                    (org.telegram.tgnet.tl.TL_fragment.TL_collectibleInfo) res;
            if (getContext() == null) return;
            org.telegram.ui.FragmentUsernameBottomSheet.open(
                    getContext(),
                    org.telegram.ui.FragmentUsernameBottomSheet.TYPE_USERNAME,
                    slug,
                    null,  // no owner user/chat object, it's a node
                    info,
                    getResourceProvider()
            );
        }));
    }

    private void showQR() {
        // QR code display — placeholder
    }

    // ─── Members adapter ──────────────────────────────────────────────

    private class MembersAdapter extends android.widget.BaseAdapter {
        private final Context context;

        MembersAdapter(Context ctx) {
            this.context = ctx;
        }

        @Override
        public int getCount() {
            return fullNode != null ? fullNode.members.size() : 0;
        }

        @Override
        public TL_nodes.TL_nodeMember getItem(int pos) {
            return fullNode != null ? fullNode.members.get(pos) : null;
        }

        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
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

                LinearLayout textCol = new LinearLayout(context);
                textCol.setOrientation(LinearLayout.VERTICAL);

                TextView name = new TextView(context);
                name.setTag("name");
                name.setTextSize(16);
                name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                name.setSingleLine();
                textCol.addView(name);

                TextView status = new TextView(context);
                status.setTag("status");
                status.setTextSize(13);
                status.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
                textCol.addView(status);

                row.addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

                TextView roleTag = new TextView(context);
                roleTag.setTag("role");
                roleTag.setTextSize(13);
                roleTag.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                row.addView(roleTag, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                convertView = row;
            }

            TL_nodes.TL_nodeMember member = getItem(position);
            if (member == null) return convertView;

            LinearLayout row = (LinearLayout) convertView;
            // Tap → member's NODE multiprofile (not their main Telegram profile)
            row.setOnClickListener(v -> presentFragment(new NodeMemberProfileActivity(nodeId, member.user_id)));
            BackupImageView av = (BackupImageView) row.findViewWithTag("av");
            TextView name = (TextView) row.findViewWithTag("name");
            TextView status = (TextView) row.findViewWithTag("status");
            TextView roleTag = (TextView) row.findViewWithTag("role");

            // Find user
            TLRPC.User user = null;
            if (fullNode != null) {
                for (TLRPC.User u : fullNode.users) {
                    if (u.id == member.user_id) {
                        user = u;
                        break;
                    }
                }
            }

            if (member.anonymous && !TextUtils.isEmpty(member.first_name)) {
                name.setText(member.first_name + (TextUtils.isEmpty(member.last_name) ? "" : " " + member.last_name));
            } else if (user != null) {
                name.setText(user.first_name + (TextUtils.isEmpty(user.last_name) ? "" : " " + user.last_name));
                AvatarDrawable ad = new AvatarDrawable();
                ad.setInfo(user);
                av.setForUserOrChat(user, ad);
            }

            status.setText("last seen recently"); // placeholder

            // Find top role
            if (fullNode != null && !member.role_ids.isEmpty()) {
                TL_nodes.TL_nodeRole topRole = null;
                for (TL_nodes.TL_nodeRole r : fullNode.roles) {
                    if (member.role_ids.contains(r.id)) {
                        if (topRole == null || r.ord > topRole.ord) topRole = r;
                    }
                }
                if (topRole != null && !topRole.is_default) {
                    roleTag.setText(topRole.title);
                    roleTag.setVisibility(View.VISIBLE);
                } else {
                    roleTag.setVisibility(View.GONE);
                }
            } else {
                roleTag.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
