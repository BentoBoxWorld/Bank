package world.bentobox.bank.requests;

import java.math.BigDecimal;

public enum RequestAction {
    /**
     * Gets the player's island bank balance
     *
     * @return BigDecimal
     */
    GET_BALANCE(BigDecimal.ZERO),
    /**
     * Adds specific amount to the player's island bank balance
     *
     * @return new balance @BigDecimal
     */
    ADD_BALANCE(BigDecimal.ZERO),
    /**
     * Remove specific amount from the player's island bank balance
     *
     * @return new balance @BigDecimal
     */
    REMOVE_BALANCE(BigDecimal.ZERO),
    /**
     * Withdraw specific amount from bank balance to player's balance
     *
     * @return a boolean is withdrawn successfully
     */
    WITHDRAW(false),
    /**
     * Deposit specific amount from player's balance to bank
     *
     * @return a boolean is deposited successfully
     */
    DEPOSIT(false),
    /**
     * Resets specific player's island bank balance
     *
     * @return BigDecimal(the old balance)
     */
    RESET(BigDecimal.ZERO);

    public Object defaultValue;

    RequestAction(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
