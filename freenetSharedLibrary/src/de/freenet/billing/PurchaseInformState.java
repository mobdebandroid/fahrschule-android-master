package de.freenet.billing;

public enum PurchaseInformState {
	PENDING,
	COMPLETED;
	
	public static PurchaseInformState valueOf(int index) {
		PurchaseInformState[] values = PurchaseInformState.values();
        if (index < 0 || index >= values.length) {
            return PENDING;
        }
        return values[index];
    }
}
