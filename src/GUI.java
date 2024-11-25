import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GUI extends JPanel implements ActionListener {
        private BufferedImage image;


        private JLabel label;
        private JFrame frame;
        private JPanel panel;
        private JButton fileButton;
        private JFrame imageFrame;
        private ImagePanel imagePanel;

        //private int[][] ditherMatrix={ {0,2}, {3,1}}; //2x2 matrix from dither slides
        private int[][] ditherMatrix= {{0,8,2,10}, {12,4,14,6}, {3,11,1,9}, {15,7,13,5}};

        public GUI() {
            imageFrame = null;
            imagePanel = null;
            frame = new JFrame();
            label = new JLabel("Project 2");
            frame.setTitle("Project 2");
            fileButton = new JButton("Open File");

            fileButton.addActionListener(this);

            JButton greyScaleButton = new JButton("Greyscale");
            greyScaleButton.addActionListener(e -> {
                if (image != null) {
                    displayBMP(image, greyScale(image));
                }
            });

            JButton ditherButton = new JButton("Ordered Dithering");
            ditherButton.addActionListener(e -> {
                if (image != null) {
                    displayBMP(greyScale(image), orderDithering(greyScale(image)));
                }
            });

            JButton autoLevelButton = new JButton("Auto Level");
            autoLevelButton.addActionListener(e -> {
                if (image != null) {
                    displayBMP(image, autoLevel(image));
                }
            });

            JButton exitButton = new JButton("Exit");
            exitButton.addActionListener(e -> System.exit(0));

            //Panel
            panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
            panel.setLayout(new GridLayout(0, 1));

            panel.add(label);
            panel.add(fileButton);
            panel.add(greyScaleButton);
            panel.add(ditherButton);
            panel.add(autoLevelButton);
            panel.add(exitButton);
            frame.add(panel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


            frame.pack();
            frame.setVisible(true);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == fileButton) {
                openFile();
            }
        }


        private void openFile() {
            JFileChooser fileChooser = new JFileChooser();

            //Returns 1 if cancelled, 0 if opened file
            int fileRes = fileChooser.showOpenDialog(null);
            // System.out.println(fileRes);

            //File Open Success
            if (fileRes == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                File inputFile = new File(path);


                if (path.toLowerCase().endsWith(".bmp")) {
                    setUpBMP(inputFile);

                    panel.revalidate();
                    panel.repaint();
                }
            }
        }

        private void setUpBMP(File inputFile) {
            //Reference https://docs.oracle.com/javase/tutorial/2d/images/index.html

            try {
                image = ImageIO.read(inputFile);



                if (image.getType() != BufferedImage.TYPE_INT_RGB  && image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                    System.out.println("Image is not 24-bit RGB");
                    throw new IllegalArgumentException("Image is not 24-bit RGB");
                }

               displayBMP(image, null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void displayBMP(BufferedImage image, BufferedImage modifiedImage){
            if (imageFrame == null) {
                imageFrame = new JFrame("BMP Image");
                imageFrame.setBackground(Color.BLACK);
                imageFrame.setResizable(false);
                imagePanel = new ImagePanel(image, modifiedImage);
                //imagePanel.setBorder(BorderFactory.createEmptyBorder(300, 300, 200, 300));

                imageFrame.add(imagePanel);


                imageFrame.pack();
                imageFrame.setVisible(true);
            } else {

                imagePanel.replaceImage(image, modifiedImage);
                imageFrame.pack();
            }

            //If they close the window set imageFrame to null
            imageFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    imageFrame = null;
                }
            });
        }

        //REFERENCE: tutorialpoints on greyScale images of Buffered images.
        private BufferedImage greyScale(BufferedImage image){
            //Greyscale Conversion Gray = 0.299xRed + 0.587xGreen + 0.114xBlue
          BufferedImage greyScaleImage = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB);
          for(int i =0; i < image.getHeight();i++){
                for (int j=0; j<image.getWidth();j++){
                    Color c = new Color(image.getRGB(j,i));
                    int red = (int)(c.getRed()*0.299);
                    int green = (int)(c.getGreen()*0.587);
                    int blue = (int)(c.getBlue()*0.114);

                    int rgbSum = red + green + blue;

                    Color newColor= new Color(rgbSum, rgbSum, rgbSum);
                    greyScaleImage.setRGB(j,i, newColor.getRGB());

                }
            }
            return greyScaleImage;

        }


        private BufferedImage orderDithering(BufferedImage greyScaleImage){

            BufferedImage orderedDitherImage = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB);
            for(int i =0; i < image.getHeight();i++){
                for (int j=0; j<image.getWidth();j++){
                    Color c = new Color(greyScaleImage.getRGB(j,i)); //get the color for each pixel of the grey scale
                    int grayVal = c.getRed(); //All have the same value, just get 1 of them

                    if( grayVal > ditherMatrix[i%4][j%4] * (255/16) ){
                        //Set to white (turn on)
                        orderedDitherImage.setRGB(j,i, Color.WHITE.getRGB());
                    }
                    else{
                        //set to black (turn off)
                        orderedDitherImage.setRGB(j,i, Color.BLACK.getRGB());
                    }
                }
            }

            return orderedDitherImage;
        }


        private int minLevel(int idealCount, int count[]){
            int minLevel =0;
            for(int i=minLevel; i <256;i++){ //everything below this becomes 0
                if(count[i] >idealCount){
                    minLevel=i;
                    return minLevel;
                }
            }
            return 0;
        }

        private int maxLevel(int idealCount, int count[]){
            int maxLevel =255;
            for(int i = maxLevel; i >=0; i--){ //everything above this becomes 255
                if(count[i] >idealCount){
                    maxLevel =i;
                    return maxLevel;
                }
            }
            return 0;
        }

        private int[] stretchLevels(int minLevel, int maxLevel){
            int[] stretch = new int[256];
            for (int i = 0; i < 256; i++) {
                if (i < minLevel) { //everything below min level set to 0
                    stretch[i]=0;
                } else if (i > maxLevel) { //everything above set to 255
                    stretch[i]=255;
                } else {
                    stretch[i]= (int)((i - minLevel) * 255.0/(maxLevel - minLevel));
                }
            }
            return stretch;
        }


        private BufferedImage autoLevel(BufferedImage image){
            BufferedImage autoLevelImage = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB);

            //Following implementation algorithm in slides, transform Red channel, G and B are same way
            int[] countRed = new int[256];
            int[] countGreen = new int[256];
            int[] countBlue = new int[256];

            //Count[i] number of pixel of level i(i in [0...255])
            for (int i=0;i <image.getHeight();i++) {
                for (int j=0; j<image.getWidth();j++){
                    Color c = new Color(image.getRGB(j,i));
                    int red = c.getRed();
                    int green= c.getGreen();
                    int blue=c.getBlue();
                    countRed[red]++;
                    countGreen[green]++;
                    countBlue[blue]++;
                }
            }
            //Ideal count = Total # pixels/256
            int idealCount = (image.getWidth()*image.getHeight())/256;

            //Stretch levels to make count closer to ideal

            //To get new max and min values based on ideal count make it closer to it (Makes a new range)
            //Red
             int redMinLevel = minLevel(idealCount, countRed);
             int redMaxLevel = maxLevel(idealCount, countRed);

             //Green
            int greenMinLevel = minLevel(idealCount, countGreen);
            int greenMaxLevel = maxLevel(idealCount, countGreen);

            //Blue
            int blueMinLevel = minLevel(idealCount, countBlue);
            int blueMaxLevel = maxLevel(idealCount, countBlue);


            //Stretch
            int[] redStretch = stretchLevels(redMinLevel,redMaxLevel);
            int[] greenStretch = stretchLevels(greenMinLevel,greenMaxLevel);
            int[] blueStretch = stretchLevels(blueMinLevel,blueMaxLevel);



            for (int i = 0; i < image.getHeight(); i++) {
                for (int j = 0; j < image.getWidth(); j++) {
                    Color c = new Color(image.getRGB(j, i));

                    int r = redStretch[c.getRed()];
                    int g = greenStretch[c.getGreen()];
                    int b = blueStretch[c.getBlue()];

                    Color newColor= new Color(r, g, b);
                    autoLevelImage.setRGB(j, i, newColor.getRGB());
                }
            }

            return autoLevelImage;


        }




    }



