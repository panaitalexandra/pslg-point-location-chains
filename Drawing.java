import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

public class Drawing extends JPanel implements ActionListener{
    private boolean mouseStarted = false;
    private boolean mouseSelected = false;

    private int[] x_coords, y_coords;
    private Color[] colorArray;
    private int mouseX, mouseY, current_color, count = 0, n_count, MMx, MMy;
    private double Mx, MMMx;
    private double My, MMMy;

    private Consumer<String> locationLogger;
    private Point[] pointArray;

    private int centerX, centerY, faceIndex;
    private Algorithm algorithm;

    private Timer timer;
    private boolean isAnimating;
    private boolean drawEdges = true; // drawPoli -> drawEdges
    private int currentEdgeIndex;
    private final int totalSteps = 50;
    private int currentStep;
    private double animStartX, animStartY, animEndX, animEndY;
    private double currentAnimX, currentAnimY;
    private double stepX, stepY;

    private final double SCALE_FACTOR = 15.0;

    public void setLocationLogger(Consumer<String> logger) {
        this.locationLogger = logger;
    }
    public Drawing(){
        this.setPreferredSize(new Dimension(500, 500));
        this.setBackground(new Color(248, 240, 227, 255));

        isAnimating = false;
        currentEdgeIndex = 0;
        currentStep = 0;

        timer = new Timer(15, this);
        timer.start();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    private void handleMouseClick(int x, int y) {
        if (mouseStarted && algorithm != null) {
            mouseX = x;
            mouseY = y;
            Mx = (mouseX - 250) / SCALE_FACTOR;
            My = (250 - mouseY) / SCALE_FACTOR;
            mouseSelected = true;

            faceIndex = algorithm.findChain(Mx, My);

            if (locationLogger != null)
                locationLogger.accept(String.format("\nPoint M: (%.2f, %.2f)\nFace: F%d", Mx, My, faceIndex));

            repaint();
        }
    }
    public void actionPerformed(ActionEvent e) {
        if (!isAnimating) {
            return;
        }

        if (currentStep < totalSteps) {
            currentAnimX += stepX;
            currentAnimY += stepY;
            currentStep++;
        } else {
            currentEdgeIndex++;

            Edge[] dcel = algorithm.getDCEL();
            if (currentEdgeIndex < dcel.length) {
                setupAnimationForCurrentEdge();
            } else {
                isAnimating = false;
                drawEdges = true;
            }
        }
        repaint();
    }

    private void setupAnimationForCurrentEdge() {
        Edge edge = algorithm.getDCEL()[currentEdgeIndex];

        animStartX = edge.getV1().getX();
        animStartY = edge.getV1().getY();
        animEndX = edge.getV2().getX();
        animEndY = edge.getV2().getY();

        currentAnimX = animStartX;
        currentAnimY = animStartY;

        stepX = (animEndX - animStartX) / totalSteps;
        stepY = (animEndY - animStartY) / totalSteps;

        currentStep = 0;
    }


    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Stroke defaultStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f));

        g2.setStroke(new BasicStroke(1.0f));
        Color defaultColor = g2.getColor();
        g2.setColor(new Color(0,0,0, 13));
        drawAxes(g2);

        if (algorithm == null || algorithm.getDCEL() == null) return;

        Edge[] dcel = algorithm.getDCEL();

        g2.setStroke(defaultStroke);

        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < currentEdgeIndex; ++i)
            drawFullEdge(g2, dcel[i]);

        if (isAnimating && currentEdgeIndex < dcel.length) {
            Color purple = new Color(248, 141, 141, 255);
            g2.setColor(purple); // Folosim o culoare diferită pentru animație
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawLine(calcX(animStartX), calcY(animStartY), calcX(currentAnimX), calcY(currentAnimY));
        }

        g2.setStroke(defaultStroke);

        if (drawEdges && !isAnimating) {
            for (int i = 0; i < dcel.length; ++i) {
                drawFullEdge(g2, dcel[i]);
            }
        }

        if (mouseSelected) {
            g.setColor(Color.red);
            g.fillOval(mouseX, mouseY, 1, 1);
            g.drawLine(mouseX + 2, mouseY - 2, mouseX - 2, mouseY + 2);
            g.drawLine(mouseX - 2, mouseY - 2, mouseX + 2, mouseY + 2);
            for (int i = 0; i < dcel.length; ++i) {
                if ((dcel[i].getF1() == faceIndex) || (dcel[i].getF2() == faceIndex))
                    g.drawLine(calcX(dcel[i].getV1().getX()),
                            calcY(dcel[i].getV1().getY()),
                            calcX(dcel[i].getV2().getX()),
                            calcY(dcel[i].getV2().getY())
                    );
            }
        }
        if (drawEdges && !isAnimating) {
            g2.setColor(Color.magenta);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));

            if (algorithm != null && algorithm.getPoints() != null) {
                Point[] allPoints = algorithm.getPoints();
                for (Point point : allPoints)
                    g2.drawString("" + point.getId(), calcX(point.getX()) + 4, calcY(point.getY()) - 4);

            }
        }
        g2.setColor(Color.black);
        // g.drawString("( " + MMMx + "," + MMMy + " )", MMx, MMy); // Păstrează logica de urmărire mouse
    }

    private void drawAxes(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.drawLine(0, 0, 500, 0);
        g2.drawLine(0, 0, 0, 500);
        g2.drawLine(500, 0, 500, 500);
        g2.drawLine(0, 500, 500, 500);

        g2.drawLine(calcX(-20), calcY(0), calcX(20), calcY(0));
        g2.drawString("x", calcX(21), calcY(-1));
        g2.drawLine(calcX(20) - 3, calcY(0) - 3, calcX(20), calcY(0));
        g2.drawLine(calcX(20) - 3, calcY(0) + 3, calcX(20), calcY(0));

        g2.drawLine(calcX(0), calcY(-20), calcX(0), calcY(20));
        g2.drawString("y", calcX(-1), calcY(21));
        g2.drawLine(calcX(0) + 3, calcY(20) + 3, calcX(0), calcY(20));
        g2.drawLine(calcX(0) - 3, calcY(20) + 3, calcX(0), calcY(20));
    }

    private void drawFullEdge(Graphics2D g2, Edge edge) {
        g2.setColor(Color.BLACK);
        g2.drawLine(calcX(edge.getV1().getX()),
                calcY(edge.getV1().getY()),
                calcX(edge.getV2().getX()),
                calcY(edge.getV2().getY())
        );


        g2.fillOval(calcX(edge.getV1().getX()) - 2, calcY(edge.getV1().getY()) - 2, 4, 4);
        g2.fillOval(calcX(edge.getV2().getX()) - 2, calcY(edge.getV2().getY()) - 2, 4, 4);

        int middleX = (calcX(edge.getV1().getX()) + calcX(edge.getV2().getX())) / 2;
        int middleY = (calcY(edge.getV1().getY()) + calcY(edge.getV2().getY())) / 2;

        double mX = (edge.getV1().getX() + edge.getV2().getX()) / 2.0;
        double mY = (edge.getV1().getY() + edge.getV2().getY()) / 2.0;


        if (drawEdges && !isAnimating) {
            g2.setColor(Color.pink);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));

            g2.drawString("" + edge.getNumber(),
                    middleX - 15,
                    middleY - 5);
        }

        double tangenta = (edge.getV2().getY() - edge.getV1().getY()) / (edge.getV2().getX() - edge.getV1().getX());
        double inclination = (edge.getV1().getX() < edge.getV2().getX()) ? -Math.PI / 2 : Math.PI / 2;
        double theta = Math.atan(tangenta) + inclination;


        if (edge.getWeight() > 0)
            g2.drawString("" + edge.getWeight(), middleX - 10, middleY + 10);
        g2.setColor(Color.black);
    }
    public void activateMouse(Algorithm algorithm){
        this.algorithm = algorithm;
        if (algorithm == null || algorithm.getDCEL() == null || algorithm.getDCEL().length == 0) {
            drawEdges = false;
            isAnimating = false;
            return;
        }

        drawEdges = false;
        mouseStarted = true;

        currentEdgeIndex = 0;
        isAnimating = true;
        setupAnimationForCurrentEdge();

        repaint();
    }

/***
 if (faceIndex == 0) { // F -> faceIndex
 ArrayList sir3 = algorithm.getUnLant(); // a -> algorithm
 g.setColor(Color.YELLOW);
 Object[] sir4=sir3.toArray();
 for(int d=0;d<sir4.length;++d){
 g.drawLine(calcX((int)((Muchie5)sir4[d]).getV1().getX())+2,
 calcY((int)((Muchie5)sir4[d]).getV1().getY())+2,
 calcX((int)((Muchie5)sir4[d]).getV2().getX())+2,
 calcY((int)((Muchie5)sir4[d]).getV2().getY())+2
 );
 }
 } else{
 ArrayList[] array=algorithm.getLant(); // a -> algorithm
 g.setColor(Color.YELLOW);
 Object[] sir1=array[0].toArray();
 for(int d=0;d<sir1.length;++d){
 g.drawLine(calcX((int)((Muchie5)sir1[d]).getV1().getX())+2,
 calcY((int)((Muchie5)sir1[d]).getV1().getY())+2,
 calcX((int)((Muchie5)sir1[d]).getV2().getX())+2,
 calcY((int)((Muchie5)sir1[d]).getV2().getY())+2

 );
 }
 g.setColor(Color.pink);
 Object[] sir2=array[1].toArray();
 for(int d=0;d<sir2.length;++d){
 g.drawLine(calcX((int)((Muchie5)sir2[d]).getV1().getX())-2,
 calcY((int)((Muchie5)sir2[d]).getV1().getY())-2,
 calcX((int)((Muchie5)sir2[d]).getV2().getX())-2,
 calcY((int)((Muchie5)sir2[d]).getV2().getY())-2
 );
 }
 g.setColor(Color.blue);
 }
 ***/

    public int calcX(double x){
         return (int)(250 + x * SCALE_FACTOR);
    }

    public int calcY(double y){
        return (int)(250 - y * SCALE_FACTOR);
    }

}