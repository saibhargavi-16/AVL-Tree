import java.util.*;
import static java.lang.Math.*;
import java.io.PrintStream;
import java.util.function.Function;

class BSTNode {
    int data;
    int h;
    BSTNode left, right;
    public BSTNode(int x){
        this.data = x;
        this.h = 1;
        this.left = this.right = null;
    }
}

class Solution {
    BSTNode insertNode(BSTNode root, int x){
        if(root == null)
            return new BSTNode(x);
        if(x < root.data){
            root.left = insertNode(root.left, x);
        } else {
            root.right = insertNode(root.right, x);
        }

        updateHeight(root);

        int bf = getBF(root);

        // Left Heavy
        if(bf == +2){
            if(getBF(root.left) >= 0){ // LL
                root = rightRotate(root);
            } else { // LR
                root.left = leftRotate(root.left);
                root = rightRotate(root);
            }
        }

        // Right Heavy
        else if(bf == -2){
            if(getBF(root.right) <= 0){ // RR
                root = leftRotate(root);
            } else { // RL
                root.right = rightRotate(root.right);
                root = leftRotate(root);
            }
        }

        return root;
    }

    BSTNode leftRotate(BSTNode root){
        BSTNode newRoot = root.right;
        BSTNode t2 = newRoot.left;

        root.right = t2;
        updateHeight(root);

        newRoot.left = root;
        updateHeight(newRoot);

        return newRoot;
    }

    BSTNode rightRotate(BSTNode root){
        BSTNode newRoot = root.left;
        BSTNode t2 = newRoot.right;

        root.left = t2;
        updateHeight(root);

        newRoot.right = root;
        updateHeight(newRoot);

        return newRoot;
    }

    int getBF(BSTNode root){
        return (root.left == null ? 0 : root.left.h) - (root.right == null ? 0 : root.right.h);
    }

    void updateHeight(BSTNode root){
        root.h = 1 + max((root.left == null ? 0 : root.left.h), (root.right == null ? 0 : root.right.h));
    }
}

class Main {
    static Scanner sc = new Scanner(System.in);
    static Solution sol = new Solution();
    public static void main(String[] args) {
        BSTNode root = null;
        TreePrinter<BSTNode> printer = new TreePrinter<>(n -> "" + n.data, n -> n.left, n -> n.right);
        System.out.println("Enter integers to insert into AVL Tree: ");
        while(sc.hasNextInt()){
            root = sol.insertNode(root, sc.nextInt());
            printer.printTree(root);
            System.out.println();
        }
    }
}
class TreePrinter<T> {
    private Function<T, String> getLabel;
    private Function<T, T> getLeft;
    private Function<T, T> getRight;

    private PrintStream outStream = System.out;
    private int hspace = 2;

    public TreePrinter(Function<T, String> getLabel, Function<T, T> getLeft, Function<T, T> getRight) {
        this.getLabel = getLabel;
        this.getLeft = getLeft;
        this.getRight = getRight;
    }

    public void printTree(T root) {
        List<TreeLine> treeLines = buildTreeLines(root);
        printTreeLines(treeLines);
    }

    private void printTreeLines(List<TreeLine> treeLines) {
        if (treeLines.size() > 0) {
            int minLeftOffset = minLeftOffset(treeLines);
            int maxRightOffset = maxRightOffset(treeLines);
            for (TreeLine treeLine : treeLines) {
                int leftSpaces = -(minLeftOffset - treeLine.leftOffset);
                int rightSpaces = maxRightOffset - treeLine.rightOffset;
                outStream.println(spaces(leftSpaces) + treeLine.line + spaces(rightSpaces));
            }
        }
    }

    private List<TreeLine> buildTreeLines(T root) {
        if (root == null) return Collections.emptyList();
        else {
            String rootLabel = getLabel.apply(root);
            List<TreeLine> leftTreeLines = buildTreeLines(getLeft.apply(root));
            List<TreeLine> rightTreeLines = buildTreeLines(getRight.apply(root));

            int leftCount = leftTreeLines.size();
            int rightCount = rightTreeLines.size();
            int minCount = Math.min(leftCount, rightCount);
            int maxCount = Math.max(leftCount, rightCount);

            int maxRootSpacing = 0;
            for (int i = 0; i < minCount; i++) {
                int spacing = leftTreeLines.get(i).rightOffset - rightTreeLines.get(i).leftOffset;
                if (spacing > maxRootSpacing) maxRootSpacing = spacing;
            }

            int rootSpacing = maxRootSpacing + hspace;
            if (rootSpacing % 2 == 0) rootSpacing++;

            List<TreeLine> allTreeLines = new ArrayList<>();
            String renderedRootLabel = rootLabel.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");

            allTreeLines.add(new TreeLine(rootLabel, -(renderedRootLabel.length() - 1) / 2, renderedRootLabel.length() / 2));

            int leftTreeAdjust = 0;
            int rightTreeAdjust = 0;

            if (leftTreeLines.isEmpty()) {
                if (!rightTreeLines.isEmpty()) {
                    allTreeLines.add(new TreeLine("\\", 1, 1));
                    rightTreeAdjust = 2;
                }
            } else if (rightTreeLines.isEmpty()) {
                allTreeLines.add(new TreeLine("/", -1, -1));
                leftTreeAdjust = -2;
            } else {
                if (rootSpacing == 1) {
                    allTreeLines.add(new TreeLine("/ \\", -1, 1));
                    rightTreeAdjust = 2;
                    leftTreeAdjust = -2;
                } else {
                    for (int i = 1; i < rootSpacing; i += 2) {
                        String branches = "/" + spaces(i) + "\\";
                        allTreeLines.add(new TreeLine(branches, -((i + 1) / 2), (i + 1) / 2));
                    }
                    rightTreeAdjust = (rootSpacing / 2) + 1;
                    leftTreeAdjust = -((rootSpacing / 2) + 1);
                }
            }

            for (int i = 0; i < maxCount; i++) {
                TreeLine leftLine, rightLine;
                if (i >= leftTreeLines.size()) {
                    rightLine = rightTreeLines.get(i);
                    rightLine.leftOffset += rightTreeAdjust;
                    rightLine.rightOffset += rightTreeAdjust;
                    allTreeLines.add(rightLine);
                } else if (i >= rightTreeLines.size()) {
                    leftLine = leftTreeLines.get(i);
                    leftLine.leftOffset += leftTreeAdjust;
                    leftLine.rightOffset += leftTreeAdjust;
                    allTreeLines.add(leftLine);
                } else {
                    leftLine = leftTreeLines.get(i);
                    rightLine = rightTreeLines.get(i);
                    int adjustedRootSpacing = (rootSpacing == 1 ? 3 : rootSpacing);
                    TreeLine combined = new TreeLine(leftLine.line + spaces(adjustedRootSpacing - leftLine.rightOffset + rightLine.leftOffset) + rightLine.line,
                            leftLine.leftOffset + leftTreeAdjust, rightLine.rightOffset + rightTreeAdjust);
                    allTreeLines.add(combined);
                }
            }

            return allTreeLines;
        }
    }

    private static int minLeftOffset(List<TreeLine> treeLines) {
        return treeLines.stream().mapToInt(l -> l.leftOffset).min().orElse(0);
    }

    private static int maxRightOffset(List<TreeLine> treeLines) {
        return treeLines.stream().mapToInt(l -> l.rightOffset).max().orElse(0);
    }

    private static String spaces(int n) {
        return String.join("", Collections.nCopies(n, " "));
    }

    private static class TreeLine {
        String line;
        int leftOffset;
        int rightOffset;

        TreeLine(String line, int leftOffset, int rightOffset) {
            this.line = line;
            this.leftOffset = leftOffset;
            this.rightOffset = rightOffset;
        }
    }
}
