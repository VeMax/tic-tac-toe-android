package notation.android.com.tictactoe.Model;

public class Player {

    private String nickname;
    private String sign;

    public Player(String nickname) {
        this.nickname = nickname;
        // set default value for player
        this.sign = "O";
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}