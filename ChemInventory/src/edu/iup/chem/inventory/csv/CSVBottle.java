package edu.iup.chem.inventory.csv;

public class CSVBottle {
	private String	cas;
	private String	name;
	private String	amount;
	private String	room;

	private String	shelf;

	private int		quantity;

	public CSVBottle(final String cas, final String name, final String amount,
			final String room, final String shelf, final int quantity) {
		super();
		this.cas = cas;
		this.name = name;
		this.amount = amount;
		this.room = room;
		this.shelf = shelf;
		this.quantity = quantity;
	}

	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * @return the cas
	 */
	public String getCas() {
		return cas;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @return the room
	 */
	public String getRoom() {
		return room;
	}

	/**
	 * @return the shelf
	 */
	public String getShelf() {
		return shelf;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(final String amount) {
		this.amount = amount;
	}

	/**
	 * @param cas
	 *            the cas to set
	 */
	public void setCas(final String cas) {
		this.cas = cas;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @param room
	 *            the room to set
	 */
	public void setRoom(final String room) {
		this.room = room;
	}

	/**
	 * @param shelf
	 *            the shelf to set
	 */
	public void setShelf(final String shelf) {
		this.shelf = shelf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CSVBottle [" + (cas != null ? "cas=" + cas + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (amount != null ? "amount=" + amount + ", " : "")
				+ (room != null ? "room=" + room + ", " : "")
				+ (shelf != null ? "shelf=" + shelf + ", " : "") + "quantity="
				+ quantity + "]";
	}

}
