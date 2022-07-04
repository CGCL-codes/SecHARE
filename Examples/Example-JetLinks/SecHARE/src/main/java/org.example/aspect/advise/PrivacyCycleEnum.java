package org.example.aspect.advise;

public enum PrivacyCycleEnum {
    /**
     * Device data cycle step
     */
    STEP_DEFAULT(0),
    ADD_DEVICE(1),
    DEL_DEVICE(2),
    DEL_USER(3),
    UPDATE(4),
    GRANT_REVOKE_AUTH(5),
    GET(6),
    VERIFY(7);

    private final int index;

    PrivacyCycleEnum(int index) {
        this.index = index;
    }

    public int value() {
        return index;
    }
}
