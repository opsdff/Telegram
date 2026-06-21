package org.telegram.ui.Nodes;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
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

import java.util.ArrayList;

/**
 * Create new role with name, icon, color, permissions.
 * Design: Add New Role/1.png
 */
public class AddNewRoleActivity extends BaseFragment {

    private final long nodeId;
    private EditText nameField;
    private String selectedIcon = null;
    private int selectedColor = 0xFF2196F3;
    private long permissions = TL_nodes.PERM_MEMBER_DEFAULT;

    // Permission switches
    private boolean[] permStates = new boolean[11];
    private static final long[] PERM_BITS = {
            TL_nodes.PERM_CHANGE_NODE_INFO,
            TL_nodes.PERM_CREATE_OWN_CHATS,
            TL_nodes.PERM_ADMIN_GROUPS,
            TL_nodes.PERM_ADMIN_CHANNELS,
            TL_nodes.PERM_DELETE_CHATS,
            TL_nodes.PERM_PIN_CHATS,
            TL_nodes.PERM_MANAGE_FOLDERS,
            TL_nodes.PERM_BAN_USERS,
            TL_nodes.PERM_ADD_MEMBERS,
            TL_nodes.PERM_REMAIN_ANONYMOUS,
            TL_nodes.PERM_MANAGE_ROLES,
    };
    private static final int[] PERM_STRINGS = {
            R.string.NodesPermChangeNodeInfo,
            R.string.NodesPermCreateChats,
            R.string.NodesPermAdminGroups,
            R.string.NodesPermAdminChannels,
            R.string.NodesPermDeleteChats,
            R.string.NodesPermPinChats,
            R.string.NodesPermManageFolders,
            R.string.NodesPermBanUsers,
            R.string.NodesPermAddMembers,
            R.string.NodesPermAnonymous,
            R.string.NodesPermManageRoles,
    };

    public AddNewRoleActivity(long nodeId) {
        this.nodeId = nodeId;
        // Default permissions
        for (int i = 0; i < PERM_BITS.length; i++) {
            permStates[i] = (TL_nodes.PERM_MEMBER_DEFAULT & PERM_BITS[i]) != 0;
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesNewRole));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 1) doCreate();
            }
        });
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_done);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        android.widget.ScrollView sv = new android.widget.ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        // Name section
        addSectionLabel(context, content, LocaleController.getString(R.string.NodesRoleName));
        LinearLayout nameCard = new LinearLayout(context);
        nameCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        nameCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(4), AndroidUtilities.dp(16), AndroidUtilities.dp(4));
        nameField = new EditText(context);
        nameField.setHint(LocaleController.getString(R.string.NodesRoleName));
        nameField.setTextSize(16);
        nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameField.setBackground(null);
        nameCard.addView(nameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        content.addView(nameCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Icon section
        addSectionLabel(context, content, LocaleController.getString(R.string.NodesRoleIcon));
        content.addView(buildColorPicker(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        // Permissions section
        addSectionLabel(context, content, LocaleController.getString(R.string.NodesPermissions));
        LinearLayout permsCard = new LinearLayout(context);
        permsCard.setOrientation(LinearLayout.VERTICAL);
        permsCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        for (int i = 0; i < PERM_BITS.length; i++) {
            permsCard.addView(buildPermRow(context, i), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        content.addView(permsCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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

    private View buildColorPicker(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        int[] colors = {0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFF44336, 0xFF9C27B0, 0xFF009688, 0xFFE91E63};
        for (int color : colors) {
            View circle = new View(context);
            circle.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(36), color));
            final int c = color;
            circle.setOnClickListener(v -> selectedColor = c);
            row.addView(circle, LayoutHelper.createLinear(36, 36, 0, 0, 8, 0));
        }

        // No icon option
        TextView noIcon = new TextView(context);
        noIcon.setText("✕");
        noIcon.setTextSize(16);
        noIcon.setGravity(Gravity.CENTER);
        noIcon.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        noIcon.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(36),
                Theme.getColor(Theme.key_windowBackgroundGray)));
        noIcon.setOnClickListener(v -> selectedIcon = null);
        row.addView(noIcon, LayoutHelper.createLinear(36, 36, 0, 0, 0, 0));

        TextView label = new TextView(context);
        label.setText(LocaleController.getString(R.string.NodesNoIcon));
        label.setTextSize(13);
        label.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        label.setPadding(AndroidUtilities.dp(8), 0, 0, 0);
        row.addView(label);

        return row;
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

    private void doCreate() {
        String name = nameField.getText().toString().trim();
        if (name.isEmpty()) {
            AndroidUtilities.shakeView(nameField);
            return;
        }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_createRole req = new TL_nodes.TL_nodes_createRole();
        req.node_id = nodeId;
        req.title = name;
        req.color = selectedColor;
        req.permissions = permissions;
        req.admin_groups_limit = 10;
        req.admin_channels_limit = 12;
        if (selectedIcon != null) {
            req.flags |= 1;
            req.icon = selectedIcon;
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodeRole) {
                finishFragment();
            } else {
                if (getParentActivity() != null) {
                    new AlertDialog.Builder(getParentActivity())
                            .setTitle(LocaleController.getString(R.string.AppName))
                            .setMessage(err != null ? err.text : "Error")
                            .setPositiveButton(LocaleController.getString(R.string.OK), null)
                            .show();
                }
            }
        }));
    }
}
