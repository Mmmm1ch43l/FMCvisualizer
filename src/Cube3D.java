import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

public class Cube3D extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    //parameters
    private final double rotationSpeed = 0.008;
    private final double perspective = 1/30.;
    private final double xAngleStart = -.3;
    private final double yAngleStart = .2;
    private final double rotationThreshold = 1e-10;
    private static final int windowWidth = 1000;
    private static final int windowHeight = 1000;
    private static final double cubeShrinkage = .95;
    private static final int turningSteps = 10;
    private double[][] basis = rotate(new double[][] {{1,0,0},{0,1,0},{0,0,1}}, xAngleStart, yAngleStart);
    // The rotation angles
    private double xAngle;
    private double yAngle;
    private double[][] basisT;
    private int currentStep = 0;
    // The colors of the faces
    private final Color[] colors = {
            new Color(100, 100, 100, 30),//0: transparent gray
            Color.white,//1: white
            Color.yellow,//2: yellow
            Color.green,//3: green
            Color.decode("#0091ff"),//4: blue
            Color.red,//5: red
            Color.decode("#ff9500")};//6: orange
    private int[][] cubeFaceColors = {
            // 0 - 26: normal pieces
            {4,0,1,0,6,0},
            {4,0,1,0,0,0},
            {4,0,1,0,0,5},
            {4,0,0,0,6,0},
            {4,0,0,0,0,0},// 4: blue center
            {4,0,0,0,0,5},
            {4,0,0,2,6,0},
            {4,0,0,2,0,0},
            {4,0,0,2,0,5},
            {0,0,1,0,6,0},
            {0,0,1,0,0,0},//10: white center
            {0,0,1,0,0,5},
            {0,0,0,0,6,0},//12: orange center
            {0,0,0,0,0,0},//13: fully transparent
            {0,0,0,0,0,5},//14: red center
            {0,0,0,2,6,0},
            {0,0,0,2,0,0},//16: yellow center
            {0,0,0,2,0,5},
            {0,3,1,0,6,0},
            {0,3,1,0,0,0},
            {0,3,1,0,0,5},
            {0,3,0,0,6,0},
            {0,3,0,0,0,0},//22: green center
            {0,3,0,0,0,5},
            {0,3,0,2,6,0},
            {0,3,0,2,0,0},
            {0,3,0,2,0,5},
            {4,4,4,4,4,4},//27: fully blue
            {4,4,5,4,4,4},//28: blue with red top
            {5,4,4,4,4,4},//29: blue with red side
            {4,4,4,5,4,4},//30: blue with red bottom
            {4,4,4,5,4,4},//31: blue with red bottom 2
            {5,4,3,4,4,4} //32: blue with red side and green top
    };
    private int[] cubeColors = new int[27];
    private int[] faceColors = new int[162];
    private double[][][] faceVerticesInit = new double[162][][];
    private double[][][] faceVertices = new double[162][][];
    private double[][][] faceVerticesT = new double[162][][];
    private boolean[] turning = new boolean[162];
    // The previous mouse position
    private int prevX;
    private int prevY;
    private double scale;

    public Cube3D() {
        // Add mouse listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        this.setFocusable(true);
        this.requestFocus();
        this.addKeyListener(this);
        // Set the background color
        setBackground(Color.lightGray);
        for (int i = 0; i < 27; i++) {
            cubeColors[i] = 13;
        }
        //set up pieces:
        for (int i = 0; i < 27; i++) {
            cubeColors[i] = i;
        }

        for (int i = 0; i < 162; i++) {
            faceColors[i] = 0;
        }
        for (int i = 0; i < 27; i++) {
            if(cubeColors[i]!=13){
                System.arraycopy(cubeFaceColors[cubeColors[i]], 0, faceColors, 6 * i, 6);
            }
        }
        for (int i = 0; i < 162; i++) {
            turning[i] = false;
        }
        int index;
        double[] start;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    index = 6*i+18*j+54*k;
                    start = new double[]{2 * i - 3+(1-cubeShrinkage)/2, 2 * j - 3+(1-cubeShrinkage)/2, 2 * k - 3+(1-cubeShrinkage)/2};
                    faceVertices[index] = new double[][]{addAndShrink(new double[]{0, 0, 0},start), addAndShrink(new double[]{1, 0, 0},start), addAndShrink(new double[]{1, 1, 0},start), addAndShrink(new double[]{0, 1, 0},start)};
                    faceVertices[index+1] = new double[][]{addAndShrink(new double[]{0, 0, 1},start), addAndShrink(new double[]{1, 0, 1},start), addAndShrink(new double[]{1, 1, 1},start), addAndShrink(new double[]{0, 1, 1},start)};
                    faceVertices[index+2] = new double[][]{addAndShrink(new double[]{0, 0, 0},start), addAndShrink(new double[]{1, 0, 0},start), addAndShrink(new double[]{1, 0, 1},start), addAndShrink(new double[]{0, 0, 1},start)};
                    faceVertices[index+3] = new double[][]{addAndShrink(new double[]{0, 1, 0},start), addAndShrink(new double[]{1, 1, 0},start), addAndShrink(new double[]{1, 1, 1},start), addAndShrink(new double[]{0, 1, 1},start)};
                    faceVertices[index+4] = new double[][]{addAndShrink(new double[]{0, 0, 0},start), addAndShrink(new double[]{0, 1, 0},start), addAndShrink(new double[]{0, 1, 1},start), addAndShrink(new double[]{0, 0, 1},start)};
                    faceVertices[index+5] = new double[][]{addAndShrink(new double[]{1, 0, 0},start), addAndShrink(new double[]{1, 1, 0},start), addAndShrink(new double[]{1, 1, 1},start), addAndShrink(new double[]{1, 0, 1},start)};
                }
            }
        }
        faceVerticesInit = deeperClone(faceVertices);
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
        scale = Math.min(width / .95, height / .95) / 12.0;
        basisT = rotate(basis, xAngle, yAngle);
        express();
        // Draw the faces of the cube
        // Get the average z value of the face
        double[] z = new double[162];
        for (int i = 0; i < 162; i++) {
            z[i] = faceVerticesT[i][0][2] + faceVerticesT[i][1][2] + faceVerticesT[i][2][2] + faceVerticesT[i][3][2];
        }
        int[] order = rank(z);
        for (int i = 0; i < 162; i++) {
            drawFace(g2d, faceColors[order[i]], faceVerticesT[order[i]]);
        }
        g2d.setColor(Color.darkGray);
        g2d.fillPolygon(new int[]{0,10,10,0}, new int[]{0,0,10+currentStep,10}, 4);
    }

    private void drawFace(Graphics2D g2d, int color, double[][] vertices) {
        int[] xCords = new int[] {(int) (vertices[0][0]*Math.exp(vertices[0][2]*perspective)*scale),
                (int) (vertices[1][0]*Math.exp(vertices[1][2]*perspective)*scale),
                (int) (vertices[2][0]*Math.exp(vertices[2][2]*perspective)*scale),
                (int) (vertices[3][0]*Math.exp(vertices[3][2]*perspective)*scale)};
        int[] yCords = new int[] {(int) (vertices[0][1]*Math.exp(vertices[0][2]*perspective)*scale),
                (int) (vertices[1][1]*Math.exp(vertices[1][2]*perspective)*scale),
                (int) (vertices[2][1]*Math.exp(vertices[2][2]*perspective)*scale),
                (int) (vertices[3][1]*Math.exp(vertices[3][2]*perspective)*scale)};
        g2d.setColor(colors[color]);
        g2d.fillPolygon(xCords, yCords, 4);
        g2d.setColor(Color.darkGray);
        g2d.drawPolygon(xCords, yCords, 4);
    }

    private void express(){
        for (int i = 0; i < 162; i++) {
            faceVerticesT[i] = new double[4][];
            for (int j = 0; j < 4; j++) {
                faceVerticesT[i][j] = new double[3];
                for (int k = 0; k < 3; k++) {
                    faceVerticesT[i][j][k] = faceVertices[i][j][0]*basisT[0][k] + faceVertices[i][j][1]*basisT[1][k] + faceVertices[i][j][2]*basisT[2][k];
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        // Store the current mouse position
        prevX = e.getX();
        prevY = e.getY();
    }

    public void mouseDragged(MouseEvent e) {
        // Update the rotation angles
        xAngle = rotationSpeed * (e.getX() - prevX);
        yAngle = rotationSpeed * (e.getY() - prevY);
        repaint();
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {
        //reset orientation axis
        xAngle = 0;
        yAngle = 0;
        basis = deepClone(basisT);
        repaint();
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

    public void makeMove (String move) {
        for (int i = 0; i < 162; i++) {
            turning[i] = false;
        }
        int orientation = -1;
        Set<Integer> toBeRotated = new HashSet<Integer>();
        int buffer;
        switch (move.charAt(0)) {
            case 'U' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[20];
                        cubeColors[20] = cubeColors[2];
                        cubeColors[2] = cubeColors[0];
                        cubeColors[0] = cubeColors[18];
                        cubeColors[18] = buffer;
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[11];
                        cubeColors[11] = cubeColors[1];
                        cubeColors[1] = cubeColors[9];
                        cubeColors[9] = buffer;
                        orientation = 0;
                    }
                    case '\'' -> {
                        buffer = cubeColors[20];
                        cubeColors[20] = cubeColors[18];
                        cubeColors[18] = cubeColors[0];
                        cubeColors[0] = cubeColors[2];
                        cubeColors[2] = buffer;
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[9];
                        cubeColors[9] = cubeColors[1];
                        cubeColors[1] = cubeColors[11];
                        cubeColors[11] = buffer;
                        orientation = 1;
                    }
                }
                toBeRotated.add(cubeColors[20]);
                toBeRotated.add(cubeColors[2]);
                toBeRotated.add(cubeColors[0]);
                toBeRotated.add(cubeColors[18]);
                toBeRotated.add(cubeColors[19]);
                toBeRotated.add(cubeColors[9]);
                toBeRotated.add(cubeColors[1]);
                toBeRotated.add(cubeColors[11]);
                for (int i = 0; i < 6; i++) {
                    turning[6*20 + i] = true;
                    turning[6*2 + i] = true;
                    turning[6*0 + i] = true;
                    turning[6*18 + i] = true;
                    turning[6*19 + i] = true;
                    turning[6*9 + i] = true;
                    turning[6*1 + i] = true;
                    turning[6*11 + i] = true;
                }
            }
            case 'D' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[26];
                        cubeColors[26] = cubeColors[24];
                        cubeColors[24] = cubeColors[6];
                        cubeColors[6] = cubeColors[8];
                        cubeColors[8] = buffer;
                        buffer = cubeColors[25];
                        cubeColors[25] = cubeColors[15];
                        cubeColors[15] = cubeColors[7];
                        cubeColors[7] = cubeColors[17];
                        cubeColors[17] = buffer;
                        orientation = 1;
                    }
                    case '\'' -> {
                        buffer = cubeColors[26];
                        cubeColors[26] = cubeColors[8];
                        cubeColors[8] = cubeColors[6];
                        cubeColors[6] = cubeColors[24];
                        cubeColors[24] = buffer;
                        buffer = cubeColors[25];
                        cubeColors[25] = cubeColors[17];
                        cubeColors[17] = cubeColors[7];
                        cubeColors[7] = cubeColors[15];
                        cubeColors[15] = buffer;
                        orientation = 0;
                    }
                }
                toBeRotated.add(cubeColors[26]);
                toBeRotated.add(cubeColors[8]);
                toBeRotated.add(cubeColors[6]);
                toBeRotated.add(cubeColors[24]);
                toBeRotated.add(cubeColors[25]);
                toBeRotated.add(cubeColors[17]);
                toBeRotated.add(cubeColors[7]);
                toBeRotated.add(cubeColors[15]);
                for (int i = 0; i < 6; i++) {
                    turning[6*26 + i] = true;
                    turning[6*8 + i] = true;
                    turning[6*6 + i] = true;
                    turning[6*24 + i] = true;
                    turning[6*25 + i] = true;
                    turning[6*17 + i] = true;
                    turning[6*7 + i] = true;
                    turning[6*15 + i] = true;
                }
            }
            case 'R' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[20];
                        cubeColors[20] = cubeColors[26];
                        cubeColors[26] = cubeColors[8];
                        cubeColors[8] = cubeColors[2];
                        cubeColors[2] = buffer;
                        buffer = cubeColors[23];
                        cubeColors[23] = cubeColors[17];
                        cubeColors[17] = cubeColors[5];
                        cubeColors[5] = cubeColors[11];
                        cubeColors[11] = buffer;
                        orientation = 2;
                    }
                    case '\'' -> {
                        buffer = cubeColors[20];
                        cubeColors[20] = cubeColors[2];
                        cubeColors[2] = cubeColors[8];
                        cubeColors[8] = cubeColors[26];
                        cubeColors[26] = buffer;
                        buffer = cubeColors[23];
                        cubeColors[23] = cubeColors[11];
                        cubeColors[11] = cubeColors[5];
                        cubeColors[5] = cubeColors[17];
                        cubeColors[17] = buffer;
                        orientation = 3;
                    }
                }
                toBeRotated.add(cubeColors[20]);
                toBeRotated.add(cubeColors[2]);
                toBeRotated.add(cubeColors[8]);
                toBeRotated.add(cubeColors[26]);
                toBeRotated.add(cubeColors[23]);
                toBeRotated.add(cubeColors[11]);
                toBeRotated.add(cubeColors[5]);
                toBeRotated.add(cubeColors[17]);
                for (int i = 0; i < 6; i++) {
                    turning[6*20 + i] = true;
                    turning[6*2 + i] = true;
                    turning[6*8 + i] = true;
                    turning[6*26 + i] = true;
                    turning[6*23 + i] = true;
                    turning[6*11 + i] = true;
                    turning[6*5 + i] = true;
                    turning[6*17 + i] = true;
                }
            }
            case 'L' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[18];
                        cubeColors[18] = cubeColors[0];
                        cubeColors[0] = cubeColors[6];
                        cubeColors[6] = cubeColors[24];
                        cubeColors[24] = buffer;
                        buffer = cubeColors[21];
                        cubeColors[21] = cubeColors[9];
                        cubeColors[9] = cubeColors[3];
                        cubeColors[3] = cubeColors[15];
                        cubeColors[15] = buffer;
                        orientation = 3;
                    }
                    case '\'' -> {
                        buffer = cubeColors[18];
                        cubeColors[18] = cubeColors[24];
                        cubeColors[24] = cubeColors[6];
                        cubeColors[6] = cubeColors[0];
                        cubeColors[0] = buffer;
                        buffer = cubeColors[21];
                        cubeColors[21] = cubeColors[15];
                        cubeColors[15] = cubeColors[3];
                        cubeColors[3] = cubeColors[9];
                        cubeColors[9] = buffer;
                        orientation = 2;
                    }
                }
                toBeRotated.add(cubeColors[18]);
                toBeRotated.add(cubeColors[24]);
                toBeRotated.add(cubeColors[6]);
                toBeRotated.add(cubeColors[0]);
                toBeRotated.add(cubeColors[21]);
                toBeRotated.add(cubeColors[15]);
                toBeRotated.add(cubeColors[3]);
                toBeRotated.add(cubeColors[9]);
                for (int i = 0; i < 6; i++) {
                    turning[6*18 + i] = true;
                    turning[6*24 + i] = true;
                    turning[6*6 + i] = true;
                    turning[6*0 + i] = true;
                    turning[6*21 + i] = true;
                    turning[6*15 + i] = true;
                    turning[6*3 + i] = true;
                    turning[6*9 + i] = true;
                }
            }
            case 'F' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[18];
                        cubeColors[18] = cubeColors[24];
                        cubeColors[24] = cubeColors[26];
                        cubeColors[26] = cubeColors[20];
                        cubeColors[20] = buffer;
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[21];
                        cubeColors[21] = cubeColors[25];
                        cubeColors[25] = cubeColors[23];
                        cubeColors[23] = buffer;
                        orientation = 4;
                    }
                    case '\'' -> {
                        buffer = cubeColors[18];
                        cubeColors[18] = cubeColors[20];
                        cubeColors[20] = cubeColors[26];
                        cubeColors[26] = cubeColors[24];
                        cubeColors[24] = buffer;
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[23];
                        cubeColors[23] = cubeColors[25];
                        cubeColors[25] = cubeColors[21];
                        cubeColors[21] = buffer;
                        orientation = 5;
                    }
                }
                toBeRotated.add(cubeColors[18]);
                toBeRotated.add(cubeColors[20]);
                toBeRotated.add(cubeColors[26]);
                toBeRotated.add(cubeColors[24]);
                toBeRotated.add(cubeColors[19]);
                toBeRotated.add(cubeColors[23]);
                toBeRotated.add(cubeColors[25]);
                toBeRotated.add(cubeColors[21]);
                for (int i = 0; i < 6; i++) {
                    turning[6*18 + i] = true;
                    turning[6*20 + i] = true;
                    turning[6*26 + i] = true;
                    turning[6*24 + i] = true;
                    turning[6*19 + i] = true;
                    turning[6*23 + i] = true;
                    turning[6*25 + i] = true;
                    turning[6*21 + i] = true;
                }
            }
            case 'B' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[0];
                        cubeColors[0] = cubeColors[2];
                        cubeColors[2] = cubeColors[8];
                        cubeColors[8] = cubeColors[6];
                        cubeColors[6] = buffer;
                        buffer = cubeColors[1];
                        cubeColors[1] = cubeColors[5];
                        cubeColors[5] = cubeColors[7];
                        cubeColors[7] = cubeColors[3];
                        cubeColors[3] = buffer;
                        orientation = 5;
                    }
                    case '\'' -> {
                        buffer = cubeColors[0];
                        cubeColors[0] = cubeColors[6];
                        cubeColors[6] = cubeColors[8];
                        cubeColors[8] = cubeColors[2];
                        cubeColors[2] = buffer;
                        buffer = cubeColors[1];
                        cubeColors[1] = cubeColors[3];
                        cubeColors[3] = cubeColors[7];
                        cubeColors[7] = cubeColors[5];
                        cubeColors[5] = buffer;
                        orientation = 4;
                    }
                }
                toBeRotated.add(cubeColors[0]);
                toBeRotated.add(cubeColors[6]);
                toBeRotated.add(cubeColors[8]);
                toBeRotated.add(cubeColors[2]);
                toBeRotated.add(cubeColors[1]);
                toBeRotated.add(cubeColors[3]);
                toBeRotated.add(cubeColors[7]);
                toBeRotated.add(cubeColors[5]);
                for (int i = 0; i < 6; i++) {
                    turning[6*0 + i] = true;
                    turning[6*6 + i] = true;
                    turning[6*8 + i] = true;
                    turning[6*2 + i] = true;
                    turning[6*1 + i] = true;
                    turning[6*3 + i] = true;
                    turning[6*7 + i] = true;
                    turning[6*5 + i] = true;
                }
            }
            case 'M' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[1];
                        cubeColors[1] = cubeColors[7];
                        cubeColors[7] = cubeColors[25];
                        cubeColors[25] = buffer;
                        buffer = cubeColors[10];
                        cubeColors[10] = cubeColors[4];
                        cubeColors[4] = cubeColors[16];
                        cubeColors[16] = cubeColors[22];
                        cubeColors[22] = buffer;
                        orientation = 3;
                    }
                    case '\'' -> {
                        buffer = cubeColors[19];
                        cubeColors[19] = cubeColors[25];
                        cubeColors[25] = cubeColors[7];
                        cubeColors[7] = cubeColors[1];
                        cubeColors[1] = buffer;
                        buffer = cubeColors[10];
                        cubeColors[10] = cubeColors[22];
                        cubeColors[22] = cubeColors[16];
                        cubeColors[16] = cubeColors[4];
                        cubeColors[4] = buffer;
                        orientation = 2;
                    }
                }
                toBeRotated.add(cubeColors[19]);
                toBeRotated.add(cubeColors[25]);
                toBeRotated.add(cubeColors[7]);
                toBeRotated.add(cubeColors[1]);
                toBeRotated.add(cubeColors[10]);
                toBeRotated.add(cubeColors[22]);
                toBeRotated.add(cubeColors[16]);
                toBeRotated.add(cubeColors[4]);
                for (int i = 0; i < 6; i++) {
                    turning[6*19 + i] = true;
                    turning[6*25 + i] = true;
                    turning[6*7 + i] = true;
                    turning[6*1 + i] = true;
                    turning[6*10 + i] = true;
                    turning[6*22 + i] = true;
                    turning[6*16 + i] = true;
                    turning[6*4 + i] = true;
                }
            }
            case 'S' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[11];
                        cubeColors[11] = cubeColors[9];
                        cubeColors[9] = cubeColors[15];
                        cubeColors[15] = cubeColors[17];
                        cubeColors[17] = buffer;
                        buffer = cubeColors[10];
                        cubeColors[10] = cubeColors[12];
                        cubeColors[12] = cubeColors[16];
                        cubeColors[16] = cubeColors[14];
                        cubeColors[14] = buffer;
                        orientation = 4;
                    }
                    case '\'' -> {
                        buffer = cubeColors[11];
                        cubeColors[11] = cubeColors[17];
                        cubeColors[17] = cubeColors[15];
                        cubeColors[15] = cubeColors[9];
                        cubeColors[9] = buffer;
                        buffer = cubeColors[10];
                        cubeColors[10] = cubeColors[14];
                        cubeColors[14] = cubeColors[16];
                        cubeColors[16] = cubeColors[12];
                        cubeColors[12] = buffer;
                        orientation = 5;
                    }
                }
                toBeRotated.add(cubeColors[11]);
                toBeRotated.add(cubeColors[17]);
                toBeRotated.add(cubeColors[15]);
                toBeRotated.add(cubeColors[9]);
                toBeRotated.add(cubeColors[10]);
                toBeRotated.add(cubeColors[14]);
                toBeRotated.add(cubeColors[16]);
                toBeRotated.add(cubeColors[12]);
                for (int i = 0; i < 6; i++) {
                    turning[6*11 + i] = true;
                    turning[6*17 + i] = true;
                    turning[6*15 + i] = true;
                    turning[6*9 + i] = true;
                    turning[6*10 + i] = true;
                    turning[6*14 + i] = true;
                    turning[6*16 + i] = true;
                    turning[6*12 + i] = true;
                }
            }
            case 'E' -> {
                switch (move.charAt(1)){
                    case ' ' -> {
                        buffer = cubeColors[23];
                        cubeColors[23] = cubeColors[21];
                        cubeColors[21] = cubeColors[3];
                        cubeColors[3] = cubeColors[5];
                        cubeColors[5] = buffer;
                        buffer = cubeColors[22];
                        cubeColors[22] = cubeColors[12];
                        cubeColors[12] = cubeColors[4];
                        cubeColors[4] = cubeColors[14];
                        cubeColors[14] = buffer;
                        orientation = 1;
                    }
                    case '\'' -> {
                        buffer = cubeColors[23];
                        cubeColors[23] = cubeColors[5];
                        cubeColors[5] = cubeColors[3];
                        cubeColors[3] = cubeColors[21];
                        cubeColors[21] = buffer;
                        buffer = cubeColors[22];
                        cubeColors[22] = cubeColors[14];
                        cubeColors[14] = cubeColors[4];
                        cubeColors[4] = cubeColors[12];
                        cubeColors[12] = buffer;
                        orientation = 0;
                    }
                }
                toBeRotated.add(cubeColors[23]);
                toBeRotated.add(cubeColors[5]);
                toBeRotated.add(cubeColors[3]);
                toBeRotated.add(cubeColors[21]);
                toBeRotated.add(cubeColors[22]);
                toBeRotated.add(cubeColors[14]);
                toBeRotated.add(cubeColors[4]);
                toBeRotated.add(cubeColors[12]);
                for (int i = 0; i < 6; i++) {
                    turning[6*23 + i] = true;
                    turning[6*5 + i] = true;
                    turning[6*3 + i] = true;
                    turning[6*21 + i] = true;
                    turning[6*22 + i] = true;
                    turning[6*14 + i] = true;
                    turning[6*4 + i] = true;
                    turning[6*12 + i] = true;
                }
            }
        }
        for (int cubeColor : toBeRotated) {
            if(cubeColor!=13){
                switch (orientation){
                    case 0 -> {
                        buffer = cubeFaceColors[cubeColor][0];
                        cubeFaceColors[cubeColor][0] = cubeFaceColors[cubeColor][4];
                        cubeFaceColors[cubeColor][4] = cubeFaceColors[cubeColor][1];
                        cubeFaceColors[cubeColor][1] = cubeFaceColors[cubeColor][5];
                        cubeFaceColors[cubeColor][5] = buffer;
                    }
                    case 1 -> {
                        buffer = cubeFaceColors[cubeColor][0];
                        cubeFaceColors[cubeColor][0] = cubeFaceColors[cubeColor][5];
                        cubeFaceColors[cubeColor][5] = cubeFaceColors[cubeColor][1];
                        cubeFaceColors[cubeColor][1] = cubeFaceColors[cubeColor][4];
                        cubeFaceColors[cubeColor][4] = buffer;
                    }
                    case 2 -> {
                        buffer = cubeFaceColors[cubeColor][0];
                        cubeFaceColors[cubeColor][0] = cubeFaceColors[cubeColor][2];
                        cubeFaceColors[cubeColor][2] = cubeFaceColors[cubeColor][1];
                        cubeFaceColors[cubeColor][1] = cubeFaceColors[cubeColor][3];
                        cubeFaceColors[cubeColor][3] = buffer;
                    }
                    case 3 -> {
                        buffer = cubeFaceColors[cubeColor][0];
                        cubeFaceColors[cubeColor][0] = cubeFaceColors[cubeColor][3];
                        cubeFaceColors[cubeColor][3] = cubeFaceColors[cubeColor][1];
                        cubeFaceColors[cubeColor][1] = cubeFaceColors[cubeColor][2];
                        cubeFaceColors[cubeColor][2] = buffer;
                    }
                    case 4 -> {
                        buffer = cubeFaceColors[cubeColor][2];
                        cubeFaceColors[cubeColor][2] = cubeFaceColors[cubeColor][4];
                        cubeFaceColors[cubeColor][4] = cubeFaceColors[cubeColor][3];
                        cubeFaceColors[cubeColor][3] = cubeFaceColors[cubeColor][5];
                        cubeFaceColors[cubeColor][5] = buffer;
                    }
                    case 5 -> {
                        buffer = cubeFaceColors[cubeColor][2];
                        cubeFaceColors[cubeColor][2] = cubeFaceColors[cubeColor][5];
                        cubeFaceColors[cubeColor][5] = cubeFaceColors[cubeColor][3];
                        cubeFaceColors[cubeColor][3] = cubeFaceColors[cubeColor][4];
                        cubeFaceColors[cubeColor][4] = buffer;
                    }
                }
            }
        }
        for (int i = 0; i < 162; i++) {
            faceColors[i] = 0;
        }
        for (int i = 0; i < 27; i++) {
            if(cubeColors[i]!=13){
                System.arraycopy(cubeFaceColors[cubeColors[i]], 0, faceColors, 6 * i, 6);
            }
        }
        double[][] rotationMatrix = new double[][]{{1,0,0},{0,1,0},{0,0,1}};
        double angle;
        for (int i = 0; i < turningSteps; i++) {
            angle = (turningSteps-i)*Math.PI/(2*turningSteps);
            switch (orientation){
                case 0 -> rotationMatrix = new double[][]{{Math.cos(angle),0,Math.sin(angle)},{0,1,0},{-Math.sin(angle),0,Math.cos(angle)}};
                case 1 -> rotationMatrix = new double[][]{{Math.cos(angle),0,-Math.sin(angle)},{0,1,0},{Math.sin(angle),0,Math.cos(angle)}};
                case 2 -> rotationMatrix = new double[][]{{1,0,0},{0,Math.cos(angle),Math.sin(angle)},{0,-Math.sin(angle),Math.cos(angle)}};
                case 3 -> rotationMatrix = new double[][]{{1,0,0},{0,Math.cos(angle),-Math.sin(angle)},{0,Math.sin(angle),Math.cos(angle)}};
                case 4 -> rotationMatrix = new double[][]{{Math.cos(angle),Math.sin(angle),0},{-Math.sin(angle),Math.cos(angle),0},{0,0,1}};
                case 5 -> rotationMatrix = new double[][]{{Math.cos(angle),-Math.sin(angle),0},{Math.sin(angle),Math.cos(angle),0},{0,0,1}};
            }
            for (int j = 0; j < 162; j++) {
                if(turning[j]){
                    for (int l = 0; l < 4; l++) {
                        faceVertices[j][l][0] = rotationMatrix[0][0]*faceVerticesInit[j][l][0] + rotationMatrix[0][1]*faceVerticesInit[j][l][1] + rotationMatrix[0][2]*faceVerticesInit[j][l][2];
                        faceVertices[j][l][1] = rotationMatrix[1][0]*faceVerticesInit[j][l][0] + rotationMatrix[1][1]*faceVerticesInit[j][l][1] + rotationMatrix[1][2]*faceVerticesInit[j][l][2];
                        faceVertices[j][l][2] = rotationMatrix[2][0]*faceVerticesInit[j][l][0] + rotationMatrix[2][1]*faceVerticesInit[j][l][1] + rotationMatrix[2][2]*faceVerticesInit[j][l][2];
                    }
                }
            }
            currentStep = i;
            repaint();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
        faceVertices = deeperClone(faceVerticesInit);
        repaint();
    }

    private double[] negative (double[] input){
        double[] output = input.clone();
        for (int i = 0; i < input.length; i++) {
            output[i] *= -1;
        }
        return output;
    }

    private double[] addAndShrink (double[] base, double[] addition){
        for (int i = 0; i < base.length; i++) {
            base[i] *= 2*cubeShrinkage;
            base[i] += addition[i];
        }
        return base;
    }

    private double[][] deepClone (double[][] input){
        double[][] output = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i].clone();
        }
        return output;
    }
    private double[][][] deeperClone (double[][][] input){
        double[][][] output = new double[input.length][][];
        for (int i = 0; i < input.length; i++) {
            output[i] = deepClone(input[i]);
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()){
            case 'j' -> makeMove("U ");
            case 'f' -> makeMove("U'");
            case 'i' -> makeMove("R ");
            case 'k' -> makeMove("R'");
            case 'd' -> makeMove("L ");
            case 'e' -> makeMove("L'");
            case 'h' -> makeMove("F ");
            case 'g' -> makeMove("F'");
            case 's' -> makeMove("D ");
            case 'l' -> makeMove("D'");
            case 'w' -> makeMove("B ");
            case 'o' -> makeMove("B'");
            case 'y', 't' -> makeMove("M ");
            case 'v', 'n' -> makeMove("M'");
            case 'u' -> makeMove("S ");
            case 'r' -> makeMove("S'");
            case 'c', 'x' -> makeMove("E ");
            case 'm', ',' -> makeMove("E'");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}