public class Review {
    String reviewText;
    int reviewStars;
    int reviewVotes;
    int userVotes;
    double posReviewScore;
    double negReviewScore;

    public Review(String reviewText, int reviewStars, int reviewVotes,
            int userVotes) {
        this.reviewText = reviewText;
        this.reviewStars = reviewStars;
        this.reviewVotes = reviewVotes;
        this.userVotes = userVotes;
       
        Double userScore = Math.exp(-((Double)1.0/(this.userVotes+1)));

        this.posReviewScore = (double) reviewStars / 5.0;
        this.posReviewScore *= (Math.log10(10 + reviewVotes))*userScore;

        this.negReviewScore = (double) (reviewStars - 5.0) / 5.0;
        this.negReviewScore *= (Math.log10(10 + reviewVotes))*userScore;
    }
}