package BTP;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList; 					
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class Squaring_fg_bg {
	 static String Name1="scarlet-macaw-parrot-1";  //outline image name
	 static String Name2="scarlet-macaw-parrot-2";  //corresponding color  image name
	 static String format=".jpg";                  //format of images
	 BufferedImage  image1;                 //to store and manipulate input outline image  
	 BufferedImage  image2;                 //to store the corresponding colour image 
	 static int width; 						//width of images both outline and colour 
     static int height;						//height of images both outline and colour 
	 static int[][]  M;						//stores the N4 distance from background of each pixel 
	 TreeSet<Pixel> ts1=new TreeSet<Pixel>();                        //temporary manipulation of distances  
	 TreeSet<Pixel> ts2=new TreeSet<Pixel>();                        //temporary manipulation of distances
	 ArrayList<TreeSet<Pixel>> ts=new ArrayList<TreeSet<Pixel>>();   //stores treesets of different N4 distance  pixels  
     Precalculated_square[] square=new  Precalculated_square[72];    //stores square pixels positions relative to centre for different radius and angles
     static int[] denomination=new int[20];                          //different square sizes , it is half diagonal   
     
     static File svg_file = new File("svg_new//svg_"+Name1 +"B.svg");
     static FileWriter fw;
     int threshold=60;
     int[][][] histogram=new int[256][256][256];					//color histogram  of points inside a square  
     static PrintWriter log;
	 static int coloured_points=0;
	 static int outline_points=0;
     
     public Squaring_fg_bg()
 	 {
    	 //  Calculate the  square  pixels for some fixed denominations 
         denomination[0]=200;
		 denomination[1]=150;
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
		         File input1 = new File("outlines_new//"+Name1+""+format);
		         File input2= new File("outlines_new//"+Name2+""+format);
		         image1 = ImageIO.read(input1);
		         image2=ImageIO.read(input2);
		         width = image1.getWidth();
		         height = image1.getHeight();
		         M=new int[height][width];
		           
		       } catch (Exception e) {}
		   
//		   Color c1,c2;		      
//		   for(int i=0; i<height; i++){
//	            for(int j=0; j<width; j++){
//	               c2=new Color(image2.getRGB(j,i));
//	               c1= new Color(image1.getRGB(j,i));
//	               if(c1.getRed()==255){ M[i][j]=-1;  image1.setRGB(j, i, Color.white.getRGB());}  //white  background
//	                else { M[i][j]=-2; image1.setRGB(j, i, Color.white.getRGB()); outline_points++;}                //black  foreground   
//	               }
//	            }
//		   
		//Initializing the svg file    
	       try {
	    	   fw=new FileWriter(svg_file);   
	    	   fw.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\""+width +"\" height=\""+height+"\">\n");
	    	   fw.write("<rect width=\""+width+"\" height=\""+height+"\" style=\"fill:rgb(255,255,255)\"/>");

		       } catch (IOException e) {
			    e.printStackTrace();
		       }
		       
	    //file to print log   
	      try {
				log = new PrintWriter(new FileWriter("log1.txt"));
			    }
			catch(Exception e){}
			
 	 }
   //****************end of constructor  
     
     private void mark_foreGround()
     {
  	   Color c1,c2;		      
  	   for(int i=0; i<height; i++){
              for(int j=0; j<width; j++){
                 c2=new Color(image2.getRGB(j,i));
                 c1= new Color(image1.getRGB(j,i));
                 if(c1.getRed()==255){ M[i][j]=-1;  image1.setRGB(j, i, Color.white.getRGB());}  //white  background
                  else { M[i][j]=-2; image1.setRGB(j, i, Color.YELLOW.getRGB()); outline_points++;}                //black  foreground   
                 }
              }
     }
     
     
     private void mark_backGround()
     {
  	   Color c1,c2;		      
  	   for(int i=0; i<height; i++){
              for(int j=0; j<width; j++){
                 c2=new Color(image2.getRGB(j,i));
                 c1= new Color(image1.getRGB(j,i));
                 if(c1.getRed()==255){ M[i][j]=-2;  }                   //white  background
                  else { M[i][j]=-1; }                //black  foreground   
                 }
              }
     }
   //Some Auxiliary Functions
   //1.Set all values of histogram  to zero    
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
 	
 	//2.Xor function needed for N4 position detection
 	private int xor(int abs, int abs2) {
 		if(abs==0 && abs2==1){return 1;}
 		else if(abs==1 && abs2==0){return 1;}
 		else { return 0; }
 		
 	}
    
 	//3.For a pixel loc it searches pixel with value find in M[][]  returns true/false 
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
 	//4. Rounds up double d
 	private int closer_int(double d)
 	{
 		int  y=(int) Math.round(d);
 		return y;
// 		double y=d-(int)d;
// 		if(y>0.5){ return (int)(d+1);}
// 		else if(y<-0.5){ return (int)(d-1);}
// 		else     { return (int) d; }
 	}
 	
 	//5. Finds the index of Precalculated square array for a particular r and angle 
 	private int get_precalculated_square_index(int r,int angle)
 	{
 		int i=0,j=0;
 		for(int k=0;k<=10;k++){if(r==denomination[k]){ i=k; break;}}
 		j=angle/15;
 		return (i*6+j);
 	}
 	
 	//6.Calculates the weighted mean of colours of square interior points   
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
 	
 	//7.Calculates variance of colours of square interior points returns true(/false) if variance less than a pre set threshold
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
 	//9. Calculates the sum  of  difference (of distance values before and after distance recalculation)  
 	private int distance_diff()
	{
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
 	//10.For a given pixel p it gives the  angle that is best fitting  
 	private int best_fit_calculation(Pixel p,int d)
	{
		int index;
		System.out.println("best_fit");
		int d1=M[p.y][p.x];
		Pixel p1,p2;
		int min = -1,temp=Integer.MAX_VALUE,diff=0;
		int temp_x,temp_y;
		int flag;
		for(int i=0;i<6;i++)
		{
			flag=0;
			index=get_precalculated_square_index(d,i*15);
	     	Iterator<Pixel> it = square[index].boundary_points.iterator();
	    	ts1.clear();
	    	while(it.hasNext())
	    	                  {
	    		                p1=it.next();
	    		                temp_x=p.x+p1.x;
	    		                temp_y=p.y+p1.y;
	    		                if( (temp_x<0||temp_x>=width) ||(temp_y<0||temp_y>=height))
	    		                {
	    		                	if(M[temp_x][temp_y]==-1){flag=1; break;}
	    		                }
	    		                p2=new Pixel(temp_x,temp_y);
	    		                ts1.add(p2);
	    	                  }
	    	if(flag==1){continue;}
	    	diff=distance_diff();
	    	if(temp>diff)
	    	{
	    		temp=diff;
	    		min=i;
	    	}	
		}
		
		if(min==-1){ try{ts.get(d1).remove(p);}catch(Exception e){log.println("Ln No.334");} return -1;}
		System.out.println("best_fit_exit");
		return (min*15);
	}
 	//**********************************************************************************
 		//Main Functions 
 	    // Function to calculate the N4  distance from boundary of each foreground points    
 		//1. Intializes M[][]  with N4 distance values , makes ts (ArrayList<Treeset>) store same N4 distance points in same arrays 
 		private void distance_calculation()
 		{
 			log.println("distance_calc started");
 			Iterator<Pixel> iterator=ts1.iterator();
 			for(int i=0; i<height; i++){
 	            for(int j=0; j<width; j++){
 	            	if(M[i][j]==-2 && Neighbour(new Pixel(j, i),-1)) //M[i][j]==2 if (j,i)  is a background pixel 
 	            	{	                                             //&&  it has a -1 value in neighbour means it is a boundary point  	            
 	            		ts1.add(new Pixel(j, i));
 	            		M[i][j]=0;	                                 //set boundary points N4 distance to 0
 	            	}
 	            }
 			}
 			ts.add((TreeSet<Pixel>) ts1.clone());	                 //add the pixel set to array
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
 		                          if( xor( Math.abs(i),Math.abs(j) )==1)    //N4 connected  
 		                          {	                        	    
 		                        	int temp_x=p.x-i,temp_y=p.y-j;
 		                        	if((temp_y<height && temp_y>-1)&& ( temp_x<width && temp_x >-1) ) //inside the image 
 		                        	{	
 		                        	  if(M[temp_y][temp_x]!=q-1 && M[temp_y][temp_x]!=q)	// this pixel is not previously visited 		  
 		                        	  {
 		                        		  ts2.add(new Pixel(temp_x,temp_y)); 
 		                        		  M[temp_y][temp_x]=q+1;
 	                             	  }
 		                        	} 
 		                          }	
 		                       }
 			          }	 
 				   }
 				   ts.add((TreeSet<Pixel>) ts2.clone());                //add the treeset to ts ArraylList
 				   ts1.clear();                                         //clear ts1 for next loop
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
 			               if( xor(Math.abs(i),Math.abs(j) )==1 )      //N4 connected 
 			               {
 			                   	int temp_x=p.x-i,temp_y=p.y-j;
 			                    if( (temp_y<height && temp_y>-1) && (temp_x<width && temp_x>-1)) //inside the image 
 			                    {
 			                      if(M[temp_y][temp_x]!=q-1 && M[temp_y][temp_x]!=q)			// this pixel is not previously visited 	
 			                       {
 			                    	 ts1.add(new Pixel(temp_x,temp_y)); 
 			                         M[temp_y][temp_x]=q+1;
 			                       }
 			                     } 
 			               }	
 			             }
 				       }	 
 			        }
 				   ts.add((TreeSet<Pixel>) ts1.clone());			  //add the treeset to ts ArraylList
 				   ts2.clear();										  //clear ts2 for next loop
 				   q++;	
 			    }		
 			}
 			log.println("distance_calc ended ");
 	   }
 		
 		//2.Draw a square of Color c with centre at Pixel p 
 		private void draw_square(int r,Pixel p,int angle,Color c,int flag1)
 		{ 
 			//int r=M[p.y][p.x];
 			log.println("r:"+r+"a:"+angle);
 			System.out.println("draw_sq");
 	    	int temp,temp1 = 0;
 	    	int[] v_x=new int[4];    //stores  x coordinates of square vertices 
 	    	int[] v_y=new int[4];    //stores  y coordinates of square vertices
 	    	int red,green,blue;
 	    	Color c1;
 	    	int sq_index=get_precalculated_square_index(r,angle);
 	    	Pixel p1,p2 ;
 	    	int max=0,max_r=0,max_g=0,max_b=0;
 	    	if(sq_index>71) { System.out.println("Sq_index out of range");
 	    		              temp1=M[p.y][p.x];
 	    		              try{ts.get(temp1).remove(p);}catch(Exception e){System.out.println("ln no. 439");} 
 	                          return; 
 	    	                } 
 	    	System.out.println("draw_sq2");
 	    	reset_histogram();
 	    	Iterator<Pixel>  it=square[sq_index].interior_points.iterator();
 	    	while(it.hasNext())
 	    	{
 	    		System.out.println("draw_sq3");
 	    		p1=it.next();
 	    		p2=new Pixel(p.x+p1.x, p.y+p1.y);
 	    		temp=M[p2.y][p2.x];
 	    		 c1=new Color(image2.getRGB(p2.x,p2.y));
 	             red=c1.getRed();
 	             green=c1.getGreen();
 	             blue=c1.getBlue();
 	             histogram[red][green][blue]++;
 	    		temp1=M[p.y][p.x];
 	            if(temp==-1){   try{ts.get(temp1).remove(p);}catch(Exception e){System.out.println("ln NO 456");}
 	                            System.out.println("return");
 	                            return; 
 	                        }
 	    	}
 	    	int total_points=square[sq_index].interior_points.size();
 	    	boolean flag=false;
 	    	System.out.println("draw_sq4");
 	    	//boolean flag=variation_above_threshold(histogram,total_points);
 	    	if(flag){   try{ts.get(temp1).remove(p);}catch(Exception e){System.out.println("ln NO 464");} 
 	    	            return;
 	    	        }
 	    	System.out.println("draw_sq5");
 	    	it=square[sq_index].interior_points.iterator();
 	    	while(it.hasNext())
 	    	                   { 
 	    		                 p1=it.next();
 	    		                 p2=new Pixel(p.x+p1.x, p.y+p1.y);
 	    		                 temp=M[p2.y][p2.x];
 	    	                     try{ts.get(temp).remove(p2);}catch(Exception e){System.out.println("ln NO 472");}
 	    	                     M[p2.y][p2.x]=-1;
 	    	                    
// 	    	                     if(histogram[red][green][blue]>max)
// 	    	                     {
// 	    	                    	 max=histogram[red][green][blue];
// 	    	                    	 max_r=red;
// 	    	                    	 max_g=green;
// 	    	                    	 max_b=blue;
// 	    	                     } 
 	    	                    c1=new Color(image2.getRGB(p2.x,p2.y)); 
 	    	                    if(flag1==1){ image1.setRGB(p2.x,p2.y,c1.getRGB());}
 	    	                    else{ image1.setRGB(p2.x,p2.y,c.getRGB());}
 	    	                   }
 	    	System.out.println("draw_sq6");
 	    	if(flag1==1){c= get_weighted_mean(histogram,total_points);  System.out.println("*******");}
 	    	
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
 				coloured_points+=square[sq_index].interior_points.size();
 				System.out.println("dr_sq_ends");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    		return ;
 	        

 		}
       //3.recalculates the N4 distances of points after drawing a square   
 		private void distance_recalculation()
 		{
 			log.println("distance recalc");
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
 	                        	if(temp_y>-1 && temp_y<height && temp_x>-1 && temp_x<width )
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

 		public static void main(String[] args) {
 			Squaring_fg_bg S=new Squaring_fg_bg();    // Constructor (1)gets some precalculated square pixels , 
 			                              //             (2)gets the outline image
 		    S.mark_foreGround();
 			S.distance_calculation();     //Calculates N4 distance of fore ground for first time 
 			
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
 	         		 

 			  int d,i,j,x;
 			  int best_fit_angle=45 ;
 	          
 	          for(i=4;i<=11;i++)
 			  {	
 				 d=denomination[i];
 				 
 				 log.println("Now :"+d);
// 				 if(i<11){x=denomination[i]-1;}
// 				 else    {x=denomination[i]-1;}
 				 try{
 				  // for(j=x+1;j<=d;j++)
 				  // {
 					  while(!S.ts.get(d).isEmpty()){
 						  
 	 			    	 System.out.println("with "+d);
 	 			    	 if(i<8){ best_fit_angle=45;}//best_fit_angle=S.best_fit_calculation(S.ts.get(d).first(),d);}
 	 			    	 else{ best_fit_angle=45; }
 	 			    	 System.out.println("best_fit_"+best_fit_angle);
 	 			    	 if(best_fit_angle==-1){System.out.println("hello");continue;}
 	 			    	 S.draw_square(denomination[i],S.ts.get(d).first(),best_fit_angle,color[i],1);
 	 				     S.distance_recalculation();
 	 				    
 	 				    }
 	 	    
 				  // }
 			          //     S.fill(i,color[i]);
 				 }
 				 catch (Exception e) {
 					
 				}
 			  }
 			
 	           //Ending the svg file
 			 
 			  
 			  
 			  // Saving the Buffered Image image1
 			 File outputfile = new File("coloured_new//color_"+Name1+"I.png");
		        try {
					ImageIO.write(S.image1, "png", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
 			  log.println((double)coloured_points/outline_points);
 			  log.println("Done123");
 			  System.out.println("foreground done");
/// 			  
               S.mark_backGround();
               S.ts1.clear();
               S.ts2.clear();
               S.ts.clear();
               
               S.distance_calculation();
               
               for(i=2;i<=11;i++)
  			  {	
  				 d=denomination[i];
  				 
  				 log.println("Now :"+d);
//  				 if(i<11){x=denomination[i]-1;}
//  				 else    {x=denomination[i]-1;}
  				 try{
  				  // for(j=x+1;j<=d;j++)
  				  // {
  					  while(!S.ts.get(d).isEmpty()){
  						  
  	 			    	 System.out.println("with "+d);
  	 			    	 if(i<8){best_fit_angle=45;}//best_fit_angle=S.best_fit_calculation(S.ts.get(d).first(),d);}
  	 			    	 else{ best_fit_angle=45; }
  	 			    	 System.out.println("best_fit_"+best_fit_angle);
  	 			    	 if(best_fit_angle==-1){System.out.println("hello");continue;}
  	 			    	 S.draw_square(denomination[i],S.ts.get(d).first(),best_fit_angle,color[i],1);
  	 				     S.distance_recalculation();
  	 				    
  	 				    }
  	 	    
  				  // }
  			          //     S.fill(i,color[i]);
  				 }
  				 catch (Exception e) {
  					
  				}
  			  }
                
               
               outputfile = new File("coloured_new//color_"+Name1+"J.png");
		        try {
					ImageIO.write(S.image1, "png", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
		      System.out.println((double)coloured_points/(height*width));
		      
		      try {
	 				fw.write("</svg>");
	 				fw.close();
	 			  } catch (IOException e) { }
		      
		      
		      log.println((double)coloured_points/(height*width)); 
 			  log.close();
 			       
 			       
 		}
     
}
