public class Edge implements Comparable{
    private int number,f1,f2,p1,p2, weight;
    private Point v1,v2;
    public Edge(int number, Point v1, Point v2, int f1, int f2, int p1, int p2){
        this.number = number;
        this.v1=v1;
        this.v2=v2;
        this.f1=f1;
        this.f2=f2;
        this.p1=p1;
        this.p2=p2;
    }
    public int getNumber(){
        return number;
    }
    public Point getV1(){
        return v1;
    }
    public Point getV2(){
        return v2;
    }
    public int getF1(){
        return f1;
    }
    public int getF2(){
        return f2;
    }
    public int getP1(){
        return p1;
    }
    public int getP2(){
        return p2;
    }
    public int compareTo(Object o){
        Edge m=(Edge)o;
        if (f1==0) return -256;
        if (f2==0) return +255;
        if ((v1.equals(m.getV1()))&&(v2.equals(m.getV2()))) return 0;
        if (f2==m.getF1()) return -256;
        return 255;
    }
    public void reverse(){
        Point paux;
        paux=v1;
        v1=v2;
        v2=paux;

        f1=f1+f2;
        f2=f1-f2;
        f1=f1-f2;

        p1=p1+p2;
        p2=p1-p2;
        p2=p1-p2;
    }
    public void setWeight(int number){
        weight = number;
    }
    public int getWeight(){
        return weight;
    }
}
