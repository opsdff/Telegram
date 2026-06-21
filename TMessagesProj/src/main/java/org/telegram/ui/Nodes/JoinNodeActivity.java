package org.telegram.ui.Nodes;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class JoinNodeActivity extends BaseFragment {

    private EditText linkField;
    private TL_nodes.TL_nodes_nodePreview previewData;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesJoinExistingNode));
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
                else if (id == 1) checkLink();
            }
        });
        actionBar.createMenu().addItem(1, R.drawable.ic_ab_done);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));

        TextView sectionLabel = new TextView(context);
        sectionLabel.setText(LocaleController.getString(R.string.NodesNodeLink));
        sectionLabel.setTextSize(13);
        sectionLabel.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        content.addView(sectionLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        linkField = new EditText(context);
        linkField.setHint("https://t.me/n/...");
        linkField.setTextSize(16);
        linkField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        linkField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        linkField.setBackground(null);
        linkField.setSingleLine();
        content.addView(linkField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        TextView hint = new TextView(context);
        hint.setText(LocaleController.getString(R.string.NodesNodeLinkHint));
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        content.addView(hint, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ((FrameLayout) fragmentView).addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        return fragmentView;
    }

    private void checkLink() {
        String rawLink = linkField.getText().toString().trim();
        if (TextUtils.isEmpty(rawLink)) {
            AndroidUtilities.shakeView(linkField);
            return;
        }
        // Extract slug from t.me/n/<slug> or raw slug
        String slug = rawLink;
        if (slug.contains("/n/")) {
            slug = slug.substring(slug.indexOf("/n/") + 3);
        }
        if (slug.startsWith("+") || slug.startsWith("https://t.me/+")) {
            // Private invite link → join directly
            String finalSlug = slug.startsWith("https") ? slug.substring(slug.lastIndexOf("/") + 1) : slug.substring(1);
            joinNode("+" + finalSlug);
            return;
        }
        String finalSlug = slug;
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_resolveNode req = new TL_nodes.TL_nodes_resolveNode();
        req.link = finalSlug;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_nodePreview) {
                previewData = (TL_nodes.TL_nodes_nodePreview) res;
                showJoinPreview(finalSlug);
            } else {
                String msg = (err != null && !TextUtils.isEmpty(err.text))
                        ? LocaleController.getString(R.string.NodesLinkInvalid)
                        : LocaleController.getString(R.string.NodesLinkInvalid);
                if (getParentActivity() != null) {
                    new AlertDialog.Builder(getParentActivity())
                            .setTitle(LocaleController.getString(R.string.AppName))
                            .setMessage(msg)
                            .setPositiveButton(LocaleController.getString(R.string.OK), null)
                            .show();
                }
            }
        }));
    }

    private void showJoinPreview(String slug) {
        if (previewData == null || getParentActivity() == null) return;

        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity(), false, getResourceProvider());
        builder.setCustomView(buildPreviewView(getParentActivity(), slug));
        builder.show();
    }

    private View buildPreviewView(Context context, String slug) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(24));

        // Close button (top right)
        FrameLayout topRow = new FrameLayout(context);
        ImageView closeBtn = new ImageView(context);
        closeBtn.setImageResource(R.drawable.ic_close_white);
        closeBtn.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        closeBtn.setOnClickListener(v -> {
            // dismiss handled by BottomSheet
        });
        topRow.addView(closeBtn, LayoutHelper.createFrame(40, 40, Gravity.END | Gravity.TOP));
        layout.addView(topRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));

        // Node avatar
        BackupImageView avatarView = new BackupImageView(context);
        avatarView.setRoundRadius(AndroidUtilities.dp(20));
        AvatarDrawable ad = new AvatarDrawable();
        ad.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        avatarView.setImageDrawable(ad);
        layout.addView(avatarView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 12));

        // Node title
        TextView title = new TextView(context);
        title.setText(previewData.node.title);
        title.setTextSize(20);
        title.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setGravity(Gravity.CENTER);
        layout.addView(title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

        // Members count
        TextView membersText = new TextView(context);
        membersText.setText(LocaleController.formatPluralString("Members", previewData.node.members_count));
        membersText.setTextSize(14);
        membersText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        membersText.setGravity(Gravity.CENTER);
        layout.addView(membersText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 8));

        // Description
        if (!TextUtils.isEmpty(previewData.node.description)) {
            TextView desc = new TextView(context);
            desc.setText(previewData.node.description);
            desc.setTextSize(14);
            desc.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
            desc.setGravity(Gravity.CENTER);
            layout.addView(desc, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));
        }

        // Member avatars (up to 5)
        if (!previewData.members.isEmpty()) {
            LinearLayout avatarsRow = new LinearLayout(context);
            avatarsRow.setOrientation(LinearLayout.HORIZONTAL);
            avatarsRow.setGravity(Gravity.CENTER);
            int max = Math.min(previewData.members.size(), 5);
            for (int i = 0; i < max; i++) {
                BackupImageView av = new BackupImageView(context);
                av.setRoundRadius(AndroidUtilities.dp(18));
                AvatarDrawable ad2 = new AvatarDrawable();
                ad2.setInfo(previewData.members.get(i));
                av.setForUserOrChat(previewData.members.get(i), ad2);
                avatarsRow.addView(av, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL, i > 0 ? -8 : 0, 0, 0, 0));
            }
            layout.addView(avatarsRow, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 4));

            // Names
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < Math.min(max, 3); i++) {
                if (i > 0) names.append(", ");
                names.append(previewData.members.get(i).first_name);
            }
            TextView namesView = new TextView(context);
            namesView.setText(names.toString());
            namesView.setTextSize(13);
            namesView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
            namesView.setGravity(Gravity.CENTER);
            layout.addView(namesView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));
        }

        // Join button
        TextView joinBtn = new TextView(context);
        joinBtn.setText(LocaleController.getString(R.string.NodesJoinNode));
        joinBtn.setTextSize(16);
        joinBtn.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        joinBtn.setTextColor(android.graphics.Color.WHITE);
        joinBtn.setGravity(Gravity.CENTER);
        joinBtn.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(8),
                Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        joinBtn.setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
        joinBtn.setOnClickListener(v -> joinNode(slug));
        layout.addView(joinBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return layout;
    }

    private void joinNode(String slug) {
        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_joinNode req = new TL_nodes.TL_nodes_joinNode();
        req.link = slug;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_fullNode) {
                TL_nodes.TL_nodes_fullNode fn = (TL_nodes.TL_nodes_fullNode) res;
                NodeController.getInstance(currentAccount).setCurrentFullNode(fn);
                NodeController.getInstance(currentAccount).invalidateMyNodes();

                presentFragment(new NodeChatListActivity(fn.node.id), true);
            } else {
                String msg = (err != null && "NODE_LINK_INVALID".equals(err.text))
                        ? LocaleController.getString(R.string.NodesLinkInvalid)
                        : (err != null ? err.text : "Error");
                if (getParentActivity() != null) {
                    new AlertDialog.Builder(getParentActivity())
                            .setTitle(LocaleController.getString(R.string.AppName))
                            .setMessage(msg)
                            .setPositiveButton(LocaleController.getString(R.string.OK), null)
                            .show();
                }
            }
        }));
    }
}
