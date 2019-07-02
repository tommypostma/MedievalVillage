package deusExMachina;

public class ExchangeRegulator {
	protected double[] exchangeRates = new double[AssetList.values().length];

	// General constructor
	public ExchangeRegulator(double exchangeRateTownProducts, double exchangeRateAgriProduce) {
		// Initializes all on zero
		for (AssetList asset : AssetList.values()) {
			exchangeRates[asset.ordinal()] = 0;
		}
		exchangeRates[AssetList.TOWN_PRODUCT.ordinal()] = exchangeRateTownProducts;
		exchangeRates[AssetList.AGRI_PRODUCE.ordinal()] = exchangeRateAgriProduce;
	}
	
	public void regulatesThe(Exchange offer) {
		offer.setPricePerUnit(setsExchangeRate(offer));
	}
	
	public double setsExchangeRate(Exchange offer) {
		double exchangeRate;
		switch (offer.getWhatIsGiven()) {
		case AGRI_PRODUCE:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				exchangeRate = exchangeRates[AssetList.AGRI_PRODUCE.ordinal()];
				break;
			default:
				exchangeRate = 0;
				break;
			}
			break;
		case TOWN_PRODUCT:
			switch (offer.getWhatIsTaken()) {
			case SILVER:
				exchangeRate = exchangeRates[AssetList.TOWN_PRODUCT.ordinal()];
				break;
			default:
				exchangeRate = 0;
				break;
			}
			break;
		default:// this is the default case of the outer switch
			exchangeRate = 0;
			break;
		}// the outer switch ends here
		return exchangeRate;
	}
}
