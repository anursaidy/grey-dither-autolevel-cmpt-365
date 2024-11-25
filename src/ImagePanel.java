import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    BufferedImage image;
    BufferedImage modifiedImage;

    public ImagePanel(BufferedImage image, BufferedImage modifiedImage){
        this.image = image;
        this.modifiedImage = modifiedImage;

        if(modifiedImage != null) {
            this.setPreferredSize(new Dimension(image.getWidth()*2, image.getHeight()));
        }else{
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(image,0,0,null);
        if(modifiedImage!= null){
            g.drawImage(modifiedImage, image.getWidth(), 0,null);
        }


    }

    public void replaceImage(BufferedImage newImage, BufferedImage modifiedImage){
        this.image = newImage;
        this.modifiedImage = modifiedImage;
        if (modifiedImage != null) {
            this.setPreferredSize(new Dimension(image.getWidth() * 2, image.getHeight()));
        } else {
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
        revalidate();
        repaint();
    }
}
