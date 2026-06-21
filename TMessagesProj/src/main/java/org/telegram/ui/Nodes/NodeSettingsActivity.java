package org.telegram.ui.Nodes;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

/**
 * Node Settings screen: Edit node info + Node Type / Invite Links / Roles / Members / Delete.
 * Design: Node Settings/Main.png
 */
public class NodeSettingsActivity extends BaseFragment {

    private final long nodeId;
    private TL_nodes.TL_nodes_fullNode fullNode;

    private EditText nameField;
    private EditText descField;
    private BackupImageView avatarView;

    public NodeSettingsActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesEdit));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 1) doSave();
            }
        });
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_done);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        android.widget.ScrollView sv = new android.widget.ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        // Photo + Name + Description edit card
        LinearLayout editCard = new LinearLayout(context);
        editCard.setOrientation(LinearLayout.VERTICAL);
        editCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        editCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

        // Avatar row
        LinearLayout avatarRow = new LinearLayout(context);
        avatarRow.setOrientation(LinearLayout.HORIZONTAL);
        avatarRow.setGravity(Gravity.CENTER_VERTICAL);

        avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(16));
        AvatarDrawable ad = new AvatarDrawable();
        ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        avatarView.setImageDrawable(ad);
        avatarRow.addView(avatarView, LayoutHelper.createLinear(64, 64, 0, 0, 16, 0));

        LinearLayout nameCol = new LinearLayout(context);
        nameCol.setOrientation(LinearLayout.VERTICAL);

        nameField = new EditText(context);
        nameField.setTextSize(16);
        nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameField.setBackground(null);
        nameField.setSingleLine();
        nameCol.addView(nameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView setPhotoBtn = new TextView(context);
        setPhotoBtn.setText(LocaleController.getString(R.string.NodesSetNewPhoto));
        setPhotoBtn.setTextSize(14);
        setPhotoBtn.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        setPhotoBtn.setOnClickListener(v -> {
            // Open photo picker
        });
        nameCol.addView(setPhotoBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        avatarRow.addView(nameCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
        editCard.addView(avatarRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        descField = new EditText(context);
        descField.setTextSize(14);
        descField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descField.setBackground(null);
        descField.setGravity(Gravity.TOP);
        editCard.addView(descField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        content.addView(editCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 1));

        // Settings rows
        addSettingRow(context, content, R.drawable.msg_account, LocaleController.getString(R.string.NodesYourNodeProfile),
                () -> presentFragment(new NodeMyProfileActivity(nodeId)));
        addSettingRow(context, content, R.drawable.msg_customize, LocaleController.getString(R.string.NodesNodeTypeMenu),
                () -> presentFragment(new NodeTypeActivity(nodeId)));
        addSettingRow(context, content, R.drawable.msg_link, LocaleController.getString(R.string.NodesInviteLinks),
                () -> presentFragment(new NodeLinksActivity(nodeId)));
        addSettingRow(context, content, R.drawable.msg_permissions, LocaleController.getString(R.string.NodesRoles),
                () -> presentFragment(new NodeRolesActivity(nodeId)));
        addSettingRow(context, content, R.drawable.msg_groups, LocaleController.getString(R.string.NodesMembersCount),
                () -> presentFragment(new NodeMembersActivity(nodeId)));
        addSettingRow(context, content, R.drawable.msg_log, LocaleController.getString(R.string.NodesRecentActions), () -> {});

        // Delete row
        content.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 0, 8, 0, 0));

        TextView deleteRow = new TextView(context);
        deleteRow.setText(LocaleController.getString(R.string.NodesDeleteAndLeave));
        deleteRow.setTextSize(16);
        deleteRow.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
        deleteRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        deleteRow.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        deleteRow.setOnClickListener(v -> confirmDeleteNode());
        content.addView(deleteRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        sv.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        ((FrameLayout) fragmentView).addView(sv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // Load data
        fullNode = NodeController.getInstance(currentAccount).getCurrentFullNode();
        if (fullNode != null && fullNode.node.id == nodeId) {
            bindData();
        } else {
            NodeController.getInstance(currentAccount).loadFullNode(nodeId, (fn, err) -> {
                if (fn != null) { fullNode = fn; bindData(); }
            });
        }

        return fragmentView;
    }

    private void addSettingRow(Context context, LinearLayout parent, int iconRes, String label, Runnable action) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(Theme.getSelectorDrawable(false));

        android.widget.ImageView icon = new android.widget.ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        row.addView(icon, LayoutHelper.createLinear(24, 24, 0, 0, 16, 0));

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(tv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        android.widget.ImageView arrow = new android.widget.ImageView(context);
        arrow.setImageResource(R.drawable.attach_arrow_right);
        arrow.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        row.addView(arrow, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        row.setOnClickListener(v -> action.run());
        parent.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 1));
    }

    private void bindData() {
        if (fullNode == null) return;
        nameField.setText(fullNode.node.title);
        if (!TextUtils.isEmpty(fullNode.node.description)) {
            descField.setText(fullNode.node.description);
        }
    }

    private void doSave() {
        String newTitle = nameField.getText().toString().trim();
        if (TextUtils.isEmpty(newTitle)) {
            AndroidUtilities.shakeView(nameField);
            return;
        }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_editNode req = new TL_nodes.TL_nodes_editNode();
        req.node_id = nodeId;
        req.flags |= 1;
        req.title = newTitle;
        String desc = descField.getText().toString().trim();
        if (!TextUtils.isEmpty(desc)) {
            req.flags |= 2;
            req.description = desc;
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_fullNode) {
                NodeController.getInstance(currentAccount).setCurrentFullNode((TL_nodes.TL_nodes_fullNode) res);
                finishFragment();
            }
        }));
    }

    private void confirmDeleteNode() {
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setTitle(LocaleController.getString(R.string.NodesDeleteNode))
                .setMessage(LocaleController.getString(R.string.NodesDeleteNodeConfirm))
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> {
                    TL_nodes.TL_nodes_deleteNode req = new TL_nodes.TL_nodes_deleteNode();
                    req.node_id = nodeId;
                    req.for_all = fullNode != null && fullNode.node.is_owner;
                    if (req.for_all) req.flags |= 1;
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        NodeController.getInstance(currentAccount).clearCurrentNode();
                        NodeController.getInstance(currentAccount).invalidateMyNodes();
                        finishFragment();
                        finishFragment(); // pop chat list too
                    }));
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }
}
