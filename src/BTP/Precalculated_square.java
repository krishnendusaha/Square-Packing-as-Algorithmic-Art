package BTP;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class Precalculated_square {

	private int r;
	private int angle;
	public TreeSet<Pixel> interior_points;
	public TreeSet<Pixel> boundary_points;	
	
	private int closer_int(double d)
	{
		double y=d-(int)d;
		if(y>0.5){ return (int)(d+1);}
		else if(y<-0.5){ return (int)(d-1);}
		else     { return (int) d; }
	}
	
	public  Precalculated_square(int r,int angle)
	{
		this.r=r-1;
		this.angle=angle;
		boundary_points=new TreeSet<Pixel>();
		interior_points=new TreeSet<Pixel>();
		float m1,m2,m3;
		int flag=0;
		if(angle>45){angle=90-angle; flag=1; }
		m1=1/(float) Math.tan(Math.toRadians(45-angle));
		m2=1/(float) Math.tan(Math.toRadians(90-angle));
		m3=1/(float) Math.tan(Math.toRadians(180-angle));
		
		float left=0,right=0;
		int int_left,int_right;
		int x1=(-1)*closer_int(r*Math.cos(Math.toRadians(angle)));
		int y1=closer_int(r*Math.sin(Math.toRadians(angle)));
		int x2=closer_int(r*Math.sin(Math.toRadians(angle)));
		int y2=closer_int(r*Math.cos(Math.toRadians(angle)));
		
		if(angle==45){
			for(int i=x1;i<=(-1)*x1;i++)
			{
				for(int j=(-1)*y1;j<=y1;j++)
				{
					interior_points.add(new Pixel(i,j));
					if(Math.abs(i)==(-1)*x1 || Math.abs(j)==y1 )
					{	
						boundary_points.add(new Pixel(i, j));
					}
				}
			}
			
		}
		else
		{
		int prev_left = 0;
		for (int i = 0; i <=y1 ; i++) {
			int_left=closer_int(left);
			int_right=closer_int(right);
		 	//System.out.println(i+":"+left+" "+right+" "+int_left+" "+int_right);
			for(int j=int_left;j<=int_right;j++){interior_points.add(new Pixel(j,i));}
			if(i==y1){prev_left=int_left;}
			left+=m3;
			right+=m2;			
		}
		left=x1+m1;
		if(angle==0){prev_left=(-1)*r;}
		for (int i = y1+1; i <=y2 ; i++) {
			int_left=closer_int(left);
			int_right=closer_int(right);
			//System.out.println(i+":&"+left+" "+right+" "+int_left+" "+int_right);
			for(int j=int_left;j<=int_right;j++){  interior_points.add(new Pixel(j,i));}
			if(int_left<int_right)
			{
				for (int j = prev_left; j <= int_left; j++) {
					boundary_points.add(new Pixel(j,i-1));	
				}	
				prev_left=int_left;
			}
			else
			{
				for (int j = prev_left; j <= int_right; j++) {
					boundary_points.add(new Pixel(j,i-1));	
				}
				
			}
			left+=m1;
			right+=m2;			
		    
		}	
		
		Iterator<Pixel>  it=interior_points.iterator();
		Pixel p;
		TreeSet<Pixel> temp=new TreeSet<Pixel>();
	  
		
        
		if(flag==1)
		{
		it=interior_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.x,p.y)) ; }
		interior_points.removeAll(interior_points);
		interior_points.addAll(temp);
		temp.removeAll(temp);

		it=boundary_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.x,p.y)) ; }
		boundary_points.removeAll(boundary_points);
		boundary_points.addAll(temp);
		temp.removeAll(temp);

		}
		
				
        
        it=interior_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.x,(-1)*p.y )); }
		interior_points.addAll(temp);
		temp.removeAll(temp);

		it=interior_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.y,p.x)) ; }
		interior_points.addAll(temp);
		temp.removeAll(temp);
	    
		it=boundary_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.x,(-1)*p.y )); }
	    boundary_points.addAll(temp);
		temp.removeAll(temp);

		it=boundary_points.iterator();
		while(it.hasNext()){ p=it.next(); temp.add(new Pixel((-1)*p.y,p.x )); }
	    boundary_points.addAll(temp);
		temp.removeAll(temp);

		
		}		
		
//		PrintWriter out2 = null;
//		int k=0,l=0;
//		try {
//			out2 = new PrintWriter(new FileWriter("points30.txt"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		out2.println("type: "+flag);
//		Iterator<Pixel> it = interior_points.iterator();
//		Pixel p;
//		while(it.hasNext()){ p=it.next(); out2.println("int"+k+": "+p.x+" "+p.y); k++; }
//		
//		
//		it=boundary_points.iterator();
//		while(it.hasNext()){ p=it.next(); out2.println("bound"+l+": "+p.x+" "+p.y); l++; }
//		
//        out2.close();	
	}	
	
	
	
//	public static void main(String[] args) {
//		
//		Precalculated_square p=new Precalculated_square(10,30);
//		PrintWriter out = null;
//		File output=new File("out30.jpg");
//		BufferedImage image=new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
//		
//		try {
//			out = new PrintWriter(new FileWriter("square_10_30.txt"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		////////////
//		for(int i=0;i<100;i++)
//		{
//			for(int j=0;j<100;j++)
//			{			   
//			   if( p.interior_points.contains(new Pixel(j-50, i-50))) { out.print("*");}
//			   else{out.print(" ");}
//			}
//			out.println();
//		}
//		out.close();
//		
//		/////////
//		Graphics g=image.createGraphics();
//		g.setColor(Color.white);
//		g.fillRect(0, 0, 200, 200);
//		
//		for(int i=0;i<100;i++)
//		{
//			for(int j=0;j<100;j++)
//			{			   
//			   if( p.interior_points.contains(new Pixel(j-50, i-50))) { image.setRGB(j,i,Color.black.getRGB());}
//			   else{}
//			}
//			out.println();
//		}
//		try {
//			ImageIO.write(image, "jpg", output);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		
//		System.out.println("Done");
//		
//
//	}

}
