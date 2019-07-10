package android.support.test.uiautomator;

import android.app.UiAutomation;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.script.ScriptApplication;
import com.cxplan.projection.mediate.script.domain.ScriptRect;
import com.cxplan.projection.mediate.script.domain.ViewNode;

/**
 * @author kenny
 */
public class CXScriptHelper {

    private static String TAG = Constant.TAG_PREFIX + "helper";

    /**
     * Retrieve view node according to specified coordinates.
     * Depending on the layout, some app may have multiple nodes matching it,
     * the best matching refers to rules below :
     * 1. minimum area
     * 2. clickable.
     *
     * @param x the x coordinate.
     * @param y ths y coordinate.
     * @return return the component covering the specified coordinates,
     * a null value will be returned if there is no component found.
     */
    public static AccessibilityNodeInfo findNode(int x, int y) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        AccessibilityNodeInfo[] rootNodes = device.getWindowRoots();
        if (rootNodes == null || rootNodes.length == 0) {
            return null;
        }

        NodeListener nodeListener = new NodeListener();
        for (AccessibilityNodeInfo node : rootNodes) {
            boolean result = findNode(node, x, y, nodeListener);
        }

        return nodeListener.foundNode;
    }

    public static boolean findNode(AccessibilityNodeInfo node, int x, int y, IFindNodeListener listener) {
        if (node == null || !node.isVisibleToUser()) {
            return false;
        }

        Rect rect = getVisibleBounds(node);
        if (!rect.contains(x, y)) {
            return false;
        }

        int childCount = node.getChildCount();
        boolean found = false;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            boolean tmpResult = findNode(childNode, x, y, listener);
            found |= tmpResult;

            /*if (tmpResult) {
                LogUtil.i(TAG, "node[" + childNode.getChildCount() + "](" + (result != null ? "yes" : "no")
                        + "[index=" + i + ", text=" + childNode.getText() + ", class=" + childNode.getClassName() + ", desc=" + childNode.getContentDescription() +
                        ",rect=" + tmpRect.toString() + "]");
            }*/
        }
        if (found) {
            return true;
        }

        listener.onFoundNode(node);
        return true;
    }

    public static UiObject2 buildObjectFromNode(AccessibilityNodeInfo node) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        return new UiObject2(device, null, node);
    }

    public static Rect getVisibleBounds(AccessibilityNodeInfo node) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        return AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, device.getDisplayWidth(), device.getDisplayHeight());
    }

    public static ViewNode convertViewModel(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        ViewNode vn = new ViewNode();
        vn.setText(normalCharSequence(node.getText()));
        vn.setResourceId(node.getViewIdResourceName());
        vn.setClassName(normalCharSequence(node.getClassName()));
        vn.setPackageName(normalCharSequence(node.getPackageName()));
        vn.setContentDesc(normalCharSequence(node.getContentDescription()));
        vn.setCheckable(node.isCheckable());
        vn.setChecked(node.isChecked());
        vn.setClickable(node.isClickable());
        vn.setEnabled(node.isEnabled());
        vn.setFocusable(node.isFocusable());
        vn.setFocused(node.isFocused());
        vn.setScrollable(node.isScrollable());
        vn.setLongClickable(node.isLongClickable());
        vn.setPassword(node.isPassword());
        vn.setSelected(node.isSelected());
        vn.setEditable(node.isEditable());
        vn.setVisible(node.isVisibleToUser());

        Rect rect = CXScriptHelper.getVisibleBounds(node);
        ScriptRect sr = new ScriptRect(rect.left, rect.top, rect.right, rect.bottom);
        vn.setBound(sr);

        return vn;
    }

    /**
     * Return current UI automation instance.
     */
    public static UiAutomation getUiAutomation() {
        return ScriptApplication.getInstance().getDevice().getUiAutomation();
    }

    private static String normalCharSequence(CharSequence content) {
        return content == null ? null : content.toString();
    }

    public interface IFindNodeListener {
        void onFoundNode(AccessibilityNodeInfo node);
    }

    private static class NodeListener implements CXScriptHelper.IFindNodeListener {
        AccessibilityNodeInfo foundNode;
        int resultWidth, resultHeight;

        @Override
        public void onFoundNode(AccessibilityNodeInfo node) {
            Rect tmpRect = CXScriptHelper.getVisibleBounds(node);

            int tmpWidth = tmpRect.width();
            int tmpHeight = tmpRect.height();
            if (foundNode != null) {
                if (tmpWidth * tmpHeight < (resultWidth * resultHeight)) {
                    foundNode = node;
                    resultWidth = tmpWidth;
                    resultHeight = tmpHeight;
                } else {
                    if ((node.isClickable() || node.isLongClickable() || node.isCheckable())
                            && !foundNode.isClickable() && !foundNode.isLongClickable() && !foundNode.isCheckable()) {
                        foundNode = node;
                        resultWidth = tmpWidth;
                        resultHeight = tmpHeight;
                    }
                }
            } else {
                foundNode = node;
                resultWidth = tmpWidth;
                resultHeight = tmpHeight;
            }
        }
    }
}
