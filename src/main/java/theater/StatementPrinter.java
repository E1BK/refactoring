package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Construct a new StatementPrinter for the given invoice and plays.
     *
     * @param invoice the invoice to print
     * @param plays   the mapping from play identifiers to play information
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer()
                        + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());

            int thisAmount;
            switch (play.getType()) {
                case "tragedy":
                    thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
                    if (p.getAudience()
                            > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                        thisAmount +=
                                Constants
                                        .TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                                * (p.getAudience()
                                        - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                    }
                    break;
                case "comedy":
                    thisAmount = Constants.COMEDY_BASE_AMOUNT;
                    if (p.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                        thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                                + (Constants
                                .COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                                * (p.getAudience()
                                - Constants.COMEDY_AUDIENCE_THRESHOLD));
                    }
                    thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                            * p.getAudience();
                    break;
                case "history":
                    thisAmount = Constants.HISTORY_BASE_AMOUNT;
                    if (p.getAudience() > Constants
                            .HISTORY_AUDIENCE_THRESHOLD) {
                        thisAmount += Constants
                                .HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                                * (p.getAudience()
                                - Constants.HISTORY_AUDIENCE_THRESHOLD);
                    }
                    break;

                case "pastoral":
                    thisAmount = Constants.PASTORAL_BASE_AMOUNT;
                    if (p.getAudience() > Constants
                            .PASTORAL_AUDIENCE_THRESHOLD) {
                        thisAmount += Constants
                                .PASTORAL_OVER_BASE_CAPACITY_PER_PERSON
                                * (p.getAudience()
                                - Constants.PASTORAL_AUDIENCE_THRESHOLD);
                    }
                    break;
                default:
                    throw new RuntimeException(String.format("unknown type: %s",
                            play.getType()));
            }

            // add volume credits
            switch (play.getType()) {
                case "tragedy":
                    volumeCredits += Math.max(p.getAudience()
                            - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
                    break;

                case "comedy":
                    volumeCredits += Math.max(p.getAudience()
                            - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
                    volumeCredits += p.getAudience()
                            / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
                    break;

                case "history":
                    volumeCredits += Math.max(p.getAudience()
                            - Constants.HISTORY_VOLUME_CREDIT_THRESHOLD, 0);
                    break;

                case "pastoral":
                    volumeCredits += Math.max(p.getAudience()
                            - Constants.PASTORAL_VOLUME_CREDIT_THRESHOLD, 0);
                    volumeCredits += p.getAudience() / 2;
                    break;

                default:
                    throw new RuntimeException(String.format("unknown type: %s",
                            play.getType()));
            }

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    play.getName(),
                    frmt.format(thisAmount / Constants.PERCENT_FACTOR),
                    p.getAudience()));
            totalAmount += thisAmount;
        }
        result.append(String.format("Amount owed is %s%n",
                frmt.format(totalAmount / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }
}
