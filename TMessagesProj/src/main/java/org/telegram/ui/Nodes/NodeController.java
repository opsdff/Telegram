package org.telegram.ui.Nodes;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.tl.TL_nodes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Singleton per-account manager for the current active Node context.
 * Keeps loaded nodes in memory and exposes convenience helpers.
 */
public class NodeController {

    private static final NodeController[] instances = new NodeController[UserConfig.MAX_ACCOUNT_COUNT];

    public static NodeController getInstance(int account) {
        if (instances[account] == null) {
            synchronized (NodeController.class) {
                if (instances[account] == null) {
                    instances[account] = new NodeController(account);
                }
            }
        }
        return instances[account];
    }

    private final int currentAccount;

    // Cached list of nodes the user belongs to
    private final ArrayList<TL_nodes.TL_node> myNodes = new ArrayList<>();
    private boolean myNodesLoaded = false;

    // Currently active full node (sidebar selection)
    private TL_nodes.TL_nodes_fullNode currentFullNode;

    private NodeController(int account) {
        this.currentAccount = account;
    }

    public int getCurrentAccount() {
        return currentAccount;
    }

    // ─── My nodes list ────────────────────────────────────────────────

    public ArrayList<TL_nodes.TL_node> getCachedNodes() {
        return myNodes;
    }

    public boolean isMyNodesLoaded() {
        return myNodesLoaded;
    }

    public void loadMyNodes(Runnable onDone) {
        TL_nodes.TL_nodes_getNodes req = new TL_nodes.TL_nodes_getNodes();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_nodes) {
                myNodes.clear();
                myNodes.addAll(((TL_nodes.TL_nodes_nodes) res).nodes);
                myNodesLoaded = true;
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.nodeListUpdated);
            }
            if (onDone != null) onDone.run();
        }));
    }

    public void invalidateMyNodes() {
        myNodesLoaded = false;
        myNodes.clear();
    }

    // ─── Active node ──────────────────────────────────────────────────

    public TL_nodes.TL_nodes_fullNode getCurrentFullNode() {
        return currentFullNode;
    }

    public TL_nodes.TL_node getCurrentNode() {
        return currentFullNode != null ? currentFullNode.node : null;
    }

    public void setCurrentFullNode(TL_nodes.TL_nodes_fullNode fn) {
        currentFullNode = fn;
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.activeNodeChanged);
    }

    public void clearCurrentNode() {
        currentFullNode = null;
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.activeNodeChanged);
    }

    public void loadFullNode(long nodeId, OnFullNodeLoaded callback) {
        TL_nodes.TL_nodes_getFullNode req = new TL_nodes.TL_nodes_getFullNode();
        req.node_id = nodeId;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            if (res instanceof TL_nodes.TL_nodes_fullNode) {
                TL_nodes.TL_nodes_fullNode fn = (TL_nodes.TL_nodes_fullNode) res;
                currentFullNode = fn;
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.activeNodeChanged);
                if (callback != null) callback.onLoaded(fn, null);
            } else {
                if (callback != null) callback.onLoaded(null, err);
            }
        }));
    }

    // ─── Role helpers ─────────────────────────────────────────────────

    /** Returns the display role (highest ord) for current user in the active node. */
    public TL_nodes.TL_nodeRole getMyTopRole() {
        if (currentFullNode == null) return null;
        TL_nodes.TL_nodeRole top = null;
        for (TL_nodes.TL_nodeRole r : currentFullNode.roles) {
            if (currentFullNode.my_role_ids.contains(r.id)) {
                if (top == null || r.ord > top.ord) top = r;
            }
        }
        return top;
    }

    public boolean hasPermission(long permBit) {
        if (currentFullNode == null) return false;
        for (TL_nodes.TL_nodeRole r : currentFullNode.roles) {
            if (currentFullNode.my_role_ids.contains(r.id)) {
                if ((r.permissions & permBit) != 0) return true;
            }
        }
        return false;
    }

    public boolean canCreateChats() {
        return hasPermission(TL_nodes.PERM_CREATE_OWN_CHATS);
    }

    public boolean isNodeOwner() {
        if (currentFullNode == null) return false;
        return currentFullNode.node != null && currentFullNode.node.is_owner;
    }

    // ─── Callback interface ───────────────────────────────────────────

    public interface OnFullNodeLoaded {
        void onLoaded(TL_nodes.TL_nodes_fullNode fullNode, org.telegram.tgnet.TLRPC.TL_error error);
    }
}
