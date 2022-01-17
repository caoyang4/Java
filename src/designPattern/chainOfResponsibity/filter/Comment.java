package src.designPattern.chainOfResponsibity.filter;

/**
 * 评论类
 * @author caoyang
 */
public class Comment {
    /**
     * 评论信息
     */
    private String comment;

    public Comment() {
    }

    public Comment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                '}';
    }
}
