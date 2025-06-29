import com.oocourse.spec3.main.MessageInterface;

public interface ForwardMessageInterface extends MessageInterface {
    //@ public instance model non_null int articleId;

    //@ public invariant socialValue == abs(articleId) % 200;
    //此处abs()表示取绝对值

    //@ ensures \result == articleId;
    public /*@ pure @*/ int getArticleId();
}
