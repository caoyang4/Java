package src.designPattern.chainOfResponsibity.filter;

public class NameFilter implements Filter<Comment>{
    @Override
    public Comment doFilter(Comment comment) {
        comment.setComment(comment.getComment().replace("caoyang", "young"));
        return comment;
    }
}
