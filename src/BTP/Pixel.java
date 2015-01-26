package BTP;
public class Pixel implements Comparable<Pixel> {
   public int x;
   public int y;
   //public int visited ;
   
  public Pixel(int a,int b)
  {
	  x=a;
	  y=b;
	  //visited=v;
  }
  
  public int compareTo(Pixel p) {
      
	  
    if((y-p.y)!=0)
     {
   	  return (y-p.y);
     }
     else 
     {
   	  return (x-p.x);
     }
	 
  }
  
}
