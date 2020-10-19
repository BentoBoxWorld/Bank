package world.bentobox.bank.data;

public enum TxType {
    /**
     * Deposit
     */
    DEPOSIT,
    /**
     * Withdrawal
     */
    WITHDRAW,
    /**
     * Admin give
     */
    GIVE,
    /**
     * Admin take
     */
    TAKE,
    /**
     * Admin Set
     */
    SET,
    /**
     * Unknown transaction type
     */
    UNKNOWN
}