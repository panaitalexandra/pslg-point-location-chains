import java.util.*;
import java.util.function.Consumer;

public class Algorithm {
        private Point[] points;
    private Edge[] dcel;
    private List sweepline;

    private double x, y;
    private Edge binaryEdge;
    private ArrayList binaryChain;
    private ArrayList[] chains;
    private int middleIndex = 0;
    private Consumer<String> logger;

    public Algorithm(Point[] points, Edge[] dcel, Consumer<String> logger){
        this.points = points;
        this.dcel = dcel;
        this.logger = logger;
        // PREPROCESAREA
        Arrays.sort(points); //Ordonarea Varfurilor: sortate varfuri de jos ın sus strict dupa coordonata Y
        swapEdges(); // Orientarea Muchiilor
        listEdges();
        calculateWeights(); // Balansarea Ponderilor
        findChains(); // Generarea lanțurilor ($y$-monotone)
    }

    private void swapEdges(){
        for(int i = 0; i < dcel.length; ++i){
            if(((dcel[i].getV1()).compareTo(dcel[i].getV2())) > 0)
                dcel[i].reverse();
        }
    }

    public void listEdges(){
        for(int i = 0; i < points.length; ++i){
            TreeSet c = new TreeSet();
            TreeSet b = new TreeSet();
            for(int j = 0; j < dcel.length; ++j){
                if(((dcel[j].getV1()).compareTo(points[i])) == 0)
                    c.add(dcel[j]);
                if(((dcel[j].getV2()).compareTo(points[i])) == 0)
                    b.add(dcel[j]);
            }
            Iterator it = c.iterator();
            System.out.print("+" + (i + 1) + "\t:");
            while(it.hasNext())
                System.out.print(" " + ((Edge)(it.next())).getNumber());
            System.out.println();
            points[i].setC(c);
            it = b.iterator();
            System.out.print("-" + (i + 1) + "\t:");
            while(it.hasNext())
                System.out.print(" " + ((Edge)(it.next())).getNumber());
            System.out.println();
            points[i].setB(b);
            for(int l = 0; l < dcel.length; ++l){
                if(dcel[l].getV1().compareTo(points[i]) == 0) {
                    dcel[l].getV1().setB(b);
                    dcel[l].getV1().setC(c);
                }
                if(dcel[l].getV2().compareTo(points[i]) == 0) {
                    dcel[l].getV2().setB(b);
                    dcel[l].getV2().setC(c);
                }
            }
        }
    }

    public void calculateWeights(){

        for(int i = 0; i < dcel.length; ++i) // Initializare: w := 1, pentru fiecare muchie
            dcel[i].setWeight(1);


        for(int i = 1; i < points.length - 1; ++i) { // Măturare de Jos în Sus (Top-Down Sweep)
            TreeSet IN = (TreeSet)(points[i].getB());
            int wIn = 0;
            Iterator it = IN.iterator();
            while(it.hasNext()){
                Edge edge = (Edge)(it.next());
                wIn += edge.getWeight();
            }
            TreeSet OUT = (TreeSet)(points[i].getC());
            int wOut = 0;
            it = OUT.iterator();
            while(it.hasNext()){
                Edge edge = (Edge)(it.next());
                wOut += edge.getWeight();
            }
            if (wIn > wOut) {
                it = OUT.iterator();
                Edge edge = (Edge)(it.next());
                edge.setWeight(1 + wIn - wOut);
            }
        }
        for(int i = points.length - 1; i > 0; --i) { // Măturare de Sus în Jos (Bottom-Up Sweep)
            TreeSet IN = (TreeSet)(points[i].getB());
            int wIn = 0;
            Iterator it = IN.iterator();
            while(it.hasNext()){
                Edge edge = (Edge)(it.next());
                wIn += edge.getWeight();
            }
            TreeSet OUT = (TreeSet)(points[i].getC());
            int wOut = 0;
            it = OUT.iterator();
            while(it.hasNext()){
                Edge edge = (Edge)(it.next());
                wOut += edge.getWeight();
            }
            if (wIn < wOut) {
                it = IN.iterator();
                Edge edge = (Edge)(it.next());
                edge.setWeight(edge.getWeight() + wOut - wIn);
            }
        }
        logger.accept(" EDGE WEIGHTS:");
        logger.accept("-----------------------");

        for (int i = 0; i < dcel.length; ++i) {
            logger.accept("  • " + dcel[i].getNumber() + " → " + dcel[i].getWeight());
        }

        logger.accept("");

    }

    public void findChains(){ // Generarea lanțurilor ($y$-monotone)
        int count = 0;
        TreeSet OUT = (TreeSet)points[0].getC();
        Iterator it = OUT.iterator();
        while(it.hasNext())
            count += ((Edge)(it.next())).getWeight();

        chains = new ArrayList[count];
        it = OUT.iterator();

        int i = -1;
        while(it.hasNext()){
            Edge edge = (Edge)(it.next());
            while(edge.getWeight() != 0){
                ++i;
                chains[i] = new ArrayList();
                createChain(edge, i);
            }
        }
        logger.accept("CHAIN LIST:");
        for (int k = 0; k < count; ++k) {
            StringBuilder chainLog = new StringBuilder("  " + (k+1) + ") start");

            Object[] array = chains[k].toArray();
            for (int j = 0; j < array.length; ++j)
                chainLog.append(" -> " + ((Edge)array[j]).getNumber());

            logger.accept(chainLog.toString());
        }

    }

    public void createChain(Edge edge, int i){ // construiește un lanț individual, pas cu pas, de la un vârf la următorul
        chains[i].add(edge);
        edge.setWeight(edge.getWeight() - 1);
        if(edge.getV2().compareTo(points[points.length - 1]) == 0) return;
        else{
            Point nextPoint = (Point)(edge.getV2());
            TreeSet o = (TreeSet)(nextPoint.getC());

            Iterator it = o.iterator();
            Edge edge1 = null;
            while(it.hasNext()){
                edge1 = (Edge)(it.next());
                if(edge1.getWeight() > 0) break;
            }
            createChain(edge1, i);
        }
    }

    public int findChain(double x, double y){
        this.x = x; this.y = y;

        binarySearchChain(0, chains.length - 1);
        Object[] array = binaryChain.toArray();
        Point[] array1 = new Point[array.length + 1];

        for(int i = 0; i < array.length; ++i)
            array1[i] = ((Edge)array[i]).getV1();

        Edge edge = null;
        array1[array1.length - 1] = ((Edge)array[array.length - 1]).getV2();
        Point searchPoint = new Point(x, y);
        int number = Math.abs(Arrays.binarySearch(array1, searchPoint));

        if (number <= 1) {edge = (Edge)array[0];}
        if (number >= array1.length) edge = (Edge)array[array.length - 1];
        if ((number > 1) && (number < array1.length)) {edge = (Edge)array[number - 2];}

        System.out.println(number);
        binaryEdge = edge;
        int faceIndex = 0;

        if(binaryEdge == null)
            return 0;
        if(calcDet(binaryEdge.getV1().getX(), binaryEdge.getV1().getY(), x, y, binaryEdge.getV2().getX(), binaryEdge.getV2().getY()) > 0)
            faceIndex = binaryEdge.getF2();
        else
            faceIndex = binaryEdge.getF1();

        return faceIndex;
    }

    public ArrayList[] getChains(){
        ArrayList[] array = new ArrayList[2];
        array[0] = binaryChain;
        if(calcDet(binaryEdge.getV1().getX(),
                binaryEdge.getV1().getY(),
                x,
                y,
                binaryEdge.getV2().getX(),
                binaryEdge.getV2().getY()
        ) > 0)
            array[1] = chains[middleIndex + 1];
        else array[1] = chains[middleIndex - 1];
        return array;
    }

    public ArrayList[] getChainList(){
        return chains;
    }

    public ArrayList getSingleChain(){
        return binaryChain;
    }

    public void binarySearchChain(int start, int end){ // Localizare M prin cautare binara
        int middle = (start + end) / 2; // Calculează lanțul de la mijloc
        if(start < end){
            Object[] array = chains[middle].toArray();
            Point[] array1 = new Point[array.length + 1];
            for(int i = 0; i < array.length; ++i){
                array1[i] = ((Edge)array[i]).getV1();
            }
            array1[array1.length - 1] = ((Edge)array[array.length - 1]).getV2();
            Point searchPoint = new Point(x, y);
            int number = Math.abs(Arrays.binarySearch(array1, searchPoint)); // nr -> number
            Edge edge = null;
            if (number <= 1) edge = (Edge)array[0];
            if (number >= array1.length) edge = (Edge)array[array.length - 1];
            if ((number > 1) && (number < array1.length)) edge = (Edge)array[number - 2];
            /*Dacă determinantul este pozitiv (> 0), punctul $(x, y)$ se află la dreapta muchiei (sau pe muchia "de deasupra"),
            deci căutarea continuă în jumătatea din dreapta (binarySearchChain(middle + 1, end)).
            Lanțurile sunt ordonate de la stânga la dreapta.*/
            if (calcDet(edge.getV1().getX(), edge.getV1().getY(), x, y, edge.getV2().getX(), edge.getV2().getY()) > 0)
                binarySearchChain(middle + 1, end); // Fața din dreapta
            else
                binarySearchChain(start, middle - 1); // Fața din stânga
        }
        else {
            binaryChain = chains[middle];
            System.out.println("middle index is: " + middle);
            this.middleIndex = middle;
        }
    }
    public Point[] getPoints(){
        return points;
    }
    public int findSlab(double x, double y){
        return ~Arrays.binarySearch(points, new Point(x, y));
    }

    public double calcDet(double x1, double y1, double x2, double y2, double x3, double y3){
        return x1 * y2 + x2 * y3 + x3 * y1 - y2 * x3 - y3 * x1 - y1 * x2;
    }

    public Edge[] getDCEL(){
        return dcel;
    }
}