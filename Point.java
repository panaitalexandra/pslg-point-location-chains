import java.util.*;

public class Point implements Comparable{
    private double x,y;
    private Set b,c;
    private int id;

    public Point(double x,double y){
        this.x=x;
        this.y=y;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public int compareTo(Object o){
        if(y>((Point)o).getY()) return 255;
        if((y==((Point)o).getY())&&(x==((Point)o).getX())) return 0;
        return -256;
    }
    public void setC(TreeSet c){
        this.c=c;
    }
    public void setB(TreeSet b){
        this.b=b;
    }
    public Set getC(){
        return c;
    }
    public Set getB(){
        return b;
    }
}