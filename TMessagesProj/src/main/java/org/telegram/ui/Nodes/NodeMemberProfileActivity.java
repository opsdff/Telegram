package org.telegram.ui.Nodes;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
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
import org.telegram.ui.ProfileActivity;

/**
 * Member profile inside a node — shows the member's NODE identity (their node-specific
 * name / bio / photo + roles), NOT their main Telegram profile. Design: Node Member Profile/
 *
 * If privacy allows (server returns main_user_id), a "Main Profile" row navigates to the
 * member's real Telegram profile.
 */
public class NodeMemberProfileActivity extends BaseFragment {

    private final long nodeId;
    private final long userId;
    private TL_nodes.TL_nodes_memberProfile data;

    private BackupImageView avatarView;
    private TextView nameView;
    private TextView bioView;
    private LinearLayout rolesContainer;
    private LinearLayout actionsContainer;

    public NodeMemberProfileActivity(long nodeId, long userId) {
        this.nodeId = nodeId;
        this.userId = userId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesMemberProfile));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        android.widget.ScrollView sv = new android.widget.ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        // Header card with node identity
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        header.setPadding(0, AndroidUtilities.dp(24), 0, AndroidUtilities.dp(16));

        avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(40));
        header.addView(avatarView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        nameView = new TextView(context);
        nameView.setTextSize(22);
        nameView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameView.setGravity(Gravity.CENTER);
        header.addView(nameView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 16, 0, 16, 4));

        bioView = new TextView(context);
        bioView.setTextSize(14);
        bioView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        bioView.setGravity(Gravity.CENTER);
        bioView.setVisibility(View.GONE);
        header.addView(bioView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 24, 0, 24, 0));

        content.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        // Roles section
        rolesContainer = new LinearLayout(context);
        rolesContainer.setOrientation(LinearLayout.VERTICAL);
        rolesContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        content.addView(rolesContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        // Actions section (Main Profile etc.)
        actionsContainer = new LinearLayout(context);
        actionsContainer.setOrientation(LinearLayout.VERTICAL);
        actionsContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        content.addView(actionsContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        sv.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        ((FrameLayout) fragmentView).addView(sv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        loadProfile();
        return fragmentView;
    }

    private void loadProfile() {
        final AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_getMemberProfile req = new TL_nodes.TL_nodes_getMemberProfile();
        req.node_id = nodeId;
        req.user_id = userId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_memberProfile) {
                data = (TL_nodes.TL_nodes_memberProfile) res;
                bindProfile();
            } else if (getParentActivity() != null) {
                org.telegram.ui.Components.BulletinFactory.of(NodeMemberProfileActivity.this).showForError(err);
            }
        }));
    }

    private void bindProfile() {
        if (data == null) return;
        Context context = getContext();
        if (context == null) return;

        TL_nodes.TL_nodeProfile profile = data.profile;

        // Node-specific display name (falls back to the user's name only if node name absent)
        String first = profile != null && !TextUtils.isEmpty(profile.first_name) ? profile.first_name
                : (data.user != null ? data.user.first_name : "");
        String last = profile != null && !TextUtils.isEmpty(profile.last_name) ? profile.last_name
                : (data.user != null && data.user.last_name != null ? data.user.last_name : "");
        String displayName = (first + " " + (last == null ? "" : last)).trim();
        nameView.setText(displayName);

        AvatarDrawable ad = new AvatarDrawable();
        ad.setInfo(userId, first, last);
        // Node photo would be loaded via profile.photo_id; fall back to colored avatar.
        avatarView.setImageDrawable(ad);

        if (profile != null && !TextUtils.isEmpty(profile.bio)) {
            bioView.setText(profile.bio);
            bioView.setVisibility(View.VISIBLE);
        } else {
            bioView.setVisibility(View.GONE);
        }

        // Roles
        rolesContainer.removeAllViews();
        TL_nodes.TL_nodes_fullNode fn = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fn != null && data.role_ids != null && !data.role_ids.isEmpty()) {
            addSectionLabel(context, rolesContainer, LocaleController.getString(R.string.NodesRoles));
            for (TL_nodes.TL_nodeRole r : fn.roles) {
                if (data.role_ids.contains(r.id)) {
                    addRoleRow(context, rolesContainer, r);
                }
            }
        }

        // Actions
        actionsContainer.removeAllViews();
        // Main Profile — only if server returned main_user_id (privacy allows)
        if ((data.flags & 1) != 0 && data.main_user_id != 0) {
            addActionRow(context, actionsContainer, R.drawable.msg_openprofile,
                    LocaleController.getString(R.string.NodesMainProfile), () -> openMainProfile(data.main_user_id));
        }
    }

    private void openMainProfile(long mainUserId) {
        // Ensure the user object is available, then open the real Telegram profile.
        MessagesController mc = MessagesController.getInstance(currentAccount);
        if (mc.getUser(mainUserId) == null && data.user != null && data.user.id == mainUserId) {
            mc.putUser(data.user, false);
        }
        Bundle args = new Bundle();
        args.putLong("user_id", mainUserId);
        presentFragment(new ProfileActivity(args));
    }

    private void addSectionLabel(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        tv.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
        parent.addView(tv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addRoleRow(Context context, LinearLayout parent, TL_nodes.TL_nodeRole role) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        TextView icon = new TextView(context);
        icon.setTextSize(18);
        icon.setGravity(Gravity.CENTER);
        if (!TextUtils.isEmpty(role.icon)) icon.setText(role.icon);
        else if (role.is_owner) icon.setText("👑");
        else if (role.is_default) icon.setText("👤");
        else icon.setText("🎭");
        icon.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(36), role.color != 0 ? role.color : 0x3300AAFF));
        row.addView(icon, LayoutHelper.createLinear(36, 36, 0, 0, 12, 0));

        TextView tv = new TextView(context);
        tv.setText(role.is_owner ? LocaleController.getString(R.string.NodesOwnerRole)
                : role.is_default ? LocaleController.getString(R.string.NodesMemberRole) : role.title);
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(tv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        parent.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addActionRow(Context context, LinearLayout parent, int iconRes, String label, Runnable action) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        row.setClickable(true);
        row.setBackground(Theme.getSelectorDrawable(false));
        row.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        row.setOnClickListener(v -> action.run());

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        row.addView(icon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(tv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        parent.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }
}
