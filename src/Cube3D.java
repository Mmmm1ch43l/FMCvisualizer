import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;

public class Cube3D extends JPanel implements MouseListener, MouseMotionListener {
    //parameters
    private final double rotationSpeed = 0.008;
    private final double perspective = 1/15.;
    private final double xAngleStart = -.3;
    private final double yAngleStart = .2;
    private final double faceShrinkage = 1;
    private final double rotationThreshold = 1e-10;
    private final int n = 3;
    private static final int windowWidth = 2000;
    private static final int windowHeight = 1000;


    // The vertices of the cube
    private final int[][] vertices = {{-1, -1, -1}, {-1, -1, 1}, {-1, 1, -1}, {-1, 1, 1}, {1, -1, -1}, {1, -1, 1}, {1, 1, -1}, {1, 1, 1}};
    private double[][] basis = rotate(new double[][] {{1,0,0},{0,1,0},{0,0,1}}, xAngleStart, yAngleStart);
    // The rotation angles
    private double xAngle;
    private double yAngle;
    private double[][] basisT;
    private double[][] verticesT;
    // The edges of the cube
    private final int[][] faces = {{0, 4, 5, 1}, {3, 7, 6, 2}, {1, 5, 7, 3}, {4, 0, 2, 6}, {5, 4, 6, 7}, {0, 1, 3, 2}};
    // The colors of the faces
    private final Color[] colors = {Color.white, Color.yellow, Color.green, Color.decode("#0091ff"), Color.red, Color.decode("#ff9500"),Color.pink,Color.lightGray};
    // The previous mouse position
    private int prevX;
    private int prevY;
    private double scale;
    private final double offset = (1-faceShrinkage)/2;
    private int[][][] faceColorsStart =
            {{{0, 0, 0},{0, 0, 0},{0, 0, 0}}//U
                    ,{{1, 1, 1},{1, 1, 1},{1, 1, 1}}//D
                    ,{{2, 2, 2},{2, 2, 2},{2, 2, 2}}//F
                    ,{{3, 3, 3},{3, 3, 3},{3, 3, 3}}//B
                    ,{{4, 4, 4},{4, 4, 4},{4, 4, 4}}//R
                    ,{{5, 5, 5},{5, 5, 5},{5, 5, 5}}};//L
    private int[][][] faceColorsOStart =
            {{{0, 0, 0},{0, 0, 0},{0, 0, 0}}//U
                    ,{{1, 1, 1},{1, 1, 1},{1, 1, 1}}//D
                    ,{{2, 2, 2},{2, 2, 2},{2, 2, 2}}//F
                    ,{{3, 3, 3},{3, 3, 3},{3, 3, 3}}//B
                    ,{{4, 4, 4},{4, 4, 4},{4, 4, 4}}//R
                    ,{{5, 5, 5},{5, 5, 5},{5, 5, 5}}};//L
            /*{{{0, 1, 0},{1, 0, 1},{0, 1, 0}}//U
                    ,{{1, 0, 1},{0, 1, 0},{1, 0, 1}}//D
                    ,{{2, 3, 2},{3, 2, 3},{2, 3, 2}}//F
                    ,{{3, 2, 3},{2, 3, 2},{3, 2, 3}}//B
                    ,{{4, 5, 4},{5, 4, 5},{4, 5, 4}}//R
                    ,{{5, 4, 5},{4, 5, 4},{5, 4, 5}}};//L*/
    private int[][][] faceColors;
    private int[][][] faceColorsO;

    public Cube3D() {
        // Add mouse listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        // Set the background color
        setBackground(Color.darkGray);
        resetCubes();
        applyMoves("B2 D' L' F2 L2 F R2 U2 B' U2 F' L2 F' D L2 U R U2 B D", false);
        applyMoves("B2 D' L' F2 L2 F R2 U2 B'U2 F' L2 F' DL2 U RU2 B", true);
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
        scale = Math.min(width / 1.8, height / .9) / 4.0;
        basisT = rotate(basis, xAngle, yAngle);
        verticesT = express();
        // Draw the faces of the cube
        // Get the average z value of the face
        double[] z = new double[faces.length];
        for (int i = 0; i < faces.length; i++) {
            z[i] = (verticesT[faces[i][0]][2] + verticesT[faces[i][1]][2] + verticesT[faces[i][2]][2] + verticesT[faces[i][3]][2]) / 4.0;
        }
        int[] order = rank(z);
        for (int i = 0; i < faces.length; i++) {
            drawFace(g2d, order[i], false);
            drawFace(g2d, order[i], true);
        }
    }

    public void drawFace(Graphics2D g2d, int face, boolean otherCube) {
        int[] xCords;
        int[] yCords;
        double[] startingVertex = verticesT[faces[face][0]].clone();
        double[] xDirection = verticesT[faces[face][1]].clone();
        double[] yDirection = verticesT[faces[face][3]].clone();
        double[] v1;
        double[] v2;
        double[] v3;
        double[] v4;
        for (int i = 0; i < startingVertex.length; i++) {
            xDirection[i] -= startingVertex[i];
            yDirection[i] -= startingVertex[i];
            xDirection[i] /= n;
            yDirection[i] /= n;
        }
        if(otherCube){
            startingVertex[0] += 1.8;
        } else {
            startingVertex[0] -= 1.8;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if(otherCube){
                    g2d.setColor(colors[faceColorsO[face][i][j]]);
                } else {
                    g2d.setColor(colors[faceColors[face][i][j]]);
                }
                v1 = startingVertex.clone();
                v2 = startingVertex.clone();
                v3 = startingVertex.clone();
                v4 = startingVertex.clone();
                for (int k = 0; k < n; k++) {
                    v1[k] += i*xDirection[k]+j*yDirection[k] + (xDirection[k]+yDirection[k])*offset;
                    v2[k] += (i+1)*xDirection[k]+j*yDirection[k] + (-xDirection[k]+yDirection[k])*offset;
                    v3[k] += (i+1)*xDirection[k]+(j+1)*yDirection[k] + (-xDirection[k]-yDirection[k])*offset;
                    v4[k] += i*xDirection[k]+(j+1)*yDirection[k] + (xDirection[k]-yDirection[k])*offset;
                }
                v1[0] *= Math.exp(v1[2]*perspective)*scale;
                v1[1] *= Math.exp(v1[2]*perspective)*scale;
                v2[0] *= Math.exp(v2[2]*perspective)*scale;
                v2[1] *= Math.exp(v2[2]*perspective)*scale;
                v3[0] *= Math.exp(v3[2]*perspective)*scale;
                v3[1] *= Math.exp(v3[2]*perspective)*scale;
                v4[0] *= Math.exp(v4[2]*perspective)*scale;
                v4[1] *= Math.exp(v4[2]*perspective)*scale;
                xCords = new int[] {(int) v1[0], (int) v2[0], (int) v3[0], (int) v4[0]};
                yCords = new int[] {(int) v1[1], (int) v2[1], (int) v3[1], (int) v4[1]};
                g2d.fillPolygon(xCords, yCords, 4);
                g2d.setColor(Color.darkGray);
                g2d.drawPolygon(xCords, yCords, 4);
            }
        }
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
        // Add the panel to the frame
        frame.add(new Cube3D());
        // Show the frame
        frame.setVisible(true);
    }

    public static int[] rank(double[] input) {
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

    private double[][] express(){
        double[][] output = new double[vertices.length][vertices[0].length];
        for (int i = 0; i < vertices.length; i++) {
            for (int j = 0; j < vertices[0].length; j++) {
                output[i][j] = vertices[i][0]*basisT[0][j] + vertices[i][1]*basisT[1][j] + vertices[i][2]*basisT[2][j];
            }
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

    public void resetCubes (){
        faceColors = deeperClone(faceColorsStart);
        faceColorsO = deeperClone(faceColorsOStart);
    }

    public void applyMoves (String moves, boolean inverted){
        moves = cleanUp(moves);
        if(inverted){
            applyMovesN(reverse(moves));
            applyMovesO(moves);
        } else {
            applyMovesN(moves);
            applyMovesO(reverse(moves));
        }
    }

    private void applyMovesN (String moves){
        while(!moves.isEmpty()){
            applyMoveN(moves.substring(0,2));
            moves = moves.substring(2);
        }
    }

    private void applyMovesO (String moves){
        while(!moves.isEmpty()){
            applyMoveO(moves.substring(0,2));
            moves = moves.substring(2);
        }
    }

    private void applyMoveN (String move){
        int buffer;
        switch (move.charAt(0)){
            case 'U' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[0][2][0];
                    faceColors[0][2][0] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[0][0][1];
                    faceColors[0][0][1] = faceColors[0][1][2];
                    faceColors[0][1][2] = faceColors[0][2][1];
                    faceColors[0][2][1] = buffer;
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[5][0][0];
                    faceColors[5][0][0] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[4][1][0];
                    faceColors[4][1][0] = faceColors[3][1][0];
                    faceColors[3][1][0] = faceColors[5][1][0];
                    faceColors[5][1][0] = buffer;
                    buffer = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[4][2][0];
                    faceColors[4][2][0] = faceColors[3][2][0];
                    faceColors[3][2][0] = faceColors[5][2][0];
                    faceColors[5][2][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[0][0][2];
                    faceColors[0][0][2] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[0][2][1];
                    faceColors[0][2][1] = faceColors[0][1][2];
                    faceColors[0][1][2] = faceColors[0][0][1];
                    faceColors[0][0][1] = buffer;
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[5][0][0];
                    faceColors[5][0][0] = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[4][0][0];
                    faceColors[4][0][0] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[5][1][0];
                    faceColors[5][1][0] = faceColors[3][1][0];
                    faceColors[3][1][0] = faceColors[4][1][0];
                    faceColors[4][1][0] = buffer;
                    buffer = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[5][2][0];
                    faceColors[5][2][0] = faceColors[3][2][0];
                    faceColors[3][2][0] = faceColors[4][2][0];
                    faceColors[4][2][0] = buffer;
                } else {
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[0][2][2];
                    faceColors[0][2][2] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[0][2][0];
                    faceColors[0][2][0] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[0][1][2];
                    faceColors[0][1][2] = buffer;
                    buffer = faceColors[0][0][1];
                    faceColors[0][0][1] = faceColors[0][2][1];
                    faceColors[0][2][1] = buffer;
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[3][0][0];
                    faceColors[3][0][0] = buffer;
                    buffer = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[5][0][0];
                    faceColors[5][0][0] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[3][1][0];
                    faceColors[3][1][0] = buffer;
                    buffer = faceColors[4][1][0];
                    faceColors[4][1][0] = faceColors[5][1][0];
                    faceColors[5][1][0] = buffer;
                    buffer = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[3][2][0];
                    faceColors[3][2][0] = buffer;
                    buffer = faceColors[4][2][0];
                    faceColors[4][2][0] = faceColors[5][2][0];
                    faceColors[5][2][0] = buffer;
                }
            }
            case 'D' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[1][2][0];
                    faceColors[1][2][0] = buffer;
                    buffer = faceColors[1][1][0];
                    faceColors[1][1][0] = faceColors[1][0][1];
                    faceColors[1][0][1] = faceColors[1][1][2];
                    faceColors[1][1][2] = faceColors[1][2][1];
                    faceColors[1][2][1] = buffer;
                    buffer = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[5][0][2];
                    faceColors[5][0][2] = faceColors[3][0][2];
                    faceColors[3][0][2] = faceColors[4][0][2];
                    faceColors[4][0][2] = buffer;
                    buffer = faceColors[2][1][2];
                    faceColors[2][1][2] = faceColors[5][1][2];
                    faceColors[5][1][2] = faceColors[3][1][2];
                    faceColors[3][1][2] = faceColors[4][1][2];
                    faceColors[4][1][2] = buffer;
                    buffer = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[5][2][2];
                    faceColors[5][2][2] = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[4][2][2];
                    faceColors[4][2][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[1][2][0];
                    faceColors[1][2][0] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[1][0][2];
                    faceColors[1][0][2] = buffer;
                    buffer = faceColors[1][1][0];
                    faceColors[1][1][0] = faceColors[1][2][1];
                    faceColors[1][2][1] = faceColors[1][1][2];
                    faceColors[1][1][2] = faceColors[1][0][1];
                    faceColors[1][0][1] = buffer;
                    buffer = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[4][0][2];
                    faceColors[4][0][2] = faceColors[3][0][2];
                    faceColors[3][0][2] = faceColors[5][0][2];
                    faceColors[5][0][2] = buffer;
                    buffer = faceColors[2][1][2];
                    faceColors[2][1][2] = faceColors[4][1][2];
                    faceColors[4][1][2] = faceColors[3][1][2];
                    faceColors[3][1][2] = faceColors[5][1][2];
                    faceColors[5][1][2] = buffer;
                    buffer = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[5][2][2];
                    faceColors[5][2][2] = buffer;
                } else {
                    buffer = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[1][2][2];
                    faceColors[1][2][2] = buffer;
                    buffer = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[1][2][0];
                    faceColors[1][2][0] = buffer;
                    buffer = faceColors[1][1][0];
                    faceColors[1][1][0] = faceColors[1][1][2];
                    faceColors[1][1][2] = buffer;
                    buffer = faceColors[1][0][1];
                    faceColors[1][0][1] = faceColors[1][2][1];
                    faceColors[1][2][1] = buffer;
                    buffer = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[3][0][2];
                    faceColors[3][0][2] = buffer;
                    buffer = faceColors[4][0][2];
                    faceColors[4][0][2] = faceColors[5][0][2];
                    faceColors[5][0][2] = buffer;
                    buffer = faceColors[2][1][2];
                    faceColors[2][1][2] = faceColors[3][1][2];
                    faceColors[3][1][2] = buffer;
                    buffer = faceColors[4][1][2];
                    faceColors[4][1][2] = faceColors[5][1][2];
                    faceColors[5][1][2] = buffer;
                    buffer = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[3][2][2];
                    faceColors[3][2][2] = buffer;
                    buffer = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[5][2][2];
                    faceColors[5][2][2] = buffer;
                }
            }
            case 'F' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[2][2][0];
                    faceColors[2][2][0] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[2][0][1];
                    faceColors[2][0][1] = faceColors[2][1][2];
                    faceColors[2][1][2] = faceColors[2][2][1];
                    faceColors[2][2][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[5][2][2];
                    faceColors[5][2][2] = faceColors[1][2][0];
                    faceColors[1][2][0] = faceColors[4][0][0];
                    faceColors[4][0][0] = buffer;
                    buffer = faceColors[0][1][2];
                    faceColors[0][1][2] = faceColors[5][2][1];
                    faceColors[5][2][1] = faceColors[1][1][0];
                    faceColors[1][1][0] = faceColors[4][0][1];
                    faceColors[4][0][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[5][2][0];
                    faceColors[5][2][0] = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[4][0][2];
                    faceColors[4][0][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[2][0][2];
                    faceColors[2][0][2] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[2][2][1];
                    faceColors[2][2][1] = faceColors[2][1][2];
                    faceColors[2][1][2] = faceColors[2][0][1];
                    faceColors[2][0][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[1][2][0];
                    faceColors[1][2][0] = faceColors[5][2][2];
                    faceColors[5][2][2] = buffer;
                    buffer = faceColors[0][1][2];
                    faceColors[0][1][2] = faceColors[4][0][1];
                    faceColors[4][0][1] = faceColors[1][1][0];
                    faceColors[1][1][0] = faceColors[5][2][1];
                    faceColors[5][2][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[4][0][2];
                    faceColors[4][0][2] = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[5][2][0];
                    faceColors[5][2][0] = buffer;
                } else {
                    buffer = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[2][2][2];
                    faceColors[2][2][2] = buffer;
                    buffer = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[2][2][0];
                    faceColors[2][2][0] = buffer;
                    buffer = faceColors[2][1][0];
                    faceColors[2][1][0] = faceColors[2][1][2];
                    faceColors[2][1][2] = buffer;
                    buffer = faceColors[2][0][1];
                    faceColors[2][0][1] = faceColors[2][2][1];
                    faceColors[2][2][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[1][2][0];
                    faceColors[1][2][0] = buffer;
                    buffer = faceColors[5][2][2];
                    faceColors[5][2][2] = faceColors[4][0][0];
                    faceColors[4][0][0] = buffer;
                    buffer = faceColors[0][1][2];
                    faceColors[0][1][2] = faceColors[1][1][0];
                    faceColors[1][1][0] = buffer;
                    buffer = faceColors[5][2][1];
                    faceColors[5][2][1] = faceColors[4][0][1];
                    faceColors[4][0][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[1][0][0];
                    faceColors[1][0][0] = buffer;
                    buffer = faceColors[5][2][0];
                    faceColors[5][2][0] = faceColors[4][0][2];
                    faceColors[4][0][2] = buffer;
                }
            }
            case 'B' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[3][0][2];
                    faceColors[3][0][2] = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[3][2][0];
                    faceColors[3][2][0] = buffer;
                    buffer = faceColors[3][1][0];
                    faceColors[3][1][0] = faceColors[3][0][1];
                    faceColors[3][0][1] = faceColors[3][1][2];
                    faceColors[3][1][2] = faceColors[3][2][1];
                    faceColors[3][2][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[4][2][0];
                    faceColors[4][2][0] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[5][0][2];
                    faceColors[5][0][2] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[4][2][1];
                    faceColors[4][2][1] = faceColors[1][1][2];
                    faceColors[1][1][2] = faceColors[5][0][1];
                    faceColors[5][0][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[5][0][0];
                    faceColors[5][0][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[3][2][0];
                    faceColors[3][2][0] = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[3][0][2];
                    faceColors[3][0][2] = buffer;
                    buffer = faceColors[3][1][0];
                    faceColors[3][1][0] = faceColors[3][2][1];
                    faceColors[3][2][1] = faceColors[3][1][2];
                    faceColors[3][1][2] = faceColors[3][0][1];
                    faceColors[3][0][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[5][0][2];
                    faceColors[5][0][2] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[4][2][0];
                    faceColors[4][2][0] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[5][0][1];
                    faceColors[5][0][1] = faceColors[1][1][2];
                    faceColors[1][1][2] = faceColors[4][2][1];
                    faceColors[4][2][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[5][0][0];
                    faceColors[5][0][0] = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[4][2][2];
                    faceColors[4][2][2] = buffer;
                } else {
                    buffer = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[3][2][2];
                    faceColors[3][2][2] = buffer;
                    buffer = faceColors[3][0][2];
                    faceColors[3][0][2] = faceColors[3][2][0];
                    faceColors[3][2][0] = buffer;
                    buffer = faceColors[3][1][0];
                    faceColors[3][1][0] = faceColors[3][1][2];
                    faceColors[3][1][2] = buffer;
                    buffer = faceColors[3][0][1];
                    faceColors[3][0][1] = faceColors[3][2][1];
                    faceColors[3][2][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[1][2][2];
                    faceColors[1][2][2] = buffer;
                    buffer = faceColors[4][2][0];
                    faceColors[4][2][0] = faceColors[5][0][2];
                    faceColors[5][0][2] = buffer;
                    buffer = faceColors[0][1][0];
                    faceColors[0][1][0] = faceColors[1][1][2];
                    faceColors[1][1][2] = buffer;
                    buffer = faceColors[4][2][1];
                    faceColors[4][2][1] = faceColors[5][0][1];
                    faceColors[5][0][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[1][0][2];
                    faceColors[1][0][2] = buffer;
                    buffer = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[5][0][0];
                    faceColors[5][0][0] = buffer;
                }
            }
            case 'R' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[4][0][2];
                    faceColors[4][0][2] = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[4][2][0];
                    faceColors[4][2][0] = buffer;
                    buffer = faceColors[4][1][0];
                    faceColors[4][1][0] = faceColors[4][0][1];
                    faceColors[4][0][1] = faceColors[4][1][2];
                    faceColors[4][1][2] = faceColors[4][2][1];
                    faceColors[4][2][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[1][2][0];
                    faceColors[1][2][0] = faceColors[3][0][2];
                    faceColors[3][0][2] = buffer;
                    buffer = faceColors[0][2][1];
                    faceColors[0][2][1] = faceColors[2][2][1];
                    faceColors[2][2][1] = faceColors[1][2][1];
                    faceColors[1][2][1] = faceColors[3][0][1];
                    faceColors[3][0][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[3][0][0];
                    faceColors[3][0][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[4][2][0];
                    faceColors[4][2][0] = faceColors[4][2][2];
                    faceColors[4][2][2] = faceColors[4][0][2];
                    faceColors[4][0][2] = buffer;
                    buffer = faceColors[4][1][0];
                    faceColors[4][1][0] = faceColors[4][2][1];
                    faceColors[4][2][1] = faceColors[4][1][2];
                    faceColors[4][1][2] = faceColors[4][0][1];
                    faceColors[4][0][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[3][0][2];
                    faceColors[3][0][2] = faceColors[1][2][0];
                    faceColors[1][2][0] = faceColors[2][2][0];
                    faceColors[2][2][0] = buffer;
                    buffer = faceColors[0][2][1];
                    faceColors[0][2][1] = faceColors[3][0][1];
                    faceColors[3][0][1] = faceColors[1][2][1];
                    faceColors[1][2][1] = faceColors[2][2][1];
                    faceColors[2][2][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[3][0][0];
                    faceColors[3][0][0] = faceColors[1][2][2];
                    faceColors[1][2][2] = faceColors[2][2][2];
                    faceColors[2][2][2] = buffer;
                } else {
                    buffer = faceColors[4][0][0];
                    faceColors[4][0][0] = faceColors[4][2][2];
                    faceColors[4][2][2] = buffer;
                    buffer = faceColors[4][0][2];
                    faceColors[4][0][2] = faceColors[4][2][0];
                    faceColors[4][2][0] = buffer;
                    buffer = faceColors[4][1][0];
                    faceColors[4][1][0] = faceColors[4][1][2];
                    faceColors[4][1][2] = buffer;
                    buffer = faceColors[4][0][1];
                    faceColors[4][0][1] = faceColors[4][2][1];
                    faceColors[4][2][1] = buffer;
                    buffer = faceColors[0][2][0];
                    faceColors[0][2][0] = faceColors[1][2][0];
                    faceColors[1][2][0] = buffer;
                    buffer = faceColors[2][2][0];
                    faceColors[2][2][0] = faceColors[3][0][2];
                    faceColors[3][0][2] = buffer;
                    buffer = faceColors[0][2][1];
                    faceColors[0][2][1] = faceColors[1][2][1];
                    faceColors[1][2][1] = buffer;
                    buffer = faceColors[2][2][1];
                    faceColors[2][2][1] = faceColors[3][0][1];
                    faceColors[3][0][1] = buffer;
                    buffer = faceColors[0][2][2];
                    faceColors[0][2][2] = faceColors[1][2][2];
                    faceColors[1][2][2] = buffer;
                    buffer = faceColors[2][2][2];
                    faceColors[2][2][2] = faceColors[3][0][0];
                    faceColors[3][0][0] = buffer;
                }
            }
            case 'L' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColors[5][0][0];
                    faceColors[5][0][0] = faceColors[5][0][2];
                    faceColors[5][0][2] = faceColors[5][2][2];
                    faceColors[5][2][2] = faceColors[5][2][0];
                    faceColors[5][2][0] = buffer;
                    buffer = faceColors[5][1][0];
                    faceColors[5][1][0] = faceColors[5][0][1];
                    faceColors[5][0][1] = faceColors[5][1][2];
                    faceColors[5][1][2] = faceColors[5][2][1];
                    faceColors[5][2][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[2][0][0];
                    faceColors[2][0][0] = buffer;
                    buffer = faceColors[0][0][1];
                    faceColors[0][0][1] = faceColors[3][2][1];
                    faceColors[3][2][1] = faceColors[1][0][1];
                    faceColors[1][0][1] = faceColors[2][0][1];
                    faceColors[2][0][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[3][2][0];
                    faceColors[3][2][0] = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[2][0][2];
                    faceColors[2][0][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColors[5][0][0];
                    faceColors[5][0][0] = faceColors[5][2][0];
                    faceColors[5][2][0] = faceColors[5][2][2];
                    faceColors[5][2][2] = faceColors[5][0][2];
                    faceColors[5][0][2] = buffer;
                    buffer = faceColors[5][1][0];
                    faceColors[5][1][0] = faceColors[5][2][1];
                    faceColors[5][2][1] = faceColors[5][1][2];
                    faceColors[5][1][2] = faceColors[5][0][1];
                    faceColors[5][0][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[2][0][0];
                    faceColors[2][0][0] = faceColors[1][0][0];
                    faceColors[1][0][0] = faceColors[3][2][2];
                    faceColors[3][2][2] = buffer;
                    buffer = faceColors[0][0][1];
                    faceColors[0][0][1] = faceColors[2][0][1];
                    faceColors[2][0][1] = faceColors[1][0][1];
                    faceColors[1][0][1] = faceColors[3][2][1];
                    faceColors[3][2][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[2][0][2];
                    faceColors[2][0][2] = faceColors[1][0][2];
                    faceColors[1][0][2] = faceColors[3][2][0];
                    faceColors[3][2][0] = buffer;
                } else {
                    buffer = faceColors[5][0][0];
                    faceColors[5][0][0] = faceColors[5][2][2];
                    faceColors[5][2][2] = buffer;
                    buffer = faceColors[5][0][2];
                    faceColors[5][0][2] = faceColors[5][2][0];
                    faceColors[5][2][0] = buffer;
                    buffer = faceColors[5][1][0];
                    faceColors[5][1][0] = faceColors[5][1][2];
                    faceColors[5][1][2] = buffer;
                    buffer = faceColors[5][0][1];
                    faceColors[5][0][1] = faceColors[5][2][1];
                    faceColors[5][2][1] = buffer;
                    buffer = faceColors[0][0][0];
                    faceColors[0][0][0] = faceColors[1][0][0];
                    faceColors[1][0][0] = buffer;
                    buffer = faceColors[3][2][2];
                    faceColors[3][2][2] = faceColors[2][0][0];
                    faceColors[2][0][0] = buffer;
                    buffer = faceColors[0][0][1];
                    faceColors[0][0][1] = faceColors[1][0][1];
                    faceColors[1][0][1] = buffer;
                    buffer = faceColors[3][2][1];
                    faceColors[3][2][1] = faceColors[2][0][1];
                    faceColors[2][0][1] = buffer;
                    buffer = faceColors[0][0][2];
                    faceColors[0][0][2] = faceColors[1][0][2];
                    faceColors[1][0][2] = buffer;
                    buffer = faceColors[3][2][0];
                    faceColors[3][2][0] = faceColors[2][0][2];
                    faceColors[2][0][2] = buffer;
                }
            }
        }
    }

    private void applyMoveO (String move){
        int buffer;
        switch (move.charAt(0)){
            case 'U' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = buffer;
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = buffer;
                    buffer = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = buffer;
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = buffer;
                    buffer = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = buffer;
                } else {
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = buffer;
                    buffer = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = buffer;
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = buffer;
                    buffer = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = buffer;
                    buffer = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = buffer;
                    buffer = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = buffer;
                    buffer = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = buffer;
                }
            }
            case 'D' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = buffer;
                    buffer = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = buffer;
                    buffer = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = buffer;
                    buffer = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = buffer;
                    buffer = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = buffer;
                    buffer = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = buffer;
                    buffer = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = buffer;
                    buffer = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = buffer;
                    buffer = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = buffer;
                } else {
                    buffer = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = buffer;
                    buffer = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = buffer;
                    buffer = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = buffer;
                    buffer = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = buffer;
                    buffer = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = buffer;
                    buffer = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = buffer;
                    buffer = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = buffer;
                    buffer = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = buffer;
                    buffer = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = buffer;
                    buffer = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = buffer;
                }
            }
            case 'F' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = buffer;
                    buffer = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = buffer;
                    buffer = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = buffer;
                } else {
                    buffer = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = buffer;
                    buffer = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = buffer;
                    buffer = faceColorsO[2][1][0];
                    faceColorsO[2][1][0] = faceColorsO[2][1][2];
                    faceColorsO[2][1][2] = buffer;
                    buffer = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = buffer;
                    buffer = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = buffer;
                    buffer = faceColorsO[0][1][2];
                    faceColorsO[0][1][2] = faceColorsO[1][1][0];
                    faceColorsO[1][1][0] = buffer;
                    buffer = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = buffer;
                    buffer = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = buffer;
                }
            }
            case 'B' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = buffer;
                    buffer = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = buffer;
                    buffer = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = buffer;
                } else {
                    buffer = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = buffer;
                    buffer = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = buffer;
                    buffer = faceColorsO[3][1][0];
                    faceColorsO[3][1][0] = faceColorsO[3][1][2];
                    faceColorsO[3][1][2] = buffer;
                    buffer = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = buffer;
                    buffer = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = buffer;
                    buffer = faceColorsO[0][1][0];
                    faceColorsO[0][1][0] = faceColorsO[1][1][2];
                    faceColorsO[1][1][2] = buffer;
                    buffer = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = buffer;
                    buffer = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = buffer;
                }
            }
            case 'R' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = buffer;
                    buffer = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = buffer;
                    buffer = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = buffer;
                    buffer = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = buffer;
                    buffer = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = buffer;
                } else {
                    buffer = faceColorsO[4][0][0];
                    faceColorsO[4][0][0] = faceColorsO[4][2][2];
                    faceColorsO[4][2][2] = buffer;
                    buffer = faceColorsO[4][0][2];
                    faceColorsO[4][0][2] = faceColorsO[4][2][0];
                    faceColorsO[4][2][0] = buffer;
                    buffer = faceColorsO[4][1][0];
                    faceColorsO[4][1][0] = faceColorsO[4][1][2];
                    faceColorsO[4][1][2] = buffer;
                    buffer = faceColorsO[4][0][1];
                    faceColorsO[4][0][1] = faceColorsO[4][2][1];
                    faceColorsO[4][2][1] = buffer;
                    buffer = faceColorsO[0][2][0];
                    faceColorsO[0][2][0] = faceColorsO[1][2][0];
                    faceColorsO[1][2][0] = buffer;
                    buffer = faceColorsO[2][2][0];
                    faceColorsO[2][2][0] = faceColorsO[3][0][2];
                    faceColorsO[3][0][2] = buffer;
                    buffer = faceColorsO[0][2][1];
                    faceColorsO[0][2][1] = faceColorsO[1][2][1];
                    faceColorsO[1][2][1] = buffer;
                    buffer = faceColorsO[2][2][1];
                    faceColorsO[2][2][1] = faceColorsO[3][0][1];
                    faceColorsO[3][0][1] = buffer;
                    buffer = faceColorsO[0][2][2];
                    faceColorsO[0][2][2] = faceColorsO[1][2][2];
                    faceColorsO[1][2][2] = buffer;
                    buffer = faceColorsO[2][2][2];
                    faceColorsO[2][2][2] = faceColorsO[3][0][0];
                    faceColorsO[3][0][0] = buffer;
                }
            }
            case 'L' -> {
                if(move.charAt(1)==' '){
                    buffer = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = buffer;
                    buffer = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = buffer;
                    buffer = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = buffer;
                } else if (move.charAt(1)=='\''){
                    buffer = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = buffer;
                    buffer = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = buffer;
                    buffer = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = buffer;
                } else {
                    buffer = faceColorsO[5][0][0];
                    faceColorsO[5][0][0] = faceColorsO[5][2][2];
                    faceColorsO[5][2][2] = buffer;
                    buffer = faceColorsO[5][0][2];
                    faceColorsO[5][0][2] = faceColorsO[5][2][0];
                    faceColorsO[5][2][0] = buffer;
                    buffer = faceColorsO[5][1][0];
                    faceColorsO[5][1][0] = faceColorsO[5][1][2];
                    faceColorsO[5][1][2] = buffer;
                    buffer = faceColorsO[5][0][1];
                    faceColorsO[5][0][1] = faceColorsO[5][2][1];
                    faceColorsO[5][2][1] = buffer;
                    buffer = faceColorsO[0][0][0];
                    faceColorsO[0][0][0] = faceColorsO[1][0][0];
                    faceColorsO[1][0][0] = buffer;
                    buffer = faceColorsO[3][2][2];
                    faceColorsO[3][2][2] = faceColorsO[2][0][0];
                    faceColorsO[2][0][0] = buffer;
                    buffer = faceColorsO[0][0][1];
                    faceColorsO[0][0][1] = faceColorsO[1][0][1];
                    faceColorsO[1][0][1] = buffer;
                    buffer = faceColorsO[3][2][1];
                    faceColorsO[3][2][1] = faceColorsO[2][0][1];
                    faceColorsO[2][0][1] = buffer;
                    buffer = faceColorsO[0][0][2];
                    faceColorsO[0][0][2] = faceColorsO[1][0][2];
                    faceColorsO[1][0][2] = buffer;
                    buffer = faceColorsO[3][2][0];
                    faceColorsO[3][2][0] = faceColorsO[2][0][2];
                    faceColorsO[2][0][2] = buffer;
                }
            }
        }
    }

    private String reverse (String input){
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i = i + 2) {
            if(input.charAt(i+1)==' '){
                output.insert(0, input.charAt(i) + "\'");
            } else if (input.charAt(i+1)=='\''){
                output.insert(0, input.charAt(i) + " ");
            } else {
                output.insert(0, input.substring(i, i + 2));
            }
        }
        return output.toString();
    }

    private String cleanUp (String input){
        String output = "";
        for (int i = 0; i < input.length()-1; i++) {
            if(input.charAt(i)=='U'||input.charAt(i)=='D'||input.charAt(i)=='F'||input.charAt(i)=='B'||input.charAt(i)=='R'||input.charAt(i)=='L'){
                if(input.charAt(i+1)=='\''||input.charAt(i+1)=='2'){
                    output += input.substring(i,i+2);
                } else {
                    output += input.charAt(i)+" ";
                }
            }
        }
        if(input.charAt(input.length()-1)=='U'||input.charAt(input.length()-1)=='D'||input.charAt(input.length()-1)=='F'||input.charAt(input.length()-1)=='B'||input.charAt(input.length()-1)=='R'||input.charAt(input.length()-1)=='L'){
            output += input.charAt(input.length()-1)+" ";
        }
        return output;
    }
}