package BTP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class Squaring {
     
	/**
	 * @param args
	 */
	 static String Name="Carlos-Santana1";
	 String format=".jpg";
	 BufferedImage  image;
	 BufferedImage  image2;
	 static int width;
     static int height;
	 static int[][]  M;
	 TreeSet<Pixel> ts1=new TreeSet<Pixel>();
	 TreeSet<Pixel> ts2=new TreeSet<Pixel>();
	 ArrayList<TreeSet<Pixel>> ts=new ArrayList<TreeSet<Pixel>>();
     Precalculated_square[] square=new  Precalculated_square[72];
     static int[] denomination=new int[20];
     Graphics2D g ;
     static File outputfile = new File("svg//color_"+Name +"ABCD.svg");
     static FileWriter fw;
     int threshold=60;
     int[][][] histogram=new int[256][256][256]; 
     static int colored_points=0;
	public Squaring()
	{
		 //  Calculate the  square  pixels for some fixed denominations 
         denomination[0]=150;
		 denomination[1]=100;
		 denomination[2]=80;
		 denomination[3]=60;
		 denomination[4]=40;
		 denomination[5]=30;
		 denomination[6]=20;
		 denomination[7]=10;
		 denomination[8]=8;
		 denomination[9]=6;
		 denomination[10]=5;
		 denomination[11]=4;
		 
		 
		 for(int i=0; i<=11 ; i++)
		 {
			 for(int j=0; j<6 ; j++)
			 {
				 square[i*6+j]=new Precalculated_square(denomination[i], j*15);
				 		 
			 }
		 }
		 
		 //  Take the input closed curve file  and make an array to manage the squaring process   and marking the foreground ,background
		   try {
		         File input = new File("jpg_outlines//"+Name+""+format);
		         File input2= new File("jpg_outlines//Carlos-Santana2.jpg");
		         image = ImageIO.read(input);
		         image2=ImageIO.read(input2);
		         width = image.getWidth();
		         height = image.getHeight();
		         M=new int[height][width];
		         Color c,c2;
		         
		         for(int i=0; i<height; i++){
		            for(int j=0; j<width; j++){
		               c2=new Color(image2.getRGB(j,i));
		               c = new Color(image.getRGB(j,i));
		               if(c.getRed()==255){ M[i][j]=-1;  image.setRGB(j, i, Color.white.getRGB());}          //white  background
		                else { M[i][j]=-2; image.setRGB(j, i, Color.white.getRGB());}                //black  foreground   
		               }
		            }
		       } catch (Exception e) {}
	   
		   
	       try {
	    	   fw=new FileWriter(outputfile);   
	    	   fw.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\""+width +"\" height=\""+height+"\">\n");
	    	   fw.write("<rect width=\""+width+"\" height=\""+height+"\" style=\"fill:rgb(255,255,255)\"/>");

		       } catch (IOException e) {
			    e.printStackTrace();
		       }
	       
           
	     
	     
	}
	
	//Some Auxiliary Functions
	private void reset_histogram()
	{
		for(int i=0;i<256;i++)
		{
			for(int j=0;j<256;j++)
			{
				for(int k=0;k<256;k++)
				{
					histogram[i][j][k]=0;
				}
			}
		}
	}
	private int xor(int abs, int abs2) {
		if(abs==0 && abs2==1){return 1;}
		else if(abs==1 && abs2==0){return 1;}
		else { return 0; }
		
	}

	private boolean Neighbour(Pixel loc ,int find )
	{
         for(int i=-1; i<=1 ; i++ )
         {
        	 for(int j=-1; j<=1 ; j++ )
                  {
        		    if( xor( Math.abs(i),Math.abs(j) )==1){
        		    	if((loc.y-i)>=0 && (loc.y-i)<height && (loc.x-j)>=0 && (loc.x-j)<width )
           		        {	 
           		              if(M[loc.y-i][loc.x-j]==find)
           		              {
           		    	         return true;
           		              }
           		        }
        		    	else 
        		    	{
        		    		return true;
        		    	}
        		    }
        		       
                  }
         }
		return false;
	}
	
	private int closer_int(double d)
	{
		double y=d-(int)d;
		if(y>0.5){ return (int)(d+1);}
		else if(y<-0.5){ return (int)(d-1);}
		else     { return (int) d; }
	}
	
	
	private int get_precalculated_square_index(int r,int angle)
	{
		int i=0,j=0;
		for(int k=0;k<=10;k++){if(r==denomination[k]){ i=k; break;}}
		j=angle/15;
		return (i*6+j);
	}
	
	private Color get_weighted_mean(int arr[][][],int total_no_points )
	{
		int r=0,g=0,b=0;
		for(int i=0;i<256;i++)
		{
			for(int j=0;j<256;j++)
			{
				for(int k=0;k<256;k++)
				{
					r+=i*arr[i][j][k];
					g+=j*arr[i][j][k];
					b+=k*arr[i][j][k];
				}
			}			
		}
		
		r=r/total_no_points;
		g=g/total_no_points;
		b=b/total_no_points;
		
		return (new Color(r,g,b));
		
		
	}
	
	
	private boolean variation_above_threshold(int arr[][][],int total_no_points)
	{
		System.out.println("variation above thresh");
		Color c=get_weighted_mean(arr, total_no_points);
		int x=0;
		for(int i=0;i<256;i++)
		{
			for(int j=0;j<256;j++)
			{
				for(int k=0;k<256;k++)
				{
					x+=(((Math.abs(i-c.getRed())+Math.abs(i-c.getRed())+Math.abs(i-c.getRed()))/3)*arr[i][j][k]);
				}
			}
		}	
		x/=total_no_points;
		if(x>threshold){System.out.println("discarded********************:"+x);return true;}
		return false;		
	}
	//**********************************************************************************
	// Function to calculate the EDT distance  for first   
	// Intializes M[][]  with EDT values , makes ts store same EDT distance points in same arrays 
	private void distance_calculation()
	{
		Iterator<Pixel> iterator=ts1.iterator();
		for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
            	if(M[i][j]==-2 && Neighbour(new Pixel(j, i),-1))
            	{
            		ts1.add(new Pixel(j, i));
            		M[i][j]=0;	
            	}
            }
		}
		ts.add((TreeSet<Pixel>) ts1.clone());	
		Pixel p;
		int q=0;
		ts2.clear();

		while(true)
		{	
		   if(q%2==0)
		   {	
			   iterator=ts1.iterator();	
			   if(!iterator.hasNext()){ break;}
			   while(iterator.hasNext())
			   {
				   p=iterator.next(); 
				   for(int i=-1; i<=1 ; i++ )
				   {
		        	 for(int j=-1; j<=1 ; j++ )
		                  {
	                          if( xor( Math.abs(i),Math.abs(j) )==1)     
	                          {	                        	    
	                        	int temp_x=p.x-i,temp_y=p.y-j;
	                        	if((temp_y<height && temp_y>-1)&& ( temp_x<width && temp_x >-1) )
	                        	{	
	                        	  if(M[temp_y][temp_x]!=q-1 && M[temp_y][temp_x]!=q)
	                        	  {
	                        		  ts2.add(new Pixel(temp_x,temp_y)); 
	                        		  M[temp_y][temp_x]=q+1;
                             	  }
	                        	} 
	                          }	
	                       }
		          }	 
			   }
			   ts.add((TreeSet<Pixel>) ts2.clone());
			   ts1.clear();
			   q++;
		   }
		   else
		   {
			   iterator=ts2.iterator();	
			   while(iterator.hasNext())
			   {
				  p=iterator.next();  
				  for(int i=-1; i<=1 ; i++ )
			      {
			        for(int j=-1; j<=1 ; j++ )
			        {	
		               if( xor(Math.abs(i),Math.abs(j) )==1 )     
		               {
		                   	int temp_x=p.x-i,temp_y=p.y-j;
		                    if( (temp_y<height && temp_y>-1) && (temp_x<width && temp_x>-1))
		                    {
		                      //System.out.println("abc"+temp_x+" "+temp_y);	
		                      if(M[temp_y][temp_x]!=q-1 && M[temp_y][temp_x]!=q)
		                       {
		                    	 ts1.add(new Pixel(temp_x,temp_y)); 
		                         M[temp_y][temp_x]=q+1;
		                       }
		                     } 
		               }	
		             }
			       }	 
		        }
			   ts.add((TreeSet<Pixel>) ts1.clone());
			   ts2.clear();
			   q++;	
		    }		
		}
		
   }
	
	
	private void draw_square(int M[][],Pixel p,int angle,Color c)
	{ 
		//System.out.println("drawsq"+p.x+" "+p.y);
    	int r=M[p.y][p.x];
    	int temp,temp1 = 0;
    	int[] v_x=new int[4];
    	int[] v_y=new int[4];
    	int red,green,blue;
    	Color c1;
    	int sq_index=get_precalculated_square_index(r,angle);
    	Pixel p1,p2 ;
    	int max=0,max_r=0,max_g=0,max_b=0;
    	if(sq_index>71) { temp1=M[p.y][p.x];
    		              try{ts.get(temp1).remove(p);}
                          catch(Exception e){System.out.println("Ex:1"+e.toString());} 
                          return; 
    	                } 
    	reset_histogram();
    	Iterator<Pixel>  it=square[sq_index].interior_points.iterator();
    	while(it.hasNext())
    	{
    		p1=it.next();
    		p2=new Pixel(p.x+p1.x, p.y+p1.y);
    		temp=M[p2.y][p2.x];
    		 c1=new Color(image2.getRGB(p2.x,p2.y));
             red=c1.getRed();
             green=c1.getGreen();
             blue=c1.getBlue();
             histogram[red][green][blue]++;
    		temp1=M[p.y][p.x];
            if(temp==-1){   try{ts.get(temp1).remove(p);}
                            catch(Exception e){System.out.println("Ex:1"+e.toString());} 
                            return; 
                        }
    	}
    	int total_points=square[sq_index].interior_points.size();
    	boolean flag=false;
    	//boolean flag=variation_above_threshold(histogram,total_points);
    	if(flag){   try{ts.get(temp1).remove(p);}
                    catch(Exception e){System.out.println("Ex:1"+e.toString());} 
    	            return;
    	        }
    	
    	it=square[sq_index].interior_points.iterator();
    	
    	while(it.hasNext())
    	                   { 
    		                 p1=it.next();
    		                 p2=new Pixel(p.x+p1.x, p.y+p1.y);
    		                 temp=M[p2.y][p2.x];
    	                     try{ts.get(temp).remove(p2);}
    	                     catch(Exception e){System.out.println("Ex:"+e.toString());}
    	                     M[p2.y][p2.x]=-1;
    	                    
//    	                     if(histogram[red][green][blue]>max)
//    	                     {
//    	                    	 max=histogram[red][green][blue];
//    	                    	 max_r=red;
//    	                    	 max_g=green;
//    	                    	 max_b=blue;
//    	                     } 
    	                    c1=new Color(image2.getRGB(p2.x,p2.y)); 
    	                    image.setRGB(p2.x,p2.y,c1.getRGB());
    	                   }
    	c= get_weighted_mean(histogram,total_points);
    	it=square[sq_index].boundary_points.iterator();
    	ts1.clear();
    	while(it.hasNext())
    	                  {
    		                p1=it.next();
    		                p2=new Pixel(p.x+p1.x, p.y+p1.y);
    		                ts1.add(p2);
    		                //image.setRGB(p2.x,p2.y,Color.white.getRGB());
    	                  }
    	
        v_x[0]=p.x+closer_int(r*Math.sin(Math.toRadians(angle)));   v_y[0]=p.y+closer_int(r*Math.cos(Math.toRadians(angle)));
    	v_x[1]=p.x+closer_int(r*Math.cos(Math.toRadians(angle)));  v_y[1]=p.y-closer_int(r*Math.sin(Math.toRadians(angle)));
    	v_x[2]=p.x-closer_int(r*Math.sin(Math.toRadians(angle)));   v_y[2]=p.y-closer_int(r*Math.cos(Math.toRadians(angle)));
    	v_x[3]=p.x-closer_int(r*Math.cos(Math.toRadians(angle)));   v_y[3]=p.y+closer_int(r*Math.sin(Math.toRadians(angle)));;

    	try {
    		System.out.println("r:"+r);
			fw.write("<polygon points=\""+v_x[0]+","+v_y[0]+" "+v_x[1]+","+v_y[1]+" "+v_x[2]+","+v_y[2]+" "+v_x[3]+","+v_y[3]+"\"");
			//fw.write(" style=\"fill:rgb("+max_r+","+max_g+","+max_b+")\" />\n");
			fw.write(" style=\"fill:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")\" />\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		return ;
        

	}
	
	private void distance_recalculation()
	{
		Iterator<Pixel> iterator=ts1.iterator();
		Pixel p;
		int q=-1;
		int temp;
		boolean b;
		while(true)
		{	
		   if(q%2==1 || q==-1)
			{	
              
            iterator=ts1.iterator();
			if(!iterator.hasNext()){ break;}
			while(iterator.hasNext())
			{
				p=iterator.next();
				for(int i=-1; i<=1 ; i++ )
				{
					for(int j=-1; j<=1 ; j++ )
					{
                       if( xor( Math.abs(i),Math.abs(j) )==1)     
                        {
                        	int temp_x=p.x-i,temp_y=p.y-j;
                        	if(temp_y<height && temp_x<width )
                        	{	
                        	  if(M[temp_y][temp_x]>q+1 )
                        	  {
                        		  temp=M[temp_y][temp_x];
                        		  p=new Pixel(temp_x,temp_y);
                        		  ts2.add(p); 
                        		  b=ts.get(temp).remove(p);
                        		  M[temp_y][temp_x]=q+1;
                        		  b=ts.get(q+1).add(p);
                         	  }
                        	} 
                        }
	                }
			    } 
		     }
			 
			 ts1.clear();  
			 q++;
			
			}
			else
			{

				iterator=ts2.iterator();
				while(iterator.hasNext())
				{
					p=iterator.next();
					for(int i=-1; i<=1 ; i++ )
					{
						for(int j=-1; j<=1 ; j++ )
						{
	                        if( xor( Math.abs(i),Math.abs(j) )==1)     
	                        {
	                        	int temp_x=p.x-i,temp_y=p.y-j;
	                        	if(temp_y<height && temp_x<width )
	                        	{	
	                        	  if(M[temp_y][temp_x]>q+1 )
	                        	  {
	                        		  temp=M[temp_y][temp_x];
	                        		  p=new Pixel(temp_x,temp_y);
	                        		  ts1.add(p); 
	                        		  b=ts.get(temp).remove(p);
	                        		  M[temp_y][temp_x]=q+1;
	                        		  b=ts.get(q+1).add(p);
	                        	 }
	                           } 
	                        }
		                }
				    }
				 }
				 ts2.clear();  
				 q++;
			     
			}
		}	
		
    }
    
	public void print_mat(String name)
	{
		 PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(name));
			  for(int i=1*(height/5); i<2*(height/5); i++){
		            for(int j=0*(width/5); j<1*(width/5); j++){
		                 if(M[i][j]==-1)  {out.print("*");}
		                 else if(M[i][j]<26){  out.printf("%c",(char)(M[i][j]+(int)'a'));}
		                 else  {  out.printf("%c",(char)(M[i][j]%26+(int)'A'));}
		            }
		            out.println();
		     }
	         out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	              
	}
	
	private void print_stats(String file)
	{
		Iterator<TreeSet<Pixel>>  it=ts.iterator();
		PrintWriter out = null;
		TreeSet<Pixel> t=new TreeSet<Pixel>();
		int i=0;
		try {
			out = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(it.hasNext()){t=it.next(); out.println(i+":"+t.size()); i++;}
		out.close();
		return ;
	}
	private int distance_diff()
	{
		//System.out.println("inside distance_diff");
		int ret=0;
		Iterator<Pixel> iterator=ts1.iterator();
		ts2.clear();
		Pixel p;
		int q=-1;
		int temp;
		boolean b;
		while(true)
		{	
		   if(q%2==1 || q==-1)
			{	
              
            iterator=ts1.iterator();
			if(!iterator.hasNext()){ break;}
			while(iterator.hasNext())
			{
				p=iterator.next();
				for(int i=-1; i<=1 ; i++ )
				{
					for(int j=-1; j<=1 ; j++ )
					{
                       if( xor( Math.abs(i),Math.abs(j) )==1)     
                        {
                        	int temp_x=p.x-i,temp_y=p.y-j;
                        	if(temp_y<height && temp_x<width )
                        	{	
                        	  if(M[temp_y][temp_x]>q+1 )
                        	  {
                        		  temp=M[temp_y][temp_x];
                        		  p=new Pixel(temp_x,temp_y);
                        		  ts2.add(p); 
                        		  ret+=(temp-(q+1));
                         	  }
                        	} 
                        }
	                }
			    } 
		     }
			 
			 ts1.clear();  
			 q++;
			
			}
			else
			{

				iterator=ts2.iterator();
				while(iterator.hasNext())
				{
					p=iterator.next();
					for(int i=-1; i<=1 ; i++ )
					{
						for(int j=-1; j<=1 ; j++ )
						{
	                        if( xor( Math.abs(i),Math.abs(j) )==1)     
	                        {
	                        	int temp_x=p.x-i,temp_y=p.y-j;
	                        	if(temp_y<height && temp_x<width )
	                        	{	
	                        	  if(M[temp_y][temp_x]>q+1 )
	                        	  {
	                        		  temp=M[temp_y][temp_x];
	                        		  p=new Pixel(temp_x,temp_y);
	                        		  ts1.add(p); 
	                        		  ret+=(temp-(q+1));
	                        	 }
	                           } 
	                        }
		                }
				    }
				 }
				 ts2.clear();  
				 q++;
			     
			}
		}	
		return ret;
    }
	private int best_fit_calculation(Pixel p)
	{
		int d=M[p.y][p.x];
		int index;
		Pixel p1,p2;
		int min = 0,temp=Integer.MAX_VALUE,diff=0;
		for(int i=0;i<6;i++)
		{
			index=get_precalculated_square_index(d,i*15);
			Iterator<Pixel> it = square[index].boundary_points.iterator();
	    	ts1.clear();
	    	while(it.hasNext())
	    	                  {
	    		                p1=it.next();
	    		                p2=new Pixel(p.x+p1.x, p.y+p1.y);
	    		                ts1.add(p2);
	    	                  }
	    	diff=distance_diff();
	    	if(temp>diff)
	    	{
	    		temp=diff;
	    		min=i;
	    	}	
		}
		return (min*15);
	}
	private void fill(int k,Color c) 
	{
		   System.out.println("fill");
		    int[] v_x=new int[4];
    	    int[] v_y=new int[4];
		    Pixel p,p1,p2;
		    
		    int d=denomination[k];
		    int temp,flag=0;
		    int sq_index=6*k,i,j=1;
		    Iterator<Pixel>  it;
		    int x;
		    int red,green,blue;
	    	Color c1;
	    	boolean flag2 = false;
	    	int max=0,max_r=0,max_g=0,max_b=0;
		    if(k<11){x=denomination[k+1];}
		    else    {x=denomination[k]-1;}
		    for(j=(denomination[k]-x)-1;j>0;j--)
		    {
		    	int total_points=square[sq_index].interior_points.size();	        	
		        while(!ts.get(d-j).isEmpty()){
		    	p=ts.get(d-j).first();
		    	for(i=0;i<6;i++)
		    	{
		    		flag=1;
		    		it=square[sq_index+i].interior_points.iterator();
		    		reset_histogram(); 
		        	while(it.hasNext())
		        	{
		        		p1=it.next();
		        		p2=new Pixel(p.x+p1.x, p.y+p1.y);
		        		temp=M[p2.y][p2.x];
		        		c1=new Color(image2.getRGB(p2.x,p2.y));
	                    red = c1.getRed();
	                    green=c1.getGreen();
	                    blue=c1.getBlue();
	                    histogram[red][green][blue]++;
		                if(temp==-1){   
		                	flag=0;
		                	break;
		                }
		        	}
		        	//flag2=variation_above_threshold(histogram,total_points);
		        	flag2=false;
		        	if(flag==1 && !flag2 )
		        	{ break;  }
		    	}
		    	
		    	if(flag==1 && !flag2){
		       System.out.println("fills*********************"+k+" "+i); 	
		    	it=square[sq_index+i].interior_points.iterator();
		    	while(it.hasNext())
		    	                   { 
		    		                 p1=it.next();
		    		                 p2=new Pixel(p.x+p1.x, p.y+p1.y);
		    		                 temp=M[p2.y][p2.x];
		    	                     try{ts.get(temp).remove(p2);}
		    	                     catch(Exception e){System.out.println("Ex:"+e.toString());}
		    	                     M[p2.y][p2.x]=-1;
		    	                     //histogram[red][green][blue]++;
//		    	                     if(histogram[red][green][blue]>max)
//		    	                     {
//		    	                    	 max=histogram[red][green][blue];
//		    	                    	 max_r=red;
//		    	                    	 max_g=green;
//		    	                    	 max_b=blue;
//		    	                     } 
		    	                     c1=new Color(image2.getRGB(p2.x,p2.y));		    	                     
		    	                    image.setRGB(p2.x,p2.y,c1.getRGB());		    	                     
		    	                   }
		    	c= get_weighted_mean(histogram,total_points);
                 
		    	it=square[sq_index+i].boundary_points.iterator();
		    	ts1.clear();
		    	while(it.hasNext())
		    	                  {
		    		                p1=it.next();
		    		                p2=new Pixel(p.x+p1.x, p.y+p1.y);
		    		                ts1.add(p2);
		    		                //image.setRGB(p2.x,p2.y,Color.white.getRGB());
		    	                  }
		    	distance_recalculation();
		        v_x[0]=p.x+closer_int(d*Math.sin(Math.toRadians(i*15)));   v_y[0]=p.y+closer_int(d*Math.cos(Math.toRadians(i*15)));
		    	v_x[1]=p.x+closer_int(d*Math.cos(Math.toRadians(i*15)));  v_y[1]=p.y-closer_int(d*Math.sin(Math.toRadians(i*15)));
		    	v_x[2]=p.x-closer_int(d*Math.sin(Math.toRadians(i*15)));   v_y[2]=p.y-closer_int(d*Math.cos(Math.toRadians(i*15)));
		    	v_x[3]=p.x-closer_int(d*Math.cos(Math.toRadians(i*15)));   v_y[3]=p.y+closer_int(d*Math.sin(Math.toRadians(i*15)));;
		    	try {
		    		System.out.println("d:"+d);
					fw.write("<polygon points=\""+v_x[0]+","+v_y[0]+" "+v_x[1]+","+v_y[1]+" "+v_x[2]+","+v_y[2]+" "+v_x[3]+","+v_y[3]+"\"");
					//fw.write(" style=\"fill:rgb("+max_r+","+max_g+","+max_b+")\" />\n");
					fw.write(" style=\"fill:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")\" />\n");	        
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	}
		    	else 
		    	{
		    		ts.get(d-j).remove(p);
		    		//System.out.println("removed"+"left"+ts.get(d-j).size());
		    	}
		    }
		    }    
	}
	public static void main(String[] args) {
		Squaring S=new Squaring();    // Constructor (1)gets some precalculated square pixels , 
		                              //             (2)gets the outline image
	    S.distance_calculation();     //Calculates EDT for first time 
		
		Color[] color=new Color[20];  //Some fixed colour for squares 
		 color[0]=new Color(255,153,204);
		 color[1]=new Color(153,51,255);
		 color[2]=new Color(0,76,153);
		 color[3]=new Color(255,0,0);
		 color[4]=new Color(0,0,255);
		 color[5]=new Color(0,255,0);
		 color[6]=new Color(178,51,50);
		 color[7]=new Color(67,142,200);
		 color[8]=new Color(255,91,165);
		 color[9]=new Color(153,87,205);
		 color[10]=new Color(59,198,182);
		 color[11]=new Color(204,255,0);
         		 

		  int d,i,x;
		  int best_fit_angle=45 ;
          int l;
          Random rn=new Random();
		  for(i=4;i<=11;i++)
		  {	
			 l=0; 
			 d=denomination[i];
			 System.out.println("Now :"+d);
			 if(i<11){x=denomination[i+1];}
			 else    {x=denomination[i]-1;}
			 try{
			 	 
		     while(!S.ts.get(d).isEmpty()){
		    	 System.out.println("with "+d);
		    	 l++;
		    	 //best_fit_angle=S.best_fit_calculation(S.ts.get(d).first());	
		    	 //int rand=rn.nextInt()%6;
		    	 //System.out.println("ra:"+rand);
		    	 //best_fit_angle=15*rand;
		    	 if(i<=7){best_fit_angle=45;}
		    	 else {best_fit_angle=S.best_fit_calculation(S.ts.get(d).first());}
		    	 S.draw_square(S.M,S.ts.get(d).first(),best_fit_angle,color[i]);
			     S.distance_recalculation();
			    
			    }
              S.fill(i,color[i]);
			 }
			 catch (Exception e) {
				
			}
//			 S.print_stats("statsabc"+i+".txt");   
//			 File outputfile = new File("coloured//carlos2//color_"+Name+""+i+"_"+l+"abc.jpg");
//		        try {
//					ImageIO.write(S.image, "jpg", outputfile);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
		  }
		  
		  try {
			fw.write("</svg>");//\n</body>\n</html>
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   File outputfile = new File("coloured//color_"+Name+"last.jpg");
		        try {
					ImageIO.write(S.image, "jpg", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
		System.out.println("Done123");
	}

}
