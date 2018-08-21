package com.ancientlore.lines;

class ManagerGame {
    private static volatile ManagerGame instance=null;
    private boolean debug = false;
    private boolean showNextOnGrid = true;
    private boolean showNextOnPanel = true;
    private boolean showPath =true;
    private boolean pathShown=true;
    private int score;
    private int bestScore;
    private GameState gameState;
    private GlobalState globalState;

    private ManagerGame(){}
    static ManagerGame getInstance(){
        if (instance==null) {
            synchronized(ManagerGame.class) {
                if (instance == null)
                    instance = new ManagerGame();
            }
        }
        return instance;
    }
    void initialize(){
        reset();
    }
    void reset(){
        score=0;
        gameState =GameState.START;
        globalState=GlobalState.PLAYING;
    }

    boolean isDebug(){return debug;}
    public boolean isShowNextOnGrid() {return showNextOnGrid;}
    public boolean isShowNextOnPanel() {return showNextOnPanel;}
    public boolean isShowPath() {return showPath;}
    public boolean isPathShown() {return pathShown;}
    public int getScore() {return score;}
    public int getBestScore() {return bestScore;}
    public GameState getGameState() {return gameState;}
    public GlobalState getGlobalState() {return globalState;}

    void setDebug(boolean value){debug=value;}
    public void setShowNextOnGrid(boolean showNextOnGrid) {this.showNextOnGrid = showNextOnGrid;}
    public void setShowNextOnPanel(boolean showNextOnPanel) {this.showNextOnPanel = showNextOnPanel;}
    public void setShowPath(boolean showPath) {this.showPath = showPath;}
    public void setPathShown(boolean pathShown) {this.pathShown = pathShown;}
    public void addScore(int var) {this.score += var;}
    public void setBestScore(int bestScore) {this.bestScore = bestScore;}
    public void setGameState(GameState gameState) {this.gameState = gameState;}
    public void setGlobalState(GlobalState globalState) {this.globalState = globalState;}
}
