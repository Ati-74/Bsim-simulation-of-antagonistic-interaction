import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.vecmath.Vector2d;

public class SutherlandHodgman extends JFrame {

    SutherlandHodgmanPanel panel;

    public static void main(String[] args) {
        JFrame f = new SutherlandHodgman();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    public SutherlandHodgman() {
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        panel = new SutherlandHodgmanPanel();
        content.add(panel, BorderLayout.CENTER);
        setTitle("SutherlandHodgman");
        pack();
        setLocationRelativeTo(null);
    }
}

class SutherlandHodgmanPanel extends JPanel {
    List<double[]> subject, clipper, intersection;
    List<double[]> subjPoints= new ArrayList<>();
    List<double[]> clipPoints = new ArrayList<>();



    public SutherlandHodgmanPanel() {
        setPreferredSize(new Dimension(1000, 1000));

        //Vector2d[] rectangle_subjPoints= {new Vector2d(396, 596),
        //        new Vector2d(584,416),new Vector2d(279,122),new Vector2d(102,307)};

        Vector2d[] rectangle_subjPoints= {new Vector2d(200,200),new Vector2d(200,400),
        		new Vector2d(400,400),new Vector2d(400, 200)};

        Vector2d[] rectangle_clipPoints= {new Vector2d(200, 400),new Vector2d(400, 600),new Vector2d(600, 400),
        		new Vector2d(400, 200)};

        clipPolygon(rectangle_subjPoints,rectangle_clipPoints);
    }

    private void clipPolygon(Vector2d[] rectangle_subjPoints,Vector2d[] rectangle_clipPoints) {

        List<double[]> subjPoints= new ArrayList<>();
        List<double[]> clipPoints = new ArrayList<>();

        //convert vector2d to list
        for (Vector2d rectangle_subjPoint : rectangle_subjPoints) {
        	subjPoints.add(new double[]{rectangle_subjPoint.x, rectangle_subjPoint.y});
        }

        for (Vector2d rectangle_clipPoint : rectangle_clipPoints) {
        	clipPoints.add(new double[]{rectangle_clipPoint.x, rectangle_clipPoint.y});
        }

        subject = subjPoints;
        intersection  = subject;
        clipper = clipPoints;
        int len = clipper.size();

        for (int i = 0; i < len; i++) {

            int len2 = intersection.size();
            List<double[]> input = intersection;
            intersection = new ArrayList<>(len2);

            double[] prev_clippper_vertex = clipper.get((i + len - 1) % len);
            double[] current_clippper_vertex = clipper.get(i);

            for (int j = 0; j < len2; j++) {

                double[] prev_input_vertex = input.get((j + len2 - 1) % len2);
                double[] current_input_vertex = input.get(j);

                if (isInside(prev_clippper_vertex, current_clippper_vertex, current_input_vertex)) {
                    if (!isInside(prev_clippper_vertex, current_clippper_vertex, prev_input_vertex))
                        intersection.add(intersectionPoint(prev_clippper_vertex, current_clippper_vertex, prev_input_vertex, current_input_vertex));
                    intersection.add(current_input_vertex);
                } else if (isInside(prev_clippper_vertex, current_clippper_vertex, prev_input_vertex))
                    intersection.add(intersectionPoint(prev_clippper_vertex, current_clippper_vertex, prev_input_vertex, current_input_vertex));
            }
        }

       // System.out.println(intersection.size());
        System.out.println(intersectionArea(intersection));
    }

    private boolean isInside(double[] a, double[] b, double[] c) {
    	 return (b[0]-a[0]) * (c[1]-a[1]) < (b[1]-a[1]) * (c[0]-a[0]);
    }

    private double[] intersectionPoint(double[] a, double[] b, double[] p, double[] q) {
        double num_x = (a[0]*b[1] - a[1]*b[0]) * (p[0]-q[0]) -
                (a[0]-b[0]) * (p[0]*q[1] - p[1]*q[0]);
      double den_x = (a[0]-b[0]) * (p[1]-q[1]) - (a[1]-b[1]) * (p[0]-q[0]);

      double num_y = (a[0]*b[1] - a[1]*b[0]) * (p[1]-q[1]) -
              (a[1]-b[1]) * (p[0]*q[1] - p[1]*q[0]);
      double den_y = (a[0]-b[0]) * (p[1]-q[1]) - (a[1]-b[1]) * (p[0]-q[0]);


      double x=num_x/den_x;
      double y=num_y/den_y;

      return new double[]{x, y};
    }

    private double intersectionArea(List<double[]> intersection) {
    	//I used Shoelace formula: https://en.wikipedia.org/wiki/Shoelace_formula

    	double area=0;

    	for (int i = 0; i < intersection.size()-1; i++) {
    		//x1.y2+x2.y3+x3.y4+....
    		area=area+(intersection.get(i)[0]*intersection.get(i+1)[1]);
    		//x2.y1-x3.y2-x4.y3-....
    		area=area-(intersection.get(i+1)[0]*intersection.get(i)[1]);

    		if(i==intersection.size()-2) {
        		//xn.y1
        		area=area+(intersection.get(intersection.size()-1)[0]*intersection.get(0)[1]);
        		//x1.yn
        		area=area-(intersection.get(0)[0]*intersection.get(intersection.size()-1)[1]);
    		}
    	}
		return 0.5*Math.abs(area);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(80, 60);
        g2.setStroke(new BasicStroke(3));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawPolygon(g2, subject, Color.blue);
        drawPolygon(g2, clipper, Color.red);
        drawPolygon(g2, intersection, Color.green);
    }

    private void drawPolygon(Graphics2D g2, List<double[]> points, Color color) {
        g2.setColor(color);
        int len = points.size();
        Line2D line = new Line2D.Double();
        for (int i = 0; i < len; i++) {
            double[] p1 = points.get(i

);
            double[] p2 = points.get((i + 1) % len);
            line.setLine(p1[0], p1[1], p2[0], p2[1]);
            g2.draw(line);
        }
    }
}