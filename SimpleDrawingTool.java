import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SimpleDrawingTool extends JFrame {
    private final DrawArea drawArea;
    private Color brushColor = Color.BLACK;
    private int brushThickness = 5;
    private boolean eraserMode = false;

    public SimpleDrawingTool() {
        drawArea = new DrawArea();
        add(drawArea, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controls = new JPanel();
        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> chooseColor());
        controls.add(colorButton);

        JButton eraserButton = new JButton("Eraser");
        eraserButton.addActionListener(e -> toggleEraser());
        controls.add(eraserButton);

        JSlider thicknessSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 20, 5);
        thicknessSlider.addChangeListener(e -> brushThickness = thicknessSlider.getValue());
        controls.add(thicknessSlider);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveImage());
        controls.add(saveButton);

        add(controls, BorderLayout.SOUTH);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose a color", brushColor);
        if (newColor != null) {
            brushColor = newColor;
            eraserMode = false;
        }
    }

    private void toggleEraser() {
        eraserMode = !eraserMode;
    }

    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedImage bufferedImage = toBufferedImage(drawArea.getImage());
                ImageIO.write(bufferedImage, "png", new File(file.getAbsolutePath() + ".png"));
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // 创建一个与原始图像相同尺寸的 BufferedImage
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // 绘制原始图像到新的 BufferedImage
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    private class DrawArea extends JPanel {
        private Image image;
        private Graphics2D g2;
        private int currentX, currentY, oldX, oldY;

        public DrawArea() {
            setDoubleBuffered(false);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    oldX = e.getX();
                    oldY = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    currentX = e.getX();
                    currentY = e.getY();

                    if (g2 != null) {
                        g2.setStroke(new BasicStroke(brushThickness,
                                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.setColor(eraserMode ? Color.WHITE : brushColor);
                        g2.drawLine(oldX, oldY, currentX, currentY);
                        repaint();
                        oldX = currentX;
                        oldY = currentY;
                    }
                }
            });
        }

        protected void paintComponent(Graphics g) {
            if (image == null) {
                image = createImage(getSize().width, getSize().height);
                g2 = (Graphics2D) image.getGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                clear();
            }
            g.drawImage(image, 0, 0, null);
        }

        public void clear() {
            g2.setPaint(Color.white);
            g2.fillRect(0, 0, getSize().width, getSize().height);
            g2.setPaint(Color.black);
            repaint();
        }

        public Image getImage() {
            return image;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleDrawingTool frame = new SimpleDrawingTool();
            frame.setVisible(true);
        });
    }
}
