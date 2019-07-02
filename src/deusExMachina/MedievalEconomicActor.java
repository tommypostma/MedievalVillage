package deusExMachina;

public abstract class MedievalEconomicActor {

	protected double[] assets = new double[AssetList.values().length];
	private int adults;

	public MedievalEconomicActor() {
		for (AssetList asset : AssetList.values()) {
			assets[asset.ordinal()] = 0;
		}
	}

	public abstract Exchange offers(AssetList what, AssetList whatFor);
	public abstract void acceptsPartOrWholeOf(Exchange offer);
	public abstract void settlesTransactionOver(Exchange exchange);
	public abstract void reportsGoods();
	public abstract void reportsSilver();
	public abstract void reportsFood();
	
	public int howManyAdults() {
		return adults;
	}

	public void setTheNumberOfAdults(int adults) {
		this.adults = adults;
	}
	
	public double acceptAsMuchAsICanPay(Exchange offer, double reserve) {
		double price = offer.getQuantityGiven() * offer.getPricePerUnit();
		if ( (assets[offer.getWhatIsTaken().ordinal()] - reserve) > price ) {
			// We have enough to pay for all, so except all
			return offer.getQuantityGiven();
		} else {
			// Otherwise, divide what we have (minus reserve) by what it costs, to get the most we can afford
			return (assets[offer.getWhatIsTaken().ordinal()] - reserve) / offer.getPricePerUnit();
		}
	}
	
	
	public double acceptAsMuchAsINeed(Exchange offer, double reserve, double initialStock, double safetyLevel, double restockLevel) {
		if ( (assets[offer.getWhatIsGiven().ordinal()]) < (initialStock * safetyLevel) ) {
			// If we have less than our safety level order our restock level (or what is given)
			offer.setQuantityGiven(Math.min(offer.getQuantityGiven(), initialStock * restockLevel));
		} else {
			// If we have enough don't order anything
			offer.setQuantityGiven(0);
		}
		// Make sure we don't spend more than we have
		return acceptAsMuchAsICanPay(offer, reserve);
	}
	
	public double getStockOf(AssetList asset) {
		return assets[asset.ordinal()];
	}
	
	public String assetsToCsv() {
		String assets = "";
		for (AssetList asset : AssetList.values()) {
			if (asset.ordinal() < (AssetList.values().length - 1)) {
				assets = assets + getStockOf(asset) + ", ";
			} else {
				assets = assets + getStockOf(asset) + "";
			}
		}
		return assets;
	}
}
