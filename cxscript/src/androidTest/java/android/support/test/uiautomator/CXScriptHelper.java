package android.support.test.uiautomator;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.cxscript.ScriptApplication;
import com.cxplan.projection.mediate.Constant;

/**
 * @author kenny
 */
public class CXScriptHelper {

    private static String TAG = Constant.TAG_PREFIX + "helper";

    public static UiObject2 findObject(int x, int y) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        AccessibilityNodeInfo[] rootNodes = device.getWindowRoots();
        if (rootNodes == null || rootNodes.length == 0) {
            return null;
        }

        int i = 0;
        for (AccessibilityNodeInfo node : rootNodes) {
            AccessibilityNodeInfo result = findNode(node, x, y);
//            LogUtil.i(TAG, "node(" + (result != null ? "yes" : "no") + "[index=" + i + ", text=" + node.getText() + ", class=" + node.getClassName() + ", desc=" + node.getContentDescription() + "]");
            if (result != null) {
                return new UiObject2(device, null, result);
            }
            i++;
        }

        return null;
    }

    public static AccessibilityNodeInfo findNode(int x, int y) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        AccessibilityNodeInfo[] rootNodes = device.getWindowRoots();
        if (rootNodes == null || rootNodes.length == 0) {
            return null;
        }

        int i = 0;
        for (AccessibilityNodeInfo node : rootNodes) {
            AccessibilityNodeInfo result = findNode(node, x, y);
//            LogUtil.i(TAG, "node[" + node.getChildCount() + "](" + (result != null ? "yes" : "no") + "[index=" + i + ", text=" + node.getText() + ", class=" + node.getClassName() + ", desc=" + node.getContentDescription() + "]");
            if (result != null) {
                return result;
            }
            i++;
        }

        return null;
    }

    public static AccessibilityNodeInfo findNode(AccessibilityNodeInfo node, int x, int y) {
        if (node == null || !node.isVisibleToUser()) {
            return null;
        }

        Rect rect = getVisibleBounds(node);
        if (!rect.contains(x, y)) {
            return null;
        }

        int childCount = node.getChildCount();
        boolean found = false;
        AccessibilityNodeInfo result = null;
        int resultWidth = 0, resultHeight = 0;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            AccessibilityNodeInfo tmpResult = findNode(childNode, x, y);
//            if(status) {
//                LogUtil.i(TAG, "node[" + childNode.getChildCount() + "](" + (result != null ? "yes" : "no")
//                        + "[index=" + i + ", text=" + childNode.getText() + ", class=" + childNode.getClassName() + ", desc=" + childNode.getContentDescription() +
//                        ",rect=" + getVisibleBounds(childNode).toString() + "]");
//            }
            if (tmpResult != null) {
                found = true;
                Rect tmpRect = getVisibleBounds(tmpResult);
                int tmpWidth = tmpRect.width();
                int tmpHeight = tmpRect.height();
                if (result != null) {
                    if (tmpWidth * tmpHeight < (resultWidth * resultHeight)) {
                        result = tmpResult;
                        resultWidth = tmpWidth;
                        resultHeight = tmpHeight;
                    }
                } else {
                    result = tmpResult;
                    resultWidth = tmpWidth;
                    resultHeight = tmpHeight;
                }
            }
        }
        if (found) {
            return result;
        }

        return node;
    }

    private static void printSubNode(AccessibilityNodeInfo node, int depth) {
        if (node.getChildCount() == 0) {
            return;
        }

        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (!childNode.isVisibleToUser()) {
                continue;
            }
            Rect rect = getVisibleBounds(childNode);

            LogUtil.i(TAG, "------node[" + depth + "]([index=" + i + ", text=" + childNode.getText() + ", class=" + childNode.getClassName()
                    + ", desc=" + childNode.getContentDescription()
            + ", bound=" + rect.toString()  + "]");
            if (childNode.getChildCount() > 0) {
                printSubNode(childNode, depth++);
            }
        }
    }

    public static UiObject2 buildObjectFromNode(AccessibilityNodeInfo node) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        return new UiObject2(device, null, node);
    }

    private static Rect getVisibleBounds(AccessibilityNodeInfo node) {
        UiDevice device = ScriptApplication.getInstance().getDevice();
        return AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, device.getDisplayWidth(), device.getDisplayHeight());
    }
}
