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
     * @return the formatted invoice statement as a string
     * @throws RuntimeException if an unknown play type is encountered
     */
    public String statement() {
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer()
                        + System.lineSeparator());

        // build a line for each performance
        for (Performance p : invoice.getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).getName(),
                    usd(getAmount(p)),
                    p.getAudience()));
        }

        // footer lines
        result.append(String.format("Amount owed is %s%n",
                usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n",
                getTotalVolumeCredits()));
        return result.toString();
    }

    /**
     * Compute the cost in cents for a given performance.
     *
     * @param performance the performance for which the amount is calculated
     * @return the amount in cents for this performance
     * @throws RuntimeException if the play type is unknown
     */
    private int getAmount(Performance performance) {
        int result;

        switch (getPlay(performance).getType()) {

            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;

            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD);
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                        * performance.getAudience();
                break;

            case "history":
                result = Constants.HISTORY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.HISTORY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.HISTORY_AUDIENCE_THRESHOLD);
                }
                break;

            case "pastoral":
                result = Constants.PASTORAL_BASE_AMOUNT;
                if (performance.getAudience() > Constants.PASTORAL_AUDIENCE_THRESHOLD) {
                    result += Constants.PASTORAL_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.PASTORAL_AUDIENCE_THRESHOLD);
                }
                break;

            default:
                throw new RuntimeException(
                        String.format("unknown type: %s",
                                getPlay(performance).getType()));
        }

        return result;
    }

    /**
     * Compute the volume credits earned for a single performance.
     *
     * @param performance the performance for which volume credits are computed
     * @return the number of credits earned for this performance
     * @throws RuntimeException if the play type is unknown
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;

        switch (getPlay(performance).getType()) {

            case "tragedy":
                result += Math.max(performance.getAudience()
                        - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
                break;

            case "comedy":
                result += Math.max(performance.getAudience()
                        - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
                result += performance.getAudience()
                        / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
                break;

            case "history":
                result += Math.max(performance.getAudience()
                        - Constants.HISTORY_VOLUME_CREDIT_THRESHOLD, 0);
                break;

            case "pastoral":
                result += Math.max(performance.getAudience()
                        - Constants.PASTORAL_VOLUME_CREDIT_THRESHOLD, 0);
                result += performance.getAudience() / 2;
                break;

            default:
                throw new RuntimeException(String.format("unknown type: %s",
                        getPlay(performance).getType()));
        }

        return result;
    }

    /**
     * Calculate the total amount in cents for all performances in the invoice.
     *
     * @return the total amount in cents for this invoice
     */
    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    /**
     * Calculate the total volume credits for all performances in the invoice.
     *
     * @return the total volume credits earned for this invoice
     */
    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }

    /**
     * Returns the Play associated with the given performance.
     *
     * @param performance the performance whose play is needed
     * @return the Play associated with this performance
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Format an amount in cents as a US currency string.
     *
     * @param amount the amount in cents to format
     * @return the formatted currency string
     */
    private String usd(int amount) {
        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        return frmt.format(amount / Constants.PERCENT_FACTOR);
    }
}
