package ark.mobile.wiki.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class BinaryTree<E extends Comparable> {

    private class TreeNode{
        E data;
        TreeNode left;
        TreeNode right;

        TreeNode(){}

        TreeNode(E data) {
            this.data = data;
        }

    }

    private TreeNode root;

    public void addAll(Collection<E> col){
        Iterator<E> it = col.iterator();
        while(it.hasNext())
            add(it.next());
    }

    public void add(E element){
        if(root == null){
            root = new TreeNode(element);
            return;
        }
        TreeNode actNode = root;
        while(actNode != null){
            int comparition = element.compareTo(actNode.data);
            if(comparition == 0) return;
            if(comparition < 0){
                if(actNode.left == null){
                    actNode.left = new TreeNode(element);
                    break;
                }else
                    actNode = actNode.left;
            }else{
                if(actNode.right == null){
                    actNode.right = new TreeNode(element);
                    break;
                }else
                    actNode = actNode.right;
            }
        }
    }

    public ArrayList<E> toArrayList(){
        ArrayList<E> arrayList = new ArrayList<>();
        searchForNode(root, arrayList);
        return arrayList;
    }

    private void searchForNode(TreeNode node, ArrayList array){
        if(node.left  != null) searchForNode(node.left,  array);
        if(node.right != null) searchForNode(node.right, array);
        array.add(node.data);
    }

}
