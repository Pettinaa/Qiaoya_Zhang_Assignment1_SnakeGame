import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class A1 extends GameEngine {

    // Main Function
    public static void main(String[] args) {
        // Create a new A1
        createGame(new A1());
    }

    //-------------------------------------------------------
    // Game
    //-------------------------------------------------------

    Image headImage;
    Image bodyImage;
    Image appleImage;
    Image badFoodImage;
    //蛇头
    double headPositionX;
    double headPositionY;
    //方向
    private String direction = "null";
    // 蛇身体
    ArrayList<Body> snakeBody = new ArrayList<>();

    // 苹果位置
    int appleX;
    int appleY;
    //毒苹果位置
    int badFoodX;
    int badFoodY;

    boolean isFailed = false;  // 添加游戏失败标志

    class Body {
        int x;
        int y;
  
        // 构造函数
        public Body(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void drawSnake() {
        saveCurrentTransform();
        drawImage(headImage, (int) headPositionX, (int) headPositionY);
        for (Body bodyPart : snakeBody) {
            drawImage(bodyImage, bodyPart.x, bodyPart.y);
        }
        restoreLastTransform();
    }

    public void BGM() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("bgm.wav"));
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio format: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("Line unavailable: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO exception: " + e.getMessage());
        }
    }

    public void updateSnake() {
        if (!direction.equals("null")) {
            // 更新蛇头位置
            int prevHeadX = (int) headPositionX;
            int prevHeadY = (int) headPositionY;

            if (direction.equals("UP")) {
                headPositionY -= 10;
            } else if (direction.equals("DOWN")) {
                headPositionY += 10;
            } else if (direction.equals("LEFT")) {
                headPositionX -= 10;
            } else if (direction.equals("RIGHT")) {
                headPositionX += 10;
            }

            // 检查蛇头是否吃到了坏食物
            if ((int) headPositionX == badFoodX && (int) headPositionY == badFoodY) {
                gameOver();
                return;
            }

            // 更新蛇身体位置
            for (int i = snakeBody.size() - 1; i > 0; i--) {
                Body currentPart = snakeBody.get(i);
                Body prevPart = snakeBody.get(i - 1);

                currentPart.x = prevPart.x;
                currentPart.y = prevPart.y;
            }

            if (!snakeBody.isEmpty()) {
                Body firstPart = snakeBody.get(0);
                firstPart.x = prevHeadX;
                firstPart.y = prevHeadY;
            }
        }
    }

    public void randomApple() {
        do {
            appleX = (int) (Math.random() * 50) * 10;
            appleY = (int) (Math.random() * 50) * 10;
        } while (isAppleOnSnake(appleX, appleY));
    }

    public boolean isAppleOnSnake(int x, int y) {
        for (Body bodyPart : snakeBody) {
            if (bodyPart.x == x && bodyPart.y == y) {
                return true;
            }
        }
        return false;
    }
    public void randomBadFood() {
        do {
            badFoodX = (int) (Math.random() * 50) * 10;
            badFoodY = (int) (Math.random() * 50) * 10;
        } while (isBadFoodOnSnake(badFoodX, badFoodY));
    }

    public boolean isBadFoodOnSnake(int x, int y) {
        for (Body bodyPart : snakeBody) {
            if (bodyPart.x == x && bodyPart.y == y) {
                return true;
            }
        }
        return false;
    }

    public void drawApple() {
        drawImage(appleImage, appleX, appleY);
    }

    public void drawBadFood() {
        drawImage(badFoodImage, badFoodX, badFoodY);
    }

    private int MAX_SNAKE_LENGTH = 19;

    public void updateApple() {
        if ((int) headPositionX == appleX && (int) headPositionY == appleY && snakeBody.size() < MAX_SNAKE_LENGTH) {
            snakeBody.add(0, new Body((int) headPositionX, (int) headPositionY)); // 添加到蛇头
            randomApple();

        }
    }
    public void updateBadFood() {
        // 不需要更新坏食物的位置
    }


    @Override
    public void update(double dt) {
        if (!isFailed) {
            updateSnake();
            updateApple();

            collisions();
        }
    }

    @Override
    public void paintComponent() {
        clearBackground(500, 500);
        changeBackgroundColor(Color.BLACK);
        drawSnake();
        drawApple();
        drawBadFood();
        drawGrid(10, 10, 500, 500);

        if (isFailed) {
            changeColor(Color.blue);
            drawBoldText(200, 300, "Game Over");
            drawText(50, 200, "Press Space to Restart");
        }
    }

    private void drawGrid(int cellWidth, int cellHeight, int width, int height) {
        changeColor(128, 138, 135);

        for (int y = 0; y < height; y += cellHeight) {
            drawLine(0, y, width, y);
        }

        for (int x = 0; x < width; x += cellWidth) {
            drawLine(x, 0, x, height);
        }
    }

    public void init() {
        headImage = loadImage("src/head.png");
        bodyImage = loadImage("src/dot.png");
        appleImage = loadImage("src/apple.png");

        try {
            badFoodImage = ImageIO.read(new File("src/badFood.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BGM();

        headPositionX = 100;
        headPositionY = 100;
        snakeBody.add(new Body(100, 100));
        randomApple();
        randomBadFood();
    }

    private void resetGame() {
        headPositionX = 100;
        headPositionY = 100;
        direction = "null";
        snakeBody.clear();
        snakeBody.add(new Body(100, 100));
        isFailed = false;  // 重置游戏失败标志
    }

    public void collisions() {
        if (snakeBody.size() > 1) {
            for (int i = 1; i < snakeBody.size(); i++) {
                if ((int) headPositionX == snakeBody.get(i).x && (int) headPositionY == snakeBody.get(i).y) {
                    gameOver();
                    return;
                }
            }
        }

        if ((int) headPositionX < 0 || (int) headPositionX >= 500 || (int) headPositionY < 0 || (int) headPositionY >= 500) {
            gameOver();
        }
    }

    public void gameOver() {
        isFailed = true;  // 设置游戏失败标志
        direction = "null";  // 停止蛇的移动
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if ((key == KeyEvent.VK_SPACE) && isFailed) {
            resetGame();
        } else if ((key == KeyEvent.VK_LEFT) && (!direction.equals("RIGHT"))) {
            direction = "LEFT";
        } else if ((key == KeyEvent.VK_RIGHT) && (!direction.equals("LEFT"))) {
            direction = "RIGHT";
        } else if ((key == KeyEvent.VK_UP) && (!direction.equals("DOWN"))) {
            direction = "UP";
        } else if ((key == KeyEvent.VK_DOWN) && (!direction.equals("UP"))) {
            direction = "DOWN";
        }
    }

    public void keyReleased(KeyEvent e) {
        // Empty method
    }
}
