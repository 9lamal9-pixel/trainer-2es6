/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

class TreeUtil {
    TreeUtil() {
    }

    static <T> int getExpandedDescendantCount(TreeItem<T> treeItem, boolean bl) {
        if (treeItem == null) {
            return 0;
        }
        if (treeItem.isLeaf()) {
            return 1;
        }
        return treeItem.getExpandedDescendentCount(bl);
    }

    static int updateExpandedItemCount(TreeItem treeItem, boolean bl, boolean bl2) {
        if (treeItem == null) {
            return 0;
        }
        if (!treeItem.isExpanded()) {
            return 1;
        }
        int n = TreeUtil.getExpandedDescendantCount(treeItem, bl);
        if (!bl2) {
            --n;
        }
        return n;
    }

    static <T> TreeItem<T> getItem(TreeItem<T> treeItem, int n, boolean bl) {
        if (treeItem == null) {
            return null;
        }
        if (n == 0) {
            return treeItem;
        }
        if (n >= TreeUtil.getExpandedDescendantCount(treeItem, bl)) {
            return null;
        }
        ObservableList<TreeItem<T>> observableList = treeItem.getChildren();
        if (observableList == null) {
            return null;
        }
        int n2 = n - 1;
        int n3 = observableList.size();
        for (int i = 0; i < n3; ++i) {
            TreeItem treeItem2 = (TreeItem)observableList.get(i);
            if (n2 == 0) {
                return treeItem2;
            }
            if (treeItem2.isLeaf() || !treeItem2.isExpanded()) {
                --n2;
                continue;
            }
            int n4 = TreeUtil.getExpandedDescendantCount(treeItem2, bl);
            if (n2 >= n4) {
                n2 -= n4;
                continue;
            }
            TreeItem<T> treeItem3 = TreeUtil.getItem(treeItem2, n2, bl);
            if (treeItem3 != null) {
                return treeItem3;
            }
            --n2;
        }
        return null;
    }

    static <T> int getRow(TreeItem<T> treeItem, TreeItem<T> treeItem2, boolean bl, boolean bl2) {
        if (treeItem == null) {
            return -1;
        }
        if (bl2 && treeItem.equals(treeItem2)) {
            return 0;
        }
        int n = 0;
        TreeItem<T> treeItem3 = treeItem;
        TreeItem<T> treeItem4 = treeItem.getParent();
        boolean bl3 = false;
        while (!treeItem3.equals(treeItem2) && treeItem4 != null) {
            if (!treeItem4.isExpanded()) {
                bl3 = true;
                break;
            }
            ObservableList observableList = treeItem4.children;
            int n2 = observableList.indexOf(treeItem3);
            for (int i = n2 - 1; i > -1; --i) {
                TreeItem treeItem5 = (TreeItem)observableList.get(i);
                if (treeItem5 == null) continue;
                n += TreeUtil.getExpandedDescendantCount(treeItem5, bl);
                if (!treeItem5.equals(treeItem2)) continue;
                if (!bl2) {
                    return -1;
                }
                return n;
            }
            treeItem3 = treeItem4;
            if ((treeItem4 = treeItem4.getParent()) == null && !treeItem3.equals(treeItem2)) {
                return -1;
            }
            ++n;
        }
        return treeItem4 == null && n == 0 || bl3 ? -1 : (bl2 ? n : n - 1);
    }
}

