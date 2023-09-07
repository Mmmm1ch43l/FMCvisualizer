import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Cube3D extends JPanel implements MouseListener, MouseMotionListener {
    //parameters
    private final double rotationSpeed = 0.008;
    private final double perspective = 1/15.;
    private final double xAngleStart = -.3;
    private final double yAngleStart = .2;
    private final double rotationThreshold = 1e-10;
    private static final int windowWidth = 800;
    private static final int windowHeight = 800;
    private double[][] basis = rotate(new double[][] {{1,0,0},{0,1,0},{0,0,1}}, xAngleStart, yAngleStart);
    // The rotation angles
    private double xAngle;
    private double yAngle;
    private double[][] basisT;
    // The colors of the faces
    private final Color[] colors = {
            Color.decode("#20000000"),//0: transparent gray
            Color.white,//1: white
            Color.yellow,//2: yellow
            Color.green,//3: green
            Color.decode("#0091ff"),//4: blue
            Color.red,//5: red
            Color.decode("#ff9500")};//6: orange
    private int[][] faceColors = {
            {0,0,0,0,0,0},//0: fully transparent
            {1,0,0,0,0,0},//1: white center
            {0,2,0,0,0,0},//2: yellow center
            {0,0,3,0,0,0},//3: green center
            {0,0,0,4,0,0},//4: blue center
            {0,0,0,0,5,0},//5: red center
            {0,0,0,0,0,6},//6: orange center
            {6,6,6,6,6,6},//7: fully orange
            {5,6,6,6,6,6} //8: orange with red top
    };
    private int[] cubeColors = {8,0,0,  0,1,0,  0,0,0,
                                0,3,0,  6,0,5,  0,4,0,
                                8,0,8,  0,2,0,  0,0,0};
    private int[] orientations = {3,0,0,  0,0,0,  0,0,0,
                                  0,0,0,  0,0,0,  0,0,0,
                                  2,0,2,  0,0,0,  0,0,0};
    private double[][] centerCoordinates;
    // The previous mouse position
    private int prevX;
    private int prevY;
    private double scale;

    public Cube3D() {
        // Add mouse listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        // Set the background color
        setBackground(Color.white);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Get the width and height of the panel
        int width = getWidth();
        int height = getHeight();
        // Get the graphics context
        Graphics2D g2d = (Graphics2D) g;
        // Set the rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Set the origin to the center of the panel
        g2d.translate(width / 2, height / 2);
        // Scale the cube to fit the panel
        scale = Math.min(width / .9, height / .9) / 12.0;
        basisT = rotate(basis, xAngle, yAngle);
    }

    private void drawCube(Graphics2D g2d, double[] centerCoordinate, int faceColor, int orientation) {
        double[][] cubeVertices = new double[8][];
        double[][][] faceVertices = new double[6][][];
        double[][] basisTemp = new double[3][];
        switch (orientation){
            case 0 -> {
                basisTemp[0] = basisT[0].clone();
                basisTemp[1] = basisT[1].clone();
                basisTemp[2] = basisT[2].clone();
            }
            case 1 -> {
                basisTemp[0] = negative(basisT[0]);
                basisTemp[1] = negative(basisT[1]);
                basisTemp[2] = negative(basisT[2]);
            }
            case 2 -> {
                basisTemp[0] = basisT[0].clone();
                basisTemp[1] = negative(basisT[2]);
                basisTemp[2] = basisT[1].clone();
            }
            case 3 -> {
                basisTemp[0] = negative(basisT[0]);
                basisTemp[1] = basisT[2].clone();
                basisTemp[2] = negative(basisT[1]);
            }
            case 4 -> {
                basisTemp[0] = negative(basisT[1]);
                basisTemp[1] = basisT[0].clone();
                basisTemp[2] = basisT[2].clone();
            }
            case 5 -> {
                basisTemp[0] = basisT[1].clone();
                basisTemp[1] = negative(basisT[0]);
                basisTemp[2] = negative(basisT[2]);
            }
        }
        int index;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    index = i+2*j+4*k;
                    cubeVertices[index] = centerCoordinate.clone();
                    cubeVertices[index][0] += ()
                }
            }
        }
        // Draw the faces of the cube
        // Get the average z value of the face
        double[] z = new double[6];
        for (int i = 0; i < 6; i++) {
            z[i] = (faceVertices[i][0][2] + faceVertices[i][1][2] + faceVertices[i][2][2] + faceVertices[i][3][2]) / 4.0;
        }
        int[] order = rank(z);
        for (int i = 0; i < 6; i++) {
            drawFace(g2d, faceColors[faceColor][order[i]], faceVertices[order[i]][0], faceVertices[order[i]][1], faceVertices[order[i]][2], faceVertices[order[i]][3]);
        }
    }

    private void drawFace(Graphics2D g2d, int color, double[] v1, double[] v2, double[] v3, double[] v4) {
        int[] xCords = new int[] {(int) (v1[0]*Math.exp(v1[2]*perspective)*scale),
                (int) (v2[0]*Math.exp(v1[2]*perspective)*scale),
                (int) (v3[0]*Math.exp(v1[2]*perspective)*scale),
                (int) (v4[0]*Math.exp(v1[2]*perspective)*scale)};
        int[] yCords = new int[] {(int) (v1[1]*Math.exp(v1[2]*perspective)*scale),
                (int) (v2[1]*Math.exp(v1[2]*perspective)*scale),
                (int) (v3[1]*Math.exp(v1[2]*perspective)*scale),
                (int) (v4[1]*Math.exp(v1[2]*perspective)*scale)};
        g2d.setColor(colors[color]);
        g2d.fillPolygon(xCords, yCords, 4);
        g2d.setColor(Color.darkGray);
        g2d.drawPolygon(xCords, yCords, 4);
    }

    public void mousePressed(MouseEvent e) {
        // Store the current mouse position
        prevX = e.getX();
        prevY = e.getY();
        //System.out.println("pressed");
    }

    public void mouseDragged(MouseEvent e) {
        // Update the rotation angles
        xAngle = rotationSpeed * (e.getX() - prevX);
        yAngle = rotationSpeed * (e.getY() - prevY);
        repaint();
        //System.out.print(".");
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {
        //reset orientation axis
        xAngle = 0;
        yAngle = 0;
        basis = deepClone(basisT);
        repaint();
        //System.out.println("\nreleased");
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public static void main(String[] args) {
        // Create a frame to display the panel
        JFrame frame = new JFrame("Cube3D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(new Cube3D());
        frame.setVisible(true);
    }

    private static int[] rank(double[] input) {
        Random r = new Random();
        // Create a copy of the input array and sort it
        double[] input2 = input.clone();
        for (int i = 0; i < input2.length; i++) {
            input2[i] += r.nextDouble(-1e-5,1e-5);
        }
        double[] sorted = input2.clone();
        Arrays.sort(sorted);

        // Create an int array to store the ranks
        int[] ranks = new int[input.length];

        // Loop through the input array and find the index of each element in the sorted array
        for (int i = 0; i < input.length; i++) {
            // Use binary search to find the index of input[i] in sorted
            ranks[i] = Arrays.binarySearch(sorted, input2[i]);
            for (int j = 0; j < i; j++) {
                if (ranks[i]==ranks[j]) ranks[i]++;
            }
        }
        int[] ranks2 = new int[ranks.length];
        for (int i = 0; i < ranks2.length; i++) {
            for (int j = 0; j < ranks.length; j++) {
                if (ranks[j]==i) ranks2[i]=j;
            }
        }
        // Return the ranks array
        return ranks2;
    }

    public double[][] rotate(double[][] input, double xAngleT, double yAngleT){
        double[][] output = deepClone(input);
        double length = Math.sqrt(Math.pow(xAngleT,2) + Math.pow(yAngleT,2));
        if(length < rotationThreshold) return output;
        double x = xAngleT/length;
        double y = yAngleT/length;
        double buffer;
        for (int i = 0; i < output.length; i++) {
            output[i][0] = input[i][0]*x + input[i][1]*y;
            output[i][1] = -input[i][0]*y + input[i][1]*x;
            buffer = output[i][0]*Math.cos(length) + output[i][2]*Math.sin(length);
            output[i][2] = -output[i][0]*Math.sin(length) + output[i][2]*Math.cos(length);
            output[i][0] = buffer;
            buffer = output[i][0]*x - output[i][1]*y;
            output[i][1] = output[i][0]*y + output[i][1]*x;
            output[i][0] = buffer;
        }
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < i; j++) {
                buffer = output[i][0]*output[j][0] + output[i][1]*output[j][1] + output[i][2]*output[j][2];
                output[i][0] -= buffer*output[j][0];
                output[i][1] -= buffer*output[j][1];
                output[i][2] -= buffer*output[j][2];
            }
            buffer = Math.sqrt(Math.pow(output[i][0],2) + Math.pow(output[i][1],2) + Math.pow(output[i][2],2));
            output[i][0] = output[i][0]/buffer;
            output[i][1] = output[i][1]/buffer;
            output[i][2] = output[i][2]/buffer;
        }
        return output;
    }

    public void makeMove (String move){
        int buffer;
        switch (move.charAt(0)) {
            case 'U' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = state[0][4];
                        state[0][4] = state[0][8];
                        state[0][8] = state[0][5];
                        state[0][5] = state[0][9];
                        state[0][9] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][6];
                        state[1][6] = state[1][1];
                        state[1][1] = state[1][7];
                        state[1][7] = buffer;
                    }
                    case '\'' -> {
                        buffer = state[0][4];
                        state[0][4] = state[0][9];
                        state[0][9] = state[0][5];
                        state[0][5] = state[0][8];
                        state[0][8] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][7];
                        state[1][7] = state[1][1];
                        state[1][1] = state[1][6];
                        state[1][6] = buffer;
                    }
                }
            }
            case 'D' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = state[0][6];
                        state[0][6] = state[0][11];
                        state[0][11] = state[0][7];
                        state[0][7] = state[0][10];
                        state[0][10] = buffer;
                        buffer = state[1][2];
                        state[1][2] = state[1][4];
                        state[1][4] = state[1][3];
                        state[1][3] = state[1][5];
                        state[1][5] = buffer;
                    }
                    case '\'' -> {
                        buffer = state[0][6];
                        state[0][6] = state[0][10];
                        state[0][10] = state[0][7];
                        state[0][7] = state[0][11];
                        state[0][11] = buffer;
                        buffer = state[1][2];
                        state[1][2] = state[1][5];
                        state[1][5] = state[1][3];
                        state[1][3] = state[1][4];
                        state[1][4] = buffer;
                    }
                    case '2' -> {
                        buffer = state[0][6];
                        state[0][6] = state[0][7];
                        state[0][7] = buffer;
                        buffer = state[0][10];
                        state[0][10] = state[0][11];
                        state[0][11] = buffer;
                        buffer = state[1][2];
                        state[1][2] = state[1][3];
                        state[1][3] = buffer;
                        buffer = state[1][4];
                        state[1][4] = state[1][5];
                        state[1][5] = buffer;
                    }
                }
            }
            case 'R' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        state[3][state[1][0]] = (state[3][state[1][0]]+1)%3;
                        state[3][state[1][4]] = (state[3][state[1][4]]+2)%3;
                        state[3][state[1][2]] = (state[3][state[1][2]]+1)%3;
                        state[3][state[1][6]] = (state[3][state[1][6]]+2)%3;
                        buffer = state[0][0];
                        state[0][0] = state[0][11];
                        state[0][11] = state[0][3];
                        state[0][3] = state[0][8];
                        state[0][8] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][4];
                        state[1][4] = state[1][2];
                        state[1][2] = state[1][6];
                        state[1][6] = buffer;
                    }
                    case '\'' -> {
                        state[3][state[1][0]] = (state[3][state[1][0]]+1)%3;
                        state[3][state[1][4]] = (state[3][state[1][4]]+2)%3;
                        state[3][state[1][2]] = (state[3][state[1][2]]+1)%3;
                        state[3][state[1][6]] = (state[3][state[1][6]]+2)%3;
                        buffer = state[0][0];
                        state[0][0] = state[0][8];
                        state[0][8] = state[0][3];
                        state[0][3] = state[0][11];
                        state[0][11] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][6];
                        state[1][6] = state[1][2];
                        state[1][2] = state[1][4];
                        state[1][4] = buffer;
                    }
                    case '2' -> {
                        buffer = state[0][0];
                        state[0][0] = state[0][3];
                        state[0][3] = buffer;
                        buffer = state[0][8];
                        state[0][8] = state[0][11];
                        state[0][11] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][2];
                        state[1][2] = buffer;
                        buffer = state[1][4];
                        state[1][4] = state[1][6];
                        state[1][6] = buffer;
                    }
                }
            }
            case 'L' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        state[3][state[1][1]] = (state[3][state[1][1]]+1)%3;
                        state[3][state[1][5]] = (state[3][state[1][5]]+2)%3;
                        state[3][state[1][3]] = (state[3][state[1][3]]+1)%3;
                        state[3][state[1][7]] = (state[3][state[1][7]]+2)%3;
                        buffer = state[0][1];
                        state[0][1] = state[0][9];
                        state[0][9] = state[0][2];
                        state[0][2] = state[0][10];
                        state[0][10] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][5];
                        state[1][5] = state[1][3];
                        state[1][3] = state[1][7];
                        state[1][7] = buffer;
                    }
                    case '\'' -> {
                        state[3][state[1][1]] = (state[3][state[1][1]]+1)%3;
                        state[3][state[1][5]] = (state[3][state[1][5]]+2)%3;
                        state[3][state[1][3]] = (state[3][state[1][3]]+1)%3;
                        state[3][state[1][7]] = (state[3][state[1][7]]+2)%3;
                        buffer = state[0][1];
                        state[0][1] = state[0][10];
                        state[0][10] = state[0][2];
                        state[0][2] = state[0][9];
                        state[0][9] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][7];
                        state[1][7] = state[1][3];
                        state[1][3] = state[1][5];
                        state[1][5] = buffer;
                    }
                    case '2' -> {
                        buffer = state[0][1];
                        state[0][1] = state[0][2];
                        state[0][2] = buffer;
                        buffer = state[0][9];
                        state[0][9] = state[0][10];
                        state[0][10] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][3];
                        state[1][3] = buffer;
                        buffer = state[1][5];
                        state[1][5] = state[1][7];
                        state[1][7] = buffer;
                    }
                }
            }
            case 'F' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        state[2][state[0][0]] = (state[2][state[0][0]]+1)%2;
                        state[2][state[0][4]] = (state[2][state[0][4]]+1)%2;
                        state[2][state[0][1]] = (state[2][state[0][1]]+1)%2;
                        state[2][state[0][7]] = (state[2][state[0][7]]+1)%2;
                        state[3][state[1][0]] = (state[3][state[1][0]]+2)%3;
                        state[3][state[1][7]] = (state[3][state[1][7]]+1)%3;
                        state[3][state[1][3]] = (state[3][state[1][3]]+2)%3;
                        state[3][state[1][4]] = (state[3][state[1][4]]+1)%3;
                        buffer = state[0][0];
                        state[0][0] = state[0][4];
                        state[0][4] = state[0][1];
                        state[0][1] = state[0][7];
                        state[0][7] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][7];
                        state[1][7] = state[1][3];
                        state[1][3] = state[1][4];
                        state[1][4] = buffer;
                    }
                    case '\'' -> {
                        state[2][state[0][0]] = (state[2][state[0][0]]+1)%2;
                        state[2][state[0][4]] = (state[2][state[0][4]]+1)%2;
                        state[2][state[0][1]] = (state[2][state[0][1]]+1)%2;
                        state[2][state[0][7]] = (state[2][state[0][7]]+1)%2;
                        state[3][state[1][0]] = (state[3][state[1][0]]+2)%3;
                        state[3][state[1][7]] = (state[3][state[1][7]]+1)%3;
                        state[3][state[1][3]] = (state[3][state[1][3]]+2)%3;
                        state[3][state[1][4]] = (state[3][state[1][4]]+1)%3;
                        buffer = state[0][0];
                        state[0][0] = state[0][7];
                        state[0][7] = state[0][1];
                        state[0][1] = state[0][4];
                        state[0][4] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][4];
                        state[1][4] = state[1][3];
                        state[1][3] = state[1][7];
                        state[1][7] = buffer;
                    }
                    case '2' -> {
                        buffer = state[0][0];
                        state[0][0] = state[0][1];
                        state[0][1] = buffer;
                        buffer = state[0][4];
                        state[0][4] = state[0][7];
                        state[0][7] = buffer;
                        buffer = state[1][0];
                        state[1][0] = state[1][3];
                        state[1][3] = buffer;
                        buffer = state[1][4];
                        state[1][4] = state[1][7];
                        state[1][7] = buffer;
                    }
                }
            }
            case 'B' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        state[2][state[0][2]] = (state[2][state[0][2]]+1)%2;
                        state[2][state[0][5]] = (state[2][state[0][5]]+1)%2;
                        state[2][state[0][3]] = (state[2][state[0][3]]+1)%2;
                        state[2][state[0][6]] = (state[2][state[0][6]]+1)%2;
                        state[3][state[1][1]] = (state[3][state[1][1]]+2)%3;
                        state[3][state[1][6]] = (state[3][state[1][6]]+1)%3;
                        state[3][state[1][2]] = (state[3][state[1][2]]+2)%3;
                        state[3][state[1][5]] = (state[3][state[1][5]]+1)%3;
                        buffer = state[0][2];
                        state[0][2] = state[0][5];
                        state[0][5] = state[0][3];
                        state[0][3] = state[0][6];
                        state[0][6] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][6];
                        state[1][6] = state[1][2];
                        state[1][2] = state[1][5];
                        state[1][5] = buffer;
                    }
                    case '\'' -> {
                        state[2][state[0][2]] = (state[2][state[0][2]]+1)%2;
                        state[2][state[0][5]] = (state[2][state[0][5]]+1)%2;
                        state[2][state[0][3]] = (state[2][state[0][3]]+1)%2;
                        state[2][state[0][6]] = (state[2][state[0][6]]+1)%2;
                        state[3][state[1][1]] = (state[3][state[1][1]]+2)%3;
                        state[3][state[1][6]] = (state[3][state[1][6]]+1)%3;
                        state[3][state[1][2]] = (state[3][state[1][2]]+2)%3;
                        state[3][state[1][5]] = (state[3][state[1][5]]+1)%3;
                        buffer = state[0][2];
                        state[0][2] = state[0][6];
                        state[0][6] = state[0][3];
                        state[0][3] = state[0][5];
                        state[0][5] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][5];
                        state[1][5] = state[1][2];
                        state[1][2] = state[1][6];
                        state[1][6] = buffer;
                    }
                    case '2' -> {
                        buffer = state[0][2];
                        state[0][2] = state[0][3];
                        state[0][3] = buffer;
                        buffer = state[0][5];
                        state[0][5] = state[0][6];
                        state[0][6] = buffer;
                        buffer = state[1][1];
                        state[1][1] = state[1][2];
                        state[1][2] = buffer;
                        buffer = state[1][5];
                        state[1][5] = state[1][6];
                        state[1][6] = buffer;
                    }
                }
            }
        }
    }

    private double[] negative (double[] input){
        double[] output = input.clone();
        for (int i = 0; i < input.length; i++) {
            output[i] *= -1;
        }
        return output;
    }

    private double[][] deepClone (double[][] input){
        double[][] output = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i].clone();
        }
        return output;
    }
    private int[][] deepClone (int[][] input){
        int[][] output = new int[input.length][];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i].clone();
        }
        return output;
    }
    private int[][][] deeperClone (int[][][] input){
        int[][][] output = new int[input.length][][];
        for (int i = 0; i < input.length; i++) {
            output[i] = deepClone(input[i]);
        }
        return output;
    }
}