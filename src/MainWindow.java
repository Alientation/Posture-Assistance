import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainWindow extends Canvas{
	
	public static final String TITLE = "Posture Detector";
	public static final int WIDTH = 1300;
	public static final int HEIGHT = 800;
	
	/**
	 * 
	 */
	PostureDetector pd;
	public JFrame frame;
	public JButton b;
	public JLabel label;
	public JLabel scoreLabel;
	
	
	private static final long serialVersionUID = 1L;
	static{System.setProperty("sun.awt.noerasebackground", "true");}
	public MainWindow(PostureDetector pd) {
		frame = new JFrame(TITLE);
		
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("logo.png"));
		
		scoreLabel = new JLabel();
		frame.add(scoreLabel);
		scoreLabel.setBounds(550, 500, 200, 80);
		scoreLabel.setText("Posture Score: 0");
		
		label = new JLabel();
		frame.add(label);
		label.setBounds(300, 650, 700, 80);
		label.setText("<html><pre>\tMake a copy of the snapshot, rename it to \'baseFloodFill.png\' \n\tOpen up an image editing software like paint, make sure to not change the dimensions.\n\tThen color in yourself with red (rgb value : 255,0,0)</pre></html>");
		
		b = new JButton("Take Snapshot of Optimal Posture");
		b.setBounds(530, 600, 240, 30);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pd != null && pd.edgeImage != null) {
					try {
						File outputfile = new File("baseEdgeImage.png");
						ImageIO.write(pd.edgeImage, "png", outputfile);
					} catch (IOException ee) {
						ee.printStackTrace();
					}
				}
			}
		});
		frame.add(b);
		
		frame.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		frame.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.add(pd);
		frame.setVisible(true);
		this.pd = pd;
		pd.start(this);
	}
	
	public int getWidth() {
		return super.getWidth();
	}
	
	public int getHeight() {
		return super.getHeight();
	}
	
	@Override
	public void paint(Graphics g) {
		if (pd.currentImage == null)
			super.paint(g);
	}
	
	@Override
	public void update(Graphics g) {
		if (pd.currentImage != null) {
			paint(g);
		}
	}
	
}
