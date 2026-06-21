package org.telegram.ui.Nodes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class NewNodeActivity extends BaseFragment {

    private final boolean isJoin;
    private EditText nameField;
    private EditText descField;

    public NewNodeActivity(boolean isJoin) {
        this.isJoin = isJoin;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesNewNode));
        actionBar.setAllowOverlayTitle(false);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    doCreate();
                }
            }
        });

        // Checkmark done button
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_done);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

        // Avatar picker + name row
        LinearLayout nameRow = new LinearLayout(context);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);

        BackupImageView avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(16));
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        avatarView.setImageDrawable(avatarDrawable);
        // Camera icon overlay would go here in production
        nameRow.addView(avatarView, LayoutHelper.createLinear(64, 64, 0, 0, 12, 0));

        nameField = new EditText(context);
        nameField.setHint(LocaleController.getString(R.string.NodesNodeName));
        nameField.setTextSize(16);
        nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameField.setBackground(null);
        nameField.setSingleLine();
        nameRow.addView(nameField, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

        content.addView(nameRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Description
        descField = new EditText(context);
        descField.setHint(LocaleController.getString(R.string.NodesDescription));
        descField.setTextSize(16);
        descField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descField.setBackground(null);
        descField.setGravity(Gravity.TOP);
        content.addView(descField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        TextView hint = new TextView(context);
        hint.setText(LocaleController.getString(R.string.NodesDescriptionHint));
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        content.addView(hint, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ((FrameLayout) fragmentView).addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 0, 0, 0));

        return fragmentView;
    }

    private void doCreate() {
        String name = nameField.getText().toString().trim();
        if (name.isEmpty()) {
            AndroidUtilities.shakeView(nameField);
            return;
        }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_createNode req = new TL_nodes.TL_nodes_createNode();
        req.title = name;
        String desc = descField.getText().toString().trim();
        if (!desc.isEmpty()) {
            req.flags |= 1;
            req.description = desc;
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_fullNode) {
                TL_nodes.TL_nodes_fullNode fn = (TL_nodes.TL_nodes_fullNode) res;
                NodeController.getInstance(currentAccount).setCurrentFullNode(fn);
                NodeController.getInstance(currentAccount).invalidateMyNodes();

                // First go to type selection, then chat list
                presentFragment(new NodeTypeActivity(fn.node.id));
            } else {
                if (getParentActivity() != null) {
                    String msg = err != null ? err.text : "Error creating node";
                    AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
                    b.setTitle(LocaleController.getString(R.string.AppName));
                    b.setMessage(msg);
                    b.setPositiveButton(LocaleController.getString(R.string.OK), null);
                    b.show();
                }
            }
        }));
    }
}
