package src.designPattern.chainOfResponsibity.filter;

public class SensitiveFilter implements Filter<Comment>{

    @Override
    public Comment doFilter(Comment comment) {
        comment.setComment(comment.getComment().replace("996", "955"));
        return comment;
    }
}
