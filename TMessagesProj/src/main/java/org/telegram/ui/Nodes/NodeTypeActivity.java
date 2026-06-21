package org.telegram.ui.Nodes;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

public class NodeTypeActivity extends BaseFragment {

    private final long nodeId;
    private boolean isPublic = true;
    private EditText linkField;
    private TextView linkError;
    private String validatedLink = null;

    public NodeTypeActivity(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.NodesNodeSettingsTitle));
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

        LinearLayout scroll = new LinearLayout(context);
        scroll.setOrientation(LinearLayout.VERTICAL);

        // Node type section
        addSectionHeader(context, scroll, LocaleController.getString(R.string.NodesNodeType));

        // Public option
        LinearLayout publicOpt = buildRadioOption(context, scroll, LocaleController.getString(R.string.NodesPublicNode),
                LocaleController.getString(R.string.NodesPublicNodeDesc), true);

        // Private option
        LinearLayout privateOpt = buildRadioOption(context, scroll, LocaleController.getString(R.string.NodesPrivateNode),
                LocaleController.getString(R.string.NodesPrivateNodeDesc), false);

        // Public link section (visible when public)
        View publicLinkSection = buildPublicLinkSection(context);
        scroll.addView(publicLinkSection, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ((FrameLayout) fragmentView).addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private void addSectionHeader(Context context, LinearLayout parent, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        tv.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        tv.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        parent.addView(tv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private LinearLayout buildRadioOption(Context context, LinearLayout parent, String title, String desc, boolean value) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        row.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        RadioButton rb = new RadioButton(context);
        rb.setChecked(isPublic == value);
        row.addView(rb, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 4, 12, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextSize(15);
        tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textCol.addView(tv);

        TextView dv = new TextView(context);
        dv.setText(desc);
        dv.setTextSize(13);
        dv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        textCol.addView(dv);

        row.addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
        row.setOnClickListener(v -> {
            isPublic = value;
            // refresh radio states
            rb.setChecked(true);
            if (linkField != null) {
                linkField.setVisibility(value ? View.VISIBLE : View.GONE);
            }
        });

        parent.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        return row;
    }

    private View buildPublicLinkSection(Context context) {
        LinearLayout section = new LinearLayout(context);
        section.setOrientation(LinearLayout.VERTICAL);

        addSectionHeader(context, section, LocaleController.getString(R.string.NodesPublicLink));

        LinearLayout linkRow = new LinearLayout(context);
        linkRow.setOrientation(LinearLayout.HORIZONTAL);
        linkRow.setGravity(Gravity.CENTER_VERTICAL);
        linkRow.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linkRow.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));

        TextView prefix = new TextView(context);
        prefix.setText("t.me/n/");
        prefix.setTextSize(16);
        prefix.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        linkRow.addView(prefix, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        linkField = new EditText(context);
        linkField.setHint("link");
        linkField.setTextSize(16);
        linkField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        linkField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        linkField.setBackground(null);
        linkField.setSingleLine();
        linkField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validatedLink = null;
                checkLink(s.toString());
            }
        });
        linkRow.addView(linkField, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));
        section.addView(linkRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        linkError = new TextView(context);
        linkError.setTextSize(13);
        linkError.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
        linkError.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(4), AndroidUtilities.dp(16), 0);
        linkError.setVisibility(View.GONE);
        section.addView(linkError, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView hint = new TextView(context);
        hint.setText(LocaleController.getString(R.string.NodesPublicLinkHint));
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        hint.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(16));
        section.addView(hint, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return section;
    }

    private Runnable checkLinkRunnable;

    private void checkLink(String link) {
        if (link.length() < 5) return;
        AndroidUtilities.cancelRunOnUIThread(checkLinkRunnable);
        checkLinkRunnable = () -> {
            TL_nodes.TL_nodes_checkPublicLink req = new TL_nodes.TL_nodes_checkPublicLink();
            req.link = link;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                if (linkError == null) return;
                if (res instanceof org.telegram.tgnet.TLRPC.TL_boolTrue) {
                    linkError.setVisibility(View.GONE);
                    validatedLink = link;
                } else {
                    linkError.setVisibility(View.VISIBLE);
                    linkError.setText(LocaleController.getString(R.string.NodesLinkTaken));
                    validatedLink = null;
                }
            }));
        };
        AndroidUtilities.runOnUIThread(checkLinkRunnable, 600);
    }

    private void doSave() {
        if (isPublic && validatedLink == null) {
            String rawLink = linkField != null ? linkField.getText().toString().trim() : "";
            if (rawLink.length() < 5) {
                AndroidUtilities.shakeView(linkField);
                return;
            }
        }

        AlertDialog progress = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
        progress.showDelayed(300);

        TL_nodes.TL_nodes_setNodeType req = new TL_nodes.TL_nodes_setNodeType();
        req.node_id = nodeId;
        req.is_public = isPublic;
        if (isPublic && validatedLink != null) {
            req.flags |= 2;
            req.link = validatedLink;
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            progress.dismiss();
            if (res instanceof TL_nodes.TL_nodes_fullNode) {
                TL_nodes.TL_nodes_fullNode fn = (TL_nodes.TL_nodes_fullNode) res;
                NodeController.getInstance(currentAccount).setCurrentFullNode(fn);

                presentFragment(new NodeChatListActivity(fn.node.id), true);
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
