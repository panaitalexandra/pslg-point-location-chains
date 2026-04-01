import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class GraphicInterface extends JFrame {
    private Drawing canvas;
    private JButton readButton;
    private JTextArea logArea, locationArea;
    private JScrollPane logScrollPane;
    private Set pointSet, edgeSet;
    private ButtonListener buttonListener;

    public GraphicInterface(){
        super("Chains Method - Visualization");
        this.getContentPane().setBackground(new Color(236, 234, 228));
        this.setLayout(new BorderLayout());

        buttonListener = new ButtonListener();
        canvas = new Drawing();
        canvas.setPreferredSize(new Dimension(650, 500));

        readButton = new JButton("CLICK ME FOR MAGIC");
        readButton.addActionListener(buttonListener);
        readButton.setBackground(new Color(255, 225, 180));

        locationArea = new JTextArea(4, 10);
        locationArea.setEditable(false);
        locationArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        locationArea.setText("\nFirst, let the magic happen.\nThen click on the canvas to locate a point.");        locationArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        locationArea.setBackground(new Color(236, 234, 228));

        logArea = new JTextArea(20, 30);
        logArea.setEditable(false);
        logArea.setBackground(new Color(250, 241, 229));
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(300, 500));
        logScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 219, 170);
                this.trackColor = new Color(236, 234, 228);
            }
        });
        logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 219, 170);    // culoarea cursorului orizontal
                this.trackColor = new Color(236, 234, 228);    // fundal
            }
        });

        canvas.setLocationLogger(this::updateLocation);

        JPanel rightOutputPanel = new JPanel(new BorderLayout());
        rightOutputPanel.add(locationArea, BorderLayout.NORTH);  // Adaugă localizarea (mică) în partea de Sus
        rightOutputPanel.add(logScrollPane, BorderLayout.CENTER); // Adaugă Log-ul (mare) în Centru, sub localizare

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.DARK_GRAY);

        centerPanel.add(canvas, BorderLayout.CENTER); // Adaugă Canvas în Centru (partea mare)

        centerPanel.add(rightOutputPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(236, 235, 228));

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(236, 236, 228));
        inputPanel.add(readButton);

        bottomPanel.add(inputPanel);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.pack();
    }
    public void appendToLog(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void updateLocation(String text) {
        locationArea.setText(text);
    }
    public class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Edge[] edgeArray = null;
            Point[] pointArray = null;

            if (e.getSource() == readButton) {
                String filename = "chains.txt";
                pointSet = new TreeSet();
                edgeSet = new TreeSet();
                logArea.setText("");
                locationArea.setText("\nReading data...\nStay close ^^");

                try {
                    StreamTokenizer st = new StreamTokenizer(new BufferedReader(new FileReader(filename)));
                    st.nextToken();
                    int edgeCount = (int) st.nval;

                    for (int i = 0; i < edgeCount; ++i) {
                        st.nextToken();
                        int x = (int) st.nval;
                        st.nextToken();
                        int y = (int) st.nval;
                        Point point1 = new Point(x, y);
                        pointSet.add(point1);

                        st.nextToken();
                        int x1 = (int) st.nval;
                        st.nextToken();
                        int y1 = (int) st.nval;
                        Point point2 = new Point(x1, y1);
                        pointSet.add(point2);

                        st.nextToken();
                        int f1 = (int) st.nval;
                        st.nextToken();
                        int f2 = (int) st.nval;
                        st.nextToken();
                        int p1 = (int) st.nval;
                        st.nextToken();
                        int p2 = (int) st.nval;

                        edgeSet.add(new Edge(i, point1, point2, f1, f2, p1, p2));
                    }
                } catch (FileNotFoundException fnfe) {
                    JOptionPane.showMessageDialog(null, "File not found!", "Error Message", JOptionPane.WARNING_MESSAGE);
                    appendToLog("ERROR: File not found!");
                    return;
                } catch (IOException eeee) {
                    System.out.println("Problems loading data!");
                    appendToLog("ERROR: Problems loading data!");
                    return;
                }

                pointArray = new Point[pointSet.size()];
                System.out.println("Number of points: " + pointSet.size());
                Iterator it = pointSet.iterator();
                for (int i = 0; i < pointSet.size(); ++i)
                    pointArray[i] = (Point) (it.next());


                edgeArray = new Edge[edgeSet.size()];
                it = edgeSet.iterator();
                for (int i = 0; i < edgeSet.size(); ++i)
                    edgeArray[i] = (Edge) (it.next());

            }
            if (pointArray != null) {
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i].setId(i + 1);
                }
            }

            Algorithm alg = new Algorithm(pointArray, edgeArray, (text) -> appendToLog(text));
            canvas.activateMouse(alg);
        }
    }
}