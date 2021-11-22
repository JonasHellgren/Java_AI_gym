package java_ai_gym.models_pong;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;

public class SearchTreePanel extends JPanel {

    DefaultMutableTreeNode root;
    JTree tree;
    JLabel label;
    VisitedStatesBuffer vsb;

    public SearchTreePanel() {
        this.tree = tree;


    }


    public void createTreeWithOnlyRootNode(int panelW, int panelH) {
        this.root = new DefaultMutableTreeNode("VSB");
        this.tree = new JTree(root);

        /*
        DefaultMutableTreeNode parent1 = new DefaultMutableTreeNode("Andhra Pradesh");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("Vijayawada");
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Vizag");
        DefaultMutableTreeNode parent2 = new DefaultMutableTreeNode("Telangana");
        DefaultMutableTreeNode  child2 = new DefaultMutableTreeNode("Hyderabad");
        DefaultMutableTreeNode  child3= new DefaultMutableTreeNode("Hyderadfdfbbad");
        DefaultMutableTreeNode  child4 = new DefaultMutableTreeNode("ffdb");

        // Adding child nodes to parent
        parent1.add(child);
        parent1.add(child1);
        parent2.add(child2);
        parent2.add(child3);
        parent2.add(child4);


        // Adding parent nodes to root
        root.add(parent1);
        root.add(parent2);  */

        JScrollPane spRight = new JScrollPane(tree);
        spRight.setBounds(new Rectangle(0, 0, panelW, panelH));
        add(spRight);

    }

    public void createLabel(int panelW, int panelH, String text) {

        this.label = new JLabel();
        this.label.setBounds(panelW / 2, 20, 100, 20);
        this.label.setForeground(Color.CYAN);
        this.label.setVisible(true);
        this.label.setText(text);
        add(this.label);
    }

    public void createTreeFromVisitedStatesBuffer(VisitedStatesBuffer vsb) {
        this.vsb = vsb;
        addChildNodesRecursive(this.root, "start");
    }


    public void addChildNodesRecursive(DefaultMutableTreeNode parent, String parentId) {
        List<StateExperience> experiences = vsb.getExperienceList(parentId);
        for (StateExperience exp : experiences) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(exp.idNewState);
            parent.add(child);
            addChildNodesRecursive(child,exp.idNewState);
        }

    }


/*
    @Override
    public void paint(Graphics g) {
        super.paint(g);  //cleans the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //plotBall(g2d);
        //plotRacket(g2d);
        //plotBorder(g2d);
        //textBallStates(g2d,carPosition.x, carPosition.y,velocity);
        //textRacketStates
    }
*/
}
