package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class GameView extends SurfaceView implements Runnable{
    volatile boolean playing;
    private Random rand;
    Thread gameThread = null;
    private Context context;
    private int screenX, screenY;

    //interface variables
    private Paint paint;
    private Paint paintInfo;
    private Canvas canvas;
    private SurfaceHolder ourHolder;
    private SoundManager soundManager;
    Bitmap tile,tile2;
    public static int globalPadding;
    public static int gridPadding;
    public static int cellSize;
    public static int upperPanelYSize;
    private Rect gridContour;

    //Game variables
    private int score;
    private int bestScore;
    private Cell [][] matrix;
    int [][] grid;
    Point [] path;
    private Point[] nextBalls;
    private final int countNextBalls=3;
    private final int matrixSize=9;
    private int touchX,touchY;
    private Point selectedBall;
    private Point selectedCell;

    //booleans
    boolean isNextTurn=true;
    boolean isBallSelected=false;
    boolean isCellSelected =false;
    boolean isMatrixChanged=false;
    boolean isShowNextBalls=true;
    boolean isShowPath =false;
    boolean isDebugging=false;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        gameThread = new Thread(this);
        this.context = context;
        this.screenX = screenX;
        this.screenY = screenY;
        rand = new Random();

        ourHolder = getHolder();
        paint = new Paint();
        paint.setTextSize(25);
        paintInfo = new Paint();
        paintInfo.setTextSize(screenX/20);
        paintInfo.setColor(Color.WHITE);
        paintInfo.setTextAlign(Paint.Align.LEFT);

        soundManager = new SoundManager();
        soundManager.loadSound(context);
        globalPadding = screenY / 50;
        upperPanelYSize=screenY / 12;
        cellSize = (this.screenX - globalPadding * 2) / matrixSize;
        gridPadding = (screenY-upperPanelYSize-cellSize*matrixSize)/2;
        gridContour = new Rect(globalPadding, upperPanelYSize+gridPadding,
                cellSize*matrixSize + globalPadding, upperPanelYSize+ gridPadding+cellSize*matrixSize );
        tile = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.lines_tile);
        tile =  Bitmap.createScaledBitmap(tile,
                cellSize, cellSize, false);
        tile2 = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.lines_tile);
        tile2 =  Bitmap.createScaledBitmap(tile2,
                cellSize, cellSize, false);

        //initialize game variables
        score=0;
        bestScore=0;
        matrix = new Cell [matrixSize][matrixSize];
        grid=new int[matrixSize+2][matrixSize+2];
        for (int i=0;i<matrixSize+2;i++)
            for (int j=0;j<matrixSize+2;j++) {
                if (i == 0 || j == 0 || i == 10 || j == 10)
                    grid[i][j] = -1;
                else grid[i][j] = -2;
            }
        for (int i=0;i<matrixSize;i++)
            for (int j=0;j<matrixSize;j++){
                matrix[i][j]=new Cell(new Rect(globalPadding+cellSize*i,upperPanelYSize+gridPadding+cellSize*j,
                        globalPadding+cellSize*(i+1),upperPanelYSize+gridPadding+cellSize*(j+1)));
            }
        path=new Point[1];
        path[0]=new Point(0,0);
        nextBalls=new Point[countNextBalls];
        for (int i=0;i<countNextBalls;i++) {
            nextBalls[i]=new Point(-1,-1);
            generateBall(i,'0');
        }
        selectedBall=new Point(-1,-1);
        selectedCell=new Point(-1,-1);
    }

    private void update(){
        if (isNextTurn){
            for (int i=0;i<countNextBalls;i++) {
                matrix[nextBalls[i].x][nextBalls[i].y].apply(context);
                grid[nextBalls[i].x+1][nextBalls[i].y+1]=-1;
                generateBall(i,'0');
            }
            isNextTurn=false;
            isMatrixChanged=true;
        }
        else if (isBallSelected && isCellSelected){
            if (matrix[selectedCell.x][selectedCell.y].getBall().getColor()>'0' &&
                    matrix[selectedCell.x][selectedCell.y].getBall().getColor()<'9') {
                for (int i=0;i<countNextBalls;i++){
                    if (nextBalls[i].x == selectedCell.x && nextBalls[i].y == selectedCell.y)
                        generateBall(i,matrix[selectedCell.x][selectedCell.y].getBall().getColor());
                }
            }
            //!!!!!!!!!!!!!!Добавить более сложный механизм нахождения пути вдальнейшем!!!!!!!!!!!!!!!!
            if(findPath()) {
                matrix[selectedCell.x][selectedCell.y].setBall(context, matrix[selectedBall.x][selectedBall.y].getBall().getColor());
                matrix[selectedBall.x][selectedBall.y].reset();
                grid[selectedCell.x+1][selectedCell.y+1]=-1;
                soundManager.playSound("move");
                isNextTurn=true;
                isMatrixChanged=true;
                isShowPath = true;
            }else soundManager.playSound("path_blocked");
            for (int i=1;i<matrixSize+1;i++)
                for (int j=1;j<matrixSize+1;j++)
                    if (grid[i][j]!=-1)
                        grid[i][j] = -2;
            isBallSelected=false;
            isCellSelected =false;
        }
        if (isMatrixChanged){
            score += checkLineHorizontal();
            score += checkLineVertical();
            score += checkLineDiagonal();
            score += checkLineContraDiagonal();
            isMatrixChanged=false;
        }

        if (score>bestScore){
            bestScore=score;
            MainActivity.editor.putInt("HiScore",bestScore);
            MainActivity.editor.commit();
        }
    }

    private int checkLineVertical(){
        int scoreToAdd=0;
        Point [] line=new Point[5];
        for (int i=0;i<5;i++) line[i]=new Point();
        char lineColor;
        int j=4,n=1;
        boolean isUp=true;
        for (int i=0;i<9;){
            if (matrix[i][4].getBall().getColor()=='.'){
                i++;
                continue;
            }
            line[0].set(i, 4);
            lineColor=matrix[i][4].getBall().getColor();
            while (j>0 && j<8 && n!=5){
                if (isUp) j--;
                else j++;
                if (matrix[i][j].getBall().getColor()==lineColor){
                    line[n].set(i,j);
                    n++;
                }else if (isUp){
                    j=4;
                    isUp=false;
                } else break;
            }
            if (n==5){
                scoreToAdd+=n;
                for (n=0;n<5;n++) {
                    matrix[line[n].x][line[n].y].reset();
                    grid[line[n].x+1][line[n].y+1]=-2;
                }
            }
            n=1;
            i++;
            j=4;
            isUp=true;
        }
        return scoreToAdd;
    }
    private int checkLineHorizontal(){
        int scoreToAdd=0;
        Point [] line=new Point[5];
        for (int i=0;i<5;i++) line[i]=new Point();
        char lineColor;
        int i=4,n=1;
        boolean isUp=true;
        for (int j=0;j<9;){
            if (matrix[4][j].getBall().getColor()=='.'){
                j++;
                continue;
            }
            line[0].set(4, j);
            lineColor=matrix[4][j].getBall().getColor();
            while (i>0 && i<8 && n!=5){
                if (isUp) i--;
                else i++;
                if (matrix[i][j].getBall().getColor()==lineColor){
                    line[n].set(i,j);
                    n++;
                }else if (isUp){
                    i=4;
                    isUp=false;
                } else break;
            }
            if (n==5){
                scoreToAdd+=n;
                for (n=0;n<5;n++){
                    matrix[line[n].x][line[n].y].reset();
                    grid[line[n].x+1][line[n].y+1]=-2;
                }
            }
            n=1;
            j++;
            i=4;
            isUp=true;
        }
        return scoreToAdd;
    }
    private int checkLineDiagonal(){
        int scoreToAdd=0;
        Point [] line=new Point[5];
        for (int i=0;i<5;i++) line[i]=new Point();
        char lineColor;
        int j,i,x,y,n=1;
        boolean direction=true,isUp=true;
        for (i=2,j=2;i<7 && j<7;){
            if (matrix[i][j].getBall().getColor()>'a' &&
                    matrix[i][j].getBall().getColor()<'z'){
                line[0].set(i, j);
                lineColor = matrix[i][j].getBall().getColor();
                x = i;
                y = j;
                while (n != 5) {//y проверять не нужно. изменяются синхронно
                    if (isUp) {x++;y--;}
                    else {x--;y++;}
                    if (isUp && (y==-1 || x==9)) {
                        x = i;
                        y = j;
                        isUp = false;
                    }else if(!isUp && (y==9 || x==-1)){
                        break;
                    }else if (matrix[x][y].getBall().getColor() == lineColor) {
                        line[n].set(x, y);
                        n++;
                    }else if (isUp) {
                        x = i;
                        y = j;
                        isUp = false;
                    }else break;
                }
                if (n == 5) {
                    scoreToAdd+=n;
                    for (n = 0; n < 5; n++) {
                        matrix[line[n].x][line[n].y].reset();
                        grid[line[n].x+1][line[n].y+1]=-2;
                    }
                }
                n=1;
                isUp=true;
            }
            if(direction) i++;
            else j++;
            direction ^= true;
        }
        return scoreToAdd;
    }
    private int checkLineContraDiagonal(){
        int scoreToAdd=0;
        Point [] line=new Point[5];
        for (int i=0;i<5;i++) line[i]=new Point();
        char lineColor;
        int j,i,x,y,n=1;
        boolean direction=true,isUp=true;
        for (i=2,j=6;i<7 && j<7;){
            if (matrix[i][j].getBall().getColor()>'a' &&
                    matrix[i][j].getBall().getColor()<'z'){
                line[0].set(i, j);
                lineColor = matrix[i][j].getBall().getColor();
                x = i;
                y = j;
                while (n != 5) {//y проверять не нужно. изменяются синхронно
                    if (isUp) {x--;y--;}
                    else {x++;y++;}
                    if (isUp && (y==-1 || x==-1)) {
                        x = i;
                        y = j;
                        isUp = false;
                    }else if(!isUp && (y==9 || x==9)){
                        break;
                    }else if (matrix[x][y].getBall().getColor() == lineColor) {
                        line[n].set(x, y);
                        n++;
                    }else if (isUp) {
                        x = i;
                        y = j;
                        isUp = false;
                    }else break;
                }
                if (n == 5) {
                    scoreToAdd+=n;
                    for (n = 0; n < 5; n++) {
                        matrix[line[n].x][line[n].y].reset();
                        grid[line[n].x+1][line[n].y+1]=-2;
                    }
                }
                n=1;
                isUp=true;
            }
            if(direction) j--;
            else i++;
            direction ^= true;
        }
        return scoreToAdd;
    }

    private void draw(){
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            //-----------------
            int tmpInt;

            canvas.drawColor(Color.DKGRAY);
            paint.setColor(Color.argb(255, 0, 0, 0));
            canvas.drawRect(0, 0, screenX, upperPanelYSize, paint);
            canvas.drawText("Score: " + score, globalPadding,
                    upperPanelYSize/2+paintInfo.getTextSize()/2, paintInfo);
            tmpInt = cellSize + cellSize/2 + 10;
            canvas.drawRect(screenX / 2 - tmpInt, globalPadding, screenX / 2 + tmpInt, 15 + cellSize, paint);

            //tmpInt = cellSize * matrixSize;
            //paint.setColor(Color.argb(255, 0, 0, 0));
            for (int i = 0; i < matrixSize; i++)
                for (int j = 0; j < matrixSize; j++)
                    canvas.drawBitmap(tile,matrix[i][j].rect.left,matrix[i][j].rect.top, paint);
            if (isBallSelected){
                canvas.drawBitmap(tile,matrix[selectedBall.x][selectedBall.y].rect.left,
                        matrix[selectedBall.x][selectedBall.y].rect.top, paint);
            }
            /*for (int i = 0; i < matrixSize+1; i++)
                canvas.drawLine(i * cellSize + globalPadding, upperPanelYSize + globalPadding,
                        i * cellSize + globalPadding, upperPanelYSize + tmpInt + globalPadding, paint);
            for (int i = 0; i < matrixSize+1; i++)
                canvas.drawLine(globalPadding, upperPanelYSize + i * cellSize + globalPadding,
                        tmpInt + globalPadding, upperPanelYSize + i * cellSize + globalPadding, paint);*/

            paint.setColor(Color.argb(255, 0, 0, 0));
            for (int i = 0; i < matrixSize; i++)
                for (int j = 0; j < matrixSize; j++) {
                    if(matrix[i][j].getBall().getColor()!='.')
                        canvas.drawBitmap(matrix[i][j].getBall().image,
                                matrix[i][j].rect.left,matrix[i][j].rect.top, paint);
                    if (isDebugging) {
                        canvas.drawText("" + matrix[i][j].getBall().getColor(),
                                30 * (i + 1), upperPanelYSize + cellSize * 10 + 30 * j, paint);
                        canvas.drawText("" + grid[i+1][j+1],
                                320 + 30 * (i + 1), upperPanelYSize + cellSize * 10 + 30 * j, paint);
                    }
                }

            if (isDebugging && isShowPath) {
                int tmpCellCentre=gridContour.left+cellSize/2;
                for (int i = 1; i < path.length; i++)
                    canvas.drawCircle(path[i].x*cellSize+tmpCellCentre,
                            upperPanelYSize + path[i].y*cellSize+tmpCellCentre,cellSize/4,paint);
                isShowPath=false;
            }
            //-----------------
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void controlFPS(){
        try {
            gameThread.sleep(17);//17 = 1000(milliseconds)/60(FPS)
        }catch(InterruptedException e){}
    }

    private void generateBall(int index, char color){
        int newX, newY, newColor;
        if (color != '0') newColor = color;
        else newColor=rand.nextInt(6);
        do{
            newX=rand.nextInt(matrixSize);
            newY=rand.nextInt(matrixSize);
        }while (!matrix[newX][newY].reserve(context, Integer.toString(newColor).charAt(0)));
        nextBalls[index].set(newX,newY);
    }

    private boolean findPath(){
        boolean stop;
        int dx[] = {1,0,-1,0};
        int dy[] = {0,1,0,-1};
        int d = 0;
        //-2 - пусто
        //-1 - стена
        grid[selectedBall.x+1][selectedBall.y+1]=0;//старт
        do{
            stop=true;
            for(int x = 1; x<matrixSize+1;++x)
                for(int y = 1; y<matrixSize+1;++y)
                    if (grid[x][y]==d) {
                        for (int k = 0; k<4; ++k)
                            if (grid[x + dx[k]][y + dy[k]] == -2) {
                                stop = false;
                                grid[x + dx[k]][y + dy[k]] = d + 1;
                            }
                    }
            d++;
        }while (!stop && grid[selectedCell.x+1][selectedCell.y+1]==-2);
        if (grid[selectedCell.x+1][selectedCell.y+1]==-2)return false;
        int len = grid[selectedCell.x+1][selectedCell.y+1];            // длина кратчайшего пути из (ax, ay) в (bx, by)
        int x = selectedCell.x;
        int y = selectedCell.y;
        d = len;
        path = new Point[len+1];
        while ( d > 0 )
        {
            path[d] = new Point(x,y);// записываем ячейку (x, y) в путь
            d--;
            for (int k = 0; k < 4; ++k)
                if (grid[x + dx[k]+1][y + dy[k]+1] == d)
                {
                    x = x + dx[k];
                    y = y + dy[k];           // переходим в ячейку, которая на 1 ближе к старту
                    break;
                }
        }
        path[0] = new Point(selectedBall.x,selectedBall.y);
        return true;
    }

    @Override
    public void run() {
        while(playing){
            update();
            draw();
            controlFPS();
        }
    }
    void pause(){
        playing = false;
        try{
            gameThread.join();
        }catch(InterruptedException e){}
    }
    void resume(){
        playing= true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        touchX=(int)motionEvent.getX();
        touchY=(int)motionEvent.getY();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                if (gridContour.contains(touchX,touchY)) {
                    if (!isBallSelected) {
                        selectedCell.set((touchX-globalPadding)/cellSize,(touchY-gridPadding-upperPanelYSize)/cellSize);
                        if (matrix[selectedCell.x][selectedCell.y].rect.contains(touchX,touchY) &&
                                matrix[selectedCell.x][selectedCell.y].getBall().getColor()>='a' &&
                                matrix[selectedCell.x][selectedCell.y].getBall().getColor()<='z') {
                            isBallSelected = true;
                            selectedBall.set(selectedCell.x,selectedCell.y);
                            break;
                        }
                    } else{
                        isCellSelected = true;
                        selectedCell.set((touchX-globalPadding)/cellSize,(touchY-gridPadding-upperPanelYSize)/cellSize);
                        if (matrix[selectedCell.x][selectedCell.y].rect.contains(touchX,touchY) &&
                                matrix[selectedCell.x][selectedCell.y].getBall().getColor()>='a' &&
                                matrix[selectedCell.x][selectedCell.y].getBall().getColor()<='z') {
                            isCellSelected = false;
                            isBallSelected = false;
                            return true;
                        }

                    }
                }
                break;
        }
        return true;
    }
}
