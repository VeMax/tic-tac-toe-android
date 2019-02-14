package notation.android.com.tictactoe.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import notation.android.com.tictactoe.R;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    /**
     * Static fields which contains key for intent
     */
    public static final String NICKNAME_TAG = "nickname";

    /**
     * Fields of EditText. Used for write name players
     */
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.EditTextNickname);
    }

    /**
     * The method responds to the join chat button.
     * <p>
     * Get nickname from edit text, then check if the
     * username is more than 4 characters, then pack
     * it into the intent and open a new activity
     */
    public void onClickEnter(View view) {
        String nickname = mEditText.getText().toString();

        if (!(nickname.length() < 4)) {
            Intent i = new Intent(MainActivity.this, GameActivity.class);
            i.putExtra(NICKNAME_TAG, nickname);

            startActivity(i);
        } else {
            Toast.makeText(this, R.string.alert_nick, LENGTH_LONG).show();
        }
    }
}