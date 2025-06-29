public class ArticleNode {
    private int articleId;
    private ArticleNode prev;
    private ArticleNode next;

    public ArticleNode(int articleId) {
        this.articleId = articleId;
        this.next = null;
        this.prev = null;
    }

    public int getArticleId() {
        return articleId;
    }

    public ArticleNode getPrev() {
        return prev;
    }

    public ArticleNode getNext() {
        return next;
    }

    public void setPrev(ArticleNode prev) {
        this.prev = prev;
    }

    public void setNext(ArticleNode next) {
        this.next = next;
    }
}
