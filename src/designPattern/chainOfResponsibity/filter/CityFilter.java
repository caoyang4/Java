package src.designPattern.chainOfResponsibity.filter;

/**
 * @author caoyang
 */
public class CityFilter implements Filter<Comment>{
    @Override
    public Comment doFilter(Comment comment) {
        comment.setComment(comment.getComment().replace("Shanghai", "Hunan"));
        return comment;
    }
}
