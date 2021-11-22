import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;



public class PostureDetector extends Canvas implements Runnable{
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	public static final int NS = 1000000000 / 60;
	public static final int MIN_EDGE_VALUE = 12;
	
	private MainWindow mainWindow;
	public static Graphics g;
	private BufferStrategy bs;
	private boolean running = false;
	private Thread thread;
	
	private boolean visited[][];
	private int count = 0;
	
	private Stack stackx = new Stack(50000);
	private Stack stacky = new Stack(50000);
	
	Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	BufferedImage currentImage;
	public BufferedImage baseEdgeImage;
	private int[] averageEdge;
	public BufferedImage baseFloodFillImage;
	
	public BufferedImage edgeImage;
	public BufferedImage floodfillImage; //floodfill from the average of all edge positions
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); System.setProperty("sun.awt.noerasebackground", "true");}
	public static void main(String[] args) {
		
		new PostureDetector();
	}
	
	public PostureDetector() {
		//load in baseImage    TODO: clean up main menu so that the user can save a snapshot of the edgeImage and then make a copy and then draw what they think are the edges in pure red 
		File file1 = new File("baseEdgeImage.png");
		File file2 = new File("baseFloodFillImage.png");
		if (file1.exists() && file2.exists()) {
			try {
				baseEdgeImage = ImageIO.read(new File("baseEdgeImage.png"));
				baseFloodFillImage = ImageIO.read(new File("baseFloodFillImage.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//then it does not exist
			System.out.println("FILES DOES NOT EXIST");
		}
		new MainWindow(this);
	}
	
	public synchronized void start(MainWindow window) {
		this.mainWindow = window;
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void tick() {
		/*TODO: Run the operations on the image
		try {
			currentImage = paintConverter.getBufferedImage(grabber.grab());
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	@Override
	public void paint(Graphics g) {
		if (currentImage == null)
			super.paint(g);
	}
	
	@Override
	public void update(Graphics g) {
		if (currentImage != null) {
			paint(g);
		}
	}
	
	public void checkPosture() {
		edgeImage = getEdges(currentImage);
		floodfillImage = getFloodFill(currentImage);
		
		//deduct from score for the difference between y position of the average edge of current and base image
		if (averageEdge == null) {
			averageEdge = getEdgePositionAverage(baseEdgeImage);
		}
		int dif = averageEdge[0] - getEdgePositionAverage(edgeImage)[0]; //how much to add by
		double subtract = Math.abs(averageEdge[1] - getEdgePositionAverage(edgeImage)[1]) * 300; //difference in y position
		
		double score1 = calculateEdgeScore(dif);
		double score2 = calculateFloodFillScore(dif);
		
		double finalScore = (100000 - (score1 + subtract)) / 500; //higher score = good, good score above 80
		mainWindow.scoreLabel.setText("Posture Score: " + Math.round(finalScore));
	}
	
	//calculates the score for the edges
	public double calculateEdgeScore(int dif) {
		//for every pixel in the current image that is different when shifted horizontally and compared to the base image (different aka more than 5 percent difference), it will be -0 pts
		//else add 2 pts
		double score = 0;
		int maxDif = 40;
		double add = 2;
		double sub = 0;
		for (int y = 0; y < edgeImage.getHeight(); y++) {
			for (int x = dif < 0 ? Math.abs(dif) : 0; x + dif < baseEdgeImage.getWidth() && x < edgeImage.getWidth(); x++) {
				if (difference(edgeImage.getRGB(x, y),baseEdgeImage.getRGB(x + dif, y), maxDif)) {
					score -= sub;
				} else {
					score += add;
				}
			}
		}
		return score;
	}
	
	//calculates if the absolute difference between two rgb's is within the acceptable range
	public boolean difference(int rgb1, int rgb2, int maxDif) {
		int sumDifference = 0;
		
		sumDifference += Math.abs((rgb1 >> 16) & 0xFF - (rgb2 >> 16) & 0xFF);
		sumDifference += Math.abs((rgb1 >> 8) & 0xFF - (rgb2 >> 8) & 0xFF);
		sumDifference += Math.abs((rgb1) & 0xFF - (rgb2) & 0xFF);
		
		return sumDifference <= maxDif;
	}
	
	//calculates the score for the floodfill
	public double calculateFloodFillScore(int dif) {
		//for every red pixel in the base image that is different when shifted and compared to the current image (different aka not red), it will be -0 pts
		//else add 1 pt
		double score = 0;
		double add = 1;
		double sub = 0;
		for (int y = 0; y < edgeImage.getHeight(); y++) {
			for (int x = dif < 0 ? Math.abs(dif) : 0; x + dif < baseEdgeImage.getWidth() && x < edgeImage.getWidth(); x++) {
				if (((baseEdgeImage.getRGB(x + dif, y) >> 16) & 0xFF) == 255 && ((baseEdgeImage.getRGB(x + dif, y) >> 8) & 0xFF) == 0 && ((baseEdgeImage.getRGB(x + dif, y)) & 0xFF) == 0) {
					if (((edgeImage.getRGB(x + dif, y) >> 16) & 0xFF) == 255 && ((edgeImage.getRGB(x + dif, y) >> 8) & 0xFF) == 0 && ((edgeImage.getRGB(x + dif, y)) & 0xFF) == 0) {
						score += add;
					} else {
						score -= sub;
					}
				}
			}
		}
		return (Math.abs(score)/score) * Math.sqrt(Math.abs(score));
		//return Math.sqrt(score);
	}
	
	//used in gaussian filter
	public static final int[][] FIVE_BY_FIVE = {
			{2, 4, 5, 4, 2},
			{4, 9, 12, 9, 4},
			{5, 12, 15, 12, 5},
			{4, 9, 12, 9, 4},
			{2, 4, 5, 4, 2}
	};
	
	//filters out any "edges" that aren't really edges but tiny fluxtuations in color
	public BufferedImage gaussianFilter(BufferedImage image) {
		BufferedImage filter = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
		for (int y = 2; y < image.getHeight() - 2; y++) {
			for (int x = 2; x < image.getWidth() - 2; x++) {
				int sum1 = 0;
				int sum2 = 0;
				int sum3 = 0;
				int rgb;
				for (int yy = 0; yy < 5; yy++) {
					for (int xx = 0; xx < 5; xx++) {
						rgb = getGrayScale(image.getRGB(xx + x - 2, yy + y - 2));
						sum1 += ((rgb >> 16) & 0xFF) * FIVE_BY_FIVE[yy][xx];
						sum2 += ((rgb >> 8 ) & 0xFF) * FIVE_BY_FIVE[yy][xx];
						sum3 += ((rgb) & 0xFF) * FIVE_BY_FIVE[yy][xx];
					}
				}
				sum1 /= 159;
				sum2 /= 159;
				sum3 /= 159;
				filter.setRGB(x, y,(sum1 << 16) | (sum2 << 8) | sum3 | 0xFF000000);
			}
		}
		return filter;
	}
	
	//average point of all edges, so we can apply shifts in case the user moves left or right. We don't want to negatively impact their score if they move left or right but maintain good posture.
	public int[] getEdgePositionAverage(BufferedImage edgeImage) {
		int[] average = new int[2];
		int numPoints = 0;
		for (int y = 0; y < edgeImage.getHeight(); y++) {
			for (int x = 0; x < edgeImage.getWidth(); x++) {
				if ((edgeImage.getRGB(x, y) & 0xff) > MIN_EDGE_VALUE) {
					average[0] += x;
					average[1] += y;
					numPoints++;
				}
			}
		}
		
		average[0] /= numPoints;
		average[1] /= numPoints;
		return average;
	}
	
	//Returns a bufferedimage that is filled with red where it believes the user is
	public BufferedImage getFloodFill(BufferedImage image) {
		BufferedImage floodfill = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
		int[] average = getEdgePositionAverage(edgeImage);
		if (visited == null || visited.length != image.getWidth() || visited[0].length != image.getHeight()) {
			visited = new boolean[image.getHeight()][image.getWidth()];
		} else {
			for (int y = 0; y < visited.length; y++) {
				for (int x = 0; x < visited[0].length; x++) {
					visited[y][x] = false;
				}
			}
		}
		count = 0;
		System.out.println(average[0] + ", " + average[1]);
		
		stackx.clear();
		stacky.clear();
		stackx.push((short) average[0]);
		stacky.push((short) average[1]);
		stackx.push((short) average[0]);
		stacky.push((short) (average[1] + 150));
		
		int x, y;
		
		while (!stackx.isEmpty()) {
			x = stackx.pop();
			y = stacky.pop();
			if (x < 1 || x >= edgeImage.getWidth()-1 || y < 1 || y >= edgeImage.getHeight()-1 || (edgeImage.getRGB(x, y) & 0xff) > MIN_EDGE_VALUE || visited[y][x]) {
				continue;
			}
			count++;
			floodfill.setRGB(x, y,getRGB(255,0,0));
			visited[y][x] = true;
			stackx.push((short) (x - 1));
			stacky.push((short) (y));
			stackx.push((short) (x + 1));
			stacky.push((short) (y));
			stackx.push((short) (x));
			stacky.push((short) (y - 1));
			stackx.push((short) (x));
			stacky.push((short) (y + 1));
		}
		
		//helperFloodFill(floodfill,average[0],average[1]);
		System.out.println("How many calls >>> " + count);
		return floodfill;
	}
	
	//no use for now
	public void helperFloodFill(BufferedImage floodfill, int x, int y) {
		if (x < 1 || x >= edgeImage.getWidth()-1 || y < 1 || y >= edgeImage.getHeight()-1 || (edgeImage.getRGB(x, y) & 0xff) > MIN_EDGE_VALUE) {
			return;
		}
		count++;
		floodfill.setRGB(x, y,getRGB(200,50,50));
		if (count > 5000) {
			return;
		}
		ArrayList<Integer> xpos = new ArrayList<Integer>();
		ArrayList<Integer> ypos = new ArrayList<Integer>();
		for (int xx = -1; xx < 2; xx++) {
			for (int yy = -1; yy < 2; yy++) {
				if (!visited[y + yy][x + xx]) {
					visited[y + yy][x + xx] = true;
					xpos.add(x + xx);
					ypos.add(y + yy);
				}
			}
		}
		for (int i = 0; i < xpos.size(); i++) {
			helperFloodFill(floodfill,xpos.get(i),ypos.get(i));
		}
	}
	
	//returns the combined rgb value
	public int getRGB(int r, int g, int b) {
		return new Color(r,g,b).getRGB();
	}
	
	//returns a sobel operated buffered image from a gray scaled image
	public BufferedImage getEdges(BufferedImage image) {
		int[] edges = new int[image.getWidth() * image.getHeight()];
		
		int maxGradient = -1;
		
		for (int y = 1; y < image.getHeight() - 1; y++) {
			for (int x = 1; x < image.getWidth() - 1; x++) {
				int val00 = getGrayScale(image.getRGB(x - 1, y - 1));
                int val01 = getGrayScale(image.getRGB(x - 1, y));
                int val02 = getGrayScale(image.getRGB(x - 1, y + 1));

                int val10 = getGrayScale(image.getRGB(x, y - 1));
                int val11 = getGrayScale(image.getRGB(x, y));
                int val12 = getGrayScale(image.getRGB(x, y + 1));

                int val20 = getGrayScale(image.getRGB(x + 1, y - 1));
                int val21 = getGrayScale(image.getRGB(x + 1, y));
                int val22 = getGrayScale(image.getRGB(x + 1, y + 1));

                int gx =  ((-1 * val00) + (0 * val01) + (1 * val02)) 
                        + ((-2 * val10) + (0 * val11) + (2 * val12)) //2 0 2
                        + ((-1 * val20) + (0 * val21) + (1 * val22));

                int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02)) // -1 -2 -1
                        + ((0 * val10) + (0 * val11) + (0 * val12))
                        + ((1 * val20) + (2 * val21) + (1 * val22));   // 1 2 1

                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;
                if (maxGradient < g) {
                	maxGradient = g;
                }
                
                edges[y * image.getWidth() + x] = g;
			}
		}
		BufferedImage bi = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
		
		double scale = 255.0 / maxGradient;

        for (int y = 1; y < bi.getHeight() - 1; y++) {
            for (int x = 1; x < bi.getWidth() - 1; x++) {
                int edgeColor = edges[x + bi.getWidth() * y];
                edgeColor = (int)(edgeColor * scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
                bi.setRGB(x, y, edgeColor);
            }
        }
		return bi;
	}
	
	//returns a gray scale for a given rgb
	public int getGrayScale(int rgb) {
		int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

        return gray;
	}
	
	//render the ui
	public static int frames = 0;
	private void render() {
		frames++;
		bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight()); //clears screen
		
		
		g.drawImage(currentImage,0,0,currentImage.getWidth(),currentImage.getHeight(),null);
		g.drawImage(edgeImage,currentImage.getWidth(),0,edgeImage.getWidth(),edgeImage.getHeight(),null);
		if (frames >= 100) {
			g.drawImage(floodfillImage,floodfillImage.getWidth(),0,floodfillImage.getWidth(),floodfillImage.getHeight(),null);
		}
		g.dispose();
		bs.show();
	}

	//main loop
	@Override
	public void run() {
		System.out.println("Running");
		
		//webcam
		VideoCapture camera = new VideoCapture(0);
		Mat frame = new Mat();
		MatOfByte matOfByte = new MatOfByte();
		
		this.requestFocus();
		long lastTime = System.nanoTime();
		double delta = 0;
		long timer = System.currentTimeMillis();
		long now;
		
		while(running) {
			now = System.nanoTime();
			delta += (now - lastTime) / NS;
			lastTime = now;
			System.out.print(".");
			while (delta >= 1) {
				System.out.println("Rendering");
				
				//read in a frame from web cam
				camera.read(frame);
				Mat image_tmp = frame;
				try {
					//convert it to a suitable image file so that it can be manipulated using BufferedImage
					Imgcodecs.imencode(".png", image_tmp, matOfByte);
					currentImage = ImageIO.read(new ByteArrayInputStream(matOfByte.toArray()));
					checkPosture();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tick();
				render();
				delta--;
			}
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
			}
		}
		System.out.println("Ending");
		stop();
	}
}
