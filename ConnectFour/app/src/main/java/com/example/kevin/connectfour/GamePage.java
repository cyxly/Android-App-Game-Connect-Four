package com.example.kevin.connectfour;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.color.holo_green_light;

public class GamePage extends AppCompatActivity {
    final static int row = 6;
    final static int column = 7;

    private Context context;
    private boolean isClicked;
    Button btnRetract;
    private TextView tvTurn;
    private TextView tvTotalMove;
    private ImageView ivTurnCell;
    //declare for imageView (Cells) array
    private ImageView[][] ivCell = new ImageView[row][column];

    private Drawable[] drawCell = new Drawable[6];//0 is empty, 1 is player red, 2 is player green, 3 is background, 4 is red win, 5 is green win


//    private Button btnPlay;
//    private TextView tvTurn;

    private int[][] valueCell = new int[row][column];///0 is empty,1 is player,2 is bot
    private int winner_play;//who is winner? 0 is noone, 1 is player, 2 is bot
//    private boolean firstMove;
    private int xMove, yMove;//x and y axis of cell => define position of cell
    private int turnPlay;// whose turn?
    ArrayList moveList = new ArrayList();
    private int countMove;

    MediaPlayer mediaPlayer;
    SoundPool soundPool;
    HashMap<Integer, Integer> soundPoolMap;
    int icon = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_game_page);
        loadResources();
        designGameBoard();
        init_game();
        play_game();

        mediaPlayer = MediaPlayer.create(this, R.raw.bg);
        mediaPlayer.setLooping(true);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void init_game() {
        //this void will create UI before game start
        //for game control we need some variables
        moveList.clear();
        btnRetract = (Button)findViewById(R.id.Retract);
        btnRetract.setClickable(false);
        btnRetract.setBackgroundColor(Color.RED);
        tvTurn = (TextView)findViewById(R.id.tvTurn);
        tvTurn.setText("Turn");
        tvTotalMove = (TextView)findViewById(R.id.tvTotalMove);
        tvTotalMove.setText("");
        ivTurnCell = (ImageView)findViewById(R.id.ivTurnCell);
        ivTurnCell.setImageDrawable(drawCell[1]);
        countMove = 0;
//        firstMove = true;
        winner_play = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                ivCell[i][j].setImageDrawable(drawCell[0]);//default or Empty cell
                valueCell[i][j] = 0;
            }
        }
        /////////////////////////////////above is init for game
    }
    private void designGameBoard() {
        //create layoutparams to optimize size of cell
        // we create a horizotal linearlayout for a row
        // which contains maxN imageView in
        //need to find out size of cell first

        int sizeofCell = Math.round(ScreenWidth() / column);
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(sizeofCell * column, sizeofCell);
        LinearLayout.LayoutParams lpCell = new LinearLayout.LayoutParams(sizeofCell, sizeofCell);

        LinearLayout linBoardGame = (LinearLayout) findViewById(R.id.GameBoard);
        //create cells
        for (int i = 0; i < row; i++) {
            LinearLayout linRow = new LinearLayout(context);
            //make a row
            for (int j = 0; j < column; j++) {
                ivCell[i][j] = new ImageView(context);
                //make a cell
                //need to set background default for cell
                //cell has 3 status, empty(defautl),player,bot
                ivCell[i][j].setBackground(drawCell[3]);
                final int x = i;
                final int y = j;
                //make that for safe and clear
//                turnPlay = 1;
                ivCell[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean empty_cell_in_row = false;
                        int rows;
                        for (rows = row-1; rows >= 0; rows--){
                            if(valueCell[rows][y] == 0){
                                empty_cell_in_row = true;
                                break;
                            }
                        }
//                        if (valueCell[x][y] == 0) {//empty cell
                        if (empty_cell_in_row) {//empty cell
                            if (!isClicked) {//turn of player
                                Log.d("tuanh","click to cell ");
                                isClicked = true;
                                xMove = rows;
                                yMove = y;//i,j must be final variable
                                make_a_move();
//                                ivCell[xMove][yMove].setImageDrawable(drawCell[turnPlay]);
//                                valueCell[xMove][yMove] = turnPlay;
                            }
                        }
                    }
                });
                linRow.addView(ivCell[i][j], lpCell);
            }
            linBoardGame.addView(linRow, lpRow);
        }
    }

    private void loadResources() {
        drawCell[3] = context.getResources().getDrawable(R.drawable.empty_t);//background
        //copy 2 image for 2 drawable player and bot
        //edit it :D
        drawCell[0] = null;//empty cell
        drawCell[1] = context.getResources().getDrawable(R.drawable.red_t);//drawable for player red
        drawCell[2] = context.getResources().getDrawable(R.drawable.green_t);//for player green
        drawCell[4] = context.getResources().getDrawable(R.drawable.red_wint);//for red win
        drawCell[5] = context.getResources().getDrawable(R.drawable.green_wint);//for green win
    }

    private float ScreenWidth() {
        Resources resources = context.getResources();//ok
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    private void make_a_move() {
        btnRetract.setClickable(true);
        btnRetract.setBackgroundColor(Color.GREEN);
        Log.d("tuanh","make a move with "+xMove+";"+yMove+";"+turnPlay);
        moveList.add(String.valueOf(xMove)+String.valueOf(yMove)+String.valueOf(turnPlay));
//        Toast.makeText(this, ""+moveList, Toast.LENGTH_SHORT).show();
        ivCell[xMove][yMove].setImageDrawable(drawCell[turnPlay]);
        valueCell[xMove][yMove] = turnPlay;
        //check if anyone win
        //aw we forget 1 thing :D change the value of valuaCell
        //if no empty cell exist => draw
        if (noEmptycell()) {
            tvTurn.setText("Draw");
            tvTotalMove.setText("total move:" + countMove);
            ivTurnCell.setImageDrawable(drawCell[0]);
//            Toast.makeText(context, "Draw!!!", Toast.LENGTH_SHORT).show();
            return;
        } else if (CheckWinner()) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(300);
//            ivTurnCell.setImageDrawable(drawCell[0]);
            tvTurn.setText("Winner");
            ivTurnCell.setImageDrawable(drawCell[winner_play]);
            tvTotalMove.setText("total move:" + countMove);
            return;
        }

        if (turnPlay == 1) {//player
            turnPlay = (1 + 2) - turnPlay;
            ivTurnCell.setImageDrawable(drawCell[turnPlay]);
//            botTurn();
            player2Turn();
        } else {//bot
            turnPlay = 3 - turnPlay;
            ivTurnCell.setImageDrawable(drawCell[turnPlay]);
            playerTurn();
        }
    }

    private boolean noEmptycell() {
        for(int i=0;i<row;i++){
            for(int j=0;j<column;j++)
                if(valueCell[i][j]==0) return false;
        }
        return true;
    }

    private void playerTurn() {
        countMove++;
        Log.d("tuanh","player1 turn");
//        tvTurn.setText("Player");
//        firstMove=false;
        isClicked = false;
        /// we get xMove,yMove of player by the way listen click on cell so turn listen on
    }

    private void player2Turn() {
        countMove++;
        Log.d("tuanh","player2 turn");
//        tvTurn.setText("Player");
//        firstMove=false;
        isClicked = false;
        /// we get xMove,yMove of player by the way listen click on cell so turn listen on
    }

    private void play_game() {
        /*
        1. we need define who play first
         */
//        Random r = new Random();
//        turnPlay = r.nextInt(2) + 1;//r.nextint(2) return value in [0,1]
        turnPlay = 1;
        playerTurn();
    }

    private boolean CheckWinner() {
//        return false;
        //we only need to check the recent move xMove,yMove can create 5 cells in a row or not
        if(winner_play!=0) return true;
        //check in row =( i forget that :D
        VectorEnd(xMove,0,0,1,xMove,yMove);
        //check column
        VectorEnd(0,yMove,1,0,xMove,yMove);
        //check left to right
        if(xMove+yMove>=row-1){
            VectorEnd(row-1,xMove+yMove-row+1,-1,1,xMove,yMove);
        } else{
            VectorEnd(xMove+yMove,0,-1,1,xMove,yMove);
        }
        //check right to left
        if(xMove<yMove){
            VectorEnd(xMove-yMove+column-1,column-1,-1,-1,xMove,yMove);
        }else{
            VectorEnd(row-1,row-1-(xMove-yMove),-1,-1,xMove,yMove);
        }
        if(winner_play!=0)
            return true;
        else
            return false;
    }

    private void VectorEnd(int xx, int yy, int vx, int vy, int rx, int ry) {
        //this void will check the row base on vector(vx,vy) in range (rx,ry)-3*(vx,vy) -> (rx,ry)+3*(vx,vy)
//        if(winner_play!=0) return;
        final int range=3;
        int i,j;
        int xbelow=rx-range*vx;
        int ybelow=ry-range*vy;
        int xabove=rx+range*vx;
        int yabove=ry+range*vy;
        String st="";
        i=xx;j=yy;
        while(!inside(i,xbelow,xabove)||!inside(j,ybelow,yabove)){
            i+=vx;j+=vy;
        }
        while(true){
            int state = 0;
            st=st+String.valueOf(valueCell[i][j]);
            if(st.length()==4){
                state = EvalEnd(st);
                if(state != 0){
                    drawWinner(vx, vy, rx, ry);
                }
                st=st.substring(1,4);//substring of st from index 1->5;=> delete first character
            }
            i+=vx;j+=vy;
            if(!inBoard(i,j) || !inside(i,xbelow,xabove)|| !inside(j,ybelow,yabove) || state!=0){
                break;
            }
        }
    }

    private boolean inBoard(int i, int j) {
        //check i,j in board or not
        if(i<0||i>row-1||j<0||j>column-1) return false;
        return true;
    }

    private int EvalEnd(String st) {
        switch (st){
            case "1111": winner_play=1;return 1;
            case "2222": winner_play=2;return 2;
            default:return 0;
        }
    }

    private boolean inside(int i, int xbelow, int xabove) {//this check i in [xbelow,xabove] or not
        return (i-xbelow)*(i-xabove)<=0;
    }

    public void restartGame(View view){
        //to restart the game
        init_game();
        play_game();
    }

    public void retract(View view){
        //this func is to retract
        if(moveList.size() > 0){
            winner_play = 0;
            isClicked = false;
            String lastMove = moveList.get(moveList.size()-1).toString();
            moveList.remove(moveList.size()-1);
            int xMove = Integer.parseInt(""+lastMove.charAt(0));
            int yMove = Integer.parseInt(""+lastMove.charAt(1));
            int turnPlayer = Integer.parseInt(""+lastMove.charAt(2));
            ivCell[xMove][yMove].setImageDrawable(drawCell[0]);
            valueCell[xMove][yMove] = 0;
            this.turnPlay = turnPlayer;
            ivTurnCell.setImageDrawable(drawCell[turnPlayer]);
            countMove--;
        }
        if(moveList.size() == 0)
            btnRetract.setBackgroundColor(Color.RED);
    }

    public void drawWinner(int vx, int vy, int rx, int ry){
        //this is to draw change the pic when winner emerges
        //and once the winner emerges, cannot retract.
        btnRetract.setClickable(false);
        btnRetract.setBackgroundColor(Color.RED);
        for(int i=0; i < 4; i++){
            if(inBoard(rx+i*vx, ry+i*vy) && (valueCell[rx+i*vx][ry+i*vy]==valueCell[rx][ry])){
                if(turnPlay == 1)
                    ivCell[rx+i*vx][ry+i*vy].setImageDrawable(drawCell[4]);
                else
                    ivCell[rx+i*vx][ry+i*vy].setImageDrawable(drawCell[5]);
            }
            if(inBoard(rx+i*vx, ry+i*vy) && valueCell[rx+i*vx][ry+i*vy]!=valueCell[rx][ry])
                break;
        }
        for(int i=0; i > -4; i--){
            if(inBoard(rx+i*vx, ry+i*vy) && (valueCell[rx+i*vx][ry+i*vy]==valueCell[rx][ry])){
                if(turnPlay == 1)
                    ivCell[rx+i*vx][ry+i*vy].setImageDrawable(drawCell[4]);
                else
                    ivCell[rx+i*vx][ry+i*vy].setImageDrawable(drawCell[5]);
            }
            if(inBoard(rx+i*vx, ry+i*vy) && valueCell[rx+i*vx][ry+i*vy]!=valueCell[rx][ry])
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
//            Toast.makeText(this, "mediaPlayer.start()", Toast.LENGTH_SHORT).show();
        }
        Log.d("lifecycle","onResume invoked");
    }
    @Override
    protected void onPause() {
        super.onPause();
//        mediaPlayer = MediaPlayer.create(this, R.raw.bg);
//        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
//        soundPoolMap = new HashMap<Integer, Integer>();
//        soundPoolMap.put(soundID, soundPool.load(this, R.raw.fallbackring, 1));

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d("lifecycle","mediaPlayer.isPlaying()is stoped");
//            if (mediaPlayer != null) {
//                mediaPlayer.release();
//                Log.d("lifecycle","mediaPlayer is released");
//            }
//            Toast.makeText(this, "mediaPlayer.stop()", Toast.LENGTH_SHORT).show();
        }
        Log.d("lifecycle","onPause invoked");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.game_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(icon == 1){
            item.setIcon(R.drawable.ic_volume_off);
            icon = 2;
            mediaPlayer.pause();
        }
        else {
            item.setIcon(R.drawable.ic_volume_up);
            icon = 1;
            mediaPlayer.start();
        }
        return super.onOptionsItemSelected(item);
    }
}
