package com.gb.lessons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

public class GameFrame extends JFrame {

    private static GameFrame window;
    private static GameField field;

    private static final int windowWidth = 800;
    private static final int windowHeight = 600;

    private static  int fieldWidth;
    private static  int fieldHeight;

    private static int dropWidth ;
    private static int dropHeight;

    private static int score = 0;

    private static Image backGroundImage;
    private static Image dropImage;
    private static Image gameOverImage;

    private static boolean isGameOver = false;

    private static Timer timerDrop;

    private static int level = 1500;

    private static final ArrayList<Drop> dropList = new ArrayList<>();

    public static GameFrame Create() throws IOException {

        if (window == null) {
            window = new GameFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(windowWidth, windowHeight);
            window.setTitle("Score: " + score);

            backGroundImage = ImageIO.read(Objects.requireNonNull(GameFrame.class.getResourceAsStream("img/background.png")));
            dropImage = ImageIO.read(Objects.requireNonNull(GameFrame.class.getResourceAsStream("img/drop.png")));
            gameOverImage = ImageIO.read(Objects.requireNonNull(GameFrame.class.getResourceAsStream("img/game_over.png")));

            dropWidth = dropImage.getWidth(null);
            dropHeight = dropImage.getHeight(null);

            field = GameField.Create();
            window.add(field);

            fieldWidth = field.getWidth();
            fieldHeight = field.getHeight();

            window.setVisible(true);
        }
        return window;
    }

    public void start() {

        Timer moveTimer = new Timer(10, e -> {
                field.repaint();

                ArrayList<Drop> removeList = new ArrayList<>();

                synchronized (dropList) {
                    IntStream.range(0, dropList.size()).forEach(i -> {
                        Drop drop = dropList.get(i);
                        drop.move();
                        if (!drop.isActive) {
                            removeList.add(drop);
                        }
                    });
                }

                synchronized (dropList) {
                    IntStream.range(0, removeList.size()).forEach(i -> {
                        Drop drop = removeList.get(i);
                        dropList.remove(drop);
                    });
                }
            }
        );
        moveTimer.start();

        timerDrop = new Timer(level, e -> {
            synchronized (dropList) {
                dropList.add(new Drop());
            }
        }
        );
        timerDrop.start();

    }


    private static class GameField extends JPanel {

        private static GameField field;

        public static GameField Create() {

            if(field == null) {
                field = new GameField();
                field.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        if (!isGameOver) {
                            int mouseX = e.getX();
                            int mouseY = e.getY();
                            IntStream.range(0, dropList.size()).forEach(i -> {
                                Drop drop = dropList.get(i);
                                drop.click(mouseX, mouseY);
                            });
                            window.setTitle("Score: " + score);
                        }
                    }
                });
                field.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        super.componentResized(e);
                        fieldWidth = field.getWidth();
                        fieldHeight = field.getHeight();
                    }
                });
            }
            return field;
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backGroundImage, 0, 0, fieldWidth, fieldHeight, null);
            IntStream.range(0, dropList.size()).forEach(i -> dropList.get(i).draw(g));
            if (isGameOver) {
                g.drawImage(gameOverImage,
                        (fieldWidth - gameOverImage.getWidth(null)) / 2,
                        (fieldHeight - gameOverImage.getHeight(null)) / 2,
                        null);
            }
        }
    }

    private static class Drop {

        private final int x;
        private int y;
        private final int dy;

        private boolean isActive;

        public Drop() {

            y = 0;
            x = (int)(Math.random() * (fieldWidth - dropWidth));
            dy = (int)(Math.random() * 2) + 1;
            isActive = true;
        }

        public void move() {
            y = y + dy;
            if (y > fieldHeight) {
                isActive = false;
                isGameOver = true;
            }

            if (x > fieldWidth) isActive = false;
        }

        public void draw(Graphics g) {
            if(!isActive) return;
            g.drawImage(dropImage, x, y, null);
        }

        public void click(int x, int y) {

            if (x >= this.x &&
                x <= this.x + dropWidth &&
                y >= this.y &&
                y <= this.y + dropHeight) {

                isActive = false;
                score++;
                level -= 20;
                if (level < 10) level = 10;
                timerDrop.setDelay(level);
            }
        }
    }
}
