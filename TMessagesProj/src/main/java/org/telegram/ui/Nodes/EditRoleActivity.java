package org.telegram.ui.Nodes;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
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

/**
 * Edit existing role. Design: Edit Role/Custom.png, Member.png, Owner.png
 */
public class EditRoleActivity extends BaseFragment {

    private final long nodeId;
    private TL_nodes.TL_nodeRole role;
    private EditText nameField;
    private boolean[] permStates = new boolean[11];
    private long permissions;

    private static final long[] PERM_BITS = {
            TL_nodes.PERM_CHANGE_NODE_INFO, TL_nodes.PERM_CREATE_OWN_CHATS,
            TL_nodes.PERM_ADMIN_GROUPS, TL_nodes.PERM_ADMIN_CHANNELS,
            TL_nodes.PERM_DELETE_CHATS, TL_nodes.PERM_PIN_CHATS,
            TL_nodes.PERM_MANAGE_FOLDERS, TL_nodes.PERM_BAN_USERS,
            TL_nodes.PERM_ADD_MEMBERS, TL_nodes.PERM_REMAIN_ANONYMOUS,
            TL_nodes.PERM_MANAGE_ROLES,
    };
    private static final int[] PERM_STRINGS = {
            R.string.NodesPermChangeNodeInfo, R.string.NodesPermCreateChats,
            R.string.NodesPermAdminGroups, R.string.NodesPermAdminChannels,
            R.string.NodesPermDeleteChats, R.string.NodesPermPinChats,
            R.string.NodesPermManageFolders, R.string.NodesPermBanUsers,
            R.string.NodesPermAddMembers, R.string.NodesPermAnonymous,
            R.string.NodesPermManageRoles,
    };

    public EditRoleActivity(long nodeId, TL_nodes.TL_nodeRole role) {
        this.nodeId = nodeId;
        this.role = role;
        this.permissions = role.permissions;
        for (int i = 0; i < PERM_BITS.length; i++) {
            permStates[i] = (role.permissions & PERM_BITS[i]) != 0;
        }
    }

    @Override
    public View createView(Context context) {
        String screenTitle = role.is_owner
                ? LocaleController.getString(R.string.NodesOwnerRole)
                : role.is_default
                ? LocaleController.getString(R.string.NodesMemberRole)
                : LocaleController.getString(R.string.NodesEditRole);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(screenTitle);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 1) doSave();
                else if (id == 2) confirmDelete();
            }
        });
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_done);
        if (!role.is_owner && !role.is_default) {
            actionBar.createMenu().addItem(2, R.drawable.ic_delete);
        }

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        android.widget.ScrollView sv = new android.widget.ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        // Name
        addSectionLabel(context, content, LocaleController.getString(R.string.NodesRoleName));
        LinearLayout nameCard = new LinearLayout(context);
        nameCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        nameCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(4), AndroidUtilities.dp(16), AndroidUtilities.dp(4));
        nameField = new EditText(context);
        nameField.setText(role.is_owner ? LocaleController.getString(R.string.NodesOwnerRole)
                : role.is_default ? LocaleController.getString(R.string.NodesMemberRole) : role.title);
        nameField.setTextSize(16);
        nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setBackground(null);
        // Owner and Member only allow name/icon/color edit for owner; restricted for default
        nameField.setEnabled(!role.is_owner && !role.is_default);
        if (role.is_owner || role.is_default) {
            nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        }
        nameCard.addView(nameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        content.addView(nameCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Permissions (disabled for owner — all permissions always on)
        if (!role.is_owner) {
            addSectionLabel(context, content, LocaleController.getString(R.string.NodesPermissions));
            LinearLayout permsCard = new LinearLayout(context);
            permsCard.setOrientation(LinearLayout.VERTICAL);
            permsCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            for (int i = 0; i < PERM_BITS.length; i++) {
                permsCard.addView(buildPermRow(context, i), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
            content.addView(permsCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        // Role users button
        TextView usersBtn = new TextView(context);
        usersBtn.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
        usersBtn.setText(LocaleController.getString(R.string.NodesRoleUsers));
        usersBtn.setTextSize(16);
        usersBtn.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        usersBtn.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        usersBtn.setClickable(true);
        usersBtn.setOnClickListener(v -> presentFragment(new NodeRoleUsersActivity(nodeId, role)));
        content.addView(usersBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 0));

        sv.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        ((FrameLayout) fragmentView).addView(sv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void addSectionLabel(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        tv.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
        parent.addView(tv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private View buildPermRow(Context context, int index) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        TextView tv = new TextView(context);
        tv.setText(LocaleController.getString(PERM_STRINGS[index]));
        tv.setTextSize(16);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(tv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        Switch sw = new Switch(context);
        sw.setChecked(permStates[index]);
        final int i = index;
        sw.setOnCheckedChangeListener((btn, checked) -> {
            permStates[i] = checked;
            rebuildPermissions();
        });
        row.addView(sw, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        return row;
    }

    private void rebuildPermissions() {
        permissions = 0;
        for (int i = 0; i < PERM_BITS.length; i++) {
            if (permStates[i]) permissions |= PERM_BITS[i];
        }
    }

    private void doSave() {
        TL_nodes.TL_nodes_editRole req = new TL_nodes.TL_nodes_editRole();
        req.role_id = role.id;
        String title = nameField.getText().toString().trim();
        if (!title.isEmpty() && !title.equals(role.title)) {
            req.flags |= 1;
            req.title = title;
        }
        if (!role.is_owner) {
            req.flags |= 4;
            req.permissions = permissions;
        }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodeRole) {
                finishFragment();
            }
        }));
    }

    private void confirmDelete() {
        if (getParentActivity() == null) return;
        new AlertDialog.Builder(getParentActivity())
                .setTitle(LocaleController.getString(R.string.NodesDeleteRole))
                .setMessage(LocaleController.getString(R.string.NodesDeleteRoleConfirm))
                .setPositiveButton(LocaleController.getString(R.string.OK), (d, w) -> {
                    TL_nodes.TL_nodes_deleteRole req = new TL_nodes.TL_nodes_deleteRole();
                    req.role_id = role.id;
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> finishFragment()));
                })
                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                .show();
    }
}
