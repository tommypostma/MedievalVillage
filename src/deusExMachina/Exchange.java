package deusExMachina;

public class Exchange {
	private AssetList whatIsGiven;
	private double quantityGiven;
	private AssetList whatIsTaken;
	private double quantityTaken;
	private double pricePerUnit;

	// this class is very simple, it contains only the data about transaction
	// what is exchanged, what quantities, and what is the exchange rate
	// in principle, everything can be exchanged for everything
	// silver is not really money, but just another asset
	public Exchange(AssetList given, AssetList taken) {
		whatIsGiven = given;
		whatIsTaken = taken;
	}

	// the most important method of this class is to set the price - a setter
	// method
	public void setPricePerUnit(double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	// the rest of the methods of this class are in fact only getters and
	// setters methods

	public void setQuantityGiven(double quantityGiven) {
		this.quantityGiven = quantityGiven;
	}

	public AssetList getWhatIsGiven() {
		return whatIsGiven;
	}

	public AssetList getWhatIsTaken() {
		return whatIsTaken;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public void setQuantityTaken(double quantityTaken) {
		this.quantityTaken = quantityTaken;
	}

	public double getQuantityGiven() {
		return quantityGiven;
	}

	public double getQuantityTaken() {
		return quantityTaken;
	}

}
