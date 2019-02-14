package notation.android.com.tictactoe.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import notation.android.com.tictactoe.Model.Player;
import notation.android.com.tictactoe.R;


/**
 * For creating this class spent many human-hours
 */
public class GameActivity extends AppCompatActivity {

    /**
     * Field constant type of Strings with tags for logging code
     */
    private static final String TAG_INFORMATION = "InformationLog";

    /**
     * Fields of TextView for display nickname first and second user,
     * score and events (user disconnect, user win etc.)
     */
    private TextView mFirstPlayerName;
    private TextView mSecondPlayerName;
    private TextView mTextViewScore;
    private TextView mTestViewEvent;

    /**
     * Field of Buttons. In game uses as cells for X and O
     */
    private Button[] mButtons = new Button[9];

    /**
     * Fields of instance of Player class. Used for storage data user
     * such as nickname and sign ('X' or 'O')
     */
    private Player mPlayer;

    /**
     * Instance of class Socket. It is used for connect with server,
     * send requests and receive them
     */
    private Socket mSocket;

    /**
     * !!!ATTENTION!!!
     * Change the values to your local IP address!
     * Otherwise, the server will NOT connect
     * !!!ATTENTION!!!
     * <p>
     * Constant fields which contains local IP address
     */
    private final String LOCAL_IP = "192.168.0.106";

    /**
     * Fields for storage host. Also need to connect with server.
     */
    private final String HOST = "2288";

    /**
     * Constant field for connect to server.
     * Holds in itself host and local IP
     */
    private final String URI = "http://" + LOCAL_IP + ":" + HOST;

    /**
     * 2d array of int which contains combination of win tic tac toe game
     */
    private final int[][] winCombination = {
            {0, 1, 2}, {0, 4, 8}, {0, 3, 6},
            {1, 4, 7}, {2, 4, 6}, {2, 5, 8},
            {3, 4, 5}, {6, 7, 8}
    };

    /**
     * Variable that records the number of filled fields
     */
    private int filledCells = 0;

    /**
     * Overriding Android method.
     * <p>
     * Used in this app as main class. It initialized all views in
     * application, establish a connection between server and client.
     * Also receive requests from the server and processes them
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // get and set text which user wrote in previous activity
        String nickname = getIntent().getExtras().getString(MainActivity.NICKNAME_TAG);
        mPlayer = new Player(nickname);

        initTextViewAndButton();

        // init socket, connect and report server about joining first user
        try {
            mSocket = IO.socket(URI);
            mSocket.connect();
            mSocket.emit("join", mPlayer.getNickname());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        /*
         * This handles is only for current player
         *
         * Handles server receive about joined first user.
         *
         * Get from server sign of creator, set it to current user and
         * set unclickable all cells until another player connects
         */
        mSocket.on("creator", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String sign = (String) args[0];
                        mPlayer.setSign(sign);

                        setClickableCells(false);

                        Log.i(TAG_INFORMATION, "First user is joined");
                    }
                });
            }
        });

        /*
         * This handles for all connected players
         *
         * Handles server receive about joined second user.
         *
         * Get from server usernames, give it to method for set
         * text view with usernames and set all cells clickable.
         * Game started
         */
        mSocket.on("second_user", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String userOne = (String) args[0];
                        String userTwo = (String) args[1];
                        fillNames(userOne, userTwo);

                        setClickableCells(true);

                        Log.i(TAG_INFORMATION, "Second user joined. Game started");
                    }
                });
            }
        });

        /*
         * This handles for all connected users except sender receive
         *
         * Handles server receive about opponent's move.
         *
         * Get button id from server and init it. Incrementing int
         * of filled cells, then determine the sign of the opponent and
         * set it to current player. Then set cells clickable.
         * Current player can make a move.
         */
        mSocket.on("cell", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int buttonId = (int) args[0];
                        Button button = findViewById(buttonId);

                        filledCells++;

                        if (mPlayer.getSign().equals("X"))
                            button.setText("O");
                        else
                            button.setText("X");

                        setClickableCells(true);
                    }
                });
            }
        });

        /*
         * This handles for all connected users
         *
         * Handles server receive about victory of one of the players.
         *
         * Get username winner and score both players. Give username
         * of winner text view responsible for event in game. Then
         * set score of users into text view for score. Set unclickable cells
         * until the game announces the winner. Pause for 2 seconds
         * to announce the winner. Then clear all cells in game,
         * set it clickable and hide event text view
         */
        mSocket.on("userWin", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String nameWinner = String.valueOf(args[0]);
                        int winsFirstUser = (int) args[1];
                        int winsSecondUser = (int) args[2];

                        Log.i(TAG_INFORMATION, nameWinner + " win");

                        mTestViewEvent.setText(getString(R.string.winner, nameWinner));
                        mTestViewEvent.setVisibility(View.VISIBLE);

                        mTextViewScore.setText(getString(R.string.score,
                                winsFirstUser, winsSecondUser));

                        setClickableCells(false);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                clearCells();
                                setClickableCells(true);

                                mTestViewEvent.setVisibility(View.INVISIBLE);
                            }
                        }, 2000);
                    }
                });
            }
        });

        /*
         * This handles for all connected users
         *
         * Handles server receive about no winner is this round.
         *
         * Set text in text view event about no winner in round and set it
         * to visible mode. Pause for 2 seconds until the end of the round
         * without a winner. Then clears cells, set cells clickable and set
         * invisible text view for event.
         */
        mSocket.on("cells_full", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTestViewEvent.setText(R.string.no_winner);
                        mTestViewEvent.setVisibility(View.VISIBLE);

                        Log.i(TAG_INFORMATION, "There is no winner");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                clearCells();
                                setClickableCells(true);

                                mTestViewEvent.setVisibility(View.INVISIBLE);
                            }
                        }, 2000);
                    }
                });
            }
        });

        /*
         * This handles for all connected users except sender receive
         *
         * Handles server receive about user disconnect.
         *
         * Announces that the player has disconnected and makes current
         * player the creator of the game
         */
        mSocket.on("user_disconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTestViewEvent.setVisibility(View.VISIBLE);
                        mTestViewEvent.setText(R.string.user_disconnect);

                        fillDefaultTextView();
                        clearCells();
                        setClickableCells(false);

                        mPlayer.setSign("X");

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTestViewEvent.setVisibility(View.INVISIBLE);
                            }
                        }, 2000);
                    }
                });
            }
        });

        /*
         * This handles is only for current player
         *
         * Handles server receive about server is full.
         *
         * It makes it very lazy, just throws this user on the previous activity
         */
        mSocket.on("max_connections", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(
                                GameActivity.this, MainActivity.class));
                    }
                });
            }
        });
    }

    /**
     * The method is responsible for pressing any of the buttons.
     * <p>
     * It get button id from variable view and create instance of class Button.
     * Then checks the button for sign availability. If buttons is empty
     * set text to sign current player and disallow click on all buttons,
     * sends a request to server with id of pressed button.
     * I wonder if someone will read it? Then checks if current user win.
     * If true sends a request to server with current nickname.
     * And finally checks if the cells are filled. If true sends request to server.
     */
    public void pushButton(View view) {
        Button button = (Button) view;

        if (button.getText().toString().isEmpty()) {
            button.setText(mPlayer.getSign());
            setClickableCells(false);

            filledCells++;

            mSocket.emit("action", button.getId());
            if (checkWin(mPlayer.getSign())) {
                Log.i(TAG_INFORMATION, "You're win");
                mSocket.emit("win", mPlayer.getNickname());
            } else if (filledCells == mButtons.length) {
                mSocket.emit("no_winner");
            }
        }
    }

    /**
     * Method which iterates all cells is game and check it for winner.
     * <p>
     * At loop through each array in 2d array winCombination then create
     * auxiliary variable i with 0 value which will be incremented and
     * serve as index of each array aWinCombination in loop. In parameter
     * block if compare 3 buttons from array with prepared index
     * between incoming variable sign.
     *
     * @param sign variable of String which contains current sign player
     *             and use it for check winner
     * @return true if there is a winner, otherwise false
     */
    public boolean checkWin(String sign) {
        for (int[] aWinCombination : winCombination) {
            int j = 0;

            if (mButtons[aWinCombination[j++]].getText().equals(sign) &&
                    mButtons[aWinCombination[j++]].getText().equals(sign) &&
                    mButtons[aWinCombination[j]].getText().equals(sign)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method used to make all cells inactive.
     */
    public void setClickableCells(boolean clickable) {
        for (Button mButton : mButtons)
            mButton.setClickable(clickable);
    }

    /**
     * Method for clear all cells in game and reset variable for
     * number of filled cells.
     * <p>
     * It set value for each button null and reset filledCells.
     * Used if the game is over and you need to start a new.
     */
    public void clearCells() {
        for (Button mButton : mButtons)
            mButton.setText(null);

        filledCells = 0;
    }

    /**
     * Method set nickname first and second user in relevant textView
     *
     * @param firstUser  username first user which will be set in textView
     * @param secondUser username second user which will be set in textView
     */
    private void fillNames(String firstUser, String secondUser) {
        mFirstPlayerName.setText(firstUser);
        mSecondPlayerName.setText(secondUser);
    }

    /**
     * Method for initialization textView, array of buttons,
     * and fill default value in TextView.
     * Because I don't want to litter a method onCreate()
     */
    public void initTextViewAndButton() {
        mFirstPlayerName = findViewById(R.id.first_player);
        mSecondPlayerName = findViewById(R.id.second_player);
        mTextViewScore = findViewById(R.id.score);
        mTestViewEvent = findViewById(R.id.event_text);

        mButtons[0] = findViewById(R.id.button0);
        mButtons[1] = findViewById(R.id.button1);
        mButtons[2] = findViewById(R.id.button2);
        mButtons[3] = findViewById(R.id.button3);
        mButtons[4] = findViewById(R.id.button4);
        mButtons[5] = findViewById(R.id.button5);
        mButtons[6] = findViewById(R.id.button6);
        mButtons[7] = findViewById(R.id.button7);
        mButtons[8] = findViewById(R.id.button8);

        fillDefaultTextView();
    }

    /**
     * Method fills textViews default values.
     * <p>
     * At first set username current user then indicate that the second
     * player is not connected in secondPlayerName and set value by zero
     * in textViewScore
     */
    private void fillDefaultTextView() {
        mFirstPlayerName.setText(mPlayer.getNickname());
        mSecondPlayerName.setText(getString(R.string.second_player, "waiting..."));
        mTextViewScore.setText(getString(R.string.score, 0, 0));
    }

    /**
     * Overriding Android method
     * <p>
     * Send report server about disconnect, give in report current
     * player name and disconnect him from server
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.emit(mPlayer.getNickname());
        mSocket.disconnect();
    }
}