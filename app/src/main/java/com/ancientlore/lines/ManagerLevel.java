package com.ancientlore.lines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

class ManagerLevel {
    private static volatile ManagerLevel instance=null;
    private final String[] colors={"red","blue","green","cyan","magenta","yellow"};
    private Rect gameField;
    private final int matrixSizeX=9;
    private final int matrixSizeY=9;
    private final int countNextBalls=matrixSizeX/3;
    private Cell[][] matrix;
    private int ballCount;
    private int [][] grid;
    private Point[] candidates;
    private Point[] path;
    private GameSprite[] sprites;
    private int tileSize;
    private int tileIndex;

    private ManagerLevel(){}
    static ManagerLevel getInstance(){
        if (instance==null) {
            synchronized (ManagerLevel.class) {
                if (instance == null)
                    instance = new ManagerLevel();
            }
        }
        return instance;
    }

    void initialize(final Context context, final int displayX,
                    final int displayY, final int upperPanelY) {
        tileSize=displayX/matrixSizeX;
        final int paddingX=(displayX-matrixSizeX*tileSize)/2;
        final int paddingY=(displayY-upperPanelY-matrixSizeY*tileSize)/2;
        gameField=new Rect(paddingX,upperPanelY+paddingY,
                displayX-paddingX,displayY-paddingY);

        matrix = new Cell [matrixSizeY][matrixSizeX];
        for (int i=0;i<matrixSizeY;i++)
            for (int j=0;j<matrixSizeX;j++){
                matrix[i][j]=new Cell(new Rect(
                        gameField.left+j*tileSize, gameField.top+i*tileSize,
                        gameField.left+j*tileSize+tileSize, gameField.top+i*tileSize+tileSize));
            }
        ballCount=0;
        grid=new int[matrixSizeY+2][matrixSizeX+2];
        for (int i=0;i<matrixSizeY+2;i++)
            for (int j=0;j<matrixSizeX+2;j++) {
                if (i == 0 || j == 0 || i == 10 || j == 10)
                    grid[i][j] = -1;
                else grid[i][j] = -2;
            }
        candidates=new Point[countNextBalls];
        for(int i=0;i<countNextBalls;i++)
            candidates[i]=new Point();

        sprites = loadBitmaps(R.drawable.class,context);
        for (GameSprite sprite:sprites)
            sprite.setBitmap(Bitmap.createScaledBitmap(sprite.getBitmap(), tileSize,tileSize, false));

        tileIndex=bitmapIndex("tile");

        generateNextBalls();
    }

    void reset(){
        matrix = new Cell [matrixSizeY][matrixSizeX];
        for (int i=0;i<matrixSizeY;i++)
            for (int j=0;j<matrixSizeX;j++){
                matrix[i][j]=new Cell(new Rect(
                        gameField.left+j*tileSize, gameField.top+i*tileSize,
                        gameField.left+j*tileSize+tileSize, gameField.top+i*tileSize+tileSize));
            }
        ballCount=0;
        grid=new int[matrixSizeY+2][matrixSizeX+2];
        for (int i=0;i<matrixSizeY+2;i++)
            for (int j=0;j<matrixSizeX+2;j++) {
                if (i == 0 || j == 0 || i == 10 || j == 10)
                    grid[i][j] = -1;
                else grid[i][j] = -2;
            }
        candidates=new Point[countNextBalls];
        for(int i=0;i<countNextBalls;i++)
            candidates[i]=new Point();
        generateNextBalls();
    }

    void update(ManagerGame _gm,ManagerInput _im,ManagerSound _sm){
        if (_gm.getGameState()==GameState.START) {
            for(int i=0;i<countNextBalls;i++) {
                matrix[candidates[i].y][candidates[i].x].setState(CellState.CONTAINS);
                ballCount++;
                _gm.addScore(checkLines(candidates[i].y,candidates[i].x));
                grid[candidates[i].y+1][candidates[i].x+1]=-1;
            }
            _gm.setGameState(GameState.GENERATE);
        } else if (_gm.getGameState()==GameState.GENERATE) {
            generateNextBalls();
            _gm.setGameState(GameState.GENERATED);
        } else if(_gm.getGameState()==GameState.MOVED){
            if(findPath(_im.getSelectedX(),_im.getSelectedY(),_im.getNewX(),_im.getNewY())) {
                _sm.playSound("move");
                String selectedColor = matrix[_im.getSelectedY()][_im.getSelectedX()].getColor();
                matrix[_im.getSelectedY()][_im.getSelectedX()].reset();
                grid[_im.getSelectedY() + 1][_im.getSelectedX() + 1] = -2;
                if (matrix[_im.getNewY()][_im.getNewX()].getState() == CellState.CANDIDATE) {
                    for (int i = 0; i < countNextBalls; i++)
                        if (candidates[i].equals(_im.getNewX(), _im.getNewY())) {
                            Point pos = getBallPosition(new Random());
                            String color = matrix[_im.getNewY()][_im.getNewX()].getColor();
                            candidates[i].set(pos.x, pos.y);
                            matrix[pos.y][pos.x].setBallCandidate(bitmapIndex(color), color);
                        }
                }
                matrix[_im.getNewY()][_im.getNewX()].setBall(bitmapIndex(selectedColor), selectedColor);
                grid[_im.getNewY() + 1][_im.getNewX() + 1] = -1;
                _gm.addScore(checkLines(_im.getNewY(),_im.getNewX()));
                _gm.setPathShown(false);
                _gm.setGameState(GameState.START);
            }else{
                _sm.playSound("blocked");
                grid[_im.getSelectedY() + 1][_im.getSelectedX() + 1] = -1;
                _gm.setGameState(GameState.GENERATED);
            }
            for (int i=1;i<matrixSizeY+1;i++)
                for (int j=1;j<matrixSizeX+1;j++)
                    if (grid[i][j]!=-1)
                        grid[i][j] = -2;
        }
        if (_gm.getScore()>_gm.getBestScore()){
            ActivityMain.editor.putInt("HiScore",_gm.getScore());
            ActivityMain.editor.commit();
        }
        if(ballCount>=matrixSizeX*matrixSizeY)
            _gm.setGlobalState(GlobalState.END);
    }

    private void generateNextBalls(){
        Random rand=new Random();
        Point pos;
        String color;
        int ballToGenerate=matrixSizeX*matrixSizeY-ballCount;
        if (ballToGenerate>=3)ballToGenerate=countNextBalls;
        for (int i=0;i<ballToGenerate;i++){
            pos=getBallPosition(rand);
            color=colors[rand.nextInt(colors.length)];
            matrix[pos.y][pos.x].setBallCandidate(bitmapIndex(color),color);
            candidates[i].set(pos.x,pos.y);
        }
    }
    private Point getBallPosition(Random rand){
        int x,y;
        do {
            y = rand.nextInt(matrixSizeY);
            x = rand.nextInt(matrixSizeX);
            if (matrix[y][x].getState()==CellState.EMPTY)
                return new Point(x,y);
        }while(true);
    }

    private int checkLines(final int row,final int col){
        int count;

        count=checkLineHorizontal(row,col);
        if (count!=0)return count;
        count=checkLineVertical(row,col);
        if (count!=0)return count;
        count=checkLineDiagonal(row,col);
        if (count!=0)return count;
        count=checkLineContradiagonal(row,col);
        if (count!=0)return count;

        return 0;
    }

    private int checkLineHorizontal(final int row,final int col){
        ArrayList<Point> line = new ArrayList<>();
        final String color = matrix[row][col].getColor();
        line.add(new Point(row, col));

        for (int i=col+1;i<matrixSizeX;i++) {
            if (matrix[row][i].getColor().equals(color))
                line.add(new Point(row,i));
            else break;
        }

        for (int i=col-1;i>=0;i--) {
            if (matrix[row][i].getColor().equals(color))
                line.add(new Point(row,i));
            else break;
        }
        final int count=line.size();
        if (count<5)return 0;
        else {
            for(Point ball:line){
                matrix[ball.x][ball.y].reset();
                grid[ball.x+1][ball.y+1]=-2;
            }
            ballCount-=count;
            return 5+factorial(count-5);
        }
    }
    private int checkLineVertical(final int row,final int col){
        ArrayList<Point> line = new ArrayList<>();
        final String color = matrix[row][col].getColor();
        line.add(new Point(col,row));

        for (int i=row+1;i<matrixSizeY;i++) {
            if (matrix[i][col].getColor().equals(color))
                line.add(new Point(col,i));
            else break;
        }

        for (int i=row-1;i>=0;i--) {
            if (matrix[i][col].getColor().equals(color))
                line.add(new Point(col,i));
            else break;
        }
        final int count=line.size();
        if (count<5)return 0;
        else {
            for(Point ball:line){
                matrix[ball.y][ball.x].reset();
                grid[ball.y+1][ball.x+1]=-2;
            }
            ballCount-=count;
            return 5+factorial(count-5);
        }
    }
    private int checkLineDiagonal(final int row,final int col){
        ArrayList<Point> line = new ArrayList<>();
        final String color = matrix[row][col].getColor();
        line.add(new Point(col,row));

        int i=row+1,j=col+1;
        while (i<matrixSizeY && j<matrixSizeX){
            if (matrix[i][j].getColor().equals(color))
                line.add(new Point(j, i));
            else break;
            i++;
            j++;
        }
        i=row-1;j=col-1;
        while (i>=0 && j>=0){
            if (matrix[i][j].getColor().equals(color))
                line.add(new Point(j, i));
            else break;
            i--;
            j--;
        }
        final int count=line.size();
        if (count<5)return 0;
        else {
            for(Point ball:line){
                matrix[ball.y][ball.x].reset();
                grid[ball.y+1][ball.x+1]=-2;
            }
            ballCount-=count;
            return 5+factorial(count-5);
        }
    }
    private int checkLineContradiagonal(final int row,final int col){
        ArrayList<Point> line = new ArrayList<>();
        final String color = matrix[row][col].getColor();
        line.add(new Point(col,row));

        int i=row+1,j=col-1;
        while (i<matrixSizeY && j>=0){
            if (matrix[i][j].getColor().equals(color))
                line.add(new Point(j, i));
            else break;
            i++;
            j--;
        }
        i=row-1;j=col+1;
        while (i>=0 && j<matrixSizeX){
            if (matrix[i][j].getColor().equals(color))
                line.add(new Point(j, i));
            else break;
            i--;
            j++;
        }
        final int count=line.size();
        if (count<5)return 0;
        else {
            for(Point ball:line){
                matrix[ball.y][ball.x].reset();
                grid[ball.y+1][ball.x+1]=-2;
            }
            ballCount-=count;
            return 5+factorial(count-5);
        }
    }
    private int factorial(int n){
        if (n==0)return 0;
        if (n==1)return 1;
        return n*factorial(n-1);
    }


    private boolean findPath(final int selectedX,final int selectedY,
                             final int newX,final int newY){
        boolean stop;
        int dy[] = {0,1,0,-1};
        int dx[] = {1,0,-1,0};
        int d = 0;
        //-2 - пусто
        //-1 - стена
        grid[selectedY+1][selectedX+1]=0;//старт
        do{
            stop=true;
            for(int y = 1; y<matrixSizeY+1;++y)
                for(int x = 1; x<matrixSizeX+1;++x)
                    if (grid[y][x]==d) {
                        for (int k = 0; k<4; ++k)
                            if (grid[y + dy[k]][x + dx[k]] == -2) {
                                stop = false;
                                grid[y + dy[k]][x + dx[k]] = d + 1;
                            }
                    }
            d++;
        }while (!stop && grid[newY+1][newX+1]==-2);
        if (grid[newY+1][newX+1]==-2)return false;
        int len = grid[newY+1][newX+1];            // длина кратчайшего пути из (ax, ay) в (bx, by)
        int y = newY;
        int x = newX;
        d = len;
        path = new Point[len+1];
        while ( d > 0 )
        {
            path[d] = new Point(x,y);// записываем ячейку (x, y) в путь
            d--;
            for (int k = 0; k < 4; ++k)
                if (grid[y + dy[k]+1][x + dx[k]+1] == d)
                {
                    y = y + dy[k];           // переходим в ячейку, которая на 1 ближе к старту
                    x = x + dx[k];
                    break;
                }
        }
        path[0] = new Point(selectedX,selectedY);
        return true;
    }

    private GameSprite[] loadBitmaps(Class<?> aClass, Context context)
            throws IllegalArgumentException{
        Field[] fields = aClass.getFields();

        ArrayList<GameSprite> res = new ArrayList<>();
        try {
            for(Field field:fields){
                if (field.getName().contains("lines_")) {
                    res.add(new GameSprite(
                            BitmapFactory.decodeResource(context.getResources(),
                                    field.getInt(null)), field.getName()));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException();
            /* Exception will only occur on bad class submitted. */
        }
        return res.toArray(new GameSprite[res.size()]);
    }
    int bitmapIndex(String name){
        name = name.replace(' ','_');
        for (int i=0;i<sprites.length;i++)
            if (sprites[i].getName().contains(name))
                return i;
        return -1;
    }

    public Rect getGameField() {return gameField;}
    public int getMatrixSizeX() {return matrixSizeX;}
    public int getMatrixSizeY() {return matrixSizeY;}
    public Cell[][] getMatrix() {return matrix;}
    public Cell getCell(int y,int x) {return matrix[y][x];}
    public int getGridCell(int y,int x) {return grid[y][x];}
    public Point getNextBall(int index) {return candidates[index];}
    public int getCountNextBalls() {return countNextBalls;}
    public int[][] getGrid() {return grid;}
    public Point[] getPath() {return path;}
    public GameSprite[] getSprites() {return sprites;}
    public GameSprite getSprite(int index) {return sprites[index];}
    public int getTileSize() {return tileSize;}
    public int getTileIndex() {return tileIndex;}

    public void setGameField(Rect gameField) {this.gameField = gameField;}
    public void setMatrix(Cell[][] matrix) {this.matrix = matrix;}
    public void setGrid(int[][] grid) {this.grid = grid;}
    public void setPath(Point[] path) {this.path = path;}
    public void setSprites(GameSprite[] sprites) {this.sprites = sprites;}
    public void setTileSize(int tileSize) {this.tileSize = tileSize;}
}
