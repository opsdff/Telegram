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
 * Your Node Profile — edit your node-specific identity (name/bio/photo + privacy).
 * Design: Set Node Profile/
 */
public class NodeMyProfileActivity extends BaseFragment {

    private final long nodeId;
    private TL_nodes.TL_nodeProfile profile;

    private BackupImageView avatarView;
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText bioField;

    // Privacy: show_main_profile / share_contact
    private int showMainProfile = TL_nodes.PRIVACY_EVERYBODY;
    private int shareContact = TL_nodes.PRIVACY_CONTACTS;

    public NodeMyProfileActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesYourNodeProfile));
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

        // Avatar + name fields card
        LinearLayout profileCard = new LinearLayout(context);
        profileCard.setOrientation(LinearLayout.VERTICAL);
        profileCard.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        profileCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

        // Avatar row
        LinearLayout avatarRow = new LinearLayout(context);
        avatarRow.setOrientation(LinearLayout.HORIZONTAL);
        avatarRow.setGravity(Gravity.CENTER_VERTICAL);

        avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(32));
        AvatarDrawable ad = new AvatarDrawable();
        ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        avatarView.setImageDrawable(ad);
        avatarView.setOnClickListener(v -> { /* photo picker */ });
        avatarRow.addView(avatarView, LayoutHelper.createLinear(64, 64, 0, 0, 16, 0));

        TextView changePhoto = new TextView(context);
        changePhoto.setText(LocaleController.getString(R.string.NodesSetNewPhoto));
        changePhoto.setTextSize(15);
        changePhoto.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        changePhoto.setOnClickListener(v -> { /* photo picker */ });
        avatarRow.addView(changePhoto);
        profileCard.addView(avatarRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // First name
        addFieldLabel(context, profileCard, LocaleController.getString(R.string.NodesFirstName));
        firstNameField = addField(context, profileCard, LocaleController.getString(R.string.NodesFirstName));

        addFieldLabel(context, profileCard, LocaleController.getString(R.string.NodesLastName));
        lastNameField = addField(context, profileCard, LocaleController.getString(R.string.NodesLastName));

        addFieldLabel(context, profileCard, LocaleController.getString(R.string.NodesBio));
        bioField = addField(context, profileCard, LocaleController.getString(R.string.NodesBio));

        content.addView(profileCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 1));

        // Privacy section
        addSectionHeader(context, content, LocaleController.getString(R.string.NodesShowMainProfile));
        content.addView(buildPrivacySelector(context, true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 1));

        addSectionHeader(context, content, LocaleController.getString(R.string.NodesShareContact));
        content.addView(buildPrivacySelector(context, false), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        sv.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        ((FrameLayout) fragmentView).addView(sv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        loadProfile();
        return fragmentView;
    }

    private void addFieldLabel(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        tv.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(2));
        parent.addView(tv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private EditText addField(Context context, LinearLayout parent, String hint) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setTextSize(16);
        et.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        et.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        et.setBackground(null);
        et.setSingleLine();
        parent.addView(et, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));
        return et;
    }

    private void addSectionHeader(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        tv.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
        parent.addView(tv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private View buildPrivacySelector(Context context, boolean isMainProfile) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        String[] options = {
                LocaleController.getString(R.string.NodesPrivacyEverybody),
                LocaleController.getString(R.string.NodesPrivacyContacts),
                LocaleController.getString(R.string.NodesPrivacyNobody)
        };
        int[] values = {TL_nodes.PRIVACY_EVERYBODY, TL_nodes.PRIVACY_CONTACTS, TL_nodes.PRIVACY_NOBODY};

        for (int i = 0; i < options.length; i++) {
            final int val = values[i];
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
            row.setClickable(true);
            row.setBackground(Theme.getSelectorDrawable(false));

            android.widget.RadioButton rb = new android.widget.RadioButton(context);
            int curVal = isMainProfile ? showMainProfile : shareContact;
            rb.setChecked(curVal == val);
            rb.setClickable(false);
            rb.setFocusable(false);
            row.addView(rb, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 12, 0));

            TextView tv = new TextView(context);
            tv.setText(options[i]);
            tv.setTextSize(16);
            tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            row.addView(tv);

            final android.widget.RadioButton finalRb = rb;
            row.setOnClickListener(v -> {
                if (isMainProfile) showMainProfile = val;
                else shareContact = val;
                // Refresh all radio states in parent
                for (int j = 0; j < card.getChildCount(); j++) {
                    View child = card.getChildAt(j);
                    if (child instanceof LinearLayout) {
                        View rbChild = ((LinearLayout) child).getChildAt(0);
                        if (rbChild instanceof android.widget.RadioButton) {
                            // Set checked based on tag
                            Object tag = child.getTag();
                            if (tag instanceof Integer) {
                                ((android.widget.RadioButton) rbChild).setChecked((Integer) tag == val);
                            }
                        }
                    }
                }
                finalRb.setChecked(true);
            });
            row.setTag(val);
            card.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        return card;
    }

    private void loadProfile() {
        TL_nodes.TL_nodes_getNodeProfile req = new TL_nodes.TL_nodes_getNodeProfile();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodeProfile) {
                profile = (TL_nodes.TL_nodeProfile) res;
                bindProfile();
            }
            // If no profile yet, fields stay empty — user fills from scratch
        }));
    }

    private void bindProfile() {
        if (profile == null) return;
        if (!TextUtils.isEmpty(profile.first_name)) firstNameField.setText(profile.first_name);
        if (!TextUtils.isEmpty(profile.last_name)) lastNameField.setText(profile.last_name);
        if (!TextUtils.isEmpty(profile.bio)) bioField.setText(profile.bio);
        showMainProfile = profile.show_main_profile;
        shareContact = profile.share_contact;
    }

    private void doSave() {
        TL_nodes.TL_nodes_updateNodeProfile req = new TL_nodes.TL_nodes_updateNodeProfile();
        req.node_id = nodeId;
        req.show_main_profile = showMainProfile;
        req.share_contact = shareContact;

        String fn = firstNameField.getText().toString().trim();
        String ln = lastNameField.getText().toString().trim();
        String bio = bioField.getText().toString().trim();

        if (!TextUtils.isEmpty(fn)) { req.flags |= 1; req.first_name = fn; }
        if (!TextUtils.isEmpty(ln)) { req.flags |= 2; req.last_name = ln; }
        if (!TextUtils.isEmpty(bio)) { req.flags |= 4; req.bio = bio; }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodeProfile) {
                profile = (TL_nodes.TL_nodeProfile) res;
                finishFragment();
            } else {
                if (getParentActivity() != null) {
                    new AlertDialog.Builder(getParentActivity())
                            .setTitle(LocaleController.getString(R.string.AppName))
                            .setMessage(err != null ? err.text : "Error saving profile")
                            .setPositiveButton(LocaleController.getString(R.string.OK), null)
                            .show();
                }
            }
        }));
    }
}
